package appsgate.lig.ard.badge.door.spec;

public interface CoreARDBadgeDoorSpec {

	public boolean getContactStatus();
    public String getLastCard();
    public String getARDClass();
    public Integer getDoorID();
    public String getStatus();
    public String getLastMessage();
    public void zoneActivate(int zone);
    public void zoneDesactivate(int zone);
    public void forceInput(int input,boolean value);

}
