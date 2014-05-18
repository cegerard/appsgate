define([
  "app",
  "models/device/device"
], function(App, Device) {

  var TemperatureSensor = {};

  /**
   * Implementation of temperature sensor
   * Specific attribute is: 
   *      value, containing the last temperature sent by the backend, in degree Celsius
   *
   * @class Device.TemperatureSensor
   */
  TemperatureSensor = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      TemperatureSensor.__super__.initialize.apply(this, arguments);
    }
  });
  return TemperatureSensor;
});
