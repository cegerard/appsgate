var ws;
var DEFAULT_SERVER_PORT = 8087;

/** Open a web socket connexion to the AppsGate server*/
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
function WebSocketClose()
{
	ws.close();
}


