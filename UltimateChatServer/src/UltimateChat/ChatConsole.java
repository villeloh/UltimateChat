package UltimateChat;

/*
    Class for posting all new messages to the system console with a time-stamp.
    @author Ville Lohkovuori
    09/2017
 */

public class ChatConsole implements Observer {

    public ChatConsole() {

        ChatHistory.getInstance().register(this); // register as an observer of ChatHistory
    }

    // updates the console with new msgs + timestamps
    public void update(ChatMessage msg) {

        System.out.println("@" + msg.getConsoleTimeStamp() + " " + msg.getSenderName() + ": " + msg.getInput());
    }
} // end class