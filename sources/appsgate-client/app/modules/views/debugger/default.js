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
            address: 'localhost',
            port: '8090'
        });
    },

    render: function() {
        var self = this;

        // render the editor with the program
        this.$el.append(this.template({}));

        // initialize debugger
        var dashboard = this.dashboard = new Debugger.Dashboard(this.$('.debugger .canvas'));
        dashboard.connect(this.connector);

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