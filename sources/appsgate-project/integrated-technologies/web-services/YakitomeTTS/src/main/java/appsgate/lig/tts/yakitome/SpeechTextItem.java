package appsgate.lig.tts.yakitome;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.tts.yakitome.impl.YakitomeAPIClient;

/**
 * Holder class to represent one Speech to Text Item,
 * containing its id, the sentence, the word count, the URL(s) for its MP3 representations
 * @author thibaud
 *
 */
public class SpeechTextItem {
	
	/**
	 * @param book_id
	 * @param text
	 * @param wordCount
	 * @param audioURLs
	 */
	public SpeechTextItem(int book_id, String text,
			int wordCount, List<String> audioURLs) {
		if(book_id < 1 ) {
			throw new NullPointerException("book_id = null");
		} else {
			this.book_id = book_id;
		}
		
		if(text == null ||text.isEmpty() ) {
			throw new NullPointerException("text = null");
		} else {
			this.text = text;
		}
		
		if(wordCount < 1 ) {
			throw new NullPointerException("wordCount < 1");
		} else {
			this.wordCount = wordCount;
		}
		
		if(audioURLs == null ||audioURLs.isEmpty() ) {
			throw new NullPointerException("audioURLs = null");
		} else {
			this.audioURLs = audioURLs;
		}
	}
	
	public SpeechTextItem(JSONObject json) {
		System.out.println("SpeechTextItem(JSONObject json : "+json.toString());
		
		if(!json.has(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY)
				||json.getInt(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY) ==0) {
			throw new NullPointerException("book_id = null");
		} else {
			this.book_id = json.getInt(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY);
		}
		
		if(!json.has(YakitomeAPIClient.TEXT_KEY)
				||json.getString(YakitomeAPIClient.TEXT_KEY) == null
				||json.getString(YakitomeAPIClient.TEXT_KEY).length()<1) {
			throw new NullPointerException("text = null");
		} else {
			this.text = json.getString(YakitomeAPIClient.TEXT_KEY);
		}
		
		if(!json.has(YakitomeAPIClient.WORD_CNT_RESPONSE_KEY)
				||json.getInt(YakitomeAPIClient.WORD_CNT_RESPONSE_KEY) <1) {
			throw new NullPointerException("wordCount < 1");
		} else {
			wordCount = json.getInt(YakitomeAPIClient.WORD_CNT_RESPONSE_KEY);
		}
		
		if(!json.has(YakitomeAPIClient.AUDIOS_RESPONSE_KEY)
				||json.optJSONArray(YakitomeAPIClient.AUDIOS_RESPONSE_KEY) == null
				||json.optJSONArray(YakitomeAPIClient.AUDIOS_RESPONSE_KEY).length()<1) {
			throw new NullPointerException("audioURLs = null");
		} else {
			audioURLs = new ArrayList<String>();
			JSONArray tmp = json.optJSONArray(YakitomeAPIClient.AUDIOS_RESPONSE_KEY);
			for(int i=0; tmp!= null && i<tmp.length(); i++) {
				audioURLs.add(tmp.getString(i));
			}
		}
	}	
	
	public int getBookId() {
		return book_id;
	}
	public String getText() {
		return text;
	}
	public int getWordCount() {
		return wordCount;
	}
	public List<String> getAudioURLs() {
		return audioURLs;
	}
	
	public String getFirstAudioURL() {
		if(audioURLs != null && audioURLs.size()>0) {
			return audioURLs.get(0);
		} else {
			return null;
		}
	}

	private int book_id;
	private String text;
	private int wordCount;
	private List<String> audioURLs;
	
	public JSONObject toJSON() {
		JSONObject resp = new JSONObject();
		resp.put(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY, book_id);
		resp.put(YakitomeAPIClient.TEXT_KEY, text);
		resp.put(YakitomeAPIClient.WORD_CNT_RESPONSE_KEY, wordCount);
		resp.put(YakitomeAPIClient.AUDIOS_RESPONSE_KEY, new JSONArray(audioURLs));
		
		return resp;
	}
}
