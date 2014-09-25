define([
  "app",
  "models/device/device",
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
//        case "value":
//          $(btn).append("<span data-i18n='devices.illumination.keyboard.get'><span>");
//          v.methodName = "getIllumination";
//          v.returnType = "number";
//          v.phrase = "devices.illumination.language.get";
//          v.unit = "lux";
//          $(btn).attr("json", JSON.stringify(v));
        case "value":
          $(btn).append("<span data-i18n='devices.illumination.keyboard.get'><span>");
          v.methodName = "getCurrentIlluminationLabel";
          v.returnType = "scale";
          v.phrase = "devices.illumination.language.get";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected device state found for illumination sensor: " + property);
          btn = null;
          break;
      }
      return btn;
    },
    getScale: function() {
        var arrayScale = 
            [
                    {
                        "value" : "lowest",
                        "label" : "devices.illumination.scale.lowest"
                    },
                    {
                        "value" : "veryLow", 
                        "label" : "devices.illumination.scale.veryLow"
                    },
                    {
                        "value" : "low",
                        "label" : "devices.illumination.scale.low"
                    },
                    {
                        "value" : "medium",
                        "label" : "devices.illumination.scale.medium"
                    },
                    {
                        "value" : "high",
                        "label" : "devices.illumination.scale.high"
                    },
                    {
                        "value" : "veryHigh",
                        "label" : "devices.illumination.scale.veryHigh"
                    },
                    {
                        "value" : "highest",
                        "label" : "devices.illumination.scale.highest"
                    }
                ];
        return arrayScale;
            
    },
  });
  return IlluminationSensor;
});
