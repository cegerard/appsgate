define([
    "app",
    "text!rsrc/conToAbs.peg"
], function(App, grammar) {

    require(["peg"]);

    var ProgramGrammar = {};
	var orderedArgs = [ "state", "stateTarget", "expBool", "target", "source", "object", "param", "rules", "events","leftOperand", "rightOperand","seqRulesTrue", "seqRulesFalse", "seqRulesThen", "rulesThen", "left", "right", "devices", "value", "waitFor", "args"];
    ProgramGrammar = Backbone.Model.extend({
        initialize: function() {
            this.grammar = this.build(grammar);
        },
        build: function(g) {
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
		
		/**
		 * Method that return the id selected depending whether the program is correct or not
		 */
		getCurrentNode: function(jsonObj, id) {
			var n = this.parse(jsonObj, id);
			if (n != null) {
				return n.id;
			} else {
				return id;
			}

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
		/**
		 *
		 */
		parseNode: function(obj, currentNode) {
            if (typeof obj == "string") {
                console.log("String found");
                //console.warn("Select nodes not supported yet.")
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
			var typedType = "";
			if (obj.deviceType !== undefined) {
				typedType = "/" + obj.deviceType + "/";
			}
			if (obj.serviceType) {
				typedType = "|" + obj.serviceType + "|";
			}
			
			var prefix = "";
			// If undefined, paramater with just a value
			if (obj.iid !== undefined) {
				var prefix = obj.iid + ":";
			}
            
            if (obj.iid == currentNode || obj.mandatory) {
				if (obj.type == "param") {
					return prefix + "param(" + typedType + ", '" +  obj.param + "')";
				}
                if (obj.type == "programs" || obj.type == "programCall") {
                    return prefix + "programs";
				}
				if (typedType != "") {
					return prefix + typedType;
				}
                return prefix + "SELECTED";
            }
            if (obj.type) {
                prefix += obj.type;
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
            return prefix + args;
        },
		/**
		 * Function that returns true if the program is empty, false otherwise
		 */
		isProgramEmpty: function (jsonObj) {
			var s = this.parseNode(jsonObj, -1);
			return ! /stopMyself|action|keepState/.test(s);
		}

    });
    return ProgramGrammar;
});
