define([
    "app",
    "views/device/menu",
    "views/device/devicebytype",
    "views/device/details"
], function(App, DeviceMenuView, DevicesByTypeView, DeviceDetailsView) {

    var DeviceRouter = {};
/**
 * Router to handle the routes for the devices
 *
 * @class Device.Router
 */
DeviceRouter = Backbone.Router.extend({
    // define the routes for the devices
    routes: {
        "devices": "list",
        "devices/types/:id": "deviceByType",
        "devices/:id": "details"
    },
    /**
     * @method list Show the list of devices
     */
    list: function() {
        // display the side menu
        appRouter.showMenuView(new DeviceMenuView());

        // set active the first element - displayed by default
        $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).addClass("active");

        // display the first category of devices
        var typeId = $($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).attr("id").split("side-")[1];
        appRouter.showDetailsView(new DevicesByTypeView({id: typeId}));

        // update the url
        appRouter.navigate("#devices/types/" + typeId);

        $(".breadcrumb").html("<li><a href='#home'><span data-i18n='navbar.home'/></a></li>");
        $(".breadcrumb").append("<li class='active'><span data-i18n='navbar.devices'/></li>");
        appRouter.translateNavbar();
    },
    /**
     * Display all the devices of a given type
     *
     * @param typeId id of the device category to show
     */
    deviceByType: function(typeId) {
        appRouter.showDetailsView(new DevicesByTypeView({id: typeId}));

        $(".breadcrumb").html("<li><a href='#home'><span data-i18n='navbar.home'/></a></li>");
        $(".breadcrumb").append("<li class='active'><span data-i18n='navbar.devices'/></li>");
        appRouter.translateNavbar();
    },
    /**
     * Show the details of a device
     *
     * @method details
     * @param id Id of the device to show
     */
    details: function(id) {
        appRouter.showDetailsView(new DeviceDetailsView({model: devices.get(id)}));
        $(".breadcrumb").html("<li><a href='#home'><span data-i18n='navbar.home'/></a></li>");
        $(".breadcrumb").append("<li><a href='#devices'><span data-i18n='navbar.devices'/></a></li>");
        $(".breadcrumb").append("<li class='active'><span>" + devices.get(id).get("name") + "</span></li>");
        appRouter.translateNavbar();
    }
});
return DeviceRouter;
});
