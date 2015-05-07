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
            this.comparator = function(program) {
                return program.get("name").toUpperCase();
            };

            // listen to the event when the list of programs is received
            dispatcher.on("listPrograms", function(programs) {
                if (!app.isDebugMode()) {
                    _.each(programs, function(program) {
                        self.add(program);
                    });
                    dispatcher.trigger("programsReady");
                }
            });
            // listen to the event when the state of the programs is received
            dispatcher.on("programState", function(programs) {
                if (app.isDebugMode()) {
                    self.reset();
                    _.each(programs, function(program) {
                        self.add(program);
                    });
                }
            });

            dispatcher.on("programIds", function(programs) {
                if (app.isDebugMode()) {
                    _.each(programs, function(p) {
                        self.get(p.pid).setCurrentNode(p.node);
                    });
                    dispatcher.trigger("refreshDisplay");
                }
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
                var p = programs.get(program.id);
                if (p) {
                    p.set(program.source);
                } else {
                    self.add(program.source);
                }
            });

            this.getPrograms();
        },
        // send the request to fetch the programs
        getPrograms: function() {
            this.reset();
            communicator.sendMessage({
                method: "getPrograms",
                args: [],
                callId: "listPrograms",
                TARGET: "EHMI"
            });

        },
        getName: function(id) {
          if(this.get(id)) {
            return this.get(id).get("name");
          }
          return "Unknown";
        },
        allProgramsStopped:function() {
          var result = true;
          _.each(programs.models, function(program) {
            if(program.get("runningState") != "DEPLOYED" && program.get("runningState") != "INVALID") {
              result = false;
            }
          });
          return result;
        },
        stopAllPrograms:function() {
          _.each(programs.models, function(program) {
            program.remoteCall("stopProgram", [{type: "String", value: program.get("id")}]);
          });
        }
    });

    return Programs;

});
