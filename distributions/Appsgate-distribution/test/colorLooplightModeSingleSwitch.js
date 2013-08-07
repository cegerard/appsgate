{
	programName : "ColorLoopMode",
	seqParameters : [
	],
	author : "Bob",
	target : "Alice",
	deamon : "true",
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
						sourceId : "ENO2842be",
						eventName : "buttonStatus",
						eventValue : "false"
					}
				],
				seqRulesThen : [
					[
						{
							type : "NodeAction",
							targetType : "device",
							targetId : "194.199.23.136-1",
								methodName : "setEffect",
							args : [
								{value:"colorloop", type:"String"}
							]
						},
					     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-2",
					             methodName : "setEffect",
							args : [
								{value:"colorloop", type:"String"}
							]
					     },
				    	 {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-3",
					         	methodName : "setEffect",
							args : [
								{value:"colorloop", type:"String"}
							]
					     },
				    	 {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-4",
					        	methodName : "setEffect",
							args : [
								{value:"colorloop", type:"String"}
							]
					     },
				    	 {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-5",
					             methodName : "setEffect",
							args : [
								{value:"colorloop", type:"String"}
							]
					     },
				    	{
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-6",
					             methodName : "setEffect",
							args : [
								{value:"colorloop", type:"String"}
							]
						}
					]
				]
			}
		]
	]
}
