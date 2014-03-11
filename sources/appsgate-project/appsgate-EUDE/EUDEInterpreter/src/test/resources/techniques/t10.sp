name:T10
author:testMaker
comment:"Faire clignoter toutes les lampes sauf celles du salon qui ont été allumées entre il y a 2h et il y a 1h."
_DECL_


_BODY_


[_select(["lamp"],[]) N _selectState(_select(["lamp"],["salon"]),isOn, "true", -7200,3600)].blink();

