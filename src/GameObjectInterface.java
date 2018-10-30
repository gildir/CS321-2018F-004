

import java.rmi.Remote;
import java.rmi.RemoteException;

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


	//Same functionality as bribeGhoul. Not currently used.
	//public String giveToGhoul(String object, String playerName) throws RemoteException;
	

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
     * Attempts to walk forward < distance > times.  If unable to make it all the way,
     *  a message will be returned.  Will display LOOK on any partial success.
     * @param name Name of the player to move
     * @param distance Number of rooms to move forward through.
     * @return Message showing success.
     * @throws RemoteException 
     */
    public String move(String name, int distance) throws RemoteException;

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
}
