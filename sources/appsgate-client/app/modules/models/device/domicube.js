define([
  "app",
  "models/device/device",
  "text!templates/program/nodes/domicubeEventNode.html"

], function(App, Device, EventTemplate) {

  var DomiCube = {};

  /**
   * Implementation of Domicube
   *
   * @class Device.DomiCube
   */
  DomiCube = Device.extend({
    /**
     * @constructor
     */
    initialize: function() {
      DomiCube.__super__.initialize.apply(this, arguments);

      // setting default friendly name if none exists
      if (typeof this.get("name") === "undefined" || this.get("name") === "") {
        this.generateDefaultName($.i18n.t("devices.domicube.name.singular"));
      }
    },
    /**
     * return the list of available events
     */
    getEvents: function() {
      return ["getNewFace"];
//      return ["Music", "Meal", "Question", "Lan", "Night", "inactivate", "activate", "east", "west", "change"];
    },
    /**
     * return the keyboard code for a given event
     */
    getKeyboardForEvent: function(evt) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v =  {
        "type": "event",
        "source": {
          "iid": "X",
          "type": 'device',
          "deviceType": this.get("type"),
          "value" : this.get("id")
        },
        "iid": "X"
      };
      switch (evt) {
        case "getNewFace":
          $(btn).append("<span data-i18n='devices.domicube.keyboard.newFace'></span>");
          v.eventName = "newFace";
          v.param = {"type": "param", "iid": "X", "deviceType": this.get("type"), "param" : "event", "mandatory" : true};
          v.phrase = "devices.domicube.language.newFace";
          
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Music":
          $(btn).append("<img src='app/img/domicube-music.png' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "3";
          v.icon = "app/img/domicube-music.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Meal":
          $(btn).append("<img src='app/img/domicube-meal.png' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "6";
          v.icon = "app/img/domicube-meal.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Question":
          $(btn).append("<img src='app/img/domicube-question.svg' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "4";
          v.icon = "app/img/domicube-question.svg";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Lan":
          $(btn).append("<img src='app/img/domicube-work.svg' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "1";
          v.icon = "app/img/domicube-work.svg";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Night":
          $(btn).append("<img src='app/img/domicube-night.png' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "5";
          v.icon = "app/img/domicube-night.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "inactivate":
          $(btn).append("<span data-i18n='devices.domicube.keyboard.inactivated'></span>");
          v.eventName = "newFace";
          v.eventValue = "2";
          //v.icon = "app/img/domicube-music.png";
          v.phrase = "devices.domicube.language.inactivated";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "activate":
          $(btn).append("<span data-i18n='devices.domicube.keyboard.activated'></span>");
          v.eventName = "leaveFace";
          v.eventValue = "2";
          v.phrase = "devices.domicube.language.activated";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "west":
          //$(btn).append("<span data-i18n='devices.domicube.keyboard.west'></span>");
          $(btn).append("<img src='app/img/cube-turn-left.png' width='36px'>");
          v.eventName = "newDirection";
          v.eventValue = "west";
          //v.phrase = "devices.domicube.language.west";
          v.icon = "app/img/cube-turn-left.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "east":
          //$(btn).append("<span data-i18n='devices.domicube.keyboard.east'></span>");
          $(btn).append("<img src='app/img/cube-turn-right.png' width='36px'>");
          v.eventName = "newDirection";
          v.eventValue = "east";
          //v.phrase = "devices.domicube.language.east";
          v.icon = "app/img/cube-turn-right.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "change":
          $(btn).append("<span data-i18n='devices.domicube.keyboard.leaveFace'></span>");
          v.eventName = "leaveFace";
          v.eventValue = "*";
          v.phrase = "devices.domicube.language.leaveFace";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for DomiCube: " + evt);
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
        //case
        case "state":
          return ["face"];
//          return ["Music", "Meal", "Question", "Lan", "Night", "inactivate"];
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
      var v = this.getJSONState("mandatory");
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      v.object.type = "device";
      v.object.deviceType = "210";
      v.object.value = this.get("id");
      
      switch (state) {
        case "face":
          $(btn).append("<span data-i18n='devices.domicube.keyboard.face'></span>");
          v.eventName = "newFace";
          v.param = {"type": "param", "iid": "X", "deviceType": this.get("type"), "param" : "state", "mandatory" : true};
          v.phrase = "devices.domicube.language.face";
          
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Music":
          $(btn).append("<img src='app/img/domicube-music.png' width='36px'>");
          v.icon = "app/img/domicube-music.png";
          v.name = "Music";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Meal":
          $(btn).append("<img src='app/img/domicube-meal.png' width='36px'>");
          v.icon = "app/img/domicube-meal.png";
          v.name = "Meal";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Question":
          $(btn).append("<img src='app/img/domicube-question.svg' width='36px'>");
          v.icon = "app/img/domicube-question.svg";
          v.name = "Question";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Lan":
          $(btn).append("<img src='app/img/domicube-work.svg' width='36px'>");
          v.icon = "app/img/domicube-work.svg";
          v.name = "Lan";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Night":
          $(btn).append("<img src='app/img/domicube-night.png' width='36px'>");
          v.icon = "app/img/domicube-night.png";
          v.name = "Night";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "inactivate":
          $(btn).append("<span data-i18n='devices.domicube.keyboard.inactivated'></span>");
          v.name = "inactivate";
          v.phrase = "devices.domicube.language.inactivated";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected state found for Domicube: " + state);
          btn = null;
          break;
      }
      return btn;
    },
    /**
     * Return the list of params for a given type of params
     */
    getParams: function(type) {
          return ["Music", "Meal", "Question", "Lan", "Night"];
    },

    getKeyboardForParam: function(which) {
      
      var v = { 'type' : 'param', 'deviceType' : this.get("type"), "iid" : "X"};
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      switch (which) {
        case "Music":
          v.value = "3";
          v.icon = "app/img/domicube-music.png";
          break;
        case "Meal":
          v.value = "6";
          v.icon = "app/img/domicube-meal.png";
          break;
        case "Question":
          v.value = "4";
          v.icon = "app/img/domicube-question.svg";
          break;
        case "Lan":
          v.value = "1";
          v.icon = "app/img/domicube-work.svg";
          break;
        case "Night":
          v.value = "5";
          v.icon = "app/img/domicube-night.png";
          break;
        default:
          console.error("unexpected state found for Domicube: " + state);
          return null;
      }
          $(btn).append("<img src='" + v.icon + "' width='36px'>");
          $(btn).attr("json", JSON.stringify(v));
      return btn;
    },
    
    /**
     * @returns the event template specific for domicube
     */
    getTemplateEvent: function() {
      return _.template(EventTemplate);
    },
    /**
     * @returns the state template specific for domicube
     * @note this is the same template as the event template
     */
    getTemplateState: function() {
      return _.template(EventTemplate);
    }

  });

  return DomiCube;
});
