define([
  "app",
  "models/device/device"
], function(App, Device) {

  var KeyCardSensor = {};

  /**
   * @class Device.KeyCardSensor
   */
  KeyCardSensor = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      KeyCardSensor.__super__.initialize.apply(this, arguments);
    },
    /**
     * return the list of available properties
     */
    getProperties: function() {
      return ["isInserted"];
    },
    /**
     * return the keyboard code for a property
     */
    getKeyboardForProperty: function(property) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONProperty("mandatory");
      v.target.deviceType = "4";
      switch(property) {
        case "isInserted":
          $(btn).append("<span data-i18n='language.card-inserted-keycard-reader-status'><span>");
          v.methodName = "getCardState";
          v.returnType = "boolean";
          v.phrase = "language.card-inserted-keycard-reader-status";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected device state found for keycard sensor: " + property);
          btn = null;
          break;
      }
      return btn;
    },
    /**
     * return the list of available events
     */
    getEvents: function() {
      return ["insertCard", "removeCard"];
    },
    /**
     * return the keyboard code for a given event
    */
    getKeyboardForEvent: function(evt){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONEvent("mandatory");
      v.source.deviceType = "4";
      v.source.value = this.get("id");
      switch(evt) {
        case "insertCard":
          $(btn).append("<span data-i18n='language.inserted-keycard-reader-event'></span>");
          v.eventName = "inserted";
          v.eventValue = "true";
          v.phrase = "language.inserted-keycard-reader-event";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "removeCard":
          $(btn).append("<span data-i18n='language.removed-keycard-reader-event'></span>");
          v.eventName = "inserted";
          v.eventValue = "false";
          v.phrase = "language.removed-keycard-reader-event";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for keycard sensor: " + evt);
          btn = null;
          break;
      }
      return btn;
    },
    /**
     * return the list of available states
     */
    getStates: function() {
      return ["inserted","empty"];
    },
    /**
     * return the keyboard code for a given state
    */
    getKeyboardForState: function(state){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONState("mandatory");
      switch(state) {
        case "inserted":
          $(btn).append("<span data-i18n='language.card-inserted-keycard-reader-status'></span>");
          v.phrase = "language.card-inserted-keycard-reader-status";
          v.name = "inserted";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "empty":
          $(btn).append("<span data-i18n='language.no-card-inserted-keycard-reader-status'/>");
          v.name = "empty";
          v.phrase = "language.no-card-inserted-keycard-reader-status";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected state found for keycard sensor: " + state);
          btn = null;
          break;
      }
      return btn;
    },

  });
  return KeyCardSensor;
});
