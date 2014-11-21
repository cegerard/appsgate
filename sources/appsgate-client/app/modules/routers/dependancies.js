define([
    "app",
    "views/dependancy/graph",
	"views/dependancy/menu"
], function (App, GraphView, DependancyMenuView) {

	var DependancyRouter = {};
	// router
	DependancyRouter = Backbone.Router.extend({
		routes: {
			"dependancy": "all",
			"dependancy/id": "selected"
		},
		// No selected entity
		all: function (id) {

			// remove and unbind the current view for the menu
			if (appRouter.currentMenuView) {
				appRouter.currentMenuView.close();
			}
			if (appRouter.currentView) {
				appRouter.currentView.close();
			}

			$("#main").html(appRouter.navbartemplate());

			// Send the request to the server to get the graph
			communicator.sendMessage({
				method: "getGraph",
				args: [],
				callId: "loadGraph",
				TARGET: "EHMI"
			});

			$("#main").append(appRouter.circlemenutemplate());

			// initialize the circle menu
			$(".controlmenu").circleMenu({
				trigger: "click",
				item_diameter: 50,
				circle_radius: 75,
				direction: 'top-right'
			});

			appRouter.navigate("#dependancies/all");

			$(".nav-item").removeClass("active");
			$("#dependancies-nav").addClass("active");

			$("body").i18n();

			// Once the dependancies have been created and added to the collection, show the graph
			dispatcher.once("dependanciesReady", function () {
				appRouter.showMenuView(new DependancyMenuView({
					model: dependancies.at(0)
				}));
				
				$(".nav-item").removeClass("active");
				$("#dependancies-nav").addClass("active");
				
				appRouter.showDetailsView(new GraphView({
					//                    el: $("#main"),
					model: dependancies.at(0)
				}));
			});
		},
		// One entity selected
		//        selected: function (id) {
		//            
		//            console.log("SELECTED id : " + id);
		//            
		//            // remove and unbind the current view for the menu
		//            if (appRouter.currentMenuView) {
		//                appRouter.currentMenuView.close();
		//            }
		//            if (appRouter.currentView) {
		//                appRouter.currentView.close();
		//            }
		//
		//            $("#main").html(appRouter.navbartemplate());
		//
		//            appRouter.currentMenuView = new GraphView({
		//                el: $("#main")
		//            });
		//            appRouter.currentMenuView.render();
		//
		//            $("#main").append(appRouter.circlemenutemplate());
		//
		//            // initialize the circle menu
		//            $(".controlmenu").circleMenu({
		//                trigger: "click",
		//                item_diameter: 50,
		//                circle_radius: 75,
		//                direction: 'top-right'
		//            });
		//
		//            appRouter.navigate("#dependancies/from" + id);
		//
		//            $(".nav-item").removeClass("active");
		//            $("#home-nav").addClass("active");
		//
		//            $("body").i18n();
		//        }
	});
	return DependancyRouter;
});