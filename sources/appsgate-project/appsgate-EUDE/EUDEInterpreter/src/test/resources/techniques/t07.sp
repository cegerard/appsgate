name:T7
author:testMaker
comment:"Allumer les lampes du salon qui étaient allumées il y a 1 heure."
_DECL_

_BODY_

_selectState(_select(["lamp"],["salon"]),isOn,"true",-3600,0).switchOn();
