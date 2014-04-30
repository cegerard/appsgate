{
	programName : "GestionAmbiance",
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
