define([
  "app",
  "models/device/device"
], function(App, Device) {

  var CO2Sensor = {};

  /**
   * Implementation of CO2 sensor
   *
   * @class Device.CO2Sensor
   */
  CO2Sensor = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      CO2Sensor.__super__.initialize.apply(this, arguments);

      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
        this.generateDefaultName($.i18n.t("devices.co2.name.singular"));
      }
    },
    /**
     * return the list of available properties
     */
    getProperties: function() {
      return ["value"];
    },
    getEvents: function() {
      return ["value-changed"];
    },
    getKeyboardForProperty: function(property) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONProperty("mandatory");
      switch (property) {
        case "value":
          $(btn).append("<span>" + $.i18n.t('devices.temperature.keyboard.get', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.temperature.keyboard.sensor') + "</span>",
          }));
          v.methodName = "getCO2Concentration";
          v.returnType = "number";
          v.phrase = "devices.co2.language.get";
          v.unit = "ppm";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected device state found for temperature sensor: " + property);
          btn = null;
          break;
      }
      return btn;
    },
    getKeyboardForEvent: function(evt) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONEvent("mandatory");
      switch (evt) {
        case "value-changed":
          $(btn).append("<span>" + $.i18n.t('devices.co2.keyboard.change', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.co2.keyboard.sensor') + "</span>",
          }));
          v.eventName = "value";
          v.eventValue = "*";
          v.phrase = "devices.co2.language.change";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for CO2Sensor: " + evt);
          btn = null;
          break;
      }
      return btn;
    },
    getValue: function() {
      value = parseInt(this.get("value"));

      if (value != -1) {
        return value.toString().concat(" ppm");
      }

      return $.i18n.t("devices.no-value");
    }
  });
  return CO2Sensor;
});
