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

				// add the new graph to the collection only if it is empty (first time) or if we want to refresh to have the last data
				if (self.length === 0 || isRefresh) {

					// Create new dependancy object
					var newDependancy = new Dependancy();
					newDependancy.loadData(graph);

					// Replace the existing dependancy if it already exists or just add it
					self.reset(newDependancy);
					isRefresh = false;
				} else {
					console.log("Collection pas vide");
				}

				dispatcher.trigger("dependanciesReady");
				dispatcher.trigger("router:loaded");
			});
		},

		refresh: function () {
			isRefresh = true;
		}
	});

	return Dependancies;

});