define([], function () {
	var returnedModule = function () {
	
		var _name = 'Time line debugger';
		this.getName = function () {return _name;}
        
        /** Start the debugger  */
		this.startDebugger = function () {
		    chmi.sendJSONCmd(eval({"method":"startDebugger", "args":[], "callId":"startDB", "TARGET":"EHMI"}))
		}
        
        /** Stop the debugger */
		this.stopDebugger = function () {
		    chmi.sendJSONCmd(eval({"method":"stopDebugger", "args":[], "callId":"stopDB", "TARGET":"EHMI"}))
		}
        
        /** Return messgae handler for trace manager */
        this.messageHandler = function messageHandler(message) {
			
            console.log("traceMan CHMI GUI: "+message);
            var callId = message.callId;
            
            if (callId == "startDB"){
                var port = parseInt(message.value);
                if(port > 0 ){
                    $( "#traceMan-port" ).html("traceMan port: "+port);
                    $( "#traceMan-state" ).html("state: STARTED");
                    $( "#traceMan-status" ).removeClass("bg-color-red").addClass("bg-color-green");
                    $( "#traceMan-status" ).attr("onclick", "javascript:chmi.getWebSocket().getDebbuger().stopDebugger();");
                    $( "#traceMan-ico" ).removeClass("icon-stop").addClass("icon-play");
                    $( "#traceMan-label" ).html("Started");
                }
            } else if (callId == "stopDB") {
                 if( String(message.value) == "true"){
                    $( "#traceMan-port" ).html("traceMan port: ");
                    $( "#traceMan-state" ).html("state: STOPPED");
                    $( "#traceMan-status" ).removeClass("bg-color-green").addClass("bg-color-red");
                    $( "#traceMan-status" ).attr("onclick", "javascript:chmi.getWebSocket().getDebbuger().startDebugger();");
                    $( "#traceMan-ico" ).removeClass("icon-play").addClass("icon-stop");
                    $( "#traceMan-label" ).html("Stopped");
                 }
            }
            
            
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
        
    };
	return returnedModule;
});
       