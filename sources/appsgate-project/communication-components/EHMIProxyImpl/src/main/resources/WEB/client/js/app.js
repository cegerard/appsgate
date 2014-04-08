define([
	"jquery",
	"underscore",
	"backbone",
	"communicator",
	"device",
    "place",
    "program",
	"grammar",
	"bootstrap",
	"i18next"
], function($, _, Backbone, Communicator, Device, Place, Program, Grammar) {

	// define the application router
	var AppRouter = Backbone.Router.extend({
        routes: {
            ""		: "index",
			"reset" : "index"
        },
		
		initialize:function() {
			this.isModalShown = false;
			this.locale = "fr-FR";
			
			$.i18n.init({ lng : this.locale }).done(function() {
				$("body").i18n();
			});
		},

        // default route of the application
        index:function() {
			this.showMenuView(new Place.Views.Menu());
			this.showView(new Place.Views.Details({ model : places.at(0) }));
			
			// set active the first item of the navbar - displayed by default
			$($(".navbar li")[0]).addClass("active");
			
			// set active the first element of the aside menu - displayed by default
			$($($(".aside-menu .list-group")[1]).find(".list-group-item")[0]).addClass("active");
        },

		// update the side menu w/ new content
		showMenuView:function(menuView) {
			// remove and unbind the current view for the menu
			if (this.currentMenuView) {
				this.currentMenuView.close();
			}
			
			this.currentMenuView = menuView;
			this.currentMenuView.render();
			$(".aside-menu").html(this.currentMenuView.$el);
			
			// update the navbar - the navbar only needs to be updated when the users changed the views (places, devices or programs) so that the side menu
			// has to be updated
			// remove active class
			_.forEach($(".navbar-nav > li"), function(navItem) {
				$(navItem).removeClass("active");
			});
			// add active class to the correct menu item
			if (Backbone.history.fragment.indexOf("places") !== -1) {
				$($(".navbar-nav > li")[0]).addClass("active");
			} else if (Backbone.history.fragment.indexOf("devices") !== -1) {
				$($(".navbar-nav > li")[1]).addClass("active");
			} else if (Backbone.history.fragment.indexOf("programs") !== -1) {
				$($(".navbar-nav > li")[2]).addClass("active");
			}
		},

        showView:function(view) {
			// remove and unbind the current view
			if (this.currentView) {
				this.currentView.close();
			}

			// update the content
			this.currentView = view;
			$(".body-content").html(this.currentView.$el);
			this.currentView.render();
        },
		
		updateLocale:function(locale) {
			this.locale = locale;
			
			$.i18n.init({ lng : this.locale }).done(function() {
				places.get("-1").set("name", $.i18n.t("places-menu.unlocated-devices"));
				appRouter.navigate("reset", { trigger : true });
				$("body").i18n();
			});
		}
    });

	/**
	 * Initialize the application and make the bindings for the navigation bar
	 */
	function initialize() {
		// Initialize the application-wide event dispatcher
        window.dispatcher = _.clone(Backbone.Events);
		
		// Setting the connection with the box
		window.communicator = new Communicator('ws://localhost:8087');

        // Wait for the socket to be opened
        dispatcher.on("WebSocketOpen", function() {
			// delete the current collections if any - in case of a reconnection
			if (typeof devices !== "undefined") {
				devices.getCoreClock().unsynchronize();
				devices.reset();
				delete devices;
			}
			if (typeof places !== "undefined") {
				places.reset();
				delete places;
			}
			if (typeof programs !== "undefined") {
				programs.reset();
				delete programs;
			}

			// wait for the data before launching the user interface
			var placesReady = false;
			var devicesReady = false;
			var programsReady = false;

			// places
			dispatcher.on("placesReady", function() {
				placesReady = true;
				if (placesReady && devicesReady && programsReady) {
					dispatcher.trigger("dataReady");
				}
			});

			// devices
			dispatcher.on("devicesReady", function() {
				devicesReady = true;
				if (placesReady && devicesReady && programsReady) {
					dispatcher.trigger("dataReady");
				}
			});

			// programs
			dispatcher.on("programsReady", function() {
				programsReady = true;
				if (placesReady && devicesReady && programsReady) {
					dispatcher.trigger("dataReady");
				}
			});

			// all data have been received, launch the user interface
			dispatcher.on("dataReady", function() {
				// initialize the grammar
				window.grammar = new Grammar();
				
				$("#lost-connection-modal").modal("hide");
				$("#settings-modal").modal("hide");

				// remove potential duplicated entries of devices in a place
				places.forEach(function(l) {
					l.set({ devices : _.uniq(l.get("devices")) });
				});

				if (navigator.splashscreen !== undefined) {
					navigator.splashscreen.hide();
				}
				
				// initialize the history management
				try { Backbone.history.start(); } catch(e) {}
				
				// navigate to the entry point of the application
				appRouter.navigate("reset", { trigger : true });
			});
			
			// Initialize the collection of places
            window.places = new Place.Collection();

            // Initialize the collection of devices
            window.devices = new Device.Collection();

            // Initialize the collection of programs
            window.programs = new Program.Collection();
        });

        // main router of the application
        window.appRouter = new AppRouter();
		
		// hide the button to flash devices if there is no camera
		if (!navigator.camera) {
			$("#flash-device-button").hide();
		}
		
		// listen to the event coming from the button to flash a device
		$("#flash-device-button").on("click", function() {
			// launch the window to flash a device
			cordova.plugins.barcodeScanner.scan(
				function(result) {
					if (result.cancelled) {
						console.log("the user cancelled the scan");
					} else {
						if (typeof devices.get(result.text) !== "undefined") {
							appRouter.navigate("#devices/" + result.text, { trigger : true });
						} else {
							navigator.notification.alert(
								"Dispositif non reconnu",
								null,
								"Information"
							);
						}
					}
				},
				function(error) {
					console.log("scanning failed: " + error);
				}
			);
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
		});
		
		// set current server address and port in the modal for settings
		$("#settings-modal .addr-server").val(communicator.getServerAddr().split("://")[1].split(":")[0]);
		$("#settings-modal .port-server").val(communicator.getServerAddr().split(":")[2]);
		
		// Initialize the communication layer
		communicator.initialize();
	}
	
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

	return {
		initialize : initialize
	};
});
