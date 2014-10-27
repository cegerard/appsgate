define([
    "app",
    "views/dependancy/graph"
], function (App, GraphView) {

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

            appRouter.currentMenuView = new GraphView({
                el: $("#main"),
                model: dependancies.at(0)
            });
            appRouter.currentMenuView.render();

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
            $("#home-nav").addClass("active");

            $("body").i18n();
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