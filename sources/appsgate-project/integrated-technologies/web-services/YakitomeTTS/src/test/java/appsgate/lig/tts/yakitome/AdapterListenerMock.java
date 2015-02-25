package appsgate.lig.tts.yakitome;

import appsgate.lig.tts.yakitome.impl.YakitomeAPIClient;

public class AdapterListenerMock implements TTSAdapter {

	@Override
	public void serviceUnavailable() {
		// TODO Auto-generated method stub

	}

	@Override
	public DAOSpeechTextItems getDAO() {
		// TODO Auto-generated method stub
		return new DAOSpeechTextItemsMock();
	}

	@Override
	public YakitomeAPI getAPI() {
		YakitomeAPI api = new YakitomeAPIClient(new AdapterListenerMock());
		// api key registered for smarthome.inria at gmail.com
		api.configure("5otuvhvboadAgcLPwy69P");
		return api;
	}

}
