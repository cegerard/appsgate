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
    },
        /**
     * return the list of available events
     */
    getEvents: function() {
      return ["Music", "Meal", "Question", "Lan", "Night", "inactivate", "activate","east","west"];
    },
    /**
     * return the keyboard code for a given event
    */
    getKeyboardForEvent: function(evt){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONEvent("mandatory");
      v.source.type = "device";
      v.source.deviceType = "210";
      v.source.value = this.get("id");
      switch(evt) {
        case "Music":
          $(btn).append("<img src='/app/img/music.png' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "3";
          v.icon = "/app/img/music.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Meal":
          $(btn).append("<img src='/app/img/meal.png' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "6";
          v.icon = "/app/img/meal.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Question":
          $(btn).append("<img src='/app/img/question.svg' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "4";
          v.icon = "/app/img/question.svg";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Lan":
          $(btn).append("<img src='/app/img/lan.svg' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "1";
          v.icon = "/app/img/lan.svg";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Night":
          $(btn).append("<img src='/app/img/night.png' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "5";
          v.icon = "/app/img/night.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "inactivate":
          $(btn).append("<span data-i18n='devices.domicube.keyboard.inactivated'></span>");
          v.eventName = "newFace";
          v.eventValue = "2";
          //v.icon = "/app/img/music.png";
          v.phrase = "devices.domicube.language.inactivated";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "activate":
          $(btn).append("<span data-i18n='devices.domicube.keyboard.inactivated'></span>");
          v.eventName = "leaveFace";
          v.eventValue = "2";
          v.phrase = "devices.domicube.language.activated";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "west":
              //$(btn).append("<span data-i18n='devices.domicube.keyboard.west'></span>");
              $(btn).append("<img src='/app/img/cube-turn-left.png' width='36px'>");
              v.eventName = "newDirection";
              v.eventValue = "west";
              //v.phrase = "devices.domicube.language.west";
              v.icon = "/app/img/cube-turn-left.png";
              $(btn).attr("json", JSON.stringify(v));
              break;
        case "east":
              //$(btn).append("<span data-i18n='devices.domicube.keyboard.east'></span>");
              $(btn).append("<img src='/app/img/cube-turn-right.png' width='36px'>");
              v.eventName = "newDirection";
              v.eventValue = "east";
              //v.phrase = "devices.domicube.language.east";
              v.icon = "/app/img/cube-turn-right.png";
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
      return ["Music", "Meal", "Question", "Lan", "Night", "inactivate","dimDirection"];
        default:
          return [];
      }
    },

    /**
     * return the keyboard code for a given state
    */
    getKeyboardForState: function(state, which){
      if (which !== "state") {
        console.error('Unsupported type of state: ' + which);
        return null;
      }
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = this.getJSONState("mandatory");
      v.object.type = "device";
      v.object.deviceType = "210";
      v.object.value = this.get("id");
      switch(state) {
        case "Music":
          $(btn).append("<img src='/app/img/music.png' width='36px'>");
          v.icon = "/app/img/music.png";
          v.name = "Music";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Meal":
          $(btn).append("<img src='/app/img/meal.png' width='36px'>");
          v.icon = "/app/img/meal.png";
          v.name = "Meal";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Question":
          $(btn).append("<img src='/app/img/question.svg' width='36px'>");
          v.icon = "/app/img/question.svg";
          v.name = "Question";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Lan":
          $(btn).append("<img src='/app/img/lan.svg' width='36px'>");
          v.icon = "/app/img/lan.svg";
          v.name = "Lan";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Night":
          $(btn).append("<img src='/app/img/night.png' width='36px'>");
          v.icon = "/app/img/night.png";
          v.name = "Night";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "inactivate":
          $(btn).append("<span data-i18n='language.domicube-inactivated'></span>");
          v.name = "inactivate";
          v.phrase = "language.domicube-inactivated";
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
