define([
    "app",
    "views/place/menu",
    "views/place/details"
], function(App, PlaceMenuView, PlaceDetailsView) {

    var PlaceRouter = {};
    // router
    PlaceRouter = Backbone.Router.extend({
        routes: {
            "places": "list",
            "places/:id": "details"
        },
        // list all the places
        list: function() {
            // display the side menu
            appRouter.showMenuView(new PlaceMenuView());

            // set active the first element - displayed by default
            $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).addClass("active");

            // display the first place
            appRouter.showDetailsView(new PlaceDetailsView({model: places.get("-1")}));

            // update the url
            appRouter.navigate("#places/-1");

            $(".breadcrumb").html("<li><a href='#home'><span data-i18n='navbar.home'/></a></li>");
            $(".breadcrumb").append("<li class='active'><span data-i18n='navbar.places'/></li>");
            appRouter.translateNavbar();
        },
        // show the details of a places (i.e. list of devices in this place)
        details: function(id) {
            appRouter.showDetailsView(new PlaceDetailsView({model: places.get(id)}));
            $(".breadcrumb").html("<li><a href='#home'><span data-i18n='navbar.home'/></a></li>");
            $(".breadcrumb").append("<li><a href='#places'><span data-i18n='navbar.places'/></a></li>");
            $(".breadcrumb").append("<li class='active'><span>" + places.get(id).get("name") + "</span></li>");
            appRouter.translateNavbar();
        }
    });
    return PlaceRouter;
});
