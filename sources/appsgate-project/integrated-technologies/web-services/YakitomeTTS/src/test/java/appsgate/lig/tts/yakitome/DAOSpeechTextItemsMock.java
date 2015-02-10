package appsgate.lig.tts.yakitome;

import java.util.HashSet;
import java.util.Set;

public class DAOSpeechTextItemsMock implements DAOSpeechTextItems {
	
	Set<SpeechTextItem> items = new HashSet<SpeechTextItem>();


	@Override
	public boolean testService() {
		return true;
	}

	@Override
	public Set<SpeechTextItem> getSpeechItemsFromDB() {
		return items;
	}

	@Override
	public void storeSpeechItemsToDB(Set<SpeechTextItem> items) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addUpdateSpeechItem(SpeechTextItem item) {
		items.add(item);

	}

	@Override
	public void removeSpeechItem(int item_id) {
		// TODO
		
	}

}
