define([
    "app",
	"text!templates/services/list/energyMonitoring.html",
], function (App, energyMonitoringTemplate) {

	var ListEnergyMonitoringView = {};

	/**
	 * Render the EnergyMonitoring Service in the list View (with all resumed groupes)
	 */
	ListEnergyMonitoringView = Backbone.View.extend({
		energyGrpTpl: _.template(energyMonitoringTemplate),
		events: {
			"click button.cancel-delete-energy-group-button": "onCancelDeleteEnergyGroup",
			"click button.delete-popover-energy-group-button": "onClickDeleteEnergyGroup",
			"click button.delete-energy-group-button": "onDeleteEnergyGroup",
			"click #add-energy-group-modal button.valid-button": "onClickAddEnergyGroup",
			"keyup #add-energy-group-modal #energyGroupNameInput": "onKeyupGroupName",
			"click button.start": "onStart",
			"click button.stop": "onStop",

			"click input#allDevice": "onCheckAllDevice",
			"click input.checkbox-select-device": "onCheckDevice",
		},
		/**
		 * Listen to the updates
		 *
		 * @constructor
		 */
		initialize: function () {
			var self = this;

			self.listenTo(services, "add", self.onAddEnergyGroup);
			self.listenTo(services, "remove", self.onAddEnergyGroup);

			services.getCoreEnergyMonitoringGroups().forEach(function (group) {
				self.attachListeners(group);
			});

		},

		/**
		 * Method to attach the listener of on energy group monitoring
		 * @param group : group to attach
		 */
		attachListeners: function (group) {
			var self = this;
			self.listenTo(group, 'groupNameChanged', function (e) {
				self.updateName(group.get('id'));
			});
			self.listenTo(group, 'energyDuringPeriodChanged', function (e) {
				self.updateValue(group.get('id'));
			});
			self.listenTo(group, 'statusChanged', function (e) {
				self.updateState(group.get('id'));
				self.updateDates(group.get('id'));
			});
			self.listenTo(group, 'budgetUnitChanged', function (e) {
				self.updateValue(group.get('id'));
			});
			self.listenTo(group, 'budgetTotalChanged', function (e) {
				self.updateValue(group.get('id'));
			});
			self.listenTo(group, 'budgetReset', function (e) {
				self.updateValue(group.get('id'));
			});
		},

		/**
		 * Method to detach the listener of on energy group monitoring
		 * @param group : group to detach
		 */
		detachListeners: function (group) {
			var self = this;
			self.stopListening(group);
		},

		/**
		 * Callback when new service added
		 *@param newService
		 */
		onAddEnergyGroup: function (newService) {
			// Test if the new service is an energy monitoring group
			if (newService.get('type') === "CoreEnergyMonitoringGroup") {
				var self = this;
				self.render();
				self.attachListeners(newService);
			}
		},

		/**
		 * Callback when one service deleted
		 *@param deletedService
		 */
		onRemoveEnergyGroup: function (deletedService) {
			// Test if deleted service is an energy monitoring group
			if (deletedService.get('type') === "CoreEnergyMonitoringGroup") {
				var self = this;
				self.render();
				self.detachListeners(deletedService)
			}
		},

		/**
		 * Render the list
		 */
		render: function () {
			if (!appRouter.isModalShown) {
				var energyMonitoringGroupsAlphabeticOrder = _.sortBy(services.getCoreEnergyMonitoringGroups(), function(group) {
					return group.get("name").toLowerCase();
				});
				this.$el.html(this.energyGrpTpl({
					energyMonitoringGroups: energyMonitoringGroupsAlphabeticOrder,
					model: services.getEnergyMonitoringAdapter()
				}));
				this.buildDevicesChoice();
				this.buildUnitSelector();
				this.setValues();
				this.setStates();
				this.setDates();

				// translate the view
				this.$el.i18n();

				// resize the list
				this.resize($(".scrollable"));

				return this;
			}
			return this;
		},

		/**
		 * Callback to add energy group
		 */
		onClickAddEnergyGroup: function () {
			var name = $("#add-energy-group-modal #energyGroupNameInput").val();
			var sensors = this.getDevicesSelected();
			var budgetTotal = $("#add-energy-group-modal #budgetValueInput").val();
			var budgetUnit = $("#add-energy-group-modal #unitSelector").val();
			console.log(name + " " + budgetTotal + " " + budgetUnit);

			services.getEnergyMonitoringAdapter().createEnergyMonitoringGroup(name, sensors, budgetTotal, budgetUnit)
			$("#add-energy-group-modal").modal("hide");
		},

		/**
		 * Callback to delete energy group
		 */
		onDeleteEnergyGroup: function (e) {
			e.preventDefault();
			var id = $(e.currentTarget).attr("idGroup");
			services.getEnergyMonitoringAdapter().removeEnergyMonitoringGroup(id);
		},

		/**
		 * Callback when the user has clicked on the button to cancel the deleting
		 */
		onCancelDeleteEnergyGroup: function (e) {
			e.preventDefault();
			var idGroup = $(e.currentTarget).attr('idGroup');
			// destroy the popover
			this.$el.find(".delete-popover-energy-group-button[idGroup='" + idGroup + "']").popover('destroy');
		},

		/**
		 * Callback when the user has clicked on the button delete.
		 */
		onClickDeleteEnergyGroup: function (e) {
			e.preventDefault();
			var self = this;
			var idGroup = $(e.currentTarget).attr('idGroup');
			// create the popover
			this.$el.find(".delete-popover-energy-group-button[idGroup='" + idGroup + "']").popover({
				html: true,
				title: $.i18n.t("services.energy-monitoring.warning-delete"),
				content: "<div class='popover-div'><button type='button' idGroup='" + idGroup + "' class='btn btn-default cancel-delete-energy-group-button'>" + $.i18n.t("form.cancel-button") + "</button><button type='button' idGroup='" + idGroup + "' class='btn btn-danger delete-energy-group-button'>" + $.i18n.t("form.delete-button") + "</button></div>",
				placement: "bottom"
			});
			// listen the hide event to destroy the popup, because it is created to every click on Delete
			this.$el.find(".delete-popover-energy-group-button[idGroup='" + idGroup + "']").on('hidden.bs.popover', function () {
				self.onCancelDeleteEnergyGroup(e);
			});
			// show the popup
			this.$el.find(".delete-popover-energy-group-button[idGroup='" + idGroup + "']").popover('show');
		},

		/**
		 * Callback to start monitoring
		 */
		onStart: function (e) {
			e.preventDefault();
			var id = $(e.currentTarget).attr("idGroup");
			services.getCoreEnergyMonitoringGroupById(id).startMonitoring();
		},

		/**
		 * Callback to start monitoring
		 */
		onStop: function (e) {
			e.preventDefault();
			var id = $(e.currentTarget).attr("idGroup");
			services.getCoreEnergyMonitoringGroupById(id).stopMonitoring();
		},

		/**
		 * Callback when check all devices
		 */
		onCheckAllDevice: function (e) {
			_.forEach($(".checkbox-select-device"), function (chkbxDevice) {
				$(chkbxDevice).prop('checked', $("#allDevice").is(':checked'));
			});
		},

		/**
		 * Callback when check one devices
		 */
		onCheckDevice: function (e) {
			if ($(".checkbox-select-device:checked").length === this.getEnergyDevices().length) {
				$("#allDevice").prop('checked', true);
			} else {
				$("#allDevice").prop('checked', false);
			}
		},

		/**
		 * Callback when typing group name
		 */
		onKeyupGroupName: function(e) {
			if ($("#energyGroupNameInput").val() !== "") {
				$(".btn.valid-button").prop("disabled", false);
				$(".btn.valid-button").removeClass("disabled");
				$(".btn.valid-button").removeClass("valid-disabled");
			} else {
				$(".btn.valid-button").prop("disabled", true);
				$(".btn.valid-button").addClass("disabled");
				$(".btn.valid-button").addClass("valid-disabled");
			}
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
						ids.push(input.id);
					}
				});
			}

			return ids;
		},

		/**
		 * Method to get the devices we want to present
		 */
		getEnergyDevices: function () {
			var eD = [];
			eD = devices.getDevicesByType(6);
			return eD;
		},

		/**
		 * Method to build the input checkbox for all energy devices
		 */
		buildDevicesChoice: function () {
			var self = this;
			var divChoice = $('#energyDevicesContainer');
			divChoice.append("<div class='col-md-12'><input type='checkbox' id='allDevice'><label for='allDevice'> " + $.i18n.t("services.energy-monitoring.modal-add.devices.all") + "</label></div>");

			_.each(self.getEnergyDevices(), function (device) {
				divChoice.append("<div class='col-md-12'><input type='checkbox' class='checkbox-select-device' id='" + device.get("id") + "'><label for='" + device.get('id') + "'> " + device.get('name') + "</label></div>");
			});
		},

		/**
		 * Method to build the unit selector with all units available
		 */
		buildUnitSelector: function () {
			var self = this;
			var selector = $('#unitSelector');

			$.each(services.getEnergyMonitoringAdapter().getUnits(), function (i, unit) {
				selector.append($('<option>', {
					value: unit.value,
					text: unit.text
				}));
			});
		},

		/**
		 * Method to set all the values to their current values
		 */
		setValues: function () {
			var self = this;
			services.getCoreEnergyMonitoringGroups().forEach(function (group) {
				self.updateValue(group.get("id"));
			});
		},

		/**
		 * Method to set all the group state
		 */
		setStates: function () {
			var self = this;
			services.getCoreEnergyMonitoringGroups().forEach(function (group) {
				self.updateState(group.get("id"));
			});
		},

		/**
		 * Method to set all the group dates
		 */
		setDates: function () {
			var self = this;
			services.getCoreEnergyMonitoringGroups().forEach(function (group) {
				self.updateDates(group.get("id"));
			});
		},

		/**
		 * Method to update the html element for the status
		 * These elements are : show/hide start/stop button, the status-div and if the progress bar is active
		 */
		updateState: function (idGroup) {
			var self = this;
			var divGroup = $("#" + idGroup);

			var btnStart = divGroup.children(".row").children("div").children(".pull-right").children(".btn.start");
			var btnStop = divGroup.children(".row").children("div").children(".pull-right").children(".btn.stop");
			var progressBar = divGroup.children(".row").children("div").children("div").children(".progress-bar");
			var divStatus = divGroup.children(".row").children("div").children(".div-status");

			if (services.getCoreEnergyMonitoringGroupById(idGroup).get('isMonitoring') === "true" || services.getCoreEnergyMonitoringGroupById(idGroup).get('isMonitoring') === true) {
				btnStart.hide();
				btnStop.show();
				progressBar.addClass("active");
				divStatus.addClass("led-processing");
				divStatus.removeClass("led-deployed");
			} else {
				btnStart.show();
				btnStop.hide();
				progressBar.removeClass("active");
				divStatus.addClass("led-deployed");
				divStatus.removeClass("led-processing");
			}
		},

		/**
		 * Method to update the html element for the value
		 * These elements are : the text of total consumption, the percent value in the progress bar and its width
		 */
		updateValue: function (idGroup) {
			var self = this;
			var divGroup = $("#" + idGroup);
			var energyGroup = services.getCoreEnergyMonitoringGroupById(idGroup);

			var arrayUnit = services.getEnergyMonitoringAdapter().getUnits();
			var unit = arrayUnit[_.findIndex(arrayUnit, {
				value: parseInt(energyGroup.get('budgetUnit'))
			})];

			var spanTotalConsumption = divGroup.children(".row").children("div").children(".span-total-consumption");
			spanTotalConsumption.text(parseFloat(energyGroup.get('energyDuringPeriod')).toFixed(4));

			var spanBudgetTotal = divGroup.children(".row").children("div").children(".span-budget-allocated");
			spanBudgetTotal.text(parseFloat(energyGroup.get('budgetTotal')).toFixed(2));

			var spanBudgetUnit = divGroup.children(".row").children("div").children(".span-budget-unit");
			spanBudgetUnit.text(unit.text);

			// ProgressBar
			var divProgressBar = divGroup.children(".row").children("div").children("div.progress")
			var progressBar = divProgressBar.children(".progress-bar.progress-bar-valid");
			var spanBudgetUsedPercent = progressBar.children(".budget-used-percent");
			var budgetUsedPercent = energyGroup.getPercentUsed();

			// If budget exceed 100%, create new red bar
			if (budgetUsedPercent > 100) {
				// Reduce valid bar before add new one
				progressBar.css("width", (200 - budgetUsedPercent) + "%");

				// If second bar already existed, don't recreate it
				if (divProgressBar.children(".progress-bar.progress-bar-over").length === 0) {
					// Create bar and append it before the valid
					progressBar.before("<div class='progress-bar progress-bar-over progress-bar-danger progress-bar-striped active' style='min-width:3em;max-width:100%;'><span class='over-budget-used-percent'></span></div>");
					// Change min width for the valid progress bar to make it disappear at 198-200%
					progressBar.css("min-width", 0);
				}
				var overProgressBar = divProgressBar.children(".progress-bar.progress-bar-over");
				var spanOverBudgetUsedPercent = $(overProgressBar).children(".over-budget-used-percent");
				overProgressBar.css("width", budgetUsedPercent - 100 + "%");

				// We change of span to show percent because 100-120 (approx) don't have place to write it in over bar
				if (budgetUsedPercent > 125) {
					spanBudgetUsedPercent.text("");
					spanOverBudgetUsedPercent.text(budgetUsedPercent + "%");
				} else {
					spanBudgetUsedPercent.text(budgetUsedPercent + "%");
					spanOverBudgetUsedPercent.text("");
				}

			} else {
				if (divProgressBar.children(".progress-bar.progress-bar-over").length > 0) {
					// In this case, progress bar was over 100 before, so we need to remove the over-progress-bar
					divProgressBar.children(".progress-bar.progress-bar-over").remove();
					// And also reset min width
					progressBar.css("min-width", "2em");
				}
				spanBudgetUsedPercent.text(budgetUsedPercent + "%");
				progressBar.css("width", budgetUsedPercent + "%");
			}
		},

		/**
		 * Method to update the group name
		 */
		updateName: function (idGroup) {
			var self = this;
			var divGroup = $("#" + idGroup);
			var energyGroup = services.getCoreEnergyMonitoringGroupById(idGroup);

			var spanName = divGroup.children(".row").children("div").children(".span-group-name");
			spanName.text(energyGroup.get('name'));
		},

		updateDates: function (idGroup) {
			var self = this;
			var divGroup = $("#" + idGroup);
			var energyGroup = services.getCoreEnergyMonitoringGroupById(idGroup);

			var spanDateFrom = divGroup.children(".row").children(".div-dates").children(".span-date-from");
			var spanDateUntil = divGroup.children(".row").children(".div-dates").children(".span-date-until");

			var intDateFrom = parseInt(energyGroup.get('startDate'));
			// At group creation, dates = -1
			if (intDateFrom < 1) {
				spanDateFrom.text("--/-- --:--:--");
				spanDateUntil.text("--/-- --:--:--");
			} else {

				var dateFrom = new Date(intDateFrom);
				var dateTokens = dateFrom.toLocaleDateString().split("/");
				var dateFromReformatted = dateTokens[0] + "/" + dateTokens[1];
				spanDateFrom.text(dateFromReformatted + " " + dateFrom.toLocaleTimeString());

				if (energyGroup.get('isMonitoring') === "true" || energyGroup.get('isMonitoring') === true) {
					spanDateUntil.text($.i18n.t("services.energy-monitoring.date.now"));
				} else {
					var dateUntil = new Date(parseInt(energyGroup.get('stopDate')));
					dateTokens = dateUntil.toLocaleDateString().split("/");
					var dateUntilReformatted = dateTokens[0] + "/" + dateTokens[1];
					spanDateUntil.text(dateUntilReformatted + " " + dateUntil.toLocaleTimeString());
				}
			}
		},


	});
	return ListEnergyMonitoringView;
});