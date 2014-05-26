define([
    "app",
    "text!templates/places/details/details.html"
], function(App, placeDetailsTemplate) {

    var PlaceDetailsView = {};

    /**
     * Detailled view of a place
     */
    PlaceDetailsView = Backbone.View.extend({
        tpl: _.template(placeDetailsTemplate),
        /**
         * Bind events of the DOM elements from the view to their callback
         */
        events: {
            "show.bs.modal #edit-name-place-modal": "initializeModal",
            "click #edit-name-place-modal button.valid-button": "validEditName",
            "keyup #edit-name-place-modal input": "validEditName",
            "click button.delete-place-button": "deletePlace",
            "click button.toggle-plug-button": "onTogglePlugButton",
            "click button.blink-lamp-button": "onBlinkLampButton",
            "click button.toggle-lamp-button": "onToggleLampButton"
        },
        /**
         * Listen to the model update and refresh if any
         * 
         * @constructor
         */
        initialize: function() {
            var self = this;

            // listen to update on its model...
            this.listenTo(this.model, "change", this.render);

            // ... and on all its devices
            this.model.get("devices").forEach(function(deviceId) {
                var device = devices.get(deviceId);

                // if the device has been found in the collection
                if (typeof device !== "undefined") {
                    self.listenTo(devices.get(deviceId), "change", self.onChangedDevice);
                }
            });
        },
        /**
         * Method called when a device has changed
         * @param model Model that changed, Device in that cas
         * @param collection Collection that holds the changed model
         * @param options Options given with the change event
         */
        onChangedDevice: function(model, options) {
            // a device has changed
            // if it's the clock, we refresh the clock only
            if (typeof options === "undefined" || (typeof options !== "undefined") && !options.clockRefresh) {
                this.render();
            }
        },
        /**
         * Clear the input text, hide the error message and disable the valid button by default
         */
        initializeModal: function() {
            $("#edit-name-place-modal input").val(this.model.get("name"));
            $("#edit-name-place-modal .text-danger").addClass("hide");
            $("#edit-name-place-modal .valid-button").addClass("disabled");
        },
        /**
         * Check the current value of the input text and show a message error if needed
         * 
         * @return false if the typed name already exists, true otherwise
         */
        checkPlace: function() {
            // name is empty
            if ($("#edit-name-place-modal input").val() === "") {
                $("#edit-name-place-modal .text-danger").removeClass("hide");
                $("#edit-name-place-modal .text-danger").text($.i18n.t("modal-edit-place.place-name-empty"));
                $("#edit-name-place-modal .valid-button").addClass("disabled");

                return false;
            }

            // name already existing
            if (places.where({name: $("#edit-name-place-modal input").val()}).length > 0) {
                $("#edit-name-place-modal .text-danger").removeClass("hide");
                $("#edit-name-place-modal .text-danger").text($.i18n.t("modal-edit-place.place-already-existing"));
                $("#edit-name-place-modal .valid-button").addClass("disabled");

                return false;
            }

            //ok
            $("#edit-name-place-modal .text-danger").addClass("hide");
            $("#edit-name-place-modal .valid-button").removeClass("disabled");

            return true;
        },
        /**
         * Check if the name of the place does not already exist. If not, update the place
         * Hide the modal when done
         */
        validEditName: function(e) {
            var self = this;

            if (e.type === "keyup" && e.keyCode === 13 || e.type === "click") {
                e.preventDefault();

                // update the name if it is ok
                if (this.checkPlace()) {
                    this.$el.find("#edit-name-place-modal").on("hidden.bs.modal", function() {
                        // set the new name to the place
                        self.model.set("name", $("#edit-name-place-modal input").val());

                        // send the update to the backend
                        self.model.save();

                        return false;
                    });

                    // hide the modal
                    $("#edit-name-place-modal").modal("hide");
                }
            } else if (e.type === "keyup") {
                this.checkPlace();
            }
        },
        /**
         * Callback when the user has clicked on the button to remove a place. Remove the place
         */
        deletePlace: function() {
            // delete the place
            this.model.destroy();

            // navigate to the list of places
            appRouter.navigate("#places", {trigger: true});
        },
        /**
         * Callback to toggle a plug
         * 
         * @param e JS mouse event
         */
        onTogglePlugButton: function(e) {
            e.preventDefault();

            var plug = devices.get($(e.currentTarget).attr("id"));
            // value can be string or boolean
            // string
            if (typeof plug.get("plugState") === "string") {
                if (plug.get("plugState") === "true") {
                    plug.set("plugState", "false");
                } else {
                    plug.set("plugState", "true");
                }
                // boolean
            } else {
                if (plug.get("plugState")) {
                    plug.set("plugState", "false");
                } else {
                    plug.set("plugState", "true");
                }
            }

            // send the message to the backend
            plug.save();

            return false;
        },
        /**
         * Callback to toggle a lamp
         * 
         * @param e JS mouse event
         */
        onToggleLampButton: function(e) {
            e.preventDefault();

            var lamp = devices.get($(e.currentTarget).attr("id"));
            // value can be string or boolean
            // string
            if (typeof lamp.get("value") === "string") {
                if (lamp.get("value") === "true") {
                    lamp.set("value", "false");
                } else {
                    lamp.set("value", "true");
                }
                // boolean
            } else {
                if (lamp.get("value")) {
                    lamp.set("value", "false");
                } else {
                    lamp.set("value", "true");
                }
            }

            // send the message to the backend
            lamp.save();

            return false;
        },
        /**
         * Callback to blink a lamp
         *
         * @param e JS mouse event
         */
        onBlinkLampButton: function(e) {
            e.preventDefault();

            var lamp = devices.get($(e.currentTarget).attr("id"));
            // send the message to the backend
            lamp.remoteCall("blink30", []);

            return false;
        },
        /**
         * Render the view
         */
        render: function() {
            if (!appRouter.isModalShown) {
                // render the view itself
                this.$el.html(this.tpl({
                    place: this.model,
                }));

                // put the name of the place by default in the modal to edit
                $("#edit-name-place-modal .place-name").val(this.model.get("name"));

                // hide the error message
                $("#edit-name-place-modal .text-error").hide();

                // initialize the popover
                this.$el.find("#delete-popover").popover({
                    html: true,
                    content: "<button type='button' class='btn btn-danger delete-place-button'>" + $.i18n.t("form.delete-button") + "</button>",
                    placement: "bottom"
                });

                // translate the view
                this.$el.i18n();

                // resize the devices list in the selected place
                this.resizeDiv($(".contents-list"));

                return this;
            }
        }
    });
    return PlaceDetailsView;
});
