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
			var self = this;
			this.render();

			// Listen event changement dans filtres entities pour mettre à jour les checkboxs
			this.listenTo(this.model, "change:currentEntitiesFilters", function () {

				// On selectionne tous les inputs du menu
				d3.selectAll("input").property("checked", function (d) {
					// Comportement différents pour checkbox all/none
					if (d3.select(this).attr("id") === "chk_node_all") {
						// All est checked si tous les filtres possibles sur les deviceTypes sont dans le current
						var checked = true;
						var subFilterDeviceType = self.model.get("subFilterDevice")["deviceType"];
						_.each(subFilterDeviceType, function (filterDeviceType) {
							if (!_.contains(self.model.get("currentEntitiesFilters"), filterDeviceType) && checked) {
								checked = false;
							}
						});
						return checked;
					} else if (d3.select(this).attr("id") === "chk_node_none") {
						// None est checked si aucun des filtres possibles sur les deviceTypes n'est dans le current
						var checked = true;
						var subFilterDeviceType = self.model.get("subFilterDevice")["deviceType"];
						_.each(subFilterDeviceType, function (filterDeviceType) {
							if (_.contains(self.model.get("currentEntitiesFilters"), filterDeviceType) && checked) {
								checked = false;
							}
						});
						return checked;
					} else {
						// Pour tous les autres, checked s'ils sont dans les current
						return _.contains(self.model.get("currentEntitiesFilters"), d) || _.contains(self.model.get("currentRelationsFilters"), d);
					}
				})
			});
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

		/*
		 * Fonction pour créer les différents filtres du menu
		 * @param model : modèle des dépendances
		 */
		createFilters: function (model) {
			var self = this;
			self.createEntityFilter(model);
			self.createRelationFilter(model);
		},

		/*
		 * Fonction pour créer et ajouter les filtres sur les entités
		 * @param model : modèle des dépendances
		 */
		createEntityFilter: function (model) {
			var self = this;

			// On va d'abord créer les filtres du premier niveau des entités : time, place, service, device et programs
			d3.select("#entitiesGroupFilter")
				.append("div")
				.attr("class", "row")
				.selectAll("div")
				// Les filtres entités possibles sont dans : "filterEntities"
				.data(model.get("filterEntities"))
				.enter()
				.append("div")
				.attr("class", "col-md-11 col-md-offset-1")
				.attr("id", function (f) {
					return "div-filter-" + f;
				})
				.append("label")
				.each(function (d) {
					// Ajouter le checkbox
					d3.select(this).append("input")
						.attr("type", "checkbox")
						.attr("id", function (d) {
							return "chk_node_" + d;
						})
						// Le checkbox de device ne doit pas apparaitre, on le disable en plus pour pas pouvoir cliquer dessus par hasard
						.style("visibility", function (d) {
							if (d === "device") {
								return "hidden"
							} else {
								return "visible";
							}
						})
						.attr("disabled", function (d) {
							if (d === "device") {
								return "disabled";
							} else {
								return null;
							}
						})
						.property("checked", function (d) {
							return _.contains(self.model.get("currentEntitiesFilters"), d);
						})
						.on("click", function (d, i) {
							self.updateCurrentEntitiesFilters(d, this.checked);
							// Reinitialize all the popover because some of them may have been recreated
							$('[data-toggle="popover"]').popover();
						})

					// Ajouter le dessin correspondant à l'entité
					self.addCaptionDrawing(this, d);

					d3.select(this).append("span")
						.attr("class", "caption-label-drawing")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.entity." + d);
						});
				});

			// Sous filtre sur les types device. On ajoute ceux ci au div créé précédement sur les devices
			d3.select("#div-filter-device")
				.append("div")
				.attr("class", "col-md-12")
				// Un div est créé pour le titre du sous-filtre 'type'. Il possède un lien pour pouvoir collapse le div qui sera créé ensuite pour les sous-filtres en question
				.append("div")
				.attr("class", "subfilter-title")
				.append("label")
				.append("a")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#collapse-deviceType")
				.text(function () {
					return $.i18n.t("dependancy.filters-label.entity.device-type.title");
				});

			// On créer le div pour le sous filtre. On sauvegarde la sélection pour pouvoir ajouter différement les sous-élements
			var panelDeviceType = d3.select("#div-filter-device")
				.append("div")
				.attr("id", "collapse-deviceType")
				.attr("class", "panel-collapse collapse in col-md-11 col-md-offset-1")
				.append("div")
				.attr("class", "row")

			// Création du div pour la checkbox All
			var checkAllDiv = panelDeviceType.append("div")
				.attr("class", "col-md-12")
				.append("label")

			checkAllDiv.append("input")
				.attr("type", "checkbox")
				.attr("id", function (d) {
					return "chk_node_all";
				})
				.property("checked", function () {
					var checked = true;
					var subFilterDeviceType = self.model.get("subFilterDevice")["deviceType"];
					_.each(subFilterDeviceType, function (filterDeviceType) {
						if (!_.contains(self.model.get("currentEntitiesFilters"), filterDeviceType)) {
							checked = false;
						}
					});
					return checked;
				})
				.on("click", function (d, i) {
					// Mise à jour des filtres seulement au changement no checked -> checked
					if (this.checked) {
						self.model.checkAllDeviceType();
					}
					// Reinitialize all the popover because some of them may have been recreated
					$('[data-toggle="popover"]').popover();
				});

			checkAllDiv.append("span")
				.attr("class", "caption-subfilter-label")
				.text(function (d) {
					return $.i18n.t("dependancy.filters-label.entity.device-type.all");
				})

			// Création du div pour la checkbox None
			var uncheckAllDiv = panelDeviceType.append("div")
				.attr("class", "col-md-12")
				// Un style est ajouté pour avoir des pointillés sous le div..
				.style({
					"border-style": "dashed",
					"border-bottom-width": "1px"
				})
				.append("label")

			uncheckAllDiv.append("input")
				.attr("type", "checkbox")
				.attr("id", function (d) {
					return "chk_node_none";
				})
				.property("checked", function () {
					var checked = true;
					var subFilterDeviceType = self.model.get("subFilterDevice")["deviceType"];
					_.each(subFilterDeviceType, function (filterDeviceType) {
						if (_.contains(self.model.get("currentEntitiesFilters"), filterDeviceType)) {
							checked = false;
						}
					});
					return checked;
				})
				.on("click", function (d, i) {
					// Mise à jour des filtres seulement au changement no checked -> checked
					if (this.checked) {
						self.model.uncheckAllDeviceType();
					}
					// Reinitialize all the popover because some of them may have been recreated
					$('[data-toggle="popover"]').popover();
				});

			uncheckAllDiv.append("span")
				.attr("class", "caption-subfilter-label")
				.text(function (d) {
					return $.i18n.t("dependancy.filters-label.entity.device-type.none");
				})

			// Ajout des div et checkbox pour tous les deviceType possibles connus dans le modèle
			panelDeviceType.selectAll("div-deviceType")
				.data(model.get("subFilterDevice")["deviceType"])
				.enter()
				.append("div")
				.attr("class", "col-md-12 div-deviceType")
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
						.attr("class", "caption-subfilter-label")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.entity.device-type." + d);
						})
				});

			// On refait la même chose mais pour les status cette fois. Pas de cas particulier pour all/none 
			d3.select("#div-filter-device")
				.append("div")
				.attr("class", "col-md-12")
				.append("div")
				.attr("class", "subfilter-title")
				.append("label")
				.append("a")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#collapse-deviceState")
				.text(function () {
					return $.i18n.t("dependancy.filters-label.entity.device-state.title");
				});

			d3.select("#div-filter-device")
				.append("div")
				.attr("id", "collapse-deviceState")
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

					// Ajouter le dessin correspondant à l'entité
					self.addCaptionDrawing(this, d);

					d3.select(this).append("span")
						.attr("class", "caption-label-drawing caption-subfilter-label")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.entity.device-state." + d);
						})
				});


			// Création des sous filtres pour les status des programmes
			d3.select("#div-filter-program")
				.append("div")
				.attr("class", "col-md-12")
				.append("div")
				.attr("class", "subfilter-title")
				.append("label")
				.append("a")
				.attr("data-toggle", "collapse")
				.attr("data-target", "#collapse-programState")
				.text(function () {
					return $.i18n.t("dependancy.filters-label.entity.program-state.title");
				});

			d3.select("#div-filter-program")
				.append("div")
				.attr("id", "collapse-programState")
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

					// Ajouter le dessin correspondant à l'entité
					self.addCaptionDrawing(this, d);

					d3.select(this).append("span")
						.attr("class", "caption-label-drawing caption-subfilter-label")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.entity.program-state." + d);
						})
				});
		},

		/*
		 * Fonction pour faire le dessin en fonction du type de l'entité
		 * @param d3Element : élément d3 de l'entité pour laquelle on veut un dessin. C'est dans cet élément que l'on va ajouter le svg et dessin qui va bien
		 * @param entity : entité (ou relation) pour laquelle on veut un dessin.
		 */
		addCaptionDrawing: function (d3Element, entity) {
			// On dessine dans un svg ajouté avant le texte
			var drawElem = d3.select(d3Element)
				.append("svg")
				.attr("class", "caption-drawing")
				.attr("width", 20)
				.attr("height", 20)
				.append("svg:g");

			switch (entity) {
			case "true":
				drawElem.append("circle")
					.attr("cx", 10)
					.attr("cy", 10)
					.attr("r", 7)
					.attr("stroke", "black")
					.attr("class", "circle-device-state-true");
				break;
			case "false":
				drawElem.append("circle")
					.attr("cx", 10)
					.attr("cy", 10)
					.attr("r", 7)
					.attr("stroke", "black")
					.attr("class", "circle-device-state-false");
				break;
			case "isGhostDevice":
			case "isGhostProgram":
				drawElem.append("circle")
					.attr("cx", 10)
					.attr("cy", 10)
					.attr("r", 9)
					.attr("fill", "white")
					.attr("stroke-width", 2)
					.attr("stroke", "blue");
				drawElem.append("line")
					.attr("x1", 3)
					.attr("y1", 3)
					.attr("x2", 17)
					.attr("y2", 17)
					.attr("class", "ghost-decoration");
				drawElem.append("line")
					.attr("x1", 3)
					.attr("y1", 17)
					.attr("x2", 17)
					.attr("y2", 3)
					.attr("class", "ghost-decoration");
				break;
			case "isMultipleTargeted":
				drawElem.append("circle")
					.attr("cx", 10)
					.attr("cy", 10)
					.attr("r", 9)
					.attr("fill", "white")
					.attr("stroke-width", 2)
					.attr("stroke", "red");
				break;
			case "INVALID":
				drawElem.append("rect")
					.attr("x", 4)
					.attr("y", 4)
					.attr("width", 12)
					.attr("height", 12)
					.attr("fill", "red")
					.attr("stroke", "black");
				break;
			case "DEPLOYED":
				drawElem.append("rect")
					.attr("x", 4)
					.attr("y", 4)
					.attr("width", 12)
					.attr("height", 12)
					.attr("fill", "#26d43b")
					.attr("stroke", "black");
				break;
			case "INCOMPLETE":
				drawElem.append("rect")
					.attr("x", 4)
					.attr("y", 4)
					.attr("width", 12)
					.attr("height", 12)
					.attr("fill", "orange")
					.attr("stroke", "black");
				break;
			case "PROCESSING":
				drawElem.append("path")
					.attr("d", "M5,5L20,13L5,20L5,5")
					.attr("fill", "#26d43b")
					.attr("stroke", "black");
				break;
			case "LIMPING":
				drawElem.append("path")
					.attr("d", "M5,5L20,13L5,20L5,5")
					.attr("fill", "orange")
					.attr("stroke", "black");
				break;
			case "time":
				drawElem.append("circle")
					.attr("cx", 10)
					.attr("cy", 10)
					.attr("r", 9)
					.attr("fill", "transparent")
					.attr("stroke", "black");
				drawElem.append("image")
					.attr("x", 2)
					.attr("y", 2)
					.attr('width', 16)
					.attr('height', 16)
					.attr("xlink:href", "app/img/home/calendar.svg");
				break;
			case "device":
				drawElem.append("circle")
					.attr("cx", 10)
					.attr("cy", 10)
					.attr("r", 9)
					.attr("fill", "transparent")
					.attr("stroke", "black");
				drawElem.append("image")
					.attr("x", 2)
					.attr("y", 2)
					.attr('width', 16)
					.attr('height', 16)
					.attr("xlink:href", "app/img/home/device3.svg");
				break;
			case "place":
				drawElem.append("circle")
					.attr("cx", 10)
					.attr("cy", 10)
					.attr("r", 9)
					.attr("fill", "transparent")
					.attr("stroke", "black");
				drawElem.append("image")
					.attr("x", 2)
					.attr("y", 2)
					.attr('width', 16)
					.attr('height', 16)
					.attr("xlink:href", "app/img/home/place1.svg");
				break;
			case "service":
				drawElem.append("circle")
					.attr("cx", 10)
					.attr("cy", 10)
					.attr("r", 9)
					.attr("fill", "transparent")
					.attr("stroke", "black");
				drawElem.append("image")
					.attr("x", 2)
					.attr("y", 2)
					.attr('width', 16)
					.attr('height', 16)
					.attr("xlink:href", "app/img/home/service1.svg");
				break;
			case "program":
				drawElem.append("circle")
					.attr("cx", 10)
					.attr("cy", 10)
					.attr("r", 9)
					.attr("fill", "transparent")
					.attr("stroke", "black");
				drawElem.append("image")
					.attr("x", 2)
					.attr("y", 2)
					.attr('width', 16)
					.attr('height', 16)
					.attr("xlink:href", "app/img/home/program2.svg");
				break;
			case "selector":
				drawElem.append("circle")
					.attr("cx", 10)
					.attr("cy", 10)
					.attr("r", 9)
					.attr("fill", "transparent")
					.attr("stroke", "black");
				drawElem.append("image")
					.attr("x", 2)
					.attr("y", 2)
					.attr('width', 16)
					.attr('height', 16)
					.attr("xlink:href", "app/img/home/devices-selector.svg");
				break;
			default:
				break;
			}


		},

		/*
		 * Fonction pour créer les filtres sur les relations
		 * @param model : le model de dépendances
		 */
		createRelationFilter: function (model) {
			var self = this;

			// Création des div/checkbox pour les filtres relations
			d3.select("#relationsGroupFilter").selectAll("div")
				// Relations possibles dans filterRelations
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

					// On dessine directement les flèches ici. La couleur sera gérée via la classe reference-filter, pour la ligne et le bout de la flèche
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

					d3.select(this).append("span")
						.text(function (d) {
							return $.i18n.t("dependancy.filters-label.relation." + d);
						});
				});
		},

		/**
		 * Fonction pour mettre à jour le tableau des filtres entités courants
		 * @param filter : le filtre à mettre à jour
		 * @param checked : état du checkbox pour savoir s'il faut ajouter ou enlever le filtre
		 */
		updateCurrentEntitiesFilters: function (filter, checked) {
			if (checked) {
				this.model.get("currentEntitiesFilters").push(filter);
			} else {
				this.model.get("currentEntitiesFilters").splice(this.model.get("currentEntitiesFilters").indexOf(filter), 1);
			}
			this.model.trigger("change:currentEntitiesFilters");
		},

		/**
		 * Fonction pour mettre à jour le tableau des filtres relations courants
		 * @param filter : le filtre à mettre à jour
		 * @param checked : état du checkbox pour savoir s'il faut ajouter ou enlever le filtre
		 */
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
			var self = this;

			// On se contente d'instancier le template du menu qui va placer les div pour les filtres, ensuite, ils seront peupler via les méthodes dans graph.js
			this.$el.html(this.tplDependancyContainer({
				dependancy: this.model
			}));

			this.createFilters(this.model);
		},

	});

	return DependencyMenuView;
});