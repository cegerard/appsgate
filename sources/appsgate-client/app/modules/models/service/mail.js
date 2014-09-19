define([
  "app",
  "models/service/service",
  "text!templates/program/nodes/mailActionNode.html"  
], function(App, Service, ActionTemplate) {

  var Mail = {};

  /**
   * Abstract class regrouping common characteristics shared by all the devices
   *
   * @class Device.Model
   */
  Mail = Service.extend({
    /**
     * @constructor
     */
    initialize: function() {
      Mail.__super__.initialize.apply(this, arguments);
    },
    /**
     * return the list of available actions
     */
    getActions: function() {
      return ["sendMail"];
    },
    /**
     * return the keyboard code for a given action
    */
    getKeyboardForAction: function(act){
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = {"type": "action", "target": {"iid": "X", "type": "service", "serviceType":this.get("type"), "value":this.get("id")}, "iid": "X"};
      switch(act) {
        case "sendMail":
          $(btn).append("<span data-i18n='services.mail.keyboard.sendMail'></span>");
          v.methodName = "sendMailSimple";
          v.args = [ {"type":"String", "value": "mail@example.com"},
                    {"type":"String", "value": "Test"},
                    {"type":"String", "value": "..."}];
          v.phrase = "services.mail.language.sendMail";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected action found for Mail: " + act);
          btn = null;
          break;
      }
      return btn;
    },
    /**
     * @returns the action template specific for mail
     */
    getTemplateAction: function() {
      return _.template(ActionTemplate);  
    },
    
  });
  return Mail;
});
