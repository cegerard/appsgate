define([
	"app",
	"models/user"
], function(App, User) {

	var Users = {};
  // collection
  Users = Backbone.Collection.extend({
    model: User,

    /**
     * Fetch the users from the server
     *
     * @constructor
     */
    initialize:function() {
      var self = this;
			

		},
		
		addUser:function(brick) {
			this.add(new User(brick));
		},
		
  });
	
	return Users;
	
});
