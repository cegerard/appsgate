define([], function () {
	var returnedModule = function () {
	
		var _name = 'Philips HUE configuration module';
		this.getName = function () {return _name;}
		
		
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
			
			//{"TARGET":"PHILIPSHUE","hueConfDevices":{"lights":[{"name":"MK2","lightId":"2"},{"name":"MK1","lightId":"1"}],"bridges":[{"lights":"2","status":"OK","MAC":"00:17:88:0a:99:c1","ip":"194.199.23.165"},{"lights":"N.A","status":"not associated","MAC":"00:17:88:18:29:30","ip":"194.199.23.135"}]}}
		}
		
		
		this.addBridgeTile = function addBridgeTile(bridge) {
			console.log(bridge.ip);
		}
		
		
		this.addLightTile = function addLightTile(light) {
			console.log(light.name);
		}
	
	};
	return returnedModule;
});