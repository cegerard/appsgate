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
            "shown.bs.modal #edit-name-place-modal": "initializeModal",
            "click #edit-name-place-modal button.valid-button": "validEditName",
            "keyup #edit-name-place-modal input": "validEditName",
            "click button.toggle-plug-button": "onTogglePlugButton",
            "click button.blink-lamp-button": "onBlinkLampButton",
            "click button.toggle-lamp-button": "onToggleLampButton",
            "click button.delete-place-button": "deletePlace",
            "click button.delete-popover-button": "onClickDeletePlace",
            "click button.cancel-delete-place-button": "onCancelDeletePlace"
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
            this.listenTo(devices, "remove", this.render);

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
            $("#edit-name-place-modal input").val(this.model.getName());
            $("#edit-name-place-modal input").focus();
            $("#edit-name-place-modal .text-danger").addClass("hide");
            $("#edit-name-place-modal .valid-button").addClass("disabled");
            $("#edit-name-place-modal .valid-button").addClass("valid-disabled");
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
                $("#edit-name-place-modal .valid-button").addClass("valid-disabled");

                return false;
            }
            // name contains html code
            if (/(&|>|<)/.test($("#edit-name-place-modal input:text").val())) {
                $("#edit-name-place-modal .text-danger")
                        .text($.i18n.t("edit-name-modal.contains-html"))
                        .removeClass("hide");
                $("#edit-name-place-modal .valid-button").addClass("disabled");
                $("#edit-name-place-modal .valid-button").addClass("valid-disabled");

                return false;
            }

            // name already existing
            if (places.where({name: $("#edit-name-place-modal input").val()}).length > 0) {
                $("#edit-name-place-modal .text-danger").removeClass("hide");
                $("#edit-name-place-modal .text-danger").text($.i18n.t("modal-edit-place.place-already-existing"));
                $("#edit-name-place-modal .valid-button").addClass("disabled");
                $("#edit-name-place-modal .valid-button").addClass("valid-disabled");

                return false;
            }

            //ok
            $("#edit-name-place-modal .text-danger").addClass("hide");
            $("#edit-name-place-modal .valid-button").removeClass("disabled");
            $("#edit-name-place-modal .valid-button").removeClass("valid-disabled");
            
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
          * Callback when the user has clicked on the button to cancel the deleting or click out of the popover.
          */
        onCancelDeletePlace : function() {
            // destroy the popover
            this.$el.find("#delete-popover").popover('destroy');
        },
        /**
          * Callback when the user has clicked on the button delete.
          */
        onClickDeletePlace : function(e) {
            var self = this;
            // create the popover
            this.$el.find("#delete-popover").popover({
                html: true,
                title: $.i18n.t("places-details.warning-place-delete"),
                content: "<div class='popover-div'><button type='button' class='btn btn-default cancel-delete-place-button'>" + $.i18n.t("form.cancel-button") + "</button><button type='button' class='btn btn-danger delete-place-button'>" + $.i18n.t("form.delete-button") + "</button></div>",
                placement: "bottom"
            });
            // listen the hide event to destroy the popup, because it is created to every click on Edit
            this.$el.find("#delete-popover").on('hidden.bs.popover', function () {
                self.onCancelDeletePlace();
            });
            // show the popup
            this.$el.find("#delete-popover").popover('show');
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
            lamp.remoteControl("blink30", []);

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
                $("#edit-name-place-modal .place-name").val(this.model.getName());

                // hide the error message
                $("#edit-name-place-modal .text-error").hide();

                // translate the view
                this.$el.i18n();

                // resize the devices list in the selected place
                this.resize($(".scrollable"));

                return this;
            }
        }
    });
    return PlaceDetailsView;
});
