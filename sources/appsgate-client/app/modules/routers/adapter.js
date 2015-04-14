define([
  "app",
    "views/adapter/menu/menu",
    "views/adapter/details/adaptersView",
    "views/adapter/details/enoceanAdapter",
    "views/adapter/details/weatherAdapter"


], function(App, AdapterMenuView, AdaptersDetailsView, EnOceanAdapterView, WeatherAdapterView) {

      var AdapterRouter = {};
      /**
      * Router to handle the routes for the adapter
      *
      * @class Adapter.Router
      */
      AdapterRouter = Backbone.Router.extend({
        // define the routes for the adapters
        routes: {
            "adapters": "list",
            "adapters/:id": "details"
        },
        /**
        * @method list Show the list of adapters
        */
        list: function() {

          appRouter.showMenuView(new AdapterMenuView());

            firstElement = $($($(".aside-menu .list-group")[0]).find(".list-group-item")[0]);

            // set active the first element - displayed by default
            firstElement.addClass("active");

            // display the first category of devices
            var type = firstElement.attr("id");

            if (type) {
                appRouter.navigate("#adapters/" + type, {replace:true});
                $(".nav-item").removeClass("active");
                $(".devices-nav").addClass("active");
                this.details(type);
            }
            dispatcher.trigger("router:loaded");
        },
        /**
        * Show the details of an Adapter
        *
        * @method details
        * @param id Id of the Adapter to show
        */
        details: function(id) {
            console.log("rendering details for type : ",id);
            adapter = adapters.get(id);
            var type = adapter.get("type");

            switch (type) {
                case "UbikitAdapterService": //
                    appRouter.showDetailsView(new EnOceanAdapterView({id:type, model: adapter}));
                    break;
                case "WeatherAdapterSpec": //
                    appRouter.showDetailsView(new WeatherAdapterView({id:type, model: adapter}));
                    break;
                default :
                    console.warn("unknown type of adapter");
            }

        }
      });
      return AdapterRouter;
    });
