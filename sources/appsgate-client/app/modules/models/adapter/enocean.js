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
      dispatcher.on(this.get("id"), function(json) {
        try {
          t = JSON.parse(json.value);
        } catch (err) {}
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
