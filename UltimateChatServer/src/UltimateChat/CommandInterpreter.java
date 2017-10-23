package UltimateChat;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

/*
    Class containing the main logic for interpreting input coming in from the clients.
    Each new client is allocated a separate CommandInterpreter instance,
    running on the server in their own dedicated threads.
    @author Ville Lohkovuori
    09/2017
 */

public class CommandInterpreter implements Observer, Runnable {

    public static final String CMD_TAG = "#?~*"; // needed to process returned command Strings on the client-side

    private static final int MAX_USERNAME_LENGTH = 12;
    private static final String COMMAND_SYMBOL = ":";
    private static final String DEFAULT_USERNAME = "(anon)";
    private static final String invalidCmdNotReg = CMD_TAG + " invalidNotReg " + "Invalid command (not a registered user).";
    private static final String invalidCmdBadCmd = CMD_TAG + " invalidTypOrNotExist " + "Invalid command (typo or doesn't exist).";
    private static final String invalidCmdNoArg = CMD_TAG + " invalidNoArg " + "Invalid command (no argument).";

    private static final LinkedList<String> ILLEGAL_USERNAME_PARTS = Utils.loadILLEGAL_LIST(DEFAULT_USERNAME);
    private static final ArrayList<String> COMMAND_LIST = Utils.loadCOMMAND_LIST(COMMAND_SYMBOL);

    private volatile boolean running = true; // used for stopping the thread that this instance of CI runs on

    private Scanner scn;
    private PrintStream strm;
    private User user;
    private ChatChannel defaultChan1 = new ChatChannel("General", null); // these could be final and/or static I guess, but I'm not taking any chances at this point
    private ChatChannel defaultChan2 = new ChatChannel("Games", null);
    private ChatChannel defaultChan3 = new ChatChannel("Humor", null);

    // the input and output streams are connected to their opposite counterparts on the side of the client,
    // enabling communication between the server and the client
    public CommandInterpreter(InputStream in, OutputStream out) {

        this.scn = new Scanner(in);
        this.strm = new PrintStream(out);

        ChannelMap.getInstance().addToChannelMap(this.defaultChan1.getChannelName(), defaultChan1); // must be done here, as it's not accepted outside of a method
        ChannelMap.getInstance().addToChannelMap(this.defaultChan2.getChannelName(), defaultChan2);
        ChannelMap.getInstance().addToChannelMap(this.defaultChan3.getChannelName(), defaultChan3);

        ChatHistory.getInstance().register(this); // register commandInterpreter as an observer of ChatHistory

        this.user = new User(Utils.appendSuffix(DEFAULT_USERNAME), defaultChan1, 1); // result: '(anon)_1', '(anon)_2', and so on
        UserMap.getInstance().addUserToMap(this.user);
    }

    // The main thread loop that waits for the user's input and calls for it to be interpreted
    @Override
    public void run() {

        postMsg(Utils.adminMsg(2, this.user.getName(), defaultChan1), 2); // post admin (join) message for the whole channel to see
        strm.println(Utils.channelJoinMsg(defaultChan1.getChannelName())); // print out a join msg to yourself

        while(running) {

            if (scn.hasNext()) {

                String input = scn.nextLine();
                processInput(input, this.user.getReg());
            }
            else { // connection interrupted
                strm.println(CMD_TAG + " quit " + "Quitting...");
                postMsg(Utils.adminMsg(0, this.user.getName(), defaultChan1), 2); // post admin (quit) message for the whole channel to see
                UserMap.getInstance().removeUser(this.user.getName());
                ChatHistory.getInstance().deregister(this);
                running = false; // stops the thread
            }
        } // end while-loop
    } // end run()

