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

		//		createFilters: function (model) {
		//			var self = this;
		//			d3.select(this.el).select("#filterContainerNodes").selectAll("div")
		//				.data(model.get("entitiesTypes"))
		//				.enter()
		//				.append("div")
		//				.attr("class", "checkbox-container")
		//				.append("label")
		//				.each(function (d) {
		//					// create checkbox for each data
		//					d3.select(this).append("input")
		//						.attr("type", "checkbox")
		//						.attr("id", function (d) {
		//							return "chk_node_" + d;
		//						})
		//						.property("checked", function (d) {
		//							return _.contains(self.model.get("currentEntitiesTypes"), d);
		//						})
		//						.on("click", function (d, i) {
		//							// register on click event
		//							self.applyFilter("entities", d, this.checked);
		//							self.updateCheckAllEntities(self.model.get("entitiesTypes"));
		//						})
		//					d3.select(this).append("span")
		//						.text(function (d) {
		//							return d;
		//						});
		//				});
		//
		//			d3.select("#filterContainerLinks").selectAll("div")
		//				.data(model.get("relationsTypes"))
		//				.enter()
		//				.append("div")
		//				.attr("class", "checkbox-container")
		//				.append("label")
		//				.each(function (d) {
		//					// create checkbox for each data
		//					d3.select(this).append("input")
		//						.attr("type", "checkbox")
		//						.attr("id", function (d) {
		//							return "chk_link_" + d;
		//						})
		//						.property("checked", function (d) {
		//							return _.contains(self.model.get("currentRelationsTypes"), d);
		//						})
		//						.on("click", function (d, i) {
		//							// register on click event
		//							self.applyFilter("relations", d, this.checked);
		//						})
		//					d3.select(this).append("span")
		//						.text(function (d) {
		//							return d;
		//						});
		//				});
		//
		//		},
		//
		//		applyFilter: function (arrayUpdated, type, checked) {
		//			force.stop();
		//			this.model.updateArrayTypes(arrayUpdated, type, checked);
		//			this.model.updateEntitiesShown();
		//			if (arrayUpdated === "entities") {
		//				if (this.model.get("rootNode") !== "" && !_.contains(this.model.get("currentEntities"), this.model.get("rootNode"))) {
		//					// Vu que si on a plus rien d'afficher, on ne fait pas le move, on a toujours l'ancienne valeur pour la root node. Risque de Bug.
		//					if (this.model.get("currentEntities").length > 0) {
		//						this.selectAndMoveRootNode(this.model.get("currentEntities")[0]);
		//					}
		//				}
		//			} else {
		//				this.model.updateRelationsShown();
		//			}
		//
		//			this.update(this.model);
		//			force.start();
		//		},
		//
		//		updateCheckAllEntities: function (entitiesTypesArray) {
		//			var nbEntities = entitiesTypesArray.length;
		//			var nbChecked = 0;
		//			entitiesTypesArray.forEach(function (entityType) {
		//				if ($("#chk_node_" + entityType).is(':checked')) {
		//					nbChecked++;
		//				}
		//			});
		//
		//			if (nbChecked === nbEntities) {
		//				$("#checkbox-all-entities").prop('checked', true);
		//			} else {
		//				$("#checkbox-all-entities").prop('checked', false);
		//			}
		//		}

	});

	return DependencyMenuView;
});