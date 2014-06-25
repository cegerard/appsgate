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
    },
        /**
     * return the list of available events
     */
    getEvents: function() {
      return ["switchB1-on", "switchB2-on","switchB1-off", "switchB2-off"];
    },
    /**
     * return the keyboard code for a given event
    */
    getKeyboardForEvent: function(evt){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONEvent("mandatory");
      switch(evt) {
        case "switchB1-on":
          $(btn).append("<span data-i18n='language.pushed-switch-B1-on'></span>");
          v.eventName = "switchNumber";
          v.eventValue = "1/true";
          v.phrase = "language.pushed-switch-B1-on";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchB2-on":
          $(btn).append("<span data-i18n='language.pushed-switch-B2-on'></span>");
          v.eventName = "switchNumber";
          v.eventValue = "0/true";
          v.phrase = "language.pushed-switch-B2-on";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchB1-off":
          $(btn).append("<span data-i18n='language.pushed-switch-B1-off'></span>");
          v.eventName = "switchNumber";
          v.eventValue = "1/false";
          v.phrase = "language.pushed-switch-B1-off";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchB2-off":
          $(btn).append("<span data-i18n='language.pushed-switch-B2-off'></span>");
          v.eventName = "switchNumber";
          v.eventValue = "0/false";
          v.phrase = "language.pushed-switch-B2-off";
          $(btn).attr("json", JSON.stringify(v));
          break;
        /*
        case "switchUp":
          $(btn).append("<span data-i18n='language.pushed-switch-event'></span>");
          v.eventName = "switchNumber";
          v.eventValue = "1";
          v.phrase = "language.pushed-telec-event-up";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchBottom":
          $(btn).append("<span data-i18n='language.pushed-switch-event'></span>");
          v.eventName = "switchNumber";
          v.eventValue = "0";
          v.phrase = "language.pushed-telec-event-bottom";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchLeft":
          $(btn).append("<span data-i18n='language.pushed-switch-event'></span>");
          v.eventName = "buttonStatus";
          v.eventValue = "false";
          v.phrase = "language.pushed-telec-event-left";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchRight":
          $(btn).append("<span data-i18n='language.pushed-switch-event'></span>");
          v.eventName = "buttonStatus";
          v.eventValue = "false";
          v.phrase = "language.pushed-telec-event-right";
          $(btn).attr("json", JSON.stringify(v));
          break;
          */
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
