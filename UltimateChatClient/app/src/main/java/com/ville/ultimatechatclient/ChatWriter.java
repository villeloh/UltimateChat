package com.ville.ultimatechatclient;

import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedTransferQueue;

/*
    A class that sends data to the server (for it to interpret and respond back).
    @author Ville Lohkovuori
    10/2017
 */

public class ChatWriter implements Runnable {

    private OutputStream out;
    private LinkedTransferQueue<String> queue;
    private volatile boolean running = true;
    private volatile boolean connected = false;
    private Socket socket;

    public ChatWriter(Socket socket) {

        try {
            this.socket = socket;
            this.out = this.socket.getOutputStream();
            this.queue = new LinkedTransferQueue<>();
        }
        catch (IOException e) {

            Log.d("error", "Failed to connect to server (writer)!");
        }
    } // end constructor

    @Override
    public void run() {

        // sends raw data (String converted to bytestream) from the client to the server
        // scn.nextLine() call on the server receives the data

        while(running) {

                String data = "";

                try {
                    data = queue.take();
                }
                catch (InterruptedException e) {

                    Log.d("error", "failed to take data from queue!");
                    continue; // hopefully this never happens... I don't see how it could end in anything but disaster
                }

                try {
                    if (!data.isEmpty()) {

                        this.out.write(data.getBytes());
                        Log.d("wrotedata", "wrote data to server");
                    }
                }
                catch (IOException e) {

                    Log.d("error", "Failed to write data to server!");
                    // ISSUE: the data was taken earlier from the queue --
                    // meaning that if this operation fails, it's gone for good! o_O
                    // not sure how to solve this...
                }
        } // end while-loop
    } // end run()

    // technically, this method doesn't do what it says, but this name is clearer in
    // MainActivity, and it is what happens in the final end (if all goes well)
    public void writeDataToServer(String data) {

        queue.add(data);
    }

    // used to close the thread from outside the instance
    public void terminate() {

        this.running = false;
    }
} // end class