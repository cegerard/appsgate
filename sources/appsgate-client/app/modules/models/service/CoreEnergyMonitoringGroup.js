define([
  "app",
  "models/service/service"
], function(App, Service) {

  var CoreEnergyMonitoringGroup = {};

  CoreEnergyMonitoringGroup = Service.extend({

    /**
     * @constructor
     */
    initialize: function() {
      CoreEnergyMonitoringGroup.__super__.initialize.apply(this, arguments);


      var self = this;

    dispatcher.on(this.get("id"), function(event) {
      console.log("CoreEnergyMonitoringGroup Service, received : ",event);
      if(event.varName === 'name') {
        console.log("name changed : ", event.value);
        self.trigger("groupNameChanged");
      } else if(event.varName === 'sensors') {
        console.log("sensors changed : ", event.value);
        self.trigger("sensorsGroupChanged");
      } else if(event.varName === 'periods') {
        console.log("periods changed : ", event.value);
        self.trigger("periodsGroupChanged");
      }
    });

    },

    setName: function(name) {
      this.remoteControl("setName", [{"type": "String", "value": name, "name": "name"}]);
    },
  	getName: function() {
		return this.get("name");
	},
    addEnergySensor: function(sensorID) {
      this.remoteControl("addEnergySensor", [{"type": "String", "value": sensorID, "name": "sensorID"}]);
    },
    removeEnergySensor: function(sensorID) {
      this.remoteControl("removeEnergySensor", [{"type": "String", "value": sensorID, "name": "sensorID"}]);
    },
    setEnergySensorsGroup: function( sensors) {
      this.remoteControl("setEnergySensorsGroup", [{"type": "JSONArray", "value": sensors, "name":"sensors"}]);
    },
    resetEnergy: function() {
      this.remoteControl("resetEnergy", [], this.id);
    },
    setBudget: function(name, sensors, budgetTotal, budgetUnit) {
      this.remoteControl("setBudget", [
        {"type": "double", "value": budgetTotal, "name": "budgetTotal"},
        {"type": "double", "value": budgetUnit, "name": "budgetUnit"}]);
    },
    addPeriod: function(startDate, endDate, resetOnStart, resetOnEnd) {
      this.remoteControl("addPeriod", [
        {"type": "long", "value": startDate, "name": "startDate"},
        {"type": "long", "value": endDate, "name": "endDate"},
        {"type": "boolean", "value": resetOnStart, "name": "resetOnStart"},
        {"type": "boolean", "value": resetOnEnd, "name": "resetOnEnd"}
      ]);
    },
    removePeriodById: function(eventID) {
      this.remoteControl("removePeriodById", [
        {"type": "String", "value": eventID, "name": "eventID"} ]);
    }

  });
  return CoreEnergyMonitoringGroup;
});
