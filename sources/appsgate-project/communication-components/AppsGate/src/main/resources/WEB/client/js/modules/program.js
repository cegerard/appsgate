define([
	"jquery",
	"underscore",
	"backbone",
	"grammar",
	"text!templates/program/menu/menu.html",
	"text!templates/program/menu/programContainer.html",
	"text!templates/program/menu/addButton.html",
	"text!templates/devices/menu/coreClockContainer.html",
	"text!templates/program/editor/editor.html"
], function($, _, Backbone, Grammar, programMenuTemplate, programContainerMenuTemplate, addProgramButtonTemplate, coreClockContainerMenuTemplate, programEditorTemplate) {
	// initialize the module
	var Program = {};

	// router
	Program.Router = Backbone.Router.extend({
		routes : {
			"programs"			: "list",
			"programs/:id"		: "details"
		},

		list:function() {
			// display the side menu
			appRouter.showMenuView(new Program.Views.Menu());
			
			// set active the first element - displayed by default
			$($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).addClass("active");
			
			// display the first program
			appRouter.showView(new Program.Views.Editor({ model : programs.at(0) }));
			
			// update the url if there is at least one program
			if (programs.length > 0) {
				appRouter.navigate("#programs/" + programs.at(0).get("id"));
			}
		},
		
		details:function(id) {
			appRouter.showView(new Program.Views.Editor({ model : programs.get(id) }));
		}
	});

	// instantiate the router
	var router = new Program.Router();

	// model
	Program.Model = Backbone.Model.extend({
		// default values
		defaults: {
			runningState : "DEPLOYED",
			userInputSource : "",
			source : {
				programName : "",
				seqParameters : [],
				author : "",
				target : "",
				daemon : "false",
				seqDefinitions : [],
				seqRules : [
					[]
				]
			}
		},
		
		/**
		 * Extract the name and the daemon attributes from the source to simplify their usage w/ backbone and in the templates
		 * 
		 * @constructor
		 */
		initialize:function() {
			var self = this;
			
			// name
			if (typeof this.get("name") === "undefined") {
				this.set("name", this.get("source").programName);
			} else {
				this.get("source").programName = this.get("name");
			}
			
			// daemon
			if (typeof this.get("daemon") === "undefined") {
				this.set("daemon", this.get("source").daemon);
			}
			
			// when the source has been updated, update the attributes of the program model
			this.on("change:source", function() {
				this.set("name", this.get("source").programName);
				this.set("daemon", this.get("source").daemon);
			});
			
			// each program listens to the event whose id corresponds to its own id
			dispatcher.on(this.get("id"), function(updatedVariableJSON) {
				self.set(updatedVariableJSON.varName, updatedVariableJSON.value);
			});
		},
		
		/**
		 * Send a message to the server to perform a remote call
		 * 
		 * @param method Remote method name to call
		 * @param args Array containing the argument taken by the method. Each entry of the array has to be { type : "", value "" }
		 */
		remoteCall:function(method, args) {
			communicator.sendMessage({
				method	: method,
				args	: args
			});
		},

		// override its synchronization method to send a notification on the network
		sync:function(method, model) {
			switch (method) {
				case "create":
					// create an id to the program
					var id;
					do {
						id = "program-" + Math.round(Math.random() * 10000).toString();
					} while (programs.where({ id : id }).length > 0);
					model.set("id", id);
					
					this.remoteCall("addProgram", [{ type : "JSONObject", value : model.toJSON() }]);
					break;
				case "delete":
					this.remoteCall("removeProgram", [{ type : "String", value : model.get("id") }]);
					break;
				case "update":
					if (model.changedAttributes()) {
						_.keys(model.changedAttributes()).forEach(function(attribute) {
							if (attribute === "runningState") {
								if (model.get("runningState") === "STARTED") {
									model.remoteCall("callProgram", [{ type : "String", value : model.get("id") }]);
								} else {
									model.remoteCall("stopProgram", [{ type : "String", value : model.get("id") }]);
								}
							} else {
								model.remoteCall("updateProgram", [{ type : "JSONObject", value : model.toJSON() }]);
							}
						});
					} else {
						model.remoteCall("updateProgram", [{ type : "JSONObject", value : model.toJSON() }]);
					}
					break;
			}
		},
		
		/**
		 * Converts the model to its JSON representation
		 */
		toJSON:function() {
			return {
				id				: this.get("id"),
				runningState	: this.get("runningState"),
				source			: this.get("source"),
				userInputSource	: this.get("userInputSource")
			}
		}
	});

	// collection
	Program.Collection = Backbone.Collection.extend({
		model: Program.Model,

		initialize:function() {
			var self = this;
			
			// sort the programs alphabetically
			this.comparator = function(location) {
				return location.get("name");
			};

			// listen to the event when the list of programs is received
			dispatcher.on("listPrograms", function(programs) {
				_.each(programs, function(program) {
					self.add(program);
				});
				
				dispatcher.trigger("programsReady");
			});

			// listen to the event when a program appears and add it
			dispatcher.on("newProgram", function(program) {
				self.add(program);
				
				// update the grammar to take the new program in consideration
				if (typeof window.grammar !== "undefined") {
					delete window.grammar;
				}
				window.grammar = new Grammar();
			});
			
			// listen to the event when a program has been removed
			dispatcher.on("removeProgram", function(program) {
				var removedProgram = programs.get(program.id);
				programs.remove(removedProgram);
				
				// update the grammar to remove the program from the grammar
				if (typeof window.grammar !== "undefined") {
					delete window.grammar;
				}
				window.grammar = new Grammar();
			});
			
			// listen to the event when a program has been updated
			dispatcher.on("updateProgram", function(program) {
				programs.get(program.id).set(program);
			});

			// send the request to fetch the programs
			communicator.sendMessage({
				method: "getPrograms",
				args: [],
				callId: "listPrograms"
			});
		}
	});

	/**
	 * Namespace for the views
	 */
	Program.Views = {};
	
	/**
	 * Render the side menu for the programs
	 */
	Program.Views.Menu = Backbone.View.extend({
		tpl						: _.template(programMenuTemplate),
		tplProgramContainer		: _.template(programContainerMenuTemplate),
		tplAddProgramButton		: _.template(addProgramButtonTemplate),
		tplCoreClockContainer	: _.template(coreClockContainerMenuTemplate),
		
		/**
		 * Bind events of the DOM elements from the view to their callback
		 */
		events : {
			"click a.list-group-item"						: "updateSideMenu",
			"show.bs.modal #add-program-modal"				: "initializeModal",
			"hidden.bs.modal #add-program-modal"			: "toggleModalValue",
			"click #add-program-modal button.valid-button"	: "validAddProgram",
			"keyup #add-program-modal input:text"			: "validAddProgram",
			"click button.start-program-button"				: "onStartProgramButton",
			"click button.stop-program-button"				: "onStopProgramButton"
		},
		
		/**
		 * Listen to the programs collection updates and refresh if any
		 * 
		 * @constructor
		 */
		initialize:function() {
			this.listenTo(programs, "add", this.render);
			this.listenTo(programs, "remove", this.render);
			this.listenTo(programs, "change", this.render);
			this.listenTo(devices.getCoreClock(), "change", this.render);
		},
		
		/**
		 * Update the side menu to set the correct active element
		 * 
		 * @param e JS click event
		 */
		updateSideMenu:function(e) {
			_.forEach($("a.list-group-item"), function(item) {
				$(item).removeClass("active");
			});
			
			if (typeof e !== "undefined") {
				$(e.currentTarget).addClass("active");
			} else {
				$("#side-" + Backbone.history.fragment.split("/")[1]).addClass("active");
			}
		},
		
		/**
		 * Clear the input text, hide the error message, check the checkbox and disable the valid button by default
		 */
		initializeModal:function() {
			$("#add-program-modal input").val("");
			$("#add-program-modal .text-danger").addClass("hide");
			$("#add-program-modal input:checkbox").prop("checked", true);
			$("#add-program-modal .valid-button").addClass("disabled");
			
			// tell the router that there is a modal
			appRouter.isModalShown = true;
		},
		
		/**
		 * Tell the router there is no modal anymore
		 */
		toggleModalValue:function() {
			appRouter.isModalShown = false;
		},
		
		/**
		 * Check the current value of the input text and show an error message if needed and activate or disactivate the valid button
		 * 
		 * @return false if the typed name already exists, true otherwise
		 */
		checkProgramName:function() {
			// name is empty
			if ($("#add-program-modal input:text").val() === "") {
				$("#add-program-modal .text-danger")
						.text($.i18n.t("modal-add-program.name-empty"))
						.removeClass("hide");
				$("#add-program-modal .valid-button").addClass("disabled");
				
				return false;
			}
			
			// name already exists
			if (programs.where({ name : $("#add-program-modal input:text").val() }).length > 0) {
				$("#add-program-modal .text-danger")
						.text($.i18n.t("modal-add-program.name-already-existing"))
						.removeClass("hide");
				$("#add-program-modal .valid-button").addClass("disabled");
				
				return false;
			}
			
			// ok
			$("#add-program-modal .text-danger").addClass("hide");
			$("#add-program-modal .valid-button").removeClass("disabled");
			
			return true;
		},
		
		/**
		 * Check if the name of the program does not already exist. If not, create the program
		 * Hide the modal when done
		 * 
		 * @param e JS event
		 */
		validAddProgram:function(e) {
			if (e.type === "keyup" && e.keyCode === 13 || e.type === "click") {
				// create the program if the name is ok
				if (this.checkProgramName()) {
					
					// instantiate the program and add it to the collection after the modal has been hidden
					$("#add-program-modal").on("hidden.bs.modal", function() {
						// tell the router there is no modal any more
						appRouter.isModalShown = false;
						
						// instantiate a model for the new program
						var program = new Program.Model({
							name	: $("#add-program-modal input:text").val(),
							daemon	: "false"
						});

						// send the program to the backend
						program.save();

						// add it to the collection
						programs.add(program);
					});
					
					// hide the modal
					$("#add-program-modal").modal("hide");
				}
			} else if (e.type === "keyup") {
				this.checkProgramName();
			}
		},
		
		/**
		 * Callback to start a program
		 * 
		 * @param e JS mouse event
		 */
		onStartProgramButton:function(e) {
			e.preventDefault();
			
			// get the program to start
			var program = programs.get($(e.currentTarget).attr("id"));
			
			// change its running state
			program.set("runningState", "STARTED");
			
			// send modification to the backend
			program.save();
			
			// refresh the menu
			this.render();
			
			return false;
		},
		
		/**
		 * Callback to stop a program
		 * 
		 * @param e JS mouse event
		 */
		onStopProgramButton:function(e) {
			e.preventDefault();
			
			// get the program to stop
			var program = programs.get($(e.currentTarget).attr("id"))
			
			// change its running state
			program.set("runningState", "STOPPED");
			
			// send modification to the backend
			program.save();
			
			// refresh the menu
			this.render();
			
			return false;
		},
		
		/**
		 * Render the side menu
		 */
		render:function() {
			if (!appRouter.isModalShown) {
				var self = this;

				// initialize the content
				this.$el.html(this.tpl());

				// put the time on the top of the menu
				$(this.$el.find(".list-group")[0]).append(this.tplCoreClockContainer({
					device	: devices.getCoreClock(),
					active	: Backbone.history.fragment === "devices/" + devices.getCoreClock().get("id") ? true : false
				}));

				// for each program, add a menu item
				this.$el.append(this.tpl());
				programs.forEach(function(program) {
					$(self.$el.find(".list-group")[1]).append(self.tplProgramContainer({
						program : program,
						active	: Backbone.history.fragment.split("/programs")[1] === program.get("name") ? true : false
					}));
				});

				// "add program" button to the side menu
				this.$el.append(this.tplAddProgramButton());

				// set active the current menu item
				this.updateSideMenu();
				
				// translate the view
				this.$el.i18n();

				return this;
			}
		}

	});
	
	/**
	 * Render the editor view
	 */
	Program.Views.Editor = Backbone.View.extend({
		tplEditor : _.template(programEditorTemplate),
		
		events : {
			"click button.save-program-button"							: "onSaveProgramButton",
			"click button.delete-program-button"						: "onDeleteProgramButton",
			"keyup textarea"											: "onKeyUpTextarea",
			"click .expected-elements > button.completion-button"		: "onClickCompletionButton",
			"click .programInput > span"								: "onClickSourceElement",
			"click button.valid-value"									: "onValidValueButton",
			"click button.deleted-elements"								: "onClickDeletedElements",
			"click button.value-popover-button"							: "onClickValuePopoverButton",
			"click button.valid-value-popover-button"					: "onClickValidValuePopoverButton",
			"click button.device-popover-button"						: "onClickDevicePopoverButton",
			"click button.delete-popover-button"						: "onClickDeletePopoverButton",
			"click button.close-popover-button"							: "onClickClosePopoverButton"
		},
		
		/**
		 * @constructor
		 */
		initialize:function() {
			if (typeof this.model !== "undefined") {
				this.userInputSource = this.model.get("name") + " " + $.i18n.t("language.written-by") + " Bob pour Alice ";
			}
		},
		
		/**
		 * Callback when the user has clicked on the button to save modifications done on a program. Send the update to the server
		 */
		onSaveProgramButton:function() {
			this.model.save();
			
			// hide the deleted elements
			if (!$(".deleted-elements").hasClass("hidden")) {
				$(".deleted-elements").addClass("hidden");
			}
		},
		
		/**
		 * Callback when the user has clicked on the button to remove a program. Remove the program
		 */
		onDeleteProgramButton:function() {
			// delete the program
			this.model.destroy();
			
			// navigate to the list of programs
			appRouter.navigate("#programs", { trigger : true });
		},
		
		onKeyUpTextarea:function() {
			this.compileProgram();
		},
		
		onClickCompletionButton:function(e) {
			if ($(e.currentTarget).text() === $.i18n.t("language.space")) {
				// $(".programInput").append(" ");
			} else {
				$(".programInput").append($(e.currentTarget).html());
			}
			this.compileProgram();
		},

		onClickSourceElement:function(e) {
			var i = $(".programInput").children().toArray().indexOf(e.currentTarget);
			var before = $(".programInput").children().splice(0, i);
			var after = $(".programInput").children().splice(i + 1, $(".programInput").children().length);
			
			// destroy the current popover if needed to avoid conflicts
			$(e.currentTarget)
					.removeAttr("data-original-title")
					.removeAttr("title");
			$(e.currentTarget).popover("destroy");
			$(".programInput .popover").remove();
			
			// user wants to edit a value
			if ($(e.currentTarget).hasClass("value")) {
				var input = $("<div>");
				input.append(this.model.get("name") + " " + $.i18n.t("language.written-by") + " Bob pour Alice ");
				before.forEach(function(span) {
					$(span).clone().appendTo(input);
				});
				try {
					grammar.parse(input.html().replace(/"/g, "'"));
				} catch (exception) {
					var content = $("<span>");
					
					exception.expected.forEach(function(nextPossibility) {
						if (nextPossibility.indexOf("input") === -1) {
							content.append("<button class='btn btn-primary value-popover-button'>" + nextPossibility.replace(/"/g, "").replace(/\\/g, "") + "</button><br>");
						} else {
							content.append($(nextPossibility).find("input"));
							content.find("input").val($(e.currentTarget).text());
							content.append("<button class='btn btn-success valid-value-popover-button'>" + $.i18n.t("form.valid-button") + "</button>");
						}
					});
					content.append("<button class='btn btn-danger delete-popover-button'>" + $.i18n.t("form.delete-button") + "</button>");
					content.append("<button class='btn btn-info close-popover-button'>" + $.i18n.t("form.cancel-button") + "</button>");
					
					$(e.currentTarget).popover({
						html		: true,
						title		: "Changer valeur",
						content		: content,
						placement	: "bottom"
					});
					
					this.valueToReplace = $(e.currentTarget);
					$(e.currentTarget).popover("show");
				}
			} else if ($(e.currentTarget).hasClass("device-name")) {
				var title = "";
				var content = $("<span>");
				
				if ($(e.currentTarget).attr("data-device-type") === "program") {
					title = "Changer programme";
					
					programs.forEach(function(p) {
						content.append("<button class='btn btn-primary device-popover-button'>" + p.get("name") + "</button>")
					});
				} else {
					title = "Changer dispositif";
					
					var possibleDevices = devices.where({ type : parseInt($(e.currentTarget).attr("data-device-type")) });
					possibleDevices.forEach(function(d) {
						content.append("<button class='btn btn-primary device-popover-button'>" + d.get("name") + "</button>");
					});
				}
				
				content.append("<button class='btn btn-danger delete-popover-button'>" + $.i18n.t("form.delete-button") + "</button>");
				content.append("<button class='btn btn-info close-popover-button'>" + $.i18n.t("form.cancel-button") + "</button>");
				
				$(e.currentTarget).popover({
					html		: true,
					title		: title,
					content		: content,
					placement	: "bottom"
				});
				
				this.valueToReplace = $(e.currentTarget);
				$(e.currentTarget).popover("show");
			} else {
				$(".deleted-elements").html("");
				after.forEach(function(element) {
					$(".deleted-elements").append(element);
				});

				$(e.currentTarget).remove();
				$(".deleted-elements").removeClass("hidden");

				this.compileProgram();
			}
		},
		
		onValidValueButton:function() {
			$(".programInput").append("<span class='value'>" + $(".expected-elements input").val() + "</span>");
			this.compileProgram();
		},
		
		onClickDeletedElements:function() {
			$(".programInput").append($("button.deleted-elements").html());
			$("button.deleted-elements").addClass("hidden");
			this.compileProgram();
		},
		
		onClickValuePopoverButton:function(e) {
			var self = this;
			this.valueToReplace.text($(e.currentTarget).text());
			
			this.valueToReplace.on("hidden.bs.popover", function() {
				self.valueToReplace
						.removeAttr("data-original-title")
						.removeAttr("title");
				$(".programInput .popover").remove();
				self.compileProgram();
			});
			
			this.valueToReplace.popover("destroy");
		},
		
		onClickValidValuePopoverButton:function() {
			var self = this;
			this.valueToReplace.text($(".programInput > .popover input").val());
			
			this.valueToReplace.on("hidden.bs.popover", function() {
				self.valueToReplace
						.removeAttr("data-original-title")
						.removeAttr("title");
				$(".programInput .popover").remove();
				self.compileProgram();
			});
			
			this.valueToReplace.popover("destroy");
		},
		
		onClickDevicePopoverButton:function(e) {
			var self = this;
			this.valueToReplace.text($(e.currentTarget).text());
			
			this.valueToReplace.on("hidden.bs.popover", function() {
				self.valueToReplace
						.removeAttr("data-original-title")
						.removeAttr("title");
				$(".programInput .popover").remove();
				self.compileProgram();
			});
			
			this.valueToReplace.popover("destroy");
		},
		
		onClickDeletePopoverButton:function() {
			var self = this;
			 //.indexOf(this.valueToReplace));
			
			this.valueToReplace.on("hidden.bs.popover", function() {
				self.valueToReplace
						.removeAttr("data-original-title")
						.removeAttr("title");
				$(".programInput .popover").remove();
				
				var after = $(".programInput").children().toArray().slice($(".programInput").children().index(self.valueToReplace) + 1, $(".programInput").children().length);
				
				$(".deleted-elements").html("");
				after.forEach(function(element) {
					$(".deleted-elements").append(element);
				});
				$(".deleted-elements").removeClass("hidden");
				
				self.valueToReplace.remove();
				
				self.compileProgram();
			});
			
			this.valueToReplace.popover("destroy");
		},
		
		onClickClosePopoverButton:function() {
			var self = this;
			
			this.valueToReplace.on("hidden.bs.popover", function() {
				self.valueToReplace
						.removeAttr("data-original-title")
						.removeAttr("title");
				$(".programInput .popover").remove();
			});
			
			this.valueToReplace.popover("destroy");
		},

		compileProgram:function() {
			// build the beginning of the user input source to be given to the parser
			var programInput = this.model.get("name") + " " + $.i18n.t("language.written-by") + " Bob pour Alice ";
			programInput += $(".programInput").html();
			programInput = programInput.replace(/"/g, "'");
			
			console.log(programInput);

			// clear the error span
			$(".expected-elements").html("");

			try {
				var ast = grammar.parse(programInput);
				$(".alert-danger").addClass("hide");
				$(".alert-success").removeClass("hide");

				this.model.set("source", ast);
				this.model.set("userInputSource", $(".programInput").html());

				console.log(ast);
				
				// include a syntax error to the program input to get the next possibilities if the user wants to add rules
				try {
					grammar.parse(programInput + " ");
				} catch(e) {
					e.expected.forEach(function(nextPossibility) {
						if ($("button.deleted-elements").html().replace(/"/g, "'") === nextPossibility.replace(/"/g, "")) {
							$("button.deleted-elements").addClass("hidden");
						}
						if (nextPossibility.indexOf("input") === -1) {
							$(".expected-elements").append("<button class='btn btn-default completion-button'>" + nextPossibility.replace(/"/g, "").replace(/\\/g, "") + "</button>&nbsp;");
						} else {
							$(".expected-elements").append(nextPossibility);
						}
					});
				}
			} catch(e) {
				$(".alert-danger").removeClass("hide");
				$(".alert-success").addClass("hide");
				
				if (e.expected.length === 1) {
					if ($("button.deleted-elements").html().replace(/" /g, "'") === e.expected[0]) {
						$("button.deleted-elements").addClass("hidden");
					}
					
					if (e.expected[0] === $.i18n.t("language.space")) {
						$(".programInput").append(" ");
						this.compileProgram();
					} else if (e.expected[0].indexOf("input") === -1) {
						$(".programInput").append(e.expected[0].replace(/"/g, ""));
						this.compileProgram();
					} else {
						$(".expected-elements").html(e.expected[0]);
					}
				} else {
					e.expected.forEach(function(nextPossibility) {
						if ($("button.deleted-elements").html().replace(/"/g, "'") === nextPossibility.replace(/"/g, "")) {
							$("button.deleted-elements").addClass("hidden");
						}
						
						if (nextPossibility.indexOf("input") === -1) {
							$(".expected-elements").append("<button class='btn btn-default completion-button'>" + nextPossibility.replace(/"/g, "").replace(/\\/g, "") + "</button>&nbsp;");
						} else {
							$(".expected-elements").append(nextPossibility);
						}
					});
				}
			}
			
			if ($(".programInput").html().replace(/"/g, "'").replace(/ /g, "") === $("button.deleted-elements").html().replace(/"/g, "'").replace(/ /g, "")) {
				$("button.deleted-elements").addClass("hidden");
			}
			
			// check if it is possible to append the deleted elements to the program
			if (!$("button.deleted-elements").hasClass("hidden")) {
				try {
					grammar.parse(programInput + $("button.deleted-elements").html().replace(/"/g, "'"));
					$("button.deleted-elements")
							.removeClass("disabled")
							.css("text-decoration", "none")
							.css("font-style", "normal");
				} catch (e) {
					$("button.deleted-elements")
							.addClass("disabled")
							.css("text-decoration", "line-through")
							.css("font-style", "italic");
				}
			}
		},

		/**
		 * Render the editor view
		 */
		render:function() {
			// render the editor with the program
			this.$el.html(this.tplEditor({
				program : this.model
			}));
			
			// initialize the popover
			this.$el.find("#delete-popover").popover({
				html		: true,
				content		: "<button type='button' class='btn btn-danger delete-program-button'>" + $.i18n.t("form.delete-button") + "</button>",
				placement	: "bottom"
			});
			
			// try to compile the program to show the potential errors
			if (typeof this.model !== "undefined") {
				this.compileProgram();
			}
			
			// translate the view
			this.$el.i18n();
			
			return this;
		}
		
	});

	return Program;
});
