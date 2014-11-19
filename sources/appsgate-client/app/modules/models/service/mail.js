define([
  "app",
  "models/service/service",
  "text!templates/program/nodes/mailActionNode.html",
    "text!templates/program/nodes/defaultEventNode.html"
], function(App, Service, ActionTemplate, EventTemplate) {

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
          try{
              t = JSON.parse(json.value);
              if (Array.isArray(t)) {
                  this.setFavorites(t);
              }
          }catch(err) {}
      });

    },

      getEvents: function() {
          return ["mailSent"];
      },
      /**
       * return the keyboard code for a given event
       */
      getKeyboardForEvent: function(evt){
          var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
          var v = this.getJSONEvent("mandatory");
          switch(evt) {
              case "mailSent":
                  $(btn).append("<span data-i18n='services.mail.keyboard.mailSent'><span>");

                  v.eventName = "mailSent";
                  v.eventValue = "*";
                  v.source.value = this.get("id");
                  v.source.type = "service";
                  v.phrase = "services.mail.keyboard.mailSent";
                  $(btn).attr("json", JSON.stringify(v));
                  break;
              default:
                  console.error("unexpected event found for Mail: " + evt);
                  btn = null;
                  break;
          }
          return btn;
      },
      /**
       * @returns event template for clock
       */
      getTemplateEvent: function() {
          return _.template(EventTemplate);
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
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
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
      v = this.getFavorites();
      for (c in v) {
        return v[c].mail;
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
          if(v[c].mail == which) {
              found=true;
              break;
          }
      }
      if(found){
          delete v[c];
      }
      this.setFavorites(v);

    },

    getNumberOfFavorites: function () {
      var i = 0;
      for (val in this.getFavorites()) {
        i++;
      }
      return i;
    },

    /**
     * update a favorite mail
     */
    updateFavorite: function(old, which) {
      v = this.getFavorites();
      if (old == "") {
        v.push({mail:which});
        this.setFavorites(v);
        //code
      } else {
        this.remoteControl("removeFavoriteRecipient", [{"type": "String", "value": old}]);
        for (t in v) {
          if (v[t].mail === old) {
              v[t].mail = which;
              this.setFavorites(v);
              break;
          }
        }
      }
      this.remoteControl("addFavoriteRecipient", [{"type": "String", "value": which}]);

    }

  });
  return Mail;
});
