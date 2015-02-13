/**
 * Created by thibaud on 13/02/15.
 */

define([
    "app"
], function(App) {

    var ttsItem = {};

    ttsItem = Backbone.Model.extend({
        idAttribute: "book_id",

        initialize: function() {
            console.log("new TTS Item, id : ",this.id);
            console.log("book_id : ",this.get("book_id"));
            console.log("text : ",this.get("text"));
            console.log("voice : ",this.get("voice"));
            console.log("speed : ",this.get("speed"));

    }

    });
    return ttsItem;
});

