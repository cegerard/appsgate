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
		},

		initialize: function () {
			var self = this;

			EnergyMonitoringDetailsView.__super__.initialize.apply(this, arguments);

			self.listenTo(self.model, 'energyChanged', function (e) {
				self.updateValues(self.model.get('id'));
			});
			self.listenTo(self.model, 'statusChanged', function (e) {
				self.updateState(self.model.get('id'));
			});
		},

		/**
		 * Return to the previous view
		 */
		onBackButton: function () {
			window.history.back();
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
				
				this.updateState(this.model.get('id'));
				this.updateValues(this.model.get('id'));
				this.resize($(".scrollable"));

				// translate the view
				this.$el.i18n();
				return this;
			}
		},

		/**
		 * Method to update the html element for the status
		 * These elements are : show/hide start/stop button, the status-div and if the progress bar is active
		 */
		updateState: function (idGroup) {
			var self = this;
			var divGroup = $("#div-summary-information");

			//			var btnStart = divGroup.children(".row").children("div").children(".pull-right").children(".btn.start");
			//			var btnStop = divGroup.children(".row").children("div").children(".pull-right").children(".btn.stop");
			var progressBar = divGroup.children(".row").children("div").children("div").children(".progress-bar");
			var divStatus = divGroup.children(".row").children("div").children(".div-status");

			if (self.model.get('isMonitoring') === "true" || self.model.get('isMonitoring') === true) {
				//				btnStart.hide();
				//				btnStop.show();
				progressBar.addClass("active");
				divStatus.addClass("led-processing");
				divStatus.removeClass("led-deployed");
			} else {
				//				btnStart.show();
				//				btnStop.hide();
				progressBar.removeClass("active");
				divStatus.addClass("led-deployed");
				divStatus.removeClass("led-processing");
			}
		},

		/**
		 * Method to update the html element for the value
		 * These elements are : the text of total consumption, allocated budget, the percent value in the progress bar and its width
		 */
		updateValues: function (idGroup) {
			var self = this;
			var divGroup = $("#div-summary-information");
			var spanTotalConsumption = divGroup.children(".row").children("div").children(".span-total-consumption");
			spanTotalConsumption.text(self.model.get('energy'));
			
			var spanBudgetTotal = divGroup.children(".row").children("div").children(".span-budget-allocated");
			spanBudgetTotal.text(self.model.get('budgetTotal'));
			
			var spanBudgetUnit = divGroup.children(".row").children("div").children(".span-budget-unit");
			spanBudgetUnit.text(self.model.get('budgetUnit'));

			var progressBar = divGroup.children(".row").children("div").children("div").children(".progress-bar");
			var spanBudgetUsedPercent = progressBar.children(".budget-used-percent");
			var budgetUsedPercent = self.model.getPercentUsed();

			spanBudgetUsedPercent.text(budgetUsedPercent);
			progressBar.css("width", budgetUsedPercent + "%");
		},
	});
	return EnergyMonitoringDetailsView
});