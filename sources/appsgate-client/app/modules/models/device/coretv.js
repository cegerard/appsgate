define([
  "app",
  "models/device/device"
], function(App, Device) {

  var TV = {};
  /**
   * @class Device.TV
   */
  TV = Device.extend({
    /**
     * @constructor
     */
    initialize:function() {
      TV.__super__.initialize.apply(this, arguments);

        // setting default friendly name if none exists
        if (typeof this.get("name") === "undefined" || this.get("name") === "") {
            this.generateDefaultName($.i18n.t("devices.tv.name.singular"));
        }
    },
    /**
     *return the list of available actions
     */
    getActions: function() {
      return ["resume", "pause", "stop", "notify", "channelup", "channeldown"];
    },
    /**
     * return the keyboard code for a given action
     */
    getKeyboardForAction: function(act){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONAction("mandatory");
      switch(act) {
          case "notify":
              $(btn).append("<span data-i18n='devices.tv.keyboard.notify'></span>");
              v.methodName = "notify";
              v.args = [ {"type":"int", "value": "0"},{"type":"String", "value": "spok-sender"},{"type":"String", "value": "test message"}
              ];
              v.phrase = "devices.tv.language.notify";
              $(btn).attr("json", JSON.stringify(v));
              break;
          case "channeldown":
              $(btn).append("<span data-i18n='devices.tv.keyboard.channeldown'></span>");
              v.methodName = "channelDown";
              v.args = [ {"type":"int", "value": "0"}];
              v.phrase = "devices.tv.language.channeldown";
              $(btn).attr("json", JSON.stringify(v));
              break;
          case "channelup":
              $(btn).append("<span data-i18n='devices.tv.keyboard.channelup'></span>");
              v.methodName = "channelUp";
              v.args = [ {"type":"int", "value": "0"}];
              v.phrase = "devices.tv.language.channelup";
              $(btn).attr("json", JSON.stringify(v));
              break;
          case "stop":
              $(btn).append("<span data-i18n='devices.tv.keyboard.stop'></span>");
              v.methodName = "stop";
              v.args = [ {"type":"int", "value": "0"}];
              v.phrase = "devices.tv.language.stop";
              $(btn).attr("json", JSON.stringify(v));
              break;
          case "pause":
              $(btn).append("<span data-i18n='devices.tv.keyboard.pause'></span>");
              v.methodName = "pause";
              v.args = [ {"type":"int", "value": "0"}];
              v.phrase = "devices.tv.language.pause";
              $(btn).attr("json", JSON.stringify(v));
              break;
        case "resume":
          $(btn).append("<span data-i18n='devices.tv.keyboard.resume'></span>");
          v.methodName = "resume";
          v.args = [ {"type":"int", "value": "0"}];
          v.phrase = "devices.tv.language.resume";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected action found for TV: " + act);
          btn = null;
          break;
      }
      return btn;
    },
      channelUp: function() {
          this.remoteControl("channelUp", [{"type":"int", "value": "0"}]);
      },
      channelDown: function() {
          this.remoteControl("channelDown", [{"type":"int", "value": "0"}]);
      },
      pause: function() {
          this.remoteControl("pause", [{"type":"int", "value": "0"}]);
      },
      stop: function() {
          this.remoteControl("stop", [{"type":"int", "value": "0"}]);
      },
      resume: function() {
          this.remoteControl("resume", [{"type":"int", "value": "0"}]);
      },
      notify: function() {
          this.remoteControl("notify", [{"type":"int", "value": "0"},{"type":"String", "value": "spok-sender"},{"type":"String", "value": "test message"}]);
      }
  });
  return TV;
});
