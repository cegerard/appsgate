define([
    "app",
    "text!templates/services/list/servicesListByCategory.html",
    "text!templates/services/list/mail.html",
    "text!templates/services/details/tts/tts.html",
    "models/service/weather"
], function(App, serviceListByCategoryTemplate, mailTemplate,ttsTemplate,  Weather) {

    var ServiceByTypeView = {};
    /**
     * Render the list of services of a given type
     */
    ServiceByTypeView = Backbone.View.extend({
        tpl: _.template(serviceListByCategoryTemplate),
        mailTpl: _.template(mailTemplate),
        ttsTpl: _.template(ttsTemplate),
        events: {
            "keyup #add-weather-modal input": "validWeatherName",
            "click #add-weather-modal button.valid-button": "addWeatherName",
            "keyup #edit-mail-modal input": "validMail",
            "click #edit-mail-modal button.valid-button": "updateMail",
            "click button.see-meteo": "openMeteo",
            "click button.update-mail" : "updateMailButton",
            "click button.delete-weather-button": "onDeleteWeatherButton",
            "click button.delete-weather": "onClickDeleteWeather",
            "click button.delete-mail": "onClickDeleteMail",
            "click button.delete-mail-button": "onDeleteMailButton",
            "click button.cancel-delete-mail-button": "onCancelDeletePopover",
            "click button.cancel-delete-weather-button": "onCancelDeletePopover",
            "shown.bs.modal #edit-mail-modal": "initializeMailModal",
            "shown.bs.modal #add-weather-modal": "initializeWeatherModal"

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

            self.listenTo(services, "add", self.reload);

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
                                $( "#weatherInput" ).val( ui.item.label );
                                $( "#WOEID" ).val( ui.item.woeid );
                                $( "#NAME" ).val( ui.item.name );
                                return false;
                            },
                            focus: function( event, ui ) {
                                $("#add-weather-modal .valid-button").removeClass("disabled");
                                $("#add-weather-modal .valid-button").removeClass("valid-disabled");
                                $( "#weatherInput" ).val( ui.item.label );
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
        * Clear the input text, hide the error message and disable the valid button by default
        */
        initializeMailModal: function() {
          $("#edit-mail-modal input#mailInput").focus();
        },
        initializeWeatherModal: function() {
          $("#add-weather-modal input#weatherInput").focus();
        },
        reload: function() {
            self = this;
          services.getServicesByType()[this.id].forEach(function(service) {
              self.listenTo(service, "change", self.render);
              self.listenTo(service, "remove", self.render);
          });

          this.render();
        },
        /**
         * Render the list
         */
        render: function() {
            if (!appRouter.isModalShown) {
                if (this.id === "102") {
                    this.$el.html(this.mailTpl({
                        mail: services.getServicesByType()[this.id][0]
                    }));
                } else if (this.id === "104") {
                    this.$el.html(this.ttsTpl({
                        tts: services.getServicesByType()[this.id][0]
                    }));
                } else {
                    this.$el.html(this.tpl({
                        type: this.id,
                        places: places
                    }));
                }

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
                adapters.getWeatherAdapter().checkLocationsStartingWith(loc);
            } else {
                $( "#WOEID" ).val( "" );
                $("#add-weather-modal .valid-button").addClass("disabled");
                $("#add-weather-modal .valid-button").addClass("valid-disabled");
            }
        },

        addWeatherName: function() {
            // instantiate the place and add it to the collection after the modal has been hidden
            $("#add-weather-modal").on("hidden.bs.modal", function() {
                // instantiate a model for the new location observer
                var loc = $("#add-weather-modal input[name='name']").val();
//                var weather = new Weather({location	: loc,
// id	: 'WeatherObserver-'+Math.round(Math.random() * 10000).toString(), name : loc, type :"103"});

                adapters.getWeatherAdapter().addLocationObserver(loc);

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
            var weatherObserver = services.get($(e.currentTarget).parents(".pull-right").children(".delete-weather").attr("brickid"));
            weatherObserver.destroy();
            appRouter.navigate("#services/types/103", {trigger: true});

        },
        /**
          * Callback when the user has clicked on the button to cancel the deleting or click out of the popover.
          */
        onCancelDeletePopover : function() {
            // destroy the popover
            this.$el.find(".delete-weather").popover('destroy');
        },
        /**
          * Callback when the user has clicked on the button delete.
          */
        onClickDeleteWeather : function(e) {
            var self = this;
            // create the popover
            var weatherObserverID = $(e.currentTarget).parents(".pull-right").children(".delete-weather").attr("id");
            this.$el.find("#" + weatherObserverID).popover({
                html: true,
                title: $.i18n.t("services.weather.warning-weather-delete"),
                content: "<div class='popover-div'><button type='button' class='btn btn-default cancel-delete-weather-button'>" + $.i18n.t("form.cancel-button") + "</button><button type='button' class='btn btn-danger delete-weather-button'>" + $.i18n.t("form.delete-button") + "</button></div>",
                placement: "bottom"
            });

            // listen the hide event to destroy the popup, because it is created to every click on Edit
            this.$el.find("#" + weatherObserverID).on('hidden.bs.popover', function () {
                self.onCancelDeletePopover();
            });
            // show the popup
            this.$el.find("#" + weatherObserverID).popover('show');
        },
        /**
         *
         */
        openMeteo: function(e) {
            e.preventDefault();
            var actuator = services.get($(e.currentTarget).attr("brickid"));
            window.open(actuator.attributes.presentationURL);
        },
                /**
          * Callback when the user has clicked on the button delete.
          */
        onClickDeleteMail : function(e) {
            e.preventDefault();
            var self = this;
            // create the popover
            var id = $(e.currentTarget).parents(".pull-right").children(".delete-mail").attr("id");
            this.$el.find("#" + id).popover({
                html: true,
                title: $.i18n.t("services.mail.warning-delete"),
                content: "<div class='popover-div'><button type='button' class='btn btn-default cancel-delete-mail-button'>" + $.i18n.t("form.cancel-button") + "</button><button type='button' class='btn btn-danger delete-mail-button'>" + $.i18n.t("form.delete-button") + "</button></div>",
                placement: "bottom"
            });
            // listen the hide event to destroy the popup, because it is created to every click on Edit
            this.$el.find("#" + id).on('hidden.bs.popover', function () {
                self.onCancelDeletePopover();
            });
            // show the popup
            this.$el.find("#" + id).popover('show');
        },
        /**
         * Callback to delete favorite mail
         */
        onDeleteMailButton: function(e) {
            mail = services.getServicesByType()["102"][0];
            mail.removeFavorite($(e.currentTarget).parents(".pull-right").children(".delete-mail").attr("email"));
            $("#mailFavCnt").html(mail.getNumberOfFavorites());
            this.reload();

        },

        /**
         *
         */
        updateMailButton : function(e) {
            var m = $(e.currentTarget).parents(".pull-right").children(".delete-mail").attr("email");
            $("#edit-mail-modal input[name='oldValue']").val(m);
            $("#edit-mail-modal input[name='inputValue']").val(m);
            $("#edit-mail-modal").modal("show");

        },
        /**
         *
         */
        validMail: function(e) {
            var mail = $("#edit-mail-modal input[name='inputValue']").val();
            var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
            if (regex.test(mail)) {
                $("#edit-mail-modal .valid-button").removeClass("disabled");
                $("#edit-mail-modal .valid-button").removeClass("valid-disabled");
                $("#edit-mail-modal .text-danger").addClass("hide");
            } else {
                $("#edit-mail-modal .valid-button").addClass("disabled");
                $("#edit-mail-modal .valid-button").addClass("valid-disabled");
                $("#edit-mail-modal .text-danger").removeClass("hide");
            }
        },

        updateMail: function() {
            mail = services.getServicesByType()["102"][0];
            self = this;

            $("#edit-mail-modal").on("hidden.bs.modal", function() {
                // instantiate a model for the new location observer
                var oldMail = $("#edit-mail-modal input[name='oldValue']").val();
                var newMail = $("#edit-mail-modal input[name='inputValue']").val();
                $("#edit-mail-modal input[name='oldValue']").val("");

                mail.updateFavorite(oldMail, newMail);


                // tell the router that there is no modal any more
                appRouter.isModalShown = false;
                $("#mailFavCnt").html(mail.getNumberOfFavorites());
                self.reload();
            });

            // hide the modal

            $("#edit-mail-modal").modal("hide");
        }

    });
    return ServiceByTypeView;
});
