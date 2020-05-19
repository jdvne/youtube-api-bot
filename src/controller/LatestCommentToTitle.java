package controller;

import com.google.api.services.youtube.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO MAKE THIS A YOUTUBE VIDEO
// "I made a youtube video that changes its title to the last comment."

// TODO what channel do I upload this to?
// TODO add retry after catching error
// TODO code commenting
// TODO add function for counting runtime

public class LatestCommentToTitle extends BotProcess {
    private final String VIDEO_ID = "l4EjoYLPke0";

    private final long SLEEP_TIME_SHORT = 180_000;   // 180 seconds
    private final long SLEEP_TIME_LONG = 480_000;    // 480 seconds

    private Video video;
    private String latestComment;

    private List<String> titles;

    public LatestCommentToTitle(){
        super();
        loadVideo();

        titles = new ArrayList<String>();
    }

    @Override
    public String getBlurb() {
        return "Titles so far: \n" + titles.toString();
    }

    private void loadVideo(){
        log("Attempting to get the video data...");
        try {
            video = super.auth
                    .getService()
                    .videos()
                    .list("snippet")
                    .setId(VIDEO_ID)
                    .execute()
                    .getItems()
                    .get(0);
        } catch (IOException e) {
            // TODO handle failures
            e.printStackTrace();
        }
        log("Successfully got the video data.");

        quotaUsage += 3;  // cost was most likely 3
    }

    private void loadLatestComment(){
        log("Attempting to get the latest comment...");
        try {
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
        } catch (IOException e) {
            // TODO handle failures
            e.printStackTrace();
        }

        log("Successfully got the latest comment, which was: \n" + latestComment);

        quotaUsage += 3;  // cost was most likely 3
    }

    private void updateVideo(){
        // set the local version of the title
        video.getSnippet().setTitle(latestComment);

        try {
            video = auth.getService()
                    .videos()
                    .update("snippet", video.setId(VIDEO_ID))
                    .setKey(auth.getDeveloperKey())
                    .execute();
        } catch (IOException e) {
            // TODO handle failures
            e.printStackTrace();
        }

        log("Successfully updated the title.");
        titles.add(latestComment);

        quotaUsage += 53;  // cost was most likely 53
    }

    public void run() {
        while(!stopped && !Thread.interrupted()){
            loadLatestComment();

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
                updateVideo();
                sleep(SLEEP_TIME_LONG);
            }
        }

        logger.close();
    }
}