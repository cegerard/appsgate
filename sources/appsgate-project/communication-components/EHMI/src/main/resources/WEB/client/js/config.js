require.config({
    // initialize the application with the main application file
    deps: ["main"],
    paths:{
        // RequireJS plugin
        text: "libs/require/text",
        // RequireJS plugin
        domReady: "libs/require/domReady",
        // jQuery
        jquery: "libs/jquery/jquery-1.8.2",
        // jQuery UI
        jqueryui: "libs/jquery/plugins/jqueryui/jquery-ui",
		// jstree
		jstree : "libs/jquery/plugins/jstree/jquery.jstree",
		// i18next for internationalization
		i18next: "libs/i18next/i18next.amd.withJQuery-1.7.1.min",
        // bootstrap
        bootstrap: "libs/bootstrap/js/bootstrap.min",
        // underscore library
        underscore: "libs/underscore/underscore",
        // Backbone.js library
        backbone: "libs/backbone/backbone",
		// Raphael.js library
		raphael: "libs/raphael/raphael.2.1.0.amd",
		// Color wheel Raphael.js plugin
		colorWheel: "libs/raphael/plugins/colorwheel",
		// parser generator
		peg: "libs/peg/peg-0.7.min",
		// Moment.js library
		moment: "libs/moment/moment.min",
        // Modules
        communicator: "modules/communicator",
        place: "modules/place",
        device: "modules/device",
        program: "modules/program",
        home: "modules/home",
		grammar: "modules/grammar"
    },
    shim: {
        "bootstrap": {
            deps: ["jquery"]
        },
        "underscore": {
            exports: "_"
        },
        "backbone": {
            deps: [ "underscore", "jquery" ],
            exports: "Backbone"
        },
		"raphael" : {
			exports: "Raphael"
		},
		"colorWheel" : {
			deps: [ "jquery", "raphael" ]
		},
        "jqueryui": {
            deps: [ "jquery" ]
        },
		"jstree" : {
			deps: [ "jquery" ]
		},
		"i18next" : {
			deps: [ "jquery" ] 
		}
    }
});
