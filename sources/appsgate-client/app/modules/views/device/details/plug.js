define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/plug.html"
  ], function(App, DeviceDetailsView, plugDetailTemplate) {

    var SmartPlugView = {};
    // detailled view of a device
    SmartPlugView = DeviceDetailsView.extend({
      tplPlug: _.template(plugDetailTemplate),
      initialize: function() {
        var self = this;
        SmartPlugView.__super__.initialize.apply(this, arguments);
      },
      /**
      * Callback to toggle a plug - used when the displayed device is a plug (!)
      */
      onTogglePlugButton: function() {
        // value can be string or boolean
        // string
        if (typeof this.model.get("plugState") === "string") {
          if (this.model.get("plugState") === "true") {
            this.model.set("plugState", "false");
            this.$el.find(".toggle-plug-button").text("Allumer");
          } else {
            this.model.set("plugState", "true");
            this.$el.find(".toggle-plug-button").text("Eteindre");
          }
          // boolean
        } else {
          if (this.model.get("plugState")) {
            this.model.set("plugState", "false");
            this.$el.find(".toggle-plug-button").text("Allumer");
          } else {
            this.model.set("plugState", "true");
            this.$el.find(".toggle-plug-button").text("Eteindre");
          }
        }

        // send the message to the backend
        this.model.save();
      },
      autoupdate: function() {
        SmartPlugView.__super__.autoupdate.apply(this);

        this.$el.find("#plug-consumption").html(device.get("consumption") + "Watt");

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
        this.$el.find("#plug-button").html(plugState);

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
