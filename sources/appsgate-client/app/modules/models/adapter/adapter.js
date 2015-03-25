define([
  "app",
  "models/brick"
], function(App, Brick) {

  var Adapter = {};


  Adapter = Brick.extend({
    /**
     * @constructor
     */
    initialize: function() {
      Adapter.__super__.initialize.apply(this, arguments);

      var self = this;

      dispatcher.on(this.get("id"), function(updatedVariableJSON) {
        if (typeof updatedVariableJSON.value !== "undefined") {
          if (typeof updatedVariableJSON.varName === "undefined" && updatedVariableJSON.value.indexOf("DIDL-Lite") !== -1) {
            dispatcher.trigger("mediaBrowserResults", updatedVariableJSON.value);
          } else {
            self.set(updatedVariableJSON.varName, updatedVariableJSON.value);
          }
        }
      });
    },
    /**
     * Send the name of the device to the server
     */
    sendName: function() {

      var devicePropertiesManager = extendedServicesCollection.getDevicePropertiesManager();
      if (devicePropertiesManager !== undefined) {
        devicePropertiesManager.remoteControl( "addName", [{
              type: "String",
              value: this.get("id")
            }, {
              type: "String",
              value: ""
            }, {
              type: "String",
              value: this.get("name")
            }],
            "devicePropertiesManagerCall");
      }
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
  return Adapter;
});
