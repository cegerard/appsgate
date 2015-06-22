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
			"click .valid-button": "onClickValidCreation",

			"click .btn-widget-select-all.btn-create": "onClickSelectAll",
			"click .btn-widget-deselect-all.btn-create": "onClickDeselectAll",
			"click .btn-widget-ignore-pattern.btn-create": "onClickIgnore"
		},

		initialize: function (options) {
			var self = this;

			self.currentSelectedLED = [];
			self.stateColorChanged = false;
			self.model = options.model || {};
			self.leds = self.cloneLEDs(self.model.get("leds"));

			self.initStateLEDs();

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
		 * Callback on click select all leds
		 **/
		onClickSelectAll: function () {
			this.currentSelectedLED = this.leds;
			this.updateFairylightWidget();
		},

		/**
		 * Callback on click deselect all leds
		 **/
		onClickDeselectAll: function () {
			this.currentSelectedLED = [];
			this.updateFairylightWidget();
		},

		/**
		 * Callback on click ignore leds in pattern
		 **/
		onClickIgnore: function () {
			// Set 'inPattern' to false for all the selection
			_.each(this.currentSelectedLED, function (led) {
				led.inPattern = false;
			});
			this.currentSelectedLED = [];
			this.updateFairylightWidget();
		},

		/**
		 * Function to clone the leds
		 * @return Clone array of leds
		 **/
		cloneLEDs: function (leds) {
			var clone = [];
			var arrayLed = this.getJSONArrayLeds();
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
			this.updateFairylightWidget();
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
		initStateLEDs: function () {
			var self = this;
			_.each(this.leds, function (led) {
				led.inPattern = false;
			});
		},

		buildFairylightWidget: function (idElementToBuild) {
			var self = this;

			var widthDiv, height, nbCircle, spacement, circleWidthDefault, circleWidthAvailable, circleWidthFinal;

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
			console.log("circlewidth build : " + circleWidthFinal);

			nodesLED = svg.selectAll(".nodeLed")
				.data(self.leds);

			nodesLED.enter()
				.append("svg:g")
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
					self.updateFairylightWidget();
				})
				.append("circle")
				.attr("class", "nodeLed")
				.attr("cx", function (n) {
					var index = _.indexOf(self.leds, n);
					return (spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index;
				})
				.attr("cy", height / 2)
				.attr("r", circleWidthFinal / 2);

			self.updateFairylightWidget();
		},

		updateFairylightWidget: function () {
			var self = this;

			var widthDiv, height, nbCircle, spacement, circleWidthDefault, circleWidthAvailable, circleWidthFinal;

			widthDiv = $("#div-fairylight-widget-creation").width();
			height = 25;

			// Get the svg and set its width to the with available 
			svg = d3.select("#div-fairylight-widget-creation").select("svg")
				.attr("width", widthDiv);

			nbCircle = 25;
			spacement = 8;
			circleWidthDefault = 18;
			circleWidthAvailable = widthDiv / (nbCircle + spacement);
			circleWidthFinal = (circleWidthAvailable < circleWidthDefault) ? circleWidthAvailable : circleWidthDefault;
			console.log("circlewidth update : " + circleWidthFinal);

			nodesLED.each(function (led) {
				//				d3.select(this).select("circle")
				//					.attr("fill", led.color)
				//					.attr("stroke-width", function (led) {
				//						if (_.findWhere(self.currentSelectedLED, {
				//								id: led.id
				//							})) {
				//							return 3;
				//						} else {
				//							return 1;
				//						}
				//					})
				//					.attr("stroke", "black");

				// Selected element, if found. Test it like boolean..
				var inSelection = _.findWhere(self.currentSelectedLED, {
					id: led.id
				});

				if (led.inPattern && inSelection) {
					d3.select(this).select("circle")
						.attr("stroke-width", 3)
						.attr("stroke", "black")
						.attr("fill", led.color);
					d3.select(this).selectAll("line").remove();
				} else if (led.inPattern && !inSelection) {
					d3.select(this).select("circle")
						.attr("stroke-width", 1)
						.attr("stroke", "black")
						.attr("fill", led.color);
					d3.select(this).selectAll("line").remove();
				} else if (!led.inPattern && inSelection) {
					// LED in !Pattern
					d3.select(this).select("circle")
						.attr("fill", "#ffffff")
						.attr("stroke-width", 3)
						.attr("stroke", "black");

					d3.select(this).append("line")
						.attr("opacity", 1)
						.attr("x1", function (n) {
							var index = _.indexOf(self.leds, n);
							return ((spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index) - (circleWidthFinal / 2) + 2;
						})
						.attr("y1", height / 4)
						.attr("x2", function (n) {
							var index = _.indexOf(self.leds, n);
							return ((spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index) + (circleWidthFinal / 2) - 2;
						})
						.attr("y2", (height / 4) * 3)
						.attr("stroke", "black");

					d3.select(this).append("line")
						.attr("opacity", 1)
						.attr("x1", function (n) {
							var index = _.indexOf(self.leds, n);
							return ((spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index) - (circleWidthFinal / 2) + 2;
						})
						.attr("y1", (height / 4) * 3)
						.attr("x2", function (n) {
							var index = _.indexOf(self.leds, n);
							return ((spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index) + (circleWidthFinal / 2) - 2;
						})
						.attr("y2", (height / 4))
						.attr("stroke", "black");

				} else if (!led.inPattern && !inSelection) {
					// LED in !Pattern
					d3.select(this).select("circle")
						.attr("fill", "#ffffff")
						.attr("stroke-width", 1)
						.attr("stroke", "black");

					d3.select(this).append("line")
						.attr("opacity", 1)
						.attr("x1", function (n) {
							var index = _.indexOf(self.leds, n);
							return ((spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index) - (circleWidthFinal / 2) + 2;
						})
						.attr("y1", height / 4)
						.attr("x2", function (n) {
							var index = _.indexOf(self.leds, n);
							return ((spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index) + (circleWidthFinal / 2) - 2;
						})
						.attr("y2", (height / 4) * 3)
						.attr("stroke", "black");

					d3.select(this).append("line")
						.attr("opacity", 1)
						.attr("x1", function (n) {
							var index = _.indexOf(self.leds, n);
							return ((spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index) - (circleWidthFinal / 2) + 2;
						})
						.attr("y1", (height / 4) * 3)
						.attr("x2", function (n) {
							var index = _.indexOf(self.leds, n);
							return ((spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index) + (circleWidthFinal / 2) - 2;
						})
						.attr("y2", (height / 4))
						.attr("stroke", "black");
				}

			});
		},

		/**
		 * Function to get the LEDs modified for the pattern
		 * @return : Array of JSON object representing LEDs of pattern (!= of the initial color)
		 **/
		getPatternLEDs: function () {
			return _.filter(this.leds, function (led) {
				return led.inPattern;
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

				// If led was not in pattern, add it
				if (!led.inPattern) {
					led.inPattern = true;
				}
			});

			// Color changed, if we select new led, reset selection
			self.stateColorChanged = true;
		},

		getJSONArrayLeds: function () {
			var arrayLed = this.model.get("leds");
			if (!Array.isArray(arrayLed)) {
				arrayLed = $.parseJSON(arrayLed);
			}
			return arrayLed;
		}
	});
	return ModalCreationView
});