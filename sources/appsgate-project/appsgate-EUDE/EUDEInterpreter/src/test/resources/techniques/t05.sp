name:T5
author:testMaker
comment:"Lorsque L2 est allumé et que dans un intervalle de 10 secondes (B1 est appuyé et dans un intervalle de 5 secondes (B2 est appuyé puis B3 est appuyé puis B2 est appuyé) ), alors si L1 est allumé alors éteindre L1 sinon allumer L1."

_DECL_

var l1 = /L1/;
var l2 = /L2/;
var b1 = /B1/;
var b2 = /B2/;
var b3 = /B3/;

_BODY_

<<<
_when[l2.switchOn(""),[b1.click(""),[b2.click(""),b3.click(""),b2.click("")/THEN]/AND/0/5] /AND/0/10] 
THEN
{
  l1.toggle();
}
>>>
