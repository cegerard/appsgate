define([
  "app",
    "views/device/devicebytype",
    "views/adapter/details/adaptersView",
    "text!templates/adapters/details/enocean/enoceanAdapter.html",
    "text!templates/adapters/details/enocean/enoceanItem.html"


  ], function(App, DeviceView, AdaptersView, EnOceanAdapterTemplate, EnOceanItemTemplate) {

    var EnOceanAdapterView = {};
    EnOceanAdapterView = AdaptersView.extend({
        tplEnocean: _.template(EnOceanAdapterTemplate),
        tplItem: _.template(EnOceanItemTemplate),

        // map the events and their callback
        events: {

        },

      initialize: function() {
        var self = this;

          EnOceanAdapterView.__super__.initialize.apply(this, arguments);

        $.extend(self.__proto__.events, EnOceanAdapterView.__super__.events);
      },


      autoupdate: function() {
          EnOceanAdapterView.__super__.autoupdate.apply(this);

        // translate the view
        this.$el.i18n();
      },


        renderItems: function(items) {

            $(".list-enocean-items").empty();
            for(var i=0; i<items.length; i++) {
                var item = devices.findWhere({id: items[i]});
                var deviceView = new DeviceView(this);
                var template = "<div>"+deviceView.createTemplate(item.get("type"))+"</div>";
                var typeName = $(".lead span",template).attr("data-i18n");




                $(".list-enocean-items").append(this.tplItem({
                    id: item.get("id"),
                    typeName : typeName,
                    name : item.get("name")
                }));
            }
            $(".list-enocean-items").i18n();
        },
      /**
      * Render the detailed view of the adapter
      */
      render: function() {

          var self = this;

          if (!appRouter.isModalShown) {
              this.$el.html(this.tpl({
                  type: this.id,
                  adapter: this.model,
                  adapterDetails: this.tplEnocean
              }));
          }
          this.renderItems(this.model.get("items"));

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
      }
    });
    return EnOceanAdapterView
  });
