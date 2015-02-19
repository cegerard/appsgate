define([
  "app",
  "raphael",
  "views/device/details/details",
  "text!templates/devices/details/phillipsHue.html",
  "colorwidget"
  ], function(App, Raphael, DeviceDetailsView, phillipsHueDetailTemplate,colorWidgetJs) {

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

        $.ajaxSetup({ cache: false });

        PhillipsHueView.__super__.initialize.apply(self, arguments);

        $.extend(self.__proto__.events, PhillipsHueView.__super__.events);

          dispatcher.on(this.model.get("id"), function(updatedVariableJSON) {
              if(updatedVariableJSON.varName == "colorChanged"){
                  hexcolor=JSON.parse(updatedVariableJSON.value).rgbcolor;
                  moveColorByHex(expandHex(hexcolor));
              }

          });

      },
      /**
      * Callback to toggle a lamp - used when the displayed device is a lamp (!)
      */
      onToggleLampButton: function() {
        if (this.model.get("value") === "true" || this.model.get("value") === true) {
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
      onBlinkLampButton: function(e) {
        e.preventDefault();
        var lamp = devices.get($(e.currentTarget).attr("id"));
        // send the message to the backend
        lamp.remoteControl("blink30", []);

        return false;
      },
      colorchanged: function(){
        var lamp = this.model;
        var rgb = $("#colorbg").css("background-color");
        var hsl = Raphael.rgb2hsb(rgb);
        var hH=Math.round(hsl.h* 65535);
        var hS=Math.round(hsl.s* 255);
        var hB=Math.round(hsl.b* 255);
        lamp.set({"color": hH, "saturation": hS, "brightness": hB});
        lamp.sendFullColor();
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
      renderColorWheel: function() {
          var self=this;
          $moving = "colors";
          $mousebutton = 0;

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
                  }else if ($moving == "hues") {
                      moveHue(a);
                  }
                  self.colorchanged();
              }

          });

          $(document).ready(function(){
              var lamp = self.model;
              moveColorByHex(expandHex(lamp.getCurrentColor()));
          });

      }
    });
    return PhillipsHueView
  });
