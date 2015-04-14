define([
  "app",
    "views/adapter/details/adaptersView",
    "text!templates/services/details/weather.html"




  ], function(App, AdaptersView, WeatherDetailsTemplate) {

    var WeatherAdapterView = {};
    WeatherAdapterView = AdaptersView.extend({

        tplWeather: _.template(WeatherDetailsTemplate),

        // map the events and their callback
        events: {
            "click button.btn-toggle-pairing": "togglePairing",
            "click button.btn-unpair": "unpair",
            "click button.btn-validate": "validate"
        },

      initialize: function() {
        var self = this;


          WeatherAdapterView.__super__.initialize.apply(this, arguments);
        $.extend(self.__proto__.events, WeatherAdapterView.__super__.events);
      },


      autoupdate: function() {
          WeatherAdapterView.__super__.autoupdate.apply(this);

        // translate the view
        this.$el.i18n();
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
                  editable : false,
                  adapterImg: ["app/img/sensors/yahoo_weather.png"],
                  adapterDetails: this.tplWeather
              }));
          }

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
      }
    });
    return WeatherAdapterView
  });
