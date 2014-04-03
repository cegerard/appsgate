define([], function () {
	var returnedModule = function () {
	
		var _name = 'UPnP configuration module';
		this.getName = function () {return _name;}
		
		/**
			UPnP configuration message handler
		*/
		this.messageHandler = function messageHandler(message) {
			if (message.hasOwnProperty("mediaServices")) {
				var conf = message.mediaServices;
				var playersArray = conf.players;
				var browsersArray = conf.browsers;
				
				for(i in playersArray) {
					this.addPlayerTile(playersArray[i]);
				}
				for(i in browsersArray) {
					this.addBrowserTile(browsersArray[i]);
				}				
			}			
		}
		
		/** Add a tile in configure GUI for the player in parameter */
		this.addPlayerTile = function addPlayerTile(player) {

			var httpRequest=new XMLHttpRequest();
			httpRequest.open("GET","./html/upnp/playerTile.html",false);
			httpRequest.send();
			
			var players_tile_list = document.getElementById("players-tile-list");
						
			var playerDiv = document.createElement('div');
			playerDiv.innerHTML = httpRequest.responseText;
			playerDiv.id = player.objectId;
			
			var atag = playerDiv.childNodes[0];
			var subDiv = atag.childNodes;
			var nbDiv = subDiv.length;
			var currentDiv;
			
			for(var i = 0; i < nbDiv; i++){
				currentDiv = subDiv[i];
				currentDiv.id = currentDiv.id+"-"+player.objectId;
				
				switch(i){
					case 1: //name
						currentDiv.innerHTML = player.friendlyName;
						break;
					case 3: //id
						currentDiv.innerHTML = player.objectId;

						break;
					case 5: //Volume
						currentDiv.innerHTML = "Current Volume: "+player.volume;
						break;
					default:
						;
				}
			}
			
			var no_players_tag = document.getElementById("no-players-detected-tag");
			if(no_players_tag != null) {
				players_tile_list.removeChild(no_players_tag);
			}
			
			players_tile_list.appendChild(playerDiv);
		}
		
		/** Add a tile in configure GUI for the browser in parameter */
		this.addBrowserTile = function addBrowserTile(browser) {

			var httpRequest=new XMLHttpRequest();
			httpRequest.open("GET","./html/upnp/browserTile.html",false);
			httpRequest.send();
			
			var browsers_tile_list = document.getElementById("browsers-tile-list");
						
			var browserDiv = document.createElement('div');
			browserDiv.innerHTML = httpRequest.responseText;
			browserDiv.id = browser.objectId;
			
			var atag = browserDiv.childNodes[0];
			var subDiv = atag.childNodes;
			var nbDiv = subDiv.length;
			var currentDiv;
			
			for(var i = 0; i < nbDiv; i++){
				currentDiv = subDiv[i];
				currentDiv.id = currentDiv.id+"-"+browser.objectId;
				
				switch(i){
					case 1: //id
						currentDiv.innerHTML = browser.friendlyName;
						break;
					case 3: //name
						currentDiv.innerHTML = browser.objectId;

						break;
					default:
						;
				}
			}
			
			var no_browsers_tag = document.getElementById("no-browsers-detected-tag");
			if(no_browsers_tag != null) {
				browsers_tile_list.removeChild(no_browsers_tag);
			}
			
			browsers_tile_list.appendChild(browserDiv);
		}		
	
	
	};
	return returnedModule;
});