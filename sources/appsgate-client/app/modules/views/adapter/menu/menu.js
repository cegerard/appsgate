define([
  "app",
  "text!templates/adapters/menu/menu.html",
  "text!templates/adapters/menu/adapterMenuContainer.html"

  ], function(App, AdapterMenuTemplate, AdapterMenuContainerTemplate) {

    var AdapterMenuView = {};
    /**
    * Render the side menu for the adapters
    */
    AdapterMenuView = Backbone.View.extend({
      tpl: _.template(AdapterMenuTemplate),
      tplAdapterMenuContainer: _.template(AdapterMenuContainerTemplate),
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
		"class": "AdapterMenuView"
	  },
      /**
      * Listen to the updates on adapters and update if any
      *
      * @constructor
      */
      initialize: function() {
        this.listenTo(adapters, "add", this.render);
        this.listenTo(adapters, "change", this.render);
        this.listenTo(adapters, "remove", this.render);

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
          if (Backbone.history.fragment === "adapters") {
            $($(".navbar li")[0]).addClass("active");
          } else if (Backbone.history.fragment.split("/")[1] === "types") {
            $("#side-" + Backbone.history.fragment.split("/")[2]).addClass("active");
          } else {
            var adapterId = Backbone.history.fragment.split("/")[1];
            $("#side-" + adapters.get(adapterId).get("type")).addClass("active");
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

          // for each category of adapter, add a menu item
          this.$el.append(this.tpl());

          var container = document.createDocumentFragment();
          _.forEach(adapters.models, function(adapter) {
              $(container).append(self.tplAdapterMenuContainer({
                adapters: adapters,
                type: adapter.get("type")
              }));
          });

          var adapterGroups = $(container).children();

          adapterGroups.sort(function(a, b) {
            return $($(a).children(".list-group-item-heading").children(":first")[0]).i18n().text().toUpperCase().localeCompare($($(b).children(".list-group-item-heading").children(":first")[0]).i18n().text().toUpperCase());
          });

          $.each(adapterGroups, function(idx, itm) {
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
    return AdapterMenuView;
  });
