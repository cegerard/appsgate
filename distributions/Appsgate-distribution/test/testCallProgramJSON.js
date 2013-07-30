{
	programName : "TestCallProgram",
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
							targetType : "device",
							targetId : "194.199.23.136-2",
							methodName : "setBlue",
							args : [
							]
						}
					]
				]
			}
		],
		[
			{
				type : "NodeWhen",
				events : [
					{
						type : "NodeEvent",
						sourceType : "device",
						sourceId : "ENO2796f3",
						eventName : "switchNumber",
						eventValue : "0"
					}
				],
				seqRulesThen : [
					[
						{
							type : "NodeAction",
							targetType : "device",
							targetId : "194.199.23.136-2",
							methodName : "setRed",
							args : [
							]
						}
					]
				]
			}
		]
	]
}
