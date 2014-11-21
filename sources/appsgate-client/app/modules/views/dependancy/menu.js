define([
  "app",
  "text!templates/dependancy/menu/menu.html",
  "text!templates/dependancy/menu/filterContainer.html",
  ], function (App, dependancyMenuTemplate, dependancyContainerMenuTemplate) {

	var DependencyMenuView = {};
	/**
	 * Render the side menu for the dependencies graph
	 */
	DependencyMenuView = Backbone.View.extend({
		tpl: _.template(dependancyMenuTemplate),
		tplDependancyContainer: _.template(dependancyContainerMenuTemplate),

		/**
		 * Render the side menu
		 */
		render: function () {
			
			// On se contente d'instancier le template du menu qui va placer les div pour les filtres, ensuite, ils seront peupler via les méthodes dans graph.js
			this.$el.append(this.tplDependancyContainer());
		},

	});

	return DependencyMenuView;
});