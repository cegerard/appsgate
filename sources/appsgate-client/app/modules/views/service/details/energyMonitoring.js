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
			"click #edit-energy-group-modal button.valid-button": "onClickEditEnergyGroup",
			"click button.back-button": "onBackButton"
		},

		initialize: function () {
			var self = this;

			EnergyMonitoringDetailsView.__super__.initialize.apply(this, arguments);

			self.listenTo(self.model, 'energyDuringPeriodChanged', function (e) {
				self.updateValues(self.model.get('id'));
			});
			self.listenTo(self.model, 'statusChanged', function (e) {
				self.updateState(self.model.get('id'));
			});
			self.listenTo(self.model, 'budgetTotalChanged', function (e) {
				self.updateValues(self.model.get('id'));
			});
			self.listenTo(self.model, 'budgetUnitChanged', function (e) {
				self.updateValues(self.model.get('id'));
			});
			self.listenTo(self.model, 'groupNameChanged', function (e) {
				$("#energy-group-name").text(self.model.get("name"));
			});
			self.listenTo(self.model, 'sensorsGroupChanged', function (e) {
				self.updateSensorsList();
			});
			self.listenTo(self.model, 'budgetReset', function (e) {
				self.updateValues(self.model.get('id'));
				self.updateHistory();
			});
		},

		/**
		 * Return to the previous view
		 */
		onBackButton: function () {
			window.history.back();
		},

		/**
		 * Callback to edit energy group
		 */
		onClickEditEnergyGroup: function () {
			var self = this;

			var name = $("#edit-energy-group-modal #energyGroupNameInput").val();
			var sensors = self.getDevicesSelected();
			var budgetTotal = $("#edit-energy-group-modal #budgetValueInput").val();
			var budgetUnit = $("#edit-energy-group-modal #unitSelector").val();
			console.log("Edit: " + name + " " + budgetTotal + " " + budgetUnit);

			if (name !== self.model.get("name")) {
				self.model.setName(name);
			}

			if (budgetTotal !== self.model.get("budgetTotal")) {
				self.model.setBudgetTotal(budgetTotal);
			}

			if (parseInt(budgetUnit) !== parseInt(self.model.get("budgetUnit"))) {
				self.model.setBudgetUnit(budgetUnit);
			}

			var sensorsChanged = function (oldSensors, newSensors) {
				if (oldSensors.length !== newSensors.length) {
					return true;
				} else {
					_.each(oldSensors, function (s) {
						if (!_.contains(newSensors, s)) {
							return true;
						}
					});
				}
				return false;
			}(self.model.get("sensors"), sensors);
			if (sensorsChanged) {
				self.model.setEnergySensorsGroup(sensors);
			}

			$("#edit-energy-group-modal").modal("hide");
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

				// Build modal with previous values
				this.buildUnitSelector();
				this.buildDevicesChoice();

				this.updateState(this.model.get('id'));
				this.updateValues(this.model.get('id'));
				this.buildSensorsList();
				this.updateHistory();
				this.updateSensorsList();

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
			divChoice.append("<div class='col-md-12'><input type='checkbox' id='allDevice'><label for='allDevice'> " + $.i18n.t("services.energy-monitoring.modal-add.devices.all") + "</label></div>");

			var energyDevices = devices.getDevicesByType(6);
			_.each(energyDevices, function (device) {
				if (_.contains(self.model.get("sensors"), (device.get('id')))) {
					divChoice.append("<div class='col-md-12'><input type='checkbox' id='" + device.get("id") + "' checked ><label for='" + device.get('id') + "'> " + device.get('name') + "</label></div>");
				} else {
					divChoice.append("<div class='col-md-12'><input type='checkbox' id='" + device.get("id") + "'><label for='" + device.get('id') + "'> " + device.get('name') + "</label></div>");
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
		 * Method to build the list of energy sensors
		 */
		buildSensorsList: function () {
			var self = this;
			var divSensorsList = $("#sensors-list");

			var energyDevices = devices.getDevicesByType(6);
			_.each(energyDevices, function (device) {
				divSensorsList.append("<div class='col-md-12'><input type='checkbox' id='sensor-" + device.get("id") + "' disabled><label for='" + device.get('id') + "'> " + device.get('name') + "</label></div>");
			});

		},

		/**
		 * Method to get the ids of the devices selected
		 */
		getDevicesSelected: function () {
			var ids = [];
			// Check All checked
			if ($("#allDevice").is(":checked")) {
				_.forEach($("input[type=checkbox]"), function (input) {
					if (input.id !== "allDevice") {
						ids.push(input.id);
					}
				});
			} else {
				_.forEach($("input[type=checkbox]:checked"), function (input) {
					if (input.id !== "allDevice") {
						ids.push(input.id.split("sensor-")[0]);
					}
				});
			}

			return ids;
		},
		getUnit: function (unitValue) {
			var arrayUnit = services.getEnergyMonitoringAdapter().getUnits();
			var unit = arrayUnit[_.findIndex(arrayUnit, {
				value: parseInt(unitValue)
			})];
			return unit;
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

			var unit = self.getUnit(self.model.get('budgetUnit'));

			var spanTotalConsumption = divGroup.children(".row").children("div").children(".span-total-consumption");
			spanTotalConsumption.text(parseFloat(self.model.get('energyDuringPeriod')).toFixed(4));

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

		/**
		 * Method to update the list of energy sensors
		 */
		updateSensorsList: function () {
			var self = this;

			var energyDevices = devices.getDevicesByType(6);
			_.each(energyDevices, function (device) {
				$("#sensor-" + device.get('id')).prop('checked', _.contains(self.model.get("sensors"), (device.get('id'))));
			});
		},

		/**
		 * Method to update the list of history values
		 */
		updateHistory: function () {
			var self = this;
			var history = [];
			history = $.parseJSON(self.model.get("history"));

			$("#history-list").empty();
			_.each(history, function (entry) {
				var unit = self.getUnit(entry.budgetUnit);
				var newEntry = "<span class='col-md-12'>";
				newEntry += new Date(entry.startDate).toLocaleDateString();
				newEntry += " ";
				newEntry += new Date(entry.startDate).toLocaleTimeString();
				newEntry += " - ";
				newEntry += new Date(entry.stopDate).toLocaleDateString();
				newEntry += " ";
				newEntry += new Date(entry.stopDate).toLocaleTimeString();
				newEntry += " : ";
				newEntry += entry.energyDuringPeriod;
				newEntry += unit.text;
				newEntry += " / ";
				newEntry += entry.budgetTotal;
				newEntry += unit.text;
				newEntry += "</span>";
				$("#history-list").append(newEntry);
			});
		}


	});
	return EnergyMonitoringDetailsView
});