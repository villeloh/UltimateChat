package UltimateChat;

/*
    Class for defining and creating user objects.
    Users have a name (that makes them unique), are present on a particular channel and are either registered or not.
    The time of entering a channel is also stored, for use in ChatHistory when printing out a channel's message history.
    @author Ville Lohkovuori
    09/2017
 */

public class User {

    private String userName;
    private ChatChannel channel;
    private int regState;
    private long channelJoinTimeStamp;

    public User(String givenUserName, ChatChannel givenChannel, int state) {

        this.userName = givenUserName;
        this.channel = givenChannel;
        this.regState = state;
        this.channelJoinTimeStamp = System.currentTimeMillis(); // needed to deal with channel history (to only display the messages that have been posted since the user joined)
    }

    // Legal regstates:
    // 1 = not registered
    // 2 = registered user

    public int getReg() {

        return this.regState;
    }

    public void setReg(int state) {

        this.regState = state;
    }

    public long getTimeStamp() {

        return this.channelJoinTimeStamp;
    }

    public String getName() {

        return this.userName;
    }

    public void setName(String newName) {

        this.userName = newName ;
    }

    public ChatChannel getChannel() {

        return this.channel;
    }

    public void setChannel(ChatChannel givenChannel) {

        this.channel = givenChannel;
        this.channelJoinTimeStamp = System.currentTimeMillis(); // time-stamp is updated each time the user joins a new channel
    }
} // end class