package UltimateChat;

import java.util.HashMap;

/*
    Singleton class for keeping a map of usernames + user objects, to ensure that users are globally unique.
    Also contains methods for printing out a list of users, both globally and on a channel-basis.
    @author Ville Lohkovuori
    09/2017
 */

public class UserMap {

    private static UserMap ourInstance = new UserMap();
    private HashMap<String, User> userMap;

    private UserMap() {

        this.userMap = new HashMap<String, User>();
    }

    public static UserMap getInstance() {

        return ourInstance;
    }

    // adds a given user to the usermap
    public void addUserToMap(User user) {

        this.userMap.put(user.getName(), user);
    }

    // removes a user from the map
    public void removeUser(String userName) {

        this.userMap.remove(userName);
    }

    // checks whether a given username is available
    public boolean userAvailable(String userName) {

        return !this.userMap.containsKey(userName);
    }

    // returns the user object based on its name
    public User getUser(String key) {

        return this.userMap.get(key);
    }

    // returns the entire, inner HashMap of usernames and users
    public HashMap<String, User> getuserMap() {

        return this.userMap;
    }

    // prints out all the users on a particular channel
    public String channelUsers(String channelName) {

        StringBuilder sb = new StringBuilder();
        String result = "";

        if (!this.userMap.isEmpty()) { // not sure if needed, but it doesn't hurt

            for (String key : this.userMap.keySet()) {

                if (this.userMap.get(key).getChannel().getChannelName().equals(channelName)) {

                    sb.append(this.userMap.get(key).getName()); // append username at key (could append the key itself, but it seems wrong somehow)
                    sb.append(" ");
                }
            }
            result = sb.substring(0, sb.length()-1)+"\n"; // remove superfluous space from the end
        }
        return result;
    } // end channelUsers()

    // prints out all the users, across all channels
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        String result = "";

        if (!this.userMap.isEmpty()) {

            for (String key : this.userMap.keySet()) {
                sb.append(this.userMap.get(key).getName());
                sb.append(" ");
            }
            result = sb.substring(0, sb.length()-1)+"\n";
        }
        return result;
    } // end toString()
} // end class