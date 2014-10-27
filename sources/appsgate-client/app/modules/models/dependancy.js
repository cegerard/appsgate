define([
    "app",
    "models/brick",
], function (App, Brick) {

    var Dependancy = {};

    /**
     * Dependancies Model class extending the Backbone model class and an abstract class for all the bricks in the application (universes, places, devices, services, programs...)
     */
    Dependancy = Brick.extend({
        initialize: function () {
        },
        loadData: function(jsonData) {
            this.set({
                entities: jsonData.nodes;
            });
        }
    });

    // Return the reference to the Dependancies constructor
    return Dependancy;
});