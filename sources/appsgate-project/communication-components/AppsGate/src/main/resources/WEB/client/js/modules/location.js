define([
	"jquery",
	"underscore",
	"backbone",
	"grammar",
	"text!templates/locations/menu/menu.html",
	"text!templates/locations/menu/placeContainer.html",
	"text!templates/devices/menu/coreClockContainer.html",
	"text!templates/locations/menu/addButton.html",
	"text!templates/locations/details/details.html",
	"i18next"
], function($, _, Backbone, Grammar, placeMenuTemplate, placeContainerMenuTemplate, coreClockContainerMenuTemplate, addPlaceButtonTemplate, locationDetailsTemplate) {
	// initialize the module
	var Location = {};

	// router
	Location.Router = Backbone.Router.extend({
		routes: {
			"locations"		: "list",
			"locations/:id"	: "details"
		},

		// list all the locations
		list:function() {
			// display the side menu
			appRouter.showMenuView(new Location.Views.Menu());
			
			// set active the first element - displayed by default
			$($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).addClass("active");
			
			// display the first place
			appRouter.showView(new Location.Views.Details({ model : locations.at(0) }));
			
			// update the url
			appRouter.navigate("#locations/" + locations.at(0).get("id"));
		},

		// show the details of a locations (i.e. list of devices in this location)
		details:function(id) {
			appRouter.showView(new Location.Views.Details({ model : locations.get(id) }));
		}
	});

	// instantiate the router
	var router = new Location.Router();

	/**
	 * Resizes the div to the maximum displayable size on the screen
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
	Location.Model = Backbone.Model.extend({

		/**
		 * @constructor
		 */
		initialize:function() {
			var self = this;
			
			// when a name is updated, update the grammar
			this.on("change:name", function() {
				if (typeof window.grammar !== "undefined") {
					delete window.grammar;
				}
				window.grammar = new Grammar();
			});
			
			// remove potential duplicated entries and trigger a refresh of the list of places event
			this.on("change:devices", function() {
				self.set({ devices : _.uniq(self.get("devices")) });
			});
		},
		
		/**
		 * Compute the average value of given sensors
		 * 
		 * @param sensors Array of sensors
		 * @return Average value of the sensors if any, undefined otherwise
		 */
		getAverageValue:function(sensors) {
			// return null if there is no temperature sensors in the room
			if (sensors.length === 0) {
				return undefined;
			}
			
			// compute the average value of the sensors
			var average = 0;
			sensors.forEach(function(s) {
				if (typeof s.get("value") !== "undefined") {
					average += parseInt(s.get("value"));
				} else {
					average += parseInt(s.get("consumption"));
				}
			});
			
			return average / sensors.length;
		},
		
		/**
		 * Compute the average temperature of the place from the temperature sensors in the place
		 * 
		 * @return Average temperature of the place if any temperature sensor, undefined otherwise
		 */
		getAverageTemperature:function() {
			return this.getAverageValue(this.getTemperatureSensors());
		},
				
		/**
		 * Compute the average illumination of the place from the illumination sensors in the place
		 * 
		 * @return Average illumination of the place if any illumination sensor, undefined otherwise
		 */
		getAverageIllumination:function() {
			return this.getAverageValue(this.getIlluminationSensors());
		},
		
		/**
		 * Compute the average consumption of the place from the plugs in the place
		 * 
		 * @return Average consumption of the place if any consumption sensor, undefined otherwise
		 */
		getAverageConsumption:function() {
			return this.getAverageValue(this.getPlugs());
		},
		
		/**
		 * Return all the devices of the place that matches a given type
		 * 
		 * @param type Type of the devices to retrieve
		 * @return Array of devices w/ good type
		 */
		getTypeSensors:function(type) {
			type = parseInt(type);
			
			// in case of wrong type, return an empty array
			if (isNaN(type)) {
				return [];
			}
			
			// get all the devices that match the type
			var sensorsId = this.get("devices").filter(function(id) {
				return (devices.get(id) !== undefined && devices.get(id).get("type") === type);
			});
			
			// get all the devices that match the type and the place
			sensors = devices.filter(function(device) {
				return (sensorsId.indexOf(device.get("id").toString()) !== -1);
			});
			
			return sensors;
		},
		
		/**
		 * @return Array of temperature sensors in the place
		 */
		getTemperatureSensors:function() {
			return this.getTypeSensors(0);
		},

		/**
		 * @return Array of illumination sensors in the place
		 */
		getIlluminationSensors:function() {
			return this.getTypeSensors(1);
		},
		
		/**
		 * @return Array of switches in the place
		 */
		getSwitches:function() {
			return this.getTypeSensors(2);
		},
		
		/**
		 * @return Array of contact sensors in the place
		 */
		getContactSensors:function() {
			return this.getTypeSensors(3);
		},
		
		/**
		 * @return Array of key-card readers in the place
		 */
		getKeyCardReaders:function() {
			return this.getTypeSensors(4);
		},
		
		/**
		 * @return Array of movement sensors in the place
		 */
		getMovementSensors:function() {
			return this.getTypeSensors(5);
		},
		
		/**
		 * @returns Array of plugs in the place
		 */
		getPlugs:function() {
			return this.getTypeSensors(6);
		},
		
		/**
		 * @return Array of Philips Hue lamps in the place
		 */
		getPhilipsHueLamps:function() {
			return this.getTypeSensors(7);
		},
		
		getMediaPlayers:function() {
			return this.getTypeSensors(31);
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

		/**
		 * Override its synchronization method to send a notification on the network
		 */
		sync:function(method, model) {
			switch (method) {
				case "create":
					// create an id to the place
					var id;
					do {
						id = "place-" + Math.round(Math.random() * 10000).toString();
					} while (locations.where({ id : id }).length > 0);
					model.set("id", id);
					
					this.remoteCall("newPlace", [{ type : "JSONObject", value : model.toJSON() }]);
					break;
				case "delete":
					this.remoteCall("removePlace", [{ type : "String", value : model.get("id") }]);
					break;
				case "update":
					this.remoteCall("updatePlace", [{ type : "JSONObject", value : model.toJSON() }]);
					break;
				default:
					break;
			}
		},

		/**
		 * Converts the model to its JSON representation.
		 */
		toJSON:function() {
			return {
				id		: this.get("id").toString(),
				name	: this.get("name"),
				devices	: this.get("devices")
			} 
		}
	});
	
	// collection
	Location.Collection = Backbone.Collection.extend({
		model: Location.Model,

		/**
		 * Fetch the locations from the server
		 *
		 * @constructor
		 */
		initialize:function() {
		 	var self = this;
			
			// sort the locations alphabetically
			this.comparator = function(location) {
				return location.get("name");
			};
			
			// add the location w/ id -1 for the unlocated devices
			this.add({
				id : "-1",
				name: $.i18n.t("places-menu.unlocated-devices"),
				devices: []
			});
			
			// a place has been removed - put its devices as unlocated
			this.on("remove", function(place) {
				self.updateDevicesRemovedPlace(place);
				
				// update the grammar
				delete window.grammar;
				window.grammar = new Grammar();
			});

		 	// listen to the event when the list of locations is received
		 	dispatcher.on("listPlaces", function(locations) {
		 		_.each(locations, function(location) {
		 			self.add(location);
		 		});
		 		dispatcher.trigger("locationsReady");
		 	});

		 	// listen to the event when a location appears and add it
		 	dispatcher.on("newPlace", function(location) {
		 		self.add(location);
				
				// update the grammar
				delete window.grammar;
				window.grammar = new Grammar();
		 	});

			// listen to the event when a place has been updated
			dispatcher.on("updatePlace", function(place) {
				locations.get(place.id).set("name", place.name);
				
				// update the grammar
				delete window.grammar;
				window.grammar = new Grammar();
			});

			// listen to the event when a place has been removed
			dispatcher.on("removePlace", function(placeId) {
				var removedPlace = locations.get(placeId);
				
				// check if the place exists in the collection
				if (typeof removedPlace !== "undefined") {
					
					// remove the place from the collection
					locations.remove(removedPlace);

					// update the devices of the place
					self.updateDevicesRemovedPlace(removedPlace);

					// refresh the content if the details of the removed place was displayed
					if (Backbone.history.fragment === "locations/" + placeId) {
						appRouter.navigate("#locations", { trigger : true });
					}
					
					// update the grammar
					delete window.grammar;
					window.grammar = new Grammar();
				}
			});

		 	// listen to the event when a device has been moved
		 	dispatcher.on("moveDevice", function(messageData) {
		 		self.moveDevice(messageData.srcLocationId, messageData.destLocationId, messageData.deviceId, false);
				
				// update the grammar
				delete window.grammar;
				window.grammar = new Grammar();
		 	});

		 	// send the request to fetch the locations
		 	communicator.sendMessage({
				method : "getPlaces",
				args: [],
				callId: "listPlaces"
			});
		 },

		 /**
		  * Return the name of the location where a device is located
		  * 
		  * @param device
		  * @return Name of the location where the device is located
		  */
		getNameByDevice:function(device) {
			try {
				return this.get(device.get("placeId")).get("name");
			} catch (e) {
				return "Non d&eacute;fini";
			}
		},
		
		/**
		 * After removing a place from the collection, its devices need to be unlocated
		 * 
		 * @param removedPlace Place that has been removed
		 */
		updateDevicesRemovedPlace:function(removedPlace) {
			var self = this;
			
			// devices located in the place are now unlocated
			removedPlace.get("devices").forEach(function(deviceId) {
				// update their attributes
				var device = devices.get(deviceId);
				if (typeof device !== "undefined") {
					device.set({ placeId : -1 });
				}

				// add it to the unlocated devices array of the collection
				self.get("-1").get("devices").push(deviceId);
			});
		},

		/**
		 * Update the locations and the device
		 *
		 * @param srcPlaceId
		 * @param destPlaceId
		 * @param deviceId
		 * @param movedByUser
		 */
		moveDevice:function(srcPlaceId, destPlaceId, deviceId, movedByUser) {
			console.log(srcPlaceId, destPlaceId, deviceId);
			var srcLocation = locations.get(srcPlaceId);
		 	var destLocation = locations.get(destPlaceId);
			
		 	// remove the device from the old location
		 	if (srcLocation !== undefined && srcLocation.get("devices").indexOf(deviceId) > -1) {
		 		srcLocation.get("devices").splice(srcLocation.get("devices").indexOf(deviceId), 1);
		 	}
			
		 	// add the device to the new location
		 	if (destLocation !== undefined && destLocation.get("devices").indexOf(deviceId) === -1) {
		 		destLocation.get("devices").push(deviceId);
		 	}
			
		 	// update the device itself
		 	devices.get(deviceId).set({ "placeId" : destPlaceId });
			
			// if the device has been moved by the user, send a notification to the backend
			if (movedByUser) {
				var messageJSON = {
					method	: "moveDevice",
					args	: [
						{ type : "String", value : deviceId },
						{ type : "String", value : srcPlaceId },
						{ type : "String", value : destPlaceId }
					]
				};
				
				// send the message
				communicator.sendMessage(messageJSON);
			}
		}
	});

	/**
	 * Namespace for the views
	 */
	Location.Views = {};
	
	/**
	 * Render the side menu for the places
	 */
	Location.Views.Menu = Backbone.View.extend({
		tpl						: _.template(placeMenuTemplate),
		tplPlaceContainer		: _.template(placeContainerMenuTemplate),
		tplCoreClockContainer	: _.template(coreClockContainerMenuTemplate),
		tplAddPlaceButton		: _.template(addPlaceButtonTemplate),
		
		/**
		 * Bind events of the DOM elements from the view to their callback
		 */
		events: {
			"click a.list-group-item"						: "updateSideMenu",
			"show.bs.modal #add-place-modal"				: "initializeModal",
			"hidden.bs.modal #add-place-modal"				: "toggleModalValue",
			"click #add-place-modal button.valid-button"	: "validEditName",
			"keyup #add-place-modal input"					: "validEditName"
		},
		
		/**
		 * Listen to the places collection update and refresh if any
		 * 
		 * @constructor
		 */
		initialize:function() {
			this.listenTo(locations, "add", this.render);
			this.listenTo(locations, "change", this.render);
			this.listenTo(locations, "remove", this.render);
			this.listenTo(devices, "change", this.onChangedDevice);
		},
		
		/**
		 * Method called when a device has changed
		 * @param model Model that changed, Device in that cas
		 * @param collection Collection that holds the changed model
		 * @param options Options given with the change event 	
		 */
		onChangedDevice:function(model, collection, options) {
			// a device has changed
			// if it's the clock, we refresh the clock only
			if(typeof options !== "undefined" && options.clockRefresh){
				this.refreshClockDisplay();
			}
			// otherwise we rerender the whole view
			else{
				this.render();
			}
		},

		/**
		 * Refreshes the time display without rerendering the whole screen
		 */
		refreshClockDisplay:function() {

			if (typeof devices.getCoreClock() !== "undefined") { // dirty hack to avoid a bug when reconnecting - TODO
				//remove existing node
				$(this.$el.find(".list-group")[0]).children().remove();

				//refresh the clock
				$(this.$el.find(".list-group")[0]).append(this.tplCoreClockContainer({
					device	: devices.getCoreClock(),
					active	: Backbone.history.fragment === "devices/" + devices.getCoreClock().get("id") ? true : false
				}));
			}
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
			
			$(e.currentTarget).addClass("active");
		},
		
		/**
		 * Clear the input text, hide the error message and disable the valid button by default
		 */
		initializeModal:function() {
			$("#add-place-modal input").val("");
			$("#add-place-modal .text-danger").addClass("hide");
			$("#add-place-modal .valid-button").addClass("disabled");
			
			// the router that there is a modal
			appRouter.isModalShown = true;
		},
		
		/**
		 * Tell the router there is no modal anymore
		 */
		toggleModalValue:function() {
			appRouter.isModalShown = false;
		},
		
		/**
		 * Check the current value of the input text and show an error message if needed
		 * 
		 * @return false if the typed name already exists, true otherwise
		 */
		checkPlace:function() {
			// name is empty
			if ($("#add-place-modal input").val() === "") {
				$("#add-place-modal .text-danger")
						.text($.i18n.t("modal-add-place.place-name-empty"))
						.removeClass("hide");
				$("#add-place-modal .valid-button").addClass("disabled");
				
				return false;
			}
			
			// name already exists
			if (locations.where({ name : $("#add-place-modal input").val() }).length > 0) {
				$("#add-place-modal .text-danger")
						.text($.i18n.t("modal-add-place.place-already-existing"))
						.removeClass("hide");
				$("#add-place-modal .valid-button").addClass("disabled");
				
				return false;
			}
			
			// ok
			$("#add-place-modal .text-danger").addClass("hide");
			$("#add-place-modal .valid-button").removeClass("disabled");
			
			return true;
		},

		/**
		 * Check if the name of the place does not already exist. If not, update the place
		 * Hide the modal when done
		 * 
		 * @param e JS event
		 */
		validEditName:function(e) {
			if (e.type === "keyup" && e.keyCode === 13 || e.type === "click") {
				// create the place if the name is ok
				if (this.checkPlace()) {
					
					// instantiate the place and add it to the collection after the modal has been hidden
					$("#add-place-modal").on("hidden.bs.modal", function() {
						// instantiate a model for the new place
						var place = new Location.Model({
							name	: $("#add-place-modal input").val(),
							devices	: []
						});

						// send the place to the backend
						place.save();

						// add it to the collection
						locations.add(place);
						
						// tell the router that there is no modal any more
						appRouter.isModalShown = false;
					});
					
					// hide the modal
					$("#add-place-modal").modal("hide");
				}
			} else if (e.type === "keyup") {
				this.checkPlace();
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
				if (typeof devices.getCoreClock() !== "undefined") { // dirty hack to avoid a bug when reconnecting - TODO
					$(this.$el.find(".list-group")[0]).append(this.tplCoreClockContainer({
						device	: devices.getCoreClock(),
						active	: Backbone.history.fragment === "devices/" + devices.getCoreClock().get("id") ? true : false
					}));
				}

				// "add place" button to the side menu
				this.$el.append(this.tplAddPlaceButton());

				// for each location, add a menu item
				this.$el.append(this.tpl());
				
				// put the unlocated devices into a separate group list
				//this.$el.append(this.tpl());
				$(this.$el.find(".list-group")[1]).append(this.tplPlaceContainer({
					place	: locations.get("-1"),
					active	: Backbone.history.fragment.split("/")[1] === "-1" ? true : false
				}));

				locations.forEach(function(location) {
					if (location.get("id") !== "-1") {
						$(self.$el.find(".list-group")[1]).append(self.tplPlaceContainer({
							place : location,
							active	: Backbone.history.fragment.split("/")[1] === location.get("id") ? true : false
						}));
					}
				});

				// translate the menu
				this.$el.i18n();

				// resize the menu
				resizeDiv($(self.$el.find(".list-group")[1]));

				return this;
			}
		}
	});

	/**
	 * Detailled view of a place
	 */
	Location.Views.Details = Backbone.View.extend({
		tpl: _.template(locationDetailsTemplate),

		/**
		 * Bind events of the DOM elements from the view to their callback
		 */
		events: {
			"show.bs.modal #edit-name-place-modal"				: "initializeModal",
			"click #edit-name-place-modal button.valid-button"	: "validEditName",
			"keyup #edit-name-place-modal input"				: "validEditName",
			"click button.delete-place-button"					: "deletePlace",
			"click button.toggle-plug-button"					: "onTogglePlugButton",
			"click button.toggle-lamp-button"					: "onToggleLampButton"
		},
		
		/**
		 * Listen to the model update and refresh if any
		 * 
		 * @constructor
		 */
		initialize:function() {
			var self = this;
			
			// listen to update on its model...
			this.listenTo(this.model, "change", this.render);
			
			// ... and on all its devices
			this.model.get("devices").forEach(function(deviceId) {
				var device = devices.get(deviceId);
				
				// if the device has been found in the collection
				if (typeof device !== "undefined") {
					self.listenTo(devices.get(deviceId), "change", self.render);
				}
			});
		},

		/**
		 * Clear the input text, hide the error message and disable the valid button by default
		 */
		initializeModal:function() {
			$("#edit-name-place-modal input").val(this.model.get("name"));
			$("#edit-name-place-modal .text-danger").addClass("hide");
			$("#edit-name-place-modal .valid-button").addClass("disabled");
		},

		/**
		 * Check the current value of the input text and show a message error if needed
		 * 
		 * @return false if the typed name already exists, true otherwise
		 */
		checkPlace:function() {
			// name is empty
			if ($("#edit-name-place-modal input").val() === "") {
				$("#edit-name-place-modal .text-danger").removeClass("hide");
				$("#edit-name-place-modal .text-danger").text($.i18n.t("modal-edit-place.place-name-empty"));
				$("#edit-name-place-modal .valid-button").addClass("disabled");
				
				return false;
			}
			
			// name already existing
			if (locations.where({ name : $("#edit-name-place-modal input").val() }).length > 0) {
				$("#edit-name-place-modal .text-danger").removeClass("hide");
				$("#edit-name-place-modal .text-danger").text($.i18n.t("modal-edit-place.place-already-existing"));
				$("#edit-name-place-modal .valid-button").addClass("disabled");
				
				return false;
			}
			
			//ok
			$("#edit-name-place-modal .text-danger").addClass("hide");
			$("#edit-name-place-modal .valid-button").removeClass("disabled");
			
			return true;
		},
				
		/**
		 * Check if the name of the place does not already exist. If not, update the place
		 * Hide the modal when done
		 */
		validEditName:function(e) {
			var self = this;
			
			if (e.type === "keyup" && e.keyCode === 13 || e.type === "click") {
				e.preventDefault();
				
				// update the name if it is ok
				if (this.checkPlace()) {
					this.$el.find("#edit-name-place-modal").on("hidden.bs.modal", function() {
						// set the new name to the place
						self.model.set("name", $("#edit-name-place-modal input").val());
						
						// send the update to the backend
						self.model.save();
						
						return false;
					});
					
					// hide the modal
					$("#edit-name-place-modal").modal("hide");
				}
			} else if (e.type === "keyup") {
				this.checkPlace();
			}
		},
		
		/**
		 * Callback when the user has clicked on the button to remove a place. Remove the place
		 */
		deletePlace:function() {
			// delete the place
			this.model.destroy();
			
			// navigate to the list of locations
			appRouter.navigate("#locations", { trigger : true });
		},
		
		/**
		 * Callback to toggle a plug
		 * 
		 * @param e JS mouse event
		 */
		onTogglePlugButton:function(e) {
			e.preventDefault();
			
			var plug = devices.get($(e.currentTarget).attr("id"));
			// value can be string or boolean
			// string
			if (typeof plug.get("plugState") === "string") {
				if (plug.get("plugState") === "true") {
					plug.set("plugState", "false");
				} else {
					plug.set("plugState", "true");
				}
			// boolean
			} else {
				if (plug.get("plugState")) {
					plug.set("plugState", "false");
				} else {
					plug.set("plugState", "true");
				}
			}
			
			// send the message to the backend
			plug.save();
			
			return false;
		},
		
		/**
		 * Callback to toggle a lamp
		 * 
		 * @param e JS mouse event
		 */
		onToggleLampButton:function(e) {
			e.preventDefault();
			
			var lamp = devices.get($(e.currentTarget).attr("id"));
			// value can be string or boolean
			// string
			if (typeof lamp.get("value") === "string") {
				if (lamp.get("value") === "true") {
					lamp.set("value", "false");
				} else {
					lamp.set("value", "true");
				}
			// boolean
			} else {
				if (lamp.get("value")) {
					lamp.set("value", "false");
				} else {
					lamp.set("value", "true");
				}
			}
			
			// send the message to the backend
			lamp.save();
			
			return false;
		},

		/**
		 * Render the view
		 */
		render:function() {
			if (!appRouter.isModalShown) {
				// render the view itself
				this.$el.html(this.tpl({
					place : this.model,
				}));

				// put the name of the place by default in the modal to edit
				$("#edit-name-place-modal .place-name").val(this.model.get("name"));

				// hide the error message
				$("#edit-name-place-modal .text-error").hide();

				// initialize the popover
				this.$el.find("#delete-popover").popover({
					html		: true,
					content		: "<button type='button' class='btn btn-danger delete-place-button'>" + $.i18n.t("form.delete-button") + "</button>",
					placement	: "bottom"
				});
				
				// translate the view
				this.$el.i18n();

				// resize the devices list in the selected location
				resizeDiv($(".contents-list"));

				return this;
			}
		}
	});

	return Location;
});
