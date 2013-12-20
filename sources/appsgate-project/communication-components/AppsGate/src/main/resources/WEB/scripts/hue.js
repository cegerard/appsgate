/**
 * Get devices
 */
function getDevices()
{
	appsgateMain.getWebSocket().send("{\"getConfDevices\":{}, \"CONFIGURATION\":\"getConfDevices\"}");
}