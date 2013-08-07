{
	programName : "decreasebri456",
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
						sourceId : "ENO2796f3",
						eventName : "switchNumber",
						eventValue : "1"
					},
					{
						type : "NodeEvent",
						sourceType : "device",
						sourceId : "ENO2796f3",
						eventName : "buttonStatus",
						eventValue : "true"
					}
				],
				seqRulesThen : [
					[
						{
							type : "NodeAction",
							targetType : "device",
							targetId : "194.199.23.136-4",
								methodName : "decreaseBrightness",
							args : [
								{value:"10", type:"int"}
							]
						},
					     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-5",
					             methodName : "decreaseBrightness",
							args : [
								{value:"10", type:"int"}
							]
					     },
				    	 {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-6",
					         	methodName : "decreaseBrightness",
							args : [
								{value:"10", type:"int"}
							]
					     }
					]
				]
			}
		]
	]
}
