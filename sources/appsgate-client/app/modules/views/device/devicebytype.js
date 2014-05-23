define([
    "app",
    "text!templates/devices/list/deviceListByCategory.html",
], function(App, deviceListByCategoryTemplate) {

    var DeviceByTypeView = {};
    /**
     * Render the list of devices of a given type
     */
    DeviceByTypeView = Backbone.View.extend({
        tpl: _.template(deviceListByCategoryTemplate),
        events: {
            "click button.toggle-plug-button": "onTogglePlugButton",
            "click button.blink-lamp-button": "onBlinkLampButton",
            "click button.toggle-lamp-button": "onToggleLampButton",
            "click button.toggle-actuator-button": "onToggleActuatorButton"
        },
        /**
         * Listen to the updates on the devices of the category and refresh if any
         * 
         * @constructor
         */
        initialize: function() {
            var self = this;

            devices.getDevicesByType()[this.id].forEach(function(device) {
                self.listenTo(device, "change", self.render);
                self.listenTo(device, "remove", self.render);
            });
        },
        /**
         * Callback to toggle a plug
         * 
         * @param e JS mouse event
         */
        onTogglePlugButton: function(e) {
            e.preventDefault();

            var plug = devices.get($(e.currentTarget).attr("id"));

            // value can be string or boolean
            // string
            if (typeof plug.get("plugState") === "string") {
                if (plug.get("plugState") === "true") {
                    plug.set("plugState", "false");
                } else {
                    plug.set("plugState", "true");
                }
                // boolean
            } else {
                if (plug.get("plugState")) {
                    plug.set("plugState", "false");
                } else {
                    plug.set("plugState", "true");
                }
            }

            // send the message to the backend
            plug.save();

            return false;
        },
        /**
         * Callback to toggle a lamp
         * 
         * @param e JS mouse event
         */
        onToggleLampButton: function(e) {
            e.preventDefault();

            var lamp = devices.get($(e.currentTarget).attr("id"));
            // value can be string or boolean
            // string
            if (typeof lamp.get("value") === "string") {
                if (lamp.get("value") === "true") {
                    lamp.set("value", "false");
                } else {
                    lamp.set("value", "true");
                }
                // boolean
            } else {
                if (lamp.get("value")) {
                    lamp.set("value", "false");
                } else {
                    lamp.set("value", "true");
                }
            }

            // send the message to the backend
            lamp.save();

            return false;
        },
        /**
         * Callback to blink a lamp
         *
         * @param e JS mouse event
         */
        onBlinkLampButton: function(e) {
            e.preventDefault();
            var lamp = devices.get($(e.currentTarget).attr("id"));
            // send the message to the backend
            lamp.remoteControl("blink", []);

            return false;
        },
        /**
         * Callback to toggle an actuator
         * 
         * @param e JS mouse event
         */
        onToggleActuatorButton: function(e) {
            e.preventDefault();

            var actuator = devices.get($(e.currentTarget).attr("id"));
            // value can be string or boolean
            // string
            if (typeof actuator.get("value") === "string") {
                if (actuator.get("value") === "true") {
                    actuator.set("value", "false");
                } else {
                    actuator.set("value", "true");
                }
                // boolean
            } else {
                if (actuator.get("value")) {
                    actuator.set("value", "false");
                } else {
                    actuator.set("value", "true");
                }
            }

            // send the message to the backend
            actuator.save();

            return false;
        },
        /**
         * Render the list
         */
        render: function() {
            if (!appRouter.isModalShown) {
                this.$el.html(this.tpl({
                    type: this.id,
                    places: places
                }));

                // translate the view
                this.$el.i18n();

                // resize the list
                this.resizeDiv($(".contents-list"));

                return this;
            }
        }
    });
    return DeviceByTypeView;
});