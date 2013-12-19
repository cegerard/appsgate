define([], function () {
    var returnedModule = function () {
        var _name = 'web socket module';
        this.getName = function () {return _name;}
        
		var ws;
		var DEFAULT_SERVER_PORT = 8087;

		/** Open a web socket connexion to the AppsGate server*/
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
		
					var callId = false;
					var jsonMess = JSON.parse(received_msg);
		
					for (key in jsonMess) {
    					if (key == "callId") {
        					callId = true;
						}
					}

					if (callId) {
        				appsgateMain.returnCallHandler(jsonMess.callId, jsonMess);
        			}else{
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