

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Kevin
 */
public interface GameObjectInterface extends Remote {

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
     * @param name Name of the player enter the bank
     * @throws RemoteException 
     */    
    public String venmo(String name) throws RemoteException;

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
    public int sellItem(String name, int shopId, String item) throws RemoteException;
    
    /**
     * Returns a Shop's inventory as a formatted string
     * @param id The shop ID
     * @return A formatted string representing the Shop's inventory
     */
    public String getShopInv(int id) throws RemoteException;

}
