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

      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
          this.generateDefaultName($.i18n.t("devices.plug.name.singular"));
      }
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
          $(btn).append("<span data-i18n='devices.plug.keyboard.turnOn'></span>");
          v.methodName = "on";
          v.phrase = "devices.plug.language.turnOn";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchOff":
          $(btn).append("<span data-i18n='devices.plug.keyboard.turnOff'></span>");
          v.methodName = "off";
          v.phrase = "devices.plug.language.turnOff";
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
          $(btn).append("<span data-i18n='devices.plug.keyboard.turnOnEvt'><span>");
          v.eventName = "plugState";
          v.eventValue = "true";
          v.phrase = "devices.plug.language.turnOnEvt";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchOff":
          $(btn).append("<span data-i18n='devices.plug.keyboard.turnOffEvt'><span>");
          v.eventName = "plugState";
          v.eventValue = "false";
          v.phrase = "devices.plug.language.turnOffEvt";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for plug: " + evt);
          btn = null;
          break;
      }
      return btn;
    },

    /**
     * Return the list of states
     */
    getStates: function(which) {
      return ["isOn", "isOff"];
    },
    /**
     * return the keyboard code for a given state
     */
    getKeyboardForState: function(state, which){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONState("mandatory");
      var keep = "";
      v.type = which;
      if (which == "maintainableState") {
        keep = "-keep";
      }
      switch(state) {
        case "isOn":
        case "isOff":
          $(btn).append("<span data-i18n='devices.plug.keyboard." + state + keep + "'><span>");
          v.name = state;
          v.phrase = "devices.plug.language." + state + keep;
          $(btn).attr("json", JSON.stringify(v));
        return btn;

        default:
          console.error("unexpected state found for Contact Sensor: " + state);
          return null;
      }
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
    },
    getValue: function () {
          value=parseInt(this.get("consumption"));

          if (value != -1 ){
              return value;
          }

          return $.i18n.t("devices.no-value");
      }
  });
  return Plug;
});
