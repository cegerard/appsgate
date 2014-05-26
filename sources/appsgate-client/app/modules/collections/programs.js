define([
    "app",
    "models/program"
], function(App, Program) {

    var Programs = {};
    // collection
    Programs = Backbone.Collection.extend({
        model: Program,
        initialize: function() {
            var self = this;

            // sort the programs alphabetically
            this.comparator = function(place) {
                return place.get("name");
            };

            // listen to the event when the list of programs is received
            dispatcher.on("listPrograms", function(programs) {
                _.each(programs, function(program) {
                    self.add(program);
                });

                dispatcher.trigger("programsReady");
            });

            // listen to the event when a program appears and add it
            dispatcher.on("newProgram", function(program) {
                self.add(program);
            });

            // listen to the event when a program has been removed
            dispatcher.on("removeProgram", function(program) {
                var removedProgram = programs.get(program.id);
                programs.remove(removedProgram);
            });

            // listen to the event when a program has been updated
            dispatcher.on("updateProgram", function(program) {
                programs.get(program.id).set(program);
            });

            // send the request to fetch the programs
            communicator.sendMessage({
                method: "getPrograms",
                args: [],
                callId: "listPrograms"
            });
        },
        stopAllPrograms:function() {
          _.each(programs.models, function(program) {
            program.set("runningState", "DEPLOYED");
            program.remoteCall("stopProgram", [{type: "String", value: program.get("id")}]);
          });
        }
    });

    return Programs;

});
