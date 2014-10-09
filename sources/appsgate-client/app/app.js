define(function(require, exports, module) {
    "use strict";

    var _ = require("underscore");
    var $ = require("jquery");
    var Backbone = require("backbone");
    var Router = require("routers/router");
    var Communicator = require("modules/communicator");

    require("moment");
    require("i18n");
    require("bootstrap");
    require("jqueryui");
    require("jqueryuitouch");
    require("circlemenu");

    // Alias the module for easier identification.
    var app = module.exports;

    // Initialization of the application
    app.initialize = function() {

        // Define your master router on the application namespace and trigger all
        // navigation from this instance.
        window.appRouter = new Router();

        // Initialize the application-wide event dispatcher
        window.dispatcher = _.clone(Backbone.Events);

        // Setting the connection with the box
        //Parser the URL called to generate the server provider.
        var serverAddr=document.URL.replace(/http:\/\//i,"").replace(/\/.*/i,"").replace(/:.*/i,"");
        //DO NOT change the IP anymore, its not necessary
        window.communicator = new Communicator('ws://'+serverAddr+':8087');
        window.addEventListener("click", onFocusOutCircleMenu, false);

        // Wait for the socket to be opened
        dispatcher.on("WebSocketOpen", function() {
            // delete the current collections if any - in case of a reconnection
            if (typeof devices !== "undefined") {
                devices.getCoreClock().unsynchronize();
                //devices.reset();
            }
            if (typeof places !== "undefined") {
                //places.reset();
            }
            if (typeof services !== "undefined") {
                //services.reset();
            }
            if (typeof programs !== "undefined") {
                //programs.reset();
            }

            // places
            dispatcher.on("placesReady", function() {
                dispatcher.trigger("dataReady");
            });

            // devices
            dispatcher.on("devicesReady", function() {
                dispatcher.trigger("dataReady");
            });

            // services
            dispatcher.on("servicesReady", function() {
                dispatcher.trigger("dataReady");
            });

            // programs
            dispatcher.on("programsReady", function() {
                dispatcher.trigger("dataReady");
            });

            // all data have been received, launch the user interface
            dispatcher.on("dataReady", function() {
                $("#lost-connection-modal").modal("hide");
                $("#settings-modal").modal("hide");

                // remove potential duplicated entries of devices in a place
                places.forEach(function(l) {
                    l.set({devices: _.uniq(l.get("devices"))});
                });

                if (navigator.splashscreen !== undefined) {
                    navigator.splashscreen.hide();
                }

                // initialize the history management
                try {
                    Backbone.history.start();
                } catch (e) {
                }

                // navigate to the entry point of the application
                appRouter.navigate("reset", {trigger: true});
            });

            // Initialize the collection of places
            require(['collections/places'], function (Places) {
                window.places = new Places();
            });

            // Initialize the collection of devices
            require(['collections/devices'], function (Devices) {
                window.devices = new Devices();
            });

            // Initialize the collection of devices
            require(['collections/services'], function (Services) {
                window.services = new Services();
            });

            // Initialize the collection of programs
            require(['collections/programs'], function (Programs) {
                window.programs = new Programs();
            });

        });

        // listen to the event coming from the valid button of the modal window for the settings
        $("#settings-modal #valid-button").bind("click", onValidSettingsButton);
        $("#settings-modal .addr-server").bind("keyup", onValidSettingsButton);

        // listen to the event coming from the modal to handle network errors
        $("#lost-connection-modal #change-addr-server").bind("click", onChangeAddrServerButton);
        $("#lost-connection-modal #reconnect-button").bind("click", onReconnectButton);

        // listen to the communicator event when the connection has been lost and display an alert
        dispatcher.on("WebSocketClose", function() {
            $("#lost-connection-modal .text-info").hide();
            $("#lost-connection-modal .text-danger").show();
            $("#lost-connection-modal .text-danger").html("La connexion a &eacute;t&eacute; interrompue.");
            $("#lost-connection-modal").modal("show");
            $("#lost-connection-modal").i18n();
        });

        // set current server address and port in the modal for settings
        $("#settings-modal .addr-server").val(communicator.getServerAddr().split("://")[1].split(":")[0]);
        $("#settings-modal .port-server").val(communicator.getServerAddr().split(":")[2]);

        // Initialize the communication layer
        communicator.initialize();
    };

    /**
     * Callback when the user has validated new settings
     *
     * @param e JS event
     */
    function onValidSettingsButton(e) {
        if ((e.type === "keyup" && e.keyCode === 13 || e.type === "click") && $("#settings-modal .addr-server") !== "") {
            // hide the error message it is displayed
            $("#settings-modal .text-danger").hide();

            // show the message to inform the user the connection is being established
            $("#settings-modal .text-info").show();

            // set the new server address
            // build the server address from the information given by the user
            var serverAddr = "ws://" + $("#settings-modal .addr-server").val() + ":";
            serverAddr += $("#settings-modal .port-server").val() === "" ? "8080" : $("#settings-modal .port-server").val();

            // update the language if updated
            if ($("#settings-modal select#language :selected").val() !== appRouter.locale) {
                appRouter.updateLocale($("#settings-modal select#language :selected").val());
                $("#settings-modal").modal("hide");
            }

            if (communicator.getServerAddr() !== serverAddr) {
                // set the new address
                communicator.setServerAddr(serverAddr);

                // reconnect w/ to the new server
                communicator.reconnect();
            }
        }
    }

    /**
     * Callback when the user has clicked on the button to change the address of the server in the modal to manage network errors
     */
    function onChangeAddrServerButton() {
        $("#lost-connection-modal").modal("hide");
        $("#settings-modal").modal("show");
        $("#settings-modal").i18n();
    }

    /**
     * Callback when the user has clicked on one of the buttons to try to reconnect to the server
     */
    function onReconnectButton() {
        // show in the modal error the information that the connection is being established
        $("#lost-connection-modal .text-danger").hide();
        $("#lost-connection-modal .text-info").show();

        communicator.reconnect();
    }

    function onFocusOutCircleMenu(e) {
      $('.circlemenu').circleMenu('close');
    }

    return app;
});
