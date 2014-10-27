define([
    "app",
    "models/dependancy"
], function (App, Dependancy) {

    var Dependancies = {};
    // collection
    Dependancies = Backbone.Collection.extend({
        model: Dependancy,
        initialize: function () {
            this.add(new Dependancy());
        },
    });

    return Dependancies;

});