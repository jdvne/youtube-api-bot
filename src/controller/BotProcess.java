package controller;

import controller.utils.Authorization;
import logger.FileLogger;

public abstract class BotProcess implements Runnable{
    Authorization auth;
    FileLogger logger;

    boolean stopped = false;
    int quotaUsage = 0;
    int failures = 0;

    BotProcess() {
        auth = new Authorization();
        logger = FileLogger.getInstance();
    }

    /**
     * Make this thread sleep for millis milliseconds.
     *
     * @param millis time in milliseconds to be slept
     */
    void sleep(long millis){
        log("Beginning sleep for " + millis + " ms.");

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log("Thread interrupted.");
            stopped = true;
            return;
        }

        log("Sleep ended.");
    }

    /**
     * Log a message using the provided Logger.
     * @param message message to be logged
     */
    void log(String message) {
        logger.log("(qu: " + quotaUsage + ") " + message);
    }

    /**
     * Returns a message relating to the status of the current process.
     *
     * @return String message
     */
    public abstract String getStatus();

    public int getQuotaUsage(){
        return quotaUsage;
    }
    public int getFailures(){
        return failures;
    }

}
