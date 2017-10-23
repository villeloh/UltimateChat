package UltimateChat;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/*
    Class for defining and starting a server.
    @author Ville Lohkovuori
    09/2017
 */

public class ChatServer {

    private ServerSocket serverSocket;
    private int portNum;
    private ChatConsole console;
    private Thread userThread;

    public ChatServer() {
    }

    // continues to listen for new connection attempts;
    // if one is successful, starts a new instance of the CommandInterpreter
    public void serve() {

        try {
            this.serverSocket = new ServerSocket(55000, 2);
            this.portNum = this.serverSocket.getLocalPort();
            this.console = new ChatConsole(); // it has to be created somewhere, and CommandInterpreter didn't seem like the right place for it

            while (true) {

                System.out.println("Listening on port # " + this.portNum + "\n");
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection established to client at " + clientSocket.getInetAddress() + " ... Starting program...\n");

                CommandInterpreter ci = new CommandInterpreter(clientSocket.getInputStream(), new PrintStream(clientSocket.getOutputStream(), true));
                userThread = new Thread(ci);
                userThread.start();
            } // end while-loop
        }
        catch (IOException o) {

            System.out.println("Connection interrupted. Please restart the application.\n");
        } // end try-catch block
    } // end serve()
} // end class