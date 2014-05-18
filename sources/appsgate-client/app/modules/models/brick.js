define([
    "app"
], function(App) {

    var Brick = {};

    /**
     * Brick Model class extending the Backbone model class and an abstract class for all the bricks in the application (universes, places, devices, services, programs...)
     */
    Brick = Backbone.Model.extend({
        initialize: function() {
            var self = this;
            Brick.__super__.initialize.apply(this, arguments);
            dispatcher.on("newSpace-" + this.cid, function(id) {
                self.set("id", id);
            });

        },
        /**
         * Send a message to the server to perform a remote call
         * 
         * @param method Remote method name to call
         * @param args Array containing the argument taken by the method. Each entry of the array has to be { type : "", value "" }
         * @param callId Identifier of the message
         */
        remoteCall: function(method, args, callId) {
            // build the message
            var messageJSON = {
                method: method,
                args: args
            };

            if (typeof callId !== "undefined") {
                messageJSON.callId = callId;
            }

            // send the message
            communicator.sendMessage(messageJSON);
        },
        /**
         * return the list of available actions
         */
        getActions: function() {
            return [];
        },
        /**
         * return the keyboard code for a given action
        */
        getKeyboardForAction: function(act){
            console.error("No action has been defined for this brick.");
            return "";
        },
		/**
		 * method to build a button from a brick (device/service...)
		 */
		buildButtonFromBrick: function() {
		  console.error("This brick does not provide a button method");
		},
        /**
         * Override its synchronization method to send a notification on the network
         */
        sync: function(method, model) {
            // copy all useful data to attributes to keep them persistent
            switch (method) {
                case "create":
                    this.remoteCall("newSpace", [{type: "String", value: model.get("parent")}, {type: "String", value: model.get("type")}, {type: "JSONObject", value: model.toJSON()}], "newSpace-" + model.cid);
                    break;
                case "delete":
                    this.remoteCall("removeSpace", [{type: "String", value: model.get("id")}], "removeSpace-" + model.cid);
                    break;
                case "update":
                    this.remoteCall("updateSpace", [{type: "String", value: model.get("id")}, {type: "JSONObject", value: model.toJSON()}], "updateSpace-" + model.cid);
                    break;
                default:
                    break;
            }
        }
    });
    // Return the reference to the Brick constructor
    return Brick;
});
