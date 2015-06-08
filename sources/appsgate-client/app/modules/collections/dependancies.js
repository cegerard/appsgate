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

			// Variable to notify refresh of the graph. It will lead to the creation of a new dependency obj
			self.isRefresh = true;
			// Variable to notify updating state
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
				App.setWorldTimeStamp(graph.timestamp);
				if (!App.isDebugMode()) {
					return;
				}
				handleGraph(self, graph);
			});

		},

		/**
		 * Method to set the variable isRefresh at true and have a new dependency object for the next loadGraph event
		 */
		refresh: function () {
			this.isRefresh = true;
		},

		/**
		 * Method to update the dependency. It will call server method to have the last value of the graph.
		 * @param buildGraph : Boolean to know if the graph needs to be rebuilt or not. It has to be rebuild for some modifications (ie add/remove entity)
		 */
		updateDependency: function (buildGraph) {
			var self = this;

			/*
			* It is possible that more than one event updateGraph are fired in same moment. So if we receive an updateGraph when we were currently in updating, we add a callback to catch the end of the first updateGraphFinished, and we call another update. Don't forget to remove this listener in this case. Like that, at the end of process we have the last state of the graph and not a intermediate state.
			*/
			
			if (self.isUpdating) {
				self.listenTo(dispatcher, "UpdateGraphFinished", function () {
					// Send the request to the server to get the graph
					communicator.sendMessage({
						method: "getGraph",
						args: [{
							type: "Boolean",
							value: buildGraph
						}],
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
					args: [{
						type: "Boolean",
						value: buildGraph
					}],
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