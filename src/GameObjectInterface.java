

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Kevin
 */
public interface GameObjectInterface extends Remote {
	
	/**
	 * Pokes the ghoul in the current room
	 * @param playerName Player name
	 * @return String message of ghoul
	 * @throws RemoteException
	 */
	public String pokeGhoul(String playerName) throws RemoteException;


	/**
	 * Pokes the ghoul in the current room
	 * @param playerName Player name
	 * @param item item's name, which will be throw. 
	 * @return String message of ghoul
	 * @throws RemoteException
	 */
	public String bribeGhoul(String playerName, String item) throws RemoteException;

    /**
     * Sends a request to join the game.  
     * Returns with the status of the join.  On true, the server expects the client
     *  will initiate a socket connection, to serve as an asynchronous, unsolicited
     *  String transfer mechanism.
     * @param name Player Name. 
     * @return true if name is available and join is successful, false otherwise.
     * @throws RemoteException 
     */
    public boolean joinGame(String name) throws RemoteException;
    
    /**
     * Returns a look at the area of the specified player.
     * @param name Player Name
     * @return String representation of the current area the player is in.
     * @throws RemoteException 
     */
    public String look(String name) throws RemoteException;
    
    /**
     * Turns the player left.
     * @param name Player Name
     * @return String message of the player turning left.
     * @throws RemoteException 
     */
    public String left(String name) throws RemoteException;
    
     /**
     * Turns the player right.
     * @param name Player Name
     * @return String message of the player turning right.
     * @throws RemoteException 
     */
    public String right(String name) throws RemoteException;
   
    /**
     * Says "message" to everyone in the current area.
     * @param name Name of the player to speak
     * @param message Message to speak
     * @return Message showing success.
     * @throws RemoteException 
     */
    public String say(String name, String message) throws RemoteException;

    /**
     * Generates list of all online players.
     * @return String of linked list PlayerList
     * @throws RemoteException
     */
    public String showPlayers() throws RemoteException;

    /**
     * Whispers "message" to a specific player.
     * @param srcName Name of the player to speak
     * @param dstName Name of the player to receive
     * @param message Message to speak
     * @return Message showing success
     * @throws RemoteException
     */
    public String whisper(String srcName, String dstName, String message) throws RemoteException;

    /**
     * Reply "message" to last whisper.
     * @param srcName Name of the player to speak
     * @param message Message to speak
     * @return Message showing success
     * @throws RemoteException
     */
    public String quickReply(String srcName, String message) throws RemoteException;

    /**
     * Player ignores further messages from another Player
     * @param srcName Player making the ignore request
     * @param dstName Player to be ignored
     * @return Message showing success
     * @throws RemoteException
     */
    public String ignorePlayer(String srcName, String dstName) throws RemoteException;

    //Feature 408. Unignore player.
    /**
     * Player unIgnores further messages from another Player
     * @param srcName Player making the unIgnore request
     * @param dstName Player to be unIgnored
     * @return Message showing success
     * @throws RemoteException
     */
    public String unIgnorePlayer(String srcName, String dstName) throws RemoteException;

    /**
     * Player displays the list of players that are being ignored
     * @param name Player who's list is being targeted
     * @return The list of players being ignored
     * @throws RemoteException
     */
    public String getIgnoredPlayersList(String name) throws RemoteException;

    // Feature 410: Joke
    /**
     * Tells a joke to the room. Reads local "chat config" file
     * that keeps a list of jokes, one per line. The command
     * chooses a random joke.
     * @param filename the "chat config" file to read the joke from.
     * */
    public String joke(String filename) throws RemoteException;


//Feature 411. Shout
    /**
     *Shouts "message" to everyone that is online
     *@param name Name of the player speaking
     *@param message Meesage to be spoken
     *@return Message showing success.
     *@throws RemoteException
     */
    public String shout(String name, String message) throws RemoteException;

    /**
     * Returns a player reference from the game core, from the player's name.
     * @param playerName the name of the player you're lookng for
     * @return the player (if found) from the game core
     */
    public void setPlayerFilteredWords(String playerName, HashSet<String> newFilteredWords) throws RemoteException;

    /**
     * Attempts to walk forward < distance > times.  If unable to make it all the way,
     *  a message will be returned.  Will display LOOK on any partial success.
     * @param name Name of the player to move
     * @param distance Number of rooms to move forward through.
     * @return Message showing success.
     * @throws RemoteException 
     */
    public String move(String name, int distance) throws RemoteException;

    public String move(String name, Direction direction)throws RemoteException;

