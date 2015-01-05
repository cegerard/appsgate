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
      "hide.bs.modal #edit-device-modal": "toggleModalValue",
      "click #edit-device-modal button.valid-button": "validEditDevice",
      "keyup #edit-device-modal input": "validEditDevice",
      "change #edit-device-modal select": "checkDevice",
	  "click button.btn-target-dependencies": "onShowDependencies",
	  "click button.btn-target-timelines": "onShowTimelines"
    },
    initialize: function() {
      var self = this;
      DeviceDetailsView.__super__.initialize.apply(this, arguments);

      this.listenTo(this.model, "change", this.autoupdate);
      this.listenTo(this.model, "remove", this.onRemove);
    },
    /**
     * Return to the previous view
     */
    onBackButton: function() {
      window.history.back();
    },
    /**
     * Called when the model is removed from the collection
     */
    onRemove: function() {
      var type = this.model.get("type");
      appRouter.navigate("#devices/types/" + type, {trigger: true});
    },
    /**
     * Clear the input text, hide the error message and disable the valid button by default
     */
    initializeModal: function() {
      $("#edit-device-modal input#device-name").val(this.model.get(
        "name").replace(/&eacute;/g, "é").replace(/&egrave;/g,
        "è"));
      $("#edit-device-modal input#device-name").focus();
      $("#edit-device-modal .text-danger").addClass("hide");
      $("#edit-device-modal .valid-button").addClass("disabled");
      // tell the router that there is a modal
      appRouter.isModalShown = true;
    },
    toggleModalValue: function() {
      appRouter.isModalShown = false;
    },
    /**
     * Check the current value given by the user - show an error message if needed
     *
     * @return false if the information are not correct, true otherwise
     */
    checkDevice: function() {
      // name already exists
      if (devices.where({
          name: $("#edit-device-modal input").val()
        }).length > 0) {
        if (devices.where({
            name: $("#edit-device-modal input").val()
          })[0].get("id") !== this.model.get("id")) {
          $("#edit-device-modal .text-danger").removeClass("hide");
          $("#edit-device-modal .text-danger").text(
            "Nom déjà existant");
          $("#edit-device-modal .valid-button").addClass("disabled");

          return false;
        } else {
          $("#edit-device-modal .text-danger").addClass("hide");
          $("#edit-device-modal .valid-button").removeClass(
            "disabled");

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

      if (e.type === "keyup" && e.keyCode === 13 || e.type ===
        "click") {
        e.preventDefault();

        // update if information are ok
        if (this.checkDevice()) {
          var destPlaceId = $("#edit-device-modal select option:selected").val();

          var deviceName = $("#edit-device-modal input#device-name").val();

          $("#edit-device-modal").on("hidden.bs.modal",
            function() {
              // tell the router that there is no modal any more
              appRouter.isModalShown = false;

              // save the name now to prevent the reset of the modal after the render called in "moveDevice"
              self.model.set("name",deviceName);
              self.model.sendName();

              if(typeof destPlaceId != "undefined" && destPlaceId != ""){
                places.moveDevice(self.model.get("placeId"), destPlaceId, self.model.get("id"), true);
              }


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
      var deviceName = "&nbsp;&nbsp;"
      deviceName += this.model.get("name") !== "" ? this.model.get("name") : $.i18n.t("devices.device-no-name");
      this.$el.find("#device-name").html(deviceName);

      // update the status
      var deviceStatus = "";
      if (this.model.get("status") === "0") {
        deviceStatus =
          "<span class='label label-danger' data-i18n='devices.status.disconnected'></span>";
      } else if (this.model.get("status" === "1")) {
        deviceStatus =
          "<span class='label label-warning' data-i18n='devices.status.waiting'></span>";
      } else {
        deviceStatus =
          "<span class='label label-success' data-i18n='devices.status.connected'></span>";
      }
      this.$el.find("#device-status").html(deviceStatus);
    },
	  
    onShowDependencies: function() {
		appRouter.navigate("#dependancies/" + this.model.get("id"), {trigger: true});
	},
	onShowTimelines: function() {
		appRouter.navigate("#debugger/" + this.model.get("id"), {trigger: true});
	}
  });
  return DeviceDetailsView
});
