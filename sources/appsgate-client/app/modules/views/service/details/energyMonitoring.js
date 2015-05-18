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
					model: this.model
				}));

				this.buildUnitSelector();
				this.buildDevicesChoice();
				
				this.updateState(this.model.get('id'));
				this.updateValues(this.model.get('id'));
				this.resize($(".scrollable"));

				// translate the view
				this.$el.i18n();
				return this;
			}
		},

		/**
		 * Method to build the input checkbox for all energy devices
		 */
		buildDevicesChoice: function () {
			var self = this;
			var divChoice = $('#energyDevicesContainer');
			divChoice.append("<div class='col-md-12'><input type='checkbox' id='allDevice'>" + $.i18n.t("services.energy-monitoring.modal-add.devices.all") + "</div>");
			
			var energyDevices = devices.getDevicesByType(6);
			_.each(energyDevices, function (device) {
				if (_.contains(self.model.get("sensors"),(device.get('id')))) {
					divChoice.append("<div class='col-md-12'><input type='checkbox' id='" + device.get("id") + "' checked >" + device.get('name') + "</div>");
				} else {
					divChoice.append("<div class='col-md-12'><input type='checkbox' id='" + device.get("id") + "'>" + device.get('name') + "</div>");
				}
			});
		},

		/**
		 * Method to build the unit selector with all units available
		 */
		buildUnitSelector: function () {
			var self = this;
			var selector = $('#unitSelector');

			$.each(services.getEnergyMonitoringAdapter().getUnits(), function (i, unit) {
				if (unit.value === parseInt(self.model.get('budgetUnit'))) {
					selector.append($('<option>', {
						value: unit.value,
						text: unit.text,
						selected: true
					}));
				} else {
					selector.append($('<option>', {
						value: unit.value,
						text: unit.text
					}));
				}
			});
		},

		/**
		 * Method to update the html element for the status
		 * These elements are : show/hide start/stop button, the status-div and if the progress bar is active
		 */
		updateState: function (idGroup) {
			var self = this;
			var divGroup = $("#div-summary-information").children(".panel-body");

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
			var divGroup = $("#div-summary-information").children(".panel-body");

			var arrayUnit = services.getEnergyMonitoringAdapter().getUnits();
			var unit = arrayUnit[_.findIndex(arrayUnit, {
				value: parseInt(self.model.get('budgetUnit'))
			})];

			var spanTotalConsumption = divGroup.children(".row").children("div").children(".span-total-consumption");
			spanTotalConsumption.text((self.model.get('energyDuringPeriod') / unit.value).toFixed(4));

			var spanBudgetTotal = divGroup.children(".row").children("div").children(".span-budget-allocated");
			spanBudgetTotal.text(self.model.get('budgetTotal'));

			var spanBudgetUnit = divGroup.children(".row").children("div").children(".span-budget-unit");
			spanBudgetUnit.text(unit.text);

			var progressBar = divGroup.children(".row").children("div").children("div").children(".progress-bar");
			var spanBudgetUsedPercent = progressBar.children(".budget-used-percent");
			var budgetUsedPercent = self.model.getPercentUsed();
			spanBudgetUsedPercent.text(budgetUsedPercent + "%");
			progressBar.css("width", budgetUsedPercent + "%");
		},
	});
	return EnergyMonitoringDetailsView
});