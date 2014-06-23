define([
    "app",
    "models/place"
], function(App, Place) {

    var Places = {};
    // collection
    Places = Backbone.Collection.extend({
        model: Place,
        /**
         * Fetch the places from the server
         *
         * @constructor
         */
        initialize: function() {
            var self = this;
            // sort the places alphabetically
            this.comparator = function(place) {
                return place.getName();
            };
            // add the place w/ id -1 for the unlocated devices
            this.add({
                id: "-1",
                name: $.i18n.t("places-menu.unlocated-devices"),
                devices: []
            });
            // a place has been removed - put its devices as unlocated
            this.on("remove", function(place) {
                self.updateDevicesRemovedPlace(place);
            });
            // listen to the event when the list of places is received
            dispatcher.on("listPlaces", function(places) {
                _.each(places, function(place) {
                    self.add(place);
                });
                dispatcher.trigger("placesReady");
            });
            // listen to the event when a place appears and add it
            dispatcher.on("newPlace", function(place) {
                self.add(place);
            });
            // listen to the event when a place has been updated
            dispatcher.on("updatePlace", function(place) {
                places.get(place.id).set("name", place.name);
            });
            // listen to the event when a place has been removed
            dispatcher.on("removePlace", function(placeId) {
                var removedPlace = places.get(placeId);
                // check if the place exists in the collection
                if (typeof removedPlace !== "undefined") {

                    // remove the place from the collection
                    places.remove(removedPlace);
                    // update the devices of the place
                    self.updateDevicesRemovedPlace(removedPlace);
                    // refresh the content if the details of the removed place was displayed
                    if (Backbone.history.fragment === "places/" + placeId) {
                        appRouter.navigate("#places", {trigger: true});
                    }
                }
            });
            // listen to the event when a device has been moved
            dispatcher.on("moveDevice", function(messageData) {
                self.moveDevice(messageData.srcLocationId, messageData.destLocationId, messageData.deviceId, false);
            });
            // send the request to fetch the places
            communicator.sendMessage({
                method: "getPlaces",
                args: [],
                callId: "listPlaces",
                TARGET: "EHMI"
            });
        },
        /**
         * Return the name of the place where a device is located
         *
         * @param device
         * @return Name of the place where the device is located
         */
        getNameByDevice: function(device) {
            try {
                return this.get(device.get("placeId")).getName();
            } catch (e) {
                return "Non d&eacute;fini";
            }
        },
        /**
         * After removing a place from the collection, its devices need to be unlocated
         *
         * @param removedPlace Place that has been removed
         */
        updateDevicesRemovedPlace: function(removedPlace) {
            var self = this;
            // devices located in the place are now unlocated
            removedPlace.get("devices").forEach(function(deviceId) {
                // update their attributes
                var device = devices.get(deviceId);
                if (typeof device !== "undefined") {
                    device.set({placeId: -1});
                }

                // add it to the unlocated devices array of the collection
                self.get("-1").get("devices").push(deviceId);
            });
        },
        /**
         * Update the places and the device
         *
         * @param srcPlaceId
         * @param destPlaceId
         * @param deviceId
         * @param movedByUser
         */
        moveDevice: function(srcPlaceId, destPlaceId, deviceId, movedByUser) {
            console.log(srcPlaceId, destPlaceId, deviceId);
            var srcPlace = places.get(srcPlaceId);
            var destPlace = places.get(destPlaceId);
            // remove the device from the old place
            if (srcPlace !== undefined && srcPlace.get("devices").indexOf(deviceId) > -1) {
                srcPlace.get("devices").splice(srcPlace.get("devices").indexOf(deviceId), 1);
            }

            // add the device to the new place
            if (destPlace !== undefined && destPlace.get("devices").indexOf(deviceId) === -1) {
                destPlace.get("devices").push(deviceId);
            }

            // update the device itself
            devices.get(deviceId).set({"placeId": destPlaceId});
            // if the device has been moved by the user, send a notification to the backend
            if (movedByUser) {
                var messageJSON = {
                    method: "moveDevice",
                    args: [
                        {type: "String", value: deviceId},
                        {type: "String", value: srcPlaceId},
                        {type: "String", value: destPlaceId}
                    ],
                    TARGET: "EHMI"
                };
                // send the message
                communicator.sendMessage(messageJSON);
            }
        }
    });

    return Places;
});
