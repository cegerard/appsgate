define([
  "app",
  "modules/grammar",
  "text!templates/program/nodes/defaultActionNode.html",
  "text!templates/program/nodes/lampActionNode.html",
  "text!templates/program/nodes/mediaPlayerActionNode.html",
  "text!templates/program/nodes/ifNode.html",
  "text!templates/program/nodes/whenNode.html",
  "text!templates/program/nodes/deviceNode.html",
  "text!templates/program/nodes/serviceNode.html",
  "text!templates/program/nodes/defaultEventNode.html",
  "text!templates/program/nodes/clockEventNode.html",
  "text!templates/program/nodes/stateNode.html",
  "text!templates/program/nodes/keepStateNode.html",
  "text!templates/program/nodes/whileNode.html",
  "text!templates/program/nodes/whitespaceNode.html",
  "text!templates/program/nodes/booleanExpressionNode.html",
  "text!templates/program/nodes/comparatorNode.html",
  "text!templates/program/nodes/numberNode.html",
  "text!templates/program/nodes/waitNode.html",
  "text!templates/program/nodes/programNode.html",
  "text!templates/program/editor/expectedInput.html",
  "text!templates/program/nodes/defaultPropertyNode.html"
  ], function(App, Grammar, defaultActionTemplate, lampActionTemplate, mediaActionTemplate, ifNodeTemplate, whenNodeTemplate, deviceNodeTemplate, serviceNodeTemplate, defaultEventNodeTemplate, clockEventNodeTemplate, stateNodeTemplate, keepStateNodeTemplate, whileNodeTemplate, whitespaceNodeTemplate, booleanExpressionNodeTemplate, comparatorNodeTemplate, numberNodeTemplate, waitNodeTemplate, programNodeTemplate, expectedInputTemplate, defaultPropertyNodeTemplate) {
    var ProgramMediator = {};
    // router
    ProgramMediator = Backbone.Model.extend({
      tplDefaultActionNode: _.template(defaultActionTemplate),
      tplLampActionNode: _.template(lampActionTemplate),
      tplMediaActionNode: _.template(mediaActionTemplate),
      tplIfNode: _.template(ifNodeTemplate),
      tplWhenNode: _.template(whenNodeTemplate),
      tplDeviceNode: _.template(deviceNodeTemplate),
      tplServiceNode: _.template(serviceNodeTemplate),
      tplEventNode: _.template(defaultEventNodeTemplate),
      tplClockEventNode: _.template(clockEventNodeTemplate),
      tplStateNode: _.template(stateNodeTemplate),
      tplKeepStateNode: _.template(keepStateNodeTemplate),
      tplWhileNode: _.template(whileNodeTemplate),
      tplWhiteSpaceNode: _.template(whitespaceNodeTemplate),
      tplBooleanExpressionNode: _.template(booleanExpressionNodeTemplate),
      tplComparatorNode: _.template(comparatorNodeTemplate),
      tplNumberNode: _.template(numberNodeTemplate),
      tplWaitNode: _.template(waitNodeTemplate),
      tplProgramNode: _.template(programNodeTemplate),
      tplExpectedInput: _.template(expectedInputTemplate),
      tplDefaultPropertyNode: _.template(defaultPropertyNodeTemplate),
      initialize: function() {
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
      buildActionKeys: function() {
        var deviceTypes = devices.getDevicesByType();
        for (type in deviceTypes) {
          if (deviceTypes[type].length > 0) {
            o = deviceTypes[type][0];
            actions = o.getActions();
            for (a in actions) {
              $(".expected-actions").append(o.getKeyboardForAction(actions[a]));
            }
          }
        }
        var serviceTypes = services.getServicesByType();
        for (type in serviceTypes) {
          if (serviceTypes[type].length > 0) {
            o = serviceTypes[type][0];
            actions = o.getActions();
            for (a in actions) {
              $(".expected-actions").append(o.getKeyboardForAction(actions[a]));
            }
          }
        }
        this.buildPrograms();

      },
      buildEventKeys: function() {
        var types = devices.getDevicesByType();
        for (type in types) {
          if (types[type].length > 0) {
            o = types[type][0];
            events = o.getEvents();
            for (a in events) {
              $(".expected-events").append(o.getKeyboardForEvent(events[a]));
            }
          }
        }
      },
      buildStateKeys: function() {
        var types = devices.getDevicesByType();
        for (type in types) {
          if (types[type].length > 0) {
            o = types[type][0];
            states = o.getStates();

            var nodeParent="";

            // This recovers the html exerpt that will indicate that the parent node is a "Keep"
            if(typeof $("#"+this.currentNode).parent().children().children()[0] != "undefined"){
            	nodeParent=$("#"+this.currentNode).parent().children().children()[0].getAttribute("name");
            }

            // Only if the parent is not a Keep and the device are not type 3,4,5 (ContactSensor,KeycardSensor,ArdLocker), meaning that we cant keep state on those devices
            if(nodeParent != "keep" || (type != "3" && type != "4" && type != "5")){
            	for (a in states) {
                   $(".expected-links").append(o.getKeyboardForState(states[a]));
               }
            }
          }
        }
        var serviceTypes = services.getServicesByType();
        for (type in serviceTypes) {
          if (serviceTypes[type].length > 0) {
            o = serviceTypes[type][0];
            states = o.getStates();
            for (a in states) {
              $(".expected-links").append(o.getKeyboardForState(states[a]));
            }
          }
        }
      },
      buildGetPropertyKeys: function() {
        var types = devices.getDevicesByType();
        for (type in types) {
          if (types[type].length > 0) {
            o = types[type][0];
            states = o.getProperties();
            for (a in states) {
              $(".expected-links").append(o.getKeyboardForProperty(states[a]));
            }
          }
        }
        var serviceTypes = services.getServicesByType();
        for (type in serviceTypes) {
          if (serviceTypes[type].length > 0) {
            o = serviceTypes[type][0];
            states = o.getProperties();
            for (a in states) {
              $(".expected-links").append(o.getKeyboardForProperty(states[a]));
            }
          }
        }
      },
      buildBooleanKeys: function() {
        var v = {
          "type": "boolean",
          "value": "true",
          "iid": "X"
        };
        var f = {
          "type": "boolean",
          "value": "false",
          "iid": "X"
        };
        var btn_v = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ><span data-i18n='keyboard.true'/></button>");
        var btn_f = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ><span data-i18n='keyboard.false'/></button>");
        $(btn_v).attr("json", JSON.stringify(v));
        $(btn_f).attr("json", JSON.stringify(f));
        $(".expected-links").append(btn_v);
        $(".expected-links").append(btn_f);

      },
      buildDevicesOfType: function(type) {
        devices.forEach(function(device) {
          if (device.get("type") == type) {
            $(".expected-devices").append(device.buildButtonFromDevice());
          }
        });
      },
      buildDevices: function() {
        devices.forEach(function(device) {
          $(".expected-devices").append(device.buildButtonFromDevice());
        });
      },
      buildServicesOfType: function(type) {
        services.forEach(function(service) {
          if (service.get("type") == type) {
            $(".expected-services").append(service.buildButtonFromBrick());
          }
        });
      },
      buildServices: function() {
        services.forEach(function(service) {
          $(".expected-services").append(service.buildButtonFromBrick());
        });
      },
      buildPrograms: function() {
        var btnCall = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");

        $(btnCall).append("<span data-i18n='language.activate-program-action'/>");
        var v = {
          "type": "action",
          "methodName": "callProgram",
          "target": {
            "iid": "X",
            "type": "programs"
          },
          "args": [],
          "iid": "X",
          "phrase": "language.activate"
        };
        $(btnCall).attr("json", JSON.stringify(v));

        var btnStop = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ></button>");
        $(btnStop).append("<span data-i18n='language.disactivate-program-action'/>");
        var w = {
          "type": "action",
          "methodName": "stopProgram",
          "target": {
            "iid": "X",
            "type": "programs"
          },
          "args": [],
          "iid": "X",
          "phrase": "language.disactivate"
        };
        $(btnStop).attr("json", JSON.stringify(w));

        $(".expected-actions").append(btnCall);
        $(".expected-actions").append(btnStop);
      },
      buildProgramsKeys: function() {
        programs.forEach(function(prg) {
          $(".expected-programs").append("<button id='" + prg.get("id") + "' class='btn btn-default btn-keyboard program-node' prg_name='" + prg.get("name") + "'><span>" + prg.get("name") + "<span></button>");
        });
      },
      buildBooleanExpressionKeys: function() {

        var btnAnd = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ><span data-i18n='language.if-and'/></button>");
        var v = {
          "type": "booleanExpression",
          "iid": "X",
          "operator": "&&",
          "leftOperand": {
            "iid": "X",
            "type": "mandatory"
          },
          "rightOperand": {
            "iid": "X",
            "type": "mandatory"
          }
        };
        $(btnAnd).attr("json", JSON.stringify(v));
        $(".expected-links").append(btnAnd);

      },
      buildComparatorKeys: function() {

        var btnEq = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ><span data-i18n='language.if-equals'/></button>");
        var v = {
          "type": "comparator",
          "iid": "X",
          "comparator": "==",
          "leftOperand": {
            "iid": "X",
            "type": "mandatory"
          },
          "rightOperand": {
            "iid": "X",
            "type": "mandatory"
          }
        };
        $(btnEq).attr("json", JSON.stringify(v));
        $(".expected-links").append(btnEq);
        var btnSup = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ><span data-i18n='language.if-sup'/></button>");
        var v = {
          "type": "comparator",
          "iid": "X",
          "comparator": ">",
          "leftOperand": {
            "iid": "X",
            "type": "mandatory"
          },
          "rightOperand": {
            "iid": "X",
            "type": "mandatory"
          }
        };
        $(btnSup).attr("json", JSON.stringify(v));
        $(".expected-links").append(btnSup);
        var btnInf = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ><span data-i18n='language.if-inf'/></button>");
        var v = {
          "type": "comparator",
          "iid": "X",
          "comparator": "<",
          "leftOperand": {
            "iid": "X",
            "type": "mandatory"
          },
          "rightOperand": {
            "iid": "X",
            "type": "mandatory"
          }
        };
        $(btnInf).attr("json", JSON.stringify(v));
        $(".expected-links").append(btnInf);
        var btnDiff = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ><span data-i18n='language.if-dif'/></button>");
        var v = {
          "type": "comparator",
          "iid": "X",
          "comparator": "!=",
          "leftOperand": {
            "iid": "X",
            "type": "mandatory"
          },
          "rightOperand": {
            "iid": "X",
            "type": "mandatory"
          }
        };
        $(btnDiff).attr("json", JSON.stringify(v));
        $(".expected-links").append(btnDiff);

        this.buildHackedBooleanComparatorKeys();
      },
      // for each boolean seviceType make a false opeator upon boolean
      buildHackedBooleanComparatorKeys: function() {
        var devicesTypes = devices.getDevicesByType();
        for (type in devicesTypes) {
          if (devicesTypes[type].length > 0) {
            o = devicesTypes[type][0];
            var boolProps = o.getBooleanProperties();
            for (a in boolProps) {
              var btn = o.getKeyboardForProperty(boolProps[a]);
              json = {};
              json = JSON.parse($(btn).attr('json'));

              var v = {
                "type": "comparator",
                "iid": "X",
                "comparator": "==",
                "rightOperand": {
                  "iid": "X",
                  "value": "true",
                  "type": "boolean"
                }
              };
              v.leftOperand = json;
              $(btn).attr("json", JSON.stringify(v));
              $(".expected-links").append(btn);
            }
          }
        }
        var serviceTypes = services.getServicesByType();
        for (type in serviceTypes) {
          if (serviceTypes[type].length > 0) {
            o = serviceTypes[type][0];
            var boolProps = o.getBooleanProperties();
            for (a in boolProps) {
              var btn = o.getKeyboardForProperty(boolProps[a]);
              json = {};
              json = JSON.parse($(btn).attr('json'));

              var v = {
                "type": "comparator",
                "iid": "X",
                "comparator": "==",
                "rightOperand": {
                  "iid": "X",
                  "value": "true",
                  "type": "boolean"
                }
              };
              v.leftOperand = json;
              $(btn).attr("json", JSON.stringify(v));
              $(".expected-links").append(btn);
            }
          }
        }
      },
      buildWaitKey: function() {
        var btn = jQuery.parseHTML("<button class='btn btn-default btn-keyboard specific-node' ><span data-i18n='language.wait'/></button>");
        var v = {
          "type": "wait",
          "iid": "X",
          "waitFor": {
            "iid": "X",
            "type": "number",
            "value": "10"
          }
        };
        $(btn).attr("json", JSON.stringify(v));
        $(".expected-actions").append(btn);
      },
      buildKeyboard: function(ex) {
        $(".expected-elements").html(this.tplExpectedInput());

        nodes = ex.expected;
        // First we treat the devices and services
        switch (ex.type) {
        case "device":
          this.buildDevicesOfType(nodes[0]);
          return;
          break;
        case "service":
          this.buildServicesOfType(nodes[0]);
          return;
          break;
        }
        if (nodes != null) {
          for (t in nodes) {
            switch (nodes[t]) {
            case '"if"':
              $(".expected-links").append("<button class='btn btn-default btn-keyboard if-node'><span data-i18n='keyboard.if-keyword'><span></button>");
              break;
            case '"comparator"':
              this.buildComparatorKeys();
              break;
            case '"booleanExpression"':
              //this.buildBooleanExpressionKeys();
              break;
            case '"when"':
              $(".expected-links").append("<button class='btn btn-default btn-keyboard when-node'><span data-i18n='keyboard.when-keyword'><span></button>");
              break;
            case '"while"':
              $(".expected-links").append("<button class='btn btn-default btn-keyboard while-node'><span data-i18n='keyboard.while-keyword'><span></button>");
              break;
            case '"state"':
              this.buildStateKeys();
              break;
            case '"seqRules"':
              break;
            case '"setOfRules"':
              break;
            case '"keepState"':
              $(".expected-links").append("<button class='btn btn-default btn-keyboard keepState-node'><span data-i18n='keyboard.keep-state'><span></button>");
              break;
            case '"device"':
              this.buildDevices();
              break;
            case 'programs':
              this.buildProgramsKeys();
              break;
            case '"variable"':
              console.log("variables not supported in the language right now");
              break;
            case '"action"':
              this.buildActionKeys();
              break;
            case '"event"':
              this.buildEventKeys();
              break;
            case '"deviceState"':
              this.buildGetPropertyKeys();
              break;
            case '"boolean"':
              this.buildBooleanKeys();
              break;
            case "ID":
              console.log("empty program");
              break;
            case '"number"':
              $(".expected-events").append("<button class='btn btn-default btn-keyboard number-node'><span>valeur<span></button>");
              break;
            case '"wait"':
              this.buildWaitKey();
              break;
            case '"empty"':
            case '"programs"':
            case 'separator':
              // silently escaping
              break;
            default:
              console.warn("Unsupported type: " + nodes[t]);
              break;
            }
          }
        } else {
          console.warn("For now, it is not supported to have multiple instruction in one program.");
        }

        $(".expected-elements").i18n();

        var keyBands = $(".expected-elements").children();
        var self = this;
        keyBands.each(function(index) {
          self.sortKeyband(this);
        });
      },
      getDeviceName: function(id) {
        if (devices.get(id) == undefined) {
          console.error("device not found: " + id);
          return "UNKNOWN DEVICE";
        }
        return devices.get(id).get("name");
      },
      getServiceName: function(id) {
        if (services.get(id) == undefined) {
          console.error("service not found: " + id);
          return "UNKNOWN SERVICE";
        }
        return services.get(id).get("name");
      },
      buildActionNode: function(param) {
        if (param.node.target.deviceType) {
          return devices.getTemplateActionByType(param.node.target.deviceType, param);
        }
        if (param.node.target.serviceType) {
          return services.getTemplateActionByType(param.node.target.serviceType, param);
        }
        if (param.node.target.type === "programCall" || param.node.target.type === "programs") {
            return this.tplProgramNode(param);
        }
        return this.tplDefaultActionNode(param);
      },
      buildPropertyNode: function(param) {
        var result = "";
        if (param.node.target.deviceType) {
          // TODO : FIXME
          // I use the same template function for event and for action... to check it is possible
          return devices.getTemplateActionByType(param.node.target.deviceType, param);
        }
        if (param.node.target.serviceType) {
          // TODO : FIXME
          // I use the same template function for event and for action... to check it is possible
          return services.getTemplateActionByType(param.node.target.serviceType, param);
        }
        return this.tplDefaultPropertyNode(param);
      },
      buildEventNode: function(param) {
        var result = "";
        if (param.node.eventName === "newFace") {
          // TODO : FIXME
          // I use the same template function for event and for action... to check it is possible
          return devices.getTemplateActionByType(param.node.source.deviceType, param);
        }
        if (param.node.eventName === "ClockAlarm") {
          var hours = [];
          for (var i = 0; i < 24; i++) {
            hours.push(i);
          }

          var minutes = [];
          for (i = 0; i < 60; i++) {
            minutes.push(i);
          }

          var time = moment(parseInt(param.node.eventValue));

          var selectedHour = time.hour();
          var selectedMinute = time.minute();
          result = this.tplClockEventNode({
            "node": param.node,
            "hours": hours,
            "minutes": minutes,
            "selectedHour": selectedHour,
            "selectedMinute": selectedMinute
          });
        } else {
          result = this.tplEventNode(param);
        }
        return result;
      },
      // Hack for a simple prestenation when X == true, we only show X
      buildComparatorNode: function(param) {
        try {
          if(param.node.comparator === "==" && param.node.rightOperand.type === "boolean" && param.node.rightOperand.value === "true") {
            return this.buildInputFromNode(param.node.leftOperand);
          } else {
            return this.tplComparatorNode(param);
          }
        } catch (e) {
          return this.tplComparatorNode(param);
        }

      },
      buildInputFromNode: function(jsonNode) {
        var self = this;

        param = {
          node: jsonNode,
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
          input += this.buildComparatorNode(param);
          break;
        case "when":
          deletable = true;
          input += this.tplWhenNode(param);
          break;
        case "device":
          param.node.name = this.getDeviceName(param.node.value);
          input += this.tplDeviceNode(param);
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
          deletable = true;
          input += this.tplStateNode(param);
          break;
        case "deviceState":
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
          jsonNode.rules.forEach(function(rule) {
            if (rule !== jsonNode.rules[0]) {
              input += "<div class='row'><div class='btn btn-default btn-prog btn-then btn-primary'><span data-i18n='language.op-then-rule'/></div></div>";
            }
            input += self.buildInputFromNode(rule);
          });
          break;
        case "setOfRules":
          jsonNode.rules.forEach(function(rule) {
            if (rule !== jsonNode.rules[0]) {
              input += "<div class='row'><div class='btn btn-default btn-prog btn-and btn-primary'><span data-i18n='language.op-and-rule'/></div></div>";
            }
            input += self.buildInputFromNode(rule);
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
          if (this.currentNode == jsonNode.iid) {
            supprClasses = "glyphicon glyphicon-trash";
          }
          input = "<div class='btn-current'>"
          + input
          + "<div class='btn-prog btn-trash " + supprClasses + "' id='" + jsonNode.iid + "' style='right:5px;position:absolute;top:0px;'></div></div>";

        }

        return input;
      },
      getInputFromJSON: function() {
        if (this.checkProgramAndBuildKeyboard()) {
          this.isValid = true;
        } else {
          this.isValid = false;
        }
        var input = $.parseHTML(this.buildInputFromNode(this.programJSON));

        $(input).find(".btn").css("padding", "3px 6px");
        
        $(input).i18n();

        var keyBands = $(".expected-elements").children();
        var self = this;
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

      },
      buildInputFromJSON: function() {
        $(".programInput").html(this.getInputFromJSON());
      },
      sortKeyband: function(keyband) {
        keyband = $(keyband);
        if (keyband.children().length < 1) {
          keyband.hide();
        } else {
          var buttons = keyband.children();

          buttons.sort(function(a, b) {
            return $(a).text().toUpperCase().localeCompare($(b).text().toUpperCase());

          });

          $.each(buttons, function(idx, itm) {
            keyband.append(itm);
          });
        }
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
          this.buildKeyboard(n);
        }
        var keyBands = $(".expected-elements").children();
        var self = this;
        keyBands.each(function(index) {
          self.sortKeyband(this);
        });

        return false;
      },
    });
    return ProgramMediator;
  });
