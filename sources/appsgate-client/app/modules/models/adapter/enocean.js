define([
  "app",
  "models/adapter/adapter",
], function(App, Adapter) {

  var EnOcean = {};

  /**
   * Abstract class regrouping common characteristics shared by all the devices
   *
   * @class Device.Model
   */
  EnOcean = Adapter.extend({
    /**
     * @constructor
     */
    initialize: function() {
      EnOcean.__super__.initialize.apply(this, arguments);
      var self = this;

      dispatcher.on(this.get("id"), function(event) {
        console.log("EnOcean adapter, received event : ",event);
        switch(event.varName) {
          case "pairingMode":
            console.log("pairing mode changed");
            self.trigger("pairingModeChanged");
            break;
          case "items":
            console.log("paired items list changed");
            self.trigger("itemsChanged");
            break;
        }
      });
    },
    unpair:function(id) {

      communicator.sendMessage({
        objectId: this.get("id"),
        "method":"unpairDevice",
        "args":[{"type":"String","value":id}],
        "callId":"unpairDevice",
        "TARGET":"EHMI"
      });
    },
    setPairingMode:function(mode) {

      communicator.sendMessage({
        objectId: this.get("id"),
        "method":"setPairingMode",
        "args":[{"type":"boolean","value":mode}],
        "callId":"setPairingMode",
        "TARGET":"EHMI"
      });
    },

    getAllItem:function() {

      communicator.sendMessage({
        objectId: this.get("id"),
        "method":"getAllItem",
        "args":[],
        "callId":"getAllItem",
        "TARGET":"EHMI"
      });
    }
  });
  return EnOcean;
});
