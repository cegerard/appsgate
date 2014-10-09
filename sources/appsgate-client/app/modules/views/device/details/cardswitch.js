define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/cardswitch.html"
  ], function(App, DeviceDetailsView, cardSwitchDetailTemplate) {

    var CardSwitchView = {};
    // detailled view of a device
    CardSwitchView = DeviceDetailsView.extend({
      tplCardSwitch: _.template(cardSwitchDetailTemplate),
      initialize: function() {
        var self = this;
        CardSwitchView.__super__.initialize.apply(this, arguments);

        $.extend(self.__proto__.events, CardSwitchView.__super__.events);
      },
      autoupdate: function() {
        CardSwitchView.__super__.autoupdate.apply(this);

        var cardSwitchStatus = ";"
        if (this.model.get("inserted")==="true") {
            cardSwitchStatus = "<span class='label label-yellow' data-i18n='devices.cardswitch.value.inserted'></span>";
        } else {
            cardSwitchStatus = "<span class='label label-default' data-i18n='devices.cardswitch.value.not-inserted'></span>";
        }
        this.$el.find("#card-switch-sensor").html(cardSwitchStatus);

        // translate the view
        this.$el.i18n();
      },
      /**
      * Render the detailled view of a device
      */
      render: function() {
        var self = this;

        if (!appRouter.isModalShown) {

          this.$el.html(this.template({
            device: this.model,
            sensorImg: ["app/img/sensors/keycard.png"],
            sensorType: $.i18n.t("devices.cardswitch.name.singular"),
            places: places,
            deviceDetails: this.tplCardSwitch
          }));

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
        }
      }
    });
    return CardSwitchView
  });
