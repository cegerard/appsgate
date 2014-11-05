package appsgate.lig.ard.badge.door.spec;

public interface CoreARDBadgeDoorSpec {

	public boolean getContactStatus();
    public Integer getLastCard();
    public String getARDClass();
    public Integer getDoorID();
    public String getStatus();
    public String getLastMessage();
    public void zoneActivate();
    public void zoneDesactivate();

}
