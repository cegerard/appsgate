define([
  "app",
  "text!templates/services/details/serviceContainer.html",
  "text!templates/services/details/mail.html",
  "text!templates/services/details/weather.html"
  ], function(App, serviceDetailsTemplate, mediaPlayerDetailTemplate, mailDetailTemplate, weatherDetailTemplate) {

    var ServiceDetailsView = {};
    // detailled view of a service
    ServiceDetailsView = Backbone.View.extend({
      template: _.template(serviceDetailsTemplate),
      tplMail: _.template(mailDetailTemplate),
      tplWeather: _.template(weatherDetailTemplate),
      // map the events and their callback
      events: {
        "click button.back-button": "onBackButton",
        "shown.bs.modal #edit-service-modal": "initializeModal",
        "hide.bs.modal #edit-service-modal": "toggleModalValue",
        "click #edit-service-modal button.valid-button": "validEditService",
        "keyup #edit-service-modal input": "validEditService",
        "change #edit-service-modal select": "checkService"
      },
      /**
      * Listen to the service update and refresh if any
      *
      * @constructor
      */
      initialize: function() {
        this.listenTo(this.model, "change", this.render);
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
        $("#edit-service-modal input#service-name").val(this.model.get("name").replace(/&eacute;/g, "é").replace(/&egrave;/g, "è"));
        $("#edit-service-modal input#service-name").focus();
        $("#edit-service-modal .text-danger").addClass("hide");
        $("#edit-service-modal .valid-button").addClass("disabled");
        $("#edit-service-modal .valid-button").addClass("valid-disabled");

        // initialize the field to edit the core clock if needed
        if (this.model.get("type") === "21" || this.model.get("type") === 21) {
          $("#edit-service-modal select#hour").val(this.model.get("moment").hour());
          $("#edit-service-modal select#minute").val(this.model.get("moment").minute());
          $("#edit-service-modal input#time-flow-rate").val(this.model.get("flowRate"));
        }

        // tell the router that there is a modal
        appRouter.isModalShown = true;
      },
      /**
      * Tell the router there is no modal anymore
      */
      toggleModalValue: function() {
        appRouter.isModalShown = false;
      },
      /**
      * Check the current value given by the user - show an error message if needed
      *
      * @return false if the information are not correct, true otherwise
      */
      checkService: function() {
        // name already exists
        if (services.where({name: $("#edit-service-modal input").val()}).length > 0) {
          if (services.where({name: $("#edit-service-modal input").val()})[0].get("id") !== this.model.get("id")) {
            $("#edit-service-modal .text-danger").removeClass("hide");
            $("#edit-service-modal .text-danger").text($.i18n.t("modal-edit-service.name-already-existing"));
            $("#edit-service-modal .valid-button").addClass("disabled");
            $("#edit-service-modal .valid-button").addClass("valid-disabled");

            return false;
          } else {
            $("#edit-service-modal .text-danger").addClass("hide");
            $("#edit-service-modal .valid-button").removeClass("disabled");
            $("#edit-service-modal .valid-button").removeClass("valid-disabled");

            return true;
          }
        }

        // ok
        $("#edit-service-modal .text-danger").addClass("hide");
        $("#edit-service-modal .valid-button").removeClass("disabled");
        $("#edit-service-modal .valid-button").removeClass("valid-disabled");

        return true;
      },
      /**
      * Save the edits of the service
      */
      validEditService: function(e) {
        var self = this;

        if (e.type === "keyup" && e.keyCode === 13 || e.type === "click") {
          e.preventDefault();

          // update if information are ok
          if (this.checkService()) {
            this.$el.find("#edit-service-modal").on("hidden.bs.modal", function() {
              // set the new name to the service
              self.model.set("name", $("#edit-service-modal input#service-name").val());

              // send the updates to the server
              self.model.save();

              // tell the router that there is no modal any more
              appRouter.isModalShown = false;

              // rerender the view
              self.render();

              return false;
            });

            // hide the modal
            $("#edit-service-modal").modal("hide");
          }
        } else if (e.type === "keyup") {
          this.checkService();
        }
      },
      /**
      * Render the detailled view of a service
      */
      render: function() {
        var self = this;

        if (!appRouter.isModalShown) {
          switch (this.model.get("type")) {
            case 102: // mail
            this.$el.html(this.template({
              service: this.model,
              sensorType: $.i18n.t("services.mail.name.singular"),
              places: places,
              serviceDetails: this.tplMail
            }));
            break;
            case 103: // weather
            this.$el.html(this.template({
              service: this.model,
              sensorType: $.i18n.t("services.weather.name.singular"),
              places: places,
              serviceDetails: this.tplWeather
            }));
            break;
          }
          // resize the panel
          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
        }
      }
    });
    return ServiceDetailsView;
  });
