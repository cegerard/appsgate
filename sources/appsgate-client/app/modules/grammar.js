define([
    "app",
    "text!rsrc/conToAbs.peg"
], function(App, grammar) {

    require(["peg"]);

    var ProgramGrammar = {};
	var orderedArgs = [ "state", "stateTarget", "expBool", "target", "source", "object", "rules", "events","leftOperand", "rightOperand","seqRulesTrue", "seqRulesFalse", "seqRulesThen", "rulesThen", "left", "right", "what", "where", "devices", "value", "waitFor"];
    ProgramGrammar = Backbone.Model.extend({
        initialize: function() {
            this.grammar = this.build(grammar);
        },
        build: function(g) {
            console.log("Building the parser...");
            try {
                this.parser = PEG.buildParser(g, {
                    cache: false,
                    trackLineAndColumn: true
                });
            } catch (e) {
                console.error("unable to build the parser");
                console.error(e);
            }
            return null;
        },
        parse: function(jsonObj, currentNode) {
            try {
                if (jsonObj) {
                    var s = this.parseNode(jsonObj, currentNode);
                    console.debug(s);
                    this.parser.parse(s);
                } else {
                    console.warn("undefined json");
                }
                return null;
            } catch (e) {
                console.debug(e.message + " on " + e.id);
                return this.tryParse(s, e);
            }
        },
        tryParse: function(toParse, e) {
            if (e.id) {
                id = e.id;
            } else {
                var l = e.offset - 5;
                id = toParse.substr(l, 4);
                while (isNaN(id)) {
                    id = id.substr(1);
                }
				e.id = id;
            }
            return e;
        },
		parseNode: function(obj, currentNode) {
            if (typeof obj == "string") {
                console.log("String found");
                console.warn("Select nodes not supported yet.")
                return "";
            }
            var args = "";
            if (obj.length) {
                for (var k in obj) {
                    args += this.parseNode(obj[k], currentNode) + " ";
                }
                return args;
            }
            if (obj.length == 0) {
                return "";
            }
            var type = obj.iid + ":";
            if (currentNode == -1) {
                if (obj.type == "mandatory" && obj.deviceType) {
                    return type + "/" + obj.deviceType + "/";
                }
                if (obj.type == "mandatory" && obj.serviceType) {
                    return type + "|" + obj.serviceType + "|";
                }
            }
            if (obj.iid == currentNode) {
                if (obj.deviceType) {
                    return type + "/" + obj.deviceType + "/";
                }
                if (obj.serviceType) {
                    return type + "|" + obj.serviceType + "|";
                }
//                if (obj.iid == currentNode && obj.type == "empty") {
                    return type + "selected";
//                }
            }
            if (obj.type) {
                type += obj.type;
            }

            for (var i in orderedArgs) {
				k = orderedArgs[i];
                if (typeof obj[k] === "object") {
                    if (obj[k].length != undefined) {
                        args += "[" + this.parseNode(obj[k], currentNode) + "]";
                    } else {
                        args += "(" + this.parseNode(obj[k], currentNode) + ")";
                    }
                }
            }
            return type + args;
        }



    });
    return ProgramGrammar;
});
