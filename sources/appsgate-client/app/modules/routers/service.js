define([
  "app",
  "views/service/menu",
  "views/service/servicebytype",
  "views/service/details/tts",
  "views/service/details"
  ], function(App, ServiceMenuView, ServicesByTypeView, TTSDetailsView, ServiceDetailsView) {

    var ServiceRouter = {};
    /**
    * Router to handle the routes for the services
    *
    * @class Service.Router
    */
    ServiceRouter = Backbone.Router.extend({
      // define the routes for the services
      routes: {
        "services": "list",
        "services/types/:id": "serviceByType",
        "services/:id": "details"
      },
      /**
      * @method list Show the list of services
      */
      list: function() {
        // display the side menu
        appRouter.showMenuView(new ServiceMenuView());

        $(".nav-item").removeClass("active");
        $(".services-nav").addClass("active");

        // set active the first element - displayed by default
        if ($($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).length > 0) {
          $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).addClass("active");

          // display the first category of services
          var typeId = $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).attr("id").split("side-")[1];
          appRouter.showDetailsView(new ServicesByTypeView({id: typeId}));

          // update the url
          appRouter.navigate("#services/types/" + typeId, {replace:true});
        }

        dispatcher.trigger("router:loaded");
      },
      /**
      * Display all the services of a given type
      *
      * @param typeId id of the service category to show
      */
      serviceByType: function(typeId) {

	  	// Direct access device, need to add the menu
	  	if (appRouter.currentMenuView === null || appRouter.currentMenuView.attributes === undefined || appRouter.currentMenuView.attributes.class !== "ServiceMenuView") {
			  // display the side menu
			  appRouter.showMenuView(new ServiceMenuView());
			  appRouter.currentMenuView.updateSideMenu();
	  	}



        switch(typeId) {
          case "104":
            appRouter.showDetailsView(new TTSDetailsView({model: services.getCoreTTS()}));
            break;
          default :
            appRouter.showDetailsView(new ServicesByTypeView({id: typeId}));
            break;
        }

        $(".nav-item").removeClass("active");
        $(".services-nav").addClass("active");
      },
      /**
      * Show the details of a service
      *
      * @method details
      * @param id Id of the service to show
      */
      details: function(id) {
	  	// Direct access device, need to add the menu
	  	if (appRouter.currentMenuView === null || appRouter.currentMenuView.attributes === undefined || appRouter.currentMenuView.attributes.class !== "ServiceMenuView") {
			  // display the side menu
			  appRouter.showMenuView(new ServiceMenuView());
			  appRouter.currentMenuView.updateSideMenu();
			  // update tab
			  $(".nav-item").removeClass("active");
			  $(".services-nav").addClass("active");
	  	}

        var service = services.get(id);
        switch(service.get("type")) {
          case 104:
            appRouter.showDetailsView(new TTSDetailsView({model: service}));
            break;
          default :
            appRouter.showDetailsView(new ServiceDetailsView({model: services.get(id)}));
            break;
        }


      }
    });
    return ServiceRouter;
  });
