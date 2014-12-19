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

			// listen to the event when the grpah is loaded
			dispatcher.on("loadGraph", function (graph) {

				// Create new dependancy object
				var newDependancy = new Dependancy();
				newDependancy.loadData(graph);

				// If not the first dependency, update the one already in the collection
				if (self.length !== 0) {
					self.updateDependency(self.at(0), newDependancy);
				}

				// Replace the existing dependancy if it already exists or just add it
				self.reset(newDependancy);

				dispatcher.trigger("dependanciesReady");
				dispatcher.trigger("router:loaded");
			});
		},

		refresh: function () {
			isRefresh = true;
		},

		/**
		* Method to update the dependency
		* @param oldDependency: Old model to update
		* @param newDependency: Model used to update the old one
		*/
		updateDependency: function (oldDependency, newDependency) {
			var oldEntities = oldDependency.get("entities");
			
			// Update each node : x/y and fixed attributes
			_.each(newDependency.get("entities"), function (e) {
				var oldE = _.find(oldEntities, function (o) {
					return o.id === e.id;
				});

				if (oldE !== undefined) {
					if (oldE.x && oldE.y) {
						e.x = oldE.x;
						e.y = oldE.y;
					}
					e.fixed = oldE.fixed;

				}
			});
			
			// Update rootNode
			if (oldDependency.get("rootNode") !== "") {
				var rootNodeNewDependency = _.find(newDependency.get("entities"), function (e) {
					return oldDependency.get("rootNode").id === e.id;
				});
				if (rootNodeNewDependency !== undefined) {
					newDependency.set({
						rootNode: rootNodeNewDependency
					});
				}
			}

			// Filters update
			newDependency.set({
				currentEntitiesTypes: oldDependency.get("currentEntitiesTypes")
			});
			newDependency.set({
				currentRelationsTypes: oldDependency.get("currentRelationsTypes")
			});
		}
	});

	return Dependancies;

});