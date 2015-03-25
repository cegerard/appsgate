define([
  "app",
  "models/adapter/adapter",
], function(App, Adapter) {

  var WeatherAdapter = {};

  /**
   * Abstract class regrouping common characteristics shared by all the devices
   *
   * @class Device.Model
   */
  WeatherAdapter = Adapter.extend({
    /**
     * @constructor
     */
    initialize: function() {
      WeatherAdapter.__super__.initialize.apply(this, arguments);
      dispatcher.on(this.get("id"), function(json) {
        try {
          t = JSON.parse(json.value);
        } catch (err) {}
      });
    },

    checkLocationsStartingWith:function(firstLetters) {
      //code
      communicator.sendMessage({
        objectId: this.get("id"),
        "method":"checkLocationsStartingWith",
        "args":[{"type":"String","value":firstLetters}],
        "callId":"checkLocation",
        "TARGET":"EHMI"
      });
    },

    addLocationObserver:function(location) {
      //code
      communicator.sendMessage({
        objectId: this.get("id"),
        "method":"addLocationObserver",
        "args":[{"type":"String","value":location}],
        "callId":"addLocationObserver",
        "TARGET":"EHMI"
      });
    },

    removeLocationObserver:function(location) {
      //code
      communicator.sendMessage({
        objectId: this.get("id"),
        "method":"removeLocationObserver",
        "args":[{"type":"String","value":location}],
        "callId":"removeLocationObserver",
        "TARGET":"EHMI"
      });
    }



  });
  return WeatherAdapter;
});
