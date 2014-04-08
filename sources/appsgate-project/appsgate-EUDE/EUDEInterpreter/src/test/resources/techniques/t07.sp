name:T7
author:testMaker
comment:"Allumer les lampes du salon qui étaient allumées il y a 1 heure."
_DECL_

var lampeSalon = _selectState(_select(["lamp"],[]),state,"true",-3600,3600);

_BODY_

<<<
lampeSalon.blink();
>>>
