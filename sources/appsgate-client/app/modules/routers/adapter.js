define([
  "app",
    "views/adapter/adaptermenu"
  ], function(App, AdapterMenuView) {

      var AdapterRouter = {};
      /**
      * Router to handle the routes for the adapter
      *
      * @class Adapter.Router
      */
      AdapterRouter = Backbone.Router.extend({
        // define the routes for the adapters
        routes: {
        },
        /**
        * @method list Show the list of devices
        */
        list: function() {

          appRouter.showMenuView(new AdapterMenuView());

          dispatcher.trigger("router:loaded");
        },
        /**
        * Show the details of an Adapter
        *
        * @method details
        * @param id Id of the Adapter to show
        */
        details: function(id) {
		  // Direct access device, need to add the menu

        }
      });
      return AdapterRouter;
    });
