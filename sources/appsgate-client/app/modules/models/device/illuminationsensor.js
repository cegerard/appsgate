define([
  "app",
  "models/device/device"
], function(App, Device) {

  var IlluminationSensor = {};

  /**
   * Implementation of an illumination sensor
   * @class Device.IlluminationSensor
   */
  IlluminationSensor = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      IlluminationSensor.__super__.initialize.apply(this, arguments);
    }
  });
  return IlluminationSensor;
});
