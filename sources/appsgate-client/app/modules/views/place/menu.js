define([
    "app",
    "models/place",
    "text!templates/places/menu/menu.html",
    "text!templates/places/menu/placeContainer.html",
    "text!templates/places/menu/addButton.html"
], function(App, Place, placeMenuTemplate, placeContainerMenuTemplate, addPlaceButtonTemplate) {

    var PlaceMenuView = {};
    /**
     * Render the side menu for the places
     */
    PlaceMenuView = Backbone.View.extend({
        tpl: _.template(placeMenuTemplate),
        tplPlaceContainer: _.template(placeContainerMenuTemplate),
        tplAddPlaceButton: _.template(addPlaceButtonTemplate),
        /**
         * Bind events of the DOM elements from the view to their callback
         */
        events: {
            "click a.list-group-item": "updateSideMenu",
            "shown.bs.modal #add-place-modal": "initializeModal",
            "hide.bs.modal #add-place-modal": "toggleModalValue",
            "click #add-place-modal button.valid-button": "validEditName",
            "keyup #add-place-modal input": "validEditName"
        },
		/**
		* Attributes to know the type of this menu
		*/
		attributes: {
			"class": "PlaceMenuView"
		},
        /**
         * Listen to the places collection update and refresh if any
         *
         * @constructor
         */
        initialize: function() {
            this.listenTo(places, "add", this.render);
            this.listenTo(places, "change", this.render);
            this.listenTo(places, "remove", this.render);
            this.listenTo(devices, "change", this.autoupdate);
            this.listenTo(devices, "remove", this.autoupdate);

            this.stopListening(devices.getCoreClock());
        },
        /**
         * Method called when a device has changed
         * @param model Model that changed, Device in that cas
         * @param collection Collection that holds the changed model
         * @param options Options given with the change event
         */
        autoupdate: function(model) {
            var place = places.get(model.get("placeId"));
            var placeId = "";
            if(Backbone.history.fragment.split("/")[1] !== "") {
              placeId = Backbone.history.fragment.split("/")[1];
              if(typeof devices.get(placeId) !== "undefined") {
                // when a device is displayed, making active the place it is in
                placeId = devices.get(placeId).get("placeId");
              }
            }
            this.$el.find("#place-" + place.get("id")).replaceWith(this.tplPlaceContainer({
                place: place,
                active: place.get("id") == placeId?true:false
            }));

            // translate the view
            this.$el.i18n();
        },
        /**
         * Update the side menu to set the correct active element
         *
         * @param e JS click event
         */
        updateSideMenu: function(e) {
          // setting the element active based on click or url
          if (typeof e !== "undefined") {
            // reset selected item
            _.forEach($("a.list-group-item"), function(item) {
              $(item).removeClass("active");
            });

            $(e.currentTarget).addClass("active");
          }
        },
        /**
         * Clear the input text, hide the error message and disable the valid button by default
         */
        initializeModal: function() {
            $("#add-place-modal input").val("");
            $("#add-place-modal input").focus();

            $("#add-place-modal .text-danger").addClass("hide");
            $("#add-place-modal .valid-button").addClass("disabled");
            $("#add-place-modal .valid-button").addClass("valid-disabled");

            // the router that there is a modal
            appRouter.isModalShown = true;
        },
        /**
         * Tell the router there is no modal anymore
         */
        toggleModalValue: function() {
            appRouter.isModalShown = false;
        },
        /**
         * Check the current value of the input text and show an error message if needed
         *
         * @return false if the typed name already exists, true otherwise
         */
        checkPlace: function() {
            // Check the length of the name
            if ($("#add-place-modal input").val().length > App.MAX_NAME_LENGTH ) {
                $("#add-place-modal .text-danger")
                        .text($.i18n.t("modal.name-too-long"))
                        .removeClass("hide");
                $("#add-place-modal .valid-button").addClass("disabled");
                $("#add-place-modal .valid-button").addClass("valid-disabled");

                return false;
            }

            // name is empty
            if ($("#add-place-modal input").val() === "") {
                $("#add-place-modal .text-danger")
                        .text($.i18n.t("modal-add-place.place-name-empty"))
                        .removeClass("hide");
                $("#add-place-modal .valid-button").addClass("disabled");
                $("#add-place-modal .valid-button").addClass("valid-disabled");

                return false;
            }
            // name contains html code
            if (/(&|>|<)/.test($("#add-place-modal input:text").val())) {
                $("#add-place-modal .text-danger")
                        .text($.i18n.t("edit-name-modal.contains-html"))
                        .removeClass("hide");
                $("#add-place-modal .valid-button").addClass("disabled");
                $("#add-place-modal .valid-button").addClass("valid-disabled");

                return false;
            }

            // name already exists
            if (places.where({name: $("#add-place-modal input").val()}).length > 0) {
                $("#add-place-modal .text-danger")
                        .text($.i18n.t("modal-add-place.place-already-existing"))
                        .removeClass("hide");
                $("#add-place-modal .valid-button").addClass("disabled");
                $("#add-place-modal .valid-button").addClass("valid-disabled");

                return false;
            }

            // ok
            $("#add-place-modal .text-danger").addClass("hide");
            $("#add-place-modal .valid-button").removeClass("disabled");
            $("#add-place-modal .valid-button").removeClass("valid-disabled");

            return true;
        },
        /**
         * Check if the name of the place does not already exist. If not, update the place
         * Hide the modal when done
         *
         * @param e JS event
         */
        validEditName: function(e) {
            if (e.type === "keyup" && e.keyCode === 13 || e.type === "click") {
                // create the place if the name is ok
                if (this.checkPlace()) {

                    // instantiate the place and add it to the collection after the modal has been hidden
                    $("#add-place-modal").on("hidden.bs.modal", function() {
                        // instantiate a model for the new place
                        var place = new Place({name	: $("#add-place-modal input").val(), devices	: []});

                        // send the place to the backend
                        place.save();

                        // tell the router that there is no modal any more
                        appRouter.isModalShown = false;
                    });

                    // hide the modal
                    $("#add-place-modal").modal("hide");
                }
            } else if (e.type === "keyup") {
                this.checkPlace();
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

                // "add place" button to the side menu
                this.$el.append(this.tplAddPlaceButton());

                // for each place, add a menu item
                this.$el.append(this.tpl());

                places.forEach(function(place) {
                    if (place.get("id") !== "-1") {
                        $(self.$el.find(".list-group")[1]).append(self.tplPlaceContainer({
                            place: place,
                            active: Backbone.history.fragment.split("/")[1] === place.get("id") ? true : false
                        }));
                    }
                });

                // put the unlocated devices into a separate group list
                $(this.$el.find(".list-group")[1]).append(this.tplPlaceContainer({
                    place: places.get("-1"),
                    active: Backbone.history.fragment.split("/")[1] === "-1" ? true : false
                }));

                $(self.$el.find(".list-group")[1]).addClass("scrollable-menu");

                // translate the menu
                this.$el.i18n();

                // resize the menu
                this.resize(self.$el.find(".scrollable-menu"));

                return this;
            }
            this.updateSideMenu();
        }
    });
    return PlaceMenuView;
});
