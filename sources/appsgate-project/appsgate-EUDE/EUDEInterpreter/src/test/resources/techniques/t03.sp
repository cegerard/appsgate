name: T3
author: testMaker
comment: "Tant que L1 est allumé alors L2 est allumé, et lorsque ça n’est plus la cas (L1 n’est plus allumé) alors éteindre L2. Tant que L2 est allumé alors L1 est allumé, et lorsque ça n’est plus la cas (L2 n’est plus allumé) alors éteindre L1. // Vérifier que les 2 lumières sont alors couplées"

_DECL_

var l1 = /L1/;
var l2 = /L2/;

_BODY_

_while(l1.isOfState(isOn)) { l2.switchOn(); } then { l2.switchOff(); }
_while(l2.isOfState(isOn)) { l1.switchOn(); } then { l1.switchOff(); }

