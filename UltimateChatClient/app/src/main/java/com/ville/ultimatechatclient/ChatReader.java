package com.ville.ultimatechatclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;
import android.util.Log;

/*
    A class for connecting to the server and reading data from it (and sending it on to MainActivity).
    @author Ville Lohkovuori
    10/2017
 */

public class ChatReader implements Runnable {

    private InputStream in;
    private MainActivity mainActivity;
    private Scanner scn;
    private Socket socket;
    private volatile boolean running = true;
    private boolean connected = false;
    private ChatWriter writer;

    public ChatReader(MainActivity mActiv) {

        this.mainActivity = mActiv;
    }

    @Override
    public void run() {

        // makes a connection and waits for data (bytestream converted to String) from the server.
        // strm.println() call on the server == send the String here (as a bytestream)

        while(running) {

            while (!connected) {

                try {
                    this.socket = new Socket("192.168.43.197", 55000); // laptop: 197; desktop: 242
                    this.in = this.socket.getInputStream();
                    this.scn = new Scanner(this.in);
                    this.writer = new ChatWriter(this.socket);
                    Thread writeThread = new Thread(this.writer);
                    writeThread.start();;
                    connected = true;
                }
                catch (IOException e) {

                    Log.d("error", "Failed to connect to the server (reader)!");
                }
            } // end while-loop

            String dataString = readDataFromServer();

            if (!dataString.isEmpty()) {

                String[] splices = Utils.spliceInput(dataString);

                if (Utils.CMD_TAG.equals(splices[0]) && splices.length > 1) {

                    String cmd = splices[1]; // the actual command msg (e.g. 'users')
                    String[] cmdBody = new String[splices.length-2];

                    for (int i = 0; i < cmdBody.length; i++) {
                        cmdBody[i] = splices[i+2]; // exclude the command tag + command msg (e.g. '#?~* users')
                    }
                    mainActivity.updateUI(cmd, cmdBody);
                }
                else {
                    ChatMessage msg = new ChatMessage(splices);
                    mainActivity.updateScreen(msg);
                }
            } // end empty-check
        } // end outer while-loop
    } // end run()

    private String readDataFromServer() {

        if (!scn.hasNext()) {
            connected = false; // try to make a new connection if the current one is interrupted
            return "";
        }
        else {

            return scn.nextLine();
        }
    } // end readDataFromServer()

    // it's pretty awkward to have the reader create the writer and refer to it, but
    // it prevents an issue where two connections get opened (one for each time the reader and the writer connect)
    public ChatWriter getWriter() {

        return this.writer;
    }

    // used to close the thread from outside the instance.
    // (something else may be needed as well, but the tutorials about
    // threads that I found were more confusing than helpful)
    public void terminate() {

        this.running = false;
    }
} // end class