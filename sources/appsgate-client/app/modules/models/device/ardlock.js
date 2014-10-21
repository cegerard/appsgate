define([
  "app",
  "models/device/device"
], function(App, Device) {

  var ARDLock = {};
  /**
   * Implementation of a Contact sensor
   * @class Device.ContactSensor
   */
  ARDLock = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      ARDLock.__super__.initialize.apply(this, arguments);

      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
            this.generateDefaultName($.i18n.t("devices.ard.name.singular"));
      }
    },
    getEvents: function() {
          return ["isAuthorized","isNotAuthorized"];
      },
    getStates: function(which) {
          switch (which) {
              case "state":
                  return []; //"getLastCard"
              default:
                  return [];
          }
      },
    getKeyboardForState: function(state, which){
      if (which !== "state") {
          console.error('Unsupported type of state: ' + which);
          return null;
      }
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONState("mandatory");
      switch(state) {
          case "getLastCard":
              $(btn).append("<span data-i18n='devices.ard.state.authorized'/>");
              v.phrase = "devices.ard.state.opened";
              v.name = "getLastCard";
              $(btn).attr("json", JSON.stringify(v));
              break;
          default:
              console.error("unexpected state found for Contact Sensor: " + state);
              btn = null;
              break;
      }
      return btn;
    },
    getKeyboardForEvent: function(evt){
          var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
          var v = this.getJSONEvent("mandatory");
          switch(evt) {
              case "isAuthorized":
                  $(btn).append("<span data-i18n='devices.ard.event.authorized'/>");
                  v.eventName = "authorized";
                  v.eventValue = "true";
                  v.phrase = "devices.ard.event.authorized";
                  $(btn).attr("json", JSON.stringify(v));
                  break;
              case "isNotAuthorized":
                  $(btn).append("<span data-i18n='devices.ard.event.non_authorized'/>");
                  v.eventName = "authorized";
                  v.eventValue = "false";
                  v.phrase = "devices.ard.event.non_authorized";
                  $(btn).attr("json", JSON.stringify(v));
                  break;
              default:
                  console.error("unexpected event found for Contact Sensor: " + evt);
                  btn = null;
                  break;
          }
          return btn;
      }
  });
  return ARDLock;
});
