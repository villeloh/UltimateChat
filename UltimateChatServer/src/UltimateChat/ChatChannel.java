package UltimateChat;

/*
    Class for defining and creating channel objects.
    The main purpose of channels is to filter msg input based on which users have
    registered on the channel.
    Channels have a name field that renders them unique, as well as
    a creator that's needed for deleting them (only its creator can delete a channel).
    @author Ville Lohkovuori
    09/2017
 */

public class ChatChannel {

    private String channelName;
    private User creator;

    public ChatChannel(String name, User usr) {

        this.channelName = name;
        this.creator = usr;
    }

    public User getCreator() {

        return this.creator;
    }

    public String getChannelName() {

        return this.channelName;
    }
    // channels could be renamed with a set method, I suppose, but mayhem might easily ensue, so I opted against it

 /*
    NOTE: I added channel as a field to User objects to mark which channel the user is on.
    I could've done it the other way around; each Channel object could have a list
    of users that are registered as observers of that channel, and therefore receive all messages
    that are posted by other registrants. I'm not sure which approach is superior, if either,
    but  in retrospect the latter one seems more natural. Oh well.
  */

} // end class