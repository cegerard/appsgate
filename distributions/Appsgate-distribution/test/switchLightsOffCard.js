{
	id:"0000000002",
	runningState:"DEPLOYED",
	userInputSource: "",
	source:{
		programName : "LightsOff",
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
							sourceId : "ENO2840bb",
							eventName : "inserted",
							eventValue : false
						}
					],
					seqRulesThen : [
						[
							{
							    type : "NodeAction",
							    targetType : "device",
							    targetId : "194.199.23.135-1",
							    	methodName : "Off",
						    	args : [
							    ]
						     },
						     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-2",
					    	         methodName : "Off",
						    	 args : [
							     ]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-3",
					    	         methodName : "Off",
						    	 args : [
						     	]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-4",
						             methodName : "Off",
							     args : [
						    	 ]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-5",
						             methodName : "Off",
							     args : [
						    	 ]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-6",
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
}
