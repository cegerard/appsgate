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
				$("#undefinedDeviceTile-"+message.newObject.id).remove();
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
	       		this.addUndefinedTile(message.newUndefinedSensor);
	       		
	       	} else if( message.hasOwnProperty("confDevices")) {
	       		var confDeviceJSON = message.confDevices
	       		_pairingMode = confDeviceJSON.pairingMode;
	       		
	       		if(_pairingMode){
	       			$( "#pairingState-value" ).html("On");
	       			$( "#pairingState-tile" ).removeClass("bg-color-red").addClass("bg-color-green");
	       		}
	       		
	       		var enoceanDevList = confDeviceJSON.enoceanDevices;
	       		for(dev in enoceanDevList){
	       			call = eval({"method":"getDevice", "args":[{"type":"String", "value":enoceanDevList[dev]}], "callId":"enocean-conf-target-get-device", "TARGET":"CHMI"});
	       			chmi.sendCmd(JSON.stringify(call));
	       		}
	       	} else {
	       		
	       		if( message.callId == "enocean-conf-target-get-device") {
	       			var returnedCall = JSON.parse(message.value);
	       			this.addDeviceTile(returnedCall);
		       	}
	       	}
		}
		
		/** catch notification from EnOcean paired devices */
		this.notificationHandler = function notificationHandler(message) {
			if (message.hasOwnProperty("newDevice")){ //New EnOcean device notification
				this.addDeviceTile(message.newDevice);
				
			}else { //Device state update notification
				chmi.getWebSocket().getEnocean().updateTileStatus(message);
			}
		}

		
		/**  Add a tile in configure GUI for the device in parameter */
		this.addDeviceTile = function addDeviceTile(deviceData) {
   			$( "#no-devices-detected-tag" ).remove();

   			$.ajax({
   				url : './html/enocean/enoceanDeviceTile.html',
   				dataType : 'html',
   				success : function(html_code, status){
   					$(html_code).appendTo("#devices-tile-list"); 
   				},
   				error : function(res, status, error){},
   				complete : function(res, status){
   					
   					$("#deviceTile").attr("id", $("#deviceTile").attr("id")+"-"+deviceData.id);
   					
   					//Put data into the tile
	       			$( "#enocean-id" ).html(deviceData.id);
	       			$( "#device-name" ).html(deviceData.name);
	       			if(deviceData.status == "2") {
	       				$( "#device-status" ).html("<span class=\"label-success\">Connected</span>");
	       			} else {
	       				$( "#device-status" ).html("<span class=\"label-warning\">Warning</span>");
	       			}
	       			
	       			switch(deviceData.type) {
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
	       		
	       			$( "#enocean-id" ).append("<span class=\"icon icon-progress-3\" id=\"icon-signal-"+
	       					deviceData.id+"\" style=\"margin-left:45px\"></span>");
	       			
	       			$("#enocean-id").attr("id", $("#enocean-id").attr("id")+"-"+deviceData.id);
	       			$("#device-name").attr("id", $("#device-name").attr("id")+"-"+deviceData.id);
	       			$("#device-status").attr("id", $("#device-status").attr("id")+"-"+deviceData.id);
	       			$("#device-type").attr("id", $("#device-type").attr("id")+"-"+deviceData.id);
	       			$("#device-value").attr("id", $("#device-value").attr("id")+"-"+deviceData.id);
	       			
	       			chmi.getWebSocket().getEnocean().setTileStatus(deviceData);
	       			
	       			chmi.addNotifHandler(deviceData.id, chmi.getWebSocket().getEnocean().notificationHandler);

				var nbDevices = $("#devices-tile-list").children().length;
				var q = (nbDevices/4>>0);
				var r = nbDevices % 4;
				if(q>0 && r==1){
					q++;
					var newclass = "tile-column-span-"+(2+q*2);
					$("#devices-tile-list").attr("class", newclass);
				}

   			}
   			});
		}
		
		/** Add an undefined tile to check the device and paired it correctly */
		this.addUndefinedTile = function (deviceData) {
			$( "#no-undefined-devices-detected-tag" ).remove();
			
			$.ajax({
   				url : './html/enocean/undefinedDeviceTile.html',
   				dataType : 'html',
   				success : function(html_code, status){
   					$(html_code).appendTo("#undefined-devices-tile-list"); 
   				},
   				error : function(res, status, error){},
   				complete : function(res, status){
   					
   					$("#undefinedDeviceTile").attr("id", $("#undefinedDeviceTile").attr("id")+"-"+deviceData.id);
   					
   					$( "#undefined-id" ).html(deviceData.id);
   					
   					//File the combo box
   					$( "#device-profiles" ).append('<select id="profile-list-'+deviceData.id+'" name="selectProfile" size="1" style="width: 160px;"></select>');
	       			var capabilities = deviceData.capabilities;
	       			
	       			for(cap in capabilities) {
	       				$('#profile-list-'+deviceData.id).append(new Option( capabilities[cap].type, capabilities[cap].profile, true, true));
	       			}
   	
   					$( "#undefined-id" ).append('<span class="icon icon-progress-3" id="icon-signal-'+deviceData.id+'" style="margin-left:45px"></span>');
   					
   					$("#validate-profile-button").attr("onclick", "chmi.getWebSocket().getEnocean().validateProfile('"+deviceData.id+"')");
   					
   					$("#undefined-id").attr("id", $("#undefined-id").attr("id")+"-"+deviceData.id);
	       			$("#device-profiles").attr("id", $("#device-profiles").attr("id")+"-"+deviceData.id);
	       			$("#validate-profile").attr("id", $("#validate-profile").attr("id")+"-"+deviceData.id);
	       			$("#validate-profile-button").attr("id", $("#validate-profile-button").attr("id")+"-"+deviceData.id);

				var nbDevices = $("#undefined-devices-tile-list").children().length;
				var q = (nbDevices/4>>0);
				var r = nbDevices % 4;
				if(q>0 && r==1){
					q++;
					var newclass = "tile-column-span-"+(2+q*2);
					$("#undefined-devices-tile-list").attr("class", newclass);
				}
   				}
   			});

		}
		
		/** send the paring mode on request */
		this.pairingON = function () {
			var call = eval({"CONFIGURATION":"setPairingMode", "setPairingMode":{"pairingMode":"true"}, "TARGET":"ENOCEAN"});
			chmi.sendCmd(JSON.stringify(call));
		}
		
		/** send the paring mode off request */
		this.pairingOFF = function () {
			var call = eval({"CONFIGURATION":"setPairingMode", "setPairingMode":{"pairingMode":"false"}, "TARGET":"ENOCEAN"});
			chmi.sendCmd(JSON.stringify(call));
		}
		
		/** Toggle the current pairing mode */
		this.togglePairingMode = function () {
		    if(_pairingMode){
		    	this.pairingOFF();
		    }else{
		    	this.pairingON();
		    }
		}

		/** Get the selected profile and trigger the validation command  */
		this.validateProfile = function (id) {
			var profile_prf = $("#profile-list-"+id+" option:selected");
			call = eval({"sensorValidation":{"id":id, "nbchoice":"1", "capabilities":[profile_prf[0].value]}, "CONFIGURATION":"sensorValidation", "TARGET":"ENOCEAN"});
			chmi.sendJSONCmd(call);
		}
		
		/** Set the tile of the device with the current status */
		this.setTileStatus = function (notif) {
			var valueField = $("#device-value-"+notif.id);
			switch(notif.type) {
				case "3": //Contact Sensor
					var contactState;
					if(notif.contact=="true") {
						contactState = "close";
					} else {
						contactState = "open";
					} 
					valueField.html('<span id="contact-'+notif.id+'" class="label-info">'+contactState+'</span>');
					break;
					
				case "4": //KeyCard sensor
					var insertedState;
					if(notif.inserted=="true") {
						insertedState = "inserted";
					} else {
						insertedState = "removed";
					} 
					valueField.html('<span id="keycard-'+notif.id+'" class="label-info">'+insertedState+'</span>');
					break;
					
				case "1": //Illumination sensor
					valueField.html('<span id="illumination-'+notif.id+'" class="label-info">'+notif.value+' Lux</span>');
					break;
					
				case "8": //ON/Off actuator
					if(notif.isOn=="true") {
						valueField.html('<span id="onoff-'+notif.id+'" class="label-success">On</span>');
					} else {
						valueField.html('<span id="onoff-'+notif.id+'" class="label-important">Off</span>');
					} 
					break;
					
				case "6": //Plug sensor/actuator
					if(notif.plugState == "true") {
						valueField.html('<span id="smartplug-state-'+notif.id+'" class="label-success">On -</span><span id="smartplug-conso-'+notif.id+'" class="label-info">- '+notif.consumption+' W</span>');
					}else{
						valueField.html('<span id="smartplug-state-'+notif.id+'" class="label-important">Off -</span><span id="smartplug-conso-'+notif.id+'" class="label-info">- '+notif.consumption+' W</span>');
					}
					
					var plugConsField = $("#smartplug-cons-"+notif.id);
					console.log(plugConsField);
					var consumption = parseFloat(notif.consumption);
					console.log(consumption);
					if(0.0 <= consumption && consumption < 21.0) {
						plugConsField.removeClass();
						plugConsField.addClass("label-success");
					}else if(21.0 <= consumption && consumption < 51.0) {
						//Nothing that's the default value
					}else if(51.0 <= consumption && consumption < 81.0) {
						plugConsField.removeClass();
						plugConsField.addClass("label-warning");
					}else if(81.0 <= consumption && consumption < 200.1) {
						plugConsField.removeClass();
						plugConsField.addClass("label-important");
					}else if(200.1 <= consumption) {
						plugConsField.removeClass();
						plugConsField.addClass("label-inverse");
					} else {
						plugConsField.removeClass();
						plugConsField.addClass("label");
					}
					
					break;
					
				case "2": //Switch sensor
					valueField.html('<span id="switch-0-'+notif.id+'" class="label-default">0: </span> <span id="switchStatus-0-'+notif.id+'"></span> / ');
					valueField.append('<span id="switch-1-'+notif.id+'" class="label-default">1: </span><span id="switchStatus-1-'+notif.id+'"></span>');
					
					var switch0Status = $("#switchStatus-0-"+notif.id);
					var switch1Status = $("#switchStatus-1-"+notif.id);
					var switchNumber;
						
					switch0Status.html('<span class="label-info">released</span>');
					switch1Status.html('<span class="label-info">released</span>');
					
					if(notif.switchNumber == "0") {
						switchNumber = $("#switch-0-"+notif.id);
						valueField = switch0Status;
					}else if (notif.switchNumber == "1") {
						switchNumber = $("#switch-1-"+notif.id);
						valueField = switch1Status;
					}
					
					if(switchNumber != null) {
						switchNumber.removeClass();
						switchNumber.addClass("label-success");
					
						if(notif.buttonStatus == 0) {
							valueField.html('<span class="label-inverse">Off</span>');
						}else if(notif.buttonStatus == 1) {
							valueField.html('<span class="label-success">On</span>');
						}else {
							valueField.html('<span class="label-info">released</span>');
						}
					}
					break;
					
				case "0": // Temperature sensor
					valueField.html('<span id="temperature-'+notif.id+'" class="label-info">'+notif.value.substring(0,4)+' °C</span>');
					break;
				
				default: //undefined sensor
					valueField.html('<span id="undefined-'+notif.id+'" class="label-warning">undefined</span>');
					break;
			}
		}
		
		/** Update the tile with the new state */
		this.updateTileStatus = function (notif) {
			var deviceType = $("#device-type-"+notif.objectId).html();
			var valueField = $("#device-value-"+notif.objectId);

			switch(deviceType) {
			
				case "Contact": //Contact Sensor
					if(notif.varName == "contact") {
						var contactState;
						if(notif.value=="true") {
							contactState = "close";
						} else {
							contactState = "open";
						} 
						valueField.html('<span id="contact-'+notif.objectId+'" class="label-info">'+contactState+'</span>');
					}
					break;
				case "Key card switch": //KeyCard sensor
					if(notif.varName == "inserted") {
						var insertedState;
						if(notif.value=="true") {
							insertedState = "inserted";
						} else {
							insertedState = "removed";
						} 
						valueField.html('<span id="keycard-'+notif.objectId+'" class="label-info">'+insertedState+'</span>');
					}
					break;
				
				case "illumination": //Illumination sensor
					if(notif.varName == "value") {
						valueField.html('<span id="illumination-'+notif.objectId+'" class="label-info">'+notif.value+' Lux</span>');
					}
					break;
				
				case "On/Off actuator": //ON/Off actuator
					if(notif.varName == "value") {
						if(notif.value =="true") {
							valueField.html('<span id="onoff-'+notif.objectId+'" class="label-success">On</span>');
						} else {
							valueField.html('<span id="onoff-'+notif.objectId+'" class="label-important">Off</span>');
						}
					}
					break;
				
				case "Smart plug": //Plug sensor/actuator
					if(notif.varName == "plugState") {
						var plugStateField = $("#smartplug-state-"+notif.objectId);
						if(notif.value == "true") {
							plugStateField.html("On -");
							plugStateField.removeClass();
							plugStateField.addClass("label-success");
						}else{
							plugStateField.html("Off -");
							plugStateField.removeClass();
							plugStateField.addClass("label-important");	
						}
						
					}else if(notif.varName == "consumption") {
						var plugConsField = $("#smartplug-conso-"+notif.objectId);
						plugConsField.html("- "+notif.value+" W");
						
						var consumption = parseFloat(notif.value);
						if(0.0 <= consumption && consumption < 21.0) {
							plugConsField.removeClass();
							plugConsField.addClass("label-success");
						}else if(21.0 <= consumption && consumption < 51.0) {
							plugConsField.removeClass();
							plugConsField.addClass("label-info");
						}else if(51.0 <= consumption && consumption < 81.0) {
							plugConsField.removeClass();
							plugConsField.addClass("label-warning");
						}else if(81.0 <= consumption && consumption < 200.1) {
							plugConsField.removeClass();
							plugConsField.addClass("label-important");
						}else if(200.1 <= consumption) {
							plugConsField.removeClass();
							plugConsField.addClass("label-inverse");
						}else {
							plugConsField.removeClass();
							plugConsField.addClass("label");
						}
					}

					break;
				
				case "Switch": //Switch sensor

					var switchNumber0 = $("#switch-0-"+notif.objectId);
					var switchNumber1 = $("#switch-1-"+notif.objectId);
					
					if(notif.varName == "switchNumber") {
						
						switchNumber0.removeClass();
						switchNumber1.removeClass();
						
						if(notif.value == "0") {
							switchNumber0.addClass("label-success");
							switchNumber1.addClass("label-default");
						}else {
							switchNumber0.addClass("label-default");
							switchNumber1.addClass("label-success");
						}
					}
					
					if(notif.varName == "buttonStatus") {
						var switchStatus;
						
						if(switchNumber0.hasClass("label-success")) {
							switchStatus = $("#switchStatus-0-"+notif.objectId);
						} else {
							switchStatus = $("#switchStatus-1-"+notif.objectId);
						}
						
						if(notif.value == "false") {
							switchStatus.html('<span class="label-inverse">Off</span>');
						}else if(notif.value == "true") {
							switchStatus.html('<span class="label-success">On</span>');
						}else {
							switchStatus.html('<span class="label-info">released</span>');
						}
					}
					
				case "Temperature": // Temperature sensor
					if(notif.varName == "value") {
						valueField.html('<span id="temperature-'+notif.objectId+'" class="label-info">'+notif.value.substring(0,4)+' °C</span>');
					}
					break;
			
				default: //undefined sensor
					console.log("no device found");
					break;
			}
		}
		
	};
	return returnedModule;
});
