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
    },
    /**
     * return the list of available properties
     */
    getProperties: function() {
      return ["value"];
    },
    /**
     * return the keyboard code for a property
     */
    getKeyboardForProperty: function(property) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONProperty("mandatory");
      switch(property) {
        case "value":
          $(btn).append("<span data-i18n='devices.temperature.keyboard.get'><span>");
          v.methodName = "getTemperature";
          v.returnType = "number";
          v.phrase = "devices.temperature.language.get";
          v.unit = "&deg; C";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected device state found for temperature sensor: " + property);
          btn = null;
          break;
      }
      return btn;
    },
    getValue: function () {
          value=parseFloat(this.get("value"));

          if (value != parseFloat(999)){
              return Math.round(value);
          }

          return $.i18n.t("devices.no-value");
      }
  });
  return TemperatureSensor;
});
