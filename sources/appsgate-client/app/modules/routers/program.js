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

            $(".nav-item").removeClass("active");
            $("#programs-nav").addClass("active");

            // update the url if there is at least one program
            if (programs.length > 0) {
              this.reader(programs.at(0).get("id"));
            }

            dispatcher.trigger("router:loaded");
        },
        reader: function(id) {
			// Direct access to a program, need to add the menu
			if (appRouter.currentMenuView === null || appRouter.currentMenuView.attributes === undefined || appRouter.currentMenuView.attributes.class !== "ProgramMenuView") {
				// display the side menu
				appRouter.showMenuView(new ProgramMenuView());
				// update tab
				$(".nav-item").removeClass("active");
				$("#programs-nav").addClass("active");
			}	

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

            $(".nav-item").removeClass("active");
            $("#programs-nav").addClass("active");

            appRouter.navigate("#programs/editor/" + id);

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
        }

    });
    return ProgramRouter;
});
