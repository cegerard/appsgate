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

                // Replace the existing dependancy if it already exists or just add it
                self.reset(newDependancy);

                dispatcher.trigger("dependanciesReady");
                dispatcher.trigger("router:loaded");
            });
        },
    });

    return Dependancies;

});