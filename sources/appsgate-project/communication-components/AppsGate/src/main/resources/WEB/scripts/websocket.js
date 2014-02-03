define(['philipshue/philipshue', 'enocean/enocean'], function (hueRef, enoceanRef) {
//define begin
    var returnedModule = function () {
    
        var _name = 'web socket module';
        this.getName = function () {return _name;}
        
        var philipshue = new hueRef();
        this.getHue = function () {return philipshue;}
        
        var enocean = new enoceanRef();
        this.getEnocean = function () {return enocean;}
        
		var ws;
		var DEFAULT_SERVER_PORT = 8087;

		/** Open a web socket connection to the AppsGate server*/
		this.WebSocketOpen = function WebSocketOpen() 
		{
  			if ("WebSocket" in window)
  			{
     			// Open a web socket
     			var server = document.location.toString().split("/");
     			server = server[2].split(":");
     			ws = new WebSocket("ws://"+server[0]+":"+DEFAULT_SERVER_PORT+"/");
     			ws.onopen = function()
     			{ 
     				ws.send("{\"method\":\"getDevices\", \"args\":[], \"callId\":\"cf-get-devices\"}");
     			};
     
     			ws.onmessage = function (evt) 
     			{ 
					var received_msg = evt.data;
					console.log(received_msg);
		
					//var callId = false;
					var jsonMess = JSON.parse(received_msg);
					
					if(jsonMess.hasOwnProperty("TARGET")){//Call the target handler
						
						if(jsonMess.TARGET == "PHILIPSHUE"){
							philipshue.messageHandler(jsonMess);
						}else if(jsonMess.TARGET == "ENOCEAN"){
							enocean.messageHandler(jsonMess);
						}
						
					}else if(jsonMess.hasOwnProperty("callId")){
						callId = jsonMess.callId;
						if(callId.indexOf("enocean-conf-target") != -1){
							enocean.messageHandler(jsonMess);
						}else if(callId.indexOf("philipshue-conf-target") != -1) {
							philipshue.messageHandler(jsonMess);
						}else {
							appsgateMain.returnCallHandler(jsonMess.callId, jsonMess);
						}
					}else { //It is a notification
						appsgateMain.notificationHandler(jsonMess);
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
        			alert("Connection is closed..."); 
     			};
 			}
  			else
  			{
     			// The browser doesn't support WebSocket
     			alert("WebSocket NOT supported by your Browser!");
  			}
		}

		/** Close the current web socket connexion */
		this.WebSocketClose = function ()
		{
			ws.close();
		}
		
		/** Send the msg message through the opened connexion */
		this.send = function (msg)
		{
			ws.send(msg);
		}
	};
    return returnedModule;
});