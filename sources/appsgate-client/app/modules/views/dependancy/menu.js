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

		initialize: function () {
			this.render();
			//			this.createFilters(this.model);
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

		createFilters: function (model) {
			var self = this;
			self.createEntityFilter(model);
			self.createRelationFilter(model);
		},

		createEntityFilter: function (model) {
			var self = this;
			d3.select("#entitiesGroupFilter")
				.append("div")
				.attr("class", "row")
				.selectAll("div")
				.data(model.get("filterEntities"))
				.enter()
				.append("div")
				.attr("class", "col-md-11 col-md-offset-1")
				.attr("id", function (f) {
					return "div-filter-" + f;
				})
				.append("label")
				.each(function (d) {
					// create checkbox for each data
					d3.select(this).append("input")
						.attr("type", "checkbox")
						.attr("id", function (d) {
							return "chk_node_" + d;
						})
						.property("checked", function (d) {
							return _.contains(self.model.get("currentEntitiesFilters"), d);
						})
						.on("click", function (d, i) {
							self.updateCurrentEntitiesFilters(d, this.checked);
							// Reinitialize all the popover because some of them may have been recreated
							$('[data-toggle="popover"]').popover();
						})
					d3.select(this).append("span")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.entity." + d);
						});
				});

			// Sous filtre type device
			d3.select("#div-filter-device")
				.append("div")
				.attr("class", "col-md-12")
				.append("div")
				.attr("class", "subfilter-title")
				.append("label")
				.append("a")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#collapse1")
				.text("Types");

			d3.select("#div-filter-device")
				.append("div")
				.attr("id", "collapse1")
				.attr("class", "panel-collapse collapse in col-md-11 col-md-offset-1")
				.append("div")
				.attr("class", "row")
				.selectAll("div")
				.data(model.get("subFilterDevice")["deviceType"])
				.enter()
				.append("div")
				.attr("class", "col-md-12")
				.append("label")
				.each(function (d) {
					// create checkbox for each data
					d3.select(this).append("input")
						.attr("type", "checkbox")
						.attr("id", function (d) {
							return "chk_node_" + d;
						})
						.property("checked", function (d) {
							return _.contains(self.model.get("currentEntitiesFilters"), d);
						})
						.on("click", function (d, i) {
							self.updateCurrentEntitiesFilters(d, this.checked);
							// Reinitialize all the popover because some of them may have been recreated
							$('[data-toggle="popover"]').popover();
						})
					d3.select(this).append("span")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.entity.device-type." + d);
						})
				});

			// Sous filtre status device
			d3.select("#div-filter-device")
				.append("div")
				.attr("class", "col-md-12")
				.append("div")
				.attr("class", "subfilter-title")
				.append("label")
				.append("a")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#collapse2")
				.text("Etat");

			d3.select("#div-filter-device")
				.append("div")
				.attr("id", "collapse2")
				.attr("class", "panel-collapse collapse in col-md-11 col-md-offset-1")
				.append("div")
				.attr("class", "row")
				.selectAll("div")
				.data(model.get("subFilterDevice")["deviceState"])
				.enter()
				.append("div")
				.attr("class", "col-md-12")
				.append("label")
				.each(function (d) {
					// create checkbox for each data
					d3.select(this).append("input")
						.attr("type", "checkbox")
						.attr("id", function (d) {
							return "chk_node_" + d;
						})
						.property("checked", function (d) {
							return _.contains(self.model.get("currentEntitiesFilters"), d);
						})
						.on("click", function (d, i) {
							self.updateCurrentEntitiesFilters(d, this.checked);
							// Reinitialize all the popover because some of them may have been recreated
							$('[data-toggle="popover"]').popover();
						})
					d3.select(this).append("span")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.entity.device-state." + d);
						})
				});


			// Sous filtre status programme
			d3.select("#div-filter-program")
				.append("div")
				.attr("class", "col-md-12")
				.append("div")
				.attr("class", "subfilter-title")
				.append("label")
				.append("a")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#collapse3")
				.text("Etat");

			d3.select("#div-filter-program")
				.append("div")
				.attr("id", "collapse3")
				.attr("class", "panel-collapse collapse in col-md-11 col-md-offset-1")
				.append("div")
				.attr("class", "row")
				.selectAll("div")
				.data(model.get("subFilterProgram")["state"])
				.enter()
				.append("div")
				.attr("class", "col-md-12")
				.append("label")
				.each(function (d) {
					// create checkbox for each data
					d3.select(this).append("input")
						.attr("type", "checkbox")
						.attr("id", function (d) {
							return "chk_node_" + d;
						})
						.property("checked", function (d) {
							return _.contains(self.model.get("currentEntitiesFilters"), d);
						})
						.on("click", function (d, i) {
							self.updateCurrentEntitiesFilters(d, this.checked);
							// Reinitialize all the popover because some of them may have been recreated
							$('[data-toggle="popover"]').popover();
						})
					d3.select(this).append("span")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.entity.program-state." + d);
						})
				});
		},

		createRelationFilter: function (model) {
			var self = this;

			d3.select("#relationsGroupFilter").selectAll("div")
				.data(model.get("filterRelations"))
				.enter()
				.append("div")
				.attr("class", "checkbox-container")
				.append("label").each(function (d) {
					// create checkbox for each data
					d3.select(this).append("input")
						.attr("type", "checkbox")
						.attr("id", function (d) {
							return "chk_link_" + d;
						})
						.property("checked", function (d) {
							return _.contains(self.model.get("currentRelationsFilters"), d);
						})
						.on("click", function (d, i) {
							self.updateCurrentRelationsFilters(d, this.checked);
							// Reinitialize all the popover because some of them may have been recreated
							$('[data-toggle="popover"]').popover();
						})
					d3.select(this).append("span")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.relation." + d);
						});
					d3.select(this).append("svg")
						.attr("class", "filter-svg-arrow")
						.append("svg:defs").append("svg:marker")
						.attr("id", "markerArrow")
						.attr("viewBox", "0 -5 10 10")
						.attr("markerWidth", 4)
						.attr("markerHeight", 4)
						.attr("orient", "auto")
						.append("svg:path")
						.classed("reference-filter", function (f) {
							return f === "WRITING";
						})
						.attr("d", "M0,-5L10,0L0,5");
					d3.select(this).select("svg").append("svg:line")
						.attr("class", "filter-arrow")
						.classed("reference-filter", function (f) {
							return f === "WRITING";
						})
						.attr("x1", "0")
						.attr("y1", "5")
						.attr("x2", "10")
						.attr("y2", "5")
						.attr("marker-end", "url(#markerArrow)");
				});
		},

		updateCurrentEntitiesFilters: function (filter, checked) {
			if (checked) {
				this.model.get("currentEntitiesFilters").push(filter);
			} else {
				this.model.get("currentEntitiesFilters").splice(this.model.get("currentEntitiesFilters").indexOf(filter), 1);
			}
			this.model.trigger("change:currentEntitiesFilters");
		},
		
		updateCurrentRelationsFilters: function (filter, checked) {
			if (checked) {
				this.model.get("currentRelationsFilters").push(filter);
			} else {
				this.model.get("currentRelationsFilters").splice(this.model.get("currentRelationsFilters").indexOf(filter), 1);
			}
			// the graphe will just have to reset; same as change on entities so no need other event
			this.model.trigger("change:currentEntitiesFilters");
		},


		/**
		 * Render the side menu
		 */
		render: function () {

			// On se contente d'instancier le template du menu qui va placer les div pour les filtres, ensuite, ils seront peupler via les méthodes dans graph.js
			this.$el.html(this.tplDependancyContainer({
				dependancy: this.model
			}));

			d3.select("#relationsGroupFilter");
			this.createFilters(this.model);


		},

	});

	return DependencyMenuView;
});