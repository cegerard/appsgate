define([
  "app",
  "models/device/device"
], function(App, Device) {

  var ARDLock = {};
  /**
   * Implementation of a Contact sensor
   * @class Device.ContactSensor
   */
  ARDLock = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      ARDLock.__super__.initialize.apply(this, arguments);
    }
  });
  return ARDLock;
});
