define([
	"underscore",
	"text!resources/grammar.peg",
	"peg"
], function(_, grammarSource) {
	/**
	 * @constructor
	 */
	function Grammar() {
		var self = this;
		
		this.grammar = grammarSource;

		// cannot be called as a function...
		if (!(this instanceof Grammar)) {
			throw new TypeError("Grammar constructor cannot be called as a function");
		}

		// strings for the list of events and actions
		var listOfEvents = "eventProgram";
		var listOfStatus = "statusProgram";
		
		var listOfActions = "actionProgram";

		// group the devices by types - if any for a type, insert its corresponding grammar and the list of devices
		var devicesByType = devices.getDevicesByType();
		_.keys(devicesByType).forEach(function(deviceType) {
			// append the rules to the grammar
			deviceTypesGrammar[deviceType].rules.forEach(function(r) {
				self.grammar += r + "\n";
			});
			
			// insert the list of the devices
			self.insertListOfDevices(deviceTypesGrammar[deviceType].listAnchor, devicesByType[deviceType], deviceType);

			// append the events to the list of events
			if (typeof deviceTypesGrammar[deviceType].eventAnchor !== "undefined") {
				listOfEvents += " / " + deviceTypesGrammar[deviceType].eventAnchor;
			}
			
			// append the status to the list of events because it is used at the same location in the grammar
			if (typeof deviceTypesGrammar[deviceType].statusAnchor !== "undefined") {
				listOfStatus += " / " + deviceTypesGrammar[deviceType].statusAnchor;
			}

			// append the actions to the list of actions
			if (typeof deviceTypesGrammar[deviceType].actionAnchor !== "undefined") {
				listOfActions += " / " + deviceTypesGrammar[deviceType].actionAnchor;
			}
			
			// translate the grammar of the device type
			if (typeof deviceTypesGrammar[deviceType].i18nData !== "undefined") {
				deviceTypesGrammar[deviceType].i18nData.forEach(function(l) {
					var regexp = new RegExp(l.grammarAnchor, "g");
					self.grammar = self.grammar.replace(regexp, $.i18n.t(l.i18nVar));
				});
			}
		});

		// insert the list of events
		this.grammar = this.grammar.replace(/{{listOfEvents}}/g, listOfEvents);
		
		// insert the list of status
		this.grammar = this.grammar.replace(/{{listOfStatus}}/g, listOfStatus);

		// insert the list of actions
		this.grammar = this.grammar.replace(/{{listOfActions}}/g, listOfActions);

		// insert the list of programs
		this.insertListOfDevices("{{listOfPrograms}}", programs.models, "program");
		
		// translate the grammar
		this.translateRootGrammar();

		// build the parser from the grammar
		try {
			this.parser = PEG.buildParser(this.grammar);
		} catch (e) {
			console.log(e);
		}
	}

	Grammar.prototype = {
		constructor: constructor,

		insertListOfDevices:function(grammarAnchor, listOfDevices, deviceType) {
			var deviceNames = "";
			listOfDevices.forEach(function(d) {
				deviceNames += '"<span class=' + "'device-name' data-device-type='" + deviceType + "'>" + d.get("name") + '</span>"/'; 
			});
			if (deviceNames !== "") {
				deviceNames = deviceNames.substring(0, deviceNames.length - 1);
			}

			var regexp = new RegExp(grammarAnchor, "g");
			this.grammar = this.grammar.replace(regexp, deviceNames);
		},
		
		translateRootGrammar:function() {
			// header
			this.grammar = this.grammar.replace(/{{writtenBy}}/g, $.i18n.t("language.written-by"));
			
			// when
			this.grammar = this.grammar.replace(/{{whenKeyWord}}/g, $.i18n.t("language.when-keyword"));
			this.grammar = this.grammar.replace(/{{endWhenBlock}}/g, $.i18n.t("language.end-when-block"));
			
			// if
			this.grammar = this.grammar.replace(/{{ifKeyWord}}/g, $.i18n.t("language.if-keyword"));
			this.grammar = this.grammar.replace(/{{elseKeyWord}}/g, $.i18n.t("language.else-keyword"));
			this.grammar = this.grammar.replace(/{{endIfBlock}}/g, $.i18n.t("language.end-if-block"));
			
			// programs
			this.grammar = this.grammar.replace(/{{activateProgramAction}}/g, $.i18n.t("language.activate-program-action"));
			this.grammar = this.grammar.replace(/{{disactivateProgramAction}}/g, $.i18n.t("language.disactivate-program-action"));
			this.grammar = this.grammar.replace(/{{isActivatedProgramEvent}}/g, $.i18n.t("language.is-activated-program-event"));
			this.grammar = this.grammar.replace(/{{isDisactivatedProgramEvent}}/g, $.i18n.t("language.is-disactivated-program-event"));
			this.grammar = this.grammar.replace(/{{isRunningProgramStatus}}/g, $.i18n.t("language.is-running-program-status"));
			this.grammar = this.grammar.replace(/{{isStoppedProgramStatus}}/g, $.i18n.t("language.is-stopped-program-status"));
			
			// general keywords
			this.grammar = this.grammar.replace(/{{thenKeyWord}}/g, $.i18n.t("language.then-keyword"));
			this.grammar = this.grammar.replace(/{{opEvent}}/g, $.i18n.t("language.op-event"));
			this.grammar = this.grammar.replace(/{{opEventBool}}/g, $.i18n.t("language.op-event-bool"));
			this.grammar = this.grammar.replace(/{{opAndRule}}/g, $.i18n.t("language.op-and-rule"));
			this.grammar = this.grammar.replace(/{{opThenRule}}/g, $.i18n.t("language.op-then-rule"));
			this.grammar = this.grammar.replace(/{{opOrBool}}/g, $.i18n.t("language.op-or-bool"));
			this.grammar = this.grammar.replace(/{{opAndBool}}/g, $.i18n.t("language.op-and-bool"));
			this.grammar = this.grammar.replace(/{{opMoreThan}}/g, $.i18n.t("language.op-more-than"));
			this.grammar = this.grammar.replace(/{{opLessThan}}/g, $.i18n.t("language.op-less-than"));
			
			// button
			this.grammar = this.grammar.replace(/{{validButton}}/g, $.i18n.t("form.valid-button"));
			
			// other
			this.grammar = this.grammar.replace(/{{space}}/g, $.i18n.t("language.space"));
			this.grammar = this.grammar.replace(/{{true}}/g, $.i18n.t("language.true"));
			this.grammar = this.grammar.replace(/{{false}}/g, $.i18n.t("language.false"));
		},

		parse:function(input) {
			try {
				return this.parser.parse(input);
			} catch(e) {
				throw e;
			}
		}
	};

	return Grammar;
});