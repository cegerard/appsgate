name:T13
author:testMaker
comment:""

_DECL_

var l1 = /L1/;
var l2 = /L2/;
var b1 = /B1/;

_BODY_

{
_when [b1.clicked("")/OR] then
<<<
{
l1.On();
_while(l1.isOfState(isOn)) {
_keep(l2, isOn);
} then
{
l2.Off();
}
}
{_when[b1.clicked("")/OR/3/3] then {
l1.toggle();
}
}

>>>
}
