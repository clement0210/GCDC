package fr.clement0210.agenda;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by cduffau on 01/07/16.
 */
public class DistributionChecker {

    /** Application name. */
    private static final String APPLICATION_NAME =
            "Google Calendar API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/calendar-java-quickstart.json");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart.json
     */
    private static final List<String> SCOPES =
            Arrays.asList(CalendarScopes.CALENDAR_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private String calendarId;

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                DistributionChecker.class.getClassLoader().getResourceAsStream("client_id.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar
    getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Constructor
     * @param calendarName a calendar name
     * @throws IOException
     */
    public DistributionChecker(String calendarName) throws IOException {
        com.google.api.services.calendar.Calendar service =
                getCalendarService();

        // Retrieve an event
        CalendarList calendar = service.calendarList().list().execute();
        for (CalendarListEntry c : calendar.getItems()) {
            if (c.getSummary().equals(calendarName)) {
                calendarId = c.getId();
            }
        }

    }

    /**
     * Method to get distribution of events with weights to each kind of event
     * @param events a list of events
     * @param weights a list of associated weights
     * @return the weighted number of each events
     * @throws IOException
     */
    public Map<String, Number> getDistributionWithWeights(List<String> events, List<Double> weights) throws Exception {
        if(events==null || weights==null || events.size()!=weights.size()){
            throw new Exception("Mismatch between events and weights");
        }
        Map<String,Number> integerMap=new HashMap<String, Number>();
        Events eventss=getCalendarService().events().list(calendarId).execute();
        for(Event event: eventss.getItems()){
            int index=events.indexOf(event.getSummary());
            if(index>=0){
                Number value=integerMap.get(events.get(index));
                if(value==null){
                    integerMap.put(events.get(index),weights.get(index));
                }
                else {
                    integerMap.put(events.get(index),integerMap.get(events.get(index)).intValue()+weights.get(index));
                }
            }
        }

        return integerMap;
    }

    /**
     * Method to get distribution of events
     * @param events a list of events
     * @return the number of each events
     * @throws IOException
     */
    public Map<String, Number> getDistribution(List<String> events) throws Exception {
        List<Double> weights=new ArrayList<Double>();
        for(int i=0;i<events.size();i++){
            weights.add(1.0);
        }
        return getDistributionWithWeights(events,weights);
    }

    /**
     * Method to get normalized distribution of events
     * @param events a list of events
     * @return the normalized number of each events
     * @throws IOException
     */
    public Map<String,Number> getNormalizedDistribution(List<String> events) throws Exception {
        Map<String,Number> distribution=getDistribution(events);
        int total=0;
        for(Number integer:distribution.values()){
            total+=integer.intValue();
        }
        for (String key:distribution.keySet()){
            distribution.put(key,distribution.get(key).doubleValue()/total);
        }
        return distribution;
    }

    /**
     * Method to get normalized distribution of events with weights to each kind of event
     * @param events a list of events
     * @param weights a list of associated weights
     * @return the normalized weighted number of each events
     * @throws IOException
     */
    public Map<String,Number> getNormalizedDistributionWithWeights(List<String> events,List<Double> weights) throws Exception {
        Map<String,Number> distribution=getDistributionWithWeights(events,weights);
        int total=0;
        for(Number integer:distribution.values()){
            total+=integer.intValue();
        }
        for (String key:distribution.keySet()){
            distribution.put(key,distribution.get(key).doubleValue()/total);
        }
        return distribution;
    }


}
