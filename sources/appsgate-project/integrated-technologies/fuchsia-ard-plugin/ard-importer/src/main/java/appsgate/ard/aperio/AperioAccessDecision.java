package appsgate.ard.aperio;

public enum AperioAccessDecision {

	GRANTED((byte)0x00),
	NOT_GRANTED((byte)0x09);
	
	private byte value;
	
	AperioAccessDecision(byte value){
		this.value=value;
	}
	
	public byte hexa(){
		return value;
	}
	
}
