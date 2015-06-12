define([
  "app",
  "raphael",
  "views/device/details/details",
  "text!templates/devices/details/fairylights.html",
  "colorwidget"
  ], function (App, Raphael, DeviceDetailsView, fairyLightsDetailTemplate, colorWidgetJs) {

	var FairyLightsView = {};
	// detailled view of a device
	FairyLightsView = DeviceDetailsView.extend({
		tplFairyLights: _.template(fairyLightsDetailTemplate),

		events: {
			"click #dpd-colors a": "onClickDropdownColors",
			"click #dpd-patterns a": "onClickDropdownPattenrs",
		},

		initialize: function () {
			var self = this;

			self.currentSelectedLED = [];

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
		/**
		 * Callback to toggle a lamp - used when the displayed device is a lamp (!)
		 */
		onToggleLampButton: function () {
			if (this.model.get("state") === "true" || this.model.get("state") === true) {
				this.model.switchOff();
			} else {
				this.model.switchOn();
			}
		},
		colorchanged: function () {
			var rgb = $("#colorbg").css("background-color");
			this.changeColorLEDs(this.currentSelectedLED, Raphael.getRGB(rgb).hex);

		},

		/**
		 * Callback on click colors in dropdown TurnOn with Color
		 **/
		onClickDropdownColors: function (e) {
			e.preventDefault();
		},

		/**
		 * Callback on click colors in dropdown TurnOn with Pattern
		 **/
		onClickDropdownPattenrs: function (e) {
			e.preventDefault();
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

			$("#luminositePickerInputRange").change(function (e) {
				self.model.set("brightness", e.target.value)
				self.model.sendBrightness();
			});


			$("#colorPickerLi").bind("mousedown", function (a) {
				if ($(a.target).parents().andSelf().hasClass("picker-colors")) {
					//a.preventDefault();
					$mousebutton = 1;
					$moving = "colors";
					moveColor(a);
					self.colorchanged();
				}
				if ($(a.target).parents().andSelf().hasClass("picker-hues")) {
					//a.preventDefault();
					$mousebutton = 1;
					$moving = "hues";
					moveHue(a);
					self.colorchanged();
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
						moveColor(a);
					} else if ($moving == "hues") {
						moveHue(a);
					}
					self.colorchanged();
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
					moveColorByHex(expandHex(arrayLed[0].color));
				} else {
					moveColorByHex(expandHex("#ffffff"));
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

				// Disabled button 'Set pattern' if no pattern available
				$("#btn-cmd-pattern-set").prop("disabled", function () {
					return false;
				})
			} else {
				$("#btn-cmd-turnon-color").show();
				$("#btn-cmd-turnon-pattern").show();
				$("#btn-cmd-pattern-set").hide();
				$("#btn-cmd-turnoff").hide();
			}

		},

		buildFairylightWidget: function (idElementToBuild, isEditable) {
			var self = this;

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

			nodesLED = svg.selectAll(".nodeLed")
				.data(arrayLed);

			nodesLED.enter()
				.append("circle")
				.attr("class", "nodeLed")
				.attr("cx", function (n) {
					var index = _.indexOf(arrayLed, n);
					return (spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index;
				})
				.attr("cy", height / 2)
				.attr("r", circleWidthFinal / 2)
				.on("click", function (led) {
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
				});

			self.updateFairylightWidget();
		},

		updateFairylightWidget: function () {
			var self = this;
			nodesLED.each(function (led) {
				d3.select(this)
					.attr("fill", led.color)
					.attr("stroke-width", function (led) {
						if (_.findWhere(self.currentSelectedLED, {
								id: led.id
							})) {
							return 3;
						} else {
							return 1;
						}
					})
					.attr("stroke", "black");
			});
		},

		/**
		 *
		 * @param LEDsChanged : JSONArray of the Led changed
		 */
		changeColorLEDs: function (LEDsChanged, color) {
			var self = this;

			_.each(LEDsChanged, function (led) {
				self.model.setOneColorLight(led.id, color);
			});
		},
	});
	return FairyLightsView
});