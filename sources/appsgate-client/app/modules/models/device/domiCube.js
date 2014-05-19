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
      return ["Face1", "Face6", "Face3", "Face4", "Face5", "inactivate"];
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
        case "Face1":
          $(btn).append("<img src='/app/img/night.png' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "1";
          v.icon = "/app/img/question.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Face6":
          $(btn).append("<img src='/app/img/meal.png' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "6";
          v.icon = "/app/img/music.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Face3":
          $(btn).append("<img src='/app/img/music.png' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "3";
          v.icon = "/app/img/lan.png";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Face4":
          $(btn).append("<img src='/app/img/lan.svg' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "4";
          v.icon = "/app/img/meal.svg";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "Face5":
          $(btn).append("<img src='/app/img/question.svg' width='36px'>");
          v.eventName = "newFace";
          v.eventValue = "5";
          v.icon = "/app/img/night.svg";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "inactivate":
          $(btn).append("<span data-i18n='language.domicube-inactivated'></span>");
          v.eventName = "newFace";
          v.eventValue = "2";
          //v.icon = "/app/img/music.png";
          v.phrase = "language.domicube-inactivated";
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
     * @returns the action template specific for lamps
     */
    getTemplateAction: function() {
      return _.template(EventTemplate);  
    }
      
  });
  
  return DomiCube;
});
