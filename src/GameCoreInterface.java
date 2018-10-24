import java.util.HashSet;

/**
 *
 * @author Kevin
 */
public interface GameCoreInterface {

    /**
     * Broadcasts a message to all other players in the same room as player.
     * @param player Player initiating the action.
     * @param message Message to broadcast.
     */
    public void broadcast(Player player, String message);

    /**
     * Broadcasts a message to all players in the specified room.
     * @param room Room to broadcast the message to.
     * @param message Message to broadcast.
     */
    public void broadcast(Room room, String message);


    /**
     * Returns the player with the given name or null if no such player.
     * @param name Name of the player to find.
     * @return Player found or null if none.
     */
    public Player findPlayer(String name);

    /**
     * Allows a player to join the game.  If a player with the same name (case-insensitive)
     *  is already in the game, then this returns false.  Otherwise, adds a new player of
     *  that name to the game.  The next step is non-coordinated, waiting for the player
     *  to open a socket for message events not initiated by the player (ie. other player actions)
     * @param name
     * @return Player that is added, null if player name is already registered to someone else
     */
    public Player joinGame(String name);

    /**
     * Returns a look at the area of the specified player.
     * @param playerName Player Name
     * @return String representation of the current area the player is in.
     */
    public String look(String playerName);

    /**
     * Turns the player left.
     * @param name Player Name
     * @return String message of the player turning left.
     */
    public String left(String name);

    /**
     * Turns the player right.
     * @param name Player Name
     * @return String message of the player turning right.
     */
    public String right(String name);

    /**
     * Says "message" to everyone in the current area.
     * @param name Name of the player to speak
     * @param message Message to speak
     * @return Message showing success.
     */
    public String say(String name, String message);

    /**
     * Whispers "message" to a specific player.
     * @param srcName Name of the player to speak
     * @param dstName Name of the player to receive
     * @param message Message to speak
     * @return Message showing success
     */
    public String whisper(String srcName, String dstName, String message);

    /**
     * Player ignores further messages from another Player
     * @param srcName Player making the ignore request
     * @param dstName Player to be ignored
     * @return Message showing success
     */
    public String ignorePlayer(String srcName, String dstName);

    /**
     * Player unIgnores further messages from another Player
     * @param srcName Player making the unIgnore request
     * @param dstName Player to be unIgnored
     * @return Message showing success
     */
    public String unIgnorePlayer(String srcName, String dstName);

    /**
     * Player displays the list of players that are being ignored
     * @param name Player who's list is being targeted
     * @return The list of players being ignored
     */
    public String getIgnoredPlayersList(String name);

    // Feature 404. Quick Reply
    /**
     * Reply "message" to last whisper.
     * @param srcName Name of the player to speak
     * @param message Message to speak
     * @return Message showing success
     */
    public String quickReply(String srcName, String message);
    // End 404.

    // Feature 410: Joke
    /**
     * Tells a joke to the room. Reads local "chat config" file
     * that keeps a list of jokes, one per line. The command
     * chooses a random joke.
     * @param filename the "chat config" file to read the joke from.
     * */
    public String joke(String filename);

    /**
     * Returns a string representation of all objects you are carrying.
     * @param name Name of the player to move
     * @return Message showing success.
     */
    public String inventory(String name);

    /**
     * Leaves the game.
     * @param name Name of the player to leave
     * @return Player that was just removed.
     */
    public Player leave(String name);

    //Feature 411. Shout
    /**
     *Shouts "message" to everyone that is online
     *@param name Name of the player speaking
     *@param message Meesage to be spoken
     *@return Message showing success.
     */
    public String shout(String name, String message);
}
