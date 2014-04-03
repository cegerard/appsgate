define([], function () {
    var returnedModule = function () {
        var _name = 'clock module';
        this.getName = function () {return _name;}


   /**
	* Set the time for the system clock time tile
	* from the human date format Wed Dec 18 19:42:57 CET 2013
 	*/
	this.setSystemClockTime = function (time)
	{
		var clocksetIdv = document.getElementById("ClockSet");
		var clockDate = document.getElementById("clockDate");
		var clockDay = document.getElementById("clockDay");
				
		splitString = time.split(" ");
		hm = splitString[3].split(":")
				
		clocksetIdv.innerHTML = hm[0]+":"+hm[1];
		clockDate.innerHTML = splitString[2];
		clockDay.innerHTML = splitString[0]+" "+splitString[1]+" "+splitString[5];
	}

	/**
 	 * Set the time for the system clock time tile
 	 * from the time in milis
	 */
	this.setSystemClockMilisTime = function (time)
	{
		var clocksetIdv = document.getElementById("ClockSet");
		var clockDate = document.getElementById("clockDate");
		var clockDay = document.getElementById("clockDay");
	
		date = new Date();
		date.setTime(time);
				
		clocksetIdv.innerHTML = date.getHours()+":"+date.getMinutes();
		clockDate.innerHTML = date.getDate();
		clockDay.innerHTML = getDayString(date.getDay())+" "+getMonthString(date.getMonth())+
																	" "+date.getYear();
	}

	/**
	 * Set the flow rate for the system clock date tile
	 */
	this.setSystemClockFlowRate = function (flowRate)
	{
		var flowRateDiv = document.getElementById("flowRate");
		flowRateDiv.innerHTML = "speed "+flowRate;
	}

	/**
	 * return the three caracter day name from
	 * day number in the week
	 */
	function getDayString(dayNumber)
	{
		var day;
		switch (dayNumber) {
 			case 1:
 				day = "Mon";
 				break;
 			case 2:
 				day = "Tue";
 				break;
 			case 3:
				day = "Wed";
 				break;
 			case 4:
 				day = "Thu";
 				break;
 			case 5:
 				day = "Fri";
 				break;
 			case 6:
 				day = "Sat";
 				break;
 			default: 
 				day = "Sun";
 				break;
		}
		return day;
	}

	/**
	 * return the three caracter month name from
	 * day month in the year
	 */
	function getMonthString(monthNumber){
		var month;
		switch (monthNumber) {
 			case 1:
 				month = "Feb";
 				break;
 			case 2:
 				month = "Mar";
 				break;
 			case 3:
				month = "Apr";
 				break;
 			case 4:
 				month = "May";
 				break;
 			case 5:
 				month = "Jun";
 				break;
 			case 6:
 				month = "Jul";
 				break;
 			case 7:
 				month = "Aug";
 				break;
 			case 8:
 				month = "Sep";
 				break;
 			case 9:
 				month = "Oct";
 				break;
 			case 10:
 				month = "Nov";
 				break;
 			case 11:
 				month = "Dec";
 				break;
 			default: 
 				month = "Jan";
 				break;
		}
		return month;
	}

    };
    return returnedModule;
});