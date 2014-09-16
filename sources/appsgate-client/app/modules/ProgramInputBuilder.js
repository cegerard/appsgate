/**
 * Created by thibaud on 19/05/2014.
 */
define([
    "text!templates/program/nodes/defaultActionNode.html",
    "text!templates/program/nodes/deviceNode.html",
    "text!templates/program/nodes/serviceNode.html",
    "text!templates/program/nodes/ifNode.html",
    "text!templates/program/nodes/whenNode.html",
    "text!templates/program/nodes/defaultEventNode.html",
    "text!templates/program/nodes/stateNode.html",
    "text!templates/program/nodes/keepStateNode.html",
    "text!templates/program/nodes/whileNode.html",
    "text!templates/program/nodes/booleanExpressionNode.html",
    "text!templates/program/nodes/comparatorNode.html",
    "text!templates/program/nodes/numberNode.html",
    "text!templates/program/nodes/waitNode.html",
    "text!templates/program/nodes/programNode.html",
    "text!templates/program/nodes/defaultPropertyNode.html",
    "text!templates/program/nodes/selectNode.html"
], function(dfltActionTpl, deviceTpl, serviceTpl, ifTpl, whenTpl, dfltEventTpl, stateTpl, keepStateTpl, whileTpl, booleanExpressionTpl, comparatorTpl, numberTpl, waitTpl, programTpl, dfltPropertyTpl, selectNodeTpl) {
    var ProgramInputBuilder = {};
    // router
    ProgramInputBuilder = Backbone.Model.extend({
        tplDefaultActionNode: _.template(dfltActionTpl),
        tplDeviceNode: _.template(deviceTpl),
        tplServiceNode: _.template(serviceTpl),
        tplIfNode: _.template(ifTpl),
        tplWhenNode: _.template(whenTpl),
        tplEventNode: _.template(dfltEventTpl),
        tplStateNode: _.template(stateTpl),
        tplKeepStateNode: _.template(keepStateTpl),
        tplWhileNode: _.template(whileTpl),
        tplBooleanExpressionNode: _.template(booleanExpressionTpl),
        tplComparatorNode: _.template(comparatorTpl),
        tplNumberNode: _.template(numberTpl),
        tplWaitNode: _.template(waitTpl),
        tplProgramNode: _.template(programTpl),
        tplDefaultPropertyNode: _.template(dfltPropertyTpl),
        tplSelectNode: _.template(selectNodeTpl),


        initialize: function() {
        },

        /**
         * buildInputFromNode is the only 'public' method, it takes as input
         * the json representation of a SPOK program and build the corresponding representation in HTML
         *
         * @param jsonNode
         * @param currentNode
         * @returns {string}
         */
        buildInputFromNode: function(jsonNode, currentNode) {
            var self = this;

            param = {
                node: jsonNode,
                currentNode : currentNode,
                engine: this
            };
            var deletable = false;
            var input = "";
            switch (jsonNode.type) {
                case "action":
                    deletable = true;
                    input += this.buildActionNode(param);
                    break;
                case "if":
                    deletable = true;
                    input += this.tplIfNode(param);
                    break;
                case "booleanExpression":
                    deletable = true;
                    input += this.tplBooleanExpressionNode(param);
                    break;
                case "comparator":
                    input += this.buildComparatorNode(param, currentNode);
                    break;
                case "when":
                case "whenImp":
                    deletable = true;
                    input += this.tplWhenNode(param);
                    break;
                case "device":
                    param.node.name = this.getDeviceName(param.node.value);
                    input += this.tplDeviceNode(param);
                    break;
                case "select":
                    input += this.tplSelectNode(param);
                    break;
                case "service":
                    param.node.name = this.getServiceName(param.node.value);
                    input += this.tplServiceNode(param);
                    break;
                case "event":
                    deletable = true;
                    input += this.buildEventNode(param);
                    break;
                case "state":
                case "maintanableState":
                    deletable = true;
                    input = this.buildStateNode(param);
                    break;
                case "property":
                    deletable = true;
                    input += this.buildPropertyNode(param);
                    break;
                case "while":
                    deletable = true;
                    input += this.tplWhileNode(param);
                    break;
                case "keepState":
                    input += this.tplKeepStateNode(param);
                    break;
                case "empty":
                    input += "<div class='btn btn-default btn-prog input-spot' id='" + jsonNode.iid + "'><span data-i18n='language.nothing-keyword'/></div>";
                    break;
                case "mandatory":
                    input += "<div class='btn btn-default btn-prog input-spot mandatory-spot' id='" + jsonNode.iid + "'><span data-i18n='language.mandatory-keyword'/></div>";
                    break;
                case "seqRules":
                    input+= "<div class='main-block-node'><h2><span data-i18n='language.actions'/></h2>";
                    jsonNode.rules.forEach(function(rule) {
                        if (rule !== jsonNode.rules[0]) {
                            input += "<div class='row'><div class='btn btn-default btn-prog btn-then btn-primary disabled'><span data-i18n='language.op-then-rule'/></div></div>";
                        }
                        input += self.buildInputFromNode(rule, currentNode);
                    });
                    input+="</div>";
                    break;
                case "setOfRules":
                    jsonNode.rules.forEach(function(rule) {
                        if (rule !== jsonNode.rules[0]) {
                            input += "<div class='row'><div class='btn btn-default btn-prog btn-and btn-primary disabled'><span data-i18n='language.op-and-rule'/></div></div>";
                        }
                        input += self.buildInputFromNode(rule, currentNode);
                    });
                    break;
                case "boolean":
                    input += "<button class='btn btn-prog btn-primary' id='" + jsonNode.iid + "'><span>" + jsonNode.value + "</span></button>";
                    break;
                case "number":
                    input += this.tplNumberNode(param);
                    break;
                case "wait":
                    deletable = true;
                    input += this.tplWaitNode(param);
                    break;
                case "programCall":
                    input += "<button class='btn btn-prog btn-prog-prog' id='" + jsonNode.iid + "'><span>" + jsonNode.name + "</span></button>";
                    break;
                default:
                    input += "<button class='btn btn-prog btn-primary' id='" + jsonNode.iid + "'><span>" + jsonNode.type + "</span></button>";
                    break;
            }

            // For only some kind of node we add a delete button
            if (deletable == true) {
                var supprClasses = "";
                if (currentNode == jsonNode.iid) {
                    supprClasses = "glyphicon glyphicon-trash";
                }
                input = "<div class='btn-current'>"
                    + input
                    + "<div class='btn-prog btn-trash " + supprClasses + "' id='" + jsonNode.iid + "' style='right:5px;position:absolute;top:0px;'></div></div>";

            }

            return input;
        },

        buildActionNode: function(param) {
            if (param.node.target.deviceType || param.node.target.type === "select") {
                return devices.getTemplateByType('action',param.node.target.deviceType, param);
            }
            if (param.node.target.serviceType) {
                return services.getTemplateByType('action',param.node.target.serviceType, param);
            }
            if (param.node.target.type === "programCall" || param.node.target.type === "programs") {
                return this.tplProgramNode(param);
            }
            return this.tplDefaultActionNode(param);
        },
        buildPropertyNode: function(param) {
            if (param.node.target.deviceType) {
                return devices.getTemplateByType('property',param.node.target.deviceType, param);
            }
            if (param.node.target.serviceType) {
                return services.getTemplateByType('property',param.node.target.serviceType, param);
            }
            return this.tplDefaultPropertyNode(param);
        },
        buildStateNode: function(param) {
            if (param.node.object.deviceType) {
                return devices.getTemplateByType('state',param.node.object.deviceType, param);
            }
            if (param.node.object.serviceType) {
                return services.getTemplateByType('state',param.node.object.serviceType, param);
            }
            return this.tplStateNode(param);
        },
        buildEventNode: function(param) {
            var result = "";
            if (param.node.source.deviceType) {
                return devices.getTemplateByType('event',param.node.source.deviceType, param);
            }
            if (param.node.source.serviceType) {
                return services.getTemplateByType('event',param.node.source.serviceType, param);

            }
            return this.tplEventNode(param);
        },
        // Hack for a simple prestenation when X == true, we only show X
        buildComparatorNode: function(param, currentNode) {
            try {
                if(param.node.comparator === "==" && param.node.rightOperand.type === "boolean" && param.node.rightOperand.value === "true") {
                    return this.buildInputFromNode(param.node.leftOperand, currentNode);
                } else {
                    return this.tplComparatorNode(param);
                }
            } catch (e) {
                return this.tplComparatorNode(param);
            }

        },

        getDeviceName: function(id) {
            var deviceName;
            if (devices.get(id) == undefined) {
                console.error("device not found: " + id);
                deviceName = "UNKNOWN DEVICE";
            } else if (devices.get(id).get("name") !== "") {
                deviceName = devices.get(id).get("name");
            } else {
                deviceName = devices.get(id).get("id");
            }
            return deviceName;
        },
        getServiceName: function(id) {
            if (services.get(id) == undefined) {
                console.error("service not found: " + id);
                return "UNKNOWN SERVICE";
            }
            return services.get(id).get("name");
        }

    });

        return ProgramInputBuilder;
});
