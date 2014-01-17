var ws;

var pairingMode;
var DEFAULT_SERVER_PORT = 8087;

/** Check */
function WebSocketOpen()
{
  if ("WebSocket" in window)
  {
     // Open a web socket
	 var server = document.location.toString().split("/");
	 server = server[2].split(":");
	 ws = new WebSocket("ws://"+server[0]+":"+DEFAULT_SERVER_PORT+"/");
     ws.onopen = function()
     {};
     
     ws.onmessage = function (evt) 
     { 
		var received_msg = evt.data;
		console.log(received_msg);
     };
     
     ws.onerror = function (evt)
     { 
        // websocket error.
        alert("error ! -->"+evt.data); 
     };
     ws.onclose = function()
     { 
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
function WebSocketClose() {
	ws.close();
}

/** Check */
function discover() {
	ws.send("{\"discover\":{}, \"CONFIGURATION\":\"discover\", \"TARGET\":\"WATTECO\"}");
}
