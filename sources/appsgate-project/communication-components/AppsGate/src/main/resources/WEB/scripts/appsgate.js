var appsgateMain;
var appsgateConfGUIVersion = '0.4.1';

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

	appsgateMain = new (function () {
	
		console.log('AppsGate configuration GUI version:', appsgateConfGUIVersion); 
		console.log('jQuery version:', $.fn.jquery); // 2.1.0
		
		var websocket = new websocketRef();
        var clockModule = new clockModuleRef();
        var handlerMap = {};
	
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
					alink.setAttribute("onclick", "javascript:appsgateMain.goBackToPreviousNavSubMenu("+pos+");"+goBackHandler);
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
		
		/**
 		 * Get a device
 		 */
		this.getDevice = function (deviceId)
		{
			var call = eval({"method":"getDevice", "args":[{"type":"String", "value":deviceId}], "callId":"cf-getdevice"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get a device
 		 */
		this.getDeviceByType = function (type)
		{
			var call = eval({"method":"getDevices", "args":[{"type":"String", "value":type}], "callId":"cf-getdevicesbytype"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get devices of specifics types in
 		 * specifoed spaces
 		 */
		this.getDevicesInSpaces = function (typeList, spaces)
		{
			var call = eval({"method":"getDevicesInSpaces", "args":[{"type":"JSONArray", "value":typeList}, {"type":"JSONArray", "value":spaces}], "callId":"cf-getdevicesinspaces"});
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
 		 * Get all places for an habitat
 		 */
		this.getPlaces = function (habitatId)
		{
			var call = eval({"method":"getPlaces", "args":[{"type":"String", "value":habitatId}], "callId":"cf-getplaces"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get place detail
 		 */
		this.getPlace = function (habitatId, placeId)
		{
			var call = eval({"method":"getPlaceInfo", "args":[{"type":"String", "value":habitatId}, {"type":"String", "value":placeId}], "callId":"cf-getplace"});
			websocket.send(JSON.stringify(call));
		}
		
		/********
		 Space management
		 	     ********/
		
		/**
		 * Get json spaces
		 */
		this.getJSONSpaces = function ()
		{
			var call = eval({"method":"getJSONSpaces", "args":[], "callId":"cf-getspaces"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Get space description
		 */
		this.getSpaceInfo = function (spaceId)
		{
			var call = eval({"method":"getSpaceInfo", "args":[{"type":"String", "value":spaceId}], "callId":"cf-getspace"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Get json spaces as a tree
		 */
		this.getJSONTreeSpaces = function ()
		{
			var call = eval({"method":"getTreeDescription", "args":[], "callId":"cf-gettreespaces"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Get json spaces as a tree
		 */
		this.getJSONSubTreeSpaces = function (nodeId)
		{
			var call = eval({"method":"getTreeDescription", "args":[{"type":"String", "value":nodeId}], "callId":"cf-getsubtreespaces"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get space by name
 		 */
		this.getSpacesByName = function (name)
		{
			var call = eval({"method":"getSpacesByName", "args":[{"type":"String", "value":name}], "callId":"cf-getspacesbyname"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get space by tags
 		 */
		this.getSpacesByTags = function (tagsArray)
		{
			var call = eval({"method":"getSpacesWithTags", "args":[{"type":"JSONArray", "value":JSON.stringify(tagsArray)}], "callId":"cf-getspacesbytag"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get space by property keys
 		 */
		this.getSpacesByPropertyKey = function (keysArray)
		{
			var call = eval({"method":"getSpacesWithProperties", "args":[{"type":"JSONArray", "value":JSON.stringify(keysArray)}], "callId":"cf-getspacesbyprop"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get space by property keys and associated value
 		 */
		this.getSpacesWithPropertiesValue = function (propArray)
		{
			var call = eval({"method":"getSpacesWithPropertiesValue", "args":[{"type":"JSONArray", "value":JSON.stringify(propArray)}], "callId":"cf-getspacesbypropvalue"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Get root pace
 		 */
		this.getRootSpace = function ()
		{
			var call = eval({"method":"getRootSpace", "args":[], "callId":"cf-getrootspace"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Add new space
 		 */
		this.newSpace = function (type, parent)
		{
			var place = eval({"type":category, "parent":parent });
			var call = eval({"method":"newSpace", "args":[{"type":"JSONObject", "value":JSON.stringify(place)}], "callId":"cf-newspace"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Update an existing space
		 */
		this.updateSpace = function (data)
		{
			var call = eval({"method":"updateSpace", "args":[{"type":"JSONObject", "value":JSON.stringify(data)}], "callId":"cf-updatespace"});
			websocket.send(JSON.stringify(call));
		}

		/**
 		 * Move an existing space
 		 */
		this.moveSpace = function (id, parent)
		{
			var place = eval({"id":id, "parent":parent});
			var call = eval({"method":"updateSpace", "args":[{"type":"JSONObject", "value":JSON.stringify(place)}], "callId":"cf-movespace"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Set up completly an existing space
		 * id, the id of the space
		 * name, the new name
		 * category, the category of this space
		 * parent, the new parent of this space
		 * tagList, the new tagList of an empty JSONArray to clear the existing list
		 * propList, the new propList of an empty JSONArray to clear the existing list
		 * objectList, the new objectList of an empty JSONArray to clear the existing list
		 **/
		this.setSpace =  function (id, name, parent, tagList, propList, objectList) {
			var place = eval({"id":id, "name":name, "parent":parent, "taglist":tagList, "proplist":propList, "coreObjectlist":objectList});
			var call = eval({"method":"updateSpace", "args":[{"type":"JSONObject", "value":JSON.stringify(place)}], "callId":"cf-setSpace"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
		 * Free the space from its tags, porperties and object list
		 */
		this.clearSpace = function(id) {
			var place = eval({"id":id, "taglist":[], "proplist":[], "coreObjectlist":[]});
			var call = eval({"method":"updateSpace", "args":[{"type":"JSONObject", "value":JSON.stringify(place)}], "callId":"cf-clearSpaceList"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * remove a space
 		 */
		this.removeSpace = function (id)
		{
			var call = eval({"method":"removeSpace", "args":[{"type":"String", "value":id}], "callId":"cf-removespace"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Add a tag
 		 */
		this.addTag = function (id, tag)
		{
			var call = eval({"method":"addTag", "args":[{"type":"String", "value":id}, {"type":"String", "value":tag}], "callId":"cf-addTag"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Remove a tag
 		 */
		this.removeTag = function (id, tag)
		{
			var call = eval({"method":"removeTag", "args":[{"type":"String", "value":id}, {"type":"String", "value":tag}], "callId":"cf-removeTag"});
			websocket.send(JSON.stringify(call));
		}
		
		/**
 		 * Add a property
 		 */
		this.addProperty = function (id, key, value)
		{
			var call = eval({"method":"addProperty", "args":[{"type":"String", "value":id}, {"type":"String", "value":key}, {"type":"String", "value":value}], "callId":"cf-addProp"});
			websocket.send(JSON.stringify(call));
		}
	
		/**
		 * Remove a porperty
		 */
		this.removeProperty = function (id, key)
		{
			var call = eval({"method":"removeProperty", "args":[{"type":"String", "value":id}, {"type":"String", "value":key}], "callId":"cf-removeProp"});
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
		
//		/**
// 		 * Get user complete inforamtion
// 		 */
//		this.getUserFullDetails = function (login)
//		{
//			var call = eval({"method":"getUserFullDetails", "args":[{"type":"String", "value":login}], "callId":"cf-getUserfulldetails"});
//			websocket.send(JSON.stringify(call));
//		}
		
		/**
 		 * Check the unicity of a login
 		 */
		this.checkIfLoginIsFree = function (login)
		{
			var call = eval({"method":"checkIfLoginIsFree", "args":[{"type":"String", "value":login}], "callId":"cf-checkforfreelogin"});
			websocket.send(JSON.stringify(call));
		}
		
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

