var appsgateMain;

require(['websocket', 'clock'], function(websocketRef, clockModuleRef){
//Require begin

	appsgateMain = new (function () {
	
		var websocket = new websocketRef();
        var clockModule = new clockModuleRef();
	
		this.getWebSocket = function () {return websocket;}
		this.getClockModule = function () {return clockModule;}
	
		/**
     	 * Connect to the AppsGate server through the web socket module
		 */
		this.connect = function ()
		{
			websocket.WebSocketOpen();
		}
	
		/**
 	     * Disconnect from the AppsGate server through the web socket module
		 */
		this.disconnect = function disconnect()
		{
			websocket.WebSocketClose();
		}

		/**
 	     * Get back to the home section menu
		 */
		this.backHome = function backHome()
		{
			//Display the top panorama div
			var sections = document.getElementById("top-panorama");
			sections.style.display="";
	 
			//hide the display use panorama for sub menu
			var panorama = document.getElementById("display-panorama")
			panorama.innerHTML="";
			panorama.style.display="";
	 
			//Remove browsed element from navigation bar
			var navBar = document.getElementById("path-app");
			var iLength = navBar.getElementsByTagName("li").length;
			for (i = 1; i < iLength; i++) { 
				navBar.removeChild(navBar.children[i]);
			}
	 
			//Set active the Home link in navigation bar
			navBar.children[0].setAttribute("class", "active");
		}

		/**
		 * Get to the Philips HUE sub menu
	 	 */
		this.goToHueSubMenu = function ()
		{
			//hide the top panorama div
			var sections = document.getElementById("top-panorama");
			sections.style.display="none";
	 
			//Get the html source for Philips HUE
			var httpRequest=new XMLHttpRequest();
			httpRequest.open("GET","./html/hue.html",false);
			httpRequest.send();
			//Displays it in the display panorama
			var panorama = document.getElementById("display-panorama")
			panorama.innerHTML=httpRequest.responseText;
			panorama.style.display="";
	 
			//Update the navigation bar
			var navBar = document.getElementById("path-app");
			var li = document.createElement('li');
			li.setAttribute("class", "active");
			var liContent = document.createTextNode("Philips HUE");
			li.appendChild(liContent);
			navBar.appendChild(li);
		}


		/**
		 * Server notifications messages interpretation
		 */
		this.notificationHandler = function (message)
		{
  	
			if (message.varName == "ClockSet"){
				clockModule.setSystemClockTime(message.value);
	 		} else if(message.varName == "flowRate"){
   	 			clockModule.setSystemClockFlowRate(message.value);	
   			}  
		}

		/**
 		 * Server return call messages interpretation
 		 */
		this.returnCallHandler = function (callId, message)
		{
			if (callId == "cf-get-devices")
			{
    			var tab_devices = message.value;
    			var jsonArray = JSON.parse(tab_devices);
    			var l_tab = jsonArray.length;
    		
    			for(i = 0; i < l_tab; i++) 
    			{
    				obj = jsonArray[i];
    		
    				//System clock initialization
       				if (obj.type == "21") 
       				{
       					var httpRequest=new XMLHttpRequest();
						httpRequest.open("GET","./html/clock.html",false);
						httpRequest.send();
						var home_left_col = document.getElementById("home-left-col");
						var clockDiv = document.createElement('div');
						clockDiv.innerHTML = httpRequest.responseText;
						clockDiv.setAttribute("id", obj.id);
						home_left_col.appendChild(clockDiv);
				
						//Set the time
						clockModule.setSystemClockMilisTime(obj.clockValue);
						//Set time flow rate
						clockModule.setSystemClockFlowRate(obj.flowRate);
       				}	
       			}
    		}
		}
		
		/**
 		 * Shutdown the AppsGate server
 		 */
		this.shutdown = function ()
		{
			websocket.send("{\"method\":\"shutdown\", \"args\":[], \"callId\":\"cf-shutdown\"}");
		}
		
	})(); //AppsGate main object definition end
	
	appsgateMain.connect();
	
});//Require end

