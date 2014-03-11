name:T9
author:testMaker
comment:"Faire clignoter toutes les lampes sauf celles du salon."
_DECL_


_BODY_


[_select(["lamp"],[]) N _select(["lamp"],["salon"])].blink();

