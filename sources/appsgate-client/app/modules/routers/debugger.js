define([
    "app",
    "views/debugger/default"
], function(App, DebuggerView) {

    var DebuggerRouter = {};
    // router
    DebuggerRouter = Backbone.Router.extend({
        routes: {
            "debugger": "all",
            "debugger/all": "all"
        },
        // debug everything
        all: function() {
            // remove and unbind the current view for the menu
            if (appRouter.currentMenuView) {
                appRouter.currentMenuView.close();
            }
            if (appRouter.currentView) {
                appRouter.currentView.close();
            }

            $("#main").html(appRouter.navbartemplate());

            appRouter.currentMenuView = new DebuggerView({el:$("#main")});
            appRouter.currentMenuView.render();

            $("#main").append(appRouter.circlemenutemplate());

            // initialize the circle menu
            $(".controlmenu").circleMenu({
                trigger: "click",
                item_diameter: 50,
                circle_radius: 150,
                direction: 'top-right'
            });

            $(".navmenu").circleMenu({
                trigger: "click",
                item_diameter: 50,
                circle_radius: 150,
                direction: 'top'
            });

            appRouter.navigate("#debugger/all");

            $(".breadcrumb").html("<li><a href='#home'><span data-i18n='navbar.home'/></a></li>");
            $(".breadcrumb").append("<li><a href='#debugger'><span data-i18n='navbar.debugger'/></a></li>");
            $(".breadcrumb").append("<li class='active'><span data-i18n='debugger.all'/></li>");
            appRouter.translateNavbar();
        }
    });
    return DebuggerRouter;
});
