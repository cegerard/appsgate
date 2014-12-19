define([
  "app",
  "models/device/device"
], function(App, Device) {

  var SwitchSensor = {};

  /**
   * Implementation of switch sensor
   * Specific attributes are:
   *      switchNumber. Values are depend of the type of the switch
   *      buttonStatus, 0 when Off, 1 when On
   *
   * @class Device.SwitchSensor
   */
  SwitchSensor = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      SwitchSensor.__super__.initialize.apply(this, arguments);

      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
        this.generateDefaultName($.i18n.t("devices.switch.name.singular"));
        this.set("switchNumber", "");
      }
      this.on("change", function() {
        self = this;
        if (this.changed.buttonStatus == undefined) {
          self.set("buttonStatus", "true");
          self.set("switchNumber", "");
          setTimeout(function() {
            self.set("buttonStatus", "false");
          }, 600);
        }

      });
    },
    /**
     * return the list of available events
     */
    getEvents: function() {
      //            return ["switchB1-on", "switchB2-on", "switchB1-off", "switchB2-off"];
      return ["switch-up", "switch-bottom"];
    },
    /**
     * return the keyboard code for a given event
     */
    getKeyboardForEvent: function(evt) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONEvent("mandatory");
      switch (evt) {
        case "switch-up":
          $(btn).append("<span>" + $.i18n.t('devices.switch.keyboard.pushed-up', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.switch.keyboard.switch') + "</span>",
          }));
          v.eventName = "switchNumber";
          v.eventValue = "7";
          v.phrase = "devices.switch.language.pushed-up";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switch-bottom":
          $(btn).append("<span>" + $.i18n.t('devices.switch.keyboard.pushed-bottom', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.switch.keyboard.switch') + "</span>",
          }));
          v.eventName = "switchNumber";
          v.eventValue = "5";
          v.phrase = "devices.switch.language.pushed-bottom";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for SwitchSensor: " + evt);
          btn = null;
          break;
      }
      return btn;
    },
  });
  return SwitchSensor;
});
