define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/plug.html"
  ], function(App, DeviceDetailsView, plugDetailTemplate) {

    var SmartPlugView = {};
    // detailled view of a device
    SmartPlugView = DeviceDetailsView.extend({
      tplPlug: _.template(plugDetailTemplate),
      // map the events and their callback
      events: {
        "click button.toggle-plug-button": "onTogglePlugButton",
      },
      initialize: function() {
        var self = this;
        SmartPlugView.__super__.initialize.apply(this, arguments);

        $.extend(self.__proto__.events, SmartPlugView.__super__.events);
      },
      /**
      * Callback to toggle a plug - used when the displayed device is a plug (!)
      */
      onTogglePlugButton: function() {
        if (this.model.get("plugState") == "true") {
          this.model.switchOff();
        } else {
          this.model.switchOn();
        }
      },
      autoupdate: function() {
        SmartPlugView.__super__.autoupdate.apply(this);
        device = this.model;
        this.$el.find("#plug-consumption").html(device.get("consumption") + " W");

        var plugState = ""
        if (this.model.get("plugState")==="true") {
            plugState = "<span class='label label-yellow' data-i18n='devices.plug.status.turnedOn'></span>";
        } else {
            plugState = "<span class='label label-default' data-i18n='devices.plug.status.turnedOff'></span>";
        }
        this.$el.find("#plug-state").html(plugState);

        var plugButton = "";
        if (this.model.get("plugState") === "true" || device.get("plugState") === true) {
            plugButton = "<span data-i18n='devices.plug.action.turnOff'></span>";
        } else {
            plugButton = "<span data-i18n='devices.plug.action.turnOn'></span>";
        }
        this.$el.find("#plug-button").html(plugButton);

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
            sensorImg: ["app/img/sensors/plug.png"],
            sensorType: $.i18n.t("devices.plug.name.singular"),
            places: places,
            deviceDetails: this.tplPlug
          }));

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
        }
      }
    });
    return SmartPlugView
  });
