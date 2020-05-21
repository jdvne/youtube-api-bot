package controller;

import com.google.api.services.youtube.model.Video;

import java.io.IOException;

public class ClickBaitSwap extends BotProcess{
    private final String VIDEO_ID = "put ID here";

    private final int MAX_TRY_COUNT = 3;

    private Video video;

    public ClickBaitSwap(){
        // todo uncomment: loadVideoSnippet(1);
    }

    private void jcabiTest() throws Exception {
        System.out.println("tried");
        throw new Exception();
    }

    // todo jcabi instead
    private void loadVideoSnippet(int tryNumber){
        if(tryNumber > MAX_TRY_COUNT){
            log("Retry limit reached.  Video snippet could not be loaded.");
            log("Terminating the bot.");
            System.exit(-1);
        }

        log("Attempting to get the video snippet...");
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
            log("Failed to get the video snippet.");
            tryNumber += 1;
            loadVideoSnippet(tryNumber);
        }
        log("Successfully got the video snippet.");

        quotaUsage += 3;  // cost was most likely 3
    }

    private void updateVideoTitle(int tryNumber){
        // todo stub
    }

    private void updateVideoThumbnail(int tryNumber){
        // todo stub
    }

    @Override
    public String getBlurb() {
        return null;
    }

    @Override
    public void run() {
        while (!stopped && !Thread.interrupted()) {
            try{
                jcabiTest();
            } catch (Exception e){
                System.out.println("failed");
                System.exit(-1);
            }

            //updateVideoTitle(1);
            //updateVideoThumbnail(1);
        }
    }
}
