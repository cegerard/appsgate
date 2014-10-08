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
      },
      autoupdate: function() {
        DomiCubeView.__super__.autoupdate.apply(this);

        var activeFace = "";
        if(this.model.get("activeFace") === "1") {
          activeFace = "<img src='/app/img/lan.svg' width='18px' class='img-responsive'>";
        } else if(this.model.get("activeFace") === "2") {
          activeFace = "<span data-i18n='devices.domicube.status.unknown'></span>";
        } else if(this.model.get("activeFace") === "3") {
          activeFace = "<img src='/app/img/music.png' width='18px' class=img-responsive>";
        } else if(this.model.get("activeFace") === "4") {
          activeFace = "<img src='/app/img/question.svg' width='18px' class='img-responsive'>";
        } else if(this.model.get("activeFace") === "5") {
          activeFace = "<img src='/app/img/night.png' width='18px' class='img-responsive'>";
        } else if(this.model.get("activeFace") === "6") {
          activeFace = "<img src='/app/img/meal.png' width='18px' class='img-responsive'>";
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
