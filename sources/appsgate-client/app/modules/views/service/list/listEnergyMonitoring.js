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

			//			services.getServicesByType()[this.id].forEach(function (service) {
			//				self.listenTo(service, "change", self.render);
			//				self.listenTo(service, "remove", self.render);
			//				self.listenTo(service, "add", self.render);
			//			});

			var energyGroupMonitoringAdapter = services.getEnergyMonitoringAdapter();
			self.listenTo(energyGroupMonitoringAdapter, "energyGroupAdded", self.onAddEnergyGroup);
			self.listenTo(energyGroupMonitoringAdapter, "energyGroupRemoved", self.onRemoveEnergyGroup);

			services.getCoreEnergyMonitoringGroups().forEach(function (group) {
				self.attachListeners(group);
			});

		},

		attachListeners: function (group) {
			var self = this;
			self.listenTo(group, 'energyChanged', function (e) {
				self.updateValue(group.get('id'));
			});
			self.listenTo(group, 'statusChanged', function (e) {
				self.updateState(group.get('id'));
			});
		},

		detachListeners: function (group) {
			var self = this;
			self.stopListening(group);
		},

		onAddEnergyGroup: function (event) {
			var self = this;
			self.render();
			self.attachListeners(services.getCoreEnergyMonitoringGroupById(event.value))
		},

		onRemoveEnergyGroup: function (event) {
			var self = this;
			self.render();
			self.detachListeners(services.getCoreEnergyMonitoringGroupById(event.value))
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
			divChoice.append("<div class='col-md-12'><input type='checkbox' id='allDevice'>" + $.i18n.t("services.energy-monitoring.modal-add.devices.all") + "</div>");
			_.each(self.getEnergyDevices(), function (device) {
				divChoice.append("<div class='col-md-12'><input type='checkbox' id='" + device.get("id") + "'>" + device.get('name') + "</div>");
			});
		},

		setValues: function () {
			var self = this;
			services.getCoreEnergyMonitoringGroups().forEach(function (group) {
				self.updateValue(group.get("id"));
			});
		},

		setStates: function () {
			var self = this;
			services.getCoreEnergyMonitoringGroups().forEach(function (group) {
				self.updateState(group.get("id"));
			});
		},

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

		updateValue: function (idGroup) {
			var self = this;
			var divGroup = $("#" + idGroup);
			var spanTotalConsumption = divGroup.children(".row").children("div").children(".span-total-consumption");
			spanTotalConsumption.text(services.getCoreEnergyMonitoringGroupById(idGroup).get('energy'));

			var progressBar = divGroup.children(".row").children("div").children("div").children(".progress-bar");
			var spanBudgetUsedPercent = progressBar.children(".budget-used-percent");
			var budgetUsedPercent = services.getCoreEnergyMonitoringGroupById(idGroup).getPercentUsed();

			spanBudgetUsedPercent.text(budgetUsedPercent);
			progressBar.css("width", budgetUsedPercent + "%");
		},

		validAddinAmount: function () {}


	});
	return ListEnergyMonitoringView;
});