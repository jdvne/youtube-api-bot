package controller.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.Scanner;

public class Authorization {
    private static final String CLIENT_SECRET= "/client_secret.json";
    private static final String DEV_KEY = "res/dev_key.txt";

    private static final Collection<String> SCOPES =
            Collections.singletonList("https://www.googleapis.com/auth/youtube.force-ssl");

    private static final String APPLICATION_NAME = "YouTubeAPIBot";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private YouTube youtubeService;
    private String developerKey;

    public Authorization() {
        loadService();
        loadDeveloperKey();
    }

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     */
    private Credential authorize(final NetHttpTransport httpTransport) {
        // Load client secrets.
        InputStream in = getClass().getResourceAsStream(CLIENT_SECRET);
        GoogleClientSecrets clientSecrets = null;
        try {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).build();
            return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Build an authorized API client service.
     */
    private void loadService() {
        System.out.println("Attempting authorization process...");

        final NetHttpTransport httpTransport;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = authorize(httpTransport);
            youtubeService = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        System.out.println("Successfully obtained authorization.");
    }

    /**
     * Grabs the developer key from dev_key.txt in the resources folder.
     */
    private void loadDeveloperKey(){
        System.out.println("Attempting to load the developer key from dev_key.txt...");

        try {
            Scanner sc = new Scanner(new File(DEV_KEY));
            developerKey = sc.next();
            sc.close();
        } catch (IOException e) {
            System.exit(-1);
            //TODO error statement here
        }

        System.out.println("Successfully loaded the developer key.");
    }

    public YouTube getService(){
        return youtubeService;
    }
    public String getDeveloperKey(){
        return developerKey;
    }
}
