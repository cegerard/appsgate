define([
  "app",
  "models/device/device"
], function(App, Device) {

  var Actuator = {};

  /**
   * Implementation of an actuator
   * @class Device.Actuator
   */
  Actuator = Device.extend({

    /**
     * @constructor
     */
    initialize: function() {
      Actuator.__super__.initialize.apply(this, arguments);
    },

    /**
     * Send a message to the backend to update the attribute value
     */
    sendValue: function() {
      if (this.get("value") === "true") {
        this.remoteControl("on", []);
      } else {
        this.remoteControl("off", []);
      }
    }
  });
  return Actuator;
});