    // deals with the raw input and determines whether it's a message, valid command, or an invalid command.
    // takes into account the user's regstate (entered as an argument)
    private void processInput(String input, int state) {

        if (input.isEmpty()) { // empty lines do nothing in any state

            return;
        }

        String[] splices = Utils.spliceInput(input); // separate the input (based on spaces) and put the resulting chunks in an array
        String cmd = splices[0];
        String arg;
        boolean hasArg;

        if (splices.length > 1) {

            arg = splices[1];
            hasArg = true;
        }
        else {
            arg = null;
            hasArg = false;
        }

        if (!COMMAND_LIST.contains(cmd)) { // message

                postMsg(input, state);
        }
        else { // command
            if (legalCmd(cmd, arg, state, hasArg)) { // validate command
                execCmd(cmd, arg); // execute if valid
            }
        }
    } // end processInput()

    // posts a message for the other clients to see
    private void postMsg(String msg, int state) {

        switch(state) {

            // The cases should be identical, but after some wonky behaviour, I'm not taking any chances with intentional fall-through.

            case 1: // 1 = not registered, logged on to the General channel as anonymous user

                ChatMessage message = new ChatMessage(this.user, msg, this.user.getChannel()); // create new msg
                ChatHistory.getInstance().insert(message); // add msg to history
                ChatHistory.getInstance().notifyObservers(message); // notify all registered observers of the msg
                break;

            case 2: // 2 = registered

                ChatMessage msge = new ChatMessage(this.user, msg, this.user.getChannel()); // create new msg
                ChatHistory.getInstance().insert(msge); // add msg to history
                ChatHistory.getInstance().notifyObservers(msge); // notify all registered observers of the msg
                break;
        }
    } // end postMsg()

    // determines whether or not the command should be executed.
    // if not, it prints out error msgs of various kinds.
    private boolean legalCmd(String cmd, String arg, int state, boolean hasArg) {

        int cmdInd = COMMAND_LIST.indexOf(cmd); // a way to get around the fact that only constant expressions may be used in switch statements
        boolean result = true;

        switch(cmdInd) {

            // (the following 6 commands are always successful, but are included here for completeness' sake)
            case 0: // help
            case 1: // list of users on the channel
            case 2: // list of channels
            case 3: // channel message history
            case 4: // quit
            case 5: // which channel the user is on
                break;

            case 6: // list of all users across channels

                if (state != 2) {
                    strm.println(invalidCmdNotReg);
                    result = false;
                }
                break;

            case 7: // log off the user

                 if (this.user.getName().contains(DEFAULT_USERNAME+"_")) {

                    strm.println(CMD_TAG + " anonLogoff " + "Failed! You may not log off as an anonymous user.");
                    result = false;
                }
                else if (state != 2) {

                    strm.println(invalidCmdNotReg); // should not be needed, but it's there just in case...
                    result = false;
                }
                break;

            case 8: // switch to a different channel

                if (state != 2) {

                    strm.println(invalidCmdNotReg);
                    result = false;
                }
                else if (!hasArg) {

                    strm.println(invalidCmdNoArg);
                    result = false;
                }
                else if (ChannelMap.getInstance().channelAvailable(arg)) {

                    strm.println(CMD_TAG + " noSuchChannel " + "Failed to join! No such channel exists at the moment.");
                    result = false;
                }
                else if (arg.equals(this.user.getChannel().getChannelName())) {

                    strm.println(String.format(CMD_TAG + " alreadyOnChannel " + "Failed to join! Already on channel '%s'.", arg));
                    result = false;
                }
                break;

            case 9: // make new channel

                if (state != 2) {

                    strm.println(invalidCmdNotReg);
                    result = false;
                }
                else if (!hasArg) {

                    strm.println(invalidCmdNoArg);
                    result = false;
                }
                else if (!ChannelMap.getInstance().channelAvailable(arg)) {

                    strm.println(CMD_TAG + " channelExists " + "Channel already exists! Please enter a different name.");
                    result = false;
                }
                break;

            case 10: // delete channel

                ChatChannel toBeDeleted = ChannelMap.getInstance().getChannelAt(arg);

                if (state != 2) {

                    strm.println(invalidCmdNotReg);
                    result = false;
                }
                else if (!hasArg) {

                    strm.println(invalidCmdNoArg);
                    result = false;
                }
                else if (ChannelMap.getInstance().channelAvailable(arg)) {

                    strm.println(CMD_TAG + " failDelNoSuchChannel " + "Failed to delete! No such channel exists at the moment.");
                    result = false;
                }
                else if (!this.user.equals(toBeDeleted.getCreator())) {

                    strm.println(CMD_TAG + " failDelNotChanCreator " + "Failed! You may only delete channels that you have created!");
                    result = false;
                }
                break;

            case 11: // make new user (or switch the name of an existing one)

                if (!UserMap.getInstance().userAvailable(arg)) {

                    strm.println(CMD_TAG + " failUsernameTaken " + "Failed! Username already taken; please enter a different one.");
                    result = false;
                }
                else if (!hasArg) {

                    strm.println(CMD_TAG + " failNoName " + "Failed! Please specify a name for the new user.");
                    result = false;
                }
                else if (arg.length() > MAX_USERNAME_LENGTH) {

                    strm.println(String.format(CMD_TAG + " failNameTooLong " + "Failed! Username may not exceed %s characters in length.", MAX_USERNAME_LENGTH));
                    result = false;
                }
                else if (ILLEGAL_USERNAME_PARTS.contains(arg)) { // the formatting is not perfect, but I won't spend more time on this

                    strm.println(String.format(CMD_TAG + " failIllegalChars " + "Failed! Username may not contain the following characters: %s", ILLEGAL_USERNAME_PARTS.toString().substring(1,ILLEGAL_USERNAME_PARTS.toString().length()-1)));
                    result = false;
                }
                break;

            default:

                strm.println(invalidCmdBadCmd); // should never happen, but just in case
                result = false;
                break;
        } // end switch-statement
        return result;
    } // end legalCmd()

