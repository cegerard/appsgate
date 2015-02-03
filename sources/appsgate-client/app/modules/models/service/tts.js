define([
  "app",
  "models/service/service"
], function(App, Service) {

  var TTS = {};

  /**
   * Abstract class regrouping common characteristics shared by all the devices
   *
   * @class Device.Model
   */
  TTS = Service.extend({
    /**
     * @constructor
     */
    initialize: function() {
      TTS.__super__.initialize.apply(this, arguments);
      var self = this;

      // listening for volume value
      dispatcher.on(this.get("id"), function(ttsItems) {
        console.log("received : " + ttsItems);
        self.set("items", ttsItems);
      });

    },

    getTTSItems: function() {
      return this.remoteControl("getSpeechTextItems", [], this.id);
    },
    getTTSItemsText: function() {
      var a = [];
      this.getTTSItems();
      items = this.get("items");

      if(items != undefined) {
        for (var i = 0; i < items.length; i++) {
          a.push(items[i].text);
        }
      }

      return a;
    }
  });
  return TTS;
});
