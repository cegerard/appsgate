define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/contact.html"
  ], function(App, DeviceDetailsView, contactDetailTemplate) {

    var ContactSensorView = {};
    // detailled view of a device
    ContactSensorView = DeviceDetailsView.extend({
      tplContact: _.template(contactDetailTemplate),
      // map the events and their callback
      initialize: function() {
        var self = this;
        ContactSensorView.__super__.initialize.apply(this, arguments);
      },
      autoupdate: function() {
        ContactSensorView.__super__.autoupdate.apply(this);

        var contactSensorStatus = ""
        if (this.model.get("contact")==="true") {
            contactSensorStatus = "<span class='label label-default' data-i18n='devices.contact.value.closed'></span>";
        } else {
            contactSensorStatus = "<span class='label label-yellow' data-i18n='devices.contact.value.opened'></span>";
        }
        this.$el.find("#contact-sensor-value").html(contactSensorStatus);

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
            sensorImg: ["app/img/sensors/contact.png"],
            sensorType: $.i18n.t("devices.contact.name.singular"),
            places: places,
            deviceDetails: this.tplContact
          }));

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
        }
      }
    });
    return ContactSensorView
  });
