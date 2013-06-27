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
				type : "NodeAction",
				targetType : "program",
				targetId : "SimpleRule",
				methodName : "start",
				args : [
				]
			}
		],
		[
			{
				type : "NodeAction",
				targetType : "device",
				targetId : "194.199.23.136-2",
				methodName : "Off",
				args : [
				]
			},
			{
				type : "NodeAction",
				targetType : "device",
				targetId : "194.199.23.136-5",
				methodName : "Off",
				args : [
				]
			}
		]
	]
}
