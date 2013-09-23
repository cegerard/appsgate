{
	id:"0000000003",
	runningState:"DEPLOYED",
	userInputSource: "",
	source:{
		programName : "DefaultLightMode",
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
							eventValue : "true"
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
									{value:"none", type:"String"}
								]
							},
						     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-2",
						             methodName : "setEffect",
							     args : [
						    	 	{value:"none", type:"String"}
						     	]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-3",
					    	     	methodName : "setEffect",
						    	 args : [
							     	{value:"none", type:"String"}
							     ]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-4",
					        		methodName : "setEffect",
							     args : [
							     	{value:"none", type:"String"}
							     ]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
						    	 targetId : "194.199.23.135-5",
						             methodName : "setEffect",
							     args : [
							     	{value:"none", type:"String"}
							     ]
						     },
				    		     {
							     type : "NodeAction",
						    	 targetType : "device",
							     targetId : "194.199.23.135-6",
						             methodName : "setEffect",
							     args : [
							     	{value:"none", type:"String"}
							     ]
						     }
						],
						[
							{
								type : "NodeAction",
								targetType : "device",
								targetId : "194.199.23.135-1",
									methodName : "setColor",
								args : [
									{value:"14922", type:"long"}
								]
							},
						     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-2",
					    	         methodName : "setColor",
						    	 args : [
						     		{value:"14922", type:"long"}
							     ]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-3",
					    	     	methodName : "setColor",
						    	 args : [
						     		{value:"14922", type:"long"}
							     ]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-4",
					    	    	methodName : "setColor",
						    	 args : [
						     		{value:"14922", type:"long"}
							     ]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-5",
					    	         methodName : "setColor",
						    	 args : [
						     		{value:"14922", type:"long"}
							     ]
						     },
					    	     {
							     type : "NodeAction",
							     targetType : "device",
							     targetId : "194.199.23.135-6",
					    	         methodName : "setColor",
						    	 args : [
						     		{value:"14922", type:"long"}
							     ]
						     }
						]
					]
				}
			]
		]
	}
}
