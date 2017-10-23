package UltimateChat;

import java.util.HashSet;
import java.util.LinkedList;

/*
    Singleton class for keeping track of all the posted messages, and informing registered observers
    of them as needed. (In retrospect, a dedicated history for each channel might've been a better solution
    than constantly filtering the one giant, common history.)
    @author Ville Lohkovuori
    09/2017
 */

public class ChatHistory implements Observable {

    private LinkedList<ChatMessage> msgList; // a LinkedList because there's no need to deal with the stored items individually
    private HashSet<Observer> registeredList;
    private static ChatHistory ourInstance = new ChatHistory();

    private ChatHistory() {

        this.msgList = new LinkedList<ChatMessage>();
        this.registeredList = new HashSet<Observer>();
    }

    public static ChatHistory getInstance() {

        return ourInstance;
    }

    // add a new message into the history
    public void insert(ChatMessage msg) {

        this.msgList.add(msg);
    }

    // register an object as an observer that gets updates whenever a msg is added to the history
    public void register(Observer e) {

        this.registeredList.add(e);
    }

    // deregister an observer (only called when quitting the program)
    public void deregister(Observer e) {

        this.registeredList.remove(e);
    }

    // whenever someone posts a new msg (adding it to the history), all clients get updated so they can view that new msg
    public void notifyObservers(ChatMessage msg) {

        for (Observer e : this.registeredList) {
            e.update(msg);
        }
    }

    // prints out a nicely formatted list of all msgs
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (!this.msgList.isEmpty()) {

            for (int i = 0; i < this.msgList.size(); i++) {
                sb.append(this.msgList.get(i).toString()); // contains both username and msg
                sb.append("\r\n");
            }
        }
        return sb.toString();
    } // end toString()

    // prints out a nicely formatted list of msgs, specific to the given channel and user (in practice, called with this.user)
    public String channelMsgs(ChatChannel channel, User user) {

        StringBuilder sb = new StringBuilder();

        if (!this.msgList.isEmpty()) {

            for (int i = 0; i < this.msgList.size(); i++) {

                // the user must be present on the channel where the msg was posted, and it must've been posted after s/he joined the channel
                if(this.msgList.get(i).getChannel().getChannelName().equals(channel.getChannelName()) && this.msgList.get(i).getPostTime() >= user.getTimeStamp()) {

                    sb.append(this.msgList.get(i).toString()); // contains both username and msg
                    sb.append("\r\n");
                }
            } // end for-loop
        } // end empty-check
        return sb.toString();
    } // end channelMsgs()
} // end class