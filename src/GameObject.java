


import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;

/**
 *
 * @author Kevin
 */
public class GameObject extends UnicastRemoteObject implements GameObjectInterface {
    private final GameCore core;

    /**
     * Creates a new GameObject.  Namely, creates the map for the rooms in the game,
     *  and establishes a new, empty, player list.
     * @throws RemoteException
     */
    public GameObject() throws RemoteException, IOException {
        super();
        core = new GameCore();
    }

    /**
     * Links an asynchronous event message connection to a player.
     * @param playerName Player to link the reply socket with.
     * @param writer PrintWriter to use for asynchronous messages.
     * @return true if player is found, false otherwise.
     */
    public boolean setReplyWriter(String playerName, PrintWriter writer) {
        Player player = core.findPlayer(playerName);
        if(player != null && writer != null) {
            player.setReplyWriter(writer);
            return true;
        }
        return false;
    }


    /**
     * Allows a player to join the game.  If a player with the same name (case-insensitive)
     *  is already in the game, then this returns false.  Otherwise, adds a new player of
     *  that name to the game.  The next step is non-coordinated, waiting for the player
     *  to open a socket for message events not initiated by the player (ie. other player actions)
     * @param name
     * @return true is player is added, false if player name is already registered to someone else
     * @throws RemoteException
     */
    @Override
    public boolean joinGame(String name) throws RemoteException {
        // Request join to the core and return the results back to the remotely calling method.
        return (core.joinGame(name) != null);
    }

    /**
     * Returns a look at the area of the specified player.
     * @param playerName Player Name
     * @return String representation of the current area the player is in.
     * @throws RemoteException
     */
    @Override
    public String look(String playerName) throws RemoteException {
        return core.look(playerName);
    }

    /**
     * Turns the player left.
     * @param name Player Name
     * @return String message of the player turning left.
     * @throws RemoteException
     */
    @Override
    public String left(String name) throws RemoteException {
        return core.left(name);
    }

    /**
     * Turns the player right.
     * @param name Player Name
     * @return String message of the player turning right.
     * @throws RemoteException
     */
    @Override
    public String right(String name) throws RemoteException {
        return core.right(name);
    }

    /**
     * Says "message" to everyone in the current area.
     * @param name Name of the player to speak
     * @param message Message to speak
     * @return Message showing success.
     * @throws RemoteException
     */
    @Override
    public String say(String name, String message) throws RemoteException {
    	return core.say(name, message);
    }

    // Feature 401. Whisper
    /**
     * Whispers "message" to a specific player.
     * @param srcName Name of the player to speak
     * @param dstName Name of the player to receive
     * @param message Message to speak
     * @return Message showing success
     * @throws RemoteException
     */
    @Override
    public String whisper(String srcName, String dstName, String message) throws RemoteException {
       	return core.whisper(srcName, dstName, message);
    }

    /**
     * Reply "message" to last whisper.
     * @param srcName Name of the player to speak
     * @param message Message to speak
     * @return Message showing success
     * @throws RemoteException
     */
    @Override
    public String quickReply(String srcName, String message) throws RemoteException {
    	return core.quickReply(srcName, message);
    }

    /**
     * Player ignores further messages from another Player
     * @param srcName Player making the ignore request
     * @param dstName Player to be ignored
     * @return Message showing success
     * @throws RemoteException
     */
    @Override
    public String ignorePlayer(String srcName, String dstName) throws RemoteException {
        return core.ignorePlayer(srcName, dstName);
    }

    //Feature 408. Unignore Player.
    /**
     * Player unIgnores further messages from another Player
     * @param srcName Player making the unIgnore request
     * @param dstName Player to be unIgnored
     * @return Message showing success
     * @throws RemoteException
     */
    @Override
    public String unIgnorePlayer(String srcName, String dstName) throws RemoteException {
        return core.unIgnorePlayer(srcName, dstName);
    }

    /**
     * Player displays the list of players that are being ignored
     * @param name Player who's list is being targeted
     * @return The list of players being ignored
     * @throws RemoteException
     */
    @Override
    public String getIgnoredPlayersList(String name) throws RemoteException{
        return core.getIgnoredPlayersList(name);
    }

   // Feature 410: Joke
    /**
     * Tells a joke to the room. Reads local "chat config" file
     * that keeps a list of jokes, one per line. The command
     * chooses a random joke.
     * @param filename the "chat config" file to read the joke from.
     * */
   @Override
   public String joke(String filename) throws RemoteException{
     return core.joke(filename);
   }

    /**
     * Attempts to walk forward < distance > times.  If unable to make it all the way,
     *  a message will be returned.  Will display LOOK on any partial success.
     * @param name Name of the player to move
     * @param distance Number of rooms to move forward through.
     * @return Message showing success.
     * @throws RemoteException
     */
    @Override
    public String move(String name, int distance) throws RemoteException {
        return core.move(name, distance);
    }

    /**
     * Attempts to pick up an object < target >. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @param target The case-insensitive name of the object to pickup.
     * @return Message showing success.
     * @throws RemoteException
     */
    @Override
    public String pickup(String name, String target) throws RemoteException {
        return core.pickup(name, target);
    }

    /**
     * Returns a string representation of all objects you are carrying.
     * @param name Name of the player to move
     * @return Message showing success.
     * @throws RemoteException
     */
    @Override
    public String inventory(String name) throws RemoteException {
        return core.inventory(name);
    }

    /**
     * Leaves the game.
     * @param name Name of the player to leave
     * @throws RemoteException
     */
    @Override
    public void leave(String name) throws RemoteException {
        Player player = core.leave(name);
        if(player != null) {
            player.getReplyWriter().close();
        }
    }

    //Feature 411. Shout
    /**
     *Shouts "message" to everyone that is online
     *@param name Name of the player speaking
     *@param message Meesage to be spoken
     *@return Message showing success.
     *@throws RemoteException
     */
    @Override
    public String shout(String name, String message) throws RemoteException {
        return core.shout(name, message);
    }

    //Begin 409 Word Filter.
    /**
     * Stores a list of words to filter from chat for the player
     * @param playerName the name of the player you're lookng for
     * @param filteredWords list of words to filter
     */
    @Override
    public void setPlayerFilteredWords(String playerName, HashSet<String> filteredWords) {
        Player player = core.findPlayer(playerName);
        player.setFilteredWords(filteredWords);
    }


    //End 409 Word Filter
}