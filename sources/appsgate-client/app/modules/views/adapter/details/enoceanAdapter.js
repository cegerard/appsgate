define([
  "app",
    "views/device/devicebytype",
    "views/adapter/details/adaptersView",
    "text!templates/adapters/details/enocean/enoceanAdapter.html",
    "text!templates/adapters/details/enocean/enoceanPairingButton.html",
    "text!templates/adapters/details/enocean/enoceanNoItem.html",
    "text!templates/adapters/details/enocean/enoceanItem.html",
    "text!templates/adapters/details/enocean/enoceanUndefinedItem.html",
    "text!templates/adapters/details/enocean/enoceanCapability.html"



  ], function(App, DeviceView, AdaptersView, EnOceanAdapterTemplate, EnOceanPairingTemplate, EnOceanNoItemTemplate,
              EnOceanItemTemplate, EnOceanUndefinedItemTemplate, EnOceanCapabilityTemplate) {

    var EnOceanAdapterView = {};
    EnOceanAdapterView = AdaptersView.extend({
        tplEnocean: _.template(EnOceanAdapterTemplate),
        tplItem: _.template(EnOceanItemTemplate),
        tplUndefinedItem: _.template(EnOceanUndefinedItemTemplate),
        tplNoItem: _.template(EnOceanNoItemTemplate),
        tplCapability: _.template(EnOceanCapabilityTemplate),
        tplPairing: _.template(EnOceanPairingTemplate),

        // map the events and their callback
        events: {
            "click button.btn-toggle-pairing": "togglePairing",
            "click button.btn-unpair": "unpair",
            "click button.btn-validate": "validate"
        },

      initialize: function() {
        var self = this;

          this.model.on("pairingModeChanged", this.renderPairingButton, this);
          this.model.on("itemsChanged", this.render, this);
          this.model.on("undefinedItemsChanged", this.render, this);

          EnOceanAdapterView.__super__.initialize.apply(this, arguments);
        $.extend(self.__proto__.events, EnOceanAdapterView.__super__.events);
      },


      autoupdate: function() {
          EnOceanAdapterView.__super__.autoupdate.apply(this);

        // translate the view
        this.$el.i18n();
      },

        unpair:function(event) {
            var id = event.currentTarget.attributes.getNamedItem("device-id").value;

            console.log("unpair enocean device : ", id);
            this.model.unpair(id)

        },

        togglePairing: function() {
            var oldMode = this.model.get("pairingMode");

            if(oldMode === "true") {
                this.model.setPairingMode(false);
            } else {
                this.model.setPairingMode(true);

            }
        },
        validate: function (event) {
            var id = event.currentTarget.attributes.getNamedItem("device-id").value;
            console.log("Validate enocean device : ", id);
            var value = $(".select-enocean-profile[device-id='"+id+"']").val();
            console.log(" -> with profile : ",value);
            this.model.validate(id, value);
        },

        renderUndefinedItems: function() {
            var tmp = this.model.get("undefinedItems");
            var items = [];
            if( typeof tmp=="string") {
                var items = JSON.parse(tmp);
            } else {
                items = tmp;
            }
            console.log("rendering undefined items list :", items);

            $(".list-enocean-undefined-items").empty();
            for (var id in items) {
                console.log("rendering item id :", id);
                $(".list-enocean-undefined-items").append(this.tplUndefinedItem({
                    id: id
                }));

                var capabilities = items[id];
                if (capabilities !== undefined) {
                    for( i=0; i< capabilities.length; i++) {
                        console.log("Adding capability : ", capabilities[i]);
                        $(".select-enocean-profile[device-id='"+id+"']").append(this.tplCapability({
                            type: capabilities[i].type,
                            profile: capabilities[i].profile
                        }));
                    }
                }
            }

            $(".list-enocean-undefined-items").i18n();
        },

        renderItems: function() {
            var tmp = this.model.get("items");
            var items = [];
            if( typeof tmp=="string") {
                var items = JSON.parse(tmp);
            } else {
                items = tmp;
            }

            console.log("rendering paired items list :", items);


            $(".list-enocean-items").empty();
            if(items.length == 0) {
                console.log("No device paired");
                $(".list-enocean-items").append(this.tplNoItem({
                }));
            } else {
                for (var i = 0; i < items.length; i++) {
                    console.log("rendering item number :", i);
                    var item = devices.findWhere({id: items[i]});
                    if (item !== undefined) {
                        console.log("Adding known item : ", item);

                        var deviceView = new DeviceView(this);
                        var template = "<div>" + deviceView.createTemplate(item.get("type")) + "</div>";
                        var typeName = $(".lead span", template).attr("data-i18n");


                        $(".list-enocean-items").append(this.tplItem({
                            id: item.get("id"),
                            typeName: typeName,
                            name: item.get("name")
                        }));
                    } else {
                        console.log("Adding unknown item (waiting for further description) ");
                        $(".list-enocean-items").append(this.tplItem({
                            id: items[i],
                            typeName: "Unknown",
                            name: "Unknown"
                        }));

                    }
                }
            }
            $(".list-enocean-items").i18n();
        },

        renderPairingButton: function(pairingMode) {
            var pairingMode = this.model.get("pairingMode");
            console.log("rendering pairing button state : ", pairingMode);

            $(".adapter-controls").empty();

            $(".adapter-controls").html(this.tplPairing({
                pairingMode: pairingMode
            }));

            $(".adapter-controls").i18n();
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
                  adapterImg: ["app/img/sensors/enocean-usb.jpg"],
                  editable : false,
                  adapterDetails: this.tplEnocean
              }));
          }
          this.renderItems();
          this.renderUndefinedItems();

          this.renderPairingButton();

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
      }
    });
    return EnOceanAdapterView
  });
