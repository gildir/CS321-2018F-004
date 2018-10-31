


import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

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
    public GameObject(String worldFile) throws RemoteException {
        super();
        
        core = new GameCore(worldFile);
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
	 * Pokes the ghoul in the current room
	 * @param playerName Player name
	 * @return String message of ghoul
	 * @throws RemoteException
	 */
	public String pokeGhoul(String playerName) throws RemoteException {
		return core.pokeGhoul(playerName);
	}

	/**
	 * Pokes the ghoul in the current room
	 * @param playerName Player name
	 * @param item item's name, which will be throw. 
	 * @return String message of ghoul
	 * @throws RemoteException
	 */
	public String bribeGhoul(String playerName, String item) throws RemoteException {
		return core.bribeGhoul(playerName,item);
	}
	
	//public String giveToGhoul(String object, String playerName) {
	//	return core.giveToGhoul(object, playerName);
	//}
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

    public String pickupAll(String name)throws RemoteException{
        return core.pickupAll(name);
    }

    /**
     * Attempts to drop  an object < target >. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @param target The case-insensitive name of the object to pickup.
     * @return Message showing success.
     * @throws RemoteException
     */
    @Override
    public String drop(String name, String target) throws RemoteException {
        return core.drop(name, target);
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
     * Sorts the given player's inventory
     * @param name Name of the player
     * @return Message showing success.
     * @throws RemoteException 
     */    
    @Override
    public String sort(String name, String modes) throws RemoteException {
        return core.sort(name, modes);
    } 

    /**
     * Offers item from one player to another
     * @param srcName Name player offer item
     * @param dstName Name player being offered item
     * @param message String name of item being offered
     */
    @Override
    public String offer (String srcName, String dstName, String message) throws RemoteException{
	    return core.offer(srcName, dstName, message);
    }
        
    /**
     * Prints message to player if request can processed, contacts other player about their request
     * @param requestingTrader Name of the player who has requested the trade
     * @param traderToRequest Name of the player whom the first player has requested to trade with
     */ 
    public void requestPlayer(String requestingTrader, String traderToRequest) throws RemoteException{
        core.requestPlayer(requestingTrader, traderToRequest);

    }

    /**
     * Return string representation of trade acceptance
     * @param acceptingTrader Name of the player who is accepting the trade
     * @param traderToAccept Name of the player who has requested a trade
     * @return Message of success or fail
     */ 
    public String playerResponse(String acceptingTrader, String traderToAccept) throws RemoteException{
        return core.playerResponse(acceptingTrader, traderToAccept);
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
}
