define([
  "app",
  "raphael",
  "text!templates/devices/details/fairylights/fairylightsModalManage.html",
  "colorwidget"
  ], function (App, Raphael, fairyLightsModalManageTemplate, colorWidgetJs) {

	var ModalManageView = {};
	// detailled view of a device
	ModalManageView = Backbone.View.extend({
		tplFairyLightsModalManage: _.template(fairyLightsModalManageTemplate),

		events: {
			"click .valid-button": "onClickValidEdit",
			"click .delete-button": "onClickDeletePattern",
			"change #select-pattern": "onChangePattern"
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
		onClickValidEdit: function (event) {
			var patternName = $("#select-pattern").val(),
				patternLEDs = this.getPatternLEDs();

			// hide the modal
			$("#modal-manage-pattern").modal("hide");
			this.model.addPattern(patternName, patternLEDs);
		},

		/**
		 * Callback on click delete
		 **/
		onClickDeletePattern: function (event) {
			var patternName = $("#select-pattern").val();

			// hide the modal
			$("#modal-manage-pattern").modal("hide");
			this.model.removePattern(patternName);
		},

		/**
		 * Callback on change select pattern
		 **/
		onChangePattern: function (event) {
			var self = this;

			self.initColorLEDs();
			self.applyPattern();
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

			this.$el.html(this.tplFairyLightsModalManage({}));

			// Set the height of the widget to avoid to give all div's height to the svg
			d3.select("#div-fairylight-widget-manage").select("svg").attr("height", 25);

			this.buildPatternsSelect();
			this.applyPattern();
			this.renderColorWheel();

			// translate the view
			this.$el.i18n();

			return this;
		},

		colorchanged: function () {
			var rgb = $("#colorPickerLi.create-modal").children("#colorbg").css("background-color");
			this.changeColorLEDs(this.currentSelectedLED, Raphael.getRGB(rgb).hex);
			this.buildFairylightWidget("div-fairylight-widget-manage", false);
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
					self.colorchanged();
				}
				if ($(a.target).parents().andSelf().hasClass("picker-hues")) {
					//a.preventDefault();
					$mousebutton = 1;
					$moving = "hues";
					moveHue(a, ".create-modal");
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
						moveColor(a, ".create-modal");
					} else if ($moving == "hues") {
						moveHue(a, ".create-modal");
					}
					self.colorchanged();
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
		 * Method to build the select of patterns
		 **/
		buildPatternsSelect: function () {
			var self = this;

			var selectPattern = $("#select-pattern");

			var objPatterns = self.model.get("patterns");
			if (typeof objPatterns === 'string') {
				objPatterns = $.parseJSON(objPatterns);
			}

			$.each(objPatterns, function (keyPattern) {
				selectPattern.append($('<option>', {
					text: keyPattern
				}));
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
		 * Method to apply the selected pattern to the widget
		 **/
		applyPattern: function () {
			var self = this;
			var selectPattern = $("#select-pattern");

			var objPatterns = self.model.get("patterns");
			if (typeof objPatterns === 'string') {
				objPatterns = $.parseJSON(objPatterns);
			}

			var patternNameSelected = $("#select-pattern").val();

			if (patternNameSelected) {

				objPatterns[patternNameSelected].forEach(function (ledPattern) {
					var ledFairy = _.findWhere(self.leds, {
						id: ledPattern.id
					});

					if (ledFairy) {
						ledFairy.color = ledPattern.color;
					}
				});

				self.updateFairylightWidget();
			}
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
	return ModalManageView
});