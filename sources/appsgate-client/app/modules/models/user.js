define([
  "app",
	"models/brick"
], function(App, Brick) {

	var User = {};

  /**
	 * Universe model class, representing a universe in AppsGate
	 */
  User = Brick.extend({
    /**
     * @constructor
     */
    initialize:function() {
      User.__super__.initialize.apply(this, arguments);
			this.type = "USER";
    },


  });
	return User;
});