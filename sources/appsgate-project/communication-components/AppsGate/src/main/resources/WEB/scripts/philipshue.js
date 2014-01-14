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
				
				for(light in lightsArray) {
					this.addLightTile(lightsArray[light]);
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
		this.addLightTile = function addLightTile(light) {
			
			var httpRequest=new XMLHttpRequest();
			httpRequest.open("GET","./html/hueLightTile.html",false);
			httpRequest.send();
			
			var lights_tile_list = document.getElementById("lights-tile-list");
						
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
			
			appsgateMain.gotToNextSubMenu("bridge "+bridge, httpRequest.responseText);
			
			var pushlinkTile = document.getElementById("pushlinkTile");
			pushlinkTile.setAttribute("onclick", "javascript:appsgateMain.getWebSocket().getHue().pushLinkSend(\""+bridge+"\");");
		}
		
		/** send the pushLink request */
		this.pushLinkSend = function addLightTile(bridge) {
			var call = eval({"CONFIGURATION":"pushlinkSync", "pushlinkSync":{"ip":bridge}, "TARGET":"PHILIPSHUE"});
			appsgateMain.sendCmd(JSON.stringify(call));
		}
	
	};
	return returnedModule;
});