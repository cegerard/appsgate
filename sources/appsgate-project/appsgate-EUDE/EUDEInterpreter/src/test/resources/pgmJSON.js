{
	id:"0000000001",
        programName : "GestionAmbiance",
	runningState:"DEPLOYED",
        userInputSource: "",

        seqParameters : [
		{ type : "Ambiance", varName : "a" }
	],
	author : "Bob",
	target : "Alice",
	seqDefinitions : [
		{ varName : "L", selector : "lampes du salon" },
		{ varName : "V", selector : "volets du salon" }
	],
	seqRules : [
		{
			type : "NodeIf",
			expBool : "a == 'film'",
			seqRulesTrue : [
				{
					seqRulesAnd : [
						{
							type : "NodeAction",
							action : "allumer L"
						},
						{
							type : "NodeAction",
							action : "fermer V"
						}
					]
				}
			],
			seqRulesFalse : [
				{
					seqRulesThen : [
						{
							type : "NodeAction",
							action : "ouvrir V"
						},
						{
							type : "NodeAction",
							action : "eteindre L"
						}
					]
				}
			]
		}
	]
}
