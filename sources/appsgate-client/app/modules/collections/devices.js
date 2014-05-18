define([
    "app",
    "models/device/device",
    "text!templates/program/nodes/defaultActionNode.html",    
    "models/device/temperaturesensor",
    "models/device/illuminationsensor",
    "models/device/switchsensor",
    "models/device/contactsensor",
    "models/device/keycardsensor",
    "models/device/ardlock",
    "models/device/plug",
    "models/device/phillipshue",
    "models/device/actuator",
    "models/device/domicube",
    "models/device/coreclock"
], function(App, Device, ActionTemplate, TemperatureSensor, IlluminationSensor, SwitchSensor, ContactSensor, KeyCardSensor, ARDLock, Plug, PhillipsHue, Actuator, DomiCube, CoreClock) {

    var Devices = {};

    // collection
    Devices = Backbone.Collection.extend({
        model: Device,
        templates: {},
        /**
         * Fetch the devices from the server
         *
         * @constructor
         */
        initialize: function() {
            var self = this;

            // listen to the event when the list of devices is received
            dispatcher.on("listDevices", function(devices) {
                _.each(devices, function(device) {
                    if (device) {
                        self.addDevice(device);
                    }
                });
                dispatcher.trigger("devicesReady");
            });

            // listen to the backend notifying when a device appears and add it
            dispatcher.on("newDevice", function(device) {
                self.addDevice(device);
            });

            dispatcher.on("removeDevice", function(deviceId) {
                var device = devices.findWhere({id: deviceId});
                devices.remove(device);
            });

            // send the request to fetch the devices
            communicator.sendMessage({
                method: "getDevices",
                args: [],
                callId: "listDevices"
            });
        },
        /**
         * Check the type of device sent by the server, cast it and add it to the collection
         *
         * @param device
         */
        addDevice: function(brick) {
            var self = this;
            var device = null;
            brick.type = parseInt(brick.type);
            switch (brick.type) {
                case 0:
                    device = new TemperatureSensor(brick);
                    break;
                case 1:
                    device = new IlluminationSensor(brick);
                    break;
                case 2:
                    device = new SwitchSensor(brick);
                    break;
                case 3:
                    device = new ContactSensor(brick);
                    break;
                case 4:
                    device = new KeyCardSensor(brick);
                    break;
                case 5:
                    device = new ARDLock(brick);
                    break;
                case 6:
                    device = new Plug(brick);
                    break;
                case 7:
                    device = new PhillipsHue(brick);
                    break;
                case 8:
                    device = new Actuator(brick);
                    break;
                case 21:
                    device = new CoreClock(brick);
                    break;
                case 210:
                    device = new DomiCube(brick);
                    break;
                default:
                    //console.log("unknown type", brick.type, brick);
                    break;
            }
            if (device != null) {
                self.templates[brick.type] = device.getTemplateAction();
                self.add(device);
                //code
            }
            places.get(brick.placeId).get("devices").push(brick.id);
        },
        /**
         * @return Array of the devices of a given type
         */
        getDevicesByType: function() {
            return this.groupBy(function(device) {
                return device.get("type");
            });
        },
        /**
         * @return Array of the temperature sensors
         */
        getTemperatureSensors: function() {
            return devices.where({type: 0});
        },
        /**
         * @return Array of the illumination sensors
         */
        getIlluminationSensors: function() {
            return devices.where({type: 1});
        },
        /**
         * @return Array of the switches
         */
        getSwitches: function() {
            return devices.where({type: 2});
        },
        /**
         * @return Array of the contact sensors
         */
        getContactSensors: function() {
            return devices.where({type: 3});
        },
        /**
         * @return Array of the key-card readers
         */
        getKeyCardReaders: function() {
            return devices.where({type: 4});
        },
        /**
         * @returns Array of the ARD Locks
         */
        getARDLocks: function() {
            return devices.where({type: 5});
        },
        /**
         * @return Array of the plugs
         */
        getPlugs: function() {
            return devices.where({type: 6});
        },
        /**
         * @return Array of the lamps
         */
        getLamps: function() {
            return devices.where({type: 7});
        },
        /**
         * @return Array of the switch actuators
         */
        getActuators: function() {
            return devices.where({type: 8});
        },
        /**
         * @return Core clock of the home - unique device
         */
        getCoreClock: function() {
            return devices.findWhere({type: 21});
        },
        /**
         * @return Array of the unlocated devices
         */
        getUnlocatedDevices: function() {
            return this.filter(function(device) {
                return device.get("placeId") === "-1";
            });
        },
        /**
         * @returns the template corresponding to the device
         */ 
        getTemplateActionByType: function(type,param) {
            if (this.templates[type]) {
                return this.templates[type](param);
            } else {
                console.warn("No template is defined for type: " + type);
            }
            return _.template(ActionTemplate)(param);
        },
        /**
         * @return Dictionnary of the devices sorted by their type - key is the type id, value - array of devices corresponding the type
         */
        getUnlocatedDevicesByType: function() {
            return _.groupBy(this.getUnlocatedDevices(), function(device) {
                return device.get("type");
            });
        }
    });

    return Devices;

});
