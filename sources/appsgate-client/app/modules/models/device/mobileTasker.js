define([
  "app",
  "models/device/device"
], function(App, Device) {

  var MobileTasker = {};

  /**
   * Implementation of an actuator
   * @class Device.Actuator
   */
  MobileTasker = Device.extend({

    /**
     * @constructor
     */
    initialize: function() {
      MobileTasker.__super__.initialize.apply(this, arguments);

      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
        this.generateDefaultName($.i18n.t("devices.mobileTasker.name.singular"));
      }
    },
    /**
     *return the list of available actions
     */
    getActions: function() {
      return ["sendMessage"];
    },
    /**
     * return the keyboard code for a given action
     */
    getKeyboardForAction: function(act) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONAction("mandatory");

      switch (act) {
        case "sendMessage":
          $(btn).append("<span>" + $.i18n.t('devices.mobileTasker.keyboard.sendMessage', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.mobileTasker.keyboard.mobile') + "</span>"
          }));
          v.methodName = "display";
          v.type="action1";
          v.args = [{
            "type": "String",
            "value": "Coucou"
          }];
          v.phrase = "devices.mobileTasker.language.sendMessage";
          $(btn).attr("json", JSON.stringify(v));
          break;

        default:
          console.error("unexpected action found for MobileDevice: " + act);
          btn = null;
          break;
      }
      return btn;
    },
    /**
     * return the list of available events
     */
    getEvents: function() {
      return ["valueChange"];
    },
    /**
     * return the keyboard code for a given event
     */
    getKeyboardForEvent: function(evt) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONEvent("mandatory");
      switch (evt) {
        case "valueChange":
          $(btn).append("<span>" + $.i18n.t('devices.mobileTasker.keyboard.msgReceivedEvt', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.mobileTasker.keyboard.mobile') + "</span>",
          }));
          v.eventName = "appsgate";
          v.eventValue = "*";
          v.phrase = "devices.mobileTasker.language.msgReceivedEvt";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for MobileDevice: " + evt);
          btn = null;
          break;
      }
      return btn;
    }
  });
  return MobileTasker;
});
