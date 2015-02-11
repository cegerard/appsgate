define([
  "app",
  "views/service/details",
  "text!templates/services/details/tts.html"
  ], function(App, ServiceDetailsView, TTSDetailTemplate) {

    var TTSView = {};
    // detailed view for TTS Service
    TTSView = Backbone.View.extend({
      tplTTS: _.template(TTSDetailTemplate),
        // map the events and their callback
        events: {
            "change select.select-voice": "onChangeVoice",
            "click button.btn-tts-speed": "onChangeSpeed",
            "change select.select-lang": "onChangeLang"
        },

      initialize: function() {
        var self = this;
          TTSView.__super__.initialize.apply(this, arguments);

        $.extend(self.__proto__.events, TTSView.__super__.events);
      },

      autoupdate: function() {
          TTSView.__super__.autoupdate.apply(this);

        // translate the view
        this.$el.i18n();
      },

        onChangeLang: function() {
            var lang = $(".select-lang").val();
            var voices = this.model.getVoices();

            $(".select-voice option").remove();
            for(var i = 0; i< voices[lang].length; i++) {
                $(".select-voice").append("<option>"+voices[lang][i][2]+"</option>");
            }
            this.model.setVoice(voices[lang][0][2]);
        },
        onChangeVoice: function(event) {
            console.log("onChangeVoice");
            event.currentTarget.value;
//            $(".select-gender").append(currentTarget.value[1]);

            this.model.setVoice(event.currentTarget.value);
        },

        onChangeSpeed   : function() {
            console.log("onChangeSpeed");
//        this.model.setSpeed();
        },


      /**
      * Render the detailed view of the service
      */
      render: function() {
        var self = this;

        if (!appRouter.isModalShown) {
            console.log("current Voice : ",this.model.getVoice());

            this.$el.html(this.tplTTS({
                service: this.model,
                sensorImg: ["app/img/tts.png"],
                sensorType: $.i18n.t("services.tts.name.singular"),
                selectedVoice: this.model.getVoice(),
                selectedLang: this.model.getLangFromVoice(this.model.getVoice()),
                selectedSpeed: this.model.getSpeed(),
                availableVoices: this.model.getVoices(),
                ttsItems: this.model.getTTSItems()
            }));

          this.resize($(".scrollable"));

          // translate the view
          this.$el.i18n();

          return this;
        }
      }
    });
    return TTSView
  });
