package UltimateChat;

/*
    Interface that CommandInterpreter and ChatConsole implement, in order to be notified of changes to the ChatHistory.
    @author Ville Lohkovuori
    09/2017
 */

public interface Observer {

    void update(ChatMessage msg);
}