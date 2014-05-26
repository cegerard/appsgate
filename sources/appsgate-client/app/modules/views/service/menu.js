define([
    "app",
    "text!templates/services/menu/menu.html",
    "text!templates/services/menu/serviceContainer.html",
    "text!templates/devices/menu/coreClockContainer.html"
], function(App, serviceMenuTemplate, serviceContainerMenuTemplate, coreClockContainerMenuTemplate) {

    var ServiceMenuView = {};
    /**
     * Render the side menu for the services
     */
    ServiceMenuView = Backbone.View.extend({
        tpl: _.template(serviceMenuTemplate),
        tplServiceContainer: _.template(serviceContainerMenuTemplate),
        tplCoreClockContainer: _.template(coreClockContainerMenuTemplate),
        /**
         * Bind events of the DOM elements from the view to their callback
         */
        events: {
            "click a.list-group-item": "updateSideMenu"
        },
        /**
         * Listen to the updates on services and update if any
         * 
         * @constructor
         */
        initialize: function() {
            this.listenTo(services, "add", this.render);
            this.listenTo(services, "change", this.onChangedService);
            this.listenTo(services, "remove", this.render);
        },
        /**
         * Method called when a service has changed
         * @param model Model that changed, Service in that cas
         * @param collection Collection that holds the changed model
         * @param options Options given with the change event
         */
        onChangedService: function(model, options) {
            // a service has changed
            // if it's the clock, we refresh the clock only
            if (typeof options !== "undefined" && options.clockRefresh) {
                this.refreshClockDisplay();
            }
            // otherwise we rerender the whole view
            else {
                this.render();
            }
        },
        /**
         * Update the side menu to set the correct active element
         * 
         * @param e JS click event
         */
        updateSideMenu: function(e) {
            _.forEach($("a.list-group-item"), function(item) {
                $(item).removeClass("active");
            });

            if (typeof e !== "undefined") {
                $(e.currentTarget).addClass("active");
            } else {
                if (Backbone.history.fragment === "services") {
                    $($(".navbar li")[0]).addClass("active");
                } else if (Backbone.history.fragment.split("/")[1] === "types") {
                    $("#side-" + Backbone.history.fragment.split("/")[2]).addClass("active");
                } else {
                    var serviceId = Backbone.history.fragment.split("/")[1];
                    $("#side-" + services.get(serviceId).get("type")).addClass("active");
                }
            }
        },
        /**
         * Refreshes the time display without rerendering the whole screen
         */
        refreshClockDisplay: function() {

            //remove existing node
            $(this.$el.find(".list-group")[0]).children().remove();

            //refresh the clock
            $(this.$el.find(".list-group")[0]).append(this.tplCoreClockContainer({
                service: services.getCoreClock(),
                active: Backbone.history.fragment === "services/" + services.getCoreClock().get("id") ? true : false
            }));

        },
        /**
         * Render the side menu
         */
        render: function() {
            if (!appRouter.isModalShown) {
                var self = this;

                // initialize the content
                this.$el.html(this.tpl());

                // display the clock
                $(this.$el.find(".list-group")[0]).append(this.tplCoreClockContainer({
                    device: devices.getCoreClock(),
                    active: Backbone.history.fragment === "services/" + devices.getCoreClock().get("id") ? true : false
                }));

                // for each category of services, add a menu item
                this.$el.append(this.tpl());
                var types = services.getServicesByType();
                _.forEach(_.keys(types), function(type) {
                    $(self.$el.find(".list-group")[1]).append(self.tplServiceContainer({
                        type: type,
                        services: types[type],
                        places: places,
                        unlocatedServices: services.filter(function(d) {
                            return (d.get("placeId") === "-1" && d.get("type") === type);
                        }),
                        active: Backbone.history.fragment.split("services/types/")[1] === type ? true : false
                    }));
                });

                // set active the current item menu
                this.updateSideMenu();

                // translate the view
                this.$el.i18n();

                // resize the menu
                this.resizeDiv($(self.$el.find(".list-group")[1]));

                return this;
            }
        }
    });
    return ServiceMenuView;
});