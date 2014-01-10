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
			
			//{"TARGET":"PHILIPSHUE","hueConfDevices":{"lights":[{"name":"MK2","lightId":"2"},{"name":"MK1","lightId":"1"}]
			//,"bridges":[{"lights":"2","status":"OK","MAC":"00:17:88:0a:99:c1","ip":"194.199.23.165"},{"lights":"N.A","status":"not associated","MAC":"00:17:88:18:29:30","ip":"194.199.23.135"}]}}
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
			console.log(light.name);
		}
	
	};
	return returnedModule;
});