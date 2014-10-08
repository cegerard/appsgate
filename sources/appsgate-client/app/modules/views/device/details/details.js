define([
    "app",
    "text!templates/devices/details/deviceContainer.html"

], function(App, deviceDetailsTemplate) {

    var DeviceDetailsView = {};
    // detailled view of a device
    DeviceDetailsView = Backbone.View.extend({
        template: _.template(deviceDetailsTemplate),
        // map the events and their callback
        events: {
            "click button.back-button": "onBackButton",
            "shown.bs.modal #edit-device-modal": "initializeModal",
            "hidden.bs.modal #edit-device-modal": "toggleModalValue",
            "click #edit-device-modal button.valid-button": "validEditDevice",
            "keyup #edit-device-modal input": "validEditDevice",
            "change #edit-device-modal select": "checkDevice",
            //"click .clockReset": "clockReset",
            "change .clockReset": "checkDevice"
        },
        initialize: function() {
            var self = this;
            DeviceDetailsView.__super__.initialize.apply(this, arguments);

            this.listenTo(this.model, "change", this.autoupdate);
        },
        /**
         * Return to the previous view
         */
        onBackButton: function() {
            window.history.back();
        },
        /**
         * Clear the input text, hide the error message and disable the valid button by default
         */
        initializeModal: function() {
            $("#edit-device-modal input#device-name").val(this.model.get("name").replace(/&eacute;/g, "é").replace(/&egrave;/g, "è"));
            $("#edit-device-modal input#device-name").focus();
            $("#edit-device-modal .text-danger").addClass("hide");
            $("#edit-device-modal .valid-button").addClass("disabled");
            // tell the router that there is a modal
            appRouter.isModalShown = true;
        },
        /**
         * Tell the router there is no modal anymore
         */
        toggleModalValue: function() {
            _.defer(function() {
                appRouter.isModalShown = false;
                appRouter.currentView.render();
            });
        },
        /**
         * Check the current value given by the user - show an error message if needed
         *
         * @return false if the information are not correct, true otherwise
         */
        checkDevice: function() {
            // name already exists
            if (devices.where({name: $("#edit-device-modal input").val()}).length > 0) {
                if (devices.where({name: $("#edit-device-modal input").val()})[0].get("id") !== this.model.get("id")) {
                    $("#edit-device-modal .text-danger").removeClass("hide");
                    $("#edit-device-modal .text-danger").text("Nom déjà existant");
                    $("#edit-device-modal .valid-button").addClass("disabled");

                    return false;
                } else {
                    $("#edit-device-modal .text-danger").addClass("hide");
                    $("#edit-device-modal .valid-button").removeClass("disabled");

                    return true;
                }
            }

            // ok
            $("#edit-device-modal .text-danger").addClass("hide");
            $("#edit-device-modal .valid-button").removeClass("disabled");

            return true;
        },
        /**
         * Save the edits of the device
         */
        validEditDevice: function(e) {
            var self = this;

            if (e.type === "keyup" && e.keyCode === 13 || e.type === "click") {
                e.preventDefault();

                // update if information are ok
                if (this.checkDevice()) {
                    var destPlaceId;

                    if (this.model.get("type") !== "21" && this.model.get("type") !== 21) {
                        destPlaceId = $("#edit-device-modal select option:selected").val();
                    }

                    // save the name now to prevent the reset of the modal after the render called in "moveDevice"
                    var nameDevice = $("#edit-device-modal input#device-name").val();

                    this.$el.find("#edit-device-modal").on("hidden.bs.modal", function() {

                        // tell the router that there is no modal any more
                        appRouter.isModalShown = false;

                        // move the device if this is not the core clock
                        if (self.model.get("type") !== "21" && self.model.get("type") !== 21) {
                            places.moveDevice(self.model.get("placeId"), destPlaceId, self.model.get("id"), true);
                        } else { // update the time and the flow rate set by the user
                            // update the moment attribute
                            self.model.get("moment").set("hour", parseInt($("#edit-device-modal select#hour").val()));
                            self.model.get("moment").set("minute", parseInt($("#edit-device-modal select#minute").val()));
                            // retrieve the value of the flow rate set by the user
                            var timeFlowRate = $("#edit-device-modal input#time-flow-rate").val();

                            // update the attributes hour and minute
                            self.model.set("hour", self.model.get("moment").hour());
                            self.model.set("minute", self.model.get("moment").minute());

                            // send the update to the server
                            self.model.save();

                            // update the attribute time flow rate
                            self.model.set("flowRate", timeFlowRate);

                            if(self.model.get("reset")===true){
                                self.model.trigger("change:resetClock");
                                self.model.set("flowRate", "1");
                            }

                            //send the update to the server
                            self.model.save();
                        }

                        // set the new name to the device
                        self.model.set("name", nameDevice);

                        // send the updates to the server
                        self.model.save();

                        // rerender the view
                        self.render();

                        return false;
                    });

                    // hide the modal
                    $("#edit-device-modal").modal("hide");
                }
            } else if (e.type === "keyup") {
                this.checkDevice();
            }
        },
        /**
         * Updates the display of the common device values
         */
        autoupdate: function() {
          // update the name
          this.$el.find("#device-name").html(this.model.get("name") !== ""?this.model.get("name"):"<em data-i18n='devices.device-no-name'></em>");

          // update the status
          var deviceStatus = "";
          if (this.model.get("status") === "0") {
            deviceStatus = "<span class='label label-danger' data-i18n='devices.status.disconnected'></span>";
          } else if (this.model.get("status" === "1")) {
            deviceStatus = "<span class='label label-warning' data-i18n='devices.status.waiting'></span>";
          } else {
            deviceStatus = "<span class='label label-success' data-i18n='devices.status.connected'></span>";
          }
          this.$el.find("#device-status").html(deviceStatus);
        },
    });
    return DeviceDetailsView
});
