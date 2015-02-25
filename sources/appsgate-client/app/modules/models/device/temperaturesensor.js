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

      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
        this.generateDefaultName($.i18n.t("devices.temperature.name.singular"));
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
          v.methodName = "getTemperature";
          v.returnType = "number";
          v.phrase = "devices.temperature.language.get";
          v.unit = "&deg; C";
          v.defaultValue = "18.5";
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
          $(btn).append("<span>" + $.i18n.t('devices.temperature.keyboard.change', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.temperature.keyboard.sensor') + "</span>",
          }));
          v.eventName = "value";
          v.eventValue = "*";
          v.phrase = "devices.temperature.language.change";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for TemperatureSensor: " + evt);
          btn = null;
          break;
      }
      return btn;
    },
    getValue: function() {
      value = parseFloat(this.get("value"));

      if (value != parseFloat(999)) {
        return value.toFixed(1);
      }

      return $.i18n.t("devices.no-value");
    }
  });
  return TemperatureSensor;
});
