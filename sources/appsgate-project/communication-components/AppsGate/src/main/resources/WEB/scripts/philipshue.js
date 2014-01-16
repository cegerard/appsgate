define([], function () {
	var returnedModule = function () {
	
		var _name = 'Philips HUE configuration module';
		this.getName = function () {return _name;}
		
		/**
			Philips HUE configuration message handler 
		*/
		this.messageHandler = function messageHandler(message) {
			if (message.hasOwnProperty("hueConfDevices")) {
				var conf = message.hueConfDevices;
				var lightsArray = conf.lights;
				var bridgesArray = conf.bridges;
				
				for(bridge in bridgesArray) {
					this.addBridgeTile(bridgesArray[bridge]);
				}
				
				var lights_tile_list = document.getElementById("lights-tile-list");
				for(light in lightsArray) {
					this.addLightTile(lightsArray[light], lights_tile_list);
				}
				
			}else if(message.hasOwnProperty("bridgeInfo")){
				var bridgeInfo = message.bridgeInfo;
				
				if(bridgeInfo.hasOwnProperty("error")) {
					var element = document.getElementById("bridgenetinfoTile-header");
					element.innerHTML = bridgeInfo.ip;
					element = document.getElementById("bridgenetinfoTile-mac");
					element.innerHTML = bridgeInfo.mac;
					
					//Push notification to associated the bridge
					var httpRequest=new XMLHttpRequest();
					httpRequest.open("GET","./html/hueBridgeSyncAlert.html",false);
					httpRequest.send();
					element = document.getElementById("alerts-container");
					element.innerHTML = httpRequest.responseText;
					
				}else{
				
					var element = document.getElementById("bridgetopinfoTile-header");
					element.innerHTML = bridgeInfo.name;
					element = document.getElementById("bridgetopinfoTile-id");
					element.innerHTML = "id: "+bridgeInfo.hueident;
					element = document.getElementById("bridgetopinfoTile-version");
					element.innerHTML = "version: "+bridgeInfo.swversion;
				
					element = document.getElementById("bridgenetinfoTile-header");
					element.innerHTML = bridgeInfo.ip;
					element = document.getElementById("bridgenetinfoTile-mac");
					element.innerHTML = bridgeInfo.mac;
					element = document.getElementById("bridgenetinfoTile-gateway");
					element.innerHTML = "gateway: "+bridgeInfo.gateway;
					element = document.getElementById("bridgenetinfoTile-dhcp");
					element.innerHTML = "dhcp: "+bridgeInfo.dhcpenabled;
					element = document.getElementById("bridgenetinfoTile-netmask");
					element.innerHTML = "netmask: "+bridgeInfo.netmask;
				
					element = document.getElementById("bridgeproxyinfoTile-header");
					element.innerHTML = "proxy: "+bridgeInfo.proxyenabled;
					element = document.getElementById("bridgeproxyinfoTile-proxy");
					element.innerHTML = bridgeInfo.proxy;
					element = document.getElementById("bridgeproxyinfoTile-port");
					element.innerHTML = "with port: "+bridgeInfo.proxyport;
				
					element = document.getElementById("bridgetimeinfoTile-time");
					element.innerHTML = bridgeInfo.time;
					element = document.getElementById("bridgetimeinfoTile-local");
					element.innerHTML = bridgeInfo.localtime;
					element = document.getElementById("bridgetimeinfoTile-zone");
					element.innerHTML = bridgeInfo.timezone;
				}
			} else if (message.hasOwnProperty("bridgeLights")) {
				var lightsArray = message.bridgeLights;
				
				var lights_tile_list = document.getElementById("lights-tile-list");
				for(light in lightsArray) {
					this.addLightTile(lightsArray[light], lights_tile_list);
				}
			
			} else if (message.hasOwnProperty("hueToastAlert")) {
				var hueAlert = message.hueToastAlert;
				
				//Push notification to associated the bridge
				var httpRequest=new XMLHttpRequest();
				httpRequest.open("GET","./html/hueBridgeAlert.html",false);
				httpRequest.send();
				element = document.getElementById("alerts-container");
				element.innerHTML = httpRequest.responseText;
				element = document.getElementById("toast-alert-heading");
				element.innerHTML = hueAlert.header;
				element = document.getElementById("toast-alert-message");
				element.innerHTML = hueAlert.text;
				
			} else if (message.hasOwnProperty("bridgeConnected")) {
				var bridgeAlert = message.bridgeConnected;
				var hueDiv = document.getElementById("philips-hue-sections");
				var bridgediv = document.getElementById("philips-hue-bridge-sections");
				
				//Force div refresh
				if(hueDiv != null) {
					hueDiv = document.getElementById("display-panorama");
					var httpRequest=new XMLHttpRequest();
					httpRequest.open("GET","./html/philipshue.html",false);
					httpRequest.send();
					hueDiv.innerHTML = httpRequest.responseText;
					appsgateMain.sendCmd("{\"getHUEConfDevices\":{}, \"CONFIGURATION\":\"getHUEConfDevices\", \"TARGET\":\"PHILIPSHUE\"}");

					
				}else if(bridgediv != null) {
					//fill the tile with bridge information
					var call = eval({"CONFIGURATION":"getBridgeInfo", "getBridgeInfo":{"ip":bridgeAlert}, "TARGET":"PHILIPSHUE"});
					appsgateMain.sendCmd(JSON.stringify(call));
					
					//set the JavaScript method call to tile click action
					var actionTile = document.getElementById("pushlinkTile");
					actionTile.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().pushLinkSend(\""+bridge+"\");");
					actionTile.className = "tile square text bg-color-green";
					
					actionTile = document.getElementById("updatesoftwareTile");
					actionTile.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().updatefirmware(\""+bridge+"\");");
					actionTile.className = "tile square text bg-color-green";
					
					actionTile = document.getElementById("findlightserialTile");
					var serial = "";
					actionTile.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().findNewLightSerial(\""+bridge+"\", \""+serial+"\");");
					actionTile.className = "tile square text bg-color-green";
					
					actionTile = document.getElementById("findnewlightsTile");
					actionTile.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().findNewLights(\""+bridge+"\");");
					actionTile.className = "tile square text bg-color-green";
					
					infoTile = document.getElementById("bridgeproxyinfoTile");
					infoTile.className = "tile wide text bg-color-blue";
					
					infoTile = document.getElementById("bridgetimeinfoTile");
					infoTile.className = "tile wide text bg-color-blue";
					
					var alert = document.getElementById("alerts-container");
					alert.innerHTML = "";
				}
			}
			
		}
		
		/** Add a tile in configure GUI for the bridge in parameter */
		this.addBridgeTile = function addBridgeTile(bridge) {

			var httpRequest=new XMLHttpRequest();
			httpRequest.open("GET","./html/hueBridgeTile.html",false);
			httpRequest.send();
			
			var bridges_tile_list = document.getElementById("bridges-tile-list");
						
			var bridgeDiv = document.createElement('div');
			bridgeDiv.innerHTML = httpRequest.responseText;
			bridgeDiv.id = bridge.ip;
			
			var atag = bridgeDiv.childNodes[0];
			atag.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().goToBridgeDisplay(\""+bridge.ip+"\");")
			var subDiv = atag.childNodes;
			var nbDiv = subDiv.length;
			var currentDiv;
			
			for(var i = 0; i < nbDiv; i++){
				currentDiv = subDiv[i];
				currentDiv.id = currentDiv.id+"-"+bridge.ip;
				
				switch(i){
					case 1: //Header
						currentDiv.innerHTML = "Status: "+bridge.status;
						if(bridge.status == "not associated") {
							currentDiv.innerHTML = "WARNING";
							atag.className = "tile wide text bg-color-yellow";
						}
						break;
					case 3: //Other
						if(bridge.status == "not associated") {
							currentDiv.innerHTML = "not associated";
						}
						break;
					case 5: //IP
						currentDiv.innerHTML = "IP: "+bridge.ip;
						break;
					case 7: //MAC
						currentDiv.innerHTML = "MAC: "+bridge.MAC;
						break;
					case 9: //nb-Lights
						currentDiv.innerHTML = "Lights: "+bridge.lights;
						break;
					default:
						;
				}
			}
			
			var no_bridge_tag = document.getElementById("no-bridge-detected-tag");
			if(no_bridge_tag != null) {
				bridges_tile_list.removeChild(no_bridge_tag);
			}
			
			bridges_tile_list.appendChild(bridgeDiv);
		}
		
		/**  Add a tile in configure GUI for the light in parameter */
		this.addLightTile = function addLightTile(light, lights_tile_list) {
			
			var httpRequest=new XMLHttpRequest();
			httpRequest.open("GET","./html/hueLightTile.html",false);
			httpRequest.send();
			
			var lightDiv = document.createElement('div');
			lightDiv.innerHTML = httpRequest.responseText;
			lightDiv.id = light.lightId;
			
			var atag = lightDiv.childNodes[0];
			var subDiv = atag.childNodes;
			var nbDiv = subDiv.length;
			var currentDiv;
			var lightState = light.state;
			
			for(var i = 0; i < nbDiv; i++){
				currentDiv = subDiv[i];
				currentDiv.id = currentDiv.id+"-"+light.lightId;
				
				switch(i){
					case 1: //Header - reachable
						var reachable = lightState.reachable;
						if(reachable == "true") {
							currentDiv.innerHTML = "OK";
						}else {
							currentDiv.innerHTML = "OUT";
							atag.className = "tile square text bg-color-red";
						}
						break;
					case 3: //Id
						currentDiv.innerHTML = light.lightId;
						break;
					case 5: //State
						currentDiv.innerHTML = lightState.on;
						break;
					case 7: //color
						currentDiv.innerHTML = lightState.hue;
						break;
					case 9: //hue-type
						currentDiv.innerHTML = light.type;
						break;
					default:
						;
				}
			}
			
			var no_light_tag = document.getElementById("no-ligths-detected-tag");
			if(no_light_tag != null) {
				lights_tile_list.removeChild(no_light_tag);
			}
			
			lights_tile_list.appendChild(lightDiv);
			
		}
		
		/** Display details for the clicked bridge tile */
		this.goToBridgeDisplay = function goToBridgeDisplay(bridge) {
			var httpRequest=new XMLHttpRequest();
			httpRequest.open("GET","./html/philipshue-bridge.html",false);
			httpRequest.send();
			
			// Get the current bridge status from the clicked tile
			var status = document.getElementById("bridge-status-"+bridge).innerHTML;
			
			//Display the next sub menu and update the navigation bar
			appsgateMain.gotToNextSubMenu("bridge "+bridge, httpRequest.responseText);
			
			if(status.indexOf("OK") != -1) {
				
				//fill the tile with bridge information
				var call = eval({"CONFIGURATION":"getBridgeInfo", "getBridgeInfo":{"ip":bridge}, "TARGET":"PHILIPSHUE"});
				appsgateMain.sendCmd(JSON.stringify(call));
				
				//set the JavaScript method call to tile click action
				var actionTile = document.getElementById("pushlinkTile");
				actionTile.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().pushLinkSend(\""+bridge+"\");");
				
				actionTile = document.getElementById("updatesoftwareTile");
				actionTile.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().updatefirmware(\""+bridge+"\");");
				
				actionTile = document.getElementById("findlightserialTile");
				var serial = "";
				actionTile.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().findNewLightSerial(\""+bridge+"\", \""+serial+"\");");
				
				actionTile = document.getElementById("findnewlightsTile");
				actionTile.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().findNewLights(\""+bridge+"\");");
				
				//Get the hue lights
				call = eval({"CONFIGURATION":"getBridgeLights", "getBridgeLights":{"ip":bridge}, "TARGET":"PHILIPSHUE"});
				appsgateMain.sendCmd(JSON.stringify(call));
				
			}else if (status.indexOf("WARNING") != -1){
				
				//fill the tile with bridge information
				var call = eval({"CONFIGURATION":"getBridgeInfo", "getBridgeInfo":{"ip":bridge}, "TARGET":"PHILIPSHUE"});
				appsgateMain.sendCmd(JSON.stringify(call));
				
				//set the JavaScript method call to tile click action
				var actionTile = document.getElementById("pushlinkTile");
				actionTile.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().pushLinkSend(\""+bridge+"\");");
				actionTile.className = "tile square text bg-color-yellow";
				
				actionTile = document.getElementById("updatesoftwareTile");
				actionTile.className = "tile square text bg-color-gray";
				
				actionTile = document.getElementById("findlightserialTile");
				actionTile.className = "tile square text bg-color-gray";
				
				actionTile = document.getElementById("findnewlightsTile");
				actionTile.className = "tile square text bg-color-gray";
				
				var infoTile = document.getElementById("bridgetopinfoTile-header");
				infoTile.innerHTML = "Bridge";
				infoTile = document.getElementById("bridgetopinfoTile-id");
				infoTile.innerHTML = "not associated";
				infoTile = document.getElementById("bridgetopinfoTile-version");
				infoTile.innerHTML = "push the top button";		
				
				infoTile = document.getElementById("bridgeproxyinfoTile");
				infoTile.className = "tile wide text bg-color-gray";
				
				infoTile = document.getElementById("bridgetimeinfoTile");
				infoTile.className = "tile wide text bg-color-gray";
				
			}else{
				console.log("status: "+status+" not supported yet.");
			}
		}
		
		/** send the pushLink request */
		this.pushLinkSend = function pushLinkSend(bridge) {
			var call = eval({"CONFIGURATION":"pushlinkSync", "pushlinkSync":{"ip":bridge}, "TARGET":"PHILIPSHUE"});
			appsgateMain.sendCmd(JSON.stringify(call));
		}
		
		/** send the find new lights request */
		this.findNewLights = function findNewLights(bridge) {
			var call = eval({"CONFIGURATION":"findNewLights", "findNewLights":{"ip":bridge}, "TARGET":"PHILIPSHUE"});
			appsgateMain.sendCmd(JSON.stringify(call));
		}
		
		/** send the find new light with serial id request */
		this.findNewLightSerial = function findNewLightSerial(bridge, serial) {
			var call = eval({"CONFIGURATION":"findNewLightSerial", "findNewLightSerial":{"ip":bridge, "serial":serial}, "TARGET":"PHILIPSHUE"});
			appsgateMain.sendCmd(JSON.stringify(call));
		}
		
		/** send the update firmware request */
		this.updatefirmware = function updatefirmware(bridge) {
			var call = eval({"CONFIGURATION":"updatefirmware", "updatefirmware":{"ip":bridge}, "TARGET":"PHILIPSHUE"});
			appsgateMain.sendCmd(JSON.stringify(call));
		}
	
	};
	return returnedModule;
});