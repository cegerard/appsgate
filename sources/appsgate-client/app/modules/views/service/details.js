define([
  "app",
  "text!templates/services/details/serviceContainer.html",
  "text!templates/services/details/mediaplayer.html",
  "text!templates/services/details/mediabrowser.html",
  "text!templates/services/details/mail.html",
  "text!templates/services/details/weather.html"
  ], function(App, serviceDetailsTemplate, mediaPlayerDetailTemplate, mediaBrowserDetailTemplate, mailDetailTemplate, weatherDetailTemplate) {

    var ServiceDetailsView = {};
    // detailled view of a service
    ServiceDetailsView = Backbone.View.extend({
      template: _.template(serviceDetailsTemplate),
      tplMediaPlayer: _.template(mediaPlayerDetailTemplate),
      tplMediaBrowser: _.template(mediaBrowserDetailTemplate),
      tplMail: _.template(mailDetailTemplate),
      tplWeather: _.template(weatherDetailTemplate),
      // map the events and their callback
      events: {
        "click button.back-button": "onBackButton",
        "click button.btn-media-play": "onPlayMedia",
        "click button.btn-media-resume": "onResumeMedia",
        "click button.btn-media-pause": "onPauseMedia",
        "click button.btn-media-stop": "onStopMedia",
        "click button.btn-media-volume": "onSetVolumeMedia",
        "click button.btn-media-browse": "onBrowseMedia",
        "show.bs.modal #edit-service-modal": "initializeModal",
        "hidden.bs.modal #edit-service-modal": "toggleModalValue",
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
      * Called when resume button is pressed and the displayed service is a media player
      */
      onPlayMedia: function() {
        this.model.sendPlay();
      },
      /**
      * Called when resume button is pressed and the displayed service is a media player
      */
      onResumeMedia: function() {
        this.model.sendResume();
      },
      /**
      * Called when pause button is pressed and the displayed service is a media player
      */
      onPauseMedia: function() {
        this.model.sendPause();
      },
      /**
      * Called when stop button is pressed and the displayed service is a media player
      */
      onStopMedia: function() {
        this.model.sendStop();
      },
      /**
      * Called when volume is chosen and the displayed service is a media player
      */
      onSetVolumeMedia: function() {
        this.model.setVolume();
      },
      /**
      * Called when browse button is pressed, displays a tree of available media
      */
      onBrowseMedia: function(e) {

        this.model.onBrowseMedia($("#selectedMedia"));
      },
      /**
      * Clear the input text, hide the error message and disable the valid button by default
      */
      initializeModal: function() {
        $("#edit-service-modal input#service-name").val(this.model.get("name").replace(/&eacute;/g, "é").replace(/&egrave;/g, "è"));
        $("#edit-service-modal .text-danger").addClass("hide");
        $("#edit-service-modal .valid-button").addClass("disabled");

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
      checkService: function() {
        // name already exists
        if (services.where({name: $("#edit-service-modal input").val()}).length > 0) {
          if (services.where({name: $("#edit-service-modal input").val()})[0].get("id") !== this.model.get("id")) {
            $("#edit-service-modal .text-danger").removeClass("hide");
            $("#edit-service-modal .text-danger").text("Nom déjà existant");
            $("#edit-service-modal .valid-button").addClass("disabled");

            return false;
          } else {
            $("#edit-service-modal .text-danger").addClass("hide");
            $("#edit-service-modal .valid-button").removeClass("disabled");

            return true;
          }
        }

        // ok
        $("#edit-service-modal .text-danger").addClass("hide");
        $("#edit-service-modal .valid-button").removeClass("disabled");

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
          case 31: // media player
          this.$el.html(this.template({
            service: this.model,
            sensorImg: "app/img/services/media_player.png",
            sensorType: $.i18n.t("services.mediaplayer.name.singular"),
            places: places,
            serviceDetails: this.tplMediaPlayer
          }));

          // initialize the volume slider
          _.defer(function() {
            $(".volume-slider").slider({
              range: "min",
              min: 0,
              max: 100,
              value: 100,
              stop: function(event, ui) {
                self.model.sendVolume($(".volume-slider").slider("value"));
              }
            });
          });

          // requesting current volume level
          this.model.remoteCall("getVolume", [], this.model.get("id") + ":volume");
          break;
        case 36: // media browser
        this.$el.html(this.template({
          service: this.model,
          sensorType: $.i18n.t("services.mediabrowser.name.singular"),
          places: places,
          serviceDetails: this.tplMediaBrowser
        }));
        break;
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
