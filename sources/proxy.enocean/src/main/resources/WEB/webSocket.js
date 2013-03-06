var ws;

var pairingMode;

/** Check */
function WebSocketOpen()
{
  if ("WebSocket" in window)
  {
     // Open a web socket
     var server = document.location.toString().split("/");
     ws = new WebSocket("ws://"+server[2]+"/");
     ws.onopen = function()
     {
        // Web Socket is connected, get all existing devices
        ws.send("{\"getConfDevices\":{}}");
        pairingMode = 0;
     };
     
     ws.onmessage = function (evt) 
     { 
		var received_msg = evt.data;
		
       	var msg_type;
        var jsonCMD = JSON.parse(received_msg);
        for(var key in jsonCMD){
    		msg_type = key;
	 	}
       	
       	if (msg_type == "newObject"){
       		var obj = jsonCMD.newObject;
       		removedUndefinedFromList(obj.id);
       		
       	} else if (msg_type == "newUndefinedSensor"){
       		addUndefinedToList(jsonCMD.newUndefinedSensor);
       		
       	} else if(msg_type == "pairingModeChanged") {
       		var pairingState = jsonCMD.pairingModeChanged;
       		var state = pairingState.pairingMode;
       		var currentStatus = document.getElementById("pairing-status");
       		if(state){
       			currentStatus.innerHTML = "ON";
       			currentStatus.style.backgroundColor="GREEN";
       		}else{
       			currentStatus.innerHTML = "OFF";
       			currentStatus.style.backgroundColor="RED";
       		}
       	} 	
     };
     
     ws.onerror = function (evt)
     { 
        // websocket error.
        alert("error ! -->"+evt.data); 
     };
     ws.onclose = function()
     { 
        // websocket is closed.
        //var device_list = document.getElementById("DevicesList");
        //device_list.length = 0;
        //document.getElementById("device-uid").innerHTML = "";
       	//document.getElementById("device-type").innerHTML = "";
       	
       	//var device_details = document.getElementById("details");
		//while (device_details.firstChild) {
  		//	device_details.removeChild(device_details.firstChild);
		//}
        
         alert("Connection is closed..."); 
     };
  }
  else
  {
     // The browser doesn't support WebSocket
     alert("WebSocket NOT supported by your Browser!");
  }
}

/** Check */
function WebSocketClose()
{
	ws.close();
}

/** Check */
function addUndefinedToList(jsondevice) {
	var sensor_list = document.getElementById("sensor-list");
	var new_sensor = document.createElement('OPTION');
	new_sensor.value = jsondevice.id;
	new_sensor.text = jsondevice.id;
    sensor_list.add(new_sensor, null);
    
    deviceToProfiles.setItem(jsondevice.id, jsondevice.capabilities);
}

/** Check */
function removedUndefinedFromList(id) {
	var sensor_list = document.getElementById("sensor-list");
	var cpt = 0;
	var end = 0;
	while(cpt < sensor_list.length && end == 0){ 
		if(sensor_list.options[cpt].value == id){
			end=1;
			sensor_list.remove(cpt);
			var profile_list = document.getElementById("profile-list");
			profile_list.length = 0;
			var profileZone = document.getElementById("selected-profile");
			profileZone.innerHTML = "";
		}
		cpt++;
	}
}

/** Check */
function getSensorInfos() {
	var sensor_list = document.getElementById("sensor-list");
	var sensor_id = sensor_list.options[sensor_list.selectedIndex].value;
	
	var profilesJSONArray = deviceToProfiles.getItem(sensor_id);
	var length = profilesJSONArray.length

	var profile_list = document.getElementById("profile-list");
	profile_list.length = 0;
	var profileZone = document.getElementById("selected-profile");
	profileZone.innerHTML = "";
	
	var cpt = 0;
	while(cpt < length){
	   addProfileToList(profilesJSONArray[cpt]);
	   cpt++;
	}
	
	selectNewProfile();
}

/** Check */
function addProfileToList(profile) {
	var profile_list = document.getElementById("profile-list");
	var new_profile = document.createElement('OPTION');
	
	new_profile.value = profile.profile;
	new_profile.text = profile.type;
	profile_list.add(new_profile, null);
}

/** Check */
function selectNewProfile() {
	var profile_list = document.getElementById("profile-list");
	var profile_prf = profile_list.options[profile_list.selectedIndex].value;
	
	var profileZone = document.getElementById("selected-profile");
	profileZone.innerHTML = profile_prf;
}

/** Check */
function validConf(){
	var sensor_list = document.getElementById("sensor-list");
	var sensor_id = sensor_list.options[sensor_list.selectedIndex].value;
	var profile_list = document.getElementById("profile-list");
	var profile_prf = profile_list.options[profile_list.selectedIndex].value;
    
    ws.send("{\"sensorValidation\":{\"id\":\""+sensor_id+"\", \"nbchoice\":\""+1+"\" ,\"capabilities\":[\""+profile_prf+"\"]}}");
}

/** Check */
function setPairingMode() {
    if(pairingMode == 1){
        ws.send("{\"setPairingMode\":{\"pairingMode\":\""+false+"\"}}");
        pairingMode = 0;
    }else{
        ws.send("{\"setPairingMode\":{\"pairingMode\":\""+true+"\"}}");
        pairingMode = 1;
    }
}

/** Check */
function HashTable(obj)
{
    this.length = 0;
    this.items = {};
    for (var p in obj) {
        if (obj.hasOwnProperty(p)) {
            this.items[p] = obj[p];
            this.length++;
        }
    }

    this.setItem = function(key, value)
    {
        var previous = undefined;
        if (this.hasItem(key)) {
            previous = this.items[key];
        }
        else {
            this.length++;
        }
        this.items[key] = value;
        return previous;
    }

    this.getItem = function(key) {
        return this.hasItem(key) ? this.items[key] : undefined;
    }

    this.hasItem = function(key)
    {
        return this.items.hasOwnProperty(key);
    }
   
    this.removeItem = function(key)
    {
        if (this.hasItem(key)) {
            previous = this.items[key];
            this.length--;
            delete this.items[key];
            return previous;
        }
        else {
            return undefined;
        }
    }

    this.keys = function()
    {
        var keys = [];
        for (var k in this.items) {
            if (this.hasItem(k)) {
                keys.push(k);
            }
        }
        return keys;
    }

    this.values = function()
    {
        var values = [];
        for (var k in this.items) {
            if (this.hasItem(k)) {
                values.push(this.items[k]);
            }
        }
        return values;
    }

    this.each = function(fn) {
        for (var k in this.items) {
            if (this.hasItem(k)) {
                fn(k, this.items[k]);
            }
        }
    }

    this.clear = function()
    {
        this.items = {}
        this.length = 0;
    }
}

var deviceToProfiles = new HashTable();
