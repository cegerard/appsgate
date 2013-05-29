package appsgate.lig.router.spec;

import java.util.ArrayList;

public interface RouterApAMSpec {
	
	@SuppressWarnings("rawtypes")
	public Runnable executeCommand(String objectId, String methodName, ArrayList<Object> args, ArrayList<Class> paramType);
}
