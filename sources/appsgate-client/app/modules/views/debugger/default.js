/**
 * Created by barraq on 8/28/14.
 */

define([
    "appsgate.debugger",
    "text!templates/debugger/default.html"
], function(Debugger, debuggerDefaultTemplate) {

  var DebuggerView = {};
  // detailled view of a debugger
  DebuggerView = Backbone.View.extend({
    template: _.template(debuggerDefaultTemplate),

    initialize: function() {
        this.connector = new Debugger.Connector({
            address: document.URL.replace(/http:\/\//i,"").replace(/\/.*/i,"").replace(/:.*/i,""),
            port: '8090'
        });
    },

    render: function() {
        var self = this;

        // render the editor with the program
        this.$el.append(this.template({}));

        // initialize debugger
        var dashboard = this.dashboard = new Debugger.Dashboard(this.$('.debugger .canvas'), {
                d3: {
                    // Define custom locale (based on http://www.localeplanet.com/icu/fr/)
                    locale: {
                        'decimal': ',',
                        'thousands': ' ',
                        'grouping': [3],
                        'currency': ['€', ''],
                        'dateTime': '%a %b %e %X %Y',
                        'date': '%m/%d/%Y',
                        'time': '%H:%M:%S',
                        'periods': ['AM', 'PM'],
                        'days': ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'],
                        'shortDays': ['Dim.', 'Lun.', 'Mar.', 'Mer.', 'Jeu.', 'Ven.', 'Sam.'],
                        'months': ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Aout', 'Septembre', 'Octobre', 'Novembre', 'Décembre'],
                        'shortMonths': ['Janv.', 'Févr.', 'Mars', 'Avr.', 'Mai', 'Juin', 'Juil.', 'Aout', 'Sept.', 'Oct.', 'Nov.', 'Déc.']
                    },
                    // Define custom multi-time format
                    timeFormatMulti: [
                        ['.%L', function (d) {
                            return d.getMilliseconds();
                        }],
                        [':%S', function (d) {
                            return d.getSeconds();
                        }],
                        ['%H:%M', function (d) {
                            return d.getMinutes();
                        }],
                        ['%Hh', function (d) {
                            return d.getHours();
                        }],
                        ['%a %d', function (d) {
                            return d.getDay() && d.getDate() != 1;
                        }],
                        ['%b %d', function (d) {
                            return d.getDate() != 1;
                        }],
                        ['%B', function (d) {
                            return d.getMonth();
                        }],
                        ['%Y', function () {
                            return true;
                        }]
                    ]
                }
            }
        );
        dashboard.connect(this.connector);

        // make it scrollable (override width for scrollable)
        this.dashboard.$('.dashboard-container').addClass('scrollable div-scrollable');

        // listen to zoom request from dashboard
        dashboard.on('zoom:request', function (context) {
            dashboard.requestHistoryTrace(context);
        });

        // listen to marker click event from dashboard
        dashboard.on('marker:click', function (decorations, textContent, htmlContent) {
            alert(textContent);
        });

        // prompt server for initial history trace
        dashboard.requestInitialHistoryTrace();
    },

    destroy: function() {
        this.connector.destroy();
        this.connector = null;
        this.dashboard = null;
    }
  });

  return DebuggerView;
});