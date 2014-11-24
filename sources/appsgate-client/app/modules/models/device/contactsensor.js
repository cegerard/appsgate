define([
  "app",
  "models/device/device"
], function(App, Device) {

  var ContactSensor = {};
  /**
   * Implementation of a Contact sensor
   * @class Device.ContactSensor
   */
  ContactSensor = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      ContactSensor.__super__.initialize.apply(this, arguments);

      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
        this.generateDefaultName($.i18n.t("devices.contact.name.singular"));
      }
    },
    /**
     * return the list of available events
     */
    getEvents: function() {
      return ["isOpen", "isClose"];
    },
    /**
     * return the keyboard code for a given event
     */
    getKeyboardForEvent: function(evt) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONEvent("mandatory");
      switch (evt) {
        case "isOpen":
          $(btn).append("<span>" + $.i18n.t('devices.contact.keyboard.openEvent', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.contact.keyboard.contact') + "</span>",
          }));
          v.eventName = "contact";
          v.eventValue = "false";
          v.phrase = "devices.contact.language.openEvent";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "isClose":
          $(btn).append("<span>" + $.i18n.t('devices.contact.keyboard.closeEvent', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.contact.keyboard.contact') + "</span>",
          }));
          v.eventName = "contact";
          v.eventValue = "true";
          v.phrase = "devices.contact.language.closeEvent";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for Contact Sensor: " + evt);
          btn = null;
          break;
      }
      return btn;
    },
    /**
     * return the list of available states
     */
    getStates: function(which) {
      switch (which) {
        case "state":
          return ["isOpen", "isClose"];
        default:
          return [];
      }
    },
    /**
     * return the keyboard code for a given state
     */
    getKeyboardForState: function(state, which) {
      if (which !== "state") {
        console.error('Unsupported type of state: ' + which);
        return null;
      }
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONState("mandatory");
      switch (state) {
        case "isOpen":
          $(btn).append("<span>" + $.i18n.t('devices.contact.keyboard.isOpen', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.contact.keyboard.contact') + "</span>",
          }));
          v.phrase = "devices.contact.language.isOpen";
          v.name = "isOpen";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "isClosed":
          $(btn).append("<span>" + $.i18n.t('devices.contact.keyboard.isClosed', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.contact.keyboard.contact') + "</span>",
          }));
          v.name = "isClose";
          v.phrase = "devices.contact.language.isClosed";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected state found for Contact Sensor: " + state);
          btn = null;
          break;
      }
      return btn;
    },
  });
  return ContactSensor;
});
