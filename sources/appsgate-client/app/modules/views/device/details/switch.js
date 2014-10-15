define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/switch.html"
  ], function(App, DeviceDetailsView, switchDetailTemplate) {

    var SwitchView = {};
    // detailled view of a device
    SwitchView = DeviceDetailsView.extend({
      tplSwitch: _.template(switchDetailTemplate),
      initialize: function() {
        var self = this;
        SwitchView.__super__.initialize.apply(this, arguments);

        $.extend(self.__proto__.events, SwitchView.__super__.events);
      },
      autoupdate: function() {
        SwitchView.__super__.autoupdate.apply(this);

        var switchButtonStatus = ""
        if (this.model.get("buttonStatus")==="true") {
            switchButtonStatus = "<span class='label label-yellow' data-i18n='devices.switch.value.opened'></span>";
        } else {
            switchButtonStatus = "<span class='label label-default' data-i18n='devices.switch.value.closed'></span>";
        }
        this.$el.find("#switch-button-status").html(switchButtonStatus);

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
            sensorImg: ["app/img/sensors/switch.png"],
            sensorType: $.i18n.t("devices.switch.name.singular"),
            places: places,
            deviceDetails: this.tplSwitch
          }));

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
        }
      }
    });
    return SwitchView
  });
