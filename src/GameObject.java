


import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
	public GameObject(String playerAccountsLocation, String worldFile) throws Exception {
        super();
        
		core = new GameCore(playerAccountsLocation, worldFile);

    }
	
	protected void shutdown() {
		core.shutdown();
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
	 * implements the chst operations 
	 * @param playerName Player name
	 * @return String message from chest operations
	 * @throws RemoteException
	 */
	public String chest(String playerName, String option, String item) throws RemoteException {
		return core.chest(playerName,option,item);
	}
	/**
	 * Used to create a hash encrypted in SHA256 for use in encrypting passwords
	 * 
	 * @param toHash
	 * @return SHA256 encrypted hash value, or "ERROR" If encryption method fails.
	 */
	public String hash(String toHash) {
		try {
			byte[] encodedhash = MessageDigest.getInstance("SHA-256").digest(toHash.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : encodedhash)
				sb.append(String.format("%02X", b));
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
		}
		return "ERROR";
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
	public boolean joinGame(String name, String password) throws RemoteException {
		// Request join to the core and return the results back to the remotely calling
		// method.
		return (core.joinGame(name, password) != null);
	}

	/**
	 * Allows a player to create an account. If the player name already exists this
	 * returns the corresponding enum. If the players name is of an invalid format
	 * this returns that corresponding emum. Otherwise this returns success and
	 * calls joinGame.
	 * 
	 * @param name
	 * @param password
	 * @return an enumeration representing the creation status, or null if password
	 *         failed to be encrypted in hash function.
	 * @throws RemoteException
	 */
	@Override
	public Responses createAccountAndJoinGame(String name, String password) throws RemoteException {
		return core.createAccountAndJoinGame(name, password);
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
     * Generates list of all online players.
     * @return String of linked list PlayerList
     * @throws RemoteException
     */
    @Override
    public String showPlayers() throws RemoteException {
      return core.showPlayers();
    }

    /**
     * Reply "message" to last whisper.
     * @param srcName Name of the player to speak
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
     * Create a new chatroom
     * @param playerName Name of the player creating the chatroom
     * @param chatName Name of the chatroom
     * @return Message showing success
     * @throws RemoteException
     */
    @Override
    public String makeChat(String playerName, String chatName) throws RemoteException {
        return core.makeChat(playerName, chatName);
    }
    
    /**
     * Invite a player to your current chatroom.
     * @param srcPlayer Name of player sending the invite
     * @param dstPlayer Name of player receiving the invite
     * @return Message showing success
     * @throws RemoteException
     */
    @Override
    public String invChat(String srcPlayer, String dstPlayer, String chatName) throws RemoteException {
        return core.invChat(srcPlayer, dstPlayer, chatName);
    }
    
    /**
     * Join a chatroom
     * @param srcPlayer Name of player joining
     * @param chatName Name of chatroom to join
     * @return Message showing success
     * @throws RemoteException
     */
    @Override
    public String joinChat(String srcPlayer, String chatName) throws RemoteException {
        return core.joinChat(srcPlayer, chatName);
    }
    
    /**
     * Leave a chatroom
     * @param srcPlayer Name of player leaving
     * @param chatName Name of chatroom to leave
     * @return Message showing success
     * @throws RemoteException
     */
    @Override
    public String leaveChat(String srcPlayer, String chatName) throws RemoteException {
        return core.leaveChat(srcPlayer, chatName);
    }
    
    /**
     * Check if chatroom exists
     * @return boolean showing success
     * @throws RemoteException
     */
    @Override
    public boolean checkChat(String command) throws RemoteException {
        return core.checkChat(command);
    }
    
    /**
     * Message a chatroom
     * @param srcPlayer Name of player sending the message
     * @param message The message to be sent
     * @param chatName The name of the chat to send the message to
     * @return Message showing success
     * @throws RemoteException
     */
    @Override
    public String messageChat(String srcPlayer, String message, String chatName) throws RemoteException {
    	return core.messageChat(srcPlayer, message, chatName);
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
	 * Initiates dialogue with NPC
	 * @param playerName Player name
	 * @param npcName NPC name
	 * @return Dialogue options for player
     * @throws RemoteException
	 */
    public String talkNpc(String name, String npcName) throws RemoteException {
        return core.talkNpc(name, npcName);
    }

	/**
	 * Selects dialogue option with NPC and gets response
	 * @param playerName Player name
	 * @param npcName NPC name
	 * @param dialogueChoice Choice of dialogue option
	 * @return Dialogue options for player
	 */
    public String selectNPCDialogueOption(String name, String npcName, int dialogueChoice) throws RemoteException {
        return core.selectNPCDialogueOption(name, npcName, dialogueChoice);
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
    @Override
    public void setPlayerFilteredWords(String playerName, HashSet<String> filteredWords) {
        Player player = core.findPlayer(playerName);
        player.setFilteredWords(filteredWords);
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
     * Attempts to walk in < direction > 1  time.  If unable to,
     *  a message will be returned.  Will display LOOK on any partial success.
     * @param name Name of the player to move
     * @param direction which direction to move forward through.
     * @return Message showing success.
     * @throws RemoteException 
     */
    @Override
    public String move(String name, Direction direction) throws RemoteException {
        return core.move(name, direction);
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

    @Override
    public String pickup(String name, String object, int amount) throws RemoteException{
        return core.pickup(name, object, amount);
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
     * Attempts to use an item< target >. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @param target The case-insensitive name of the object to use.
     * @return Message showing success.
     * @throws RemoteException
     */
    @Override
    public String useItem(String name, String target) throws RemoteException {
        return core.useItem(name, target);
    }

    /**
     * Gets the title of the player. Will return a message on any success or failure.
     * @param name Name of the player
     * @return title of player, if applicable.
     * @throws RemoteException
     */
    @Override
    public String getPlayerTitle(String name) throws RemoteException {
        return core.getPlayerTitle(name);
    }

    /**
     *  Strips title from a player.
     *  @param name name of the player
     *  @throws RemoteException
     */
    public boolean removePlayerTitle(String name) {
	return core.removePlayerTitle(name);
    }

	public String examine(String name, String target) throws RemoteException 
	{
		return core.examine(name, target);
	}
    /**
     * Attempts to erase the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardErase(String name) throws RemoteException {
        return core.whiteboardErase(name);
    }

    /**
     * Attempts to read the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardRead(String name) throws RemoteException {
        return core.whiteboardRead(name);
    }

    /**
     * Attempts to  the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @param text Text to write on the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardWrite(String name, String text) throws RemoteException {
        return core.whiteboardWrite(name, text);
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
     * @author Group: King
     * @param name Name of the player trying to shop
     * @return Returns the id of the room the player has just entered a bank in 
     * @throws RemoteException
     */
    public int bank(String name) throws RemoteException {
    	return core.bank(name);
    }
    
    /**
     * Gives the central bank object commands (implimented like this for maximum encapsulation)
     * @param cmd_id The id of the command to be used (mapped in the BankClient class)
     * @param name The name of the user interacting with the Bank
     * @param cmd Any extra arguments that may need to be sent to the command
     * @return A string based on the success or failure of the command
     */
    public String bankCmdRunner(String cmd, String name, String args) {
    	return core.bankCmdRunner(cmd, name, args);
    }
    
    /**
     * @author Group 4: King
     * Lets player shop if in a shoppable location
     * @param name Name of the player trying to shop
     * @return Returns the shop the player just entered or Null if they couldnt enter one
     * @throws RemoteException
     */
    public int shop(String name) throws RemoteException{
    	return core.shop(name);
    }
    
    /**
     * @author Group 4: King
     * Returns the Player object from the name
     * @param name The player's name
     * @return A reference to the Player object
     */
    // Warning: Doesn't work because player obj not serializable
    public Player getPlayer(String name) throws RemoteException{
    	return core.findPlayer(name);
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
    public String offer (String srcName, String message1, String junk, String message2) throws RemoteException{
	    return core.offer(srcName, message1, junk, message2);
    }

    /**
     * Returns a string message about success of offer and status of inventory
     * @param dstName Name of player accepting or rejecting the offer
     * @param reply whther the offer has been accepted or rejected
     * @return Message showing status of offer reply
     */
    public String offerReply(String dstName, boolean reply) throws RemoteException{
        return core.offerReply(dstName, reply);
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
     * Prompts a message that someone is rejecting a challenge to R-P-S
     * @param challenger is the name of the player challenging someone in the area
     * @param challengee is the name of the player rejecting 
     * @return Messaging showing success
     * @throws RemoteException
     */
    public String reject(String challenger, String challengee) throws RemoteException{
	return core.reject(challenger, challengee);
    }

    /**
      * Prompts a message that someone pick either R-P-S
      * @param name is the name of the player
      * @param option is either R-P-S
      * @return Messaging showing success
      * @throws RemoteException
      */
    public String pickRPS(String name, String option) throws RemoteException {
        return core.pickRPS(name, option);
    }

    /**
      * Sends a message to the play to teach them how to play R-P-S
      * @param challenger is the name of the player who wants to be taught
      * @return Messaging showing success
      * @throws RemoteException
      */
     public String teach(String player) throws RemoteException{
         return core.teach(player);
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
    
    
    /**
     * Takes the player into venmo. The new and improved way to exchange money with other players.
     * 
     * @author Team 4: Alaqeel
     * @param name Name of the player enter the bank
     * @param tokens 
     * @throws RemoteException 
     */    
	@Override
	public String venmo(String name, ArrayList<String> tokens) throws RemoteException {
		return core.venmo(name, tokens);
		
	}    
	
	/**
	 * @author Team 4: King
	 * Returns a string representation of how much money a player has
	 */
	public String wallet(String name) throws RemoteException {
		return core.wallet(name);
	}
	
	public String getShopStr(int id) throws RemoteException{
		return core.getShopStr(id);
	}
	
	/**
     * Allows player to sell an item to a shop, and increases their money
     * @author Team 4: King
     * @param name Name of the player
     * @param shopId The ID of the shop the player is selling an item to
     * @param item The item the player is selling (eventually will be an Item obj)
     */
    public double sellItem(String name, int shopId, String item) throws RemoteException{
    	return core.sellItem(name, shopId, item);
    }

    /**
     * 605B_buy_method
     * Allows player to sell an item to a shop, and increases their money
     * @author Team 4: Mistry
     * @param name Name of the player
     * @param shopId The ID of the shop the player is selling an item to
     * @param item The item the player is selling (eventually will be an Item obj)
     */
    public String buyItem(String name, int shopId, String item) throws RemoteException{
    	return core.buyItem(name, shopId, item);
    }

    /**
     * updates the playlist in the Shop
     * @param name Name of the player
     * @return void
     */
    public void shopLeft(String name) throws RemoteException
    {
        core.shopLeft(name);
    }
    
    /**
     * Returns a Shop's inventory as a formatted string
     * @param id The shop ID
     * @return A formatted string representing the Shop's inventory
     */
    public String getShopInv(int id) throws RemoteException{
    	return core.getShopInv(id);
    }

    /**
     * Returns a Shop's "In Demand" inventory as a formatted string
     * @param id The shop ID
     * @return A formatted string representing the Shop's "In Demand" inventory
     */
    public String getShopDemInv(int id) throws RemoteException{
        return core.getShopDemInv(id);
    }

    /**
     * 108 In game ASCII map
     * Returns an ascii representation of nearby rooms
     * @param name Name of the player
     * @return String representation of the map
     */
    public String showMap(String name) throws RemoteException{
       return core.showMap(name);
    }	
	/**
	 * Delete a player's account.
	 * 
	 * @param name Name of the player to be deleted
	 * @throws RemoteException
	 */
	@Override
	public void deleteAccount(String name) throws RemoteException {
		Player player = core.deleteAccount(name);
		if (player != null) {
			player.getReplyWriter().close();
		}
	}

	/**
	 * Adds a player to your friends list
	 * 
	 * @param name
	 * @param friend
	 * @return responseType
	 * @throws RemoteException
	 */
	@Override
	public Responses addFriend(String name, String friend) throws RemoteException {
		return core.addFriend(name, friend);
	}

	/**
	 * Removes a player from your friends list
	 * 
	 * @param name
	 * @param ex
	 * @return responseType
	 * @throws RemoteException
	 */
	@Override
	public Responses removeFriend(String name, String ex) throws RemoteException {
		return core.removeFriend(name, ex);
	}
	
	/**
	 * returns a message showing all online friends
	 * 
	 * @param Player name name of player requesting list of friends
         * @param onlineOnly true if you only want a list of online friends, else false.
	 * @return Message showing online friends
	 * @throws RemoteException 
	 */
	@Override
    public String viewFriends(String name, boolean onlineOnly) throws RemoteException {
        return core.viewFriends(name, onlineOnly);
    }
    
    @Override
    public void heartbeatCheck(String name) throws RemoteException{
        core.heartbeatCheck(name);
    }
        

    /**Prompts a message that someone is challenging them to a R-P-S
     * @param challenger is the name of the player challenging someone in the area
     * @param challenge is the name of the player being challenge
     * @return Message showing success
     * @throws RemoteException
     */
    public String challenge(String challenger, String challengee) throws RemoteException{
      return core.challenge(challenger, challengee);
    }

    /**Prompts a message that someone is accepting a challenge to a R-P-S
     * @param challenger is the name of the player challenging someone in the area
     * @param challenge is the name of the player accepting
     * @return Message showing success
     * @throws RemoteException
     */
    public String accept(String challenger, String challengee, String rounds) throws RemoteException{
      return core.accept(challenger, challengee, rounds);
    }

	@Override
	public Responses removeQuestion(String name, int num) throws RemoteException {
		return core.removeQuestion(name, num);
	}

	@Override
	public DataResponse<ArrayList<String>> getQuestions(String name) throws RemoteException {
		return core.getQuestions(name);
	}

	@Override
	public Responses verifyAnswers(String name, ArrayList<String> answers) throws RemoteException {
		return core.verifyAnswers(name, answers);
	}

	@Override
	public Responses addRecoveryQuestion(String name, String question, String answer) throws RemoteException {
		return core.addRecoveryQuestion(name, question, answer);
	}

	@Override
	public DataResponse<Long> getAccountAge(String name)  throws RemoteException{
		return core.getAccountAge(name);
	}

	@Override
	public Responses verifyPassword(String name, String password) throws RemoteException {
		return core.verifyPassword(name, password);
	}

	@Override
	public Responses changePassword(String name, String password)  throws RemoteException{
		return core.changePassword(name, password);
	}

    /**
     * @param player is the name of the player that wants to turn off RPS resolutions
     * @return Message showing success
     * @throws RemoteException
     */
    public String toggleRPSChat(String player) throws RemoteException{
	return core.toggleRPSChat(player);
    }
  
    /**
     * Sets a player's chat prompt string
     * @param playerName - player you're setting the chat prefix for
     * @param newPrefix - the player's new prefix.
     * @throws RemoteException
     */
    public void setPlayerChatPrefix(String playerName, String newPrefix) throws RemoteException {
        Player player = core.findPlayer(playerName);
        player.setPrefix(newPrefix);
    }
    
    /**
     * Checks the Venmo mailbox. If mailbox is not empty, prints the content.
     * 
     * @param playerName Name of the player
     * @throws RemoteException
     */
    public void checkVenmoMail(String playerName) throws RemoteException {
        Venmo.checkMail(playerName);
    }
}
