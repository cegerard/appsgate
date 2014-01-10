package appsgate.ard.dao;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class AuthorizationRequest {

	private byte lengthH;
	private byte lengthL;
	private byte frameH;
	private byte frameL;
	private byte functionH;
	private byte functionL;
	private byte typeH;
	private byte typeL;
	private byte door;
	private byte card[];

	public byte getLengthH() {
		return lengthH;
	}

	public void setLengthH(byte lengthH) {
		this.lengthH = lengthH;
	}

	public byte getLengthL() {
		return lengthL;
	}

	public void setLengthL(byte lengthL) {
		this.lengthL = lengthL;
	}

	public byte getFrameH() {
		return frameH;
	}

	public void setFrameH(byte frameH) {
		this.frameH = frameH;
	}

	public byte getFrameL() {
		return frameL;
	}

	public void setFrameL(byte frameL) {
		this.frameL = frameL;
	}

	public byte getFunctionH() {
		return functionH;
	}

	public void setFunctionH(byte functionH) {
		this.functionH = functionH;
	}

	public byte getFunctionL() {
		return functionL;
	}

	public void setFunctionL(byte functionL) {
		this.functionL = functionL;
	}

	public byte getTypeH() {
		return typeH;
	}

	public void setTypeH(byte typeH) {
		this.typeH = typeH;
	}

	public byte getTypeL() {
		return typeL;
	}

	public void setTypeL(byte typeL) {
		this.typeL = typeL;
	}

	public byte getDoorId() {
		return door;
	}

	public void setDoorId(byte door) {
		this.door = door;
	}

	public byte[] getCard() {
		return card;
	}

    public int getCardIntCode(){
        return java.nio.ByteBuffer.wrap(card).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
    }

	public void setCard(byte[] card) {
		this.card = card;
	}

    public static AuthorizationRequest fromData(byte door,Integer card) {
        AuthorizationRequest ar = new AuthorizationRequest();
        ar.door=door;

        ByteBuffer bigInt = ByteBuffer.allocate(8).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        bigInt.putInt(card);

        ar.card=bigInt.array();

        return ar;
    }

	public static AuthorizationRequest fromStream(byte[] stream) {

		byte card[] = new byte[8];

		System.arraycopy(stream, 9, card, 0, 7);

		AuthorizationRequest ar = new AuthorizationRequest();
		ar.setLengthL(stream[0]);
		ar.setLengthH(stream[1]);
		ar.setFrameL(stream[2]);
		ar.setFrameH(stream[3]);
		ar.setFunctionL(stream[4]);
		ar.setFunctionH(stream[5]);
		ar.setTypeL(stream[6]);
		ar.setTypeH(stream[7]);
		ar.setDoorId(stream[8]);
		ar.setCard(card);

		assert stream.length == stream[0];

		assert ar.getLengthL() == 0x11;
		assert ar.getLengthH() == 0x00;

		assert ar.getFunctionL() == 0x26;
		assert ar.getFunctionH() == 0x01;

		assert ar.getTypeL() == 0x05;
		assert ar.getTypeH() == 0x00;
		
		return ar;

	}

	public String toString() {
		return "AuthorizationRequest [door=" + door + " card="
				+ Arrays.toString(card) + "] "+getCardIntCode();
	}

}