    // executes the command. since the validation is already taken care of, this should always succeed
    private void execCmd(String cmd, String arg) {

        int indx = COMMAND_LIST.indexOf(cmd);

        switch(indx) {

            case 0: // help

                // used to print out all available commands, but as it's useless with the visual interface, I deleted the help msg
                break;

            case 1: // list all users on channel

                strm.println(CMD_TAG + " users " + UserMap.getInstance().channelUsers(this.user.getChannel().getChannelName()));
                break;

            case 2: // list all available channels (plus the user's current channel; this is useful on the client-side)

                strm.println(CMD_TAG + " channels " + this.user.getChannel().getChannelName() + " " + ChannelMap.getInstance().toString());
                break;

            case 3: // show channel history (messages)

                strm.println(CMD_TAG + " messages " + "Messages:" + ChatHistory.getInstance().channelMsgs(this.user.getChannel(), this.user));
                break;

            case 4: // quit

                strm.println(CMD_TAG + " quit " + "Quitting...");
                postMsg(Utils.adminMsg(0, this.user.getName(), defaultChan1), 2); // post admin message for the whole channel to see
                UserMap.getInstance().removeUser(this.user.getName());
                ChatHistory.getInstance().deregister(this);
                running = false; // stops the thread
                strm.close(); // not sure if needed, but I saw it recommended to close down I/O separately
                scn.close();
                break;

            case 5: // print out the name of the current channel

                strm.println(String.format(CMD_TAG + " channel " + "Currently on channel '%s'.", this.user.getChannel().getChannelName()));
                break;

            case 6: // list all users (globally)

                strm.println(CMD_TAG + " globalusers " + UserMap.getInstance().toString());
                break;

            case 7: // log off (deregister user)

                postMsg(Utils.adminMsg(1, this.user.getName(), defaultChan1), 2); // post admin message for the whole channel to see
                UserMap.getInstance().removeUser(this.user.getName());
                this.user = new User(Utils.appendSuffix(DEFAULT_USERNAME), defaultChan1, 1);
                UserMap.getInstance().addUserToMap(this.user);
                strm.println(String.format(CMD_TAG + " logoff " + "Logged off! Returning to channel '%s' as an anonymous user.", defaultChan1.getChannelName()));
                break;

            case 8: // switch channel

                postMsg(Utils.adminMsg(0, this.user.getName(), defaultChan1), 2); // post admin (quit) message for the whole channel to see
                String oldChannel = this.user.getChannel().getChannelName();
                this.user.setChannel(ChannelMap.getInstance().getChannelAt(arg));
                postMsg(Utils.adminMsg(2, this.user.getName(), defaultChan1), 2); // post admin (join) message for the whole channel to see
                strm.println(String.format(CMD_TAG + " tochan " + "Switched channel from '%s' to '%s'!", oldChannel, this.user.getChannel().getChannelName()));
                break;

            case 9: // make new channel

                ChatChannel newChan = new ChatChannel(arg, this.user);
                ChannelMap.getInstance().addToChannelMap(arg, newChan);
                strm.println(String.format(CMD_TAG + " makechan " + "New channel %s created!", arg));
                this.user.setChannel(newChan);
                postMsg(Utils.adminMsg(2, this.user.getName(), defaultChan1), 2); // post admin message (only you will see it, but just for completeness' sake)
                strm.println(Utils.channelJoinMsg(newChan.getChannelName())); // print out a join msg
                break;

            case 10: // delete channel

                for (String usrName : UserMap.getInstance().getuserMap().keySet()) {

                    if (UserMap.getInstance().getUser(usrName).getChannel().getChannelName().equals(arg)) {
                        UserMap.getInstance().getUser(usrName).setChannel(defaultChan1); // move users back to General channel if deleting the channel that they're currently on
                        strm.println(String.format("Channel '%s' was deleted! Moved user '%s' back to channel '%s'.", arg, usrName, defaultChan1.getChannelName()));
                    }
                }
                ChannelMap.getInstance().removeChannel(arg);
                strm.println(String.format(CMD_TAG + " delchan " + "Deleted channel '%s'!", arg));
                break;

            case 11: // make new user (doing this again will rename your current user)

                ChatChannel oldChan = this.user.getChannel();
                UserMap.getInstance().removeUser(this.user.getName()); // remove current user

                if (this.user.getReg() != 2) {

                    strm.println(String.format(CMD_TAG + " user " + "New user ('%s') created!", arg));
                    postMsg(Utils.adminMsg(3, this.user.getName(), defaultChan1), 2); // msg about X changing their name
                    postMsg(Utils.adminMsg(2, arg, defaultChan1), 2); // msg that says Y (X's new name) has joined the channel
                    this.user = new User(arg, defaultChan1, 2); // new users always get moved to the general channel
                }
                else {
                    strm.println(String.format(CMD_TAG + " user " + "Changed username to '%s'!", arg));
                    postMsg(Utils.adminMsg(3, this.user.getName(), defaultChan1), 2); // msg about X changing their name
                    postMsg(Utils.adminMsg(2, arg, defaultChan1), 2); // msg that says Y (X's new name) has joined the channel
                    this.user = new User(arg, oldChan, 2);
                }
                UserMap.getInstance().addUserToMap(this.user);
                break;
        } // end switch-statement
    } // end execCmd()

    // updates the view of the client with posted new msgs (if the user's channel is the same as the msg's)
    // called every time a new msg is posted (on any client) due to the Observer pattern
    public void update(ChatMessage msg) {

        if (this.user.getChannel().getChannelName().equals(msg.getChannel().getChannelName())) {

            if (this.user.getName().equals(msg.getSenderName())) {

                msg.setIsOwn(true);
            }
            else {
                msg.setIsOwn(false); // SHOULD NOT BE NEEDED! For some reason, msgs are being fed to update() that have their isOwn set to 'true' by default, even though this should be impossible !!!
            }
            strm.println(msg.toString());
        } // end outer if-statement
    } // end update()
} // end class