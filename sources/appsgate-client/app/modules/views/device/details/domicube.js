define([
  "app",
  "views/device/details/details",
  "text!templates/devices/details/domicube.html"
  ], function(App, DeviceDetailsView, domicubeDetailTemplate) {

    var DomiCubeView = {};
    // detailled view of a device
    DomiCubeView = DeviceDetailsView.extend({
      tplDomiCube: _.template(domicubeDetailTemplate),
      initialize: function() {
        var self = this;
        DomiCubeView.__super__.initialize.apply(this, arguments);

        $.extend(self.__proto__.events, DomiCubeView.__super__.events);
      },
      autoupdate: function() {
        DomiCubeView.__super__.autoupdate.apply(this);

        var activeFace = "";
        if(this.model.get("activeFace") === "1") {
          activeFace = "<img src='app/img/domicube-work.svg' width='18px' class='img-responsive'>";
        } else if(this.model.get("activeFace") === "2") {
          activeFace = "<svg class='white-face-svg-domus img-responsive'>" +
            "<rect class='white-face-rect-domus' x='10' y='10' rx='25' ry='25' width='95%' height='90%'/>" +
            "<text class='white-face-text-domus' x='50%' y='47%'>" + $.i18n.t('devices.domicube.white-face.first-elem') +
            "</text><text class='white-face-text-domus' x='50%' y='54%'>" + $.i18n.t('devices.domicube.white-face.second-elem') +
            "</text></svg>";
        } else if(this.model.get("activeFace") === "3") {
          activeFace = "<img src='app/img/domicube-music.png' width='18px' class=img-responsive>";
        } else if(this.model.get("activeFace") === "4") {
          activeFace = "<img src='app/img/domicube-question.svg' width='18px' class='img-responsive'>";
        } else if(this.model.get("activeFace") === "5") {
          activeFace = "<img src='app/img/domicube-night.png' width='18px' class='img-responsive'>";
        } else if(this.model.get("activeFace") === "6") {
          activeFace = "<img src='app/img/domicube-meal.png' width='18px' class='img-responsive'>";
        } else {
          activeFace = "<span data-i18n='devices.domicube.status.unknown'></span>";
        }

        this.$el.find("#domicube-active-face").html(activeFace);

        // translate the view
        this.$el.i18n();
      },
      /**
      * Render the detailled view of a device
      */
      render: function() {
        var self = this;

        this.$el.html(this.template({
          device: this.model,
          sensorImg: ["app/img/sensors/domicube.jpg"],
          sensorType: $.i18n.t("devices.domicube.name.singular"),
          places:places,
          deviceDetails: this.tplDomiCube
        }));

        this.resize($(".scrollable"));

        // translate the view
        this.$el.i18n();

        return this;
      }
    });
    return DomiCubeView
  });
