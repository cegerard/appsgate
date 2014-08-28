define(function(require, exports, module) {
  "use strict";

  // External dependencies.
  var LoginView = require("views/login/loginview");
  var HomeView = require("views/homeview");

  var PlacesRouter = require("routers/place");
  var DevicesRouter = require("routers/device");
  var ServicesRouter = require("routers/service");
  var ProgramsRouter = require("routers/program");
  var DebuggerRouter = require("routers/debugger");

  var mainTemplate = require("text!templates/home/main.html");
  var navbarTemplate = require("text!templates/home/navbar.html");
  var circleMenuTemplate = require("text!templates/home/circlemenu.html");

  // define the application router
  var Router = Backbone.Router.extend({

    placesRouter: new PlacesRouter(),
    devicesRouter: new DevicesRouter(),
    servicesRouter: new ServicesRouter(),
    programsRouter: new ProgramsRouter(),
    debuggerRouter: new DebuggerRouter(),

    maintemplate : _.template(mainTemplate),
    navbartemplate : _.template(navbarTemplate),
    circlemenutemplate : _.template(circleMenuTemplate),

    routes: {
      "": "login",
      "login": "login",
      "reset": "home",
      "home": "home",
      "places": "habitat",
      "devices": "devices",
      "services": "services",
      "programs": "programs",
      "debugger": "debugger"
    },
    // default route of the application
    login: function() {
      this.showView(new LoginView());
    },
    home: function() {
      this.showView(new HomeView());
      $(".breadcrumb").html("<li class='active'><span data-i18n='navbar.home'/></li>");
      this.translateNavbar();
    },
    habitat: function() {
      this.placesRouter.list();
    },
    devices: function() {
      this.devicesRouter.list();
    },
    services: function() {
      this.servicesRouter.list();
    },
    programs: function() {
      this.programsRouter.list();
    },
    debugger: function() {
      this.debuggerRouter.all();
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

      $(".navmenu").circleMenu({
        trigger: "click",
        item_diameter: 50,
        circle_radius: 150,
        direction: 'top'
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
    translateNavbar:function(){
      $(".navbar").i18n();
    },
    updateLocale:function(locale) {
      this.locale = locale;

      $.i18n.init({ lng : this.locale }).done(function() {
        appRouter.navigate("reset", { trigger : true });
        $("body").i18n();
      });
    }
  });

  module.exports = Router;

});
