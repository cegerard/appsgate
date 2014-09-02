package appsgate.lig.proxy.google.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collection;

/**
 * Helper Class to connect to Google Calendar Service
 * Created by thibaud on 20/08/2014.
 */
public class GoogleCalendarService {

    private static Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);

    public static final String APPLICATION_NAME = "AppsGateCalendarAdapter";

    public static GoogleClientSecrets loadClientSecretsResource(String pathToJSONSecret,
                                                                String fileEncoding,
                                                                JsonFactory jsonFactory) throws IOException {
        logger.debug("loadClientSecretsResource(String pathToJSONSecret : "+pathToJSONSecret
                +", String fileEncoding : "+fileEncoding
                +", JsonFactory jsonFactory"+jsonFactory
                +")");
        try{
            return GoogleClientSecrets.load(
                    jsonFactory,
                    new InputStreamReader(
                            GoogleAdapter.class.getResourceAsStream(pathToJSONSecret), fileEncoding));
        } catch (IOException exc) {
            logger.error("loadClientSecretsResource(...) throws IOException : "+exc.getMessage());
            throw new IOException(exc);
        }
    }

    public static Credential buildCredentialFromP12File(String clientAccountMail,
                                             JsonFactory jsonFactory,
                                             HttpTransport httpTransport,
                                             String pathToP12File,
                                             Collection<String> scopes) throws IOException, GeneralSecurityException{
        logger.debug("buildCredentialFromP12File(String clientAccountMail : "+clientAccountMail
                + ", JsonFactory jsonFactory: "+jsonFactory
                + ", HttpTransport httpTransport: "+httpTransport
                + ", String pathToP12File: "+pathToP12File
                + ", Collection<String> scopes: "+scopes
                +")");

        try {
            return new GoogleCredential.Builder().setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(clientAccountMail)
                    .setServiceAccountScopes(scopes)
                    .setServiceAccountPrivateKeyFromP12File(new File(pathToP12File))
                    .setServiceAccountUser("smarthome.inria@gmail.com")
                    .build();

        } catch (IOException exc) {
            logger.error("buildCredentialFromP12File(...) throws IOException : "+exc.getMessage());
           throw new IOException(exc);
        } catch (GeneralSecurityException exc) {
            logger.error("buildCredentialFromP12File(...) throws GeneralSecurityException : "+exc.getMessage());
            throw new GeneralSecurityException(exc);

        }
    }


    public static Calendar getGoogleCalendarService(JsonFactory jsonFactory,
                                                    HttpTransport httpTransport,
                                                    Credential credential) throws Exception {
        logger.debug("getGoogleCalendarService(JsonFactory jsonFactory : "+jsonFactory
                + ", HttpTransport httpTransport: "+httpTransport
                + ", Credential credential: "+credential
                +")");

            Calendar service = new Calendar.Builder(
                    httpTransport, jsonFactory, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        if (service==null) {
            logger.error("getGoogleCalendarService(...) Cannot connect to Google Calendar Service");
            throw new Exception("Cannot connect to Google Calendar Service");
        }
            logger.debug("Calendar service build successfully");


        logger.debug("settings : "+service.settings().list().executeUnparsed().parseAsString());
        logger.debug("calendar list : " + service.calendarList().list().executeUnparsed().parseAsString());

        return service;
    }


}
