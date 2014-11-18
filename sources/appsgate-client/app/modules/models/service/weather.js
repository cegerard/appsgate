define([
    "app",
    "models/service/service",
    "text!templates/program/nodes/weatherPropertyNode.html",
    "text!templates/program/nodes/weatherStateNode.html"

], function (App, Service, PropertyTemplate, StateTemplate) {

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
        initialize: function () {
            Weather.__super__.initialize.apply(this, arguments);

            // setting default friendly name if none exists
            if (this.get("name") == undefined  || this.get("name") === "") {
                this.set("name", this.get("location"));
            }
        },

        /**
         * return the list of available events
         */
        getEvents: function () {
            return ["sunrise", "sunset"];
        },
        /**
         * return the keyboard code for a given event
         */
        getKeyboardForEvent: function (evt) {
            var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
            var v = this.getJSONEvent("mandatory");
            switch (evt) {
            case "sunrise":
                $(btn).append("<span data-i18n='services.weather.keyboard.sunrise'/>");
                v.eventName = "daylightEvent";
                v.eventValue = "sunrise";
                v.phrase = "services.weather.language.sunrise";
                $(btn).attr("json", JSON.stringify(v));
                break;
            case "sunset":
                $(btn).append("<span data-i18n='services.weather.keyboard.sunset'/>");
                v.eventName = "daylightEvent";
                v.eventValue = "sunset";
                v.phrase = "services.weather.language.sunset";
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
        getStates: function (which) {
            if (which == "state") {
                //code
                return ["daylightState", "moonlightState"];
            }
            return [];
        },

        getKeyboardForState: function (state, which) {
            var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
            //var v = this.getJSONState("mandatory");
            var v = {
                "type": "state",
                "object": {
                    "iid": "X",
                    "type": 'mandatory',
                    "serviceType": this.get("type")
                },
                "iid": "X"
            };
            switch (state) {
            case "daylightState":
                $(btn).append("<span data-i18n='services.weather.keyboard.currently-daylight'/>");

				v.name = state;

                v.phrase = "services.weather.language.currently-daylight";
                $(btn).attr("json", JSON.stringify(v));
                break;
            case "moonlightState":
                $(btn).append("<span data-i18n='services.weather.keyboard.currently-moonlight'/>");

				v.name = state;

                v.phrase = "services.weather.language.currently-moonlight";
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
        getTemplateState: function () {
            return _.template(StateTemplate);
        },

        /**
         * return the list of available properties
         */
        getProperties: function () {
            return [
                //"getCurrentTemperature", // Removing those current weather states to remove some buttons on the HMI
                //"getCurrentWeatherCode",
                //"getForecastMinTemperature",
                //"getForecastMaxTemperature",
                "getForecastWeatherCode"
            ];
        },
        /**
         * return the keyboard code for a given property
         */
        getKeyboardForProperty: function (property) {
            var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
            var v = {
                "type": "property",
                "target": {
                    "iid": "X",
                    "type": 'mandatory',
                    "serviceType": this.get("type")
                },
                "iid": "X"
            };

            //var v = this.getJSONProperty("mandatory");
            switch (property) {
            case "getCurrentTemperature":
                $(btn).append("<span data-i18n='services.weather.keyboard.current-temperature'><span>");

                v.methodName = property;
                v.returnType = "number";
                v.phrase = "services.weather.language.current-temperature";
                v.unit = "&deg;C";
                $(btn).attr("json", JSON.stringify(v));
                break;

            case "getCurrentWeatherCode":
                $(btn).append("<span data-i18n='services.weather.keyboard.current-weather-code-state'><span>");

                v.methodName = "getCurrentWeatherString";
                v.returnType = "scale";
                v.phrase = "services.weather.language.current-weather-code-state";
                $(btn).attr("json", JSON.stringify(v));
                break;

            case "getForecastWeatherCode":
                $(btn).append("<span data-i18n='services.weather.keyboard.forecast-weather-code-state'><span>");

                v.methodName = "getForecastWeatherString";
                v.args = [
                    {
                        "type": "int",
                        "value": "0"
                    }]
                v.returnType = "scale";
                v.phrase = "services.weather.language.forecast-weather-code-state";
                $(btn).attr("json", JSON.stringify(v));
                break;
                // TODO : Add the other properties

            case "getForecastMinTemperature":
                $(btn).append("<span data-i18n='services.weather.keyboard.forecast-temperature-min'><span>");

                v.methodName = property;
                v.args = [
                    {
                        "type": "int",
                        "value": "0"
                    }];
                v.returnType = "number";
                v.phrase = "services.weather.language.forecast-temperature-min";
                v.unit = "&deg;C";
                $(btn).attr("json", JSON.stringify(v));
                break;
            case "getForecastMaxTemperature":
                $(btn).append("<span data-i18n='services.weather.keyboard.forecast-temperature-max'><span>");

                v.methodName = property;
                v.args = [
                    {
                        "type": "int",
                        "value": "0"
                    }];
                v.returnType = "number";
                v.phrase = "services.weather.language.forecast-temperature-max";
                v.unit = "&deg;C";
                $(btn).attr("json", JSON.stringify(v));
                break;
            default:
                console.error("unexpected service state found for Weather : " + property);
                btn = null;
                break;
            }
            return btn;
        },
        getScale: function () {
            var arrayScale = [
                {
                    "value": "sunny",
                    "label": "services.weather.language.weather-code-0"
                    },
                {
                    "value": "cloudy",
                    "label": "services.weather.language.weather-code-1"
                    },
                {
                    "value": "rainy",
                    "label": "services.weather.language.weather-code-2"
                    },
                {
                    "value": "snowy",
                    "label": "services.weather.language.weather-code-3"
                    },
                {
                    "value": "thunder",
                    "label": "services.weather.language.weather-code-4"
                    },
                {
                    "value": "foggy",
                    "label": "services.weather.language.weather-code-5"
                    },
                {
                    "value": "other",
                    "label": "services.weather.language.weather-code-6"
                    },
                {
                    "value": "special",
                    "label": "services.weather.language.weather-code-7"
                    },
                ];
            return arrayScale;

        },
        /**
         * Override its synchronization method to send a notification on the network
         */
        sync: function(method, model) {
            switch (method) {
                case "create":
                case "update":
                    // create an id to the place
                communicator.sendMessage({
                    method: "addLocationObserver",
                    //method: "addLocationObserverFromWOEID",
                    args: [{type:"String", value:model.attributes.location}],
                    TARGET: "EHMI",
                    id:"addLocationObserver"
                    }
                );
                    break;
                case "delete":
				  communicator.sendMessage({
					method: "removeLocationObserver",
					args: [{type:"String", value:model.attributes.location}],
					TARGET: "EHMI",
					id:"removeLocationObserver"
				  });
				  break;
                default:
                  break;
            }
        },

        /**
         * @returns the default template state
         */
        getTemplateProperty: function () {
            return _.template(StateTemplate);
        }
    });
    return Weather;
});
