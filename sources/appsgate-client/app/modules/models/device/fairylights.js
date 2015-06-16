define([
  "app",
  "models/device/device",
  "text!templates/program/nodes/FairyLightsActionNode.html"

], function (App, Device, ActionTemplate) {

	var FairyLights = {};
	/**
	 * @class Device.FairyLights
	 */
	FairyLights = Device.extend({
		/**
		 * @constructor
		 */
		initialize: function () {
			var self = this;

			FairyLights.__super__.initialize.apply(this, arguments);

			// setting default friendly name if none exists
			if (typeof this.get("name") === "undefined" || this.get("name") === "") {
				this.generateDefaultName($.i18n.t("devices.fairylights.name.singular"));
			}
		},
		/**
		 * @returns the action template specific
		 */
		getTemplateAction: function () {
			return _.template(ActionTemplate);
		},
		/**
		 *return the list of available actions
		 */
		getActions: function () {
			return ["setAllColorLight", "setOneColorLight", "setColorPattern", "singleChaserAnimation", "roundChaserAnimation", "setColorAnimation", "colorNextLight", "colorPreviousLight"];
		},
		/**
		 * return the keyboard code for a given action
		 */
		getKeyboardForAction: function (act) {
			var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' group-id='" + this.get("type") + "'></button>");
			var v = this.getJSONAction("mandatory");
			switch (act) {
			case "setAllColorLight":
				$(btn).append("<span>" + $.i18n.t('devices.fairylights.keyboard.setAllColorLight', {
					myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.fairylights.name.singular') + "</span>"
				}));
				v.methodName = "setAllColorLight";
				v.args = [{
					"type": "String",
					"value": "#ffffff"
          		}];
				v.phrase = "devices.fairylights.language.setAllColorLight";
				$(btn).attr("json", JSON.stringify(v));
				break;
			case "setOneColorLight":
				$(btn).append("<span>" + $.i18n.t('devices.fairylights.keyboard.setOneColorLight', {
					myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.fairylights.name.singular') + "</span>"
				}));
				v.methodName = "setOneColorLight";
				v.args = [{
					"type": "int",
					"value": "0"
            	}, {
					"type": "String",
					"value": "#ffffff"
          		}];
				v.phrase = "devices.fairylights.language.setOneColorLight";
				$(btn).attr("json", JSON.stringify(v));
				break;
			case "setColorPattern":
				$(btn).append("<span>" + $.i18n.t('devices.fairylights.keyboard.setColorPattern', {
					myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.fairylights.name.singular') + "</span>"
				}));
				v.methodName = "setColorPattern";
				v.args = [{
					"type": "JSONArray",
					"value": []
          		}];
				v.phrase = "devices.fairylights.language.setColorPattern";
				$(btn).attr("json", JSON.stringify(v));
				break;
			case "singleChaserAnimation":
				$(btn).append("<span>" + $.i18n.t('devices.fairylights.keyboard.singleChaserAnimation', {
					myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.fairylights.name.singular') + "</span>"
				}));
				v.methodName = "singleChaserAnimation";
				v.args = [{
					"type": "int", // start
					"value": "0"
          		}, {
					"type": "int", // end
					"value": "24"
          		}, {
					"type": "String",
					"value": "#ffffff"
          		}, {
					"type": "int", // tail
					"value": "1"
          		}];
				v.phrase = "devices.fairylights.language.singleChaserAnimation";
				$(btn).attr("json", JSON.stringify(v));
				break;
			case "roundChaserAnimation":
				$(btn).append("<span>" + $.i18n.t('devices.fairylights.keyboard.roundChaserAnimation', {
					myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.fairylights.name.singular') + "</span>"
				}));
				v.methodName = "roundChaserAnimation";
				v.args = [{
					"type": "int", // start
					"value": "0"
          		}, {
					"type": "int", // end
					"value": "24"
		  		}, {
					"type": "String",
					"value": "#ffffff"
		  		}, {
					"type": "int", // tail
					"value": "1"
          		}, {
					"type": "int", // nb rounds
					"value": "1"
        		}];
				v.phrase = "devices.fairylights.language.roundChaserAnimation";
				$(btn).attr("json", JSON.stringify(v));
				break;
			case "setColorAnimation":
				$(btn).append("<span>" + $.i18n.t('devices.fairylights.keyboard.setColorAnimation', {
					myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.fairylights.name.singular') + "</span>"
				}));
				v.methodName = "setColorAnimation";
				v.args = [{
					"type": "int", // start
					"value": "0"
				}, {
					"type": "int", // end
					"value": "24"
          		}, {
					"type": "String",
					"value": "#ffffff"
          		}];
				v.phrase = "devices.fairylights.language.setColorAnimation";
				$(btn).attr("json", JSON.stringify(v));
				break;
			case "colorNextLight":
				$(btn).append("<span>" + $.i18n.t('devices.fairylights.keyboard.colorNextLight', {
					myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.fairylights.name.singular') + "</span>"
				}));
				v.methodName = "changeContiguousLights";
				v.args = [{
					"type": "int", // nb
					"value": "1"
				}, {
					"type": "String",
					"value": "#ffffff"
          		}];
				v.phrase = "devices.fairylights.language.colorNextLight";
				$(btn).attr("json", JSON.stringify(v));
				break;
			case "colorPreviousLight":
				$(btn).append("<span>" + $.i18n.t('devices.fairylights.keyboard.colorPreviousLight', {
					myVar: "<span class='highlight-placeholder'>" + $.i18n.t('devices.fairylights.name.singular') + "</span>"
				}));
				v.methodName = "changeContiguousLights";
				v.args = [{
					"type": "int", // nb
					"value": "-1"
          		}, {
					"type": "String",
					"value": "#ffffff"
          		}];
				v.phrase = "devices.fairylights.language.colorPreviousLight";
				$(btn).attr("json", JSON.stringify(v));
				break;
			default:
				console.error("unexpected action found for FairyLights: " + act);
				btn = null;
				break;
			}
			return btn;
		},

		setAllColorLight: function (color) {
			this.remoteControl("setAllColorLight", [{
				"type": "String",
				"value": color,
				"name": "color"
			}]);
		},

		setOneColorLight: function (lightNumber, color) {
			this.remoteControl("setOneColorLight", [{
				"type": "int",
				"value": lightNumber,
				"name": "lightNumber"
			}, {
				"type": "String",
				"value": color,
				"name": "color"
			}]);
		},

		addPattern: function (patternName, pattern) {
			this.remoteControl("addUpdateColorPattern", [{
				"type": "String",
				"value": patternName,
				"name": "patternName"
			}, {
				"type": "JSONArray",
				"value": pattern,
				"name": "pattern"
			}]);
		},

		removePattern: function (patternName) {
			this.remoteControl("removeColorPattern", [{
				"type": "String",
				"value": patternName,
				"name": "patternName"
			}]);
		},

		setPattern: function (patternName) {
			this.remoteControl("setColorPattern", [{
				"type": "String",
				"value": patternName,
				"name": "patternName"
			}]);
		}

	});
	return FairyLights;
});