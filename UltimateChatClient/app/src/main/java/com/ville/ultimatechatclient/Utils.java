package com.ville.ultimatechatclient;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;

/*
    Class containing various useful utility functions (for use in MainActivity).
    @author Ville Lohkovuori
    10/2017
 */

public class Utils {

    public final static String CMD_TAG = "#?~*"; // it may be bad form, but as it is final, I figured it can be public as well

    public static HashMap<String, Integer> userColors = new HashMap<>(); // a trillion billion getters and other methods are needed with these as private... sue me! :p
    public static ArrayList<Integer> freeColors = loadColorList();

    // used for loading up freeColors with the available user colors in MainActivity
    public static ArrayList<Integer> loadColorList() {

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(R.color.colorCLightBlue);
        colors.add(R.color.colorCLightBlue2);
        colors.add(R.color.colorCLightBlue3);
        colors.add(R.color.colorCRed);
        colors.add(R.color.colorCRed2);
        colors.add(R.color.colorCRed3);
        colors.add(R.color.colorCYellow);
        colors.add(R.color.colorCYellow2);
        colors.add(R.color.colorCYellow3);
        colors.add(R.color.colorCPaleGreen);
        colors.add(R.color.colorCPaleGreen2);
        colors.add(R.color.colorCPaleGreen3);

        return colors;
    } // end loadColorList()

    // removes the first item from a given array of Strings.
    // needed for properly formatting the channel list after opening the side drawer
    public static String[] trimFirstItem(String[] orig) {

        String[] newArr = new String[orig.length-1];

        for (int i = 0; i < newArr.length; i++) {
            newArr[i] = orig[i+1];
        }
        return newArr;
    } // end trimFirstItem()

    // splices a String into multiple pieces (based on spaces) and stores them in an array
    public static String[] spliceInput(String input) {
        return input.split("\\s+");
    }

    // converts a String array to a String
    public static String arrToStr(String[] arr) {

        StringBuilder sb = new StringBuilder();

        for (String s : arr) {
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString().trim();
    } // end arrToStr()

    // rearranges the command body that's returned by the ':channels' command to the correct
    // order, so that the CustomSpinnerAdapter can set channels correctly for the channelSpinner
    public static String[] formatToChannelArray(String[] cmdBody) {

        String userChannel = cmdBody[0]; // this gets sent with the list of channels, to ease the following operations

        // make it so that the channel that the user is on (on the server-side) gets put to the first place
        // of the final channel array that is to be fed to the ArrayAdapter
        for (int i = 1; i < cmdBody.length; i++) {

            if (cmdBody[i].equals(userChannel)) {
                String temp = cmdBody[1];
                cmdBody[1] = userChannel; // instead of userChannel, it could be: cmdBody[i]
                cmdBody[i] = temp;
                break;
            }
        }
        return Utils.trimFirstItem(cmdBody); // the array now contains no duplicate elements (userChannel is present twice without this operation)
    } // end formatToChannelArray()

    // for posting Toast messages (for feedback on the user's actions)
    public static void toast(String text, Context context) {

        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 200);
        toast.show();
    } // end toast()

    // initial welcome msg (part 1). Due to the 'newLine == new msg' issue, I decided to do these on the client-side
    public static String startWelcomeMsg() {

        return "Welcome to UltimateChat (by Ville L.)!";
    }

    // 2nd part of the initial welcome msg (I thought it looks neater with two ChatBubbles)
    public static String middleWelcomeMsg() {

        return "If you wish to register a new user, click on the menu in the upper right corner " +
                "(the three dots), and choose 'Register user'. Without registering, you may only chat on the General " +
                "channel as an anonymous user, and you won't be able to make or delete new channels, nor see the global list of users.";
    }

    // 3rd part of the initial welcome msg
    public static String endWelcomeMsg() {

        return "If you need help with the chat interface, choose 'Help' from the menu. Otherwise, happy chatting! ^^";
    }

    // it's just a long String, basically, but I put it here to have it out of the way
    // and not have to deal with xml stuffs
    public static String helpBlurb() {

        return "Here are the CONTROLS for UltimateChat:\n" +
                "\n" +
                "UPPER RIGHT-HAND CORNER MENU\n" +
                "\n" +
                " * To register a new user (or rename an existing one), click on 'Register user' / 'Rename user'.\n" +
                "\n" +
                " * 'Log off' will unregister your user and return you to the General " +
                "channel as an anonymous user. (You may re-register at any time.)\n" +
                "\n" +
                " * Choosing 'Help' will bring up this list of controls.\n" +
                "\n" +
                " * Choose 'Quit' to quit UltimateChat.\n" +
                "\n" +
                "SIDE-DRAWER CHANNEL MENU\n" +
                "\n" +
                "In order to bring up the Channel Menu, drag with your finger from the left edge of the screen towards the center.\n" +
                "\n" +
                "If you're not registered, you will only be able to see the channel you're on ('General') and a list of users that are " +
                "on the channel. Upon registering, a host of new options become available:\n" +
                "\n" +
                " * You may switch channels by clicking on the channel spinner and choosing a new channel.\n" +
                "\n" +
                " * You may create a new channel or delete one with the 'Create channel' or 'Delete channel' buttons, respectively. " +
                "(Note that you may only delete channels that you have created.)\n" +
                "\n" +
                " * Finally, clicking 'Global View' will bring up a list of users across all channels. " +
                "Return to the Channel View by clicking the corresponding button.";
    } // end helpBlurb()
} // end class