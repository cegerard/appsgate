/**
 * Get devices
 */
function getDevices()
{
	appsgateMain.sendCmd("{\"getConfDevices\":{}, \"CONFIGURATION\":\"getConfDevices\", \"TARGET\":\"ENOCEAN\"}");
}