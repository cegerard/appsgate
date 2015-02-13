define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/mediaplayer.html"
  ], function(App, DeviceDetailsView, mediaPlayerDetailTemplate) {

    var MediaPlayerView = {};
    // detailled view of a device
    MediaPlayerView = DeviceDetailsView.extend({
      tplMediaPlayer: _.template(mediaPlayerDetailTemplate),
      events: {
        "click button.btn-media-play": "onPlayMedia",
        "click button.btn-media-resume": "onResumeMedia",
        "click button.btn-media-pause": "onPauseMedia",
        "click button.btn-media-stop": "onStopMedia",
        "click button.btn-media-volume": "onSetVolumeMedia",
        "click button.btn-media-browse": "onBrowseMedia",
        "click button.btn-media-audioNotification": "onAudioNotification",
        "click .ttsInput": "updateAudioNotif"

      },
      initialize: function() {
        var self = this;
        MediaPlayerView.__super__.initialize.apply(this, arguments);

        $.extend(self.__proto__.events, MediaPlayerView.__super__.events);

      },
      autoupdate: function() {
        MediaPlayerView.__super__.autoupdate.apply(this);


        var player = this.model;

        this.$el.html(this.template({
          device: player,
          sensorImg: "app/img/services/media_player.png",
          sensorType: $.i18n.t("devices.mediaplayer.name.singular"),
          places: places,
          deviceDetails: this.tplMediaPlayer
        }));

        player.requestVolume();


        // initialize the volume slider
        _.defer(function() {
          $(".volume-slider").slider({
            range: "min",
            min: 0,
            max: 100,
            value: player.get("volume"),
            stop: function(event, ui) {
              player.sendVolume($(".volume-slider").slider("value"));
            }
          });
          player.save();
        });

        // translate the view
        this.$el.i18n();
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
       * Called when click on AudioNotification
       */
      onAudioNotification: function() {

        this.model.onAudioNotification($("#selectedMessage").val(),
            services.getCoreTTS().getVoice(),
            services.getCoreTTS().getSpeed()
        );
      },

      updateAudioNotif: function() {

        if (services.getCoreTTS() != undefined) {
          $(document).find(".ttsInput").autocomplete({
            source: services.getCoreTTS().getTTSItemsText()
          });}
      },

      /**
      * Render the detailled view of a device
      */
      render: function() {
        var self = this;


        if (!appRouter.isModalShown) {

          var player = this.model;
          this.$el.html(this.template({
            device: player,
            sensorImg: "app/img/services/media_player.png",
            sensorType: $.i18n.t("devices.mediaplayer.name.singular"),
            places: places,
            deviceDetails: this.tplMediaPlayer
          }));

          player.requestVolume();

          // initialize the volume slider
          _.defer(function() {
            $(".volume-slider").slider({
              range: "min",
              min: 0,
              max: 100,
              value: player.get("volume"),
              stop: function(event, ui) {
                self.model.sendVolume($(".volume-slider").slider("value"));
              }
            });
            self.model.save();
          });


          this.resize($(".scrollable"));

            // translate the view
            this.$el.i18n();
          this.updateAudioNotif();


          return this;
        }
      }
    });
    return MediaPlayerView
  });
