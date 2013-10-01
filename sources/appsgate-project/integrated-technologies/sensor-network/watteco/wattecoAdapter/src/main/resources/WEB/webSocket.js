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
	ws.send("{\"discover\":{}, \"CONFIGURATION\":\"discover\"}");
}
