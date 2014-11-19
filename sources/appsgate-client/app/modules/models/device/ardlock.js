define([
  "app",
  "models/device/device",
  "text!templates/program/nodes/ardActionNode.html"
], function(App, Device,ActionTemplate) {

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
        var self = this;
      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
            this.generateDefaultName($.i18n.t("devices.ard.name.singular"));
      }

      this.remoteControl("getZonesAvailable", [], "zonesavailable");
      this.remoteControl("getInputsAvailable", [], "inputsavailable");

      dispatcher.on('zonesavailable', function(zones) {
            console.log("zones available received: "+JSON.stringify(zones,4,null));
            self.set("zones",zones);
      });

      dispatcher.on('inputsavailable', function(inputs) {
            console.log("inputs available received: "+JSON.stringify(inputs,4,null));
            self.set("inputs",inputs);
      });
    },
    getEvents: function() {
          return ["isAuthorized","isNotAuthorized","alarmFired"];
      },
    getStates: function(which) {
          switch (which) {
              case "state":
                  return []; //"getLastCard"
              default:
                  return [];
          }
      },
    getActions: function() {
          return ["zoneActivate", "zoneDesactivate", "forceInput"];
      },
      getKeyboardForAction: function(act){
          var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
          var v = this.getJSONAction("mandatory");

          switch(act) {
              case "zoneActivate":
                  $(btn).append("<span data-i18n='devices.ard.keyboard.zone-activate'/>");
                  v.methodName = "zoneActivate";
                  v.phrase = "devices.ard.action.zone-activate";
                  v.args = [ {"type":"int", "value": "1"}];
                  $(btn).attr("json", JSON.stringify(v));
                  break;
              case "zoneDesactivate":
                  $(btn).append("<span data-i18n='devices.ard.keyboard.zone-desactivate'/>");
                  v.methodName = "zoneDesactivate";
                  v.phrase = "devices.ard.action.zone-desactivate";
                  v.args = [ {"type":"int", "value": "1"}];
                  $(btn).attr("json", JSON.stringify(v));
                  break;
              case "forceInput":
                  $(btn).append("<span data-i18n='devices.ard.keyboard.force-input'/>");
                  v.methodName = "forceInput";
                  v.phrase = "devices.ard.action.force-input";
                  v.args = [ {"type":"int", "value": "1"}];
                  $(btn).attr("json", JSON.stringify(v));
                  break;
              default:
                  console.error("unexpected action found for ARD: " + act);
                  btn = null;
                  break;
          }
          return btn;
      },
    getKeyboardForState: function(state, which){
      if (which !== "state") {
          console.error('Unsupported type of state: ' + which);
          return null;
      }
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONState("mandatory");
      switch(state) {
          case "getLastCard":
              $(btn).append("<span data-i18n='devices.ard.state.authorized'/>");
              v.phrase = "devices.ard.state.opened";
              v.name = "getLastCard";
              $(btn).attr("json", JSON.stringify(v));
              break;
          default:
              console.error("unexpected state found for ARD Lock: " + state);
              btn = null;
              break;
      }
      return btn;
    },
    getKeyboardForEvent: function(evt){
          var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
          var v = this.getJSONEvent("mandatory");
          switch(evt) {
              case "isAuthorized":
                  $(btn).append("<span data-i18n='devices.ard.event.authorized'/>");
                  v.eventName = "authorized";
                  v.eventValue = "true";
                  v.phrase = "devices.ard.state.authorized";
                  $(btn).attr("json", JSON.stringify(v));
                  break;
              case "isNotAuthorized":
                  $(btn).append("<span data-i18n='devices.ard.event.non_authorized'/>");
                  v.eventName = "authorized";
                  v.eventValue = "false";
                  v.phrase = "devices.ard.state.non_authorized";
                  $(btn).attr("json", JSON.stringify(v));
                  break;
              case "alarmFired":
                  $(btn).append("<span data-i18n='devices.ard.event.alarm_fired'/>");
                  v.eventName = "alarmFired";
                  v.eventValue = "*";
                  v.phrase = "devices.ard.state.alarm_fired";
                  $(btn).attr("json", JSON.stringify(v));
                  break;
              default:
                  console.error("unexpected event found for Contact Sensor: " + evt);
                  btn = null;
                  break;
          }
          return btn;
      },
    getTemplateAction: function() {
      return _.template(ActionTemplate);
    },
    getTemplateParameter: function(){
      console.log("Actual zones:"+JSON.stringify(this.get("zones"),4,null));
      return {zones:this.get("zones"),inputs:this.get("inputs")}//inputs:[{input_idx:1,input_name:"porte1"},{input_idx:2,input_name:"porte2"}]};//{zones:[{'zone_idx':1,'zone_name':"exterieur"}]};

    }

  });
  return ARDLock;
});
