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
         * Weather Events (TODO) should only be to notify of a weather change ( ???)
         */


        /**
         * return the list of available states (only those returning a boolean)
         */
        getStates: function() {
            return ["isCurrentlyDaylight"];//"isWeatherSimplifiedCodeForecast"];
        },

        getKeyboardForState: function(state,which){
            var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
            //var v = this.getJSONState("mandatory");
            var v = {"type": "state", "object": {"iid": "X", "type": 'mandatory', "serviceType":this.get("type")}, "iid": "X"};
            switch(state) {
                case "isWeatherSimplifiedCodeForecast":
                    $(btn).append("<span data-i18n='keyboard.is-weather-code-state'/>");

                    v.methodName = "isWeatherSimplifiedCodeForecast";
                    v.returnType = "boolean";

                    v.args = [ {"type":"String", "value": "Grenoble"},
                        {"type":"int", "value": "0"},
                        {"type":"int", "value": "0"}];

                    v.phrase = "keyboard.is-weather-code-state";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "isCurrentlyDaylight":
                    $(btn).append("<span data-i18n='keyboard.currently-daylight'/>");

                    v.methodName = "isCurrentlyDaylight";
                    v.name = "daylightState";
                    v.returnType = "boolean";

                    v.phrase = "keyboard.currently-daylight";
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
                "getTomorrowWeatherCode",
                "getTomorrowMinTemperature",
                "getTomorrowMaxTemperature",
                "getCurrentWeatherCode" ];
        },
        /**
         * return the keyboard code for a given property
         */
        getKeyboardForProperty: function(property) {
            var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
            var v = this.getJSONProperty("mandatory");
            switch(property) {
                case "getCurrentWeatherCode":
                    $(btn).append("<span data-i18n='keyboard.current-weather-code-state'><span>");

                    v.methodName = property;
                    v.returnType = "number";
                    v.phrase = "keyboard.current-weather-code-state";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "getTomorrowMinTemperature":
                    $(btn).append("<span data-i18n='keyboard.tomorrow-temperature-min'><span>");

                    v.methodName = property;
                    v.returnType = "number";
                    v.phrase = "keyboard.tomorrow-temperature-min";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "getTomorrowMaxTemperature":
                    $(btn).append("<span data-i18n='keyboard.tomorrow-temperature-max'><span>");

                    v.methodName = property;
                    v.returnType = "number";
                    v.phrase = "keyboard.tomorrow-temperature-max";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                case "getCurrentTemperature":
                    $(btn).append("<span data-i18n='keyboard.current-temperature'><span>");

                    v.methodName = property;
                    v.returnType = "number";
                    v.phrase = "keyboard.current-temperature";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                // TODO : Add the other properties
                case "getTomorrowWeatherCode":
                    $(btn).append("<span data-i18n='keyboard.tomorrow-weather-code-state'><span>");

                    v.methodName = property;
                    v.returnType = "number";
                    v.phrase = "keyboard.tomorrow-weather-code-state";
                    $(btn).attr("json", JSON.stringify(v));
                    break;
                // TODO : Add the other properties
                default:
                    console.error("unexpected service state found for Weather : " + property);
                    btn = null;
                    break;
            }
            return btn;
        },




    });
    return Weather;
});
