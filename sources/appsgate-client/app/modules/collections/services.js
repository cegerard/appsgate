define([
    "app",
    "models/service/service",
    "text!templates/program/nodes/defaultActionNode.html",
    "text!templates/program/nodes/defaultEventNode.html",
    "text!templates/program/nodes/defaultPropertyNode.html",
    "text!templates/program/nodes/defaultStateNode.html",
    "models/service/mail",
    "models/service/weather",
    "models/service/tts"
], function(App, Service, ActionTemplate, EventTemplate, PropertyTemplate, StateTemplate, Mail, Weather, TTS) {

    var Services = {};

    // collection
    Services = Backbone.Collection.extend({
        model: Service,
        templates: {'action' : {}, 'event' : {}, 'state': {}, 'property' : {}},
        /**
         * Fetch the devices from the server
         *
         * @constructor
         */
        initialize: function() {
            var self = this;

            // listen to the event when the list of devices is received
            dispatcher.on("listDevices", function(devices) {
                _.each(devices, function(service) {
                    if (service) {
                        self.addService(service);
                    }
                });
                dispatcher.trigger("servicesReady");
            });
            // listen to the backend notifying when a device appears and add it
            dispatcher.on("newService", function(service) {
                self.addService(service);
            });

            dispatcher.on("removeService", function(serviceId) {

                var service = self.findWhere({id: serviceId.objectId});
                self.remove(service);

            });
            dispatcher.trigger("servicesCollectionWaiting");
        },
        /**
         * Check the type of device sent by the server, cast it and add it to the collection
         *
         * @param device
         */
        addService: function(brick) {
            var self = this;
            var service = null;
            switch (brick.type) {
                case "102":
                    service = new Mail(brick);
                    break;
                case "103":
                    service = new Weather(brick);
                    break;
                case "104":
                    service = new TTS(brick);
                    break;
                default:
                    console.log("unknown type of SERVICE ", brick.type, brick);
                    break;
            }
            if (service != null) {
                self.add(service);
                self.templates['action'][brick.type] = service.getTemplateAction();
                self.templates['event'][brick.type] = service.getTemplateEvent();
                self.templates['state'][brick.type] = service.getTemplateState();
                self.templates['property'][brick.type] = service.getTemplateProperty();
            }
        },
        /**
         * @return Array of the devices of a given type
         */
        getServicesByType: function() {
            return this.groupBy(function(service) {
                return service.get("type");
            });
        },
        /**
         * @return Core mail of the home - unique device
         */
        getCoreMail: function() {
            return services.findWhere({type: "102"});
        },
        /**
         * @return Core weather of the home - unique device
         */
        getCoreWeather: function() {
            return services.findWhere({type: "103"});
        },
        /**
         * @return Core TTS of the home - unique Service
         */
        getCoreTTS: function() {
            return services.findWhere({type: "104"});
        },
        /**
         * @returns the template corresponding to the device
         */
        getTemplateByType: function(word,type,param) {
            if (this.templates[word][type]) {
                return this.templates[word][type](param);
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
                    console.debug(param);
                    return "<span>unknown</span>";
            }
        }
    });

    return Services;

});
