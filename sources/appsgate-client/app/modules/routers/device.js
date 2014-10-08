define([
  "app",
  "views/device/menu",
  "views/device/devicebytype",
  "views/device/details/actuator",
  "views/device/details/ard",
  "views/device/details/contact",
  "views/device/details/domicube",
  "views/device/details/lightsensor",
  "views/device/details/cardswitch",
  "views/device/details/phillipshue",
  "views/device/details/plug",
  "views/device/details/switch",
  "views/device/details/temperature"
  ], function(App, DeviceMenuView, DevicesByTypeView, ActuatorView, ArdView, ContactSensorView,
    DomiCubeView, LightSensorView, CardSwitchView, PhillipsHueView, SmartPlugView, SwitchView, TemperatureSensorView) {

      var DeviceRouter = {};
      /**
      * Router to handle the routes for the devices
      *
      * @class Device.Router
      */
      DeviceRouter = Backbone.Router.extend({
        // define the routes for the devices
        routes: {
          "devices": "list",
          "devices/types/:id": "deviceByType",
          "devices/:id": "details"
        },
        /**
        * @method list Show the list of devices
        */
        list: function() {
          // display the side menu
          appRouter.showMenuView(new DeviceMenuView());

          // set active the first element - displayed by default
          $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).addClass("active");

          // display the first category of devices
          var typeId = $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).attr("id").split("side-")[1];
          appRouter.showDetailsView(new DevicesByTypeView({id: typeId}));

          // update the url
          appRouter.navigate("#devices/types/" + typeId);

          $(".nav-item").removeClass("active");
          $("#devices-nav").addClass("active");

          appRouter.translateNavbar();
        },
        /**
        * Display all the devices of a given type
        *
        * @param typeId id of the device category to show
        */
        deviceByType: function(typeId) {
          appRouter.showDetailsView(new DevicesByTypeView({id: typeId}));

          $(".nav-item").removeClass("active");
          $("#devices-nav").addClass("active");

          appRouter.translateNavbar();
        },
        /**
        * Show the details of a device
        *
        * @method details
        * @param id Id of the device to show
        */
        details: function(id) {
          var device = devices.get(id);
          switch (device.get("type")) {
            case 0: // temperature sensor
            appRouter.showDetailsView(new TemperatureSensorView({model:device}));
            break;
            case 1: // illumination sensor
            appRouter.showDetailsView(new LightSensorView({model:device}));
            break;
            case 2: // switch sensor
            appRouter.showDetailsView(new SwitchView({model:device}));
            break;
            case 3: // contact sensor
            appRouter.showDetailsView(new ContactSensorView({model:device}));
            break;
            case 4: // card switch sensor
            appRouter.showDetailsView(new CardSwitchView({model:device}));
            break;
            case 5: // ARD lock
            appRouter.showDetailsView(new ArdView({model:device}));
            break;
            case 6: // smart plug
            appRouter.showDetailsView(new SmartPlugView({model:device}));
            break;
            case 7: // phillips hue
            appRouter.showDetailsView(new PhillipsHueView({model:device}));
            break;
            case 8: // switch actuator
            appRouter.showDetailsView(new SwitchView({model:device}));
            break;
            case 210: // domicube
            appRouter.showDetailsView(new DomiCubeView({model:device}));
            break;
          }

          $(".nav-item").removeClass("active");
          $("#devices-nav").addClass("active");

          appRouter.translateNavbar();
        }
      });
      return DeviceRouter;
    });
