define([
    "app",
    "models/device/device",
    "text!templates/program/nodes/defaultActionNode.html",
    "text!templates/program/nodes/defaultEventNode.html",
    "text!templates/program/nodes/defaultPropertyNode.html",
    "text!templates/program/nodes/defaultStateNode.html",
    "models/device/temperaturesensor",
    "models/device/co2sensor",
    "models/device/illuminationsensor",
    "models/device/switchsensor",
    "models/device/contactsensor",
    "models/device/keycardsensor",
    "models/device/ardlock",
    "models/device/plug",
    "models/device/phillipshue",
    "models/device/actuator",
    "models/device/domicube",
    "models/device/mediaplayer",
    "models/device/mediabrowser",
    "models/device/coretv",
    "models/device/coreclock",
    "models/device/fairylights",
    "models/device/mobileTasker"
], function(App, Device, ActionTemplate, EventTemplate, PropertyTemplate, StateTemplate, TemperatureSensor, CO2Sensor, IlluminationSensor, SwitchSensor,
            ContactSensor, KeyCardSensor, ARDLock, Plug, PhillipsHue, Actuator, DomiCube, MediaPlayer, MediaBrowser, CoreTV, CoreClock, FairyLights, MobileTasker) {

    var Devices = {};

    // collection
    Devices = Backbone.Collection.extend({
        model: Device,
        templates: {'action' : {}, 'action-parameter' : {}, 'event' : {},'event-parameter' : {}, 'state': {},'state-parameter': {}, 'property' : {},'property-parameter' : {}},
        /**
         * Fetch the devices from the server
         *
         * @constructor
         */
        initialize: function() {
            var self = this;

            this.comparator = function(device) {
                if (device.get("name")) {
                    return device.get("name").toUpperCase();
                }
                return "";
            };

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

            dispatcher.on("removeDevice", function(device) {
              var deviceModel = devices.findWhere({id: device.objectId});
              self.removeDevice(deviceModel);
            });

            dispatcher.trigger("devicesCollectionWaiting");


        },
        /**
         * Check the type of device sent by the server, cast it and add it to the collection
         *
         * @param device
         */
        addDevice: function(brick) {
            var self = this;
            var device = null;
            //brick.type = parseInt(brick.type);
            switch (brick.type) {
                case "0":
                    device = new TemperatureSensor(brick);
                    break;
                case "1":
                    device = new IlluminationSensor(brick);
                    break;
                case "2":
                    device = new SwitchSensor(brick);
                    break;
                case "3":
                    device = new ContactSensor(brick);
                    break;
                case "4":
                    device = new KeyCardSensor(brick);
                    break;
                case "5":
                    device = new ARDLock(brick);
                    break;
                case "6":
                    device = new Plug(brick);
                    break;
                case "7":
                    device = new PhillipsHue(brick);
                    break;
                case "8":
                    device = new Actuator(brick);
                    break;
                case "21":
                    device = new CoreClock(brick);
                    break;
                case "124":
                    device = new CoreTV(brick);
                    break;
                case "31":
                    device = new MediaPlayer(brick);
                    break;
                case "32":
                    device = new CO2Sensor(brick);
                    break;
                case "36":
                    device = new MediaBrowser(brick);
                    break;
                case "210":
                    device = new DomiCube(brick);
                    break;
                case "MobileDevice":
                    device = new MobileTasker(brick);
                    break;
                case "CoreFairyLightsSpec":
                    device = new FairyLights(brick);
                    break;
                default:
                    console.log("unknown type of DEVICE : ", brick.type, brick);
                    break;
            }
            if (device != null) {
                self.templates['action'][brick.type] = device.getTemplateAction();
                self.templates['action-parameter'][brick.type] = device.getActionTemplateParameter();
                self.templates['event'][brick.type] = device.getTemplateEvent();
                self.templates['event-parameter'][brick.type] = device.getEventTemplateParameter();
                self.templates['state'][brick.type] = device.getTemplateState();
                self.templates['state-parameter'][brick.type] = {};
                self.templates['property'][brick.type] = device.getTemplateProperty();
                self.templates['property-parameter'][brick.type] = {};

                if(typeof brick.placeId !== "undefined"){
                  places.get(brick.placeId).get("devices").push(brick.id);
                  places.get(brick.placeId).trigger('change');
                }
                else{
                  places.get("-1").get("devices").push(brick.id);
                  places.get("-1").trigger('change');
                }
                self.add(device);
            }
        },
        /**
         * Removes a given device
         * @param device
         */
        removeDevice: function(device) {
          if(typeof device.get("placeId") !== "undefined"){
            var index = places.get(device.get("placeId")).get("devices").indexOf(device.id);
            places.get(device.get("placeId")).get("devices").splice(index,1);
          }
          devices.remove(device);
        },
        /**
         * @return Array of the devices of a given type
         */
        getDevicesByType: function(type) {
            var array = this.groupBy(function(device) {
                    return device.get("type");
                });
            if (type === undefined) {
                return array;
            } else {
                if (array[type] != undefined) {
                    return array[type];
                }
                return Array();
                
            }
        },
        /**
         * Retrieves all devices of a given type (excludes the clock, mail and weather)
         * @param type
         * @returns {*|Array}
         */
        getDevicesFilterByType: function(type) {

            devAll=devices.where({type: type});

            devs=_.reject(devAll,function(device){ return !$.inArray(device.get("type"), ["21","102","103"]) });

            return devs;

        },
        /**
         * Get list of the device types sorted by its i18n key
         * @returns {Array|*}
         */
        getTypes: function() {

            var types=[];

            allavailabletypes=this.groupBy(function(device) {
                return device.get("type");
            });

            _.each(allavailabletypes,function(box){
                types[types.length]=box[0].get("type");
            });

            sortedTypes= _.sortBy(types,
                function(type){
                    var i18=this.getTypeLabelPrefix(type);
                    return $.i18n.t(i18+"singular").toLowerCase();
                },this);


            return sortedTypes;

        },
        /**
         * Retrieved the i18n key for the device name (without the the postfix plural / singular)
         * @param type
         * @returns String with the i18n key for the device type
         */
        getTypeLabelPrefix:function(type){
            var i18;
            if (type == "0") {
                i18="devices.temperature.name.";
            } else if (type == "1") {
                i18="devices.illumination.name.";
            } else if (type == "2") {
                i18="devices.switch.name.";
            } else if (type == "3") {
                i18="devices.contact.name.";
            } else if (type == "4") {
                i18="devices.cardswitch.name.";
            } else if (type == "5") {
                i18="devices.ard.name.";
            } else if (type == "6") {
                i18="devices.plug.name.";
            } else if (type == "7") {
                i18="devices.lamp.name.";
            } else if (type == "8") {
                i18="devices.actuator.name.";
            } else if (type == "CoreFairyLightsSpec") {
                i18="devices.fairylights.name.";
            } else if (type == "31") {
                i18="devices.mediaplayer.name.";
            } else if (type == "32") {
                i18="devices.co2.name.";
            }else if (type == "124") {
                i18="devices.tv.name.";
            } else if (type == "210") {
                i18="devices.domicube.name.";
            }
            return i18;
        },
        /**
         * @return Array of the temperature sensors
         */
        getTemperatureSensors: function() {
            return devices.where({type: "0"});
        },
        /**
         * @return Array of the illumination sensors
         */
        getIlluminationSensors: function() {
            return devices.where({type: "1"});
        },
        /**
         * @return Array of the switches
         */
        getSwitches: function() {
            return devices.where({type: "2"});
        },
        /**
         * @return Array of the switches
         */
        getCO2Sensors: function() {
            return devices.where({type: "32"});
        },
        /**
         * @return Array of the contact sensors
         */
        getContactSensors: function() {
            return devices.where({type: "3"});
        },
        /**
         * @return Array of the key-card readers
         */
        getKeyCardReaders: function() {
            return devices.where({type: "4"});
        },
        /**
         * @returns Array of the ARD Locks
         */
        getARDLocks: function() {
            return devices.where({type: "5"});
        },
        /**
         * @return Array of the plugs
         */
        getPlugs: function() {
            return devices.where({type: "6"});
        },
        /**
         * @return Array of the lamps
         */
        getLamps: function() {
            return devices.where({type: "7"});
        },
        /**
         * @return Array of the switch actuators
         */
        getActuators: function() {
            return devices.where({type: "8"});
        },
        /**
         * @return Array of the FairyLights devices
         */
        getFairyLights: function() {
            return devices.where({type: "CoreFairyLightsSpec"});
        },
        /**
         * @return Core clock of the home - unique device
         */
        getCoreClock: function() {
            return devices.findWhere({type: "21"});
        },
        /**
         * @return Array of UPnP media players
         */
        getMediaPlayers: function() {
            return devices.where({type: "31"});
        },
        /**
         * @return Array of UPnP media browsers
         */
        getMediaBrowsers: function() {
            return devices.where({type: "36"});
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
        getTemplateByType: function(word,type,param) {
            if (this.templates[word][type]) {
                return this.templates[word][type]($.extend(param,this.templates[word+'-parameter'][type]));
            } else {
                console.warn("No template is defined for type: " + type);
            }
            switch(word) {
                case 'action':
                    return _.template(ActionTemplate)(param);
                case'event':
                    return _.template(EventTemplate)(param);
                case'property':
                    return _.template(PropertyTemplate)(param);
                case'state':
                    return _.template(StateTemplate)(param);
                default:
                    console.error("unknown word: " + word+ ", for type: " + type);
                    console.log(param);
                    return "<span>unknown</span>";
            }
        },
        /**
         * @return Dictionnary of the devices sorted by their type - key is the type id, value - array of devices corresponding the type
         */
        getUnlocatedDevicesByType: function() {
            return _.groupBy(this.getUnlocatedDevices(), function(device) {
                return device.get("type");
            });
        },
        emergencyStop:function() {
          _.each(devices.models, function(device) {
            if($.inArray(device.get("type"), ["6","7","8"])){
                device.emergencyStop();
            }
          });
        }
    });

    return Devices;

});
