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
                appRouter.currentMenuView = null;
            }
            if (appRouter.currentView) {
                appRouter.currentView.close();
                appRouter.currentView = null;
            }

            $("#main").html(appRouter.navbartemplate());

            appRouter.navigate("#debugger/all");
            $(".nav-item").removeClass("active");
            $("#home-nav").addClass("active");

            appRouter.currentView = new DebuggerView({el:$("#main")});
            appRouter.currentView.render();

            $("#main").append(appRouter.circlemenutemplate());

            // initialize the circle menu
            $(".controlmenu").circleMenu({
                trigger: "click",
                item_diameter: 50,
                circle_radius: 75,
                direction: 'top-right'
            });

            // resize the menu
            appRouter.currentView.resize($(".div-scrollable"));

            $(document).i18n();

            dispatcher.trigger("router:loaded");
        }
    });
    return DebuggerRouter;
});
