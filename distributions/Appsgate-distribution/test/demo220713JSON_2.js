{
	programName : "Demo220713_2",
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
						sourceId : "ENO1645c",
						eventName : "value",
						eventValue : "0"
					}
				],
				seqRulesThen : [
					[
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-4",
					             methodName : "On",
						     args : [
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-6",
					             methodName : "On",
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
						sourceId : "ENO1645c",
						eventName : "value",
						eventValue : "900"
					}
				],
				seqRulesThen : [
					[
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-4",
					             methodName : "Off",
						     args : [
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-6",
					             methodName : "Off",
						     args : [
						     ]
					     }
					]
				]
			}
		]
	]
}
