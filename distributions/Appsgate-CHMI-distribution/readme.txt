Warning in this version bundles need to be instantiated manually:

 Instantiate in this order:
	- AppsGateClientCommunicationManager
	- PropertyHistoryManagerMongoImpl
	- All needed Adapter (PhilipsHUE, UPnP, Ubikit, Watteco, etc.)
	- CHMIProxyImpl
