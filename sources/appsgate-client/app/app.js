define(function(require, exports, module) {
    "use strict";
    
    var _ = require("underscore");
    var $ = require("jquery");
    var Backbone = require("backbone");
    var Router = require("routers/router");
    var Communicator = require("modules/communicator");
    var alertify=require("alertifyjs");
    
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
    
        this.MAX_NAME_LENGTH = 30;
    
        // Initialize the application-wide event dispatcher
        window.dispatcher = _.clone(Backbone.Events);
    
        // Define your master router on the application namespace and trigger all
        // navigation from this instance.
        window.appRouter = new Router();
    
        // Setting the connection with the box
        //Parser the URL called to generate the server provider.
        this.serverAddr = document.URL.replace(/http:\/\//i, "").replace(/\/.*/i, "").replace(/:.*/i, "");
        window.communicator = new Communicator('ws://' + this.serverAddr + ':8087');
    
        window.addEventListener("click", onFocusOutCircleMenu, false);
    
        // initialize the history management
        try {
            Backbone.history.start({"silent":"true"});
            Backbone.history.fragment = null;
        } catch (e) {}

        // navigate to the entry point of the application
        appRouter.navigate("home", {
            trigger: true
        });
    
        $(".navbar").i18n();
    
    
        this.getServerName = function() {
            return communicator.getServerName();
        };
        // Wait for the socket to be opened
        dispatcher.on("WebSocketOpen", function() {
    
            // start the debugger if it is not
            communicator.sendMessage({"method":"startDebugger", "args":[], "TARGET":"EHMI"});
      
            // wait for the data before launching the user interface
            var placesReady = false;
            var devicesReady = false;
            var servicesReady = false;
            var programsReady = false;
            var dependanciesReady = false;
            var adaptersReady = false;

      
            // places
            dispatcher.on("placesReady", function() {
              placesReady = true;
              // Initialize the collection of devices
              require(['collections/devices'], function(Devices) {
                window.devices = new Devices();
              });
      
              // Initialize the collection of devices
              require(['collections/services'], function(Services) {
                window.services = new Services();
              });
            });

            // devices
            dispatcher.on("devicesReady", function() {
              devicesReady = true;
              if (placesReady && devicesReady && servicesReady && programsReady) {
                dispatcher.trigger("dataReady");
              }
            });

            // services
            dispatcher.on("servicesReady", function() {
              servicesReady = true;
              if (placesReady && devicesReady && servicesReady && programsReady) {
                dispatcher.trigger("dataReady");
              }
            });
        
            // programs
            dispatcher.on("programsReady", function() {
              programsReady = true;
              if (placesReady && devicesReady && servicesReady && programsReady) {
                dispatcher.trigger("dataReady");
              }
            });

            // adapters
            dispatcher.on("adaptersReady", function() {
                adaptersReady = true;
                if (placesReady && devicesReady && servicesReady && programsReady) {
                    dispatcher.trigger("dataReady");
                }
            });

            // all data have been received, launch the user interface
            dispatcher.on("dataReady", function() {
                appRouter.initialized = true;
                $("#lost-connection-modal").modal("hide");
                $("#settings-modal").modal("hide");
          
                // remove potential duplicated entries of devices in a place
                places.forEach(function(l) {
                  l.set({
                    devices: _.uniq(l.get("devices"))
                  });
                });
            });

            dispatcher.on("system", function(msg) {
                console.log("System received message " + JSON.stringify(msg));
                if(msg.message!=undefined){
                     var l10nMessage=$.i18n.t(msg.message)
                     var timeout=10000;
                     alertify.log(l10nMessage, "log", timeout);
                }
            });

            // Initialize the collection of places
            require(['collections/places'], function(Places) {
              window.places = new Places();
            });
    
            // Initialize the collection of programs
            require(['collections/programs'], function(Programs) {
              window.programs = new Programs();
            });
            
            // Initialize the collection dependancies
            require(['collections/dependancies'], function(Dependancies) {
                window.dependancies = new Dependancies();
            });

            // Initialize the collection adapters
            require(['collections/adapters'], function(Adapters) {
                window.adapters = new Adapters();
            });

        });

        // listen to the event coming from the valid button of the modal window for the settings
        $("#settings-modal #valid-button").bind("click",onValidSettingsButton);
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
        $("#settings-modal .addr-server").val(communicator.getServerName());
        $("#settings-modal .port-server").val(communicator.getServerPort());

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
            var serverName =  $("#settings-modal .addr-server").val();
            var communicatorAddr = "ws://" + serverName + ":" + ($("#settings-modal .port-server").val() === "" ? "8080" : $("#settings-modal .port-server").val());
      
            // update the language if updated
            if ($("#settings-modal select#language :selected").val() !== appRouter.locale) {
                appRouter.updateLocale($("#settings-modal select#language :selected").val());
                $("#settings-modal").modal("hide");
            }
      
            if (communicator.getServerAddr() !== communicatorAddr) {
              // set the new address
              communicator.setServerAddr(communicatorAddr);
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
