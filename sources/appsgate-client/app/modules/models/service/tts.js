define([
  "app",
  "collections/ttsItemsCollection",
  "models/service/service"
], function(App, TTSItemsCollection, Service) {

  var TTS = {};

  TTS = Service.extend({

    translationOngoing:[],

    /**
     * @constructor
     */
    initialize: function() {
      TTS.__super__.initialize.apply(this, arguments);


      var self = this;
      var ttsItemsCollection = new TTSItemsCollection();
      ttsItemsCollection.set(self.get("ttsItems"));
      var itemsRunning = this.get("ttsRunning");
      if (itemsRunning) {
        for(var i = 0; i<itemsRunning.length; i++) {
          this.translationOngoing.push(itemsRunning[i].book_id);
        }
      }

    dispatcher.on(this.get("id"), function(event) {
      console.log("TTS Service, received : ",event);
      if(event.varName === 'voice') {
        console.log("new voice setted : ", event.value);
        self.trigger("voiceChanged");
      } else if(event.varName === 'speed') {
        console.log("new speed setted : ", event.value);
        self.trigger("speedChanged");
      } else if(event.varName === 'ttsItems') {
        console.log("new ttsItems setted : ", event.value);
        ttsItemsCollection.set(JSON.parse(event.value));
        self.set("ttsItems",ttsItemsCollection.toJSON());
        self.trigger("itemsChanged");
        dispatcher.trigger("tts:itemsChanged");
      } else if(event.varName === 'ttsRunning') {
        console.log("ttsItem onGoing  : ", event.value);
        self.addOngoing(event.value);
        self.trigger("ttsRunning");
      } else if(event.varName === 'ttsDone') {
        console.log("ttsItem done : ", event.value);
        self.removeOngoing(event.value);
        self.trigger("ttsDone");
      }
    });

    },

    addOngoing: function(book_id) {
      var index = this.getOngoingIndex(book_id);
      if(index == -1) {
        this.translationOngoing.push(book_id);
      }
    },
    removeOngoing: function(book_id) {
      var index = this.getOngoingIndex(book_id);
      if(index != -1) {
        this.translationOngoing.splice(index,1);
      }
    },
    getOngoingIndex: function(book_id) {
      if(this.translationOngoing.length ==0 ) {
        return -1;
      } else {
        for(var i = 0; i<this.translationOngoing.length; i++) {
          if(this.translationOngoing[i] == book_id) {
            return i;
          }
        }
        return -1;
      }

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
    getVoiceDescription: function() {
      var voice = this.getVoice();
      var voices = this.getVoices();
      for(var lang in voices) {
        for(var i = 0; i< voices[lang].length; i++) {
          if(voices[lang][i][2] === voice) {
            return voices[lang][i];
          }
        }
      }
      return null;
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
    prepareTTS: function(text, voice, speed) {
      this.remoteControl("asynchronousTTSGeneration", [{"type": "String", "value": text}, {"type": "String", "value": voice}, {"type": "int", "value": speed}], this.id);
    },
    getTTSItems: function() {
      return this.get("ttsItems");
    },
    deleteTTSItem: function(book_id) {
      this.remoteControl("deleteSpeechText", [{"type": "int", "value": book_id}], this.id);
    },
    getTTSItemsText: function() {
      var a = [];
      items = this.getTTSItems();

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
