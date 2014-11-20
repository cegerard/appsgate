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

      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
        this.generateDefaultName($.i18n.t("devices.illumination.name.singular"));
      }
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
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONProperty("mandatory");
      switch (property) {
        //        case "value":
        //          $(btn).append("<span data-i18n='devices.illumination.keyboard.get'><span>");
        //          v.methodName = "getIllumination";
        //          v.returnType = "number";
        //          v.phrase = "devices.illumination.language.get";
        //          v.unit = "lux";
        //          $(btn).attr("json", JSON.stringify(v));
        case "value":
          $(btn).append("<span>" + $.i18n.t('devices.illumination.keyboard.get', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.illumination.keyboard.sensor') + "</span>",
            myVar2: "<span class='highlight-placeholder'>" + $.i18n.t('devices.illumination.keyboard.brightness') + "</span>"
          }));
          v.methodName = "getCurrentIlluminationLabel";
          v.returnType = "scale";
          v.unit = "lux";
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
    getEvents: function() {
      return ["value-changed"];
    },
    getKeyboardForEvent: function(evt) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONEvent("mandatory");
      switch (evt) {
        case "value-changed":
          $(btn).append("<span>" + $.i18n.t('devices.illumination.keyboard.change', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.illumination.keyboard.sensor') + "</span>",
          }));
          v.eventName = "value";
          v.eventValue = "*";
          v.phrase = "devices.illumination.language.change";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for IlluminationSensor: " + evt);
          btn = null;
          break;
      }
      return btn;
    },
    getScale: function() {
      var arrayScale = [{
        "value": "low",
        "label": "devices.illumination.scale.low",
        "minValue": 0,
        "maxValue": 300,
      }, {
        "value": "medium",
        "label": "devices.illumination.scale.medium",
        "minValue": 301,
        "maxValue": 500,
      }, {
        "value": "high",
        "label": "devices.illumination.scale.high",
        "minValue": 501,
        "maxValue": 1000,
      }, {
        "value": "veryHigh",
        "label": "devices.illumination.scale.veryHigh",
        "minValue": 1001,
        "maxValue": 2000,
      }, {
        "value": "highest",
        "label": "devices.illumination.scale.highest",
        "minValue": 2001,
        "maxValue": 30000,
      }];
      return arrayScale;

    },
    getValue: function() {
      value = parseInt(this.get("value"));

      //Means that its a valid value
      if (value != 9999) {

        //If the sensor returns 300 it means that the number of Lux may be 300 or less
        if (value == 300) {
          return "<= " + value;
        }

        return value;
      }

      return $.i18n.t("devices.no-value");
    }
  });
  return IlluminationSensor;
});
