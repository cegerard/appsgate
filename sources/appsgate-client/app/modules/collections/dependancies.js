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

            // listen to the event when the list of programs is received
            dispatcher.on("graph", function (graph) {
                
                var newDependancy = new Dependancy();
                    newDependancy.loadData(graph);
                    self.add(newDependancy);

                dispatcher.trigger("dependanciesReady");
            });

            // send the request to fetch the programs
            communicator.sendMessage({
                method: "getGraph",
                args: [],
                callId: "graph",
                TARGET: "EHMI"
            });
        },
    });

    return Dependancies;

});