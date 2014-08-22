define([
    "app",
    "models/brick"

], function(App, Brick) {

    var Device = {};

    /**
     * Abstract class regrouping common characteristics shared by all the devices
     *
     * @class Device.Model
     */
    Device = Brick.extend({
        /**
         * @constructor
         */
        initialize: function() {
            var self = this;

            // each device listens to the event whose id corresponds to its own id. This ensures to
            // receive only relevant events
            dispatcher.on(this.get("id"), function(updatedVariableJSON) {
                self.set(updatedVariableJSON.varName, updatedVariableJSON.value);
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
                ],
                TARGET: "EHMI"
            };

            // send the message
            communicator.sendMessage(messageJSON);
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
         * default method to build a button with its name as the name of the button
         */
        buildButtonFromDevice: function() {
            return "<button id='" + this.get("id") + "' class='btn btn-default btn-keyboard device-node'><span>" + this.get("name") + "<span></button>"
        },

        getJSONAction: function (type) {
            return {"type": "action", "target": {"iid": "X", "type": 'mandatory', "deviceType":this.get("type")}, "args": [], "iid": "X"};
        },

        getJSONEvent: function (type) {
            return {"type": "event",  "source": {"iid": "X", "type": 'mandatory', "deviceType":this.get("type")}, "iid": "X"};
        },
        getJSONState: function (type) {
            return {"type": "state", "object": {"iid": "X", "type": 'mandatory', "deviceType":this.get("type")}, "iid": "X"};
        },
        getJSONProperty: function (type) {
            return {"type": "property", "iid": "X", "target": {"iid": "X", "type": 'mandatory', "deviceType":this.get("type")}, "args":[]};
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
                            } else if (attribute === "plugState") {
                                model.sendPlugState();
                            } else if (attribute === "value" && (model.get("type") === "7" || model.get("type") === 7)) {
                                model.sendValue();
                            } else if (attribute === "value" && (model.get("type") === "8" || model.get("type") === 8)) {
                                model.sendValue();
                            } else if (attribute === "color" && (model.get("type") === "7" || model.get("type") === 7)) {
                                model.sendColor();
                            } else if (attribute === "saturation" && (model.get("type") === "7" || model.get("type") === 7)) {
                                model.sendSaturation();
                            } else if (attribute === "brightness" && (model.get("type") === "7" || model.get("type") === 7)) {
                                model.sendBrightness();
                            } else if ((attribute === "hour" || attribute === "minute") && (model.get("type") === "21" || model.get("type") === 21)) {
                                model.sendTimeInMillis();
                            } else if (attribute === "flowRate" && (model.get("type") === "21" || model.get("type") === 21)) {
                                model.sendTimeFlowRate();
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        },
        emergencyStop:function(method,model) {
          this.remoteControl("off", []);
        }

    });
    return Device;
});
