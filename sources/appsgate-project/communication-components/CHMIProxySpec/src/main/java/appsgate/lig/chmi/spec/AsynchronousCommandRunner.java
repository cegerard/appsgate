package appsgate.lig.chmi.spec;

public interface AsynchronousCommandRunner extends Runnable {
	
	/**
	 * This method allow the router to invoke methods on an abstract java
	 * object.
	 *  Invocation parameters :
	 *  Object obj, Object[] args, ArrayList<Class> paramTypes, String methodName
	 *  
	 * @param obj, the abstract object on which the method will be invoke
	 * @param args, all arguments for the method call
	 * @param methodName, the method to invoke
	 * @return the result of dispatching the method represented by this object
	 *         on obj with parameters args
	 * @throws Exception
	 */	
	
	
	/**
	 * This one is designed to execute the command, can be called synchronously
	 * or will be called using the run method
	 * @return
	 * @throws Exception
	 */
	public Object abstractInvoke()
			throws Exception;
	
	/**
	 * Get the return object of the last call
	 * @return the return type of the last call
	 */
	public Object getReturn();

}
