package UltimateChat;

import java.util.HashMap;

/*
    Singleton class for storing created ChatChannels in a HashMap. Used for switching, creating and deleting channels in CommandInterpreter.
    @author: Ville Lohkovuori
    09/2017
 */

public class ChannelMap {

    private static ChannelMap ourInstance = new ChannelMap();
    private HashMap<String, ChatChannel> map;

    private ChannelMap() {

        this.map = new HashMap<>();
    }

    public static ChannelMap getInstance() {

        return ourInstance;
    }

    // checks whether a given channel already exists or not
    public boolean channelAvailable(String channelName) {

        return !this.map.containsKey(channelName);
    } // end channelAvailable()

    public ChatChannel getChannelAt(String key) {

        return this.map.get(key);
    }

    // add channel to the map (first its uniqueness should be ensured by calling channelAvailable())
    public void addToChannelMap(String channelName, ChatChannel chn) {

        this.map.put(channelName, chn); // the name of the channel is the key, while the channel object is the value
    }

    public void removeChannel(String key) {

        this.map.remove(key);
    }

    // prints out the keys of the map, i.e., the names of the channels
    @Override
    public String toString() {

        String str =  this.map.keySet().toString();

        str = str.substring(1, str.length()-1); // get rid of the brackets at the beginning and end of the String
        str = str.replace(",", ""); // get rid of the colons

        String[] array = Utils.spliceInput(str);
        StringBuilder sb = new StringBuilder();

        // search for the String 'General' and put it first in the returned String.
        // (This is needed to format the channel list correctly on the client-side)
        for (int i = 0; i < array.length; i++) {

            if (array[i].equals("General")) {
                sb.append(array[i]);
                sb.append(" ");
                break;
            }
        }

        // add the rest of the Strings
        for (int i = 0; i < array.length; i++) {

            if (!array[i].equals("General")) {
                sb.append(array[i]);
                sb.append(" ");
            }
        }
        return sb.toString();
    } // end toString()
} // end class