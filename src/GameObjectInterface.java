

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
     * Prompts a messaging that they are rejectin a challenge from someone to R-P-S
     * @param challenger is the name of the player challenging someone in the area
     * @param challengee is the name of the player accepting
     * @return MEssage showing success
     * @throws Remote Exception
     */
    public String reject(String challenger, String challengee) throws RemoteException;

    /**
      * Prompts a message that picks R-P-S
      * @param name is the name of the player
      * @param options is either R-P-S
      * @return Message showing success
      * @throws RemoteException
      */
    public String pickRPS(String name, String options) throws RemoteException;

    /**
      * Sends a messages teaching the player how to play R-P-S
      * @param player is the name of the player that needs to be taught
      * @return MEssage showing success
      * @throws Remote Exception
      */
     public String teach(String player) throws RemoteException;
     
     /**
     * Leaves the game.
     * @param name Name of the player to leave
     * @throws RemoteException 
     */    
    public void leave(String name) throws RemoteException;       
}
