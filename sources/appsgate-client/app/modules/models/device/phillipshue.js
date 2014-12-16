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

      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
        this.generateDefaultName($.i18n.t("devices.lamp.name.singular"));
      }
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
    getKeyboardForAction: function(act) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONAction("mandatory");

      switch (act) {
        case "switchOn":
          $(btn).append("<span>" + $.i18n.t('devices.lamp.keyboard.turnOn', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.lamp.keyboard.lamp') + "</span>",
          }));
          v.methodName = "setWhite";
          v.phrase = "devices.lamp.language.turnOn";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchOff":
          $(btn).append("<span>" + $.i18n.t('devices.lamp.keyboard.turnOff', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.lamp.keyboard.lamp') + "</span>",
          }));
          v.methodName = "off";
          v.phrase = "devices.lamp.language.turnOff";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "blink":
          $(btn).append("<span>" + $.i18n.t('devices.lamp.keyboard.blink', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.lamp.keyboard.lamp') + "</span>",
          }));
          v.methodName = "blink";
          v.args = [{
            "type": "long",
            "value": "20"
          },{
            "type": "long",
            "value": "1000"
          }];
          v.phrase = "devices.lamp.language.blink";
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
    getKeyboardForEvent: function(evt) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONEvent("mandatory");
      switch (evt) {
        case "switchOn":
          $(btn).append("<span>" + $.i18n.t('devices.lamp.keyboard.turnOnEvt', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.lamp.keyboard.lamp') + "</span>",
          }));
          v.eventName = "value";
          v.eventValue = "true";
          v.phrase = "devices.lamp.language.turnOnEvt";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "switchOff":
          $(btn).append("<span>" + $.i18n.t('devices.lamp.keyboard.turnOffEvt', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.lamp.keyboard.lamp') + "</span>",
          }));
          v.eventName = "value";
          v.eventValue = "false";
          v.phrase = "devices.lamp.language.turnOffEvt";
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
    getKeyboardForState: function(state, which) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONState("mandatory");
      v.type = which;
      var keep = "";
      if (which == "maintainableState") {
        keep = "-keep";
      }
      switch (state) {
        case "isOn":
        case "isOff":
          $(btn).append("<span>" + $.i18n.t('devices.lamp.keyboard.' + state + keep, {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.lamp.keyboard.lamp') + "</span>",
          }));
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
    // getProperties: function() {
    //   return ["getBrightness"];
    // },
    /**
     * return the keyboard code for a property
     */
    getKeyboardForProperty: function(property) {
      // var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      // var v = this.getJSONProperty("mandatory");
      // switch(property) {
      //   case "getBrightness":
      //     $(btn).append("<span>" + $.i18n.t('devices.lamp.keyboard.brightness',
      //       {
      //         myVar:"<span class='highlight-placeholder'>" + $.i18n.t('devices.lamp.keyboard.lamp') + "</span>",
      //       })
      //     );
      //     v.methodName = "getLightBrightness";
      //     v.returnType = "number";
      //     v.phrase = "devices.lamp.language.brightness";
      //     $(btn).attr("json", JSON.stringify(v));
      //     break;
      //   default:
      //     console.error("unexpected device state found for PhilipsHue: " + property);
      //     btn = null;
      //     break;
      // }
      return "";
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
    sendValue: function() {
      if (this.get("value") === "true" || this.get("value") === true) {
        this.switchOn();
      } else {
        this.switchOff();
      }
    },
    /**
     * Send messages to switch the lamp on
     */
    switchOn: function() {
      this.remoteControl("on", []);
      //          this.sendColor();
      //          this.sendSaturation();
      //          this.sendBrightness();
      this.sendFullColor();
    },
    /**
     * Send message to switch the lamp off
     */
    switchOff: function() {
      this.remoteControl("off", []);
    },
    sendFullColor: function() {
      this.remoteControl("setColorJson", [{
        type: "JSONObject",
        value: {
          hue: this.get("color"),
          sat: this.get("saturation"),
          bri: this.get("brightness")
        }
      }], this.id);
    },
    /**
     * Send a message to the backend to update the attribute color
     */
    sendColor: function() {
      this.remoteControl("setColor", [{
        type: "long",
        value: this.get("color")
      }], this.id);
    },
    /**
     * Send a message to the backend to update the attribute saturation
     */
    sendSaturation: function() {
      this.remoteControl("setSaturation", [{
        type: "int",
        value: this.get("saturation")
      }], this.id);
    },
    /**
     * Send a message to the backend to update the attribute brightness
     */
    sendBrightness: function() {
      this.remoteControl("setBrightness", [{
        type: "long",
        value: this.get("brightness")
      }], this.id);
    }
  });
  return PhillipsHue;
});
