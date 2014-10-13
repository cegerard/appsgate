define([
    "app",
    "text!templates/services/list/servicesListByCategory.html",
    "models/service/weather"
], function(App, serviceListByCategoryTemplate, Weather) {

    var ServiceByTypeView = {};
    /**
     * Render the list of services of a given type
     */
    ServiceByTypeView = Backbone.View.extend({
        tpl: _.template(serviceListByCategoryTemplate),
        events: {
            "keyup #add-weather-modal input": "validWeatherName",
            "click #add-weather-modal button.valid-button": "addWeatherName",
            "click button.see-meteo": "openMeteo",
            "click button.delete-weather-button": "onDeleteWeatherButton",
            "click button.delete-popover": "onClickDeleteWeather",
            "click button.cancel-delete-weather-button": "onCancelDeleteWeather"
        },
        /**
         * Listen to the updates on the services of the category and refresh if any
         *
         * @constructor
         */
        initialize: function() {
            var self = this;

            services.getServicesByType()[this.id].forEach(function(service) {
                self.listenTo(service, "change", self.render);
                self.listenTo(service, "remove", self.render);
            });
            dispatcher.on("checkLocation", function(l) {

                if (l!= undefined ) {
                    for (var i = 0; i < l.length ; i++) {
                        l[i].label = l[i].name + " / " + l[i].country;
                    }
                    $( "#weatherInput" ).autocomplete(
                        {
                            source:l,
                            select: function( event, ui ) {
                                $("#add-weather-modal .valid-button").removeClass("disabled");
                                $("#add-weather-modal .valid-button").removeClass("valid-disabled");
                                $( "#WOEID" ).val( ui.item.woeid );
                                $( "#NAME" ).val( ui.item.name );
                                return false;
                            }
                        }
                    );

            } else {
                    console.warn(l);
                }
            });
        },
        /**
         * Render the list
         */
        render: function() {
            if (!appRouter.isModalShown) {
                this.$el.html(this.tpl({
                    type: this.id,
                    places: places
                }));

                // translate the view
                this.$el.i18n();

                // resize the list
                this.resize($(".scrollable"));

                return this;
            }
            return this;
        },
        /**
         *
         */
        validWeatherName: function(e) {
            var loc = $("#add-weather-modal input[name='inputValue']").val();
            if (loc != undefined && loc.length > 2) {
                //code
                communicator.sendMessage({
                    "method":"checkLocationsStartingWith",
                    "args":[{"type":"String","value":loc}],
                    "callId":"checkLocation",
                    "TARGET":"EHMI"
                    });
            } else {
                $( "#WOEID" ).val( "" );
                $("#add-weather-modal .valid-button").addClass("disabled");
                $("#add-weather-modal .valid-button").addClass("valid-disabled");
            }
        },

        addWeatherName: function() {
            //var loc = $("#add-weather-modal input[name='woeid']").val();
            // instantiate the place and add it to the collection after the modal has been hidden
            $("#add-weather-modal").on("hidden.bs.modal", function() {
                // instantiate a model for the new location observer
                var loc = $("#add-weather-modal input[name='name']").val();
                var weather = new Weather({location	: loc, id	: 'Weather-Observer-'+loc, name : loc});

                weather.save();

                // tell the router that there is no modal any more
                appRouter.isModalShown = false;
                appRouter.navigate("#services/types/103", {trigger: true});
            });

            // hide the modal
            
            $("#add-weather-modal").modal("hide");
        },
        /**
         * Callback to delete the weather place
         */
        onDeleteWeatherButton: function(e) {
            var weatherObserver = services.get($(e.currentTarget).parents(".pull-right").children(".delete-popover").attr("id"));
            weatherObserver.destroy();
            appRouter.navigate("#services/types/103", {trigger: true});

        },
        /**
          * Callback when the user has clicked on the button to cancel the deleting or click out of the popover.
          */
        onCancelDeleteWeather : function() {
            // destroy the popover
            this.$el.find(".delete-popover").popover('destroy');
        },
        /**
          * Callback when the user has clicked on the button delete.
          */
        onClickDeleteWeather : function(e) {
            e.preventDefault();
            var self = this;
            // create the popover
            var weatherObserverID = $(e.currentTarget).parents(".pull-right").children(".delete-popover").attr("id");
            this.$el.find("#" + weatherObserverID).popover({
                html: true,
                title: $.i18n.t("services.weather.warning-weather-delete"),
                content: "<div class='popover-div'><button type='button' class='btn btn-default cancel-delete-weather-button'>" + $.i18n.t("form.cancel-button") + "</button><button type='button' class='btn btn-danger delete-weather-button'>" + $.i18n.t("form.delete-button") + "</button></div>",
                placement: "bottom"
            });
            // listen the hide event to destroy the popup, because it is created to every click on Edit
            this.$el.find("#" + weatherObserverID).on('hidden.bs.popover', function () {
                self.onCancelDeleteWeather();
            });
            // show the popup
            this.$el.find("#" + weatherObserverID).popover('show');
        },
        /**
         *
         */
        openMeteo: function(e) {
            e.preventDefault();
            var actuator = services.get($(e.currentTarget).attr("id"));
            window.open(actuator.attributes.presentationURL);
        }
    });
    return ServiceByTypeView;
});
