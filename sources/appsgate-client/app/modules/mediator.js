define([
  "app",
  "modules/grammar",
  "modules/ProgramKeyboardBuilder",
  "modules/ProgramInputBuilder"
  ], function(App, Grammar, ProgramKeyboardBuilder, ProgramInputBuilder) {
    var ProgramMediator = {};
    // router
    ProgramMediator = Backbone.Model.extend({
        initialize: function() {
          this.ProgramKeyboardBuilder = new ProgramKeyboardBuilder();
          this.ProgramInputBuilder = new ProgramInputBuilder();

          this.resetProgramJSON();
        this.currentNode = 1;
        this.maxNodeId = 1;
        this.lastAddedNode = null;
        this.Grammar = new Grammar();
      },
      resetProgramJSON: function() {
        this.programJSON = {
          iid: 0,
          type: "setOfRules",
          rules: [{
            iid: 1,
            type: "empty"
          }]
        };
      },
      loadProgramJSON: function(programJSON) {
        this.programJSON = programJSON;
        this.maxNodeId = this.findMaxId(programJSON);
        this.currentNode = -1;
      },
      findMaxId: function(curNode) {
        for (var o in curNode) {
          if (typeof curNode[o] === 'object') {
            this.findMaxId(curNode[o]);
          }
          if (curNode[o].iid > this.maxNodeId) {
            this.maxNodeId = curNode[o].iid;
          }
        }
        return this.maxNodeId;
      },
      setCurrentPos: function(id) {
        if (id) {
            console.log("Setting current_pos to: " + id);
            this.currentNode = id;
        } else {
            this.currentNode = -1;
            console.error("A non valid pos has been passed to setCurrent pos: " + id);
        }
      },
      setCursorAndBuildKeyboard: function(id) {
        this.setCurrentPos(id);
        this.checkProgramAndBuildKeyboard(this.programJSON);
      },
      buttonPressed: function(button) {
        n = {};
        if ($(button).hasClass("specific-node")) {
          n = JSON.parse($(button).attr('json'));
        } else if ($(button).hasClass("device-node")) {
          n = this.getDeviceJSON(button.id);
        } else if ($(button).hasClass("service-node")) {
          n = this.getServiceJSON(button.id);
        } else if ($(button).hasClass("program-node")) {
          n = {
            "value": button.id,
            "name": $(button).attr('prg_name'),
            "iid": "X",
            "type": "programCall"
          };
        } else if ($(button).hasClass("number-node")) {
          n = {
            "type": "number",
            "iid": "X",
            "value": "0"
          };
        } else if ($(button).hasClass("if-node")) {
          n = this.getIfJSON();
        } else if ($(button).hasClass("when-node")) {
          n = {
            "type": "when",
            "iid": "X",
            "events": this.getEmptyJSON("mandatory"),
            "seqRulesThen": {
              "iid": "X",
              "type": "seqRules",
              "rules": [this.getEmptyJSON("mandatory")]
            }
          };
        } else if ($(button).hasClass("while-node")) {
          n = {
            "type": "while",
            "iid": "X",
            "state": this.getEmptyJSON("mandatory"),
            "rules": {
              "type": "keepState",
              "iid": "X",
              "state": this.getEmptyJSON("mandatory")

            },
            "rulesThen": {
              "iid": "X",
              "type": "seqRules",
              "rules": [this.getEmptyJSON("empty")]
            }
          };
        } else if ($(button).hasClass("clock-node")) {
          n = this.getEventJSON("ClockAlarm", "il est 7h00", "7h00");
        } else if ($(button).hasClass("TODO-node")) {
          console.warn("Node has to be implemented");
        }

        this.lastAddedNode = this.setIidOfJson(n);
        this.appendNode(this.lastAddedNode, this.currentNode);

        // reset the selection because a node was added
        this.setCurrentPos(-1);
        dispatcher.trigger("refreshDisplay");
      },
      appendNode: function(node, pos) {
        this.programJSON = this.recursivelyAppend(node, pos, this.programJSON);
      },
      recursivelyAppend: function(nodeToAppend, pos, curNode) {
        if (parseInt(curNode.iid) === parseInt(pos)) {
          curNode = nodeToAppend;
        } else {
          for (var o in curNode) {
            if (typeof curNode[o] === "object") {
              var prevIid = curNode[o].iid;
              // If adding an element to a rules array, we add an empty element to allow further insertions
              curNode[o] = this.recursivelyAppend(nodeToAppend, pos, curNode[o]);
              if (parseInt(prevIid) === parseInt(pos) && $.isArray(curNode)) {
                if(curNode.length-1 == o){
                  curNode.push(this.setIidOfJson(this.getEmptyJSON("empty")));
                }
              }
            }
          }
        }
        return curNode;
      },
      removeSelectedNode: function() {
        this.programJSON = this.recursivelyRemove(this.currentNode, this.programJSON);
        this.buildInputFromJSON();
      },
      recursivelyRemove: function(pos, curNode, parentNode) {
        if (parseInt(curNode.iid) === parseInt(pos)) {
          curNode = {
            iid: curNode.iid,
            type: "empty"
          };
        } else {
          for (var o in curNode) {
            if (typeof curNode[o] === "object") {
              if (Array.isArray(curNode[o])) {
                curNode[o] = this.recRemoveArray(pos, curNode[o], curNode);
              } else {
                curNode[o] = this.recursivelyRemove(pos, curNode[o], curNode);
              }
            }
          }
        }
        return curNode;
      },
      recRemoveArray: function(pos, curNode, parentNode) {
        var prevIsEmpty = false;
        var retNode = [];
        for (var o in curNode) {
          if (typeof curNode[o] === "object" && Array.isArray(curNode[o])) {
            console.error("An array has been found inside an array");
            return curNode;
          }
          var recNode = this.recursivelyRemove(pos, curNode[o], curNode);
          if (recNode.type !== "empty") {
            retNode.push(recNode);
            prevIsEmpty = false;
          } else {
            if (!prevIsEmpty) {
              retNode.push(recNode);
            } else {
              // Check whether the deleted node is not the node with the current pos
              if (recNode.iid == pos) {
                retNode.pop();
                retNode.push(recNode);
              }
            }
            prevIsEmpty = true;
          }
        }
        return retNode;
      },
      /*
      * set a node attribute
      */
      setNodeArg: function(iid, index, value) {
        this.recursivelySetNodeArg(iid, index, value, this.programJSON);
      },
      recursivelySetNodeArg: function(iid, index, value, curNode) {
        if (parseInt(curNode.iid) === parseInt(iid)) {
          curNode.args[index] = value;
        } else {
          for (var o in curNode) {
            if (typeof curNode[o] === "object") {
              this.recursivelySetNodeArg(iid, index, value, curNode[o]);
            }
          }
        }
      },
      /*
      * set a node attribute
      */
      setNodeAttribute: function(iid, attribute, value) {
        this.recursivelySetNodeAttribute(iid, attribute, value, this.programJSON);
      },
      recursivelySetNodeAttribute: function(iid, attribute, value, curNode) {
        if (parseInt(curNode.iid) === parseInt(iid)) {
          curNode[attribute] = value;
        } else {
          for (var o in curNode) {
            if (typeof curNode[o] === "object") {
              this.recursivelySetNodeAttribute(iid, attribute, value, curNode[o]);
            }
          }
        }
      },
      setIidOfJson: function(obj) {
        if (obj.iid === "X") {
          obj.iid = this.maxNodeId++;
        }
        for (var k in obj) {
          if (typeof obj[k] === "object") {
            this.setIidOfJson(obj[k]);
          }
        }
        return obj;
      },
      getDeviceJSON: function(deviceId) {
        var d = devices.get(deviceId);
        var deviceName = d.get("name");
        return {"type": "device", "value": deviceId, "name": deviceName, "iid": "X", "deviceType": d.get("type")};

      },
      getServiceJSON: function(serviceId) {
        var s = services.get(serviceId);
        var serviceName = s.get("name");
        return {"type": "service", "value": serviceId, "name": serviceName, "iid": "X", "serviceType": s.get("type")};
      },
      getIfJSON: function() {
        return {
          "type": "if",
          "iid": "X",
          "expBool": this.getEmptyJSON("mandatory"),
          "seqRulesTrue": {
            "type": "seqRules",
            "iid": "X",
            "rules": [this.getEmptyJSON("mandatory")]
          },
          "seqRulesFalse": {
            "type": "seqRules",
            "iid": "X",
            "rules": [this.getEmptyJSON("empty")]
          }
        };
      },
      getEmptyJSON: function(type) {
        return {
          "type": type,
          "iid": "X"
        };
      },

      getInputFromJSON: function() {
        if (this.checkProgramAndBuildKeyboard()) {
          this.isValid = true;
        } else {
          this.isValid = false;
        }
        var input = $.parseHTML(this.ProgramInputBuilder.buildInputFromNode(this.programJSON, this.currentNode));

        $(input).find(".btn").css("padding", "3px 6px");
        
        $(input).i18n();

        var keyBands = $(".expected-elements").children();
        var self = this.ProgramKeyboardBuilder;
        keyBands.each(function(index) {
          self.sortKeyband(this);
        });

        return input;
      },
      findNextInput: function(selected) {
      	//
      	while(typeof selected != "undefined") {
      		console.log("selected(inside) : "+selected.nextAll(".input-spot").attr("id"));
      		if(selected.nextAll(".input-spot").length != 0) {
      			return selected;
      		}
      		selected = selected.parent();
      	}
		return selected;

      },
      buildInputFromJSON: function() {
        $(".programInput").html(this.getInputFromJSON());
      },

      checkProgramAndBuildKeyboard: function(programJSON) {
        if (typeof programJSON !== "undefined")
        this.programJSON = programJSON;
        var n = this.Grammar.parse(this.programJSON, this.currentNode);
        if (n == null) {
          console.log("Program is correct");
          return true;
        } else if (n.expected[0] === "ID") {
          this.resetProgramJSON();
          this.checkProgramAndBuildKeyboard();
        } else {
          console.log("Invalid at " + n.id);
          if (typeof n.id !== "undefined") {
            this.setCurrentPos(n.id);
          }
          this.ProgramKeyboardBuilder.buildKeyboard(n);
        }
        var keyBands = $(".expected-elements").children();
        var self = this.ProgramKeyboardBuilder;
        keyBands.each(function(index) {
          self.sortKeyband(this);
        });

        return false;
      }
    });
    return ProgramMediator;
  });
