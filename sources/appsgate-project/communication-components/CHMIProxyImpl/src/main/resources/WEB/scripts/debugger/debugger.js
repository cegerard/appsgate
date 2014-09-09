define([], function () {
	var returnedModule = function () {
	
		var _name = 'Time line debugger';
		this.getName = function () {return _name;}
        
        /** Toggle the current pairing mode */
		this.startDebugger = function () {
		    chmi.sendJSONCmd(eval({"method":"startDebugger", "args":[], "callId":"startDB", "TARGET":"EHMI"}))
		}
        
    };
	return returnedModule;
});
       