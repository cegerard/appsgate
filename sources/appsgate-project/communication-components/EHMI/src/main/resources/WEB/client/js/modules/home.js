define([
	'jquery',
	'underscore',
	'backbone',
	'text!templates/home/home.html',
], function($, _, Backbone, homeTemplate) {
	// initialize the module
	var Home = {};

	// initialize the views array
	Home.Views = {};

	// home view
	Home.Views.Default = Backbone.View.extend({
		el: $("#container"),
		template: _.template(homeTemplate),

		// render the homepage of the application
		render:function() {
			this.$el.html(this.template());
			return this;
		}
	});

	return Home;
});
