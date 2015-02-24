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

    }



  });
  return EnOcean;
});
