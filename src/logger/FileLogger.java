package logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class FileLogger {
    public static FileLogger logger;
    private String fileName = "res/log.txt";
    private FileWriter fw;

    private FileLogger(){
        try {
            fw = new FileWriter(fileName);
        } catch (IOException e) {
            System.out.println("ERROR: An error occurred when opening the log file.");
        }
        log("Created logger instance.");
    }

    public static FileLogger getInstance(){
        if(logger == null){
            logger = new FileLogger();
        }

        return logger;
    }

    public void log(String message){
        try {
            fw.write(new Date() + " | " + message + "\n");
        } catch (IOException e) {
            System.out.println("ERROR: An error occurred when writing to the log file.");
        }
    }

    public void close(){
        try {
            fw.close();
        } catch (IOException e) {
            System.out.println("ERROR: An error occurred when closing the log file.");
        }
    }
}
