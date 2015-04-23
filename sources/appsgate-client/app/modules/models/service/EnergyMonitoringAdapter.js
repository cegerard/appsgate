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
        self.trigger("energyGroupAdded");
      } else if(event.varName === 'energyGroupRemoved') {
        console.log("Removed Energy Monitoring Group : ", event.value);
        self.trigger("energyGroupRemoved");
      }
    });

    },

    createEmptyGroup: function(name) {
      this.remoteControl("createEmptyGroup", [{"type": "String", "value": name, "name": "name"}]);
    },
    removeGroup: function(groupID) {
      this.remoteControl("removeGroup", [{"type": "String", "value": groupID, "name": "groupID"}]);
    },
    createGroup: function(name, sensors, budgetTotal, budgetUnit) {
      this.remoteControl("createGroup", [{"type": "String", "value": name, "name": "name"},
        {"type": "JSONArray", "value": sensors, "name":"sensors"},
        {"type": "double", "value": budgetTotal, "name": "budgetTotal"},
        {"type": "double", "value": budgetUnit, "name": "budgetUnit"}]);
    }

  });
  return EnergyMonitoringAdapter;
});
