define([
    "app",
], function (App) {

    var Dependancy = {};

    /**
     * Dependancies Model class extending the Backbone model class and an abstract class for all the bricks in the application (universes, places, devices, services, programs...)
     */
    Dependancy = Backbone.Model.extend({
        initialize: function () {
            var self = this;
            Dependancy.__super__.initialize.apply(this, arguments);

            var arrayEntity = [{
                    "id": "1",
                    "type": "truc",
                    "name": "toto"
            },
                {
                    "id": "2",
                    "type": "truc",
                    "name": "toto"
            }];

            this.set({
                entities: arrayEntity
            });
        },
        test: function () {
            bidule();
            console.log("test");
        }
    });

    function bidule() {
        console.log("bidule");
    }

    // Return the reference to the Dependancies constructor
    return Dependancy;
});