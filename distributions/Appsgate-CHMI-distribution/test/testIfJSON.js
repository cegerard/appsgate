{
	programName : "TestIf",
	seqParameters : [
	],
	author : "Bob",
	target : "Alice",
	seqDefinitions : [
	],
	seqRules : [
		[
			{
				type : "NodeIf",
				expBool : [
					[
						{
							type : "NodeRelationBool",
							operator : "==",
							leftOperand : {
								deviceId : "194.199.23.136-1",
								methodName : "getCurrentState",
								returnType : "boolean",
								args : [
								]
							},
							rightOperand : {
								type : "boolean",
								value : "true"
							}
						}
					]
				],
				seqRulesTrue : [
					[
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-1",
							methodName : "Off",
							args : [
							]
						},
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-2",
							methodName : "Off",
							args : [
							]
						},
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-3",
							methodName : "Off",
							args : [
							]
						},
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-5",
							methodName : "Off",
							args : [
							]
						}
					]
				],
				seqRulesFalse : [
					[
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-1",
							methodName : "On",
							args : [
							]
						},
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-2",
							methodName : "On",
							args : [
							]
						},
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-3",
							methodName : "On",
							args : [
							]
						},
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-5",
							methodName : "On",
							args : [
							]
						}
					],
					[
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-1",
							methodName : "setPink",
							args : [
							]
						},
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-2",
							methodName : "setRed",
							args : [
							]
						},
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-3",
							methodName : "setBlue",
							args : [
							]
						},
						{
							type : "NodeAction",
							deviceId : "194.199.23.136-5",
							methodName : "setGreen",
							args : [
							]
						}
					]
				]
			}
		]
	]
}
