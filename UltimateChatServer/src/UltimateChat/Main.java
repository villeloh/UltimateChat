package UltimateChat;

/*
    Main class for starting the app.
    @author Ville Lohkovuori
    09/2017
 */

public class Main {

    public static void main(String args[]) {

        ChatServer server = new ChatServer();
        server.serve();
    }
}