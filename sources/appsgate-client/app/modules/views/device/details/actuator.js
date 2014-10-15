define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/actuator.html"
  ], function(App, DeviceDetailsView, actuatorDetailTemplate) {

    var ActuatorView = {};
    // detailled view of a device
    ActuatorView = DeviceDetailsView.extend({
      tplActuator: _.template(actuatorDetailTemplate),
      // map the events and their callback
      events: {
        "click button.toggle-actuator-button": "onToggleActuatorButton"
      },
      /**
      * Listen to the device update and refresh if any
      *
      * @constructor
      */
      initialize: function() {
          var self = this;
          ActuatorView.__super__.initialize.apply(this, arguments);

          $.extend(self.__proto__.events, ActuatorView.__super__.events);
      },
      /**
      * Callback to toggle a plug - used when the displayed device is a plug (!)
      */
      onToggleActuatorButton: function() {
        // value can be string or boolean
        // string
        if (typeof this.model.get("value") === "string") {
          if (this.model.get("value") === "true") {
            this.model.set("value", "false");
            this.$el.find(".toggle-actuator-button").text("Allumer");
          } else {
            this.model.set("value", "true");
            this.$el.find(".toggle-actuator-button").text("Eteindre");
          }
          // boolean
        } else {
          if (this.model.get("value")) {
            this.model.set("value", "false");
            this.$el.find(".toggle-actuator-button").text("Allumer");
          } else {
            this.model.set("value", "true");
            this.$el.find(".toggle-actuator-button").text("Eteindre");
          }
        }

        // send the message to the backend
        this.model.save();
      },
      autoupdate: function() {
        ActuatorView.__super__.autoupdate.apply(this);

        var actuatorValue = ";"
        if (device.get("inserted")==="true") {
            actuatorValue = "<span class='label label-yellow' data-i18n='devices.switch.value.opened'></span>";
        } else {
            actuatorValue = "<span class='label label-default' data-i18n='devices.switch.value.closed'></span>";
        }
        this.$el.find("#actuator-value").html(cardSwitchStatus);

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
            sensorImg: ["app/img/sensors/doubleSwitch.jpg"],
            sensorType: $.i18n.t("devices.actuator.name.singular"),
            places: places,
            deviceDetails: this.tplActuator
          }));

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();
        }

        return this;
      }
    });
    return ActuatorView
  });
