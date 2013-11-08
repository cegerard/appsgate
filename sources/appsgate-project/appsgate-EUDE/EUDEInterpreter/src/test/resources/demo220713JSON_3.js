{
	programName : "Demo220713_3",
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
						eventValue : "0"
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
						     targetId : "194.199.23.136-5",
					             methodName : "setGreen",
						     args : [
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-3",
					             methodName : "setPurple",
						     args : [
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-2",
					         methodName : "setRed",
						     args : [
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-1",
					         methodName : "setPink",
						     args : [
						     ]
					     }
						  
					]
				]
			}
		]
	]
}
