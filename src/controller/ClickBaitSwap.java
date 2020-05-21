package controller;

import com.google.api.services.youtube.model.Video;

import java.io.IOException;

public class ClickBaitSwap extends BotProcess{
    private final String VIDEO_ID = "put ID here";

    private final int TRY_COUNT = 3;

    private final long RETRY_DELAY = 1_000;          // 1 second

    private Video video;

    public ClickBaitSwap(){
        for(int tries = 0; tries < TRY_COUNT; tries++) {
            try {
                loadVideoSnippet();
                tries = TRY_COUNT;

            } catch (IOException e) {
                if(tries == TRY_COUNT - 1){
                    log("Retry limit reached.  Video snippet could not be loaded.");
                    log("Terminating the bot.");
                    System.exit(-1);
                }
                failures += 1;
                sleep(RETRY_DELAY);
            }
        }
    }

    private void loadVideoSnippet() throws IOException{
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

    private void updateVideoTitle() throws IOException{
        // set the local version of the title
        //video.getSnippet().setTitle(latestComment); // todo import title / thumbnail data

        video = auth.getService()
                .videos()
                .update("snippet", video.setId(VIDEO_ID))
                .setKey(auth.getDeveloperKey())
                .execute();

        log("Successfully updated the title.");
        //titles.add(latestComment);

        quotaUsage += 53;  // cost was most likely 53
    }

    private void updateVideoThumbnail() throws IOException{
        // set the local version of the title
        //video.getSnippet().setTitle(latestComment); // todo import title / thumbnail data
        // todo update request to change thumbnail

        video = auth.getService()
                .videos()
                .update("snippet", video.setId(VIDEO_ID))
                .setKey(auth.getDeveloperKey())
                .execute();

        log("Successfully updated the title.");
        //titles.add(latestComment);

        quotaUsage += 53;  // cost was most likely 53 todo RECALCULATE QUOTA USAGE
    }

    @Override
    public String getStatus() {
        // todo fill
        return "";
    }

    @Override
    public void run() {
        while (!stopped && !Thread.interrupted()) {
            // try to update the video title
            for(int tries = 0; tries < TRY_COUNT; tries++){
                try {
                    updateVideoTitle();
                    tries = TRY_COUNT;

                } catch (IOException e) {
                    if (tries == TRY_COUNT - 1){
                        log("Retry limit reached.  Video title could not be updated.");
                        log("Terminating the bot.");
                        System.exit(-1);
                    }
                    failures += 1;
                    sleep(RETRY_DELAY);
                }
            }

            // try to update the video title
            for(int tries = 0; tries < TRY_COUNT; tries++){
                try {
                    updateVideoThumbnail();
                    tries = TRY_COUNT;

                } catch (IOException e) {
                    if (tries == TRY_COUNT - 1){
                        log("Retry limit reached.  Video thumbnail could not be updated.");
                        log("Terminating the bot.");
                        System.exit(-1);
                    }
                    failures += 1;
                    sleep(RETRY_DELAY);
                }
            }
        }
    }
}
