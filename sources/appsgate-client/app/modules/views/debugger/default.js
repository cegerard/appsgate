/**
 * Created by barraq on 8/28/14.
 */

var MWIMS = 3000000;

define([
    "appsgate.debugger",
    "text!templates/debugger/default.html",
    "bootstrap-datetimepicker"
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

        // customize our theme from `basic` theme.
        var theme = _.merge(Debugger.themes.basic, {
            'dashboard': {
                'widget': {
                    'margin': {
                        top: 5  // add 5px top margin to each widget
                    }
                }
            }
        });

        // initialize debugger
        var dashboard = this.dashboard = new Debugger.Dashboard(this.$('.debugger .canvas'), {
                theme: theme,
                d3: {
                    // Define custom locale (based on http://www.localeplanet.com/icu/fr/)
                    locale: {
                        'decimal': ',',
                        'thousands': ' ',
                        'grouping': [3],
                        'currency': ['€', ''],
                        'dateTime': '%a %b %e %X %Y',
                        'date': '%d/%m/%Y',
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
                },
                i18n: {
                    ns: "debugger"
                },
                livetrace: {
                    delayBeforeFlush: MWIMS
                }
            }
        );
        dashboard.connect(this.connector);

        // make it scrollable (override width for scrollable)
        this.dashboard.$('.dashboard-container').addClass('scrollable div-scrollable');

        // setup date time pickers options
        var datetimepicker_options = {
            use24hours: true
        };

        // set date time pickers
        $('#datetimepicker-from').datetimepicker(datetimepicker_options);
        $('#datetimepicker-to').datetimepicker(datetimepicker_options);

        $("#datetimepicker-from").on("dp.change",function (e) {
            var now = new Date();
            $('#datetimepicker-to').data("DateTimePicker").setMinDate(new Date(Math.min(now, e.date.valueOf() + MWIMS)));
			if (self.idFiltered === undefined) {
				dashboard.requestInitialHistoryTrace({
					from: e.date.valueOf(),
					to: $('#datetimepicker-to').data("DateTimePicker").getDate().valueOf()
				});
			} else {
				dashboard.requestInitialHistoryTrace({
					from: e.date.valueOf(),
					to: $('#datetimepicker-to').data("DateTimePicker").getDate().valueOf(),
					focus: self.idFiltered,
					focusType: "id",
					order: "dep"
				});
			}
        });
        $("#datetimepicker-to").on("dp.change",function (e) {
            var now = new Date();
            $('#datetimepicker-from').data("DateTimePicker").setMaxDate(new Date(Math.min(now, e.date.valueOf()) - MWIMS));
			if (self.idFiltered === undefined) {
				dashboard.requestInitialHistoryTrace({
					from: $('#datetimepicker-from').data("DateTimePicker").getDate().valueOf(),
					to: e.date.valueOf()
				});
			} else {
				dashboard.requestInitialHistoryTrace({
					from: $('#datetimepicker-from').data("DateTimePicker").getDate().valueOf(),
					to: e.date.valueOf(),
					focus: self.idFiltered,
					focusType: "id",
					order: "dep"
				});
			}
        });

        // listen to zoom request from dashboard
        dashboard.on('zoom:request', function (context) {
            self.$('#datetimepicker-toolbar').show();
            dashboard.requestHistoryTrace(context);
        });

        // listen to marker click event from dashboard
        dashboard.on('marker:click', function (decorations, textContent, htmlContent) {
            $("#bubbleModal").find(".modal-body").html(htmlContent);
            $("#bubbleModal").modal("show");
            console.log(decorations);
        });


        // listen to widget focus request from dashboard
        dashboard.on('eventline:focus:request', function(context, attributes) {
            self.$('#datetimepicker-toolbar').show();
            dashboard.requestHistoryTrace(context);
        });

        // listen to widget name click from dashboard
        dashboard.on('eventline:name:click', function(context, attributes) {
            if (attributes.kind == 'program') {
                console.log("Program with id "+attributes.id+" was clicked");
            } else {
                console.log("Device of type "+attributes.type+" and with id "+attributes.id+" was clicked");
            }
        });

        // setup ui
        this.$('.btn-primary').on('click', function(){
            if( $(this).find('input').attr('id') == 'live' && !dashboard.isLiveMode()) {
                self.$('#datetimepicker-toolbar').hide();
                dashboard.requestLiveTrace();
            } else if ( $(this).find('input').attr('id') == 'history' && !dashboard.isHistoryMode()) {
                self.$('#datetimepicker-toolbar').show();
                self._resetDateTimePicker();
            }
        });

        // activate history mode
        self.$('#datetimepicker-toolbar').show();
        this.$('#debugger-action-history').addClass('active');
        self._resetDateTimePicker();
    },

    _resetDateTimePicker: function() {
        var now = new Date();
        $('#datetimepicker-from').data("DateTimePicker").setDate(new Date(now - 86400000));
        $('#datetimepicker-from').data("DateTimePicker").setMaxDate(new Date(now - MWIMS));
        $('#datetimepicker-to').data("DateTimePicker").setDate(now);
        $('#datetimepicker-to').data("DateTimePicker").setMaxDate(now);
    },
	  
  	setIDFilter: function(id) {
		this.idFiltered = id;
	},

    destroy: function() {
        this.connector.destroy();
        this.connector = null;
        this.dashboard = null;
    }
  });

  return DebuggerView;
});
