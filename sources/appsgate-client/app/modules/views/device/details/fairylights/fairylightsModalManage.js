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
			"click #btn-valid-manage-pattern.valid-button": "onClickValidEdit",
			"click #btn-delete-manage-pattern.delete-button": "onClickDeletePattern",
			"change #select-pattern": "onChangePattern",

			"click .btn-widget-select-all.btn-manage": "onClickSelectAll",
			"click .btn-widget-deselect-all.btn-manage": "onClickDeselectAll",
			"click .btn-widget-ignore-pattern.btn-manage": "onClickIgnore"
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
		onClickValidEdit: function (event) {
			if (this.checkPatternValidation()) {
				var patternName = $("#select-pattern").val(),
					patternLEDs = this.getPatternLEDs();

				// hide the modal
				$("#modal-manage-pattern").modal("hide");
				this.model.addPattern(patternName, patternLEDs);
			}
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

			self.initStateLEDs();
			self.applyPattern();
		},

		/**
		 * Callback on click select all leds
		 **/
		onClickSelectAll: function () {
			var self = this;
			this.currentSelectedLED = [];
			_.each(this.leds, function (led) {
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
		 * Method to check if the pattern is valid
		 * @return : true if at least one led in pattern
		 **/
		checkPatternValidation: function () {

			// Check led in pattern
			if (this.getPatternLEDs().length === 0) {
				$("#modal-manage-pattern .valid-button").addClass("disabled");
				$("#modal-manage-pattern .valid-button").addClass("valid-disabled");

				return false;
			}

			$("#modal-manage-pattern .valid-button").removeClass("disabled");
			$("#modal-manage-pattern .valid-button").removeClass("valid-disabled");

			return true;
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
			this.updateFairylightWidget();
		},

		/**
		 * Render the color wheel for the Philips Hue
		 */
		renderColorWheel: function () {
			var self = this;
			$moving = "colors";
			$mousebutton = 0;

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
				// Initialize widget to the color of the first LED 
				if (self.leds[0].color) {
					// Check if first led has color before
					moveColorByHex(expandHex(self.leds[0].color), ".create-modal");
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

		/**
		 * Main method to build the widget of fairylights
		 * @param : idElementToBuild String id of the element where build the widget. It will mainly create the svg element and the initial state of fairy
		 **/
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

		/**
		 * Method that update the actual fairyligth widget. It will add the different dynamic element when update state of leds. Changing state when adding led to selection, adding led to pattern, etc.
		 **/
		updateFairylightWidget: function () {
			var self = this;

			// Check to update valid button
			self.checkPatternValidation();

			var widthDiv, height, nbCircle, spacement, circleWidthDefault, circleWidthAvailable, circleWidthFinal;

			widthDiv = $("#div-fairylight-widget-manage").width();
			height = 25;

			// Get the svg and set its width to the with available 
			svg = d3.select("#div-fairylight-widget-manage").select("svg")
				.attr("width", widthDiv);

			nbCircle = 25;
			spacement = 8;
			circleWidthDefault = 18;
			circleWidthAvailable = widthDiv / (nbCircle + spacement);
			circleWidthFinal = (circleWidthAvailable < circleWidthDefault) ? circleWidthAvailable : circleWidthDefault;

			nodesLED.each(function (led) {

				// Selected element, if found. Test it like boolean..
				var inSelection = _.findWhere(self.currentSelectedLED, {
					id: led.id
				});

				if (led.inPattern && inSelection) {

					// inPattern = No cross & color
					if (!d3.select(this).selectAll("line").empty()) {
						d3.select(this).selectAll("line").remove();
					}

					d3.select(this).select(".nodeLed")
						.attr("stroke", "white")
						.attr("fill", led.color);

					// inSelection = Aura selection
					if (d3.select(this).selectAll(".auraSelection").empty()) {
						d3.select(this).insert("circle", ".nodeLed")
							.attr("class", "auraSelection")
							.attr("fill", "#898989")
							.attr("cx", function (n) {
								var index = _.indexOf(self.leds, inSelection);
								return (spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index;
							})
							.attr("cy", height / 2)
							.attr("r", circleWidthFinal / 1.6);
					}

				} else if (led.inPattern && !inSelection) {

					// inPattern = No cross & color
					if (!d3.select(this).selectAll("line").empty()) {
						d3.select(this).selectAll("line").remove();
					}

					d3.select(this).select(".nodeLed")
						.attr("stroke", "white")
						.attr("fill", led.color);

					// !inSelection = No aura selection
					if (!d3.select(this).selectAll(".auraSelection").empty()) {
						d3.select(this).select(".auraSelection").remove();
					}


				} else if (!led.inPattern && inSelection) {

					// inSelection = Aura selection
					if (d3.select(this).selectAll(".auraSelection").empty()) {
						d3.select(this).insert("circle", ".nodeLed")
							.attr("class", "auraSelection")
							.attr("fill", "#898989")
							.attr("cx", function (n) {
								var index = _.indexOf(self.leds, inSelection);
								return (spacement / 2) + (circleWidthFinal / 2) + ((spacement / 2) + circleWidthFinal) * index;
							})
							.attr("cy", height / 2)
							.attr("r", circleWidthFinal / 1.6);
					}

					// !inPattern = Cross & No Color & black stroke
					if (d3.select(this).select("line").empty()) {
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

					d3.select(this).select(".nodeLed")
						.attr("fill", "#ffffff")
						.attr("stroke", "black");


				} else if (!led.inPattern && !inSelection) {

					// !Pattern = Cross & No Color & black stroke
					if (d3.select(this).select("line").empty()) {
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

					d3.select(this).select(".nodeLed")
						.attr("fill", "#ffffff")
						.attr("stroke", "black");

					// !inSelection = No aura selection
					if (!d3.select(this).selectAll(".auraSelection").empty()) {
						d3.select(this).select(".auraSelection").remove();
					}
				}

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
				return led.inPattern;
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
						ledFairy.inPattern = true;
					}
				});

				// Reinite selected led
				this.currentSelectedLED = [];
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
	return ModalManageView
});