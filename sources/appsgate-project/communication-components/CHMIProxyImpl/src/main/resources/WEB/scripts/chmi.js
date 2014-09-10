var chmi;
var chmiConfGUIVersion = '0.6.5';

require.config({
    paths: {
        'jQuery': 'vendor/jquery-2.1.0.min',
    },
    shim: {
        'jQuery': {
            exports: '$'
        }
    }
});

require(['websocket', 'clock', 'jQuery'], function(websocketRef, clockModuleRef, $){
//Require begin

	chmi = new (function () {
	
		console.log('CHMI configuration GUI version:', chmiConfGUIVersion); 
		console.log('jQuery version:', $.fn.jquery); // 2.1.0
		
		var websocket = new websocketRef();
        var clockModule = new clockModuleRef();
        var handlerMap = {};
	
		this.getWebSocket = function () {return websocket;}
		this.getClockModule = function () {return clockModule;}
	
		/**
     	 * Connect to the CHMI through the web socket module
		 */
		this.connect = function ()
		{
			websocket.WebSocketOpen();
		}
	
		/**
 	     * Disconnect from the CHMI through the web socket module
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
			var homeLi = navBar.children[0];
			while( navBar.firstChild) {
				navBar.removeChild( navBar.firstChild);
			}
			navBar.appendChild(homeLi);
			this.clearNotifHandler();
		}

		/**
		 * Get to the Philips HUE sub menu
	 	 */
		this.goToHueSubMenu = function ()
		{
			//Get the html source for Philips HUE
			var httpRequest=new XMLHttpRequest();
			httpRequest.open("GET","./html/philipshue/philipshue.html",false);
			httpRequest.send();
			
			this.gotToNextSubMenu("Philips HUE", httpRequest.responseText, "");
			
			this.sendCmd("{\"getHUEConfDevices\":{}, \"CONFIGURATION\":\"getHUEConfDevices\", \"TARGET\":\"PHILIPSHUE\"}");
		}
		
		/**
		 * Get to the EnOcean configuration sub menu
	 	 */
		this.goToEnOceanSubMenu = function ()
		{
			//Get the html source for Philips HUE
			var httpRequest=new XMLHttpRequest();
			httpRequest.open("GET","./html/enocean/enocean.html",false);
			httpRequest.send();
			
			this.gotToNextSubMenu("EnOcean", httpRequest.responseText, "");
			
			this.sendCmd("{\"getConfDevices\":{}, \"CONFIGURATION\":\"getConfDevices\", \"TARGET\":\"ENOCEAN\"}");
		}
		
        
        /**
         * Get to the UPnP configuration sub menu
         */
        this.goToUPnPSubMenu = function ()
        {
            //Get the html source for UPnP
            var httpRequest=new XMLHttpRequest();
            httpRequest.open("GET","./html/upnp/upnp.html",false);
            httpRequest.send();
            this.gotToNextSubMenu("UPnP", httpRequest.responseText, "");
            this.sendCmd("{\"getMediaServices\":{}, \"CONFIGURATION\":\"getMediaServices\", \"TARGET\":\"UPNP\"}");
        }
        
        /**
         *  Get the time line configuration sub menu
         */
        this.goToTimeLinesSubMenu = function ()
        {
           //Get the html source for time lines
			var httpRequest=new XMLHttpRequest();
			httpRequest.open("GET","./html/debugger/debugger.html",false);
			httpRequest.send();
			
			this.gotToNextSubMenu("TimeLines", httpRequest.responseText, "");
            this.sendJSONCmd(eval({"method":"getTraceManStatus", "args":[], "callId":"getTraceManConf", "TARGET":"EHMI"}))
        }
        
		/**
		 * Go to the newt sub menu generic method.
		 */
		this.gotToNextSubMenu = function(name, html, goBackHandler) 
		{
			//hide the top panorama div
			var sections = document.getElementById("top-panorama");
			sections.style.display="none";
			
			//Displays the html source in the display panorama
			var panorama = document.getElementById("display-panorama")
			panorama.innerHTML=html;
			panorama.style.display="";
			
			//Update the navigation bar
			var navBar = document.getElementById("path-app");
			var li = document.createElement('li');
			li.className = "active";
			var liContent = document.createTextNode(name);
			li.appendChild(liContent);
			var divider = document.createElement('span');
			divider.setAttribute("class", "divider");
			divider.innerHTML = "/";
			li.appendChild(divider);
			
			if (goBackHandler != "") {
				//toggle the previous menu entry to active link
				var children = navBar.children;
				var pos = children.length-1;
				var lastChild = children[pos];
				var devider = lastChild.children[0];
				
				if(!devider.hasOwnProperty("href")) {
					lastChild.removeChild(devider);
					var linkName = lastChild.innerHTML;
					lastChild.innerHTML = "";
					var alink = document.createElement('a');
					alink.setAttribute("href", "#");
					alink.setAttribute("onclick", "javascript:chmi.goBackToPreviousNavSubMenu("+pos+");"+goBackHandler);
					alink.innerHTML = linkName;
					lastChild.appendChild(alink);
					lastChild.appendChild(devider);
				}
			}
			
			navBar.appendChild(li);
		}
		
		this.goBackToPreviousNavSubMenu = function (index) 
		{
			var navBar = document.getElementById("path-app");
			while( navBar.children[index]) {
				navBar.removeChild( navBar.children[index]);
			}
		}


		/**
		 * Server notifications messages interpretation
		 */
		this.notificationHandler = function (message)
		{
			if(message.hasOwnProperty("newDevice")) {
				var newDevice = message.newDevice;
				
				if(newDevice.hasOwnProperty("deviceType")) {
					//Tiles update
					if (newDevice.deviceType == "PHILIPS_HUE_LIGHT") {
						var hue_tile_light_count = document.getElementById("philips-index-tile-count");
						var currentCount = hue_tile_light_count.innerHTML.valueOf();
						currentCount++;
						hue_tile_light_count.innerHTML = currentCount;
						//Not use cause we ask the PhilipsHUE adapter directly
						//this.getWebSocket().getHue().notificationHandler(message);
					} else if (newDevice.deviceType.indexOf("EEP") != -1 || newDevice.deviceType == "EnOcean_DEVICE") {
						var currentCount = $("#enocean-index-tile-count").html();
						currentCount++;
						$("#enocean-index-tile-count").html(currentCount);
						//Use cause we catch ApAM EnOcean core device instantiation instance 
						this.getWebSocket().getEnocean().notificationHandler(message);
   					}
				}
   				
			} else {
			
				if (message.varName == "ClockSet"){
					clockModule.setSystemClockTime(message.value);
				} else if(message.varName == "flowRate"){
   	 				clockModule.setSystemClockFlowRate(message.value);	
				} else if(message.varName == "newFace"){
					$("#domiCube-face-"+message.objectId).html("Face: "+message.value);
				} else if(message.varName == "newDimValue"){
					$("#domiCube-angle-"+message.objectId).html("Angle: "+message.value);
				} else if(message.varName == "newBatteryLevel"){
					$("#domiCube-battery-"+message.objectId).html("Battery: "+message.value+" %");	
				} else { //find the handler for this notification
					var handler = handlerMap[message.objectId]
					if(handler != null) {
						handler(message);
					}
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
    		
    				
       				if (obj.type == "21") //System clock initialization
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
       				else if (obj.type == "210") //DomiCube tile initialization
       				{
       					var httpRequest=new XMLHttpRequest();
						httpRequest.open("GET","./html/domicubeTile.html",false);
						httpRequest.send();
						var domicubeDiv = document.createElement('div');
						domicubeDiv.innerHTML = httpRequest.responseText;
						domicubeDiv.setAttribute("id", obj.id);
       					$("#device-manager-list").append(domicubeDiv);
						$("#domiCube-face").attr("id", "domiCube-face-"+obj.id);
						$("#domiCube-angle").attr("id", "domiCube-angle-"+obj.id);
						$("#domiCube-battery").attr("id", "domiCube-battery-"+obj.id);
       				}
       				
       				
       				//Adapter tiles counters initialization
       				if(obj.hasOwnProperty("deviceType")) {
       					if (obj.deviceType == "PHILIPS_HUE_LIGHT") {
       						PhilipsLightCount++;
       						var hue_tile_light_count = $("#philips-index-tile-count").html(PhilipsLightCount);
       					
       					} else if (obj.deviceType.indexOf("EEP") != -1 || obj.deviceType == "EnOcean_DEVICE") {
       						PhilipsLightCount++;
       						$("#enocean-index-tile-count").html(PhilipsLightCount);
       					}
       				}
       			}
    		}
		}
		
		/**
		 * Add a notification handler
		 */
		this.addNotifHandler = function (objectId, handler) {
			handlerMap[objectId] = handler;
		}
		
		/**
		 * Remove a notification handler
		 */
		this.removeNotifHandler = function (objectId) {
			delete handlerMap[objectId];
		}
		
		/**
		 * Clear the notification map handler
		 */
		this.clearNotifHandler = function (objectId) {
			handlerMap = {};
		}
		
		/**
		 * Add toast message
		 */
		this.addToastMsg = function (alertHTML, alertHeader, alertMessage) {
			var alertContainer = document.getElementById("alerts-container");
			alertContainer.innerHTML = alertHTML;
			alertContainer = document.getElementById("toast-alert-heading");
			alertContainer.innerHTML = alertHeader;
			alertContainer = document.getElementById("toast-alert-message");
			alertContainer.innerHTML = alertMessage;
		}
		
		/**
		 * Remove the current alert message
		 */
		this.removeToastMsg = function () {
			var alertContainer = document.getElementById("alerts-container");
			alertContainer.innerHTML = "";
		}
		
		/*****************************************/
		/**			Console Commands			**/
		/*****************************************/
		
		/**
 		 * Shutdown the server
 		 */
		this.shutdown = function ()
		{
			var call = eval({"method":"shutdown", "args":[], "callId":"cf-shutdown", "TARGET":"CHMI"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Send a command to the server
 		 */
		this.sendCmd = function (msg)
		{
			websocket.send(msg);
		}
		
		/**
 		 * Send a command to the server
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
			var call = eval({"method":"getDevices", "args":[], "callId":"cf-getdevices", "TARGET":"CHMI"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get a device
 		 */
		this.getDevice = function (deviceId)
		{
			var call = eval({"method":"getDevice", "args":[{"type":"String", "value":deviceId}], "callId":"cf-getdevice", "TARGET":"CHMI"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get a device
 		 */
		this.getDevicesByType = function (type)
		{
			var call = eval({"method":"getDevices", "args":[{"type":"String", "value":type}], "callId":"cf-getdevicesbytype", "TARGET":"CHMI"});
			websocket.send(JSON.stringify(call));
		}
		
//		/********
//		 Device properties management
//		 			   ********/
//		/**
// 		 * add user object name
// 		 */
//		this.setUserObjectName = function (objectId, user, name)
//		{
//			var call = eval({"method":"setUserObjectName", "args":[{"type":"String", "value":objectId},{"type":"String", "value":user},{"type":"String", "value":name}], "callId":"cf-setuserobjectname"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * get user object name
// 		 */
//		this.getUserObjectName = function (objectId, user)
//		{
//			var call = eval({"method":"getUserObjectName", "args":[{"type":"String", "value":objectId},{"type":"String", "value":user}], "callId":"cf-getuserobjectname"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * remove user object name
// 		 */
//		this.deleteUserObjectName = function (objectId, user)
//		{
//			var call = eval({"method":"deleteUserObjectName", "args":[{"type":"String", "value":objectId},{"type":"String", "value":user}], "callId":"cf-deleteuserobjectname"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * get object grammar
// 		 */
//		this.getObjectTypeGrammar = function (objectType)
//		{
//			var call = eval({"method":"getGrammarFromType", "args":[{"type":"String", "value":objectType}], "callId":"cf-getObjectTypeGrammar"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/********
//		 Place management
//		 	     ********/
//		
//		/**
// 		 * Get all places for an habitat
// 		 */
//		this.getPlaces = function ()
//		{
//			var call = eval({"method":"getPlaces", "args":[], "callId":"cf-getplaces"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Add new place
// 		 */
//		this.newPlace = function (name, devices, parent)
//		{
//			var place = eval({"name":name, "devcies":devices,  "parent":parent });
//			var call = eval({"method":"newPlace", "args":[{"type":"JSONObject", "value":JSON.stringify(place)}], "callId":"cf-newplace"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Update an existing place
//		 */
//		this.updatePlace = function (data)
//		{
//			var call = eval({"method":"updatePlace", "args":[{"type":"JSONObject", "value":JSON.stringify(data)}], "callId":"cf-updateplace"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * remove an existing place
//		 */
//		this.removePlace = function (placeId)
//		{
//			var call = eval({"method":"removePlace", "args":[{"type":"String", "value":placeId}], "callId":"cf-removeplace"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Move a device in a place
// 		 */
//		this.moveDevice = function (deviceId, srcPlaceId, destPlaceId)
//		{
//			var call = eval({"method":"moveDevice", "args":[{"type":"String", "value":deviceId}, {"type":"String", "value":srcPlaceId}, {"type":"String", "value":destPlaceId}], "callId":"cf-moveDevice"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Move a service in a place
// 		 */
//		this.moveService = function (serviceId, srcPlaceId, destPlaceId)
//		{
//			var call = eval({"method":"moveService", "args":[{"type":"String", "value":serviceId}, {"type":"String", "value":srcPlaceId}, {"type":"String", "value":destPlaceId}], "callId":"cf-moveService"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Get core object place identifier
//		 */
//		this.getObjectPlaceId = function (objectId)
//		{
//			var call = eval({"method":"getCoreObjectPlaceId", "args":[{"type":"String", "value":objectId}], "callId":"cf-getcoreobjectplaceid"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Get places by name
// 		 */
//		this.getPlacesByName = function (name)
//		{
//			var call = eval({"method":"getPlacesByName", "args":[{"type":"String", "value":name}], "callId":"cf-getplacesbyname"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Get places by tags
// 		 */
//		this.getPlacesByTags = function (tagsArray)
//		{
//			var call = eval({"method":"getPlacesWithTags", "args":[{"type":"JSONArray", "value":JSON.stringify(tagsArray)}], "callId":"cf-getplacesbytag"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Get places by property keys
// 		 */
//		this.getPlacesByPropertyKey = function (keysArray)
//		{
//			var call = eval({"method":"getPlacesWithProperties", "args":[{"type":"JSONArray", "value":JSON.stringify(keysArray)}], "callId":"cf-getplacesbyprop"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Get place by property keys and associated value
// 		 */
//		this.getPlacesWithPropertiesValue = function (propArray)
//		{
//			var call = eval({"method":"getPlacesWithPropertiesValue", "args":[{"type":"JSONArray", "value":JSON.stringify(propArray)}], "callId":"cf-getplacesbypropvalue"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Get root places
// 		 */
//		this.getRootPlaces = function ()
//		{
//			var call = eval({"method":"getRootPlaces", "args":[], "callId":"cf-getrootplace"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Add a tag
// 		 */
//		this.addTag = function (id, tag)
//		{
//			var call = eval({"method":"addTag", "args":[{"type":"String", "value":id}, {"type":"String", "value":tag}], "callId":"cf-addTag"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Remove a tag
// 		 */
//		this.removeTag = function (id, tag)
//		{
//			var call = eval({"method":"removeTag", "args":[{"type":"String", "value":id}, {"type":"String", "value":tag}], "callId":"cf-removeTag"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Add a property
// 		 */
//		this.addProperty = function (id, key, value)
//		{
//			var call = eval({"method":"addProperty", "args":[{"type":"String", "value":id}, {"type":"String", "value":key}, {"type":"String", "value":value}], "callId":"cf-addProp"});
//			websocket.send(JSON.stringify(call));
//		}
//	
//		/**
//		 * Remove a porperty
//		 */
//		this.removeProperty = function (id, key)
//		{
//			var call = eval({"method":"removeProperty", "args":[{"type":"String", "value":id}, {"type":"String", "value":key}], "callId":"cf-removeProp"});
//			websocket.send(JSON.stringify(call));
//		}
//	
//		
//		/********
//		 End User management
//		 	        ********/
//		/**
// 		 * Get users
// 		 */
//		this.getUsers = function ()
//		{
//			var call = eval({"method":"getUsers", "args":[], "callId":"cf-getusers"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Create user
// 		 */
//		this.createUser = function (login, psw, lastName, firstName, role)
//		{
//			var call = eval({"method":"createUser", "args":[{"type":"String", "value":login}, {"type":"String", "value":psw}, {"type":"String", "value":lastName}, {"type":"String", "value":firstName}, {"type":"String", "value":role}], "callId":"cf-createuser"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Delete user
// 		 */
//		this.deleteUser = function (login, psw)
//		{
//			var call = eval({"method":"deleteUser", "args":[{"type":"String", "value":login}, {"type":"String", "value":psw}], "callId":"cf-deleteuser"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Get user details
// 		 */
//		this.getUserDetails = function (login)
//		{
//			var call = eval({"method":"getUserDetails", "args":[{"type":"String", "value":login}], "callId":"cf-getuserdetails"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Get user complete inforamtion
// 		 */
//		this.getUserFullDetails = function (login)
//		{
//			var call = eval({"method":"getUserFullDetails", "args":[{"type":"String", "value":login}], "callId":"cf-getUserfulldetails"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
// 		 * Check the unicity of a login
// 		 */
//		this.checkIfLoginIsFree = function (login)
//		{
//			var call = eval({"method":"checkIfLoginIsFree", "args":[{"type":"String", "value":login}], "callId":"cf-checkforfreelogin"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Associate a device id user
//		 */
//		this.associateDevice = function (login, psw, objId)
//		{
//			var call = eval({"method":"associateDevice", "args":[{"type":"String", "value":login}, {"type":"String", "value":psw}, {"type":"String", "value":objId}], "callId":"cf-associatedevice"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Associate a device id user
//		 */
//		this.separateDevice = function (login, psw, objId)
//		{
//			var call = eval({"method":"separateDevice", "args":[{"type":"String", "value":login}, {"type":"String", "value":psw}, {"type":"String", "value":objId}], "callId":"cf-separatedevice"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/********
//		 Service account management
//		 	               ********/
//		
//		/**
//		 * Associate a service account to a user
//		 */
//		this.addServiceAccount = function (login, psw, jsonAccount)
//		{
//			var call = eval({"method":"synchronizeAccount", "args":[{"type":"String", "value":login}, {"type":"String", "value":psw}, {"type":"JSONObject", "value":jsonAccount}], "callId":"cf-syncserviceaccount"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Associate a service account to a user
//		 */
//		this.removeServiceAccount = function (login, psw, jsonAccount)
//		{
//			var call = eval({"method":"desynchronizedAccount", "args":[{"type":"String", "value":login}, {"type":"String", "value":psw}, {"type":"JSONObject", "value":jsonAccount}], "callId":"cf-removeserviceaccount"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/********
//		 Programs management
//		 	        ********/
//		/**
// 		 * Get the program list
// 		 */
//		this.getPrograms = function ()
//		{
//			var call = eval({"method":"getPrograms", "args":[], "callId":"cf-getprograms"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Add a program
//		 */
//		this.addProgram = function (pgmJson)
//		{
//			var call = eval({"method":"addProgram", "args":[{"type":"JSONObject", "value":pgmJson}], "callId":"cf-addprogram"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * remove a program
//		 */
//		this.removeProgram = function (pgmId)
//		{
//			var call = eval({"method":"removeProgram", "args":[{"type":"String", "value":pgmId}], "callId":"cf-removeprogram"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Update a program
//		 */
//		this.updateProgram = function (pgmJson)
//		{
//			var call = eval({"method":"updateProgram", "args":[{"type":"JSONObject", "value":pgmJson}], "callId":"cf-updateprogram"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Start a program
//		 */
//		this.startProgram = function (pgmId)
//		{
//			var call = eval({"method":"callProgram", "args":[{"type":"String", "value":pgmId}], "callId":"cf-startprogram"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Stop a program
//		 */
//		this.stopProgram = function (pgmId)
//		{
//			var call = eval({"method":"stopProgram", "args":[{"type":"String", "value":pgmId}], "callId":"cf-stopprogram"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Pause a program
//		 */
//		this.pauseProgram = function (pgmId)
//		{
//			var call = eval({"method":"pauseProgram", "args":[{"type":"String", "value":pgmId}], "callId":"cf-pauseprogram"});
//			websocket.send(JSON.stringify(call));
//		}
//		
//		/**
//		 * Pause a program
//		 */
//		this.isProgramActive = function (pgmId)
//		{
//			var call = eval({"method":"isProgramActive", "args":[{"type":"String", "value":pgmId}], "callId":"cf-ispgmactive"});
//			websocket.send(JSON.stringify(call));
//		}
		
	})(); //chmi main object definition end
	
	chmi.connect();
	
});//Require end

