define([
  "app",
  "models/device/device",
  "text!templates/program/nodes/mailMobile.html"
], function(App, Device, ActionTemplate) {

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
     * @returns the action template specific for mail
     */
    getTemplateAction: function() {
      return _.template(ActionTemplate);
    },
    /**
     *return the list of available actions
     */
    getActions: function() {
      return ["vocalMessage", "sendSMS", "whereIsMyPhone"];
    },
    /**
     * return the keyboard code for a given action
     */
    getKeyboardForAction: function(act) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONAction("mandatory");

      switch (act) {
        case "vocalMessage":
          $(btn).append("<span>" + $.i18n.t('devices.mobileTasker.keyboard.vocalMessage', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.mobileTasker.keyboard.mobile') + "</span>"
          }));
          v.methodName = "vocalMessage";
          v.type="action";
          v.args = [
          {
            "type": "String",
            "value": "Coucou"
          }];
          v.phrase = "devices.mobileTasker.language.vocalMessage";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "sendSMS":
          $(btn).append("<span>" + $.i18n.t('devices.mobileTasker.keyboard.sendSMS', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.mobileTasker.keyboard.mobile') + "</span>"
          }));
          v.methodName = "sendSMS";
          v.type="action";
          v.args = [
          {
            "type": "String",
            "value": "num"
          },
          {
            "type": "String",
            "value": "Coucou"
          }];
          v.phrase = "devices.mobileTasker.language.sendSMS";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "whereIsMyPhone":
          $(btn).append("<span>" + $.i18n.t('devices.mobileTasker.keyboard.whereIsMyPhone', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.mobileTasker.keyboard.mobile') + "</span>"
          }));
          v.methodName = "whereIsMyPhone";
          v.type="action";
          v.phrase = "devices.mobileTasker.language.whereIsMyPhone";
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
