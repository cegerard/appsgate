define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/ARD.html"
  ], function(App, DeviceDetailsView, ARDDetailTemplate) {

    var ArdView = {};
    // detailled view of a device
    ArdView = DeviceDetailsView.extend({
      tplARD: _.template(ARDDetailTemplate),
      initialize: function() {
        var self = this;
        ArdView.__super__.initialize.apply(this, arguments);

        $.extend(self.__proto__.events, ArdView.__super__.events);
      },
      autoupdate: function() {
        ArdView.__super__.autoupdate.apply(this);

        var ardLastAuthorized = ""
        if (this.model.get("authorized")==="true") {
            ardLastAuthorized = "<span class='label label-yellow' data-i18n='devices.ard.affirmative'></span>";
        } else {
            ardLastAuthorized = "<span class='label label-default' data-i18n='devices.ard.negative'></span>";
        }
        this.$el.find("#ard-last-authorized").html(ardLastAuthorized);

        var ardLastMessage = "<span class='pull-right label label-";
        ardLastMessage += this.model.get("authorized") === true || this.model.get("authorized") === "true"?"success>":"default>";
        if (device.get("lastMessage")!=="") {
            ardLastMessage += this.model.get("lastMessage") + "</span>";
        } else {
            ardLastMessage = $.i18n.t("devices.ard.last-message-empty") + "</span>";
        }
        this.$el.find("#ard-last-message").html(ardLastMessage);

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
            sensorImg: ["app/img/sensors/ard-logo.png"],
            sensorType: $.i18n.t("devices.ard.name.singular"),
            places: places,
            deviceDetails: this.tplARD
          }));


          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
        }
      }
    });
    return ArdView
  });
