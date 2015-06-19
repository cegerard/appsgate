define([
  "app",
  "raphael",
  "text!templates/devices/details/fairylights/fairylightsModalCreation.html",
  "colorwidget"
  ], function (App, Raphael, fairyLightsModalCreationTemplate, colorWidgetJs) {

	var ModalCreationView = {};
	// detailled view of a device
	ModalCreationView = Backbone.View.extend({
		tplFairyLightsModalCreation: _.template(fairyLightsModalCreationTemplate),

		events: {
			"click .valid-button": "onClickValidCreation"
		},

		initialize: function (options) {
			var self = this;

			self.currentSelectedLED = [];
			self.initialColor = "#000000";
			self.model = options.model || {};
			self.leds = self.cloneLEDs(self.model.get("leds"));

			self.initColorLEDs();

		},

		/**
		 * Callback on click validation
		 **/
		onClickValidCreation: function (event) {
			var patternName = $("#input-new-pattern").val(),
				patternLEDs = this.getPatternLEDs();

			// hide the modal
			$("#modal-create-pattern").modal("hide");
			this.model.addPattern(patternName, patternLEDs);
		},

		/**
		 * Function to clone the leds
		 * @return Clone array of leds
		 **/
		cloneLEDs: function (leds) {
			var clone = [];
			var arrayLed = leds;
			if (!Array.isArray(arrayLed)) {
				arrayLed = $.parseJSON(arrayLed);
			}
			_.each(arrayLed, function (led) {
				clone.push($.extend(true, {}, led));
			});

			return clone;
		},

		/**
		 * Render the detailled view of a device
		 */
		render: function () {
			var self = this;

			this.$el.html(this.tplFairyLightsModalCreation({}));

			// Set the height of the widget to avoid to give all div's height to the svg
			d3.select("#div-fairylight-widget-creation").select("svg").attr("height", 25);

			this.renderColorWheel();

			// translate the view
			this.$el.i18n();

			return this;
		},

		colorchanged: function () {
			var rgb = $("#colorPickerLi.create-modal").children("#colorbg").css("background-color");
			this.changeColorLEDs(this.currentSelectedLED, Raphael.getRGB(rgb).hex);
			this.buildFairylightWidget("div-fairylight-widget-creation", false);
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


			$("#colorPickerLi.create-modal").bind("mousedown", function (a) {
				if ($(a.target).parents().andSelf().hasClass("picker-colors")) {
					//a.preventDefault();
					$mousebutton = 1;
					$moving = "colors";
					moveColor(a, ".create-modal");
				}
				if ($(a.target).parents().andSelf().hasClass("picker-hues")) {
					//a.preventDefault();
					$mousebutton = 1;
					$moving = "hues";
					moveHue(a, ".create-modal");
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
						moveColor(a, ".create-modal");
					} else if ($moving == "hues") {
						moveHue(a, ".create-modal");
					}
				}

			});

			$(document).ready(function () {
				var arrayLed = self.leds;
				if (!Array.isArray(arrayLed)) {
					arrayLed = $.parseJSON(arrayLed);
				}
				// Initialize widget to the color of the first LED 
				if (arrayLed[0].color) {
					// Check if first led has color before
					moveColorByHex(expandHex(arrayLed[0].color), ".create-modal");
				} else {
					moveColorByHex(expandHex("#ffffff"), ".create-modal");
				}
			});

		},

		/**
		 * Method to initiate the color leds -> black
		 **/
		initColorLEDs: function () {
			var self = this;
			_.each(this.leds, function (led) {
				led.color = self.initialColor;
			});
		},
		
		buildFairylightWidget: function (idElementToBuild) {
			var self = this;

			var widthDiv, height, nbCircle, spacement, circleWidthDefault, circleWidthAvailable, circleWidthFinal, arrayLed;

			widthDiv = $("#" + idElementToBuild).width();
			height = 25;

			// Get the svg and set its width to the with available 
			svg = d3.select("#" + idElementToBuild).select("svg")
				.attr("width", widthDiv);

			nbCircle = 25;
			spacement = 8;
			circleWidthDefault = 18;
			circleWidthAvailable = widthDiv / (nbCircle + spacement);
			circleWidthFinal = (circleWidthAvailable < circleWidthDefault) ? circleWidthAvailable : circleWidthDefault;

			arrayLed = self.leds;
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
		 * Function to get the LEDs modified for the pattern
		 * @return : Array of JSON object representing LEDs of pattern (!= of the initial color)
		 **/
		getPatternLEDs: function () {
			var self = this;
			return _.filter(this.leds, function (led) {
				return led.color !== self.initialColor;
			});
		},

		/**
		 *
		 * @param LEDsChanged : JSONArray of the Led changed
		 */
		changeColorLEDs: function (LEDsChanged, color) {
			var self = this;

			_.each(LEDsChanged, function (led) {
				led.color = color;
			});
		},
	});
	return ModalCreationView
});