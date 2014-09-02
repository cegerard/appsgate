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
            port: '3000'
        });
    },

    render: function() {
        var self = this;

        // render the editor with the program
        this.$el.append(this.template({}));

        // initialize debugger
        this.dashboard = new Debugger.Dashboard(this.$('.debugger .canvas'));
        this.dashboard.connect(this.connector);
    },

    destroy: function() {
        this.connector.destroy();
        this.connector = null;
        this.dashboard = null;
    }
  });

  return DebuggerView;
});