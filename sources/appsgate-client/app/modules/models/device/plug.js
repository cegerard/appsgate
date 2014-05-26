define([
  "app",
  "models/device/device"
], function(App, Device) {

  var Plug = {};
  /**
   * @class Device.Plug
   */
  Plug = Device.extend({
    /**
     * @constructor
     */
    initialize:function() {
      Plug.__super__.initialize.apply(this, arguments);
    },
    /**
     *return the list of available actions
     */
    getActions: function() {
      return ["switchOn", "switchOff"];
    },
    /**
     * return the keyboard code for a given action
     */
    getKeyboardForAction: function(act){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONAction("mandatory");
      switch(act) {
        case "switchOn":
          $(btn).append("<span data-i18n='keyboard.turn-on-plug-action'></span>");
          v.methodName = "on";
          v.phrase = "devices.plug.action.turnOn";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchOff":
          $(btn).append("<span data-i18n='keyboard.turn-off-plug-action'></span>");
          v.methodName = "off";
          v.phrase = "devices.plug.action.turnOff";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected action found for Plug: " + act);
          btn = null;
          break;
      }
      return btn;
    },
    /**
     * Return the list of states
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
          $(btn).append("<span data-i18n='keyboard.turned-on-plug-state'></span>");
          v.phrase = "language.turned-on-plug-state";
          v.name = "isOn";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "isOff":
          $(btn).append("<span data-i18n='keyboard.turned-off-plug-state'></span>");
          v.name = "isOff";
          v.phrase = "language.turned-off-plug-state";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected state found for Contact Sensor: " + state);
          btn = null;
          break;
      }
      return btn;
    },

    

    /**
     * Send a message to the backend to update the attribute plugState
     */
    sendPlugState:function() {
      if (this.get("plugState") === "true") {
        this.remoteControl("on", []);
      } else {
        this.remoteControl("off", []);
      }
    }
  });
  return Plug;
});
