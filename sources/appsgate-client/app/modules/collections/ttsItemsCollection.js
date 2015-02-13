/**
 * Created by thibaud on 13/02/15.
 */

define ([
        "app",
        "models/service/ttsItem"
    ], function(App, TTSItem) {
        var TTSItemsCollection = {}

    TTSItemsCollection = Backbone.Collection.extend({
            model: TTSItem,

            initialize: function() {
                console.log("ttsItems initialized");

            }
        });

    return TTSItemsCollection;
});
