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
      this.set("favorites", []);
      Mail.__super__.initialize.apply(this, arguments);
      dispatcher.on(this.get("id"), function(json) {
        t = JSON.parse(json.value);
        if (Array.isArray(t)) {
          this.setFavorites(t);
        }
      });

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
          v.args = [ {"type":"String", "value": this.getFavoriteMail()},
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
    
    /**
     */
    setFavorites: function(array) {
      this.set("favorite-recipients", array);
    },
    /**
     * @returns the list of favorites mail
     */
    getFavorites: function() {
      return this.get("favorite-recipients");
    },
    getFavoriteMail: function() {
      if (this.getFavorites().length > 0) {
        return this.getFavorites()[0].mail;
      }
      return "mail@example.com";
    },
    getFavoriteArray: function() {
      var a = [];
      v = this.getFavorites();
      for (c in v) {
        a.push(v[c].mail);
      }
      return a;
    },
    /**
     * remove a favorite mail
     */
    removeFavorite: function(which) {
      this.remoteControl("removeFavoriteRecipient", [{"type": "String", "value": which}]);
      v = this.getFavorites();
      var c, found=false;
      for(c in v) {
          if(obj[c].mail == val) {
              found=true;
              break;
          }
      }
      if(found){
          delete v[c];
      }
      this.setFavorites(v);

    },
    
    /**
     * update a favorite mail
     */
    updateFavorite: function(old, which) {
      console.log("UPD_MAIL: Not implemented yet");  
      this.remoteControl("removeFavoriteRecipient", [{"type": "String", "value": old}]);
      this.remoteControl("addFavoriteRecipient", [{"type": "String", "value": which}]);
      v = this.getFavorites();
      for (t in v) {
        if (v[t].mail === old) {
            v[t].mail = which;
            this.setFavorites(v);
            return;
        }
      }

    }    
    
  });
  return Mail;
});
