define([
    "app",
    "models/service/service",
    "text!templates/program/nodes/weatherPropertyNode.html",
    "text!templates/program/nodes/weatherStateNode.html"

], function(App, Service, PropertyTemplate, StateTemplate) {

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

            // setting default friendly name if none exists
            if (this.get("name") === "") {
                this.set("name", this.get("location"));
            }
        },

        /**
         * return the list of available events
         */
        getEvents: function() {
            return ["sunrise","sunset"];
        },
        /**
         * return the keyboard code for a given event
         */
        getKeyboardForEvent: function(evt){
            var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
            var v = this.getJSONEvent("mandatory");
            switch(evt) {
                case "sunrise":
                    $(btn).append("<span data-i18n='services.weather.event.sunrise'/>");
                    v.eventName = "daylightEvent";
                    v.eventValue = "sunrise";
                    v.phrase = "services.weather.event.sunrise";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "sunset":
                    $(btn).append("<span data-i18n='services.weather.event.sunset'/>");
                    v.eventName = "daylightEvent";
                    v.eventValue = "sunset";
                    v.phrase = "services.weather.event.sunset";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                default:
                    console.error("unexpected event found for Weather : " + evt);
                    btn = null;
                    break;
            }
            return btn;
        },


        /**
         * return the list of available states (only those returning a boolean)
         */
        getStates: function(which) {
		  if (which == "state") {
			//code
            return ["isCurrentlyDaylight", "isCurrentWeatherCode", "isForecastWeatherCode"];
		  }
		  return [];
        },

        getKeyboardForState: function(state,which){
            var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
            //var v = this.getJSONState("mandatory");
            var v = {"type": "state", "object": {"iid": "X", "type": 'mandatory', "serviceType":this.get("type")}, "iid": "X"};
            switch(state) {
                case "isCurrentWeatherCode":
                    $(btn).append("<span data-i18n='keyboard.weather.is-current-weather-code-state'/>");

                    v.methodName = "isWeatherSimplifiedCodeForecast";
                    v.returnType = "boolean";
                    v.name = "currentWeatherCodeState";


                    v.args = [ {"type":"int", "value": "0"}];

                    v.phrase = "keyboard.weather.is-current-weather-code-state";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "isForecastWeatherCode":
                    $(btn).append("<span data-i18n='keyboard.weather.is-forecast-weather-code-state'/>");

                    v.methodName = "isWeatherSimplifiedCodeForecast";
                    v.returnType = "boolean";
                    v.name = "forecastWeatherCodeState";


                    v.args = [ {"type":"int", "value": "0"},
                        {"type":"int", "value": "0"}];

                    v.phrase = "keyboard.weather.is-forecast-weather-code-state";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "isCurrentlyDaylight":
                    $(btn).append("<span data-i18n='keyboard.weather.currently-daylight'/>");

                    v.methodName = "isCurrentlyDaylight";
                    v.name = "daylightState";
                    v.returnType = "boolean";

                    v.phrase = "keyboard.weather.currently-daylight";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                default:
                    console.error("unexpected state found for Weather: " + state);
                    btn = null;
                    break;
            }
            return btn;

        },

        /**
         * @returns the default template state
         */
        getTemplateState: function() {
            return _.template(StateTemplate);
        },

        /**
         * return the list of available properties
         */
        getProperties: function() {
            return [ "getCurrentTemperature",
                //"getCurrentWeatherCode", disabled because, it is ambiguous with the STATE which is near
                //"getForecastWeatherCode", disabled because, it is ambiguous with the STATE which is near
                "getForecastMinTemperature",
                "getForecastMaxTemperature"];
        },
        /**
         * return the keyboard code for a given property
         */
        getKeyboardForProperty: function(property) {
            var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
            var v = {"type": "property", "target": {"iid": "X", "type": 'mandatory', "serviceType":this.get("type")}, "iid": "X"};

            //var v = this.getJSONProperty("mandatory");
            switch(property) {
                case "getCurrentTemperature":
                    $(btn).append("<span data-i18n='keyboard.weather.current-temperature'><span>");

                    v.methodName = property;
                    v.returnType = "number";
                    v.phrase = "keyboard.weather.current-temperature";
                    $(btn).attr("json", JSON.stringify(v));
                    break;

                case "getCurrentWeatherCode":
                    $(btn).append("<span data-i18n='keyboard.weather.current-weather-code-state'><span>");

                    v.methodName = property;
                    v.returnType = "number";
                    v.phrase = "keyboard.weather.current-weather-code-state";
                    $(btn).attr("json", JSON.stringify(v));
                    break;

                case "getForecastWeatherCode":
                    $(btn).append("<span data-i18n='keyboard.weather.forecast-weather-code-state'><span>");

                    v.methodName = property;
                    v.args = [
                        {"type":"int", "value": "0"}];
                    v.returnType = "number";
                    v.phrase = "keyboard.weather.weather.forecast-weather-code-state";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                // TODO : Add the other properties

                case "getForecastMinTemperature":
                    $(btn).append("<span data-i18n='keyboard.weather.forecast-temperature-min'><span>");

                    v.methodName = property;
                    v.args = [
                        {"type":"int", "value": "0"}];
                    v.returnType = "number";
                    v.phrase = "keyboard.weather.forecast-temperature-min";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "getForecastMaxTemperature":
                    $(btn).append("<span data-i18n='keyboard.weather.forecast-temperature-max'><span>");

                    v.methodName = property;
                    v.args = [
                        {"type":"int", "value": "0"}];
                    v.returnType = "number";
                    v.phrase = "keyboard.weather.forecast-temperature-max";
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
         * @returns the default template state
         */
        getTemplateProperty: function() {
            return _.template(StateTemplate);
        }
    });
    return Weather;
});
