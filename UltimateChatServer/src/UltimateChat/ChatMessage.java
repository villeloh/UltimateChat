package UltimateChat;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
    Class for defining and creating ChatMessages. They have fields for sender and input (used for displaying the message body),
    channel (for filtering messages based on registered users on that channel), and two kinds of time-stamps, one for use
    in ChatConsole and the other, more 'raw' form for use in displaying msg histories correctly.
    @author Ville Lohkovuori
    09/2017
 */

public class ChatMessage {

    private String input;
    private User sender;
    private String consoleTimeStamp;
    private String clientTimeStamp;
    private static final SimpleDateFormat consoleFormat = new SimpleDateFormat ("d/M/y(kk:mm:ss)");
    private static final SimpleDateFormat clientFormat = new SimpleDateFormat ("kk:mm");
    private ChatChannel channel;
    private long postTime;
    private boolean isOwn;

    public ChatMessage(User sender, String input, ChatChannel givenChannel) {

        this.sender = sender;
        this.input = input;
        this.channel = givenChannel;
        this.postTime = System.currentTimeMillis(); // needed for displaying channels' message histories correctly (comparison with user's login-time to the channel)

        Date date = new Date(); // there might be a way to derive the date from postTime, but I'm not going to bother atm
        this.consoleTimeStamp = consoleFormat.format(date);
        this.clientTimeStamp = clientFormat.format(date);

        this.isOwn = false; // needed on the client-side to orient the chat-bubbles differently based on sender
    }

    public String getInput() {

        return this.input;
    }

    public String getConsoleTimeStamp() {

        return this.consoleTimeStamp;
    }

    public long getPostTime() {

        return this.postTime;
    }

    public ChatChannel getChannel() {

        return this.channel;
    }

    public String getSenderName() {

        return this.sender.getName();
    }

    public void setIsOwn(boolean ownStatus) {

        this.isOwn = ownStatus;
    }

    public boolean getIsOwn() {

        return this.isOwn;
    }

    // used as the standard format of printed-out messages.
    // further parsed by the client (as it stands, this is the only way to send info about users and msgs to the client,
    // so all the relevant fields of the msg object must be communicated, clumsy as it is)
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(this.clientTimeStamp);
        sb.append(" ");
        sb.append(this.isOwn);
        sb.append(" ");
        sb.append(this.sender.getName());
        sb.append(": ");
        sb.append(this.input);

        return sb.toString();
    } // end toString()
} // end class