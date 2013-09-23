{
	id:"0000000004",
	runningState:"DEPLOYED",
	userInputSource: "",
	source:{
		programName : "ColorLoopMode",
		seqParameters : [
		],
		author : "Bob",
		target : "Alice",
		daemon : "true",
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
								targetId : "194.199.23.135-1",
									methodName : "setEffect",
								args : [
									{value:"colorloop", type:"String"}
								]
							},
						     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-2",
					            	 methodName : "setEffect",
								args : [
									{value:"colorloop", type:"String"}
								]
						     },
					    	 {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-3",
					        	 	methodName : "setEffect",
								args : [
									{value:"colorloop", type:"String"}
								]
						     },
					    	 {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-4",
					    	    	methodName : "setEffect",
								args : [
									{value:"colorloop", type:"String"}
								]
						     },
					    	 {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-5",
					    	         methodName : "setEffect",
								args : [
									{value:"colorloop", type:"String"}
								]
						     },
					    	{
							     type : "NodeAction",
								     targetType : "device",
							     targetId : "194.199.23.135-6",
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
}
