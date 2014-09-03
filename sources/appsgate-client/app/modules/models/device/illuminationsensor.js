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
          $(btn).append("<span data-i18n='keyboard.getIllumination'><span>");
          v.methodName = "getIllumination";
          v.returnType = "number";
          v.phrase = "language.getIllumination";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected device state found for illumination sensor: " + property);
          btn = null;
          break;
      }
      return btn;
    },
  });
  return IlluminationSensor;
});
