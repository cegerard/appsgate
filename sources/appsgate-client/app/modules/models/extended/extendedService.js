define([
  "app",
  "models/brick"
], function(App, Brick) {

  var ExtendedService = {};


  ExtendedService = Brick.extend({
    /**
     * @constructor
     */
    initialize: function() {
      ExtendedService.__super__.initialize.apply(this, arguments);

      var self = this;
    },
    /**
     * Send a message to the server to perform a remote call
     *
     * @param method Remote method name to call
     * @param args Array containing the argument taken by the method. Each entry of the array has to be { type : "", value "" }
     */
    remoteControl: function(method, args, callId) {
      // build the message
      var messageJSON = {
        targetType: "1",
        objectId: this.get("id"),
        method: method,
        args: args,
        TARGET: "EHMI"
      };

      if (typeof callId !== "undefined") {
        messageJSON.callId = callId;
      }

      // send the message
      communicator.sendMessage(messageJSON);
    },
    /**
     * Override its synchronization method to send a notification on the network
     */
    sync: function(method, model) {
      if (model.changedAttributes()) {
        switch (method) {
          case "update":
            _.keys(model.changedAttributes()).forEach(function(attribute) {
              if (attribute === "name") {
                model.sendName();
              }
            });
            break;
          default:
            break;
        }
      }
    }
  });
  return ExtendedService;
});
