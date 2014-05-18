define([
  "app",
  "text!templates/home/login.html",
	"text!templates/users/userContainer.html"
], function(App, homeTemplate, userTemplate) {

  var LoginView = {};

  /**
   * View used as login page of the application
   */
  LoginView = Backbone.View.extend({
    el: $("#main"),
    template: _.template(homeTemplate),
		tplUserContainer: _.template(userTemplate),

    /**
     * Bind events of the DOM elements from the view to their callback
     */
    events: {
      "click button.btn-lg"	: "submit",
      "keyup input.user-id"	: "validUserId",
      "keyup input.password" : "validPassword",
      "keyup input.confirm-password" : "validPassword"

    },

    /**
     * constructor
     */
    initialize:function() {
      var self = this;
      LoginView.__super__.initialize.apply(this, arguments);

    },

    // render the homepage of the application
    render:function() {
			var self = this;
			this.$el.html(this.template());
			
			/*if(AppsGate.Users.length > 0) {
				AppsGate.Users.forEach(function(user) {
					$(self.$el.find(".user-list")[0]).append(self.tplUserContainer({
						user : user
					}));
				});
			}
			else {*/
				$(self.$el.find(".user-list")[0]).append(self.tplUserContainer());
			//}

      // translate the view
      this.$el.i18n();

      return this;
    },

    submit:function(e) {
      e.preventDefault();

      // name already exists
      /*if (AppsGate.Users.where({ id : $(this.$el.find(".user-id")[0]).val() }).length > 0) {
        var user = AppsGate.Users.where({ id: $(this.$el.find(".user-id")[0]).val() });
        if(user.get("password") !== $(this.$el.find(".password")[0]).val()){
          return false;
        }
      }
      else{*/
          // Routing to home
          AppsGate.Router.home();
      //}
    },

    validUserId:function(e) {

      // name is empty
      if ($(this.$el.find(".user-id")[0]).val() === "") {
        $(".btn-lg").addClass("disabled");

        return false;
      }

      // ok
      $(".btn-lg").removeClass("disabled");

      return true;

    },

    validPassword:function(e) {
      // passwords aren't same
      if ($(this.$el.find(".password")[0]).val() !== $(this.$el.find(".confirm-password")[0]).val()) {
        $(".btn-lg").addClass("disabled");

        return false;
      }
      else {
        return this.validUserId();
      }
    }


  });



  return LoginView;
});
