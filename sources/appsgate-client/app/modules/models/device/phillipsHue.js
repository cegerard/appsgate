define([
  "app",
  "models/device/device",
  "text!templates/program/nodes/lampActionNode.html"
], function(App, Device, ActionTemplate) {

  var PhillipsHue = {};

  /**
   * Implementation of the Phillips Hue lamp
   * 
   * @class Device.PhillipsHue
   */
  PhillipsHue = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      PhillipsHue.__super__.initialize.apply(this, arguments);
    },
    /**
     *return the list of available actions
     */
    getActions: function() {
      return ["switchOn", "switchOff", "blink"];
    },
    /**
     * return the keyboard code for a given action
     */
    getKeyboardForAction: function(act){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONAction("mandatory");

      switch(act) {
        case "switchOn":
          $(btn).append("<span data-i18n='keyboard.turn-on-lamp-action'/>");
          v.methodName = "setWhite";
          v.phrase = "language.turn-on-lamp-action";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchOff":
          $(btn).append("<span data-i18n='keyboard.turn-off-lamp-action'/>");
          v.methodName = "Off";
          v.phrase = "language.turn-off-lamp-action";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "blink":
          $(btn).append("<span data-i18n='devices.lamp.action.blink'/>");
          v.methodName = "blink";
          v.phrase = "devices.lamp.action.blink";
          $(btn).attr("json", JSON.stringify(v));
          break;          
        default:
          console.error("unexpected action found for PhilipsHue: " + act);
          btn = null;
          break;
      }
      return btn;
    },
    /**
     * return the list of available events
     */
    getEvents: function() {
      return ["switchOn", "switchOff"];
    },
    /**
     * return the keyboard code for a given event
    */
    getKeyboardForEvent: function(evt){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONEvent("mandatory");
      switch(evt) {
        case "switchOn":
          $(btn).append("<span data-i18n='keyboard.is-turned-on-lamp-event'><span>");
          v.eventName = "state";
          v.eventValue = "true";
          v.phrase = "language.turned-on-lamp-event";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchOff":
          $(btn).append("<span data-i18n='keyboard.is-turned-off-lamp-event'><span>");
          v.eventName = "state";
          v.eventValue = "false";
          v.phrase = "language.turned-off-lamp-event";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for PhilipsHue: " + evt);
          btn = null;
          break;
      }
      return btn;
    },

        /**
     * return the list of available states
     */
    getStates: function() {
      return ["isOn", "isOff"];
    },
    /**
     * return the keyboard code for a given state
    */
    getKeyboardForState: function(state){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONState("mandatory");

      switch(state) {
        case "isOn":
          $(btn).append("<span data-i18n='keyboard.is-turned-on-lamp-state'><span>");
          v.name = "isOn";
          v.phrase = "language.is-turned-on-lamp-state";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "isOff":
          $(btn).append("<span data-i18n='keyboard.is-turned-off-lamp-state'><span>");
          v.name = "isOff";
          v.phrase = "language.is-turned-off-lamp-state";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected state found for PhilipsHue: " + state);
          btn = null;
          break;
      }
      return btn;
    },

    /**
     * return the list of available properties
     */
    getProperties: function() {
      return ["isOn", "getBrightness"];
    },
    /**
     * return the keyboard code for a property
     */
    getKeyboardForProperty: function(property) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONProperty("mandatory");
      switch(property) {
        case "isOn":
          $(btn).append("<span data-i18n='keyboard.is-turned-on-lamp-status'><span>");
          v.methodName = "getCurrentState";
          v.returnType = "boolean";
          v.phrase = "language.is-turned-on-lamp-status";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "getBrightness":
          $(btn).append("<span data-i18n='keyboard.light-brightness'><span>");
          v.methodName = "getLightBrightness";
          v.returnType = "number";
          v.phrase = " brille";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected device state found for PhilipsHue: " + property);
          btn = null;
          break;
      }
      return btn;
    },
    
    /**
     * @returns the action template specific for lamps
     */
    getTemplateAction: function() {
      return _.template(ActionTemplate);  
    },


    /**
     * Send a message to the backend to update the attribute value
     */
    sendValue:function() {
      if (this.get("value") === "true") {
        this.remoteControl("On", []);
      } else {
        this.remoteControl("Off", []);
      }
    },

    /**
     * Send a message to the backend to update the attribute color
     */
    sendColor:function() {
      this.remoteControl("setColor", [{ type : "long", value : this.get("color") }], this.id);
    },

    /**
     * Send a message to the backend to update the attribute saturation
     */
    sendSaturation:function() {
      this.remoteControl("setSaturation", [{ type : "int", value : this.get("saturation") }], this.id);
    },
    
    /**
     * Send a message to the backend to update the attribute brightness
     */
    sendBrightness:function() {
      this.remoteControl("setBrightness", [{ type : "long", value : this.get("brightness") }], this.id);
    }
  });
  return PhillipsHue;
});
