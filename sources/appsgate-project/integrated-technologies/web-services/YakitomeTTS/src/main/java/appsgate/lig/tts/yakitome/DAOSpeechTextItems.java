package appsgate.lig.tts.yakitome;

import java.util.Set;

public interface DAOSpeechTextItems {

	public boolean testService();

	public Set<SpeechTextItem> getSpeechItemsFromDB();

	public void storeSpeechItemsToDB(Set<SpeechTextItem> items);

	public void addUpdateSpeechItem(SpeechTextItem item);

	public void removeSpeechItem(int id);

}