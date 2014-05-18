define([
    "app",
    "views/service/menu",
    "views/service/servicebytype",
    "views/service/details"
], function(App, ServiceMenuView, ServicesByTypeView, ServiceDetailsView) {

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

        // set active the first element - displayed by default
        $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).addClass("active");

        // display the first category of services
        var typeId = $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).attr("id").split("side-")[1];
        appRouter.showDetailsView(new ServicesByTypeView({id: typeId}));

        // update the url
        appRouter.navigate("#services/types/" + typeId);

        $(".breadcrumb").html("<li><a href='#home'><span data-i18n='navbar.home'/></a></li>");
        $(".breadcrumb").append("<li class='active'><span data-i18n='navbar.services'/></li>");
        appRouter.translateNavbar();
    },
    /**
     * Display all the services of a given type
     *
     * @param typeId id of the service category to show
     */
    serviceByType: function(typeId) {
        appRouter.showDetailsView(new ServicesByTypeView({id: typeId}));

        $(".breadcrumb").html("<li><a href='#home'><span data-i18n='navbar.home'/></a></li>");
        $(".breadcrumb").append("<li class='active'><span data-i18n='navbar.services'/></li>");
        appRouter.translateNavbar();
    },
    /**
     * Show the details of a service
     *
     * @method details
     * @param id Id of the service to show
     */
    details: function(id) {
        appRouter.showDetailsView(new ServiceDetailsView({model: services.get(id)}));

        $(".breadcrumb").html("<li><a href='#home'><span data-i18n='navbar.home'/></a></li>");
        $(".breadcrumb").append("<li><a href='#services'><span data-i18n='navbar.services'/></a></li>");
        $(".breadcrumb").append("<li class='active'><span>" + services.get(id).get("name") + "</span></li>");
        appRouter.translateNavbar();
    }
});
return ServiceRouter;
});
