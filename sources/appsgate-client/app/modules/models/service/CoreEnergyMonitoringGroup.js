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
      } else if(event.varName === 'energyDuringPeriod') {
        console.log("energy changed : ", event.value);
        self.trigger("energyDuringPeriodChanged");
      } else if(event.varName === 'isMonitoring') {
        console.log("status changed : ", event.value);
        self.trigger("statusChanged");
      } else if(event.varName === 'budgetTotal') {
        console.log("budgetTotal changed : ", event.value);
        self.trigger("budgetTotalChanged");
      } else if(event.varName === 'budgetUnit') {
        console.log("budgetUnit changed : ", event.value);
        self.trigger("budgetUnitChanged");
      } else if(event.varName === 'budgetReset') {
        console.log("budgetUnit changed : ", event.value);
        self.trigger("budgetReset");
      }
    });

    },
	  
	getPercentUsed: function() {
		return (((this.get("energyDuringPeriod")) / (this.get("budgetTotal"))) * 100).toFixed(0);
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
  	setBudgetTotal: function(budgetTotal) {
	  this.remoteControl("setBudget", [
        {"type": "double", "value": budgetTotal, "name": "budgetTotal"}]);
    },
	setBudgetUnit: function(budgetUnit) {
      this.remoteControl("setBudgetUnit", [
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
    },
	  
	startMonitoring: function() {
		this.remoteControl("startMonitoring", [], this.id);
	},
	  
	stopMonitoring: function() {
		this.remoteControl("stopMonitoring", [], this.id);
	},

    getEvents: function() {
      return ["energyChanged"]; // Maybe add "monitoringReset", "monitoringStarted", "monitoringStopped"
    },
    /**
     * return the keyboard code for a given event
     */
    getKeyboardForEvent: function(evt) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONEvent("mandatory");
      switch (evt) {
        case "energyChanged":
          $(btn).append("<span>" + $.i18n.t('services.energy-monitoring.keyboard.energyChanged', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('services.energy-monitoring.keyboard.group') + "</span>"
          }));
          v.eventName = "energyDuringPeriod";
          v.eventValue = "*";

          v.phrase = "services.energy-monitoring.language.energyChanged";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected event found for Energy Monitoring Group: " + evt);
          btn = null;
          break;
      }
      return btn;
    },
    /**
     *return the list of available actions
     */
    getActions: function() {
      return ["startMonitoring", "stopMonitoring", "resetMonitoring"];
    },
    /**
     * return the keyboard code for a given action
     */
    getKeyboardForAction: function(act) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONAction("mandatory");

      switch (act) {
        case "startMonitoring":
          $(btn).append("<span>" + $.i18n.t('services.energy-monitoring.keyboard.startMonitoring', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('services.energy-monitoring.keyboard.group') + "</span>"
          }));
          v.methodName = "startMonitoring";
          v.phrase = "services.energy-monitoring.language.startMonitoring";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "stopMonitoring":
          $(btn).append("<span>" + $.i18n.t('services.energy-monitoring.keyboard.stopMonitoring', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('services.energy-monitoring.keyboard.group') + "</span>"
          }));
          v.methodName = "stopMonitoring";
          v.phrase = "services.energy-monitoring.language.stopMonitoring";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "resetMonitoring":
          $(btn).append("<span>" + $.i18n.t('services.energy-monitoring.keyboard.resetMonitoring', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('services.energy-monitoring.keyboard.group') + "</span>"
          }));
          v.methodName = "resetMonitoring";
          v.phrase = "services.energy-monitoring.language.resetMonitoring";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected action found for Energy Monitoring Group: " + act);
          btn = null;
          break;
      }
      return btn;
    },
    /**
     * return the list of available properties
     */
    getProperties: function() {
      return ["energyConsumed", "budgetRemaining"];
    },
    /**
     * return the keyboard code for a property
     */
    getKeyboardForProperty: function(property) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
      var v = this.getJSONProperty("mandatory");
      switch (property) {
        case "energyConsumed":
          $(btn).append("<span>" + $.i18n.t('services.energy-monitoring.keyboard.getEnergyDuringTimePeriod', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('services.energy-monitoring.keyboard.group') + "</span>"
          }));
          v.methodName = "getEnergyDuringTimePeriod";
          v.returnType = "number";
          v.unit = "watt";
          v.phrase = "services.energy-monitoring.language.getEnergyDuringTimePeriod";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "budgetRemaining":
          $(btn).append("<span>" + $.i18n.t('services.energy-monitoring.keyboard.getRemainingBudget', {
            myVar: "<span class='highlight-placeholder'>" + $.i18n.t('services.energy-monitoring.keyboard.group') + "</span>"
          }));
          v.methodName = "getRemainingBudget";
          v.returnType = "number";
          v.unit = "watt";
          v.phrase = "services.energy-monitoring.language.getRemainingBudget";
          $(btn).attr("json", JSON.stringify(v));
          break;
        default:
          console.error("unexpected device state found for group: " + property);
          btn = null;
          break;
      }
      return btn;
    },

  });
  return CoreEnergyMonitoringGroup;
});
