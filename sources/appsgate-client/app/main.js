// Break out the application running from the configuration definition to
// assist with testing.
require(["config"], function() {
    // Kick off the application.
    require(["domReady", "app"], function(domReady, app) {

        /**
         * Encapsulate actions to perform before switching the view to avoid ghost views
         * @returns {undefined}
         */
        Backbone.View.prototype.close = function() {
            this.stopListening();
            this.undelegateEvents();
            // check if there is a destroy function on the view
            if(_.isFunction('destroy')) {
                // if yes, call destroy
                this.destroy.apply(this);
            }
            // unbind all the events associated to the view
            this.unbind();
        };

        /**
         * Bind an event on a dom element that is not contained in the view
         *
         * @param callback Callback to invoke when the event has been triggered
         * @param domElement dom element that will trigger the event
         * @param event Event to bind
         */
        Backbone.View.prototype.addExternalElement = function(domElements) {
            // instantiate the array to store external dom elements binded to the view - used in close() to avoid ghost views
            if (typeof this.externalElements === "undefined") {
                this.externalElements = [];
            }

            // add the domElement to the array containing all the external dom elements to the view
            this.externalElements = this.externalElements.concat(domElements);
        };
        /**
         * Resizes the div to the maximum displayable size on the screen
         */
        Backbone.View.prototype.resize = function(region) {
            if (typeof region !== "undefined" && typeof region[0] !== "undefined") {
                region[0].classList.add("div-scrollable");
                setTimeout(function() {
                    var divSize = window.innerHeight - (region.offset().top + region.outerHeight(true) - region.innerHeight());

                    region.height(divSize);

                    // if there is an active element, make it visible
                    var activeItem = region.children(".list-group-item.active")[0];
                    if (typeof activeItem !== "undefined" && typeof $(".list-group-item")[1] !== "undefined") {
                        region.scrollTop((activeItem.offsetTop) - ($(".list-group-item")[1].offsetTop));
                    }
                    // otherwise display the top of the list
                    else {
                        region.scrollTop(0);
                    }
                }, 0);
            }
        };

        domReady(function() {
            function onDeviceReady(desktop) {
                console.log("device ready, initializing application...");
                // Initialize the application-wide event dispatcher
                // initialise localisation
                $.i18n.init({
                    resGetPath: 'app/locales/__lng__/__ns__.json',
                    lng: 'fr',
                    fallbackLng: 'fr',
                    ns: {
                        namespaces: ['translation', 'debugger'],
                        defaultNs: 'translation'
                    }
                }).done(function() {
                    app.initialize();
                });

                $(window).resize(function(){
                  Backbone.View.prototype.resize($(".scrollable"));
                  Backbone.View.prototype.resize($(".scrollable-menu"));
                });

                // Hiding splash screen when app is loaded
                if (desktop !== true && typeof navigator.splashscreen !== 'undefined') {
                    console.log("hiding splashscreen");
                    navigator.splashscreen.hide();
                }
            }

            if (navigator.userAgent.toLowerCase().match(/(ipad|ipod|iphone|android|blackberry)/)) {
                // This is running on a device so waiting for deviceready event
                document.addEventListener('deviceready', onDeviceReady, false);
            } else {
                // On desktop don't have to wait for anything
                onDeviceReady(true);
            }
        });
    });
});
