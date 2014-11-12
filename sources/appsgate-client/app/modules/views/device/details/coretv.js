define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/coreTV.html"
  ], function(App, DeviceDetailsView, CoreTVDetailTemplate) {

    var CoreTVView = {};
    // detailed view of the TV
    CoreTVView = DeviceDetailsView.extend({
      tplTV: _.template(CoreTVDetailTemplate),
        // map the events and their callback
        events: {
            "click button.btn-tv-channelup": "onTVChannelUp",
            "click button.btn-tv-channeldown": "onTVChannelDown",
            "click button.btn-tv-resume": "onTVResume",
            "click button.btn-tv-pause": "onTVPause",
            "click button.btn-tv-stop": "onTVStop",
            "click button.btn-tv-notify": "onTVNotify"
        },
      initialize: function() {
        var self = this;
          CoreTVView.__super__.initialize.apply(this, arguments);

        $.extend(self.__proto__.events, CoreTVView.__super__.events);
      },
      autoupdate: function() {
          CoreTVView.__super__.autoupdate.apply(this);

        // translate the view
        this.$el.i18n();
      },

    onTVChannelUp: function() {
        this.model.channelUp();
        },
    onTVChannelDown: function() {
        this.model.channelDown();
        },
    onTVResume: function() {
        this.model.resume();
        },
    onTVPause: function() {
        this.model.pause();
        },
    onTVStop: function() {
        this.model.stop();
        },
    onTVNotify: function() {
        this.model.notify();
    },
      /**
      * Render the detailled view of a device
      */
      render: function() {
        var self = this;

        if (!appRouter.isModalShown) {

            this.$el.html(this.template({
                device: this.model,
                sensorImg: ["app/img/tv.gif"],
                sensorType: $.i18n.t("devices.tv.name.singular"),
                places: places,
                deviceDetails: this.tplTV
            }));

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
        }
      }
    });
    return CoreTVView
  });
