define([
  "app",
  "models/service/service"
], function(App, Service) {

  var EnergyMonitoringAdapter = {};

  EnergyMonitoringAdapter = Service.extend({

    /**
     * @constructor
     */
    initialize: function() {
      EnergyMonitoringAdapter.__super__.initialize.apply(this, arguments);


      var self = this;
		
    dispatcher.on(this.get("id"), function(event) {
      console.log("EnergyMonitoringAdapter Service, received : ",event);
      if(event.varName === 'energyGroupAdded') {
        console.log("Added Energy Monitoring Group : ", event.value);
        self.trigger("energyGroupAdded", event);
      } else if(event.varName === 'energyGroupRemoved') {
        console.log("Removed Energy Monitoring Group : ", event.value);
        self.trigger("energyGroupRemoved", event);
      }
    });

    },

    createEnergyMonitoringEmptyGroup: function(name) {
      this.remoteControl("createEnergyMonitoringEmptyGroup", [{"type": "String", "value": name, "name": "name"}]);
    },
    removeEnergyMonitoringGroup: function(groupID) {
      this.remoteControl("removeEnergyMonitoringGroup", [{"type": "String", "value": groupID, "name": "groupID"}]);
    },
    createEnergyMonitoringGroup: function(name, sensors, budgetTotal, budgetUnit) {
      this.remoteControl("createEnergyMonitoringGroup", [{"type": "String", "value": name, "name": "name"},
        {"type": "JSONArray", "value": sensors, "name":"sensors"},
        {"type": "double", "value": budgetTotal, "name": "budgetTotal"},
        {"type": "double", "value": budgetUnit, "name": "budgetUnit"}]);
    },
	  
  	getCoreEnergyMonitoringGroups: function() {
		return services.getCoreEnergyMonitoringGroups();
	},
	  
  	getUnits: function () {
		var unitsMap = [];
		unitsMap.push({
			text: "W",
			value: 1
		});
		unitsMap.push({
			text: "Wh",
			value: 3600
		});
		unitsMap.push({
			text: "kWh",
			value: 3600000
		});
		unitsMap.push({
			text: "€",
			value: 540000
		});

		return unitsMap;
	},

  });
  return EnergyMonitoringAdapter;
});
