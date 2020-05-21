package ui;

import controller.BotProcess;
import controller.ClickBaitSwap;
import controller.LatestCommentToTitle;

import java.util.Scanner;

public class CommandLineUI {
    private BotProcess botProcess;
    private Thread thread;
    private Scanner in;

    private boolean running;

    private String title =
                    " __  __         ______     __         ___   ___  ____  ___       __ \n" +
                    " \\ \\/ /__  __ _/_  __/_ __/ /  ___   / _ | / _ \\/  _/ / _ )___  / /_\n" +
                    "  \\  / _ \\/ // // / / // / _ \\/ -_) / __ |/ ___// /  / _  / _ \\/ __/\n" +
                    "  /_/\\___/\\_,_//_/  \\_,_/_.__/\\__/ /_/ |_/_/  /___/ /____/\\___/\\__/ \n" +
                    "                                                                    ";

    public CommandLineUI(){
        in = new Scanner(System.in);
    }

    public void start(){
        println(title);
        displayBotProcessOptions();
        processBotProcessSelection(getIntegerInput(1, 2));
        runProcess();
    }

    private void displayBotProcessOptions(){
        println("Which bot process would you like to use?");
        println(" 1] LatestCommentToTitle");
        println(" 1] ClickBaitSwap");
    }

    /**
     * prompt user to choose a choice until a correct one is provided
     * @return
     */
    private int getIntegerInput(int low, int high){
        boolean isSelectionValid = false;
        int selection = -1;

        while(!isSelectionValid){
            String input = in.nextLine().trim();

            try{
                selection = Integer.parseInt(input);

                if(selection >= low && selection <= high){
                    isSelectionValid = true;
                } else {
                    println("Input must be a number from " + low + " to " + high + ".  Try again.");
                }
            } catch (NumberFormatException e){
                println("Input must be a number.  Try again.");
            }
        }

        return selection;
    }

    /**
     * select BotProcess depending upon user selection
     * @param selection
     */
    private void processBotProcessSelection(int selection){
        switch(selection) {
            case 1:
                botProcess = new LatestCommentToTitle();
                break;
            case 2:
                botProcess = new ClickBaitSwap();
                break;
        }
    }

    /**
     * select BotProcess depending upon user selection
     * @param selection
     */
    private void processRuntimeSelection(int selection){
        switch(selection) {
            case 0:
                println("Sending an interrupt signal to the bot.");
                thread.interrupt();

                try {
                    thread.join();
                } catch (InterruptedException ignored){ }

                println("The bot was successfully interrupted.");
                break;
            case 1:
                println("Quota usage: " + botProcess.getQuotaUsage() + " units.");
                break;
            case 2:
                println(botProcess.getBlurb());
                break;
        }
    }

    private void displayRuntimeOptions(){
        println("\nThe bot is currently running...");
        println("What would you like to do?");
        println(" 1] See the current quota usage.");
        println(" 2] Get a status blurb from the bot.");
        println("");
        println(" 0] Terminate the bot.");
    }

    private void runProcess(){
        // begin bot process thread
        thread = new Thread(botProcess);
        thread.start();

        while(thread.isAlive()){
            displayRuntimeOptions();
            processRuntimeSelection(getIntegerInput(0, 2));
        }
    }

    private void println(String message){
        System.out.println(message);
    }
}
