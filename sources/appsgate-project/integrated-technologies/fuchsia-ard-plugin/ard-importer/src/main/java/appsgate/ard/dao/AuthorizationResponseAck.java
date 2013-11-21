package appsgate.ard.dao;

public class AuthorizationResponseAck {

	private byte lengthH;
	private byte lengthL;
	
	private byte frameH;
	private byte frameL;
	
	private byte functionH;
	private byte functionL;

	public static AuthorizationResponseAck fromStream(byte[] stream){
		
		AuthorizationResponseAck ara=new AuthorizationResponseAck();
		
		ara.lengthL=stream[0];
		ara.lengthH=stream[1];
		ara.functionL=stream[4];
		ara.functionH=stream[5];
		
		assert ara.lengthL==0x06;
		assert ara.lengthH==0x00;
		
		return ara;
	}
	
}