    /**
     * Attempts to pick up an object < object >. Will return a message on any success or failure.
     * @param name Name of the player to pickup an object
     * @param object The case-insensitive name of the object to pickup.
     * @return Message showing success.
     * @throws RemoteException 
     */    
    public String pickup(String name, String object) throws RemoteException;

    public String pickupAll(String name)throws RemoteException;
 
    /**
     * Attempts to drop an object < object >. Will return a message on any success or failure.
     * @param name Name of the player to drop an object
     * @param object The case-insensitive name of the object to drop.
     * @return Message showing success.
     * @throws RemoteException 
     */ 
    public String drop(String name, String object) throws RemoteException;
   
    /**
     * Attempts to sort the player's inventory. Will return a message on any success or failure.
     * @param name Name of the player
     * @return Message showing success.
     * @throws RemoteException 
     */ 
    public String sort(String name, String modes) throws RemoteException; 

    /**
     * Prints message to player if request can processed, contacts other player about their request
     * @param requestingTrader Name of the player who has requested the trade
     * @param traderToRequest Name of the player whom the first player has requested to trade with
     */ 
    public void requestPlayer(String requestingTrader, String traderToRequest) throws RemoteException;

    /**
     * Attempts to offer a player an object
     * @param srcName Name of the player making offer
     * @param dstName Name of player receiving offer
     * @param object String item beig offered
     * @throws RemoteException
     */
    public String offer (String srcName, String dstName, String object) throws RemoteException;

    /**
     * Return string representation of trade acceptance
     * @param acceptingTrader Name of the player who is accepting the trade
     * @param traderToAccept Name of the player who has requested a trade
     * @return Message of success or fail
     */ 
    public String playerResponse(String acceptingTrader, String traderToAccept) throws RemoteException;

    /**
     * Attempts to erase the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardErase(String name) throws RemoteException;

    /**
     * Attempts to read the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardRead(String name) throws RemoteException; 

    /**
     * Attempts to  the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @param text Text to write on the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardWrite(String name, String text) throws RemoteException;


     /**
     * Returns a string representation of all objects you are carrying.
     * @param name Name of the player to view their inventory
     * @return Message showing success.
     * @throws RemoteException 
     */    
    public String inventory(String name) throws RemoteException;   
    
     /**
     * Leaves the game.
     * @param name Name of the player to leave
     * @throws RemoteException 
     */    
    public void leave(String name) throws RemoteException;
    
    /**
     * Takes the player into venmo. The new and improved way to exchange money with other players.
     * 
     * @author Team 4: Alaqeel
     * @param name Name of the player enter the bank
     * @param tokens 
     * @throws RemoteException 
     */    
    public String venmo(String name, ArrayList<String> tokens) throws RemoteException;

    /**
     * @author Team 4: King
     * Lets player shop if in a shoppable area
     * @param name Name of the player
     * @return Returns the id of the shop the player just entered or -1 if they can't shop
     * @throws RemoteException
     */
    public int shop(String name) throws RemoteException;
    
    /**
     * Returns a player object when given the player's name
     * @param name The name of the player to find
     * @return The player object or Null if not found
     * @throws RemoteException
     */
    public Player getPlayer(String name) throws RemoteException;
   
    /**
     * Returns the amount of money in a player's wallet
     * @param name The name of the player
     * @return The amount of money a player has formatted with 2 decimals
     * @throws RemoteException
     */
    public String wallet(String name) throws RemoteException;
    
    /**
     * Returns a reference to a shop 
     * @param id
     * @return the shop or null
     * @throws RemoteException 
     */
    public String getShopStr(int id) throws RemoteException;
    
    /**
     * Allows player to sell an item to a shop, and increases their money
     * @author Team 4: King
     * @param name Name of the player
     * @param shopId The ID of the shop the player is selling an item to
     * @param item The item the player is selling (eventually will be an Item obj)
     */
    public double sellItem(String name, int shopId, String item) throws RemoteException;
    
    /**
     * 605B_buy_method
     * Allows player to sell an item to a shop, and increases their money
     * @author Team 4: Mistry
     * @param name Name of the player
     * @param shopId The ID of the shop the player is selling an item to
     * @param item The item the player is selling (eventually will be an Item obj)
     */
    public String buyItem(String name, int shopId, String item) throws RemoteException;

    /**
     * Returns a Shop's inventory as a formatted string
     * @param id The shop ID
     * @return A formatted string representing the Shop's inventory
     */
    public String getShopInv(int id) throws RemoteException;
}
