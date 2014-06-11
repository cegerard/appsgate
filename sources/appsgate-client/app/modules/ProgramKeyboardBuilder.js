/**
 * Created by thibaud on 19/05/2014.
 * This "class" focuses on building the Keyboard part of the Program Editor
 */
define([
    "text!templates/program/editor/expectedInput.html"
], function( expectedInputTemplate) {
    var ProgramKeyboardBuilder = {};
    // router
    ProgramKeyboardBuilder = Backbone.Model.extend({
        tplExpectedInput: _.template(expectedInputTemplate),
        initialize: function() {

        },


        /**
         * buildKeyboard should be the only 'public' function
         * it takes as input the currently selectedNode and it build the corresponding html keyBoard in HTML
         * @param ex
         */
        buildKeyboard: function(ex) {
            $(".expected-elements").html(this.tplExpectedInput());

            nodes = ex.expected;
            if (nodes != null) {            // First we treat the devices and services
                switch (ex.type) {
                    case "device":
                        this.buildDevicesOfType(nodes[0]);
                        break;
                    case "service":
                        this.buildServicesOfType(nodes[0]);
                        break;
                    default:
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
                                    this.buildStateKeys("state");
                                    break;
                                case '"maintanableState"':
                                    this.buildStateKeys("maintanableState");
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
                                case '"property"':
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
                                case '"programCall"':
                                case '"service"':
                                    // silently escaping
                                    break;
                                default:
                                    console.warn("Unsupported type: " + nodes[t]);
                                    break;
                            }
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

        /**
         * Add all the devices from a given type
         */
        buildDevicesOfType: function(type) {
            devices.forEach(function(device) {
                if (device.get("type") == type) {
                    $(".expected-devices").append(device.buildButtonFromDevice());
                }
            });
        },

        /**
         * Add all the services from a given type
         */
        buildServicesOfType: function(type) {
            services.forEach(function(service) {
                if (service.get("type") == type) {
                    $(".expected-services").append(service.buildButtonFromBrick());
                }
            });
        },

        buildComparatorKeys: function() {

            this.buildHackedBooleanComparatorKeys();
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
        buildStateKeys: function(which) {
            var types = devices.getDevicesByType();
            for (type in types) {
                if (types[type].length > 0) {
                    o = types[type][0];
                    states = o.getStates(which);
                    for (a in states) {
                        $(".expected-links").append(o.getKeyboardForState(states[a], which));
                    }
                }
            }
            var serviceTypes = services.getServicesByType();
            for (type in serviceTypes) {
                if (serviceTypes[type].length > 0) {
                    o = serviceTypes[type][0];
                    states = o.getStates(which);
                    for (a in states) {
                        $(".expected-links").append(o.getKeyboardForState(states[a], which));
                    }
                }
            }
        },
        buildDevices: function() {
            devices.forEach(function(device) {
                $(".expected-devices").append(device.buildButtonFromDevice());
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


        // for each boolean seviceType make a false opeator upon boolean
        buildHackedBooleanComparatorKeys: function() {
            var devicesTypes = devices.getDevicesByType();
            for (type in devicesTypes) {
                if (devicesTypes[type].length > 0) {
                    o = devicesTypes[type][0];
                    var boolProps = o.getProperties();
                    for (a in boolProps) {
                        var btn = o.getKeyboardForProperty(boolProps[a]);
                        console.log("coucou");
                        json = {};
                        json = JSON.parse($(btn).attr('json'));

                        var v = {
                            "type": "comparator",
                            "iid": "X",
                            "comparator": "==",
                            "rightOperand": {
                                "value" : "0",
                                "type": "number"
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
                    var boolProps = o.getProperties();
                    for (a in boolProps) {
                        console.log("coucou");

                        var btn = o.getKeyboardForProperty(boolProps[a]);
                        json = {};
                        json = JSON.parse($(btn).attr('json'));

                        var v = {
                            "type": "comparator",
                            "iid": "X",
                            "comparator": "==",
                            "rightOperand": {
                                "iid": "X",
                                "value" : "0",
                                "type": "number"
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
            $(".expected-events").append(btn_v);
            $(".expected-events").append(btn_f);

        },
        /**
         * method to sort a keyband alphabetically
         */
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
        }




    });

    return ProgramKeyboardBuilder;
});