define([
  "app",
  "models/service/service",
  "text!templates/program/nodes/weatherStateCodeNode.html"
], function(App, Service, ActionTemplate) {

  var Weather = {};

  /**
   * Implementation of the Yahoo Weather service
   * 
   * @class Service.Weather
   */
  Weather = Service.extend({
    /**
     * @constructor
     */
    initialize: function() {
      Weather.__super__.initialize.apply(this, arguments);
    },
    
    /**
     * Weather Events (TODO) should only be to notify of a weather change ( ???)
     */
    

    /**
    * return the list of available properties (only those returning a boolean)
    */
    getBooleanProperties: function() {
            return ["isWeatherSimplifiedCodeForecast"];
    },        

    /**
     * return the list of available properties
     */
    getProperties: function() {
      return [ "getMinTemperatureForecast", "getMaxTemperatureForecast", "getAvgTemperatureForecast"];
    },
    /**
     * return the keyboard code for a given property
     */
    getKeyboardForProperty: function(property) {
      var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
      var v = {"type": "deviceState", "target": {"iid": "X", "type": "service", "serviceType":this.get("type"), "value":this.get("id")}, "iid": "X"};
      switch(property) {
        case "isWeatherSimplifiedCodeForecast":
          $(btn).append("<span data-i18n='keyboard.is-weather-code-state'><span>");
          
          v.methodName = "isWeatherSimplifiedCodeForecast";
          v.returnType = "boolean";          
                              
          v.args = [ {"type":"String", "value": "Grenoble"},
                    {"type":"int", "value": "0"},
                    {"type":"int", "value": "0"}];          
                    
          v.phrase = "keyboard.is-weather-code-state";
          $(btn).attr("json", JSON.stringify(v));
          break;      	
        // case "getWeatherCodeForecast":
          // $(btn).append("<span data-i18n='keyboard.get-weather-code-state'><span>");
//           
		  // v.methodName = property;
          // v.args = [ {"type":"String", "value": "Grenoble"},
                    // {"type":"int", "value": "0"}];          
          // v.returnType = "number";
          // v.phrase = "keyboard.get-weather-code-state";
          // $(btn).attr("json", JSON.stringify(v));
          // break;
        case "getMinTemperatureForecast":
          $(btn).append("<span data-i18n='keyboard.weather-min'><span>");
          
		  v.methodName = property;
          v.args = [ {"type":"String", "value": "Grenoble"},
                    {"type":"int", "value": "0"}];          
          v.returnType = "number";
          v.phrase = "keyboard.weather-min";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "getMaxTemperatureForecast":
          $(btn).append("<span data-i18n='keyboard.weather-max'><span>");
          
		  v.methodName = property;
          v.args = [ {"type":"String", "value": "Grenoble"},
                    {"type":"int", "value": "0"}];          
          v.returnType = "number";
          v.phrase = "keyboard.weather-max";
          $(btn).attr("json", JSON.stringify(v));
          break;
        case "getAvgTemperatureForecast":
          $(btn).append("<span data-i18n='keyboard.weather-avg'><span>");
          
		  v.methodName = property;
          v.args = [ {"type":"String", "value": "Grenoble"},
                    {"type":"int", "value": "0"}];          
          v.returnType = "number";
          v.phrase = "keyboard.weather-avg";
          $(btn).attr("json", JSON.stringify(v));
          break;
          
        default:
          console.error("unexpected service state found for Weather : " + property);
          btn = null;
          break;
      }
      return btn;
    },
	    /**
     * @returns the action template specific for weather
     */
    getTemplateAction: function() {
      return _.template(ActionTemplate); 
    },

  });
  return Weather;
});
