define([
    "app",
    "views/program/menu",
    "views/program/reader",
    "views/program/editor"
], function(App, ProgramMenuView, ProgramReaderView, ProgramEditorView) {

    var ProgramRouter = {};
    // router
    ProgramRouter = Backbone.Router.extend({
        routes: {
            "programs": "list",
            "programs/:id": "reader",
            "programs/editor/:id": "editor",
        },
        list: function() {
            // display the side menu
            appRouter.showMenuView(new ProgramMenuView());

            // update the url if there is at least one program
            if (programs.length > 0) {
              this.reader(programs.at(0).get("id"));
            }

            $(".nav-item").removeClass("active");
            $("#programs-nav").addClass("active");
        },
        reader: function(id) {

            // display the requested program
            appRouter.showDetailsView(new ProgramReaderView({model: programs.get(id)}));

            // update the url
            appRouter.navigate("#programs/" + id);

            appRouter.currentMenuView.updateSideMenu();
        },
        editor: function(id) {
            // remove and unbind the current view for the menu
            if (appRouter.currentMenuView) {
                appRouter.currentMenuView.close();
            }
            if (appRouter.currentView) {
                appRouter.currentView.close();
            }

            $("#main").html(appRouter.navbartemplate());

            appRouter.currentMenuView = new ProgramEditorView({el:$("#main"),model: programs.get(id)});
            appRouter.currentMenuView.render();

            $("#main").append(appRouter.circlemenutemplate());

            // initialize the circle menu
            $(".controlmenu").circleMenu({
                trigger: "click",
                item_diameter: 50,
                circle_radius: 75,
                direction: 'top-right'
            });

            appRouter.navigate("#programs/editor/" + id);
        }

    });
    return ProgramRouter;
});
