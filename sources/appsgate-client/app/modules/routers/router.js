define(function(require, exports, module) {
  "use strict";

  // External dependencies.
  var HomeView = require("views/home/home");
  var PlacesRouter = require("routers/place");
  var DevicesRouter = require("routers/device");
  var ServicesRouter = require("routers/service");
  var ProgramsRouter = require("routers/program");
  var DebuggerRouter = require("routers/debugger");
  var DependanciesRouter = require("routers/dependancies");

  var mainTemplate = require("text!templates/home/main.html");
  var navbarTemplate = require("text!templates/home/navbar.html");
  var circleMenuTemplate = require("text!templates/home/circlemenu.html");
  var loadingWidgetTemplate = require("text!templates/home/loadingWidget.html");

  // define the application router
  var Router = Backbone.Router.extend({

    placesRouter: new PlacesRouter(),
    devicesRouter: new DevicesRouter(),
    servicesRouter: new ServicesRouter(),
    programsRouter: new ProgramsRouter(),
    debuggerRouter: new DebuggerRouter(),
    dependanciesRouter: new DependanciesRouter(),

    maintemplate : _.template(mainTemplate),
    navbartemplate : _.template(navbarTemplate),
    circlemenutemplate : _.template(circleMenuTemplate),
    loadingtemplate : _.template(loadingWidgetTemplate),
    routes: {
      "": "home",
      "reset": "home",
      "home": "home",
      "dashboard": "debugger",
      "places": "places",
      "devices": "devices",
      "services": "services",
      "programs": "programs",
      "dependancies": "dependancies",
//      "dependancies/:id": "dependanciesId"
    },
    initialize: function() {
      dispatcher.on("router:loading", function() {
          appRouter.loading = true;
          _.delay(function() {
            if(appRouter.loading) {
              appRouter.showLoadingWidget();
            }
          },100);
      });

      dispatcher.on("router:loaded", function() {
        appRouter.loading = false;
        appRouter.hideLoadingWidget();
      });
    },

    // default route of the application
    places: function() {
      dispatcher.trigger("router:loading");
      this.placesRouter.list();
    },
    devices: function() {
      dispatcher.trigger("router:loading");
      this.devicesRouter.list();
    },
    services: function() {
      dispatcher.trigger("router:loading");
      this.servicesRouter.list();
    },
    programs: function() {
      dispatcher.trigger("router:loading");
      this.programsRouter.list();
    },
    debugger: function() {
      dispatcher.trigger("router:loading");
      this.debuggerRouter.all();
    },
    dependancies: function() {
      dispatcher.trigger("router:loading");
      this.dependanciesRouter.all();
    },
    home: function() {
      // remove and unbind the current view for the menu
      if (this.currentMenuView) {
          this.currentMenuView.close();
      }
      if (this.currentView) {
          this.currentView.close();
      }

      appRouter.currentMenuView = new HomeView({el:$("#main")});
      appRouter.currentMenuView.render();

      $("#main").append(appRouter.circlemenutemplate());

      // initialize the circle menu
      $(".controlmenu").circleMenu({
          trigger: "click",
          item_diameter: 50,
          circle_radius: 75,
          direction: 'top-right'
      });
    },
    // update the side menu w/ new content
    showMenuView: function(menuView) {
      // remove and unbind the current view for the menu
      if (this.currentMenuView) {
        this.currentMenuView.close();
      }

      $("#main").html(this.navbartemplate());
      $("#main").append(this.maintemplate());
      $("#main").append(this.circlemenutemplate());

      this.currentMenuView = menuView;
      this.currentMenuView.render();
      $(".aside-menu").html(this.currentMenuView.$el);

      // initialize the circle menu
      $(".controlmenu").circleMenu({
        trigger: "click",
        item_diameter: 50,
        circle_radius: 75,
        direction: 'top-right'
      });

      $("body").i18n();
    },
    showDetailsView: function(view) {
      // remove and unbind the current view
      if (this.currentView) {
        this.currentView.close();
      }

      // update the content
      this.currentView = view;
      $(".body-content").html(this.currentView.$el);
      this.currentView.render();
    },
    showView: function(view) {
      // remove and unbind the current view
      if (this.currentView) {
        this.currentView.close();
      }

      // update the content
      this.currentView = view;
      this.currentView.render();
    },
    showLoadingWidget: function() {
      $("body").append(this.loadingtemplate);
    },
    hideLoadingWidget: function() {
      $(".loading-widget-wrapper").remove();
    },
    updateLocale:function(locale) {
      this.locale = locale;

      $.i18n.init({ lng : this.locale }).done(function() {
        $("body").i18n();
      });
    }
  });

  module.exports = Router;

});
