define([
  "app",
  "models/device/device",
  "text!templates/program/nodes/keycardNode.html"
], function(App, Device, StateTemplate) {

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
    getStates: function(which) {
      switch(which) {
        case 'state':
          return ["inserted","empty"];
        default:
          return [];
      }
    },
    /**
     * return the keyboard code for a given state
    */
    getKeyboardForState: function(state, which){
      if (which !== "state") {
        console.error('Unsupported type of state: ' + which);
        return null;
      }
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONState("mandatory");
      switch(state) {
        case "inserted":
          $(btn).append("<span data-i18n='devices.keycard-reader.keyboard.card-inserted'></span>");
          v.phrase = "devices.keycard-reader.language.card-inserted";
          v.name = "inserted";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "empty":
          $(btn).append("<span data-i18n='devices.keycard-reader.keyboard.no-card-inserted'/>");
          v.name = "empty";
          v.phrase = "devices.keycard-reader.lanugage.no-card-inserted";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected state found for keycard sensor: " + state);
          btn = null;
          break;
      }
      return btn;
    },
	/**
	 * @returns state template for keycard sensor
	 */
	getTemplateState: function() {
	  return _.template(StateTemplate); 
	},

  });
  return KeyCardSensor;
});
