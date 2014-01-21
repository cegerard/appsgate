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
			httpRequest.open("GET","./html/philipshue.html",false);
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
			
			this.sendCmd("{\"getHUEConfDevices\":{}, \"CONFIGURATION\":\"getHUEConfDevices\", \"TARGET\":\"PHILIPSHUE\"}");
		}
		
		this.gotToNextSubMenu = function(name, html) 
		{
			//Displays the html source in the display panorama
			var panorama = document.getElementById("display-panorama")
			panorama.innerHTML=html;
			panorama.style.display="";
			
			//Update the navigation bar
			var navBar = document.getElementById("path-app");
			var li = document.createElement('li');
			li.setAttribute("class", "active");
			var liContent = document.createTextNode(name);
			li.appendChild(liContent);
			//toggle the previous menu entry to active link
			var children = navBar.children;
			var lastChild = children[children.length-1]
			var divider = document.createElement('span');
			divider.setAttribute("class", "divider");
			divider.innerHTML = "/";
			lastChild.appendChild(divider);
			//li.setAttribute("class", "active");
			
			navBar.appendChild(li);
		}


		/**
		 * Server notifications messages interpretation
		 */
		this.notificationHandler = function (message)
		{
			if(message.hasOwnProperty("newDevice")) {
				var newDevice = message.newDevice;
				
				//Tiles update
   				if (newDevice.deviceType == "PHILIPS_HUE_LIGHT") {
   					var hue_tile_light_count = document.getElementById("philips-index-tile-count");
   					var currentCount = hue_tile_light_count.innerHTML.valueOf();
   					currentCount++;
   					hue_tile_light_count.innerHTML = currentCount;
   				}

			}else {
			
				if (message.varName == "ClockSet"){
					clockModule.setSystemClockTime(message.value);
				} else if(message.varName == "flowRate"){
   	 				clockModule.setSystemClockFlowRate(message.value);	
				} 
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
    		
    			var PhilipsLightCount = 0;
    			
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
       				
       				//Tile initialization
       				if (obj.deviceType == "PHILIPS_HUE_LIGHT") {
       					PhilipsLightCount++;
       					var hue_tile_light_count = document.getElementById("philips-index-tile-count");
       					hue_tile_light_count.innerHTML = PhilipsLightCount;
       				}
       			}
    		}
		}
		
		/*****************************************/
		/**			Console Commands			**/
		/*****************************************/
		
		/**
 		 * Shutdown the AppsGate server
 		 */
		this.shutdown = function ()
		{
			var call = eval({"method":"shutdown", "args":[], "callId":"cf-shutdown"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Send a command to the AppsGate server
 		 */
		this.sendCmd = function (msg)
		{
			websocket.send(msg);
		}
		
		/**
 		 * Send a command to the AppsGate server
 		 */
		this.sendJSONCmd = function (jsonmsg)
		{
			websocket.send(JSON.stringify(jsonmsg));
		}
		
		/********
		 Device management
		 		  ********/
		/**
 		 * Get the list of device
 		 */
		this.getDevices = function ()
		{
			var call = eval({"method":"getDevices", "args":[], "callId":"cf-getdevices"});
			websocket.send(JSON.stringify(call));
		}
		
		/********
		 Device name management
		 			   ********/
		/**
 		 * add user object name
 		 */
		this.setUserObjectName = function (objectId, user, name)
		{
			var call = eval({"method":"setUserObjectName", "args":[{"type":"String", "value":objectId},{"type":"String", "value":user},{"type":"String", "value":name}], "callId":"cf-setuserobjectname"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * get user object name
 		 */
		this.getUserObjectName = function (objectId, user)
		{
			var call = eval({"method":"getUserObjectName", "args":[{"type":"String", "value":objectId},{"type":"String", "value":user}], "callId":"cf-getuserobjectname"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * remove user object name
 		 */
		this.deleteUserObjectName = function (objectId, user)
		{
			var call = eval({"method":"deleteUserObjectName", "args":[{"type":"String", "value":objectId},{"type":"String", "value":user}], "callId":"cf-deleteuserobjectname"});
			websocket.send(JSON.stringify(call));
		}
		
		/********
		 Place management
		 	     ********/
		/**
 		 * Get all places
 		 */
		this.getPlaces = function ()
		{
			var call = eval({"method":"getPlaces", "args":[], "callId":"cf-getplaces"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * add new place
 		 */
		this.addPlace = function (id, name)
		{
			var place = eval({"id":id, "name":name, "devices":[] });
			var call = eval({"method":"newPlace", "args":[{"type":"JSONObject", "value":JSON.stringify(place)}], "callId":"cf-addplace"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * rename existing place
 		 */
		this.renamePlace = function (id, name)
		{
			var place = eval({"id":id, "name":name});
			var call = eval({"method":"updatePlace", "args":[{"type":"JSONObject", "value":JSON.stringify(place)}], "callId":"cf-renameplace"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * remove a place
 		 */
		this.removePlace = function (id)
		{
			var call = eval({"method":"removePlace", "args":[{"type":"String", "value":id}], "callId":"cf-removeplace"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * move device into a place
 		 */
		this.moveDeviceTo = function (objId, srcPlaceId, destPlaceId)
		{
			var call = eval({"method":"moveDevice", "args":[{"type":"String", "value":objId}, {"type":"String", "value":srcPlaceId}, {"type":"String", "value":destPlaceId}], "callId":"cf-movedeviceto"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get core object place id
 		 */
		this.getCoreObjectPlaceId = function (objId)
		{
			var call = eval({"method":"getCoreObjectPlaceId", "args":[{"type":"String", "value":objId}], "callId":"cf-getcoreobjectplaceid"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get object associated place id
 		 */
		this.getCoreObjectPlaceId = function (objectId)
		{
			var call = eval({"method":"getCoreObjectPlaceId", "args":[{"type":"String", "value":objectId}], "callId":"cf-getcoreobjectplaceid"});
			websocket.send(JSON.stringify(call));
		}
		
		/********
		 End User management
		 	        ********/
		/**
 		 * Get users
 		 */
		this.getUsers = function ()
		{
			var call = eval({"method":"getUsers", "args":[], "callId":"cf-getusers"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Create user
 		 */
		this.createUser = function (login, psw, lastName, firstName, role)
		{
			var call = eval({"method":"createUser", "args":[{"type":"String", "value":login}, {"type":"String", "value":psw}, {"type":"String", "value":lastName}, {"type":"String", "value":firstName}, {"type":"String", "value":role}], "callId":"cf-createuser"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Delete user
 		 */
		this.deleteUser = function (login, psw)
		{
			var call = eval({"method":"deleteUser", "args":[{"type":"String", "value":login}, {"type":"String", "value":psw}], "callId":"cf-deleteuser"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get user details
 		 */
		this.getUserDetails = function (login)
		{
			var call = eval({"method":"getUserDetails", "args":[{"type":"String", "value":login}], "callId":"cf-getuserdetails"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get user complete inforamtion
 		 */
		this.getUserFullDetails = function (login)
		{
			var call = eval({"method":"getUserFullDetails", "args":[{"type":"String", "value":login}], "callId":"cf-getUserfulldetails"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Check the unicity of a login
 		 */
		this.checkIfIdIsFree = function (login)
		{
			var call = eval({"method":"checkIfIdIsFree", "args":[{"type":"String", "value":login}], "callId":"cf-checkforfreelogin"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Associate a device id user
		 */
		this.associateDevice = function (login, psw, objId)
		{
			var call = eval({"method":"associateDevice", "args":[{"type":"String", "value":login}, {"type":"String", "value":psw}, {"type":"String", "value":objId}], "callId":"cf-associatedevice"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Associate a device id user
		 */
		this.separateDevice = function (login, psw, objId)
		{
			var call = eval({"method":"separateDevice", "args":[{"type":"String", "value":login}, {"type":"String", "value":psw}, {"type":"String", "value":objId}], "callId":"cf-separatedevice"});
			websocket.send(JSON.stringify(call));
		}
		
		/********
		 Programs management
		 	        ********/
		/**
 		 * Get the program list
 		 */
		this.getPrograms = function ()
		{
			var call = eval({"method":"getPrograms", "args":[], "callId":"cf-getprograms"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Add a program
		 */
		this.addProgram = function (pgmJson)
		{
			var call = eval({"method":"addProgram", "args":[{"type":"JSONObject", "value":pgmJson}], "callId":"cf-addprogram"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * remove a program
		 */
		this.removeProgram = function (pgmId)
		{
			var call = eval({"method":"removeProgram", "args":[{"type":"String", "value":pgmId}], "callId":"cf-removeprogram"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Update a program
		 */
		this.updateProgram = function (pgmJson)
		{
			var call = eval({"method":"updateProgram", "args":[{"type":"JSONObject", "value":pgmJson}], "callId":"cf-updateprogram"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Start a program
		 */
		this.startProgram = function (pgmId)
		{
			var call = eval({"method":"callProgram", "args":[{"type":"String", "value":pgmId}], "callId":"cf-startprogram"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Stop a program
		 */
		this.stopProgram = function (pgmId)
		{
			var call = eval({"method":"stopProgram", "args":[{"type":"String", "value":pgmId}], "callId":"cf-stopprogram"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Pause a program
		 */
		this.pauseProgram = function (pgmId)
		{
			var call = eval({"method":"pauseProgram", "args":[{"type":"String", "value":pgmId}], "callId":"cf-pauseprogram"});
			websocket.send(JSON.stringify(call));
		}
		
		/********
		 Service account management
		 	               ********/
		
	})(); //AppsGate main object definition end
	
	appsgateMain.connect();
	
});//Require end

