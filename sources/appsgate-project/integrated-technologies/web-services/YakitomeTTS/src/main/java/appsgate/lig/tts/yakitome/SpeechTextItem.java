package appsgate.lig.tts.yakitome;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import appsgate.lig.tts.yakitome.utils.HttpUtils;

/**
 * Holder class to represent one Speech to Text Item,
 * containing its id, a string representation of the encoded sentence, the word count, the URL(s) for its MP3 representations
 * @author thibaud
 *
 */
public class SpeechTextItem {
	
	/**
	 * @param speechTextId
	 * @param encodedSentence
	 * @param wordCount
	 * @param audioURLs
	 */
	public SpeechTextItem(String speechTextId, String sentence,
			int wordCount, List<String> audioURLs) {
		if(speechTextId == null ||speechTextId.isEmpty() ) {
			throw new NullPointerException("speechTextId = null");
		} else {
			this.speechTextId = speechTextId;
		}
		
		if(sentence == null ||sentence.isEmpty() ) {
			throw new NullPointerException("sentence = null");
		} else {
			try {
				this.encodedSentence = URLEncoder.encode(sentence, HttpUtils.DEFAULT_ENCODING);
			} catch (UnsupportedEncodingException e) {
				throw new NullPointerException("Unable to encode sentence, "+e.getMessage());
			}
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
		
		if(!json.has(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY)
				||json.getString(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY) == null
				||json.getString(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY).length()<1) {
			throw new NullPointerException("speechTextId = null");
		} else {
			this.speechTextId = json.getString(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY);
		}
		
		if(!json.has(YakitomeAPIClient.TEXT_KEY)
				||json.getString(YakitomeAPIClient.TEXT_KEY) == null
				||json.getString(YakitomeAPIClient.TEXT_KEY).length()<1) {
			throw new NullPointerException("sentence = null");
		} else {
			encodedSentence = json.getString(YakitomeAPIClient.TEXT_KEY);
		}
		
		if(!json.has(YakitomeAPIClient.WORD_CNT_RESPONSE_KEY)
				||json.getInt(YakitomeAPIClient.WORD_CNT_RESPONSE_KEY) <1) {
			throw new NullPointerException("wordCount < 1");
		} else {
			encodedSentence = json.getString(YakitomeAPIClient.TEXT_KEY);
		}
		
		if(!json.has(YakitomeAPIClient.AUDIOS_RESPONSE_KEY)
				||json.getJSONArray(YakitomeAPIClient.AUDIOS_RESPONSE_KEY) == null
				||json.getJSONArray(YakitomeAPIClient.AUDIOS_RESPONSE_KEY).length()<1) {
			throw new NullPointerException("audioURLs = null");
		} else {
			audioURLs = new ArrayList<String>();
			JSONArray tmp = json.optJSONArray(YakitomeAPIClient.AUDIOS_RESPONSE_KEY);
			for(int i=0; tmp!= null && i<tmp.length(); i++) {
				audioURLs.add(tmp.getString(i));
			}
		}
	}	
	
	public String getSpeechTextId() {
		return speechTextId;
	}
	public String getEncodedSentence() {
		return encodedSentence;
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

	private String speechTextId;
	private String encodedSentence;
	private int wordCount;
	private List<String> audioURLs;
	
	public JSONObject toJSON() {
		JSONObject resp = new JSONObject();
		resp.put(YakitomeAPIClient.BOOK_ID_RESPONSE_KEY, speechTextId);
		resp.put(YakitomeAPIClient.TEXT_KEY, encodedSentence);
		resp.put(YakitomeAPIClient.WORD_CNT_RESPONSE_KEY, wordCount);
		resp.put(YakitomeAPIClient.AUDIOS_RESPONSE_KEY, new JSONArray(audioURLs).toString());
		
		return resp;
	}
}
