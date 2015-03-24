define([
    "app",
    "models/brick",
], function(App, Brick) {

    var Place = {};

    /**
     * Place model class representing a place in AppsGate
     */
    Place = Brick.extend({
        /**
         * @constructor
         */
        initialize: function() {
            var self = this;

            // remove potential duplicated entries and trigger a refresh of the list of places event
            this.on("change:devices", function() {
                self.set({devices: _.uniq(self.get("devices"))});
            });
        },
        /**
         * As we cannot change the name of unlocated devices, this must change depending on which language we've chosen
         */
        getName: function() {
            if (this.get("id") == "-1") {
                return $.i18n.t("places-menu.unlocated-devices");
            }
            return this.get("name");
        },
        /**
         * Returns the number of devices the place contains
         */
        getDevicesNumber: function() {
            var devicesPlace = this.get("devices");
            
            // get all the devices that match the place
            var resultDevices = devices.filter(function(device) {
                return (devicesPlace.indexOf(device.get("id").toString()) !== -1);
            });
            
            if(this.get("id") == "-1") {
                // ignoring clock which is in unlocated devices
                return resultDevices.length - 1;
             }
          return resultDevices.length;
        },
        /**
         * Compute the average value of given sensors
         *
         * @param sensors Array of sensors
         * @return Average value of the sensors if any, undefined otherwise
         */
        getAverageValue: function(sensors,invalidReturn) {

            var total=this.getTotalValue(sensors,invalidReturn);

            if(isNaN(total)) return total;

            var valid=this.getCountCallback(sensors,function(a){ if(a!=invalidReturn) return true; else return false;}).length;

            return this.getFormatAverage(Math.round(total / valid));
        },
        getCountCallback: function(sensors,callb){
            var devs = [];
                sensors.forEach(function(s) {
                if (typeof s.get("value") !== "undefined") {
                    value=parseInt(s.get("value"));
                    if(callb(value)){
                        devs.push(s);
                    }
                } else {
                    value=parseInt(s.get("consumption"));
                    if(callb(value)){
                        devs.push(s);
                    }

                }
            });
            return devs;
        },
        /**
         * Compute the average value of given sensors
         *
         * @param sensors Array of sensors
         * @return Average value of the sensors if any, undefined otherwise
         */
        getTotalValue: function(sensors,invalidReturn) {

            var valid=this.getCountCallback(sensors,function(a){if(a!=invalidReturn) return true; else return false;});

            //If there are only invalid values (not yet fetched) OR there are no sensors return default display value
            if(valid.length==0){
                return "---";
            }

            var total = 0;

            valid.forEach(function(s) {
                if (typeof s.get("value") !== "undefined") {
                    value=parseFloat(s.get("value"));
                } else {
                    value=parseFloat(s.get("consumption"));
                }
                total += value;
            });


            return total;
        },
        getFormatAverage: function(value) {
            if (value == 300) {
                return $.i18n.t("devices.avg") + " <= " + value;
            } else {
                return $.i18n.t("devices.avg") + value;
            }
        },
        /**
         * Compute the average temperature of the place from the temperature sensors in the place
         *
         * @return Average temperature of the place if any temperature sensor, undefined otherwise
         */
        getAverageTemperature: function() {
            return this.getAverageValue(this.getTemperatureSensors(),999);
        },
		/**
		* Method to get the total of temperature value, without any format. 
		* @return the total of temperature value
		*/
		getTotalTemperatureValue: function() {
		 	return this.getTotalValue(this.getTemperatureSensors(), 9999);
		},
        /**
         * Compute the average co2 of the place from the co2 sensors in the place
         *
         * @return Average co2 of the place if any temperature sensor, undefined otherwise
         */
        getAverageCO2Sensor: function() {
            return this.getAverageValue(this.getCO2Sensors(),-1);
        },
		/**
		* Method to get the total of co2 value, without any format. 
		* @return the total of co2 value
		*/
		getTotalCO2Value: function() {
		 	return this.getTotalValue(this.getCO2Sensors(), -1);
		},
        /**
         * Compute the average illumination of the place from the illumination sensors in the place
         *
         * @return Average illumination of the place if any illumination sensor, undefined otherwise
         */
        getAverageIllumination: function() {
            return this.getAverageValue(this.getIlluminationSensors(),9999);
        },
		/**
		* Method to get the total of illumination value, without any format. 
		* @return the total of illumination value
		*/
		getTotalIlluminationValue: function() {
		 	return this.getTotalValue(this.getIlluminationSensors(), 9999);
		},
        /**
         * Compute the average consumption of the place from the plugs in the place
         *
         * @return Average consumption of the place if any consumption sensor, undefined otherwise
         */
        getAverageConsumption: function() {
            return this.getAverageValue(this.getPlugs(),-1);
        },
        /**
         * Compute the total consumption of the place from the plugs in the place
         *
         * @return total consumption of the place if any consumption sensor, undefined otherwise
         */
        getTotalConsumption: function() {
            return this.getTotalValue(this.getPlugs(),-1);
        },
        /**
         * Return all the devices of the place that matches a given type
         *
         * @param type Type of the devices to retrieve
         * @return Array of devices w/ good type
         */
        getTypeSensors: function(type) {
            intType = parseInt(type);

            // in case of wrong type, return an empty array
            if (isNaN(intType)) {
                return [];
            }

            // get all the devices that match the type
            var sensorsId = this.get("devices").filter(function(id) {
                return (devices.get(id) !== undefined && devices.get(id).get("type") === type);
            });

            // get all the devices that match the type and the place
            sensors = devices.filter(function(device) {
                return (sensorsId.indexOf(device.get("id").toString()) !== -1);
            });

            return sensors;
        },
        /**
         * Return all the services of the place that matches a given type
         *
         * @param type Type of the services to retrieve
         * @return Array of services w/ good type
         */
        getTypeServices: function(type) {
            var intType = parseInt(type);

            // in case of wrong type, return an empty array
            if (isNaN(intType)) {
                return [];
            }

            // get all the services that match the type
            var sensorsId = this.get("devices").filter(function(id) {
                return (services.get(id) !== undefined && services.get(id).get("type") === type);
            });

            // get all the services that match the type and the place
            var result = services.filter(function(service) {
                return (sensorsId.indexOf(service.get("id").toString()) !== -1);
            });

            return result;
        },
        /**
         * @return Array of temperature sensors in the place
         */
        getTemperatureSensors: function() {
            return this.getTypeSensors("0");
        },
        /**
         * @return Array of illumination sensors in the place
         */
        getIlluminationSensors: function() {
            return this.getTypeSensors("1");
        },
        /**
         * @return Array of switches in the place
         */
        getSwitches: function() {
            return this.getTypeSensors("2");
        },
        /**
         * @return Array of contact sensors in the place
         */
        getContactSensors: function() {
            return this.getTypeSensors("3");
        },
        /**
         * @return Array of key-card readers in the place
         */
        getKeyCardReaders: function() {
            return this.getTypeSensors("4");
        },
        /**
         * @return Array of ARD in the place
         */
        getARDLock: function() {
            return this.getTypeSensors("5");
        },
        getCO2Sensors: function() {
            return this.getTypeSensors("32");
        },
        /**
         * @returns Array of plugs in the place
         */
        getPlugs: function() {
            return this.getTypeSensors("6");
        },
        /**
         * @return Array of Philips Hue lamps in the place
         */
        getPhilipsHueLamps: function() {
            return this.getTypeSensors("7");
        },
        /**
         * @return Array of the actuators in the place
         */
        getActuators: function() {
            return this.getTypeSensors("8")
        },
        /**
         * @return Array of the DomiCubes in the place
         */
        getDomiCubes: function() {
            return this.getTypeSensors("210");
        },
        /**
         * @return Array of the MediaPlayers in the place
         */
        getMediaPlayers: function() {
            return this.getTypeSensors("31");
        },
        /**
         * @return Array of the TV in the place
         */
        getTVs: function() {
            return this.getTypeSensors("124");
        },
        /**
         * Send a message to the server to perform a remote call
         *
         * @param method Remote method name to call
         * @param args Array containing the argument taken by the method. Each entry of the array has to be { type : "", value "" }
         */
        remoteCall: function(method, args) {
            var placeManager = extendedServicesCollection.getPlaceManager();
            if (placeManager !== undefined) {
                placeManager.remoteControl(method, args, "placeManagerCall");
            }
        },
        /**
         * Override its synchronization method to send a notification on the network
         */
        sync: function(method, model) {
            switch (method) {
                case "create":
                    // create an id to the place
                    var id;
                    do {
                        id = "place-" + Math.round(Math.random() * 10000).toString();
                    } while (places.where({id: id}).length > 0);
                    model.set("id", id, {silent: true});

                    this.remoteCall("newPlace", [{type: "JSONObject", value: model.toJSON()}]);
                    model = null;
                    break;
                case "delete":
                    this.remoteCall("removePlace", [{type: "String", value: model.get("id")}]);
                    break;
                case "update":
                    this.remoteCall("updatePlace", [{type: "JSONObject", value: model.toJSON()}]);
                    break;
                default:
                    break;
            }
        },
        /**
         * Converts the model to its JSON representation.
         */
        toJSON: function() {
            return {
                id: this.get("id").toString(),
                name: this.getName(),
                devices: this.get("devices")
            };
        }
    });

    return Place;
});
