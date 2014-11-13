// This is the runtime configuration file.  It complements the Gruntfile.js by
// supplementing shared properties.
require.config({
    paths: {
        // VENDOR
        // Make vendor easier to access.
        "vendor": "../vendor",
        // Almond is used to lighten the output filesize.
        "almond": "../vendor/bower/almond/almond",
        // Opt for Lo-Dash Underscore compatibility build over Underscore.
        "underscore": "../vendor/bower/lodash/dist/lodash",
        // JQuery and Backbone
        "jquery": "../vendor/bower/jquery/jquery",
        "jqueryui": "../vendor/bower/jqueryui/ui/jquery-ui",
        "jqueryuitouch": "../vendor/bower/jqueryui-touch-punch/jquery.ui.touch-punch",
        "backbone": "../vendor/bower/backbone/backbone",
        "peg": "../vendor/pegjs/peg",
        // JQuery plugins
        "i18n": "../vendor/bower/i18next/release/i18next.amd.withJQuery-1.7.1",
        // RequireJS plugins
        "text": "../vendor/bower/requirejs-text/text",
        "domReady": "../vendor/bower/requirejs-domready/domReady",
        // Bootstrap
        "bootstrap": "../vendor/bower/bootstrap/dist/js/bootstrap",
        // Snap.svg
        "snapsvg": "../vendor/bower/Snap.svg/dist/snap.svg",
        // MomentJS
        "moment": "../vendor/bower/momentjs/moment",
        // JQuery Circle Menu Plugin
        "circlemenu": "../vendor/circlemenu/JQuery.circlemenu",
        // Raphael.js library
        raphael: "../vendor/bower/raphael/raphael-min",
        // Color wheel Raphael.js plugin
        colorwheel: "../vendor/raphael/plugins/colorwheel",
        colorwidget: "../vendor/colorwidget/loadcolor",
        // jsTree for file browsing
        jstree: "../vendor/jstree/jquery.jstree",
        // d3 library
        d3: "../vendor/bower/d3/d3",
        // APPSGATE
        // Modules
        "modules": "../app/modules",
        // Routers
        "routers": "../app/modules/routers",
        // Collections
        "collections": "../app/modules/collections",
        // Models
        "models": "../app/modules/models",
        // Views
        "views": "../app/modules/views",
        // Templates
        "templates": "../app/templates",
        // APPSGATE.debugger
        "appsgate.debugger": "../vendor/bower/appsgate.debugger/lib/appsgate.debugger.dev"
    },
    shim: {
        // This is required to ensure Backbone works as expected within the AMD
        // environment.
        "underscore": {
            exports: "_"
        },
        "bootstrap": {
            deps: ["jquery"]
        },
        "jqueryui": {
            deps: ["jquery"]
        },
        "jqueryuitouch": {
            deps: ["jquery", "jqueryui"]
        },
        "circlemenu": {
            deps: ["jquery"]
        },
        "backbone": {
            // These are the two hard dependencies that will be loaded first.
            deps: ["jquery", "underscore"],
            // This maps the global `Backbone` object to `require("backbone")`.
            exports: "Backbone"
        },
        "raphael": {
            exports: "Raphael"
        },
        "colorWheel": {
            deps: ["jquery", "raphael"]
        },
        "i18n": {
            deps: ["jquery"]
        },
        "moment": {
            exports: "moment"
        },
        "d3": {
            exports: "d3"
        },
        "appsgate.debugger": {
            deps: ['jquery', 'jqueryui', 'backbone', 'underscore', 'd3']
        }
    }
});



