{
	programName : "DefaultLightMode",
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
						eventValue : "true"
					}
				],
				seqRulesThen : [
					[
						
						{
							type : "NodeAction",
							targetType : "device",
							targetId : "194.199.23.136-1",
								methodName : "setColor",
							args : [
								{value:"14922", type:"long"}
							]
						},
					     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-2",
					             methodName : "setColor",
						     args : [
						     	{value:"14922", type:"long"}
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-3",
					         	methodName : "setColor",
						     args : [
						     	{value:"14922", type:"long"}
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-4",
					        	methodName : "setColor",
						     args : [
						     	{value:"14922", type:"long"}
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-5",
					             methodName : "setColor",
						     args : [
						     	{value:"14922", type:"long"}
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-6",
					             methodName : "setColor",
						     args : [
						     	{value:"14922", type:"long"}
						     ]
					     },
					     
					     {
							type : "NodeAction",
							targetType : "device",
							targetId : "194.199.23.136-1",
								methodName : "setEffect",
							args : [
								{value:"none", type:"String"}
							]
						},
					     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-2",
					             methodName : "setEffect",
						     args : [
						     	{value:"none", type:"String"}
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-3",
					         	methodName : "setEffect",
						     args : [
						     	{value:"none", type:"String"}
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-4",
					        	methodName : "setEffect",
						     args : [
						     	{value:"none", type:"String"}
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-5",
					             methodName : "setEffect",
						     args : [
						     	{value:"none", type:"String"}
						     ]
					     },
				    	     {
						     type : "NodeAction",
						     targetType : "device",
						     targetId : "194.199.23.136-6",
					             methodName : "setEffect",
						     args : [
						     	{value:"none", type:"String"}
						     ]
					     },
					]
				]
			}
		]
	]
}
