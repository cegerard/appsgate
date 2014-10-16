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
                    self.listenTo(device, "change", self.autoupdate);
                }
            });
        },
        /**
         * Method called when a device has changed
         * @param device Model that changed, Device in that case
         * @param collection Collection that holds the changed model
         * @param options Options given with the change event
         */
        autoupdate: function(device, options) {
            var type = device.get("type");
            var place = places.get(device.get("placeId"));

            this.$el.find("#device-" + device.cid + "-name").html(device.get("name") !== "" ?device.get("name") : $.i18n.t(places-details.body.device-no-name));

            switch (type) {
              case 0:
                $("#device-" + device.cid + "-value").text(device.getValue() + " &deg;C");
                break;
              case 1:
                $("#device-" + device.cid + "-value").attr("data-i18n", "devices.illumination.scale." + device.get("label"));
                break;
              case 2:
                if (device.get("buttonStatus") === "true") {
                  $("#device-" + device.cid + "-value").attr("class","label label-yellow");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.switch.value.opened");
                } else {
                  $("#device-" + device.cid + "-value").attr("class","label label-default");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.switch.value.closed");
                }
                break;
              case 3:
                if (device.get("contact") !== "true") {
                  $("#device-" + device.cid + "-value").attr("class","label label-yellow");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.contact.value.opened");
                } else {
                  $("#device-" + device.cid + "-value").attr("class","label label-default");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.contact.value.closed");
                }
                break;
              case 4:
                if (device.get("inserted") === "true") {
                  $("#device-" + device.cid + "-value").attr("class","label label-yellow");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.cardswitch.value.inserted");
                } else {
                  $("#device-" + device.cid + "-value").attr("class","label label-default");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.cardswitch.value.not-inserted");
                }
                break;
              case 6:
                if (device.get("plugState") === "true" || device.get("plugState") === true) {
                  $("#device-" + device.cid + "-button").attr("data-i18n", "devices.plug.action.turnOff");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.plug.status.turnedOn");
                  $("#device-" + device.cid + "-value").attr("class","label label-yellow");
                } else {
                  $("#device-" + device.cid + "-button").attr("data-i18n", "devices.plug.action.turnOn");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.plug.status.turnedOff");
                  $("#device-" + device.cid + "-value").attr("class","label label-default");
                }
                $("#device-" + device.cid + "-consumption").text(device.getValue() + " W");
                break;
              case 7:
                if (device.get("value") === "true" || device.get("value") === true) {
                  $("#device-" + device.cid + "-button").attr("data-i18n", "devices.lamp.action.turnOff");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.lamp.status.turnedOn");
                  $("#device-" + device.cid + "-value").attr("class", "label label-yellow");
                } else {
                  $("#device-" + device.cid + "-button").attr("data-i18n", "devices.lamp.action.turnOn");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.lamp.status.turnedOff");
                  $("#device-" + device.cid + "-value").attr("class", "label label-default");
                }
                $("#device-" + device.cid + "-color").attr("style", "background-color:" + device.getCurrentColor());
                break;
              case 8:
                if (device.get("value") === "true" || device.get("value") === true) {
                  $("#device-" + device.cid + "-button").attr("data-i18n", "devices.actuator.action.turnOff");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.actuator.status.turnedOn");
                  $("#device-" + device.cid + "-value").attr("class","label label-yellow");
                } else {
                  $("#device-" + device.cid + "-button").attr("data-i18n", "devices.actuator.action.turnOn");
                  $("#device-" + device.cid + "-value").attr("data-i18n", "devices.actuator.status.turnedOff");
                  $("#device-" + device.cid + "-value").attr("class","label label-default");
                }
                break;
              case 210:
                var activeFace = "";
                switch (device.get("activeFace")) {
                  case "1":
                    activeFace = "<img id='device-" + device.cid + "-value' src='/app/img/domicube-work.svg' width='18px' class='img-responsive'>";
                    break;
                  case "2":
                    activeFace = "<svg id='device-" + device.cid + "-value' class='white-face-svg-domus img-responsive'>" +
                      "<rect class='white-face-rect-domus' x='10' y='10' rx='25' ry='25' width='95%' height='90%'/>" +
                      "<text class='white-face-text-domus' x='50%' y='47%'>" + $.i18n.t('devices.domicube.white-face.first-elem') +
                      "</text><text class='white-face-text-domus' x='50%' y='54%'>" + $.i18n.t('devices.domicube.white-face.second-elem') + "</text></svg>";
                    break;
                  case "3":
                    activeFace = "<img id='device-" + device.cid + "-value' src='/app/img/domicube-music.png' width='18px' class='img-responsive'>";
                    break;
                  case "4":
                    activeFace = "<img id='device-" + device.cid + "-value' src='/app/img/domicube-question.svg' width='18px' class='img-responsive'>";
                    break;
                  case "5":
                    activeFace = "<img id='device-" + device.cid + "-value' src='/app/img/domicube-night.png' width='18px' class='img-responsive'>";
                    break;
                  case "6":
                    activeFace = "<img id='device-" + device.cid + "-value' src='/app/img/domicube-meal.png' width='18px' class='img-responsive'>";
                    break;
                  default:
                    //TODO
                    break;
                }
                this.$el.find("#device-" + device.cid + "-value").replaceWith(activeFace);
                break;
              }
              if (device.get("status") === "0") {
                $("#device-" + device.cid + "-status").attr("class","label label-danger");
                $("#device-" + device.cid + "-status").attr("data-i18n", "devices.status.disconnected");
              } else if (device.get("status" === "1")) {
                $("#device-" + device.cid + "-status").attr("class","label label-warning");
                $("#device-" + device.cid + "-status").attr("data-i18n", "devices.status.waiting");
              } else {
                $("#device-" + device.cid + "-status").attr("class","label label-success");
                $("#device-" + device.cid + "-status").attr("data-i18n", "devices.status.connected");
              }
              // translate the view
              this.$el.i18n();
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

            var plug = devices.get($(e.currentTarget).attr("device-id"));
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

            var lamp = devices.get($(e.currentTarget).attr("device-id"));
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

            var lamp = devices.get($(e.currentTarget).attr("device-id"));
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
