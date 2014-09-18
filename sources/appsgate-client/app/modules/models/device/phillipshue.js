define([
  "app",
  "models/device/device",
  "raphael",
  "text!templates/program/nodes/lampActionNode.html"
], function(App, Device, Raphael, ActionTemplate) {

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
          v.methodName = "off";
          v.phrase = "language.turn-off-lamp-action";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "blink":
          $(btn).append("<span data-i18n='devices.lamp.action.blink'/>");
          v.methodName = "blink30";
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
          $(btn).append("<span data-i18n='devices.lamp.keyboard.turnOnEvt'><span>");
          v.eventName = "state";
          v.eventValue = "true";
          v.phrase = "devices.lamp.language.turnOnEvt";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchOff":
          $(btn).append("<span data-i18n='devices.lamp.keyboard.turnOffEvt'><span>");
          v.eventName = "state";
          v.eventValue = "false";
          v.phrase = "devices.lamp.keyboard.turnOffEvt";
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
    getStates: function(which) {
      return ["isOn", "isOff"];
    },
    /**
     * return the keyboard code for a given state
    */
    getKeyboardForState: function(state,which){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONState("mandatory");
      v.type = which;
      var keep = "";
      if (which == "maintainableState") {
        keep = "-keep";
      }
      switch(state) {
        case "isOn":
        case "isOff":
          $(btn).append("<span data-i18n='devices.lamp.keyboard." + state + keep + "'><span>");
          v.name = state;
          v.phrase = "devices.lamp.language." + state + keep;
          $(btn).attr("json", JSON.stringify(v));
        return btn;
        default:
          console.error("unexpected state found for PhilipsHue: " + state);
          return null;
      }
    },

    /**
     * return the list of available properties
     */
    getProperties: function() {
      return ["getBrightness"];
    },
    /**
     * return the keyboard code for a property
     */
    getKeyboardForProperty: function(property) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONProperty("mandatory");
      switch(property) {
        case "getBrightness":
          $(btn).append("<span data-i18n='keyboard.light-brightness'><span>");
          v.methodName = "getLightBrightness";
          v.returnType = "number";
          v.phrase = "language.light-brightness";
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
     * @returns the current lamp color as a hex value
     */
    getCurrentColor: function() {
      return Raphael.hsb((this.get("color") / 65535), (this.get("saturation") / 255), (this.get("brightness") / 255));
    },


    /**
     * Send a message to the backend to update the attribute value
     */
    sendValue:function() {
      if (this.get("value") === "true") {
        this.remoteControl("on", []);
      } else {
        this.remoteControl("off", []);
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
