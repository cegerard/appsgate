define([], function () {
	var returnedModule = function () {
	
		var _name = 'EnOcean configuration module';
		this.getName = function () {return _name;}
		
		var _pairingMode = false;
		
		/**
			EnOcean configuration message handler 
		*/
		this.messageHandler = function messageHandler(message) {
			if (message.hasOwnProperty("newObject")){
	       		var obj = jsonCMD.newObject;
	       		if (obj.type == "SENSOR"){
	       			removedUndefinedFromList(obj.id);
	       		}else if (obj.type == "ACTUATOR") {
	       			addActuatorToList(obj);
	       		}	
	       		
	       	} else if(message.hasOwnProperty("pairingModeChanged")) {
	       		var pairingState = message.pairingModeChanged;
	       		var state = pairingState.pairingMode;
	       		if(state){
	       			_pairingMode = true;
	       			$( "#pairingState-value" ).html("On");
	       			$( "#pairingState-tile" ).removeClass("bg-color-red").addClass("bg-color-green");
	       		}else{
	       			_pairingMode = false;
	       			$( "#pairingState-value" ).html("Off");
	       			$( "#pairingState-tile" ).removeClass("bg-color-green").addClass("bg-color-red");
	       		}
	       		
	       	} else if (message.hasOwnProperty("newUndefinedSensor")){
	       		addUndefinedToList(jsonCMD.newUndefinedSensor);
	       		
	       	} else if( message.hasOwnProperty("confDevices")) {
	       		var confDeviceJSON = message.confDevices
	       		_pairingMode = confDeviceJSON.pairingMode;
	       		
	       		if(_pairingMode){
	       			$( "#pairingState-value" ).html("On");
	       			$( "#pairingState-tile" ).removeClass("bg-color-red").addClass("bg-color-green");
	       		}
	       		
	       		var enoceanDevList = confDeviceJSON.enoceanDevices;
	       		for(dev in enoceanDevList){
	       			call = eval({"method":"getDevice", "args":[{"type":"String", "value":enoceanDevList[dev]}], "callId":"enocean-conf-target-get-device"});
	       			appsgateMain.sendCmd(JSON.stringify(call));
	       		}
	       	} else {
	       		
	       		if( message.callId == "enocean-conf-target-get-device") {
	       			var returnedCall = JSON.parse(message.value);
	       			
	       			//add device tile
	       			$( "#no-devices-detected-tag" ).remove();
	       			$.ajax({
	       				url : './html/enocean/enoceanDeviceTile.html',
	       				dataType : 'html',
	       				success : function(html_code, status){
	       					$(html_code).appendTo("#devices-tile-list"); 
	       				},
	       				error : function(res, status, error){},
	       				complete : function(res, status){
	       					
	       					$("#deviceTile").attr("id", $("#deviceTile").attr("id")+"-"+returnedCall.id);
	       					
	       					//Put data into the tile
	    	       			$( "#enocean-id" ).html(returnedCall.id);
	    	       			$( "#device-name" ).html(returnedCall.name);
	    	       			if(returnedCall.status == "2") {
	    	       				$( "#device-status" ).html("<span class=\"label-success\">Connected</span>");
	    	       			} else {
	    	       				$( "#device-status" ).html("<span class=\"label-warning\">Warning</span>");
	    	       			}
	    	       			
	    	       			switch(returnedCall.type) {
	    	       				case "3": //Contact Sensor
	    	       					$( "#device-type" ).html("Contact");
	    	       					break;
	    	       				case "4": //KeyCard sensor
	    	       					$( "#device-type" ).html("Key card switch");
	    	       					break;
	    	       				case "1": //Illumination sensor
	    	       					$( "#device-type" ).html("illumination");
	    	       					break;
	    	       				case "8": //ON/Off actuator
	    	       					$( "#device-type" ).html("On/Off actuator");
	    	       					break;
	    	       				case "6": //Plug sensor/actuator
	    	       					$( "#device-type" ).html("Smart plug");
	    	       					break;
	    	       				case "2": //Switch sensor
	    	       					$( "#device-type" ).html("Switch");
	    	       					break;
	    	       				case "0": // Temperature sensor
	    	       					$( "#device-type" ).html("Temperature");
	    	       					break;
	    	       				
	    	       				default: //undefined sensor
	    	       					$( "#device-type" ).html("Undefined");
	    	       					break;
	    	       			}
	    	       			
	    	       			$("#device-value").html("N.A.");
	    	       		
	    	       			$( "#enocean-id" ).append("<span class=\"icon icon-progress-3\" id=\"icon-signal-"+
	    	       					returnedCall.id+"\" style=\"margin-left:45px\"></span>");
	    	       			
	    	       			$("#device-id").attr("id", $("#device-id").attr("id")+"-"+returnedCall.id);
	    	       			$("#device-name").attr("id", $("#device-name").attr("id")+"-"+returnedCall.id);
	    	       			$("#device-status").attr("id", $("#device-status").attr("id")+"-"+returnedCall.id);
	    	       			$("#device-type").attr("id", $("#device-type").attr("id")+"-"+returnedCall.id);
	    	       			$("#device-value").attr("id", $("#device-value").attr("id")+"-"+returnedCall.id);
	       				}
	       			});
		       	}
	       	}
		}
		
		this.notificationHandler = function notificationHandler(message) {
			//TODO handle notification from sensors
		}

		
		/**  Add a tile in configure GUI for the device in parameter */
		this.addDeviceTile = function addDeviceTile(device, device_tile_list) {
			
		}
		
		/** Trigger the "type" command on the id object through AppsGate */
		this.triggerCmd = function triggerCmd(type, id, optBridgeIp){
			var call;
			
			if (type == "validate") {
				call = eval({"method":"setBrightness", "args":[{"type":"long", "value":bri.value}], "objectId":id, "callId":"HUE-cf-light"});
			}
			
			appsgateMain.sendJSONCmd(call);
		}
		
		/** send the paring mode on request */
		this.pairingON = function () {
			var call = eval({"CONFIGURATION":"setPairingMode", "setPairingMode":{"pairingMode":"true"}, "TARGET":"ENOCEAN"});
			appsgateMain.sendCmd(JSON.stringify(call));
		}
		
		/** send the paring mode off request */
		this.pairingOFF = function () {
			var call = eval({"CONFIGURATION":"setPairingMode", "setPairingMode":{"pairingMode":"false"}, "TARGET":"ENOCEAN"});
			appsgateMain.sendCmd(JSON.stringify(call));
		}
		
		/** Toggle the current pairing mode */
		this.togglePairingMode = function () {
		    if(_pairingMode){
		    	this.pairingOFF();
		    }else{
		    	this.pairingON();
		    }
		}
		
		this.addUndefinedToList = function(jsondevice) {
			var sensor_list = document.getElementById("sensor-list");
			var new_sensor = document.createElement('OPTION');
			new_sensor.value = jsondevice.id;
			new_sensor.text = jsondevice.id;
		    sensor_list.add(new_sensor, null);
		    
		    deviceToProfiles.setItem(jsondevice.id, jsondevice.capabilities);
		}
		
		this.addActuatorProfileToList = function (profileList) {
			var cpt = 0;
			var profile;
			var profile_list = document.getElementById("actuator-profile");
			var new_profile;
			
			while(cpt < profileList.length) { 
				profile = profileList[cpt];
				new_profile = document.createElement('OPTION');
				new_profile.value = profile;
				new_profile.text = profile;
				profile_list.add(new_profile, null);
				cpt++;
			}
		}
		
		this.addActuatorToList = function (obj) {
			var actuator_list = document.getElementById("actuator-list");
			var  nameInput = document.getElementById("actuatorName-input");
			
			var new_actuator = document.createElement('OPTION');
			new_actuator.value = obj.id;
			new_actuator.text = obj.name;

			var act = new device(obj.id, obj.name, obj.type, obj.deviceType, obj.paired);
			addToDeviceList(act);
			
			actuator_list.add(new_actuator, null);
			nameInput.value = "";
		}

		/**  */
		this.removedUndefinedFromList = function (id) {
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

		/**  */
		this.getSensorInfos = function () {
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

		/**  */
		this.addProfileToList = function (profile) {
			var profile_list = document.getElementById("profile-list");
			var new_profile = document.createElement('OPTION');
			
			new_profile.value = profile.profile;
			new_profile.text = profile.type;
			profile_list.add(new_profile, null);
		}

		/**  */
		this.selectNewProfile = function () {
			var profile_list = document.getElementById("profile-list");
			var profile_prf = profile_list.options[profile_list.selectedIndex].value;
			
			var profileZone = document.getElementById("selected-profile");
			profileZone.innerHTML = profile_prf;
		}

		/**  */
		this.validConf = function (){
			var sensor_list = document.getElementById("sensor-list");
			var sensor_id = sensor_list.options[sensor_list.selectedIndex].value;
			var profile_list = document.getElementById("profile-list");
			var profile_prf = profile_list.options[profile_list.selectedIndex].value;
		    
		    ws.send("{\"sensorValidation\":{\"id\":\""+sensor_id+"\", \"nbchoice\":\""+1+"\" ,\"capabilities\":[\""+profile_prf+"\"]}, \"CONFIGURATION\":\"sensorValidation\", \"TARGET\":\"ENOCEAN\"}");
		}

	};
	return returnedModule;
});