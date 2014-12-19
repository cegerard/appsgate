define([
    "app",
    "text!templates/services/menu/menu.html",
    "text!templates/services/menu/serviceContainer.html"
], function(App, serviceMenuTemplate, serviceContainerMenuTemplate) {

    var ServiceMenuView = {};
    /**
     * Render the side menu for the services
     */
    ServiceMenuView = Backbone.View.extend({
        tpl: _.template(serviceMenuTemplate),
        tplServiceContainer: _.template(serviceContainerMenuTemplate),
        /**
         * Bind events of the DOM elements from the view to their callback
         */
        events: {
            "click a.list-group-item": "updateSideMenu"
        },
		/**
		* Attributes to know the type of this menu
		*/
		attributes: {
			"class": "ServiceMenuView"
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
            this.render();
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
                    if (services.get(serviceId)) {
                        $("#side-" + services.get(serviceId).get("type")).addClass("active");
                    }
                }
            }
        },
        /**
         * Render the side menu
         */
        render: function() {
            if (!appRouter.isModalShown) {
                var self = this;

                // initialize the content
                this.$el.html(this.tpl());
                
                // for each category of services, add a menu item
                this.$el.append(this.tpl());
                var types = services.getServicesByType();
                var container = document.createDocumentFragment();
                _.forEach(_.keys(types), function(type) {
                  if (type !== "36") {
                    $(container).append(self.tplServiceContainer({
                        type: type,
                        services: types[type],
                        places: places,
                        unlocatedServices: services.filter(function(d) {
                            return (d.get("placeId") === "-1" && d.get("type") === type);
                        }),
                        active: Backbone.history.fragment.split("services/types/")[1] === type ? true : false
                    }));
                  }
                });

                var serviceGroups = $(container).children();

                serviceGroups.sort(function(a, b) {
                  return $($(a).children(".list-group-item-heading").children(":first")[0]).i18n().text().toUpperCase().localeCompare($($(b).children(".list-group-item-heading").children(":first")[0]).i18n().text().toUpperCase());
                });

                $.each(serviceGroups, function(idx, itm) {
                    $(self.$el.find(".list-group")[1]).append(itm);
                });

                $(self.$el.find(".list-group")[1]).addClass("scrollable-menu");

                // set active the current item menu
                this.updateSideMenu();

                // translate the view
                this.$el.i18n();

                // resize the menu
                this.resize(self.$el.find(".scrollable-menu"));

                return this;
            }
        }
    });
    return ServiceMenuView;
});
