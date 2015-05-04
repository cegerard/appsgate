define([
  "app",
    "views/service/details",
    "text!templates/services/details/energyMonitoring.html"

  ], function (App, ServiceDetailsView, EnergyMonitoringTemplate) {

	var EnergyMonitoringDetailsView = {};
	// detailed view for TTS Service
	EnergyMonitoringDetailsView = Backbone.View.extend({
		tplEnergyMonitoring: _.template(EnergyMonitoringTemplate),

		// map the events and their callback
		events: {
			"click button.back-button": "onBackButton"
			//            "click button.btn-tts-delete": "onDeleteTTSUI",
			//            "click button.btn-tts-add": "onAddTTSUI",
			//            "change select.select-voice": "onChangeVoiceUI",
			//            "change input.select-speed": "onChangeSpeedUI",
			//            "change select.select-lang": "onChangeLangUI",
			//            "change select.select-tts": "onChangeTTSUI"
		},

		initialize: function () {
			var self = this;
			
			EnergyMonitoringDetailsView.__super__.initialize.apply(this, arguments);

			//          this.model.on("itemsChanged", this.onItemsChangedModel, this);
			//          this.model.on("voiceChanged", this.onVoiceChangedModel, this);
			//          this.model.on("speedChanged", this.onSpeedChangedModel, this);
			//          this.model.on("ttsRunning", this.onTTSonGoingModel, this);
			//          this.model.on("ttsDone", this.onTTSonGoingModel, this);
			//
			//          TTSView.__super__.initialize.apply(this, arguments);
			//
			//        $.extend(self.__proto__.events, TTSView.__super__.events);
		},

		/**
		 * Return to the previous view
		 */
    	onBackButton: function() {
			window.history.back();
    	},
		

		autoupdate: function () {
			TTSView.__super__.autoupdate.apply(this);

			// translate the view
			this.$el.i18n();
		},

		/**
		 * This function does a partial rendering of the view
		 * only the voice selection part is changed
		 */
		renderVoices: function (voice) {
			var voices = this.model.getVoices();
			var lang = this.model.getLangFromVoice(voice);

			this.renderLang(voice);

			$(".select-voice option").remove();
			for (var i = 0; i < voices[lang].length; i++) {

				$(".select-voice").append(this.tplTTSVoice({
					selectedVoice: voice,
					voice: voices[lang][i][2],
					gender: voices[lang][i][1],
					country: voices[lang][i][0]
				}));
			}
			$(".select-lang").i18n();

		},
		renderLang: function (voice) {
			$(".select-lang option").remove();
			var voices = this.model.getVoices();
			var s = this.model.getLangFromVoice(voice);
			for (i in voices) {
				$(".select-lang").append(this.tplTTSLang({
					selectedLang: s,
					lang: i
				}));
			}
			$(".select-lang").i18n();

		},

		/**
		 * This function does a partial rendering of the view
		 * only the voice selection part is changed
		 * @param speed
		 */
		renderSpeed: function (speed) {
			$(".select-speed").val(speed);
			$(".select-speed").i18n();
		},

		/**
		 * This function does a partial rendering of the view
		 * only the TTS Items selection part is changed
		 * @param speed
		 */
		renderTTS: function (ttsItems) {
			$(".select-tts option").remove();
			var latest_book = -1;
			for (var i = 0; i < ttsItems.length; i++) {
				if (latest_book == -1 || ttsItems[i].book_id > ttsItems[latest_book].book_id) {
					latest_book = i;
				}
				$(".select-tts").append(this.tplTTSItem({
					book_id: ttsItems[i].book_id,
					text: ttsItems[i].text,
					voice: ttsItems[i].voice,
					speed: ttsItems[i].speed,
					audioUrl: ttsItems[i].audios[0],
					deletable: true
				}));
			}
			if (latest_book >= 0) {
				this.showAudio(ttsItems[latest_book].audios[0]);
				$(".select-tts").val(ttsItems[latest_book].book_id);
			} else {
				this.hideAudio();
			}
			$(".select-tts").i18n();
		},

		/**
		 * Render the detailed view of the service
		 */
		render: function () {
			var self = this;

			if (!appRouter.isModalShown) {

				this.$el.html(this.tplEnergyMonitoring({
					service: this.model
				}));


				this.resize($(".scrollable"));
				
				// translate the view
				this.$el.i18n();
				return this;
			}
		}
	});
	return EnergyMonitoringDetailsView
});