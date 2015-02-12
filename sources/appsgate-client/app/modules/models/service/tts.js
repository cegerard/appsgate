define([
  "app",
  "models/service/service"
], function(App, Service) {

  var TTS = {};

  TTS = Service.extend({
    /**
     * @constructor
     */
    initialize: function() {
      TTS.__super__.initialize.apply(this, arguments);
      var self = this;

      dispatcher.on(this.get("id"), function(ttsItems) {
        console.log("received : " + ttsItems);
        self.set("items", ttsItems);
      });

    },
    getVoices: function() {
      return this.get("voices");
    },
    getSpeed: function() {
      return this.get("speed");
    },
    setSpeed: function(speed) {
      this.remoteControl("setDefaultSpeed", [{"type": "int", "value": speed}], this.id);
    },
    getVoice: function() {
      return this.get("voice");
    },
    getLangFromVoice: function(voice) {
      var voices = this.getVoices();
      for(var lang in voices) {
        for(var i = 0; i< voices[lang].length; i++) {
          if(voices[lang][i][2] === voice) {
            return lang;
          }
        }
      }
      return null;
    },

    setVoice: function(voice) {
      this.remoteControl("setDefaultVoice", [{"type": "String", "value": voice}], this.id);
    },
    prepareTTS: function(text) {
      this.remoteControl("asynchronousTTSGeneration", [{"type": "String", "value": text}], this.id);
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
