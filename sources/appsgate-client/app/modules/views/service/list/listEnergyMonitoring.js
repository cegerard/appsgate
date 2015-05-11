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
			"click button.delete-energy-group": "onDeleteEnergyGroup",
			"click #add-energy-group-modal button.valid-button": "onAddEnergyGroup",
			"keyup #add-energy-group-modal input": "validAddinAmount",

		},
		/**
		 * Listen to the updates
		 *
		 * @constructor
		 */
		initialize: function () {
			var self = this;

			services.getServicesByType()[this.id].forEach(function (service) {
				self.listenTo(service, "change", self.render);
				self.listenTo(service, "remove", self.render);
				self.listenTo(service, "add", self.render);
			});
//			self.listenTo(services.getEnergyMonitoringAdapter(), "add", self.render);
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
		onAddEnergyGroup: function () {
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
        onDeleteEnergyGroup: function(e) {
			e.preventDefault();
			var id = $(e.currentTarget).attr("idGroup");
			services.getEnergyMonitoringAdapter().removeEnergyMonitoringGroup(id);
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

		updateVisibilityBtnStartStop: function () {

		},

		validAddinAmount: function () {}


	});
	return ListEnergyMonitoringView;
});