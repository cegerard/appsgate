define([
  "app",
  "raphael",
  "views/device/details/details",
  "text!templates/devices/details/phillipsHue.html",
  "colorwheel"
  ], function(App, Raphael, DeviceDetailsView, phillipsHueDetailTemplate) {

    var PhillipsHueView = {};
    // detailled view of a device
    PhillipsHueView = DeviceDetailsView.extend({
      tplPhillipsHue: _.template(phillipsHueDetailTemplate),
       events: {
            "click button.toggle-lamp-button": "onToggleLampButton",
            "click button.blink-lamp-button": "onBlinkLampButton",
            "click button.toggle-actuator-button": "onToggleActuatorButton",
        },
      initialize: function() {
        var self = this;
        PhillipsHueView.__super__.initialize.apply(self, arguments);

        $.extend(self.__proto__.events, PhillipsHueView.__super__.events);
      },
      /**
      * Callback to toggle a lamp - used when the displayed device is a lamp (!)
      */
      onToggleLampButton: function() {
        // value can be string or boolean
        // string
        if (typeof this.model.get("value") === "string") {
          if (this.model.get("value") === "true") {
            this.model.set("value", "false");
            this.$el.find(".toggle-lamp-button").text("Allumer");
          } else {
            this.model.set("value", "true");
            this.$el.find(".toggle-lamp-button").text("Eteindre");
          }
          // boolean
        } else {
          if (this.model.get("value")) {
            this.model.set("value", "false");
            this.$el.find(".toggle-lamp-button").text("Allumer");
          } else {
            this.model.set("value", "true");
            this.$el.find(".toggle-lamp-button").text("Eteindre");
          }
        }

        // send the message to the backend
        this.model.save();
      },
      /**
      * Callback to blink a lamp
      *
      * @param e JS mouse event
      */
      onBlinkLampButton: function(e) {
        e.preventDefault();
        var lamp = devices.get($(e.currentTarget).attr("id"));
        // send the message to the backend
        lamp.remoteControl("blink30", []);

        return false;
      },

      /**
      * Set the new color to the lamp
      *
      * @param e JS mouse event
      */
      onChangeColor: function(e) {
        //var lamp = devices.get(Backbone.history.fragment.split("/")[1]);
        //var rgb = $(".picker_h").css("background-color");//var rgb = Raphael.getRGB(10,10,10); //colorWheel.color()
        //var hsl = Raphael.rgb2hsl(rgb);

        //lamp.set({color: Math.floor(hsl.h * 65535), "saturation": Math.floor(hsl.s * 255), "brightness": Math.floor(hsl.l * 255)});

        //var result = lamp.save();

      },
      autoupdate: function() {
        PhillipsHueView.__super__.autoupdate.apply(this);

        var lampState = ""
        if (this.model.get("value")==="true" || this.model.get("value") === true) {
            lampState = "<span class='label label-yellow' data-i18n='devices.lamp.status.turnedOn'></span>";
        } else {
            lampState = "<span class='label label-default' data-i18n='devices.lamp.status.turnedOff'></span>";
        }
        this.$el.find("#lamp-status").html(lampState);

        var lampButton = "";
        if (this.model.get("value") === "true" || this.model.get("value") === true) {
            lampButton = "<span data-i18n='devices.lamp.action.turnOff'></span>";
        } else {
            lampButton = "<span data-i18n='devices.lamp.action.turnOn'></span>";
        }
        this.$el.find("#lamp-button").html(lampButton);

        // get the current color
        var color = Raphael.hsl((this.model.get("color") / 65535), (this.model.get("saturation") / 255), (this.model.get("brightness") / 255));

        // get the current state
        var enabled = this.model.get("value");

        // if the lamp is on, we allow the user to pick a color
        this.renderColorWheel(enabled, color);

        // translate the view
        this.$el.i18n();
      },
      /**
      * Render the detailled view of a device
      */
      render: function() {
        var self = this;

        if (!appRouter.isModalShown) {

          var lamp = this.model;

          this.$el.html(this.template({
            device: lamp,
            sensorImg: ["app/img/sensors/philips-hue.jpg"],
            sensorType: $.i18n.t("devices.lamp.name.singular"),
            places: places,
            deviceDetails: this.tplPhillipsHue
          }));

          // get the current color
          var color = Raphael.hsl((lamp.get("color") / 65535), (lamp.get("saturation") / 255), (lamp.get("brightness") / 255));

          // get the current state
          var enabled = lamp.get("value");

          // if the lamp is on, we allow the user to pick a color
          this.renderColorWheel(enabled, color);


          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
        }
      },
      /**
      * Render the color wheel for the Philips Hue
      */
      renderColorWheel: function(enabled, color) {
        // create the color picker
        var wheelRadius = $(".body-content").outerWidth() / 10 + 80;

        var colorPickerDomElement=$(".color-picker")[0];

        if(colorPickerDomElement){

          $(colorPickerDomElement).html("");

          // instantiate the color wheel
          window.colorWheel = Raphael.colorwheel(colorPickerDomElement, wheelRadius * 2).color(color);

          // bind the events
          if (typeof enabled !== undefined && enabled === "true") {
            // color change enabled
            window.colorWheel.ondrag(null, this.onChangeColor);
          }
          else {
            // color change disabled
            window.colorWheel.onchange(function() {
              window.colorWheel.color(color);
            });
          }

          // update the size of the color picker container
          this.$el.find(".color-picker").height(colorWheel.size2 * 2);
        }


      }
    });
    return PhillipsHueView
  });
