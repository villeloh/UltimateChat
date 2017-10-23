package UltimateChat;
/*
    Interface that ChatHistory implements, in order to be able to notify its registered observers
    (CommandInterpreter and ChatConsole) of any changes to itself.
    @author Ville Lohkovuori
    09/2017
 */

public interface Observable {

    void register(Observer e);

    void deregister(Observer e);

    void notifyObservers(ChatMessage msg);
}