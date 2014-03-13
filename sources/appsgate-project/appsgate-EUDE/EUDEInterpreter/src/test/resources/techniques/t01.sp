name:T1
author:testMaker
comment:"Lorsque, dans un intervalle de 5 secondes, L1 s’allume puis B1 est pressé, alors allumer L2"

_DECL_

var l1 = /L1/;
var l2 = /L2/;
var b1 = /B1/;

_BODY_


_when[l1.switchOn(""),b1.click("") /THEN/0/5] 
THEN
{
  l2.switchOn();
}
