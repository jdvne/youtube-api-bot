package main;

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
import com.google.api.services.youtube.model.Video;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

// TODO MAKE THIS A YOUTUBE VIDEO
// "I made a youtube video that changes its title to the last comment."

// TODO upload to SpiceMeatbol, not personal acc
// TODO add fail-safes (try/catch)

public class LatestCommentToTitle {
    private static final String CLIENT_SECRET= "client_secret.json";

    private static final Collection<String> SCOPES =
            Collections.singletonList("https://www.googleapis.com/auth/youtube.force-ssl");

    private static final String VIDEO_ID = "l4EjoYLPke0";
    private static final String APPLICATION_NAME = "LatestCommentToTitle";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final long SLEEP_TIME_BASE = 180000; // 3 minutes
    private static final long SLEEP_TIME_LONG = 480000; // 8 minutes

    private static String developerKey;
    private static int quotaUsage = 0;

    /**
     * Loads the developer key from dev_key.txt in the resources folder.
     */
    public static void loadDeveloperKey() {
        try {
            Scanner sc = new Scanner(new File("src/main/resources/dev_key.txt"));
            developerKey = sc.next();
            sc.close();
        } catch (IOException e) {
            log("ERROR: Could not load developer key from dev_key.txt.");
            System.exit(-1);
        }
    }

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        InputStream in = LatestCommentToTitle.class.getResourceAsStream(CLIENT_SECRET);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    /**
     * Log a message to the console with a timestamp.
     *
     * @param message message
     */
    private static void log (String message) {
        System.out.println(new Date() + " qu: " + quotaUsage + " | " + message);
    }

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException
     */
    public static void main(String[] args) throws GeneralSecurityException, IOException, InterruptedException {
        log("Attempting to load the developer key from dev_key.txt...");
        loadDeveloperKey();
        log("Successfully loaded the developer key.");

        log("Attempting authorization process...");
        YouTube youtubeService = getService();
        log("Successfully obtained authorization.");

        log("Attempting to get the video data...");
        Video video = youtubeService.videos()
                .list("snippet")
                .setId(VIDEO_ID)
                .execute()
                .getItems()
                .get(0);
        log("Successfully got the video data.");

        quotaUsage += 3;  // cost was most likely 3

        // temp timer
        long sleepTime = SLEEP_TIME_BASE;
        log("Set the sleep time to " + SLEEP_TIME_BASE + " ms.");

        while(true){
            // each time there is a new comment, sleep for eight minutes
            // otherwise, sleep for 3
            log("Beginning sleep for " + sleepTime + " ms.");
            Thread.sleep(sleepTime);
            log("Sleep ended.");

            log("Attempting to get the latest comment...");
            String latestComment  = youtubeService.commentThreads()
                    .list("snippet")
                    .setKey(developerKey)
                    .setFields("items(snippet/topLevelComment/snippet/textDisplay)")
                    .setMaxResults(1L)
                    .setModerationStatus("published")
                    .setOrder("time")
                    .setVideoId(VIDEO_ID)
                    .execute()
                    .getItems()
                    .get(0)
                    .getSnippet()
                    .getTopLevelComment()
                    .getSnippet()
                    .getTextDisplay();

            log("Successfully got the latest comment, which was: \n" + latestComment);

            quotaUsage += 3;  // cost was most likely 3

            // cut the latest comment under 100 characters if it is over
            if(latestComment.length() > 99){
                latestComment = latestComment.substring(0,100);
                log("Latest comment was over 100 characters, cutting down to 100.");
            }

            log("Attempting to update title.");
            if(video.getSnippet().getTitle().equals(latestComment)){
                log("Title was already up to date.");

                sleepTime = SLEEP_TIME_BASE;
                log("Set the sleep time to " + SLEEP_TIME_BASE + " ms.");
                continue;
            }

            // set the local version of the title
            video.getSnippet().setTitle(latestComment);

            video = youtubeService.videos()
                    .update("snippet", video.setId(VIDEO_ID))
                    .setKey(developerKey)
                    .execute();

            log("Successfully updated the title.");

            quotaUsage += 53;  // cost was most likely 53

            sleepTime = SLEEP_TIME_LONG;
            log("Set the sleep time to " + SLEEP_TIME_LONG + " ms.");
        }
    }
}