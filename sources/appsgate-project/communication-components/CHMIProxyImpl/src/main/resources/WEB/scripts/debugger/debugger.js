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
			
            console.log("traceMan CHMI GUI: "+eval(message));
            var callId = message.callId;
            
            if (callId == "startDB"){
                var port = parseInt(message.value);
                if(port > 0 ){
                    $( "#traceMan-port" ).html("traceMan port: "+port);
                    $( "#traceMan-state" ).html("state: STARTED");
                    $( "#traceMan-status" ).removeClass("bg-color-green").addClass("bg-color-red");
                    $( "#traceMan-status" ).attr("onclick", "javascript:chmi.getWebSocket().getDebbuger().stopDebugger();");
                    $( "#traceMan-ico" ).removeClass("icon-play").addClass("icon-stop");
                    $( "#traceMan-label" ).html("Stop");
                }
            } else if (callId == "stopDB") {
                 if( String(message.value) == "true"){
                    $( "#traceMan-port" ).html("traceMan port: ");
                    $( "#traceMan-state" ).html("state: STOPPED");
                    $( "#traceMan-status" ).removeClass("bg-color-red").addClass("bg-color-green");
                    $( "#traceMan-status" ).attr("onclick", "javascript:chmi.getWebSocket().getDebbuger().startDebugger();");
                    $( "#traceMan-ico" ).removeClass("icon-stop").addClass("icon-play");
                    $( "#traceMan-label" ).html("Start");
                 }
            } else if (callId == "getTraceManConf") {
                var value = JSON.parse(message.value);
                 $( "#traceMan-port" ).html("traceMan port: "+value.port);
                if(value.state){
                    $( "#traceMan-state" ).html("state: STARTED");
                    $( "#traceMan-status" ).removeClass("bg-color-green").addClass("bg-color-red");
                    $( "#traceMan-status" ).attr("onclick", "javascript:chmi.getWebSocket().getDebbuger().stopDebugger();");
                    $( "#traceMan-ico" ).removeClass("icon-play").addClass("icon-stop");
                    $( "#traceMan-label" ).html("Stop"); 
                }else{
                    $( "#traceMan-state" ).html("state: STOPPED");
                    $( "#traceMan-status" ).removeClass("bg-color-red").addClass("bg-color-green");
                    $( "#traceMan-status" ).attr("onclick", "javascript:chmi.getWebSocket().getDebbuger().startDebugger();");
                    $( "#traceMan-ico" ).removeClass("icon-stop").addClass("icon-play");
                    $( "#traceMan-label" ).html("Start");  
                }
                $( "#traceMan-mode" ).html("mode: "+value.mode);
            }
		}
        
    };
	return returnedModule;
});
       