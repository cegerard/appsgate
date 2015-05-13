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
          v.methodName = "sendMessage";
          v.phrase = "devices.mobileTasker.language.sendMessage";
          $(btn).attr("json", JSON.stringify(v));
          break;

        default:
          console.error("unexpected action found for PhilipsHue: " + act);
          btn = null;
          break;
      }
      return btn;
    }
  });
  return MobileTasker;
});
