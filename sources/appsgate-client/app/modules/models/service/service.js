define([
  "app",
  "models/brick",
  "text!templates/program/nodes/defaultActionNode.html",
  ], function(App, Brick, ActionTemplate) {

    var Service = {};

    /**
    * Abstract class regrouping common characteristics shared by all the devices
    *
    * @class Device.Model
    */
    Service = Brick.extend({
      /**
      * @constructor
      */
      initialize: function() {
        Service.__super__.initialize.apply(this, arguments);

        var self = this;

        // each device listens to the event whose id corresponds to its own id. This ensures to
        // receive only relevant events
        dispatcher.on(this.get("id"), function(updatedVariableJSON) {
            if(typeof updatedVariableJSON.value !== "undefined") {
                if (typeof updatedVariableJSON.varName === "undefined" && updatedVariableJSON.value.indexOf("DIDL-Lite") !== -1) {
                    dispatcher.trigger("mediaBrowserResults", updatedVariableJSON.value);
                }
                else {
                    self.set(updatedVariableJSON.varName, updatedVariableJSON.value);
                }
            }
        });
      },
      /**
      * Send the name of the device to the server
      */
      sendName: function() {
        // build the message
        var messageJSON = {
          method: "setUserObjectName",
          args: [
          {type: "String", value: this.get("id")},
          {type: "String", value: ""},
          {type: "String", value: this.get("name")}
          ]
        };

        // send the message
        communicator.sendMessage(messageJSON);
      },

      /**
      * default method to build a button with its name as the name of the button
      */
      buildButtonFromBrick: function() {
        return "<button id='" + this.get("id") + "' class='btn btn-default btn-keyboard service-node'><span>" + this.get("name") + "<span></button>"
      },
      getJSONAction: function (type) {
        return {"type": "action", "target": {"iid": "X", "type": 'mandatory', "serviceType":this.get("type")}, "args": [], "iid": "X"};
      },

      getJSONEvent: function (type) {
        return {"type": "event",  "source": {"iid": "X", "type": 'mandatory', "serviceType":this.get("type")}, "iid": "X"};
      },
      getJSONState: function (type) {
        return {"type": "state", "object": {"iid": "X", "type": 'mandatory', "serviceType":this.get("type")}, "iid": "X"};
      },
      getJSONProperty: function (type) {
        return {"type": "property", "iid": "X", "target": {"iid": "X", "type": 'mandatory', "serviceType":this.get("type")}, "args":[]};
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
          args: args
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
    return Service;
  });
