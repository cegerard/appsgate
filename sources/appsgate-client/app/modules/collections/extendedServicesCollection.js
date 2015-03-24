define([
    "app",
    "models/extended/extendedService"

], function(App, ExtendedService) {

    var ExtendedServicesCollection = {};

    // collection
    ExtendedServicesCollection = Backbone.Collection.extend({
        model: ExtendedService,
        /**
         * Fetch the extended services from the server
         *
         * @constructor
         */
        initialize: function() {
            var self = this;

            // listen to the event when the list of devices is received
            dispatcher.on("listDevices", function(devices) {
                _.each(devices, function(service) {
                    if (service && service.coreType === 'EXTENDED') {
                        self.addService(service);
                    }
                });
                dispatcher.trigger("ExtendedServicesCollectionReady");
            });

            dispatcher.on("newExtendedService", function(service) {
                if (service && service.coreType === 'EXTENDED') {
                    self.addService(service);
                }
            });

            dispatcher.on("removeExtendedService", function(service) {
              var extendedServiceModel = extendedServicesCollection.findWhere({id: service.objectId});
              self.removeService(extendedServiceModel);
            });
            dispatcher.trigger("extendedServicesCollectionWaiting");


        },


        addService: function(brick) {
            var self = this;
            var service = null;
            switch (brick.type) {
                case "PlaceManagerSpec":
                    console.log("found a Place Manager");
                    self.add(new ExtendedService(brick));
                    dispatcher.trigger("PlaceManagerReady");
                    break;

                default:
                    console.log("unknown type of EXTENDED SERVICE : ", brick.type, brick);
                    break;
            }
        },


        removeService: function(service) {

            extendedServicesCollection.remove(service);
        },


        getTypes: function() {

            var types=[];

            availableTypes=this.groupBy(function(service) {
                return service.get("type");
            });

            return availableTypes;
        },


        getExtendedByType: function(extType) {
            return extendedServicesCollection.findWhere({type: extType});
        },

        getPlaceManager: function() {
            return extendedServicesCollection.findWhere({type: "PlaceManagerSpec"});
        }

    });

    return ExtendedServicesCollection;

});
