define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/co2.html"
  ], function(App, DeviceDetailsView, co2DetailTemplate) {

    var CO2SensorView = {};
    // detailled view of a device
  CO2SensorView = DeviceDetailsView.extend({
      tplCo2: _.template(co2DetailTemplate),
      initialize: function() {
        var self = this;
        CO2SensorView.__super__.initialize.apply(this, arguments);
        this.listenTo(this.model, "change", this.render);
        $.extend(self.__proto__.events, CO2SensorView.__super__.events);
      },
      autoupdate: function() {
          CO2SensorView.__super__.autoupdate.apply(this);

        this.$el.find("#co2-value").html(this.model.getValue());

        // translate the view
        this.$el.i18n();
      },
      /**
      * Render the detailed view of a device
      */
      render: function() {
        var self = this;

        if (!appRouter.isModalShown) {
          this.$el.html(this.template({
            device: this.model,
            sensorImg: ["app/img/sensors/enoceanNanoSenseE4000.jpg"],
            sensorCaption: [$.i18n.t("devices.co2.caption.intern")],
            sensorType: $.i18n.t("devices.co2.name.singular"),
            places: places,
            deviceDetails: this.tplCo2
          }));
        }

        this.resize($(".scrollable"));

        // translate the view
        this.$el.i18n();

        return this;
      }
    });
    return CO2SensorView
  });
