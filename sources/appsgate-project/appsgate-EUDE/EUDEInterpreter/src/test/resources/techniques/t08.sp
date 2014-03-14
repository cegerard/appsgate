name:T8
author:testMaker
comment:"Allumer les lampes du salon qui ont été allumées entre il y a 2h et il y a 1h."
_DECL_

_BODY_
<<<
_selectState(_select(["lamp"],["salon"]),isOn,"true",-7200,3600).switchOn();
>>>
