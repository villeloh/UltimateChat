package UltimateChat;

import java.util.ArrayList;
import java.util.LinkedList;

/*
    Class containing useful utility functions, for use in CommandInterpreter.
    @author Ville Lohkovuori
    09/2017
 */

public class Utils {

    // splices an input String into multiple pieces (based on spaces) and stores them in an array
    // used in CommandInterpreter, to parse the raw input coming in from the client
    public static String[] spliceInput(String input) {
        return input.split("\\s+");
    }

    // helper method for giving the available commands to the command list in CommandInterpreter
    public static ArrayList<String> loadCOMMAND_LIST(String COMMAND_SYMBOL) {

        ArrayList<String> list = new ArrayList<>();

        list.add(COMMAND_SYMBOL + "help");
        list.add(COMMAND_SYMBOL + "users");
        list.add(COMMAND_SYMBOL + "channels");
        list.add(COMMAND_SYMBOL + "messages");
        list.add(COMMAND_SYMBOL + "quit");
        list.add(COMMAND_SYMBOL + "channel");
        list.add(COMMAND_SYMBOL + "globalusers");
        list.add(COMMAND_SYMBOL + "logoff");
        list.add(COMMAND_SYMBOL + "tochan");
        list.add(COMMAND_SYMBOL + "makechan");
        list.add(COMMAND_SYMBOL + "delchan");
        list.add(COMMAND_SYMBOL + "user");

        return list;
    } // end loadCOMMAND_LIST()

    // helper method for adding the illegal characters/Strings that cannot be used in usernames
    // to the LinkedList ILLEGAL_USERNAME_PARTS in CommandInterpreter
    // NOTE: *smilies* can still be used... -.- It would be unacceptable in a real app, but I lack
    // the necessary time to pursue the issue.
    public static LinkedList<String> loadILLEGAL_LIST(String DEFAULT_USERNAME) {

        LinkedList<String> list = new LinkedList<>();
        list.add(";");
        list.add(":");
        list.add("?");
        list.add("!");
        list.add(" "); // for some reason this doesn't work... it takes the first part before the space as the username
        list.add("\"");
        list.add("'");
        list.add("#");
        list.add(",");
        list.add(".");
        list.add(DEFAULT_USERNAME);
        list.add(DEFAULT_USERNAME+"_");

        return list;
    } // end loadILLEGAL_LIST()

    // returns a channel join msg (for use in CommandInterpreter)
    public static String channelJoinMsg(String channelName) {

        return String.format(CommandInterpreter.CMD_TAG + " joinMsg " +"You have joined channel '%s'!\r\n", channelName);
    }

    // appends an underscore + number to the default user name, turning '(anon)' into '(anon)_1', and so on
    public static String appendSuffix(String userName) {

        String orig = userName;
        Integer suffix = 1;

        while (true) {

            String suffixString = "_" + suffix.toString();
            userName += suffixString;

            if (UserMap.getInstance().getuserMap().containsKey(userName)) {
                userName = orig;
                suffix++; // if the name is taken, increase the suffix
            }
            else {
                return userName;
            }
        }
    } // end appendSuffix()

    // sends an 'admin' message to everyone every time a user quits, logs off, or joins the channel
    // NOTE: the poster of the message appears as the user in question, not 'ADMIN'... it would be a
    // difficult fix due to the way that I've set things up.
    public static String adminMsg(int msgType, String userName, ChatChannel channel) {

        String msg = "";

        switch(msgType) {

            case 0: // quit

                msg = String.format("User %s has quit the channel! Goodbye!", userName);
                break;

            case 1: // log off

                msg = String.format("User %s has logged off and returned to the %s channel.", userName, channel.getChannelName());
                break;

            case 2: // joined channel

                msg = String.format("User %s has joined the channel! Welcome!", userName);
                break;

            case 3: // renamed user

                msg = String.format("User %s was renamed!", userName);
                break;
        }
        return msg;
    } // end adminMsg()
} // end class