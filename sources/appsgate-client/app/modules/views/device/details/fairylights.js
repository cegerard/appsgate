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
			"click button.toggle-lamp-button": "onToggleLampButton",
			"click button.blink-lamp-button": "onBlinkLampButton",
			"click button.toggle-actuator-button": "onToggleActuatorButton"
		},
		initialize: function () {
			var self = this;

			$.ajaxSetup({
				cache: false
			});

			FairyLightsView.__super__.initialize.apply(self, arguments);

			$.extend(self.__proto__.events, FairyLightsView.__super__.events);

			dispatcher.on(this.model.get("id"), function (updatedVariableJSON) {
				console.log("toto");
				if (updatedVariableJSON.varName == "colorChanged") {
					hexcolor = JSON.parse(updatedVariableJSON.value).rgbcolor;
					moveColorByHex(expandHex(hexcolor));
				}
				if (updatedVariableJSON == "leds") {
					self.buildFairylightWidget("div-fairylight-widget", false);
				}

			});

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
		/**
		 * Callback to blink a lamp
		 *
		 * @param e JS mouse event
		 */
		onBlinkLampButton: function (e) {
			e.preventDefault();
			var lamp = devices.get($(e.currentTarget).attr("id"));
			// send the message to the backend
			lamp.remoteControl("blink30", []);

			return false;
		},
		colorchanged: function () {
			var lamp = this.model;
			var rgb = $("#colorbg").css("background-color");
			var hsl = Raphael.rgb2hsb(rgb);
			var hH = Math.round(hsl.h * 65535);
			var hS = Math.round(hsl.s * 255);
			var hB = Math.round(hsl.b * 255);
			lamp.set({
				"color": hH,
				"saturation": hS,
				"brightness": hB
			});
			lamp.sendFullColor();
		},
		autoupdate: function () {
			FairyLightsView.__super__.autoupdate.apply(this);

			var lampState = ""
			if (this.model.get("state") === "true" || this.model.get("state") === true) {
				lampState = "<span class='label label-yellow' data-i18n='devices.lamp.status.turnedOn'></span>";
			} else {
				lampState = "<span class='label label-default' data-i18n='devices.lamp.status.turnedOff'></span>";
			}
			this.$el.find("#lamp-status").html(lampState);

			var lampButton = "";
			if (this.model.get("state") === "true" || this.model.get("state") === true) {
				lampButton = "<span data-i18n='devices.lamp.action.turnOff'></span>";
			} else {
				lampButton = "<span data-i18n='devices.lamp.action.turnOn'></span>";
			}
			this.$el.find("#lamp-button").html(lampButton);

			this.buildFairylightWidget("div-fairylight-widget", false);

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
				var lamp = self.model;
				//				moveColorByHex(expandHex(lamp.getCurrentColor()));
			});

		},

		buildFairylightWidget: function (idElementToBuild, isEditable) {
			var self = this;

			var widthDiv = $("#" + idElementToBuild).width();
			var height = 25;

			var svg = d3.select("#" + idElementToBuild).select("svg")
				.attr("width", widthDiv)
				.attr("height", height);

			var nbCircle = 25;
			var spacement = 8;
			var circleWidthDefault = 18;
			var circleWidthAvailable = widthDiv / (nbCircle + spacement);
			var circleWidthFinal = (circleWidthAvailable < circleWidthDefault) ? circleWidthAvailable : circleWidthDefault;

			var arrayLed = self.model.get("leds");
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
				.attr("r", circleWidthFinal / 2);

			nodesLED.each(function (led) {
				d3.select(this)
					.attr("fill", led.color);
			});

		},
	});
	return FairyLightsView
});