

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
	public String giveToGhoul(String object, String playerName) throws RemoteException;
	

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
    public String move(String name, Direction direction) throws RemoteException;

    /**
     * Attempts to pick up an object < object >. Will return a message on any success or failure.
     * @param name Name of the player to pickup an object
     * @param object The case-insensitive name of the object to pickup.
     * @return Message showing success.
     * @throws RemoteException 
     */    
    public String pickup(String name, String object) throws RemoteException;
    
    /**
     * Attempts to pick up all objects in the room. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @return Message showing success. 
     */    
    public String pickupAll(String name) throws RemoteException;

     /**
     * Returns a string representation of all objects you are carrying.
     * @param name Name of the player to view their inventory
     * @return Message showing success.
     * @throws RemoteException 
     */    
    public String inventory(String name) throws RemoteException;   
    
    /**
    * Prompts a message that someone is challenging them to a R-P-S
    * @param challenger is the name of the player challenging someone in the area
    * @param challenge is the name of the player being challenge
    * @return Message showing success
    * @throws RemoteException
    */
    public String challenge(String challenger, String challengee) throws RemoteException;
     
    /**
    * Prompts a message that they are accepting a challnge from someone to a R-P-S
    * @param challenger is the name of the player challenging someone in the area
    * @param challenge is the name of the player accepting
    * @return Message showing success
    * @throws RemoteException
    */
    public String accept(String challenger, String challengee) throws RemoteException;

     /**
     * Leaves the game.
     * @param name Name of the player to leave
     * @throws RemoteException 
     */    
    public void leave(String name) throws RemoteException;       
}
