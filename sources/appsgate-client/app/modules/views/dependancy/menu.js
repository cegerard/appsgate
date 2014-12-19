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

		events: {
			"click input#checkbox-all-entities": "onClickCheckAllEntities",
			"click input#checkbox-all-relations": "onClickCheckAllRelations",
		},

		onClickCheckAllEntities: function (e) {
			var self = this;
			// Clear or reinite the current array of type
			if (!$("#checkbox-all-entities").is(':checked')) {
				this.model.set({
					currentEntitiesTypes: []
				});
			} else {
				this.model.set({
					// Clone of the entities type to avoid ref pb
					currentEntitiesTypes: self.model.get("entitiesTypes").slice(0)
				});
			} 
		},
		
		onClickCheckAllRelations: function (e) {
			var self = this;
			// Clear or reinite the current array of type
			if (!$("#checkbox-all-relations").is(':checked')) {
				this.model.set({
					currentRelationsTypes: []
				});
			} else {
				this.model.set({
					// Clone of the entities type to avoid ref pb
					currentRelationsTypes: self.model.get("relationsTypes").slice(0)
				});
			} 
		},

		/**
		 * Render the side menu
		 */
		render: function () {

			// On se contente d'instancier le template du menu qui va placer les div pour les filtres, ensuite, ils seront peupler via les méthodes dans graph.js
			this.$el.html(this.tplDependancyContainer());
//			this.createFilters(this.model);
		},

	});

	return DependencyMenuView;
});