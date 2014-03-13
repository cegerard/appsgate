name:T6
author:testMaker
comment:""

_DECL_

var l1 = /L1/;
var l2 = /L2/;
var b1 = /B1/;
var b2 = /B2/;
var b3 = /B3/;

_BODY_


<<<
_when[
      [l1.switchOn(""),l2.switchOn("")/OR/1],
      [b1.click(""),
       [b2.click(""),b3.click(""),b2.click("")/THEN]
      /AND/0/5] 
    /AND/0/10] 
THEN
{
  l1.toggle();
}
>>>
