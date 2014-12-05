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
  "views/device/details/temperature",
  "views/device/details/mediaplayer",
    "views/device/details/coretv"
  ], function(App, DeviceMenuView, DevicesByTypeView, ActuatorView, ArdView, ContactSensorView,
    DomiCubeView, LightSensorView, CardSwitchView, PhillipsHueView, SmartPlugView, SwitchView, TemperatureSensorView, MediaPlayerView, CoreTVView) {

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
          var id = $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).attr("id");
          if (id) {
            var typeId = $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).attr("id").split("side-")[1];
            // update the url
            appRouter.navigate("#devices/types/" + typeId, {replace:true});
            $(".nav-item").removeClass("active");
            $(".devices-nav").addClass("active");

            appRouter.showDetailsView(new DevicesByTypeView({id: typeId}));
          }

          dispatcher.trigger("router:loaded");
        },
        /**
        * Display all the devices of a given type
        *
        * @param typeId id of the device category to show
        */
        deviceByType: function(typeId) {
          appRouter.showDetailsView(new DevicesByTypeView({id: typeId}));
        },
        /**
        * Show the details of a device
        *
        * @method details
        * @param id Id of the device to show
        */
        details: function(id) {
		  // Direct access device, need to add the menu
		  if (appRouter.currentMenuView === null || appRouter.currentMenuView.attributes === undefined
        || (appRouter.currentMenuView.attributes.class !== "DeviceMenuView" && appRouter.currentMenuView.attributes.class !== "PlaceMenuView")) {
			  // display the side menu
			  appRouter.showMenuView(new DeviceMenuView());
			  appRouter.currentMenuView.updateSideMenu();
			  // update tab
			  $(".nav-item").removeClass("active");
			  $(".devices-nav").addClass("active");
		  }

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
            case 31: // media player
            appRouter.showDetailsView(new MediaPlayerView({model:device}));
            break;
            case 124: // CoreTV
                  appRouter.showDetailsView(new CoreTVView({model:device}));
                  break;
            case 210: // domicube
            appRouter.showDetailsView(new DomiCubeView({model:device}));
            break;
          }
        }
      });
      return DeviceRouter;
    });
