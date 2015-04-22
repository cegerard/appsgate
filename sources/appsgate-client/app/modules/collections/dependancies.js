define([
    "app",
    "models/dependancy"
], function (App, Dependancy) {

	var Dependancies = {};
	// collection
	Dependancies = Backbone.Collection.extend({
		model: Dependancy,
		initialize: function () {
			var self = this;

			self.isRefresh = true;
			self.isUpdating = false;

			// listen to the event when the graph is loaded
			dispatcher.on("loadGraph", function (graph) {
				if (App.isDebugMode()) {
					console.log("debug mode");
					return;
				}
				handleGraph(self, graph);
			});
			dispatcher.on("loadGraphAt", function (graph) {
				if (!App.isDebugMode()) {
					console.log("normal mode");
					return;
				}
				handleGraph(self, graph);
			});

//			self.differencies = (function () {
//				this.relations = [];
//				this.entities = [];
//
//				this.mergeRelations = function (newRelations) {
//					_.forEach(newRelations, function (newR) {
//						var isUpdated = false;
//						_.forEach(relations, function (oldR) {
//							if (newR.target.id === oldR.target.id && newR.source.id === oldR.source.id) {
//								oldR = newR;
//								isUpdated = true;
//							}
//						});
//						if (!isUpdated) {
//							relations.push(newR);
//						}
//					});
//				};
//
//				this.mergeEntities = function (newEntities) {
//					_.forEach(newEntities, function (newE) {
//						var entityAlreadyWaiting = _.find(entities, function (e) {
//							return e.id === newE.id;
//						});
//						if (entityAlreadyWaiting) {
//							entityAlreadyWaiting = newE;
//						} else {
//							entities.push(newE);
//						}
//					});
//				};
//
//				return {
//					pushRelations: function (newRelations) {
//						mergeRelations(newRelations);
//					},
//					pushEntities: function (newEntities) {
//						mergeEntities(newEntities);
//					},
//					pushDifferencies: function (newDifferencies) {
//						mergeEntities(newDifferencies.get("entities"));
//						mergeRelations(newDifferencies.get("relations"));
//					},
//					getRelationsWaiting: function () {
//						return relations;
//					},
//					getEntitiesWaiting: function () {
//						return entities;
//					}
//				};
//			})();
		},

		/**
		 * Method to set the variable isRefresh at true and have a new dependency object for the next loadGraph event
		 */
		refresh: function () {
			this.isRefresh = true;
		},

		updateDependency: function (buildGraph) {
			var self = this;

			if (self.isUpdating) {
				self.listenTo(dispatcher, "UpdateGraphFinished", function () {
					// Send the request to the server to get the graph
					communicator.sendMessage({
						method: "getGraph",
						args: [{type:"Boolean", value: buildGraph}],
						callId: "loadGraph",
						TARGET: "EHMI"
					});
					self.isUpdating = true;
					self.stopListening(dispatcher, "UpdateGraphFinished");
				});
			} else {

				// Send the request to the server to get the graph
				communicator.sendMessage({
					method: "getGraph",
					args: [{type:"Boolean", value: buildGraph}],
					callId: "loadGraph",
					TARGET: "EHMI"
				});
				self.isUpdating = true;
			}


		}
	});
	function handleGraph(self, graph) {
				if (!self.isRefresh) {

					var currentDependency = self.at(0);
					currentDependency.updateModel(graph);
					console.log("Dependence 0 apres modif %o", currentDependency);
					//					self.isUpdate = false;
					dispatcher.trigger("UpdateGraphFinished");
					//
				} else {
					// Create new dependancy object
					var newDependancy = new Dependancy();
					newDependancy.loadData(graph);

					if (self.length > 0) {
						// If there was already a model, detachEvent to be sure to have no event attached on this old model
						self.at(0).detachEvents();
					}

					// Replace the existing dependancy if it already exists or just add it
					self.reset(newDependancy);
					self.isRefresh = false;
					dispatcher.trigger("dependanciesReady");
					dispatcher.trigger("router:loaded");
				}

	}
	return Dependancies;

});