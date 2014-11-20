define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/lightsensor.html"
  ], function(App, DeviceDetailsView, illuminationDetailTemplate) {

    var LightSensorView = {};
    // detailled view of a device
    LightSensorView = DeviceDetailsView.extend({
      tplIllumination: _.template(illuminationDetailTemplate),
      initialize: function() {
        var self = this;
        LightSensorView.__super__.initialize.apply(this, arguments);

        $.extend(self.__proto__.events, LightSensorView.__super__.events);
      },
      autoupdate: function() {
        LightSensorView.__super__.autoupdate.apply(this);
        this.$el.find("#light-sensor-value").html($.i18n.t("devices.illumination.scale." + this.model.get("label")) + " : " + this.model.getValue() + " Lux");

        // translate the view
        this.$el.i18n();
      },
      /**
      * Render the detailled view of a device
      */
      render: function() {
        var self = this;

        if (!appRouter.isModalShown) {

          this.$el.html(this.template({
            device: this.model,
            sensorImg: ["app/img/sensors/illumination_intern.png", "app/img/sensors/illumination_extern.png"],
            sensorCaption: [$.i18n.t("devices.illumination.caption.intern"), $.i18n.t("devices.illumination.caption.extern")],
            sensorType: $.i18n.t("devices.illumination.name.singular"),
            places: places,
            deviceDetails: this.tplIllumination
          }));

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
        }
      }
    });
    return LightSensorView
  });
