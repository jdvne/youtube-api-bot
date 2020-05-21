package controller;

import com.google.api.services.youtube.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO MAKE THIS A YOUTUBE VIDEO
// "I made a youtube video that changes its title to the last comment."

// TODO what channel do I upload this to?
// TODO code commenting
// TODO add function for counting runtime
// TODO password protection?
//TODO add retry on failure

public class LatestCommentToTitle extends BotProcess {
    private final String VIDEO_ID = "l4EjoYLPke0";

    private final int ATTEMPT_COUNT = 3;

    private final long RETRY_DELAY = 1_000;          // 1 second
    private final long SLEEP_TIME_SHORT = 180_000;   // 180 seconds
    private final long SLEEP_TIME_LONG = 480_000;    // 480 seconds

    private Video video;
    private String latestComment;

    private List<String> titles;

    public LatestCommentToTitle(){
        try {
            loadVideoSnippet();
        } catch (IOException e) {
            log("Retry limit reached.  Video snippet could not be loaded.");
            log("Terminating the bot.");
            System.exit(-1);
        }

        titles = new ArrayList<String>();
    }

    @Override
    public String getBlurb() {
        return "Titles so far: \n" + titles.toString();
    }

    private void loadVideoSnippet() throws IOException {
        log("Attempting to get the video snippet...");
        video = super.auth
                .getService()
                .videos()
                .list("snippet")
                .setId(VIDEO_ID)
                .execute()
                .getItems()
                .get(0);

        log("Successfully got the video snippet.");

        quotaUsage += 3;  // cost was most likely 3
    }

    private void loadLatestComment() throws IOException {
        log("Attempting to get the latest comment...");
        latestComment = auth.getService()
                .commentThreads()
                .list("snippet")
                .setKey(auth.getDeveloperKey())
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
    }

    private void updateVideoTitle() throws IOException{
        // set the local version of the title
        video.getSnippet().setTitle(latestComment);

        video = auth.getService()
                .videos()
                .update("snippet", video.setId(VIDEO_ID))
                .setKey(auth.getDeveloperKey())
                .execute();

        log("Successfully updated the title.");
        titles.add(latestComment);

        quotaUsage += 53;  // cost was most likely 53
    }

    public void run() {
        while(!stopped && !Thread.interrupted()){
            try {
                loadLatestComment();
            } catch (IOException e) {
                log("Retry limit reached.  Latest comment could not be loaded");
                log("Terminating the bot.");
                System.exit(-1);
            }

            // cut the latest comment under 100 characters if it is over
            if(latestComment.length() > 99){
                latestComment = latestComment.substring(0,100);
                log("Latest comment was over 100 characters, cutting down to 100.");
            }

            log("Attempting to update title.");
            if(video.getSnippet().getTitle().equals(latestComment)){
                log("Title was already up to date.");
                sleep(SLEEP_TIME_SHORT);

            } else {
                try {
                    updateVideoTitle();
                } catch (IOException e) {
                    log("Retry limit reached.  Video title could not be updated.");
                    log("Terminating the bot.");
                    System.exit(-1);
                }
                sleep(SLEEP_TIME_LONG);
            }
        }

        logger.close();
    }
}