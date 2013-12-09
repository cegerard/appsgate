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

	/**
	 * Resizes the div of program list or program input to the maximum displayable size on the screen
	 */	
	function resizeDiv(jqNode){
		if(typeof jqNode !== "undefined"){
			jqNode[0].classList.add("div-scrollable");
			setTimeout(function(){
				var divSize = window.innerHeight-(jqNode.offset().top + jqNode.outerHeight(true) - jqNode.innerHeight());

				// if there isn't enough space to display the whole div, we adjust its size to the screen
				if(divSize<jqNode.outerHeight(true)){	
					jqNode.height(divSize);
				}

				// if there is an active element, make it visible
				var activeItem = jqNode.children(".list-group-item.active")[0];
				if(typeof activeItem !== "undefined"){
					jqNode.scrollTop((activeItem.offsetTop)-($(".list-group-item")[1].offsetTop));
				}
				// otherwise display the top of the list
				else{
					jqNode.scrollTop(0);
				}
			}, 0);
		}
	}

	// model
	Program.Model = Backbone.Model.extend({
		// default values
		defaults: {
			runningState : "DEPLOYED",
			modified : true,
			userInputSource : "",
			source : {
				programName : "",
				seqParameters : [],
				author : "",
				target : "",
				daemon : "true",
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
			console.log(model.toJSON());
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
				modified	: this.get("modified"),
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
			this.comparator = function(place) {
				return place.get("name");
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
			"keyup #add-program-modal input:text"			: "validAddProgram"
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
			this.listenTo(devices.getCoreClock(), "change", this.refreshClockDisplay);
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
		 * Refreshes the time display without rerendering the whole screen
		 */
		refreshClockDisplay:function() {
			
			//remove existing node
			$(this.$el.find(".list-group")[0]).children().remove();

			//refresh the clock
			$(this.$el.find(".list-group")[0]).append(this.tplCoreClockContainer({
				device	: devices.getCoreClock(),
				active	: Backbone.history.fragment === "devices/" + devices.getCoreClock().get("id") ? true : false
			}));

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
  			var self = this;
            if (e.type === "keyup" && e.keyCode === 13 || e.type === "click") {
                // create the program if the name is ok
				if (this.checkProgramName()) {
					var program;
					
					// if a file is selected we use the program from file
					if (typeof window.FileReader !== 'undefined' && $("#add-program-modal input:file")[0].files[0]){
						var file = $("#add-program-modal input:file")[0].files[0];
						var fileR = new FileReader();
						fileR.onload = receivedText;
						fileR.readAsText(file);
						
						// build the program from data read in file
						function receivedText(){
							var result = fileR.result;
							
							try{
								var resultJSON = JSON.parse(result);
								
								program = new Program.Model(resultJSON);
								program.set("id",null); // make sure the program is considered as new one
								program.set("name", $("#add-program-modal input:text").val()); // use the name selected by the user
								program.get("source").programName = $("#add-program-modal input:text").val();
								
								// hide the modal
								$("#add-program-modal").modal("hide");
							} catch (exception){
								$("#add-program-modal .text-danger")
									.text($.i18n.t("modal-add-program.unable-to-parse"))
									.removeClass("hide");
								$("#add-program-modal .valid-button").addClass("disabled");
							}
						}
												  
					}
					else{
						// instantiate a model for the new program
						program = new Program.Model({
							name	: $("#add-program-modal input:text").val(),
							daemon	: "false"
						});
						
						// hide the modal
						$("#add-program-modal").modal("hide");
					}
					
					// instantiate the program and add it to the collection after the modal has been hidden
					$("#add-program-modal").on("hidden.bs.modal", function() {
						// tell the router there is no modal any more
						appRouter.isModalShown = false;
						
						// send the program to the backend
						program.save();

						// add it to the collection
						programs.add(program);

						// display the new program
						appRouter.showView(new Program.Views.Editor({ model : program }));
			
						// update the url to the new program
						appRouter.navigate("#programs/" + program.get("id"));

						// set the current program active
						_.forEach($("a.list-group-item"), function(item) {
							if(item.id === "side-"+program.id){
								$(item).addClass("active");
								$(self.$el.find(".list-group")[1]).scrollTop(1000);
							}
							else{
								$(item).removeClass("active");
							}
						});
					});
				}
			} else if (e.type === "keyup") {
				this.checkProgramName();
			}
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

				// "add program" button to the side menu
				this.$el.append(this.tplAddProgramButton());

				// for each program, add a menu item
				this.$el.append(this.tpl());
				var programsDiv = $(self.$el.find(".list-group")[1]);

				// we build a temporary container with each model
 				var container = document.createDocumentFragment();
				programs.forEach(function(program) {
					$(container).append(self.tplProgramContainer({
						program : program,
						active	: Backbone.history.fragment.split("/programs")[1] === program.get("name") ? true : false
					}));
				});

				// we add all elements all at once to avoid rendering them individually and thus reflowing the dom 					several times
				programsDiv.append(container);

				// set active the current menu item
				this.updateSideMenu();
				
				// translate the view
				this.$el.i18n();

				// fix the programs list size to be able to scroll through it
				resizeDiv(programsDiv);

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
			"show.bs.modal #edit-program-name-modal"			: "initializeModal",
			"hidden #edit-program-name-modal"				: "render",
			"click #edit-program-name-modal button.valid-button"		: "validEditName",
			"keyup #edit-program-name-modal input"				: "validEditName",
			"click button.start-program-button"				: "onStartProgramButton",
			"click button.stop-program-button"				: "onStopProgramButton",
			"click button.save-program-button"				: "onSaveProgramButton",
			"click button.delete-program-button"				: "onDeleteProgramButton",
			"keyup textarea"						: "onKeyUpTextarea",
			"click .expected-elements > button.completion-button"		: "onClickCompletionButton",
			"click .programInput > span"					: "onClickSourceElement",
			"click button.browse-media"					: "onClickBrowseMedia",
			"click button.valid-value"					: "onValidValueButton",
			"click button.valid-media"					: "onValidMusicButton",
			"click button.replace-media"				: "onReplaceMusicButton",
            "click button.deleted-elements"					: "onClickDeletedElements",
			"click button.value-popover-button"				: "onClickValuePopoverButton",
			"click button.valid-value-popover-button"			: "onClickValidValuePopoverButton",
			"click button.device-popover-button"				: "onClickDevicePopoverButton",
			"click button.delete-popover-button"				: "onClickDeletePopoverButton",
			"click button.close-popover-button"				: "onClickClosePopoverButton"
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
		 * Callback when the user has clicked on the button to save modifications done on a program. Send the update to the server
		 */
		onSaveProgramButton:function() {

			this.model.set("modified", false);

			// replace span text
			if (!$(".save-span").hasClass("hidden") && $(".saving-span").hasClass("hidden")) {
				$(".save-span").addClass("hidden");
				$(".saving-span").removeClass("hidden");
			}
		
			// save the model
			this.model.save();
			
			// hide the deleted elements
			if (!$(".deleted-elements").hasClass("hidden")) {
				$(".deleted-elements").addClass("hidden");
			}

			// after a while reset save button text, TODO: get info from server when the programm is saved
			setTimeout(function(){
				$(".save-span").removeClass("hidden");
				$(".saving-span").addClass("hidden");
				$(".start-program-button").prop("disabled",false);
			}, 1000);

		},

		/**
		 * Clear the input text, hide the error message and disable the valid button by default
		 */
		initializeModal:function() {
			$("#edit-program-name-modal input").val(this.model.get("name"));
			$("#edit-program-name-modal .text-danger").addClass("hide");
			$("#edit-program-name-modal .valid-button").addClass("disabled");
		},

		/**
		 * Check the current value of the input text and show a message error if needed
		 * 
		 * @return false if the typed name already exists, true otherwise
		 */
		checkProgramName:function() {
			// name is empty
			if ($("#edit-program-name-modal input").val() === "") {
				$("#edit-program-name-modal .text-danger").removeClass("hide");
				$("#edit-program-name-modal .text-danger").text($.i18n.t("modal-edit-program.program-name-empty"));
				$("#edit-program-name-modal .valid-button").addClass("disabled");
				
				return false;
			}
			
			// name already existing
			if (programs.where({ name : $("#edit-program-name-modal input").val() }).length > 0) {
				$("#edit-program-name-modal .text-danger").removeClass("hide");
				$("#edit-program-name-modal .text-danger").text($.i18n.t("modal-edit-program.program-already-existing"));
				$("#edit-program-name-modal .valid-button").addClass("disabled");
				
				return false;
			}
			
			//ok
			$("#edit-program-name-modal .text-danger").addClass("hide");
			$("#edit-program-name-modal .valid-button").removeClass("disabled");
			
			return true;
		},
				
		/**
		 * Check if the name of the program does not already exist. If not, update the program
		 * Hide the modal when done
		 */
		validEditName:function(e) {
			var self = this;
			
			if (e.type === "keyup" && e.keyCode === 13 || e.type === "click") {
				e.preventDefault();
				
				// update the name if it is ok
				if (this.checkProgramName()) {
					this.$el.find("#edit-program-name-modal").on("hidden.bs.modal", function() {
						// set the new name to the place
						self.model.set("name", $("#edit-program-name-modal input").val());
						self.model.get("source").programName = $("#edit-program-name-modal input").val();

						// send the update to the backend
						self.model.save();
						
						return false;
					});
					
					$("#edit-program-name-modal").on("hidden.bs.modal",function(){
						self.render();
					});

					// hide the modal
					$("#edit-program-name-modal").modal("hide");

				}
			} else if (e.type === "keyup") {
				this.checkProgramName();
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
			this.model.set("modified", true);
			this.compileProgram();
		},
		
		onClickCompletionButton:function(e) {
			this.model.set("modified", true);
			if ($(e.currentTarget).text() === $.i18n.t("language.space")) {
				// $(".programInput").append(" ");
			} else {
				$(".programInput").append($(e.currentTarget).html());
			}
			this.compileProgram();
		},

		onClickSourceElement:function(e) {
			this.model.set("modified", true);
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
			} else if ($(e.currentTarget).hasClass("music")) {
				var title = "Change music";
				var content = $("<span>");
				content.append("<button class='btn btn-primary browse-media' data-target='#media-browser-modal' data-toggle='modal'>" + $.i18n.t("form.browse-button") + "</button><span id='playurl' style='display:none'></span>");
				$(".media-button").addClass("replace-media").removeClass("valid-media");
				
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
			this.model.set("modified", true);
			$(".programInput").append("<span class='value'>" + $(".expected-elements input").val() + "</span>");
			this.compileProgram();
		},
		
		onValidMusicButton:function() {
		$("#media-browser-modal").modal("hide");
			this.model.set("modified", true);
			$(".programInput").append("<span class='music' url='"+$("#playurl").attr('url')+"'>" + $("#playurl").html() + "</span>");
			this.compileProgram();
		},
		
		onReplaceMusicButton:function() {
			$("#media-browser-modal").modal("hide");
			this.model.set("modified", true);
			var self = this;
			this.valueToReplace.replaceWith("<span class='music' url='"+$("#playurl").attr('url')+"'>" + $("#playurl").html() + "</span>");
			
			this.valueToReplace.on("hidden.bs.popover", function() {
				self.valueToReplace
						.removeAttr("data-original-title")
						.removeAttr("title");
				$(".programInput .popover").remove();
				
				$(".media-button").addClass("valid-media").removeClass("replace-media");
				
				self.compileProgram();
			});
			
			this.valueToReplace.popover("destroy");
		},
		
		onClickBrowseMedia:function() {
			var possibleDevices = devices.where({ type : 31 });
			var currentDevice = devices.get(possibleDevices[0].get("id"));
			currentDevice.onBrowseMedia($("#playurl"));
		},
		
		onClickDeletedElements:function() {
			this.model.set("modified", true);
			$(".programInput").append($("button.deleted-elements").html());
			$("button.deleted-elements").addClass("hidden");
			this.compileProgram();
		},
		
		onClickValuePopoverButton:function(e) {
			this.model.set("modified", true);
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
			this.model.set("modified", true);
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
			this.model.set("modified", true);
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
			this.model.set("modified", true);
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
			this.model.set("modified", true);
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

			var self = this;

			// build the beginning of the user input source to be given to the parser
			var programInput = this.model.get("name") + " " + $.i18n.t("language.written-by") + " Bob pour Alice ";
			programInput += $(".programInput").html();
			programInput = programInput.replace(/"/g, "'");
			
			// clear the error span
			$(".expected-elements").html("");

			try {
				var ast = grammar.parse(programInput);
				$(".alert-danger").addClass("hide");
				$(".alert-success").removeClass("hide");

				this.model.set("source", ast);
				this.model.set("userInputSource", $(".programInput").html());

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
            	if ( typeof(e.expected) !== 'undefined') {
            
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

			// fix the program input size to be able to scroll through it
			resizeDiv($(self.$el.find(".editorWorkspace")[0]));
		},

		/**
		 * Render the editor view
		 */
		render:function() {
			
			var self = this;

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

			// put the name of the place by default in the modal to edit
			if(typeof this.model !== 'undefined'){
				$("#edit-program-name-modal .program-name").val(this.model.get("name"));
			}

			// hide the error message
			$("#edit-program-name-modal .text-error").hide();
			
			// try to compile the program to show the potential errors
			if (typeof this.model !== "undefined") {
				this.compileProgram();
			}

			// fix the programs list size to be able to scroll through it
			resizeDiv($(self.$el.find(".editorWorkspace")[0]));
			
			// disable start button if there is unsaved changes
			$(".start-program-button").prop("disabled",this.model.get("modified"));

			// translate the view
			this.$el.i18n();
			
			return this;
		}
		
	});

	return Program;
});
