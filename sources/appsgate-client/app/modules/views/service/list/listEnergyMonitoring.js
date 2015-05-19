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
			"click button.delete-energy-group": "onClickDeleteEnergyGroup",
			"click #add-energy-group-modal button.valid-button": "onClickAddEnergyGroup",
			"keyup #add-energy-group-modal input": "validAddinAmount",
			"click button.start": "onStart",
			"click button.stop": "onStop",

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
			});
			self.listenTo(group, 'budgetUnitChanged', function (e) {
				self.updateValue(group.get('id'));
			});
			self.listenTo(group, 'budgetTotalChanged', function (e) {
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
				this.$el.html(this.energyGrpTpl({
					energyMonitoringGroups: services.getCoreEnergyMonitoringGroups(),
				}));
				this.buildDevicesChoice();
				this.buildUnitSelector();
				this.setValues();
				this.setStates();

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
		 * Callback to delete amount
		 */
		onClickDeleteEnergyGroup: function (e) {
			e.preventDefault();
			var id = $(e.currentTarget).attr("idGroup");
			services.getEnergyMonitoringAdapter().removeEnergyMonitoringGroup(id);
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
				divChoice.append("<div class='col-md-12'><input type='checkbox' id='" + device.get("id") + "'><label for='" + device.get('id') + "'> " + device.get('name') + "</label></div>");
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
			spanBudgetTotal.text(energyGroup.get('budgetTotal'));

			var spanBudgetUnit = divGroup.children(".row").children("div").children(".span-budget-unit");
			spanBudgetUnit.text(unit.text);

			var progressBar = divGroup.children(".row").children("div").children("div").children(".progress-bar");
			var spanBudgetUsedPercent = progressBar.children(".budget-used-percent");
			var budgetUsedPercent = energyGroup.getPercentUsed();
			spanBudgetUsedPercent.text(budgetUsedPercent + "%");
			progressBar.css("width", budgetUsedPercent + "%");
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

		validAddinAmount: function () {}


	});
	return ListEnergyMonitoringView;
});