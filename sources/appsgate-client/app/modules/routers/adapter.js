define([
  "app",
    "views/adapter/menu/menu",
    "views/adapter/details/adaptersView",
    "views/adapter/details/enoceanAdapter"

], function(App, AdapterMenuView, AdaptersDetailsView, EnOceanAdapterView) {

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

            firstElement = $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]);

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
            adapter = adapters.findWhere({type: id});

            switch (id) {
                case "UbikitAdapterService": //
                    appRouter.showDetailsView(new EnOceanAdapterView({id:id, model: adapter}));
                    break;
            }

        }
      });
      return AdapterRouter;
    });
