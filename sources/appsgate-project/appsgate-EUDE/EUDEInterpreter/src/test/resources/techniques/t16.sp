name:T16
author:testMaker
comment:""

_DECL_

var l1 = /L1/;
var b1 = /B1/;
var b2 = /B2/;
var b3 = /B3/;

_BODY_

{
_when [l1.isOn("")/OR] then
{
_when[b3.clicked(""),[[b1.clicked("")/OR/2/2],[b2.clicked("")/OR/2/2]/THEN]/AND/0/5] then
{
_selectState(_select(["lamp"],["salon"]),isOn, "true",0,0).blink();
}
}

}
