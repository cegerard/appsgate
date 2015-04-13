define([
  "app",
  "text!templates/adapters/details/adapterContainer.html"

], function(App, adapterContainerTemplate) {

    var AdaptersView = {};
    /**
    * Render the list of devices of a given type
    */
    AdaptersView = Backbone.View.extend({
      tpl: _.template(adapterContainerTemplate),

      events: {
      },

      /**
      * Listen to the updates on the devices of the category and refresh if any
      *
      * @constructor
      */
      initialize: function() {
        var self = this;
        self.listenTo(adapters, "add", self.reload);
        adapters.models.forEach(function(adapter) {
           // self.listenTo(adapter, "change", self.autoupdate);
           // self.listenTo(adapter, "remove", self.render);
        });
      },
      autoupdate: function(device) {
          this.render();
      },

      reload: function() {


        this.render();
      },
      /**
      * Render the list
      */
      render: function() {

        if (!appRouter.isModalShown) {
          this.$el.html(this.tpl({
            type: this.id,
            adapter: adapters.findWhere({type: this.id})
          }));

          // translate the view
          this.$el.i18n();

          // resize the list
          this.resize($(".scrollable"));

          return this;
        }
        return null;
      }
    });
    return AdaptersView;
  });
