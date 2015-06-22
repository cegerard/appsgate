define([
  "app",
  "raphael",
  "views/device/details/details",
  "views/device/details/fairylights/fairylightsModalCreation",
  "views/device/details/fairylights/fairylightsModalManage",
  "text!templates/devices/details/fairylights/fairylights.html",
  "colorwidget"
  ], function (App, Raphael, DeviceDetailsView, ModalCreationView, ModalManageView, fairyLightsDetailTemplate, colorWidgetJs) {

	var FairyLightsView = {};
	// detailled view of a device
	FairyLightsView = DeviceDetailsView.extend({
		tplFairyLights: _.template(fairyLightsDetailTemplate),

		events: {
			"click #dpd-colors a": "onClickDropdownColors",
			"click #dpd-patterns a": "onClickDropdownPattenrs",

			"show.bs.modal #modal-create-pattern": "onShowCreateModal",
			"shown.bs.modal #modal-create-pattern": "onCreateModalShown",
			"hidden.bs.modal #modal-create-pattern": "onCreateModalHidden",

			"show.bs.modal #modal-manage-pattern": "onShowManageModal",
			"shown.bs.modal #modal-manage-pattern": "onManageModalShown",
			"hidden.bs.modal #modal-manage-pattern": "onManageModalHidden",

			"click #btn-cmd-turnoff": "onClickTurnOff",

			"click .btn-widget-select-all.btn-command": "onClickSelectAll",
			"click .btn-widget-deselect-all.btn-command": "onClickDeselectAll"
		},

		initialize: function () {
			var self = this;

			self.currentSelectedLED = [];
			/*
			 * Boolean to know the current state in process of changing color.
			 * First select all leds to change. Second, change color. You can change color some time to get the wanted color. When change color, this boolean go to true. If we select another led, we check it and deselect previous leds before make new selection.
			 */
			self.stateColorChanged = false;

			FairyLightsView.__super__.initialize.apply(self, arguments);
			$.extend(self.__proto__.events, FairyLightsView.__super__.events);

			// Voir si on bind cet event pour les fairy..
			//			dispatcher.on(this.model.get("id"), function (updatedVariableJSON) {
			//				if (updatedVariableJSON.varName == "colorChanged") {
			//					hexcolor = JSON.parse(updatedVariableJSON.value).rgbcolor;
			//					moveColorByHex(expandHex(hexcolor));
			//				}
			//			});

		},

		colorchanged: function () {
			var rgb = $("#colorbg").css("background-color");
			this.changeColorLEDs(this.currentSelectedLED, Raphael.getRGB(rgb).hex);
		},

		/**
		 * Callback on click colors in dropdown TurnOn with Color
		 **/
		onClickDropdownColors: function (e) {
			// Prevent the redirection of <a>
			e.preventDefault();

			var colorSelected = $(e.currentTarget).attr("color-value");
			this.model.setAllColorLight(colorSelected);
		},

		/**
		 * Callback on click colors in dropdown TurnOn with Pattern
		 **/
		onClickDropdownPattenrs: function (e) {
			// Prevent the redirection of <a>
			e.preventDefault();

			var patterNameClicked = $(e.currentTarget).text();
			this.model.setPattern(patterNameClicked);
		},

		/**
		 * Callback on click turn off
		 **/
		onClickTurnOff: function () {
			this.model.setAllColorLight("#000000");
		},

		/**
		 * Callback when the create modal is shown. Build it before show. Do not build the fairylights widget now, because elements width are not known.
		 **/
		onShowCreateModal: function () {
			var self = this;

			self.currentModal = new ModalCreationView({
				el: "#modal-create-pattern",
				model: self.model
			});

			self.currentModal.render();
		},

		/**
		 * Callback when the create modal has been shown. Build the fairylights widget at this moment because elements width have been calculated.
		 **/
		onCreateModalShown: function () {
			this.currentModal.buildFairylightWidget("div-fairylight-widget-creation");
		},

		/**
		 * Callback when the create modal has been hidden. We destroy the modal contents.
		 **/
		onCreateModalHidden: function () {
			$("#modal-create-pattern").empty();
			this.buildFairylightWidget("div-fairylight-widget", false);
		},

		/**
		 * Callback when the manage modal is shown. Build it before show.
		 **/
		onShowManageModal: function () {
			var self = this;

			self.currentModal = new ModalManageView({
				el: "#modal-manage-pattern",
				model: self.model
			});

			self.currentModal.render();
		},

		/**
		 * Callback when the manage modal has been shown. Build the fairylights widget at this moment because elements width have been calculated.
		 **/
		onManageModalShown: function () {
			this.currentModal.buildFairylightWidget("div-fairylight-widget-manage");
		},

		/**
		 * Callback when the manage modal has been hidden. We destroy the modal contents.
		 **/
		onManageModalHidden: function () {
			$("#modal-manage-pattern").empty();
		},

		/**
		 * Callback on click select all leds
		 **/
		onClickSelectAll: function () {
			var self = this;
			this.currentSelectedLED = [];
			_.each(this.getJSONArrayLeds(), function (led) {
				self.currentSelectedLED.push(led);
			});
			this.updateFairylightWidget();
		},

		/**
		 * Callback on click deselect all leds
		 **/
		onClickDeselectAll: function () {
			this.currentSelectedLED = [];
			this.updateFairylightWidget();
		},

		autoupdate: function () {
			FairyLightsView.__super__.autoupdate.apply(this);


			var lampButton = "";
			if (this.model.get("state") === "true" || this.model.get("state") === true) {
				lampButton = "<span data-i18n='devices.lamp.action.turnOff'></span>";
			} else {
				lampButton = "<span data-i18n='devices.lamp.action.turnOn'></span>";
			}
			this.$el.find("#lamp-button").html(lampButton);

			this.buildFairylightState();
			this.buildFairylightWidget("div-fairylight-widget", false);
			//			this.updateFairylightWidget();
			this.buildDropdownPatterns();
			this.updateCmdButtonVisibilty();

			// translate the view
			this.$el.i18n();
		},
		/**
		 * Render the detailled view of a device
		 */
		render: function () {
			var self = this;

			if (!appRouter.isModalShown) {

				var lamp = this.model;

				this.$el.html(this.template({
					device: lamp,
					sensorImg: ["app/img/fairylights.jpg"],
					sensorType: $.i18n.t("devices.fairylights.name.singular"),
					places: places,
					deviceDetails: this.tplFairyLights
				}));

				this.buildFairylightWidget("div-fairylight-widget", false);
				this.buildFairylightState();
				this.buildDropdownPatterns();
				this.updateCmdButtonVisibilty();

				// if the lamp is on, we allow the user to pick a color
				this.renderColorWheel();

				this.resize($(".scrollable"));

				// translate the view
				this.$el.i18n();

				return this;
			}
		},
		/**
		 * Render the color wheel for the Philips Hue
		 */
		renderColorWheel: function () {
			var self = this;
			$moving = "colors";
			$mousebutton = 0;

			$("#colorPickerLi").bind("mousedown", function (a) {
				if ($(a.target).parents().andSelf().hasClass("picker-colors")) {
					//a.preventDefault();
					$mousebutton = 1;
					$moving = "colors";
					moveColor(a, "");
				}
				if ($(a.target).parents().andSelf().hasClass("picker-hues")) {
					//a.preventDefault();
					$mousebutton = 1;
					$moving = "hues";
					moveHue(a, "");
				}
			}).bind("mouseup", function (a) {
				//a.preventDefault();
				$mousebutton = 0;
				$moving = "";
				self.colorchanged();

			}).bind("mousemove", function (a) {
				//a.preventDefault();
				if ($mousebutton == 1) {
					if ($moving == "colors") {
						moveColor(a, "");
					} else if ($moving == "hues") {
						moveHue(a, "");
					}
				}

			});

			$(document).ready(function () {
				var arrayLed = self.model.get("leds");
				if (!Array.isArray(arrayLed)) {
					arrayLed = $.parseJSON(arrayLed);
				}
				// Initialize widget to the color of the first LED 
				if (arrayLed[0].color) {
					// Check if first led has color before
					moveColorByHex(expandHex(arrayLed[0].color, ""));
				} else {
					moveColorByHex(expandHex("#ffffff"), "");
				}
			});

		},

		/**
		 * Method to check if the fairylight is ON. It is if one LED is not in black
		 * @return the first LED which is not in black if there is one, undefined otherwise
		 **/
		isFairyLightOn: function () {
			// Get the Led array
			var arrayLed = this.model.get("leds");
			if (!Array.isArray(arrayLed)) {
				arrayLed = $.parseJSON(arrayLed);
			}

			// Function to check one led
			var functionIsLEDOn = function (led) {
				return led.color != "#000000";
			};

			// If find one led not in black return it
			return _.find(arrayLed, functionIsLEDOn);
		},

		/**
		 * Method to add the state html element.
		 **/
		buildFairylightState: function () {
			var self = this;

			var state = ""

			if (self.isFairyLightOn()) {
				state = "<span class='label label-yellow' data-i18n='devices.lamp.status.turnedOn'></span>";
			} else {
				state = "<span class='label label-default' data-i18n='devices.lamp.status.turnedOff'></span>";
			}
			this.$el.find("#div-fairylight-state").html(state);
		},

		/**
		 * Method to update the state of the command buttons
		 **/
		updateCmdButtonVisibilty: function () {
			var self = this;
			if (self.isFairyLightOn()) {
				$("#btn-cmd-turnon-color").hide();
				$("#btn-cmd-turnon-pattern").hide();
				$("#btn-cmd-pattern-set").show();
				$("#btn-cmd-turnoff").show();
				$("#btn-cmd-color-set").show();

			} else {
				$("#btn-cmd-turnon-color").show();
				$("#btn-cmd-turnon-pattern").show();
				$("#btn-cmd-pattern-set").hide();
				$("#btn-cmd-turnoff").hide();
				$("#btn-cmd-color-set").hide();
			}

			// Disable pattern commands if no patterns
			var objPatterns = self.model.get("patterns");
			if (typeof objPatterns === 'string') {
				objPatterns = $.parseJSON(objPatterns);
			}
			if (Object.keys(objPatterns).length > 0) {
				$("#btn-cmd-pattern-set").removeClass("disabled");
				$("#btn-manage-patterns").removeClass("disabled");
			} else {
				$("#btn-cmd-pattern-set").addClass("disabled");
				$("#btn-manage-patterns").addClass("disabled");
			}
		},

		buildFairylightWidget: function (idElementToBuild, isEditable) {
			var self = this;

			// Variables needed to place elements
			var widthDiv, height, nbCircle, spacement, circleWidthDefault, circleWidthAvailable, circleWidthFinal, arrayLed;

			widthDiv = $("#" + idElementToBuild).width();
			height = 25;

			svg = d3.select("#" + idElementToBuild).select("svg")
				.attr("width", widthDiv)
				.attr("height", height);

			nbCircle = 25;
			spacement = 8;
			circleWidthDefault = 18;
			circleWidthAvailable = widthDiv / (nbCircle + spacement);
			circleWidthFinal = (circleWidthAvailable < circleWidthDefault) ? circleWidthAvailable : circleWidthDefault;

			arrayLed = self.model.get("leds");
			if (!Array.isArray(arrayLed)) {
				arrayLed = $.parseJSON(arrayLed);
			}

			nodesLED = svg.selectAll(".groupLed")
				.data(this.getJSONArrayLeds());

			nodesLED.enter()
				.append("svg:g")
				.attr("class", "groupLed")
				.on("click", function (led) {
					// Test if we have changed color. If true, we deselect the previous leds selection.
					if (self.stateColorChanged) {
						self.currentSelectedLED = [];
						self.stateColorChanged = false;
					}

					var ledInCurrentSelected = _.findWhere(self.currentSelectedLED, {
						id: led.id
					});

					if (ledInCurrentSelected) {
						self.currentSelectedLED.splice(_.indexOf(self.currentSelectedLED, ledInCurrentSelected), 1);
					} else {
						self.currentSelectedLED.push(led);
					}
					console.log(self.currentSelectedLED);
					self.updateFairylightWidget();
				})
				.append("circle")
				.attr("class", "nodeLed")
				.attr("cx", function (n) {
					var index = _.indexOf(arrayLed, _.findWhere(arrayLed, {
						id: n.id
					}));
					return (spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index;
				})
				.attr("cy", height / 2)
				.attr("r", circleWidthFinal / 2);

			self.updateFairylightWidget();
		},

		updateFairylightWidget: function () {
			var self = this;

			// Variables needed to place elements
			var widthDiv, height, nbCircle, spacement, circleWidthDefault, circleWidthAvailable, circleWidthFinal, arrayLed;

			widthDiv = $("#div-fairylight-widget").width();
			height = 25;

			svg = d3.select("#div-fairylight-widget").select("svg")
				.attr("width", widthDiv)
				.attr("height", height);

			nbCircle = 25;
			spacement = 8;
			circleWidthDefault = 18;
			circleWidthAvailable = widthDiv / (nbCircle + spacement);
			circleWidthFinal = (circleWidthAvailable < circleWidthDefault) ? circleWidthAvailable : circleWidthDefault;

			arrayLed = self.model.get("leds");
			if (!Array.isArray(arrayLed)) {
				arrayLed = $.parseJSON(arrayLed);
			}

			nodesLED.each(function (led) {

				//Selected element, if found. Test it like boolean..
				var inSelection = _.findWhere(self.currentSelectedLED, {
					id: led.id
				});
				d3.select(this).select(".nodeLed")
					.attr("stroke", "white")
					.attr("fill", led.color)
					.attr("stroke-width", 1);

				if (_.findWhere(self.currentSelectedLED, {
						id: led.id
					})) {
					// inSelection = Aura selection
					if (d3.select(this).selectAll(".auraSelection").empty()) {
						d3.select(this).insert("circle", ".nodeLed")
							.attr("class", "auraSelection")
							.attr("fill", "#898989")
							.attr("cx", function (n) {
								var index = led.id;
								return (spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index;
							})
							.attr("cy", height / 2)
							.attr("r", circleWidthFinal / 1.6);
					}

				} else {
					// !inSelection = No aura selection
					if (!d3.select(this).selectAll(".auraSelection").empty()) {
						d3.select(this).selectAll(".auraSelection").remove();
					}


				}

			});
		},

		/**
		 *
		 * @param LEDsChanged : JSONArray of the Led changed
		 */
		changeColorLEDs: function (LEDsChanged, color) {
			var self = this;

			// Special case if we selected all leds, call setAllColor
			if (LEDsChanged.length === self.getJSONArrayLeds().length) {
				self.model.setAllColorLight(color);
			} else {
				_.each(LEDsChanged, function (led) {
					self.model.setOneColorLight(led.id, color);
				});
			}

			// Color changed, if we select new led, reset selection
			self.stateColorChanged = true;
		},

		/**
		 * Method to build the dropdown of patterns
		 */
		buildDropdownPatterns: function () {
			var self = this;
			var listPattern = $('#dpd-patterns');

			// Always clear dpd before reappend all patterns
			$('#dpd-patterns').empty();

			var objPatterns = self.model.get("patterns");
			if (typeof objPatterns === 'string') {
				objPatterns = $.parseJSON(objPatterns);
			}

			$.each(objPatterns, function (keyPattern) {
				listPattern.append("<li><a href='#'>" + keyPattern + "</a></li>");
			});
		},

		getJSONArrayLeds: function () {
			var arrayLed = this.model.get("leds");
			if (!Array.isArray(arrayLed)) {
				arrayLed = $.parseJSON(arrayLed);
			}
			return arrayLed;
		}
	});
	return FairyLightsView
});