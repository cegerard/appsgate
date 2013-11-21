package appsgate.ard.dao;

import appsgate.ard.aperio.AperioAccessDecision;
import appsgate.ard.aperio.AperioConstants;

import java.io.ByteArrayOutputStream;

public class AuthorizationResponse {

	AperioAccessDecision decision;
	AuthorizationRequest ar;
	
	public AuthorizationResponse(AperioAccessDecision access,AuthorizationRequest ar){
		this.decision =access;
		this.ar=ar;
	}

	public byte[] toStream(){
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(9);
		buffer.write(0x09);
		buffer.write(0);
		
		buffer.write(AperioConstants.EMPTY);
		buffer.write(AperioConstants.EMPTY);
		
		buffer.write(0x3A);
		buffer.write(0);
		
		buffer.write(0x01);
		
		buffer.write(ar.getDoorId());
		
		buffer.write(decision.hexa());
		
		return buffer.toByteArray(); 
		
	}

    public AperioAccessDecision getDecision() {
        return decision;
    }
}
