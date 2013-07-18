{
	programName : "TestWhen",
	seqParameters : [
	],
	author : "Bob",
	target : "Alice",
	seqDefinitions : [
	],
	seqRules : [
		[
			{
				type : "NodeWhen",
				events : [
					{
						type : "NodeEvent",
						sourceType : "device",
						sourceId : "ENO2796f3",
						eventName : "switchNumber",
						eventValue : "1"
					}
				],
				seqRulesThen : [
					[
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-2",
							methodName : "On",
							args : [
							]
						}
					]
				]
			}
		]
	]
}
