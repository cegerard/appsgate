define([
  "app",
  "models/device/device"
], function(App, Device) {

  var KeyCardSensor = {};

  /**
   * @class Device.KeyCardSensor
   */
  KeyCardSensor = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      KeyCardSensor.__super__.initialize.apply(this, arguments);
    }
  });
  return KeyCardSensor;
});
