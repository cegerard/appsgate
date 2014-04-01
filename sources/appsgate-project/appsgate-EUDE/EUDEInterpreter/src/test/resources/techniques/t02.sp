name: T2
author: testMaker
comment: "Tant que L1 est allumé alors L2 est allumé, et lorsque ça n’est plus la cas (L1 n’est plus allumé) alors éteindre L2."

_DECL_

var l1 = /L1/;
var l2 = /L2/;

_BODY_

<<<
_while(l1.isOfState(isOn)) {
 l2.On();
} then {
 l2.Off();
}
>>>
