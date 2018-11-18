
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.lang.StringBuilder;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.logging.FileHandler;

/**
 *
 * @author Kevin
 */
public class GameCore implements GameCoreInterface {
    private final PlayerList playerList;
    private final Map map;
    protected DailyLogger dailyLogger;
    private HashMap<Integer,Shop> shoplist;
    private Ghoul ghoul;
    private PrintWriter pw;
    private Bank bank;
    private final Logger rpsLogger = Logger.getLogger("battles");
    private FileHandler rpsHandler;
    private boolean pickRPSToggle = false;

	// Accounts and Login
	private final PlayerAccountManager accountManager;
	private final Object loginLock = new Object();
	private final Object createAccountLock = new Object();
	private Logger playerLogger = Logger.getLogger("connections");
	private FriendsManager friendsManager;
	private final Object friendsLock = new Object();
    
    private int dormCountId = 100002;//used for dormroom initialization   
    /**
	 * Creates a new GameCoreObject. Namely, creates the map for the rooms in the
	 * game, and establishes a new, empty, player list.
	 * 
	 * This is the main core that both the RMI and non-RMI based servers will
	 * interface with.
	 * 
	 * @throws Exception
	 * 
	 */
    public GameCore(String playerAccountsLocation, String worldFile) throws Exception {

        // Generate the game map.
        map = new Map(worldFile);
        this.dailyLogger = new DailyLogger();
        dailyLogger.write("SERVER STARTED");
        playerList = new PlayerList(); 
        
        // Builds a list of shops mapped to their map id (can be expanded as needed)
        shoplist = new HashMap<Integer,Shop>();
        shoplist.put(new Integer(1), new Shop("Clocktower shop", "The shopping destination for all of your gaming needs."));

        // Set up empty central bank
        bank = new Bank();
        
        pw = new PrintWriter(new FileWriter("chatlog.txt"));
        pw.flush();
        pw.close();

        initConnectionsLogger();
	rpsLogger();
        
        accountManager = new PlayerAccountManager(playerAccountsLocation);
        
		friendsManager = FriendsManager.Create(new File("friends.json"));
        		
        Thread objectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Random rand = new Random();
               
                Room room;
                Item object;
                ArrayList<Item> objects = new ArrayList<Item>();
                try
                {
                    double inWeight = 0;
                    double inValue = 0;
                    String inName = "";
			String inFlavor = "";
			String inDisc = "";
                    Scanner scanner = new Scanner(new File("./items.csv"));
                    scanner.nextLine();
                    scanner.useDelimiter(",|\\r\\n|\\n|\\r");

                    while(scanner.hasNext())
                    {
                        inName = scanner.next();
                        inWeight = Double.parseDouble(scanner.next().replace(",", ""));
			inValue = Double.parseDouble(scanner.next().replace(",", ""));
			inDisc = scanner.next();
                        inFlavor = scanner.next().replace("\\r\\n|\\r|\\n", "");
                        Item newItem = new Item(inName, inWeight, inValue, inDisc, inFlavor);
                        objects.add(newItem);

                    }
                }
                catch(IOException e)
                {
                    objects.add(new Item("Flower", 1.0, 0.0, null, null));
                    objects.add(new Item("Textbook", 10.3, 5.2, null, null));
                    objects.add(new Item("Phone", 2.9, 1.0, null, null));
                    objects.add(new Item("Newspaper", 10.0, 9.0, null, null));
                }
                while(true) {
                    try {
                        Thread.sleep(rand.nextInt(60000));
                        object = objects.get(rand.nextInt(objects.size()));
                        room = map.randomRoom();
                        room.addObject(object);
                        room.addObject(object);
                        room.addObject(object);
                        room.addObject(object);
                        room.addObject(object);

						GameCore.this.broadcast(room, "You see a student rush past and drop a " + object + " on the ground.");
						
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);}
                }}});

                Thread hbThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true) {
                            try {
                                Thread.sleep(5000);
                                List<String> expiredPlayers  = playerList.getExpiredPlayers();
                                expiredPlayers.forEach(s -> {
                                        leave(s);
                                        });
                                  } catch (InterruptedException ex) {
                                        }
                            }
                         }
                    });
                 hbThread.setDaemon(true);
                 hbThread.setName("heartbeatChecker");
                 hbThread.start();
        
                // new thread awake and control the action of Ghoul.
                // team5 added in 10/13/2018
                Thread awakeDayGhoul = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random rand = new Random();
                        Room room = map.randomRoom();
                        ghoul = new Ghoul(room.getId());
                        room.hasGhoul = true;
                        GameCore.this.broadcast(room, "You see a Ghoul appear in this room");

                        while (true) {
                            try {
                                // Ghoul move in each 10-15 seconds.
                                Thread.sleep(1200000 + rand.nextInt(500000));

                                // make Ghoul walk to other room;
                                GameCore.this.ghoulWander(ghoul, room);
                                room.hasGhoul = false;
                                GameCore.this.broadcast(room, "You see a Ghoul leave this room");
                                room = map.findRoom(ghoul.getRoom());
                                room.hasGhoul = true;
                                GameCore.this.broadcast(room, "You see a Ghoul enter this room");


                            } catch (InterruptedException ex) {
                                Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                });

                objectThread.setDaemon(true);
                awakeDayGhoul.setDaemon(true);
                objectThread.start();
                awakeDayGhoul.start();
            }
	
	/**
	 * Used to create a hash encrypted in SHA256 for use in encrypting passwords
	 * 
	 * @param toHash
	 * @return SHA256 encrypted hash value, or "ERROR" If encryption method fails.
	 */
	private String hash(String toHash) {
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

	public void ghoulWander(Ghoul g, Room room) {
		Random rand = new Random();
		int[] candinateRoom = new int[4];

		// easiest way to get all possible room;
		candinateRoom[0] = room.getLink(Direction.NORTH);
		candinateRoom[1] = room.getLink(Direction.SOUTH);
		candinateRoom[2] = room.getLink(Direction.WEST);
		candinateRoom[3] = room.getLink(Direction.EAST);

		// random walk.
		while (true) {
			int roomID = candinateRoom[rand.nextInt(4)];
			if (roomID != 0) {
				g.setRoom(roomID);
				return;
			}
		}
    }
    

    /**
     * @author Group 4: King
     * If player is in an area that allows it, they may enter a bank and save/withdraw money
     * @param name 
     * @return the id of the bank they have entered
     */
    public int bank(String name) {
    	Player player = this.playerList.findPlayer(name);
    	Room room = map.findRoom(player.getCurrentRoom());
    	
    	if (map.isShoppable(room)) { // For now we will make shop rooms also banks: subject to change
    		return room.getId();
    	}
    	return -1;
    }
    
    /**
     * Gives the central bank object commands (implimented like this for maximum encapsulation)
     * @param cmd The id of the command to be used (mapped in the BankClient class)
     * @param name The name of the user interacting with the Bank
     * @param args Any extra arguments that may need to be sent to the command
     * @return A string based on the success or failure of the command
     */
    public String bankCmdRunner(String cmd, String name, String args) {
		//parse arguments
    	String tokens[] = args.split("\\s+");
    	Player player = this.playerList.findPlayer(name);
    	double value;
    	
    	switch (cmd) {
			case "deposit":
			// Expects a double for how much player deposits
				value = Double.parseDouble(tokens[0]);
				double playerMoney = player.getMoney();
				
				if (value < 0) {
					return "Surprisingly, this is the only place in college you can't put yourself in debt";
				}
				
				else if (value == 0) {
					return "The teller looks at your deposit slip for $0.00 and frowns. She's not amused";
				}
				
				else if (playerMoney >= value) {
					player.setMoney(player.getMoney() - value);
					double newBalance = bank.deposit(name, value);
					return String.format("New account balance: $%.2f", newBalance);
				} else {
					return String.format("You don't have enough money to deposit $%.2f", value);
				}
			
			case "withdraw":
			// Expects a double for how much player withdraws 
				value = Double.parseDouble(tokens[0]);
				
				if(bank.canWithdraw(name, value)) {
					player.setMoney(player.getMoney() + value);
					return 	bank.withdraw(name, Long.parseLong(tokens[0])) + 
							String.format("\nNew wallet balance: $%.2f", player.getMoney());
				} else {
					return "You don't have enough money in your account";
				}
				
			case "printAccount":
				return bank.printAccount(name);	
		}
    	
    	return "";
    }
    
    /**
     * @author Group 4: King
     * Adds the player to list of players in store, and returns shop they just entered
     * @param name Name of the player to add to shop
     * @return The id of the shop the player will enter, -1 otherwise
     */
    public int shop(String name) {
    	Player player = this.playerList.findPlayer(name);
    	Room room = map.findRoom(player.getCurrentRoom());
    	
    	// Add player to shop in room if applicable
    	if (map.isShoppable(room)) {
    		return room.getId();
    	}
    	return -1;
    }

    /**
     * Returns Shop.tostring
     * @param id The shop's id in the hashmap
     * @return a reference to the shop
     */
    public String getShopStr(int id) {
    	return this.shoplist.get(id).toString();
    }

    /**
     * Allows player to sell an item to a shop, and increases their money
     * @author Team 4: King
     * @param name Name of the player
     * @param shopId The ID of the shop the player is selling an item to
     * @param item The item the player is selling (eventually will be an Item obj)
     */
    public double sellItem(String name, int shopId, String item) {
    	Player player = this.playerList.findPlayer(name);
    	Shop s = shoplist.get(shopId);
    	double value = 0;
    	
    	Item removed = player.removeObjectFromInventory(item);
    	if (removed != null) {
    		s.add(removed);
    		value = removed.price;
        	player.changeMoney(value);
    	}
    	
    	//int value = removed.getValue();
    	return value;
    }
    
	public String bribeGhoul(String playerName, String item){
		item = item.toLowerCase();
		Player player = playerList.findPlayer(playerName);
		Room room = this.map.findRoom(player.getCurrentRoom());
		Item object = player.removeObjectFromInventory(item);
		if(player == null){
			return null;
		}
		if(room.hasGhoul){
			//LinkedList<Item> itemList = player.getCurrentInventory();
			//boolean giveAble = false;
			//if (player.currentyInventory.size() > 0){
			//    giveAble = true;
			//    break;
            //}
			//for (String thing : itemList){
				//if(thing.equalsIgnoreCase(item)){
				//	giveAble = itemList.remove(thing);
				//	break;
				//}
            boolean giveAble = false;
            if (object != null){
                giveAble = true;
            }



			if(giveAble){
				try {
					GhoulLog myLog = new GhoulLog();
					myLog.glLog("GameCore","bribeGhoul", "Player" + " " + playerName + " has just given a " + object + " to the Ghoul");
				} catch (Exception e){
					e.printStackTrace();
				}

				ghoul.modifyAngryLevel(-1);
				int angryLv = ghoul.getAngryLevel();
				String message = "Ghoul gets " + item + ", " + "and its anger level decreases to " + angryLv + ".";
				return  message;
			}else{
				return "Do not have this item......";
			}
		}else{
			return "No Ghoul in this room";
		}

	}

	public String pokeGhoul(String playerName) {
		Player player = playerList.findPlayer(playerName);
		Room room = this.map.findRoom(player.getCurrentRoom());

		if (player != null) {
			if (!room.hasGhoul) {
				return "There is no ghoul in this room.";
			}

			try {
				GhoulLog myLog = new GhoulLog();
				myLog.glLog("GameCore","pokeGhoul", "Player" + " " + playerName + " has just poked the Ghoul");
				player.addPoke();
			} catch (Exception e){
				e.printStackTrace();
			}

			ghoul.modifyAngryLevel(1);
			int angerLvl = ghoul.getAngryLevel();
			if (angerLvl >= 7) {
				ghoul.Drag(player);
				draggedToSpawn(player);
			}
			return ("Ghoul anger level has increased to " + angerLvl);
		} else {
			return null;
		}}
    /**
     * 605B_buy_method
     * Allows player to sell an item to a shop, and increases their money
     * @author Team 4: Mistry
     * @param name Name of the player
     * @param shopId The ID of the shop the player is selling an item to
     * @param item The item the player is buying (eventually will be an Item obj)
     */
    public String buyItem(String name, int shopId, String itemName)
    {
    	double val = 0;
    	Player player = this.playerList.findPlayer(name);
    	Shop s = shoplist.get(shopId);
    	
    	Item item = null;
    	
    	for (Item ii : s.getInven()) 
    		if (ii.name.compareToIgnoreCase(itemName) == 0) 
    			item = ii;

    	if (item == null)  return "Item not in stock!!!";    	
    	
    	if(s.getInven().contains(item))
    	{
    		if (player.getMoney() > item.getPrice() * 1.2) {
    			s.remove(item);
    		}
    		else {
    			return "Not enough money!!!";
    		}
    	}
    	
    	
    	player.addObjectToInventory(item);
    
    	val = item.getPrice() * 1.2;
    	player.changeMoney(-val);
    	return "Thank you, that will be $" + val + ".";
    }

    /**
     * Takes the player into venmo. The new and improved way to exchange money with other players.
     * 
     * @author Team 4: Alaqeel
     * @param name Name of the player enter the bank
     */	@Override
	public String venmo(String name, ArrayList<String> tokens) {
		// checks :if the player forgot to enter enough commands
		if (tokens.isEmpty()) return "You need to provide more arguments.\n" + Venmo.instructions();
		
		// Gets the object of the caller player
		Player player1 = this.playerList.findPlayer(name);
		Player player2;
		double amount;
		// Executes the relevant commands
		switch(tokens.remove(0).toUpperCase()) {
			case "SEND": // sending a transaction
				if (tokens.isEmpty()) return "Specify recipient and amount. Type \"venmo help\" to learn more.";
				// gets the object of the receiving player
				player2 = this.playerList.findPlayer(tokens.remove(0));
				// checks that the name is correct
				if (player2 == null) return "Incorrect player name. Type \"venmo help\" to learn more."; 
				// checks if user entered a transaction amount
				if (tokens.isEmpty()) return "Specify transaction amount. Type \"venmo help\" to learn more.";
				
				// checks if the player entered a valid number
				try {
					amount = Float.parseFloat(tokens.remove(0));
				} catch (NumberFormatException e) {
					return "Please enter a valid number. Type \"venmo help\" to learn more.";
				}
				return Venmo.send(player1, player2, amount);
			case "OFFER": // offering a transaction
                if (tokens.isEmpty()) return "Specify recipient and amount. Type \"venmo help\" to learn more.";
                // gets the object of the receiving player
                player2 = this.playerList.findPlayer(tokens.remove(0));
                // checks that the name is correct
                if (player2 == null) return "Incorrect player name. Type \"venmo help\" to learn more."; 
                // checks if user entered a transaction amount
                if (tokens.isEmpty()) return "Specify transaction amount. Type \"venmo help\" to learn more.";
                
                // checks if the player entered a valid number
                try {
                    amount = Float.parseFloat(tokens.remove(0));
                } catch (NumberFormatException e) {
                    return "Please enter a valid number. Type \"venmo help\" to learn more.";
                }
                return Venmo.offer(player1, player2, amount);
			case "ACCEPT": // accepting a transaction
                if (tokens.isEmpty()) return "Enter the transaction ID. Type \"venmo help\" to learn more.";
                return Venmo.accept(player1, tokens.remove(0));
			case "REJECT": // rejecting a transaction
                if (tokens.isEmpty()) return "Enter the transaction ID. Type \"venmo help\" to learn more.";
                return Venmo.reject(player1, tokens.remove(0));
			case "LIST": // listing pending transactions
			    return Venmo.list(player1);
			case "HELP": // prints the help menu
				return "This is how you can use Venmo:\n" + Venmo.instructions();
			case "DEMO": // helpful for demo purposes
				if (!tokens.isEmpty() && tokens.remove(0).equalsIgnoreCase("********")) {
					player1.changeMoney(10);
					System.out.printf("[Venmo] %s excuted the demo command\n", player1.getName());
					return "Shush! Don't tell anyone that I added $10.00 to your wallet.";
				}
			default:
				return "Unknown argument.\n" + Venmo.instructions();
		}		
	}      
	
	/**
	 * Shows player how much money they have
	 * @param name Name of the player
	 * @return A string representation of the player's money
	 */
	public String wallet(String name) {
		Player player = this.playerList.findPlayer(name);
		double m = player.getMoney();
		
		return "$" + String.format("%.02f", m);
	}

	/**
     * Returns a Shop's inventory as a formatted string
     * @param id The shop ID
     * @return A formatted string representing the Shop's inventory
     */
    public String getShopInv(int id) {
		Shop s = this.shoplist.get(new Integer(id));
		return s.getObjects();
    }

    /**
     * Picks up multiple items of the name type
     * @param name name of the the player
     * @param target name of the item
     * @param amount amount of pickup
     * @return String indicating how many items was picked up
     */
    public String pickup(String name, String target, int amount) {
        Player player = this.playerList.findPlayer(name);
        if (player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            for (int x = 0; x < amount; x++) {
                if (player.currentInventory.size() < 10) {
                    Item object = room.removeObject(target);
                    if (object != null) {
                        player.addObjectToInventory(object);
                    } else {
                        if (x == 0) {
                            this.broadcast(player, player.getName()
                                    + " bends over to pick up something, but doesn't seem to find what they were looking for.");
                            return "You look around for a " + target + ", but can't find one.";
                        } else {
                            this.broadcast(player, player.getName() + " bends over to pick up " + amount + " " + target
                                    + "s but only picks up " + x + " " + target + "s");
                            return "You look around for " + amount + " " + target + "s but only picked up " + x + " "
                                    + target + "s";
                        }

                    }
                } else {
                    if (x == 0) {
                        return " your inventory is full.";
                    } else {
                        this.broadcast(player, player.getName() + " bends over to pick up " + amount + " " + target
                                + "s but only picks up " + x + " " + target + "s because of inventory space");
                        return "You look around for " + amount + " " + target + "s but only picked up " + x + " "
                                + target + "s because your inventory is full";
                    }
                }

            }
            this.broadcast(player,
                    player.getName() + " bends over to pick up " + amount + " " + target + "s that was on the ground.");
            return "You bend over and pick up " + amount + " "+target + "s.";

        } else {
            return null;
        }

    }
    
    /**
     * Attempts to pick up all objects in the room. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @return Message showing success. 
     */    
    public String pickupAll(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            LinkedList<Item> objects = room.removeAllObjects();
            if(objects != null && objects.size() > 0) {
                for (Item object : objects)
                {
                    player.addObjectToInventory(object);
                }
                this.broadcast(player, player.getName() + " bends over to pick up all objects that were on the ground.");
                return "You bend over and pick up all objects on the ground.";
            }
            else {
                this.broadcast(player, player.getName() + " bends over to pick up something, but doesn't find anything.");
                return "You look around for objects but can't find any.";
            }
        }
        else {
            return null;
        }
    }       

    /**
     * Returns a string of what and who you plan to offer an item to
     * @param srcName Name of player making offer
     * @param dstName Name of player receiving offer
     * @param message Object item being offered
     * @return Message showing status of offer
     */
    public String offer(String srcName, String message1, String junk, String message2){
	Player srcPlayer = this.playerList.findPlayer(srcName);
	String dstName = srcPlayer.getTradePartner();
	Player dstPlayer = this.playerList.findPlayer(dstName);
	Room room = map.findRoom(srcPlayer.getCurrentRoom());
	String returnMessage;
	Item object = srcPlayer.removeObjectFromInventory(message1);
	Item object2 = dstPlayer.removeObjectFromInventory(message2);
    if(!(srcPlayer.isInTrade() && srcPlayer.getTradePartner().equals(dstPlayer))){
        returnMessage = "You must request a trade with "+ dstName +", and they must accept this request before you can trade";
    }
        if (srcPlayer == dstPlayer)
		returnMessage = "So now we talking to ourselves? Is that what's hot?";
	else if (dstPlayer != null && (dstPlayer.getCurrentRoom() != srcPlayer.getCurrentRoom()))
	    returnMessage = "Player ain't in your room, or your life";
	else if (object == null)
	    returnMessage = "You ain't got that fool: " + message1;
	else if (dstPlayer == null)
	    returnMessage = "Player " + dstName + " not found.";
	else if (srcPlayer == null)
	    returnMessage = "Messge failed, check connection to server.";
	else {
	    dstPlayer.getReplyWriter().println(srcPlayer.getName() + " offers you an item: " + message1);
	    dstPlayer.getReplyWriter().println("for your: " + message2);
	    dstPlayer.getReplyWriter().println("Do you accept? (Type O_Reply accept/reject to answer)");
	    returnMessage = "You offer to " + dstPlayer.getName() + " an item: " + message1 + " for a: " + message2;
	}

    //I am using the tradeRequested flag as a way to indicate that this player has sent an offer 
    //tradeRequested and tradeReceived are set to false after the trade begins, 
    //but tradeRequested is turned on again once an offer is made
    //this way I can check in offerReply does not reply to an offer that was never made
    srcPlayer.setTradeRequest(true);
    srcPlayer.setTradeItem(object);
    dstPlayer.setTradeItem(object2);
	return returnMessage;
	}

    /**
     * Returns a string message about success of offer and status of inventory
     * @param dstName Name of player accepting or rejecting the offer
     * @param reply whther the offer has been accepted or rejected
     * @return Message showing status of offer reply
     */
    public String offerReply(String dstName, boolean reply){
        
        Player dstPlayer = this.playerList.findPlayer(dstName);

        if (dstPlayer == null)
            return "Player " + dstName + " not found.";

        String srcName = dstPlayer.getTradePartner();
        Player srcPlayer = this.playerList.findPlayer(srcName);

        if (srcPlayer == null)
            return "Player " + srcName + " not found";
        if (dstPlayer != null && (dstPlayer.getCurrentRoom() != srcPlayer.getCurrentRoom()))
            return "You must be in the same room as the player: " + srcPlayer.getCurrentRoom();
        if( !(dstPlayer.isInTrade()) ){
            return "You need to setup a trade to offer and receive items";
        }
        if(!srcPlayer.hasTradeRequest()){
            return "You need to have been offered an item by your trade partner to respond to an offer!";
        }
        else{
            Item itemRequested = dstPlayer.getTradeItem();
            Item itemOffered = srcPlayer.getTradeItem();
            if(itemRequested == null || itemOffered == null){
                return "The offer items have not been found in their respective inventories";
            }
            if(reply){//the offer is accepted
                srcPlayer.addObjectToInventory(itemRequested);
                dstPlayer.addObjectToInventory(itemOffered);  
            }
            else{
                srcPlayer.addObjectToInventory(itemOffered);
                dstPlayer.addObjectToInventory(itemRequested);  
            }

            srcPlayer.getReplyWriter().println("Your inventory now: "+srcPlayer.viewInventory());
            dstPlayer.getReplyWriter().println("Your inventory now: "+dstPlayer.viewInventory());


            srcPlayer.setTradeRequest(false);
            
            srcPlayer.setInTrade(false);
            dstPlayer.setInTrade(false);
            srcPlayer.setTradePartner("");
            dstPlayer.setTradePartner("");
            srcPlayer.setTradeItem(null);
            dstPlayer.setTradeItem(null);

        }

        return "";
    }

	public String examine(String srcName, String itemName)
	{
		Player pName = this.playerList.findPlayer(srcName);
		Room room = map.findRoom(pName.getCurrentRoom());
		LinkedList<Item> playerInv = pName.getCurrentInventory();
		for(int i = 0; i < playerInv.size(); i++)
		{
			if(playerInv.get(i).getName().equalsIgnoreCase(itemName))
			{
				return playerInv.get(i).getDiscrip();
			}
		}
		return "No item by the name of " + itemName + " is in your inventory.";
	}
	//Same functionality as bribe_ghoul, not currently used
	//public String giveToGhoul(String object, String playerName) {
	//	Player player = playerList.findPlayer(playerName);
	//	Room room = this.map.findRoom(player.getCurrentRoom());
	//	boolean isItem = false;
		
	//	if (player != null) {
	//		if (!room.hasGhoul) {
	//			return "There is no ghoul in this room.";
	//		}
	//		else {
	//			for (String s : player.getCurrentInventory()) {
	//				if (s.equals(object)) {
	//					isItem = true;
	//				}
	//			}
	//			if (! isItem) {
	//				return "you don't have" + object + "!";
	//			}
	//			player.getCurrentInventory().remove(object);
	//			ghoul.modifyAngryLevel(-1);
	//			return("the ghoul is a little more calm");
	//		}
	//	}
	//	else {
	//		return "failed to give ghoul item.";
	//	}
	//}

	/**
	 * Broadcasts a message to all other players in the same room as player.
	 * 
	 * @param player  Player initiating the action.
	 * @param message Message to broadcast.
	 */
	@Override
	public void broadcast(Player player, String message) {
		for (Player otherPlayer : this.playerList) {
			if (otherPlayer != player && otherPlayer.getCurrentRoom() == player.getCurrentRoom()) {
                // todo This is going to log the same transmission for N times for N = number plays in this room
				dailyLogger.write(message);
				otherPlayer.getReplyWriter().println(message);
			}
		}
	}

	/**
	 * Broadcasts a message to all players in the specified room.
	 * 
	 * @param room    Room to broadcast the message to.
	 * @param message Message to broadcast.
	 */
	@Override
	public void broadcast(Room room, String message) {
		for (Player player : this.playerList) {
			if (player.getCurrentRoom() == room.getId()) {
                //todo This is going to log the same transmission for N times for N = number plays in this room
				if(pickRPSToggle == false){
				dailyLogger.write(message);
			    String newMessage = player.filterMessage(message);
				player.getReplyWriter().println(newMessage);
				/* Delete this, functionality remains unchanged
				dailyLogger.write(message);
				player.getReplyWriter().println(message);
				*/
				}
				else{
					if(player.toggleChat == false){
						dailyLogger.write(message);
						String newMessage = player.filterMessage(message);
						player.getReplyWriter().println(newMessage);

					}
				}

			}
		}
	}

	/**
	 * Returns the player with the given name or null if no such player.
	 * 
	 * @param name Name of the player to find.
	 * @return Player found or null if none.
	 */
	@Override
	public Player findPlayer(String name) {
		for (Player player : this.playerList) {
			if (player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}
     /**
     * Prints message to player if request can processed, contacts other player about their request
     * @param requestingTrader Name of the player who has requested the trade
     * @param traderToRequest Name of the player whom the first player has requested to trade with
     */ 
    public void requestPlayer(String requestingTrader, String traderToRequest){
        Player playerToRequest = this.playerList.findPlayer(traderToRequest);
        Player requestingPlayer = this.playerList.findPlayer(requestingTrader);
        if(requestingTrader.equals(traderToRequest)){
            requestingPlayer.getReplyWriter().println("You cannot trade with yourself.");
            return;
        }
        else if(playerToRequest == null){
            requestingPlayer.getReplyWriter().println("This player does not exist, choose an existing trade partner");
            return;
        }
	    if(requestingPlayer.isInTrade()){
            requestingPlayer.getReplyWriter().println("There is already a trade in progress! You can only be in one trade at a time ");
            return;
        }
        if (playerToRequest.isInTrade()){
            requestingPlayer.getReplyWriter().println("There is already a trade in progress! "+traderToRequest+" can only be in one trade at a time ");
            return;
        
        }

        playerToRequest.setReceivedTrade(true);
        requestingPlayer.setTradeRequest(true);
        requestingPlayer.setTradePartner(traderToRequest);

        playerToRequest.getReplyWriter().println(requestingTrader + " has requested a trade. You may ignore this request or type: A_TRADE " +requestingTrader+" to proceed. ");
        requestingPlayer.getReplyWriter().println("Player has been contacted. You will receive a notification when they accept. ");
    }

    /**
     * Return string representation of trade acceptance
     * @param acceptingTrader Name of the player who is accepting the trade
     * @param traderToAccept Name of the player who has requested a trade
     * @return Message of success or fail
     */ 
    public String playerResponse(String acceptingTrader, String traderToAccept){

        Player playerToAccept = this.playerList.findPlayer(traderToAccept);
        Player acceptingPlayer = this.playerList.findPlayer(acceptingTrader); 
        if(playerToAccept == null){
            return "This player cannot be found. ";
        }
        if(traderToAccept.equals(acceptingTrader)){
            return "You can't trade with yourself. ";
        }
        if(acceptingPlayer.isInTrade()){
            return "There is already a trade in progress! You can only be in one trade at a time ";
        }
        if (playerToAccept.isInTrade()){
            return "There is already a trade in progress! "+traderToAccept+" can only be in one trade at a time ";
        }
        if(!acceptingPlayer.hasReceivedTrade()){
            return "You cannot accept a trade because you have not been asked to enter any trade";
        }
        if(!playerToAccept.hasTradeRequest() || !playerToAccept.getTradePartner().equals(acceptingTrader)){
            return "You cannot accept this trade because you have not been asked to enter a trade by "+ traderToAccept;
        }

        acceptingPlayer.setInTrade(true);
        playerToAccept.setInTrade(true);
        acceptingPlayer.setTradePartner(traderToAccept);
        
        acceptingPlayer.setReceivedTrade(false);
        playerToAccept.setTradeRequest(false);
        

        playerToAccept.getReplyWriter().println(playerToAccept.getTradePartner() + " has accepted your request.");
	playerToAccept.getReplyWriter().println("Your inventory: " + playerToAccept.viewInventory());
	playerToAccept.getReplyWriter().println("");
	playerToAccept.getReplyWriter().println(acceptingTrader + "'s inventory: " + acceptingPlayer.viewInventory());
	playerToAccept.getReplyWriter().println("");
	playerToAccept.getReplyWriter().println("To make offer type: O (your item name) for (their item name), and hit Enter");

        return "You have accepted to enter a trade with " + acceptingPlayer.getTradePartner();
    }


	/**
	 * Allows a player to join the game. If a player with the same name
	 * (case-insensitive) is already in the game, then this returns false.
	 * Otherwise, adds a new player of that name to the game. The next step is
	 * non-coordinated, waiting for the player to open a socket for message events
	 * not initiated by the player (ie. other player actions)
	 * 
     * @param name
     * @param password password hash for corresponding account.
	 * @return Player is player is added, null if player name is already registered
	 *         to someone else
     */
    @Override
	public Player joinGame(String name, String password) {
		synchronized (loginLock) {
			password = hash(password);
			// Check to see if the player of that name is already in game.
			Player player = this.playerList.findPlayer(name);
			if (player != null)
				return null;
			PlayerAccountManager.AccountResponse resp = accountManager.getAccount(name, password);
			if (!resp.success())
				return null;
			player = resp.player;
			this.playerList.addPlayer(player);

            //112a DormRoom creation
            player.setDormId(dormCountId);
            DormRoom dorm = new DormRoom(dormCountId,"inside","Dorm Room","Your very own, personal dorm room!");                                                                                                   dorm.addExit(Direction.valueOf("NORTH"),-100000,"You go back to the elevator");
            dorm.addExit(Direction.valueOf("EAST"),-100000,"You go back to the elevator");
            dorm.addExit(Direction.valueOf("SOUTH"),100000,"You go back to the elevator");
            dorm.addExit(Direction.valueOf("WEST"),-100000,"You go back to the elevator");
	    dorm.addNPC("HAL_9000",dormCountId);
            dorm.setChest(player.chestImage);//point to the chest
            this.map.addRoom(dorm);
            if(player.getCurrentRoom() > 100000){player.setCurrentRoom(dormCountId);}
            dormCountId++;

			this.broadcast(player, player.getName() + " has arrived.");
			connectionLog(true, player.getName());
			return player;
		}
	}

	/**
	 * Allows a player to create an account. If the player name already exists this
	 * returns the corresponding enum. If the players name is of an invalid format
	 * this returns that corresponding emum. Otherwise this returns success and
	 * calls joinGame.
	 * 
	 * @param name
	 * @param password
	 * @param recovery List of recovery questions and answers, ordered q1,a1,q2,a2,q3,a3
	 * @return an enumeration representing the creation status.
	 */
	@Override

	public synchronized Responses createAccountAndJoinGame(String name, String password) {
		synchronized (createAccountLock) {
			PlayerAccountManager.AccountResponse resp = accountManager.createNewAccount(name, hash(password));
			if (!resp.success())
				return resp.error;
			if (joinGame(name, password) != null)
				return Responses.SUCCESS;
			return Responses.UNKNOWN_FAILURE;
		}
	}

       	/**
	 * Returns a look at the area of the specified player.
 * 
	 * @param playerName Player Name
	 * @return String representation of the current area the player is in.
	 */
	@Override
	public String look(String playerName) {
		Player player = playerList.findPlayer(playerName);

		if (player != null) {
			// Find the room the player is in.
			Room room = this.map.findRoom(player.getCurrentRoom());

			// Send a message to all other players in the room that this player is looking
			// around.
			this.broadcast(player, player.getName() + " takes a look around.");

			// Return a string representation of the room state.
			// return room.toString(this.playerList, player);
			// modified in 2018.10.17, which for player can look ghoul.
			if (room.hasGhoul) {
				String watchGhoul = "\n\nTHERE IS A GHOUL IN THE ROOM!!!!!!\n\n";
				return room.toString(this.playerList, player) + watchGhoul;
			} else {
				return room.toString(this.playerList, player);
			}
		}
		// No such player exists
		else {
			return null;
		}
	}

	/**
	 * Turns the player left.
	 * 
	 * @param name Player Name
	 * @return String message of the player turning left.
	 */
	@Override
	public String left(String name) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			// Compel the player to turn left 90 degrees.
			player.turnLeft();

			// Send a message to every other player in the room that the player has turned
			// left.
			this.broadcast(player, player.getName() + " turns to the left.");

			// Return a string back to the calling function with an update.
			return "You turn to the left to face " + player.getCurrentDirection();
		} else {
			return null;
		}
	}

	/**
	 * Turns the player right.
	 * 
	 * @param name Player Name
	 * @return String message of the player turning right.
	 */
	@Override
	public String right(String name) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			// Compel the player to turn left 90 degrees.
			player.turnRight();

			// Send a message to every other player in the room that the player has turned
			// right.
			this.broadcast(player, player.getName() + " turns to the right.");

			// Return a string back to the calling function with an update.
			return "You turn to the right to face " + player.getCurrentDirection();
		} else {
			return null;
		}
	}

	/**
	 * Says "message" to everyone in the current area.
	 * 
	 * @param name    Name of the player to speak
	 * @param message Message to speak
	 * @return Message showing success.
	 */
	@Override
	public String say(String name, String message) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
		    for (Player otherPlayer : this.playerList)
		        if (otherPlayer != player && otherPlayer.getCurrentRoom() == player.getCurrentRoom())
		            otherPlayer.messagePlayer(player, "says", message);
            chatLog(player, 0, message, "Room " + player.getCurrentRoom());
            return player.getMessage() + "say, " + message;

		} else {
			return null;
		}
	}

	public String draggedToSpawn(Player player) {
		if (player == null) {
			return null;
		}
		this.broadcast(player, player.getName() + "has been knocked out by the ghoul!");
		player.setCurrentRoom(1);
		this.broadcast(player, player.getName() + " woke up in the area");
		player.getReplyWriter().println(this.map.findRoom(player.getCurrentRoom()).toString(playerList, player));
		if (player.getCurrentInventory().size() > 0) {
			Random rn = new Random();
			int rand = rn.nextInt((player.getCurrentInventory().size() - 1) - 0 + 1) + 0;
			player.getCurrentInventory().remove(player.getCurrentInventory().get(rand));
		}
		return "You wake up and find yourself at the clock tower, with a lighter burden.";
	}

	/**
	 * Attempts to walk forward < distance > times. If unable to make it all the
	 * way, a message will be returned. Will display LOOK on any partial success.
	 * 
	 * @param name     Name of the player to move
	 * @param distance Number of rooms to move forward through.
	 * @return Message showing success.
	 */
	public String move(String name, int distance) {
		Player player = this.playerList.findPlayer(name);
		if (player == null || distance <= 0) {
			return null;
		}
		Room room;
		while (distance-- != 0) {
			room = map.findRoom(player.getCurrentRoom());
			if (room.canExit(player.getDirection())) {
				this.broadcast(player, player.getName() + " has walked off to the " + player.getCurrentDirection());
				player.getReplyWriter().println(room.exitMessage(player.getDirection()));
				player.setCurrentRoom(room.getLink(player.getDirection()));
				this.broadcast(player, player.getName() + " just walked into the area.");
				Ghost g = new Ghost(player);
				g.start();
				player.getReplyWriter()
						.println(this.map.findRoom(player.getCurrentRoom()).toString(playerList, player));
			} else {
				player.getReplyWriter().println(room.exitMessage(player.getDirection()));
				return "You grumble a little and stop moving.";
			}
		}
		return "You stop moving and begin to stand around again.";
	}
	

    /**115 jorge team 6
    *This takes the player into the chest action menu
    * where they can move things between their pockets 
    * and the chest in their dorm room 
    * this should only run when the player is 
    * in the dorm room 
     */
    public String chest (String name, String opt, String input)  {
        
        Player player = this.playerList.findPlayer(name);
        Room droom = map.findRoom(player.getCurrentRoom());

        switch (opt){

            case "check"://ensure player is in his room 

                if(player.getCurrentRoom() != player.getDormId()) {                                                      
                           return ("not in dorm room");
                            
                 }return "ok";
            case "menu":
               return ((DormRoom)droom).chestMenu();
            case "a"://adds an item to the chest 
                Item object = player.removeObjectFromInventory(input);
                if(object != null) {                                                               
                      return ((DormRoom)droom).addObjectToChest(object);
                }else{
                    return "error";
                }
            case "p": //print the chest content
                return ((DormRoom)droom).printChest();
            case "x":
                object = ((DormRoom)droom).removeObjectfromChest(input);
                if(object != null) {                     
                     player.addObjectToInventory(object);
                     return "ok";  
                }else{
                    return "error";
                }
        }//end switch

            return "chest switch error in GameCore chest()";
    }//end chest command  


	/**
     * Attempts to walk towards <direction> 1 time.  If unable to make it all the way,
     *  a message will be returned.  Will display LOOK on any partial success.
     * @param name Name of the player to move
     * @param distance Number of rooms to move forward through.
     * @return Message showing success.
     */
    public String move(String name, Direction direction) {
        Player player = this.playerList.findPlayer(name);
        if(player == null) {
            return null;
        }
        Room room = map.findRoom(player.getCurrentRoom());
        if(room.canExit(direction)) {
            this.broadcast(player, player.getName() + " has walked off to the " + direction);
            player.getReplyWriter().println(room.exitMessage(direction));
            //private room redirection 112a
            if(room.getLink(direction) == 100001)
            {    
                player.setCurrentRoom(player.getDormId());
            }    
            else 
            {    
                player.setCurrentRoom(room.getLink(direction));
            }    
//            player.setCurrentRoom(room.getLink(direction));
            String logMessage = String.format("%s used command MOVE %s [moved from %s to %s]", player.getName(), direction.toString(), room.getTitle(), map.findRoom(player.getCurrentRoom()).getTitle());
			this.broadcast(player, player.getName() + " just walked into the area.");
			Ghost g = new Ghost(player);
				g.start();
            player.getReplyWriter().println(this.map.findRoom(player.getCurrentRoom()).toString(playerList, player));
        } else {
            String logMessage  = String.format("%s used command MOVE %s [unable to move in direction]", player.getName(), direction.toString());
            player.getReplyWriter().println(room.exitMessage(direction));
            return "You grumble a little and stop moving.";
        }
        return "You stop moving and begin to stand around again.";
	}

    /**
     * Attempts to pick up an object < target >. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @param target The case-insensitive name of the object to pickup.
     * @return Message showing success.
     */
    public String pickup(String name, String target) {
        Player player = this.playerList.findPlayer(name);
        if(player != null)  {
            //Demonstration purpose only
            Room room = map.findRoom(player.getCurrentRoom());
            NPC npc = room.getNPCs().get("questNPC");
            if (npc != null)
            {
                player.getDialogueIdFromList("questNPC", "pickup");
                if (player.currentInventory.size() < 3)
                {
                    player.updateDialogueList(npc.getName(), "pickup", 1);
                }
                else
                {
                    player.updateDialogueList(npc.getName(), "pickup", 2);
                }
            }

            if (player.currentInventory.size() <10){
                room = map.findRoom(player.getCurrentRoom());
                Item object = room.removeObject(target);
                if(object != null) {
                    player.addObjectToInventory(object);
                    this.broadcast(player, player.getName() + " bends over to pick up a " + target + " that was on the ground.");
                    return "You bend over and pick up a " + target + ".";
                }
                else {
                    this.broadcast(player, player.getName() + " bends over to pick up something, but doesn't seem to find what they were looking for.");
                    return "You look around for a " + target + ", but can't find one.";
                }
            }
            else {
                return " your inventory is full.";
            }
        }
        else {
            return null;
        }
    }

    /**
     * Attempts to drops an object < target >. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @param target The case-insensitive name of the object to drop.
     * @return Message showing success.
     */
    public String drop(String name, String target) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());

            //Demonstration purpose only
            NPC npc = room.getNPCs().get("questNPC");
            if (npc != null)
            {
                player.getDialogueIdFromList("questNPC", "pickup");
                if (player.currentInventory.size() < 3)
                {
                    player.updateDialogueList(npc.getName(), "pickup", 1);
                }
                else
                {
                    player.updateDialogueList(npc.getName(), "pickup", 2);
                }
            }

            Item object = player.removeObjectFromInventory(target);
            if(object != null) {
                room.addObject(object);
                this.broadcast(player, player.getName() + " rummages their inventory to find a " + target);
                return "You drop a " + target + " into the room.";
            }
            else {
                this.broadcast(player, player.getName() + " tries to drop something, but doesn't seem to find what they were looking for.");
                return "You look around for a " + target + " in your pockets, but can't find one.";
            }
        }
        else {
            return null;
        }
    }       

    /**
     * Attempts to use an item in the player's inventory. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @param itemName name of item to use
     * @return Message showing success. 
     */    
    public String useItem(String name, String itemName) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
	    Item usedItem = player.removeObjectFromInventory(itemName);
	    if(usedItem != null) {
		player.setTitle(usedItem.getFlavor());
		player.setHasTitle(true);
                this.broadcast(player, player.getName() + " used " + usedItem.getName());
                return "You have used the item, and it magically disappears into the void.";
            }
            else {
                this.broadcast(player, player.getName() + " tried to use " + itemName + ", but couldn't find it.");
                return "You tried to use an item that you don't have.";
            }
        }
        else {
            return null;
        }
    }

    /** 
     * Gets the title of a player
     * @param name name of the player
     * @return the title given by the item used if applicable
     */
    public String getPlayerTitle(String name) {
	Player player = this.playerList.findPlayer(name);
	if(player != null) {
		return player.getTitle();
	}
	else {
		return null;
	}
    }

    /**
     * Removes the title from a player
     * @param name name of player
     */
    public boolean removePlayerTitle(String name) {
	Player player = this.playerList.findPlayer(name);
	if(player != null) {
		player.setTitle("");
		player.setHasTitle(false);
		return true;
	}
	return false;
    }

    /**
     * Attempts to erase the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardErase(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            room.whiteboardErase();
            this.broadcast(player, player.getName() + " erases the text on the whiteboard.");
            dailyLogger.write(player.getName(), "WHITEBOARD ERASE", room.getTitle());
            return "You erase the text on the whiteboard.";
        }
        else {
            return null;
        }
    }

    /**
     * Attempts to read the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardRead(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            String text = room.getWhiteboardText();
            dailyLogger.write(player.getName(), "WHITEBOARD READ", room.getTitle());
            this.broadcast(player, player.getName() + " reads the text on the whiteboard.");
            if (text != null && !text.isEmpty()) {
                return "The whiteboard says: \"" + text + "\".";
            }
            else {
                return "The whiteboard is empty";
            }

        }
        else {
            return null;
        }
    }

    /**
     * Attempts to  the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @param text Text to write on the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardWrite(String name, String text) {
        try {
            Player player = this.playerList.findPlayer(name);
            if(player != null) {
                Room room = map.findRoom(player.getCurrentRoom());
                boolean success = room.addWhiteboardText(text);
                dailyLogger.write(player.getName(), "WHITEBOARD WRITE", text, room.getTitle());
                if (success) { 
                    this.broadcast(player, player.getName() + " writes on the whiteboard.");
                    return "You write text on the whiteboard.";
                }
                else {
                    this.broadcast(player, player.getName() + " tries to write on the whiteboard, but it's full.");
                    return "You try to write text on the whiteboard, but there's not enough space.";
                }
            }
            else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
	 * Returns a string representation of all objects you are carrying.
	 * 
	 * @param name Name of the player to move
	 * @return Message showing success.
	 */
	@Override
	public String inventory(String name) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			this.broadcast(player, "You see " + player.getName() + " looking through their pockets.");
			return "You look through your pockets and see" + player.viewInventory();
		} else {
			return null;
		}
	}
    /**
     * Returns a string representation of all objects you are carrying.
     * @param name Name of the player to move
     * @return Message showing success.
     */
    public String sort(String name, String modes) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            player.sortCurrentInventory(modes);
            return "You sort the items in your inventory";
        }
        else {
            return null;
        }
    }

    @Override
    public String challenge(String challenger, String challengee){
      Player playerChallenger = this.playerList.findPlayer(challenger);
      Player playerChallengee = this.playerList.findPlayer(challengee);
      if(playerChallengee == null || playerChallenger == null){
        return "This player does not exist in the game.";
      }
      if(playerChallenger.getInBattle() == true){
        return "You are already in a R-P-S battle.";
      }
      if(playerChallengee.getInBattle()){
        return "This player is already in a R-P-S battle";
      }
      if(playerChallengee.getInBattle() == true){
        return playerChallengee.getName() + " is already in a R-P-S battle.";
      }
      if(playerChallenger != playerChallengee && playerChallenger.getCurrentRoom() == playerChallengee.getCurrentRoom()) {
        playerChallengee.setChallenger(challenger);
        playerChallenger.setChallenger(challengee);
        playerChallengee.setHasChallenge(true);
        playerChallengee.getReplyWriter().println(playerChallenger.getName() + " challenges you to a R-P-S.");

        return "You challenged " + playerChallengee.getName() + " to a R-P-S.";
      }
      else if(playerChallenger == playerChallengee)
        return "You can't challenge yourself to R-P-S.";
      else {
        return "This person is not in the same room as you or doesn't exist in the game.";
      }
    }

    @Override
    public String accept(String challengee, String challenger, String sRounds){
      Player playerChallenger = this.playerList.findPlayer(challenger);
      Player playerChallengee = this.playerList.findPlayer(challengee);
      int rounds = 0;
      if(playerChallengee == null || playerChallenger == null){
        return "This player does not exist in the game.";
      }
      switch(sRounds){
        case "1":
        case "ONE":
            rounds = 1;
            break;
        case "3":
        case "THREE":
            rounds = 3;
            break;
        case "5":
        case "FIVE":
            rounds = 5;
            break;
      }
      if(rounds != 1 && rounds != 3 && rounds != 5){
        return "This is an invalid number of rounds, please choose from 1, 3, or 5 rounds: ";
      }
      if(playerChallengee.getChallenger().equals(playerChallenger.getName()) && playerChallengee.getHasChallenge() == true){
        if(playerChallenger != playerChallengee && playerChallenger.getCurrentRoom() == playerChallengee.getCurrentRoom()) {
          playerChallenger.setRounds(rounds);
          playerChallengee.setRounds(rounds);
          playerChallenger.getReplyWriter().println(playerChallengee.getName() + " accepts your challenge to a R-P-S for " + rounds + " rounds");
          playerChallengee.setHasChallenge(false);
          playerChallengee.setInBattle(true);
          playerChallenger.setInBattle(true);
          playerChallengee.getReplyWriter().println("You accept " + playerChallenger.getName() + "\'s challenge to a R-P-S for " + rounds + " rounds");
          playerChallenger.getReplyWriter().println("Entering Round\nPick rock, paper, or scissors: ");
          return "Entering Round\nPick rock, paper, or scissors: ";
        }
        else
        {
          return "This person is not in the same room as you or doesn't exist in the game.";
        }
      }
      else if(playerChallenger == playerChallengee){
        return "You can't challenge yourself to R-P-S.";
      }
      else{
        return "You have not been challenged by " + playerChallenger.getName();
      }
    }

    @Override
    public String reject(String challengee, String challenger){
      Player playerChallenger = this.playerList.findPlayer(challenger);
      Player playerChallengee = this.playerList.findPlayer(challengee);
      if(playerChallengee == null || playerChallenger == null){
        return "This player does not exist in the game.";
      }
      if(playerChallengee.getChallenger().equals(playerChallenger.getName()) && playerChallengee.getHasChallenge() == true){
        if(playerChallenger != playerChallengee && playerChallenger.getCurrentRoom() == playerChallengee.getCurrentRoom()) {
          playerChallengee.setChallenger(" ");
          playerChallenger.setChallenger(" ");
          playerChallengee.setHasChallenge(false);
          playerChallenger.getReplyWriter().println(playerChallengee.getName() + " rejects your challenge to a R-P-S");
          return "You reject " + playerChallenger.getName() + "\'s challenge to a R-P-S.";
        }
        else if(playerChallenger == playerChallengee)
          return "You can't challenge yourself to R-P-S.";
        else {
          return "This person is not in the same room as you or doesn't exist in the game.";
        }
      }
      else if(playerChallenger == playerChallengee){
        return "You can't challenge yourself to R-P-S.";
      }
      else{
        return "You have not been challenged by " + playerChallenger.getName();
      }
    }
    private void rpsLog(String winner, String loser, String status, String winnerPick, String loserPick){
	    rpsLogger.info(winner + " " + status + " against " + loser + "\n" + 
              winner +  " pick " + winnerPick + ", " + loser +  " pick " + loserPick + "\n");
    }
    private void rpsLogger() throws IOException {
       rpsHandler = new FileHandler("battles.log", true);
       SimpleFormatter simpleformat = new SimpleFormatter();
       rpsHandler.setFormatter(simpleformat);
       rpsLogger.setLevel(Level.ALL);
       rpsLogger.setUseParentHandlers(false);
       rpsLogger.addHandler(rpsHandler);
    }

    @Override
    public String pickRPS(String name,  String option){
      Player player = this.playerList.findPlayer(name);
      Player challengee = this.playerList.findPlayer(player.getChallenger());
      pickRPSToggle = true;

      if(player.getInBattle() == true && player.getRounds() > 0){
        if(player.getOption().equals("ROCK") || player.getOption().equals("PAPER") || player.getOption().equals("SCISSORS")){
          return "You already pick rock, paper or scissors. You picked " + player.getOption();
        }
        player.setOption(option);
        challengee.setChallengerOption(option);
        String winner = "";

        if(challengee.getOption().equals("ROCK") || challengee.getOption().equals("PAPER") || challengee.getOption().equals("SCISSORS")){
          player.setRounds(player.getRounds() - 1);
          challengee.setRounds(challengee.getRounds()-1);
          switch(player.getOption()) {
            case "ROCK":
              player.getReplyWriter().println("You chose ROCK.");
              if (challengee.getOption().equals("PAPER")) {
                challengee.getReplyWriter().println("You chose PAPER.");
                player.getReplyWriter().println(challengee.getName() + " chose PAPER: You lose.");
                challengee.getReplyWriter().println(player.getName() + " chose ROCK: You win.");
                winner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + challengee.getName() + " won this round.";
                challengee.setWins(challengee.getWins()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(player.getName(), challengee.getName(), "wins", player.getOption(), challengee.getOption());
	      }		      
              else if (challengee.getOption().equals("ROCK")){
                challengee.getReplyWriter().println("You chose ROCK.");
                player.getReplyWriter().println(challengee.getName() + " chose ROCK: It is a tie.");
                challengee.getReplyWriter().println(player.getName() + " chose ROCK: It is a tie.");
                winner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: It is a tie this round.";
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(player.getName(), challengee.getName(), "ties", player.getOption(), challengee.getOption());
              	
	      }
              else {
                challengee.getReplyWriter().println("You chose SCISSORS.");
                player.getReplyWriter().println(challengee.getName() + " chose SCISSORS: You win.");
                challengee.getReplyWriter().println(player.getName() + " chose ROCK: You lose.");
                winner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won this round.";
                player.setWins(player.getWins()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(challengee.getName(), player.getName(), "wins", challengee.getOption(), player.getOption());
              	
              }
              if(player.getRounds() > 0){
                player.setOption("");
                challengee.setOption("");
                challengee.getReplyWriter().println("You are entering the next round with a score of " + challengee.getWins() + " to " + player.getWins() + "\nPick rock, paper, or scissors: ");
                player.getReplyWriter().println("You are entering the next round with a score of " + player.getWins() + " to " + challengee.getWins() + "\nPick rock, paper, or scissors: ");
                //player.setRounds(player.getRounds() - 1);
              }
              else{

                int p1Win = player.getWins();
                int p2Win = challengee.getWins();
                if(p1Win > p2Win){
                    String winner2 = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
		    pickRPSToggle = false;
                }
                else if(p2Win > p1Win){
                    String winner2 = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
		    pickRPSToggle = false;
                }
                else{
                    String noWinner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: They tied in the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), noWinner);
		    pickRPSToggle = false;
                }

                player.setInBattle(false);
                player.setChallenger(" ");
                player.setOption("");
                player.setWins(0);
                challengee.setChallenger(" ");
                challengee.setInBattle(false);
                challengee.setOption("");
                challengee.setWins(0);
              }
              break;
            case "PAPER":
              player.getReplyWriter().println("You chose PAPER.");
              if (challengee.getOption().equals("SCISSORS")) {
                challengee.getReplyWriter().println("You chose SCISSORS.");
                player.getReplyWriter().println(challengee.getName() + " chose SCISSORS: You lose.");
                challengee.getReplyWriter().println(player.getName() + " chose PAPER: You win.");
                winner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + challengee.getName() + " won this round.";
                challengee.setWins(challengee.getWins()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(challengee.getName(), player.getName(), "wins", challengee.getOption(), player.getOption());
              	
	      }
              else if (challengee.getOption().equals("PAPER")){
                challengee.getReplyWriter().println("You chose PAPER.");
                player.getReplyWriter().println(challengee.getName() + " chose PAPER: It is a tie.");
                challengee.getReplyWriter().println(player.getName() + " chose PAPER: It is a tie.");
                winner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: It is a tie this round.";
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(player.getName(), challengee.getName(), "ties", player.getOption(), challengee.getOption());
              
	      }
              else {
                challengee.getReplyWriter().println("You chose ROCK.");
                player.getReplyWriter().println(challengee.getName() + " chose ROCK: You win.");
                challengee.getReplyWriter().println(player.getName() + " chose PAPER: You lose.");
                winner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won this round.";
                player.setWins(player.getWins()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(player.getName(), challengee.getName(), "wins", player.getOption(), challengee.getOption());
              	
              }
              if(player.getRounds() > 0){

                player.setOption("");
                challengee.setOption("");
                challengee.getReplyWriter().println("You are entering the next round with a score of " + challengee.getWins() + " to " + player.getWins() + "\nPick rock, paper, or scissors: ");
                player.getReplyWriter().println("You are entering the next round with a score of " + player.getWins() + " to " + challengee.getWins() + "\nPick rock, paper, or scissors: ");
                //player.setRounds(player.getRounds() - 1);
              }
              else{

                int p1Win = player.getWins();
                int p2Win = challengee.getWins();
                if(p1Win > p2Win){
                    String winner2 = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
                    pickRPSToggle = false;
		}
                else if(p2Win > p1Win){
                    String winner2 = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
		    pickRPSToggle = false;
                }
                else{
                    String noWinner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: They tied in the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), noWinner);
		    pickRPSToggle = false;
                }

                player.setInBattle(false);
                player.setChallenger(" ");
                player.setOption("");
                player.setWins(0);
                challengee.setChallenger(" ");
                challengee.setInBattle(false);
                challengee.setOption("");
                challengee.setWins(0);
              }
              break;
            case "SCISSORS":
              player.getReplyWriter().println("You chose SCISSORS.");
              if (challengee.getOption().equals("ROCK")) {
                challengee.getReplyWriter().println("You chose ROCK.");
                player.getReplyWriter().println(challengee.getName() + " chose ROCK: You lose.");
                challengee.getReplyWriter().println(player.getName() + " chose SCISSORS: You win.");
                winner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + challengee.getName() + " won this round.";
                challengee.setWins(challengee.getWins()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(challengee.getName(), player.getName(), "wins", challengee.getOption(), player.getOption());
              	
	      }
              else if (challengee.getOption().equals("SCISSORS")){
                challengee.getReplyWriter().println("You chose SCISSORS.");
                player.getReplyWriter().println(challengee.getName() + " chose SCISSORS: It is a tie.");
                challengee.getReplyWriter().println(player.getName() + " chose SCISSORS: It is a tie.");
                winner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: It is a tie this round.";
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(challengee.getName(), player.getName(), "ties", challengee.getOption(), player.getOption());
           
	      }
              else {
                challengee.getReplyWriter().println("You chose PAPER.");
                player.getReplyWriter().println(challengee.getName() + " chose PAPER: You win.");
                challengee.getReplyWriter().println(player.getName() + " chose SCISSORS: You lose.");
                winner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won this round.";
                player.setWins(player.getWins()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(player.getName(), challengee.getName(), "wins", player.getOption(), challengee.getOption());
              	
              }
              if(player.getRounds() > 0){

                player.setOption("");
                challengee.setOption("");
                challengee.getReplyWriter().println("You are entering the next round with a score of " + challengee.getWins() + " to " + player.getWins() + "\nPick rock, paper, or scissors: ");
                player.getReplyWriter().println("You are entering the next round with a score of " + player.getWins() + " to " + challengee.getWins() + "\nPick rock, paper, or scissors: ");
                //player.setRounds(player.getRounds() - 1);
              }
              else{

                int p1Win = player.getWins();
                int p2Win = challengee.getWins();
                if(p1Win > p2Win){
                    String winner2 = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
		    pickRPSToggle = false;
		    player.addRpsVictory();//Win counter for Main Questline
                }
                else if(p2Win > p1Win){
                    String winner2 = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
		    pickRPSToggle = false;
		    challengee.addRpsVictory();//Win counter for Main Questline
                }
                else{
                    String noWinner = player.getName() + " challenged " + challengee.getName() + " to a Rock Paper Scissors Battle: They tied in the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), noWinner);
		    pickRPSToggle = false;
                }

                player.setInBattle(false);
                player.setChallenger(" ");
                player.setOption("");
                player.setWins(0);
                challengee.setChallenger(" ");
                challengee.setInBattle(false);
                challengee.setOption("");
                challengee.setWins(0);
              }
              break;
            default:
              break;
          }
        }
        //player.setRounds(player.getRounds() - 1);
        //challengee.setRounds(challengee.getRounds()-1);
        return ""; 
      }
      else
        return "You are not in a R-P-S challenge.";
    }

	/**
	 * Leaves the game.
	 * 
	 * @param name Name of the player to leave
	 * @return Player that was just removed.
	 */
	@Override
	public Player leave(String name) {
		Player player = this.playerList.findPlayer(name);
        Room droom = map.findRoom(player.getDormId());
		if (player != null) {
            player.chestImage = ((DormRoom)droom).getChest();    
			this.broadcast(player, "You see " + player.getName() + " heading off to class.");
			this.playerList.removePlayer(name);
            connectionLog(false, player.getName());
            this.accountManager.forceUpdateData(player);
			return player;
		}
		return null;
	}

	/**
     * Whispers "message" to a specific player.
     * @param srcName Name of the player to speak
     * @param dstName Name of the player to receive
     * @param message Message to speak
     * @return Message showing success
     */
    public String whisper(String srcName, String dstName, String message){
        Player srcPlayer = this.playerList.findPlayer(srcName);
        Player dstPlayer = this.playerList.findPlayer(dstName);
        if (dstPlayer == null)
            return "Player " + dstName + " not found.";
        if (!dstPlayer.messagePlayer(srcPlayer, "whispers", message))
            return "Player " + dstPlayer.getName() + " is ignoring you.";
        dstPlayer.setLastPlayer(srcName);
        chatLog(srcPlayer, 1, message, dstPlayer.getName());
        return srcPlayer.getMessage() + "whisper to " + dstPlayer.getName() + ", " + message;
    }

    /**
     * Reply "message" to last whisper.
     * @param srcName Name of the player to speak
     * @param message Message to speak
     * @return Message showing success
     */
    public String quickReply(String srcName, String message) {
        String target = this.playerList.findPlayer(srcName).getLastPlayer();
        return whisper(srcName, target, message);
    }

   /**
     * Player ignores further messages from another Player
     * @param srcName Player making the ignore request
     * @param dstName Player to be ignored
     * @return Message showing success
     */
    public String ignorePlayer(String srcName, String dstName) {
        Player srcPlayer = this.playerList.findPlayer(srcName);
        Player dstPLayer = this.playerList.findPlayer(dstName);
        String returnMessage;
        if (dstPLayer == null)
            returnMessage = "Player " + dstName + " not found.";
        else if (srcPlayer == null)
            returnMessage = "Ignore failed, check connection to server.";
        else if (srcPlayer.getName() == dstPLayer.getName())
            returnMessage = "You cannot ignore yourself! <no matter how hard you try>";
        else if (srcPlayer.isIgnoring(dstPLayer))
            returnMessage = "You're already ignoring " + dstPLayer.getName() + "!";
        else {
            srcPlayer.ignorePlayer(dstPLayer);
            returnMessage = "You're now ignoring " + dstPLayer.getName() + ".";
        }
        return returnMessage;
    }

    /**
     * Player unIgnores further messages from another Player
     * @param srcName Player making the unIgnore request
     * @param dstName Player to be unIgnored
     * @return Message showing success
     */
    public String unIgnorePlayer(String srcName, String dstName) {
        Player srcPlayer = this.playerList.findPlayer(srcName);
        Player dstPLayer = this.playerList.findPlayer(dstName);
        String returnMessage;
        if (dstPLayer == null)
            returnMessage = "Player " + dstName + " not found.";
        else if (srcPlayer == null)
            returnMessage = "Unignore failed, check connection to server.";
        else if (srcPlayer.getName() == dstPLayer.getName())
            returnMessage = "You never ignored yourself in the first place";
        else if (!srcPlayer.isIgnoring(dstPLayer))
            returnMessage = "You aren't ignoring " + dstPLayer.getName() + "!";
        else {
            srcPlayer.unIgnorePlayer(dstPLayer);
            returnMessage = "You're no longer ignoring " + dstPLayer.getName() + ".";
        }
        return returnMessage;
    }

    /**
     * Player displays the list of players that are being ignored
     * @param name Player who's list is being targeted
     * @return The list of players being ignored
     */
    public String getIgnoredPlayersList(String name) {
        Player player = this.playerList.findPlayer(name);
        String returnMessage;
        if(player != null){
            returnMessage = player.getIgnoredPlayersList();
        }else{
            returnMessage = "Error: Could not find player. Check server connection status";
        }
        return returnMessage;
    }

    /**
     * Tells a joke to the room. Reads local "chat config" file
     * that keeps a list of jokes, one per line. The command
     * chooses a random joke.
     * NOTE: Importing Scanners, File, ArrayList, Random, and
     * FileNotFoundException for this method.
     * @param filename the "chat config" file to read the joke from.
     * */
    public String joke(String filename){
      File file = new File(filename);
      try{
      Scanner sc = new Scanner(file);
      // using ArrayList to store jokes in file for randomization.
      ArrayList<String> joke = new ArrayList<String>();

      while (sc.hasNextLine()){
        joke.add(sc.nextLine());
      }

      sc.close();
      Random r = new Random();
      return joke.get(r.nextInt(joke.size()));
      }
      catch (FileNotFoundException e){
        return ("File not found. Please add a joke.");
      }
    }

	/**
	 * Initiates dialogue with NPC
	 * @param playerName Player name
	 * @param npcName NPC name
	 * @return Dialogue options for player
	 */
    public String talkNpc(String name, String npcName) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            NPC npc = room.getNPCs().get(npcName);
            if (npc != null) {

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < npc.getDialogueList().size(); i++) {
                    sb.append(i + 1).append(": ");
                    sb.append(npc.getDialogueList().get(i).getPrompt());
                    sb.append("\n");
                }

                this.broadcast(player, player.getName() + " begins to talk to NPC: " + npcName + ".");
                return sb.toString();
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

	/**
	 * Selects dialogue option with NPC and gets response
	 * @param playerName Player name
	 * @param npcName NPC name
	 * @param dialogueChoice Choice of dialogue option
	 * @return Dialogue options for player
	 */
    public String selectNPCDialogueOption(String name, String npcName, int dialogueChoice) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            NPC npc = room.getNPCs().get(npcName);
            if (npc != null) {
                if (dialogueChoice < npc.getDialogueList().size() && dialogueChoice >= 0) {
                    return npc.getDialogueList().get(dialogueChoice).getResponse(npcName, player.getDialogueIdFromList(npcName, npc.getDialogueList().get(dialogueChoice).getTag(), npc.getDialogueList().get(dialogueChoice).getPrompt()));
                }
                return "No dialogue choice by that number.";
            }
            else {
                return "No npc by that name is in the room.";
            }
        }
        else {
            return null;
        }
    }

    //Feature 411. Shout
    /**
     * Shouts "message" to everyone in the current area.
     * @param name Name of the player to speak
     * @param message Message to speak
     * @return Message showing success.
     */
    @Override
    public String shout(String name, String message) {
        Player player = this.playerList.findPlayer(name);
        if(player != null){
            for(Player otherPlayer : this.playerList) {
                if(otherPlayer != player)
                    otherPlayer.messagePlayer(player, "shouts", message);
            }
                    chatLog(player, 2, message,"Everyone");
            return player.getMessage() + "shout, " + message;
        } else {
            return null;
        }
    }

    private void chatLog(Player player, int chatType, String message, String target) {
        try {
            pw = new PrintWriter(new FileWriter("chatlog.txt", true));

            String type = "";
            String msg;
            switch(chatType) {
                case 0:
                    type = "SAID";
                    break;
                case 1:
                    type = "WHISPERED";
                    break;
                case 2:
                    type = "SHOUTED";
                    break;
            }
            msg = String.format("[%tD %<tT]", GameServer.getDate()) + " PLAYER [" + player.getName() + "] " + type + " (" + message + ") to [" + target + "]\n";
            pw.write(msg);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
     public String teach(String player){
         Player players = this.playerList.findPlayer(player);
         String message;
         if(players.getCurrentRoom() == 1){
            message = "Here is the Heirarchy of power in R-P-S:\n\tRock beats Scissors\n\tScissors beats Paper\n\tPaper beats Rock\n\nCHALLENGE <name>: \tIf you challenge someone, you must wait for them to accept or reject\nACCEPT/REJECT <name>: \tIf you have been challenge, you must accept or reject the challenge\nYou may not be challenged while in a R-P-S battle\n";
         }
         else{
            message = "You are not in the Clock in the Main Quad where the teacher is located\n";
         }
         return message;
     }

    /**
     * Generates list of all online players.
     * @return String of linked list PlayerList
     */
    public String showPlayers(){
      StringBuilder users = new StringBuilder();
      users.append("Players online:\n");
      for(Player a : playerList){
        users.append(a.getName() + "\n");
      }
      return users.toString();
    }

    /**
     * 108 In game ASCII map
     * Returns an ascii representation of nearby rooms
     * @param name Name of the player
     * @return String representation of the map
     */
    public String showMap(String name){
       int roomId = this.playerList.findPlayer(name).getCurrentRoom();
       return map.asciiMap(roomId);
    }

    /**
     * Talk to an NPC in the player's room
     * @param player Name of the player
     * @param npc Name of the npc
     * @return String response from the npc if found
     */
    public String talk(String playerName, String npcName){
       Player player = this.playerList.findPlayer(playerName);
       boolean found = false;
       NPC npc = null;
       for(NPC temp : map.findRoom(player.getCurrentRoom()).getNPCs().values()){
           if(temp.getName().toUpperCase().equals(npcName.toUpperCase())){
              found = true;
	      npc = temp;
              break;
	   }
       }
       if(!found){
           return "NPC " + npcName + " is not located in your current room";
       }
       else{
	   return npc.talk(player);
       }
    }

    /**
     * Checks the implementation of the given npc
     * @param npc Name of the npc
     * @return True if uses team 6 implementation
     */
    public boolean checkNPCValidity(String playerName, String npcName){
       Player player = this.playerList.findPlayer(playerName);
       boolean found = false;
       NPC npc = null;
       for(NPC temp : map.findRoom(player.getCurrentRoom()).getNPCs().values()){
           if(temp.getName().toUpperCase().equals(npcName)){
              found = true;
              npc = temp;
              break;
           }
       }
       if(found)
	  return npc.checkValidDialogue();
       return false;
    }

    /**
     * Returns an the player's current quest
     * @param name Name of the player
     * @return String representation of current quest progress
     */
    public String journal(String name){
	try{
           int progress = this.playerList.findPlayer(name).getProgress();
	   File questDesc = new File("./NPCDialogues/questDescriptions");
	   Scanner sc = new Scanner(questDesc);
	   String ret = "Current Quest Description:\n";
	   String temp = "";
	   for(int i = 0; i <= progress; i ++)
	      temp = sc.nextLine();
	   return ret + temp;
	}catch(FileNotFoundException ex){
	   System.out.println("[RUNTIME] No Quest Description File ./NPCDialogues/questDescriptions");
	   return null;
	}
    }


	/**
	 * Logs player connections
	 * 
	 * @param connecting true if they're connecting, false if they are disconnecting
	 * @param name
	 */
	private void connectionLog(boolean connecting, String name) {
		playerLogger.info(String.format("(%s) logged %s", name, connecting ? "in" : "out"));
		for (Handler h : playerLogger.getHandlers())
			h.flush();
	}

	/**
	 * Creates the logger outside of the constructor. Uses RFC3339 timestamps
	 * 
	 * @throws IOException
	 */
	private void initConnectionsLogger() throws IOException {
		File f = new File("connections.log");
		if (!f.exists())
			f.createNewFile();
		FileOutputStream out = new FileOutputStream(f, true);
		StreamHandler handle = new StreamHandler(out, new SimpleFormatter() {
			private final SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

			@Override
			public String format(LogRecord log) {
				StringBuilder sb = new StringBuilder();
				sb.append("[").append(rfc3339.format(new Date(log.getMillis()))).append("] ");
				sb.append("[").append(log.getLoggerName()).append("] ");
				sb.append("[").append(log.getLevel()).append("] ");
				sb.append(log.getMessage()).append("\r\n");
				Throwable e = log.getThrown();
				if (e != null)
					for (StackTraceElement el : e.getStackTrace())
						sb.append(el.toString()).append("\r\n");
				return sb.toString();
			}
		});
		playerLogger.setUseParentHandlers(false);
		playerLogger.addHandler(handle);
		playerLogger.info("Player connections logger has started");
		handle.flush();
	}

	/**
	 * Delete a player's account.
	 * 
	 * @param name Name of the player to be deleted
	 * @return Player that was just deleted.
	 */
	@Override
	public Player deleteAccount(String name) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			this.broadcast(player, "You hear that " + player.getName() + " has dropped out of school.");
			this.playerList.removePlayer(name);
			this.accountManager.deleteAccount(player.getName());
			synchronized (friendsLock) {
				this.friendsManager.purge(name);
			}
			return player;
		}
		return null; // No such player was found.
	}

	/**
	 * Adds a player to the friend list if the player exists and isn't on the friend
	 * list already
	 * 
	 * @param name
	 * @param friend
	 * @return responseType
	 */
	@Override
	public Responses addFriend(String name, String friend) {
		synchronized (friendsLock) {
			if (!this.accountManager.accountExists(name))
				return Responses.INTERNAL_SERVER_ERROR;
			if (!this.accountManager.accountExists(friend))
				return Responses.NOT_FOUND;
			return this.friendsManager.addFriend(name, friend);
		}
	}

	/**
	 * Removes a player from the friend list
	 * 
	 * @param name
	 * @param ex
	 * @return reponseType
	 */
	@Override
	public Responses removeFriend(String name, String ex) {
		synchronized (friendsLock) {
			if (!this.accountManager.accountExists(name))
				return Responses.INTERNAL_SERVER_ERROR;
			return this.friendsManager.removeFriend(name, ex);
		}
	}
	
	/**
	 * Returns a message showing all online friends
	 * 
     * @param name name of player requesting list of friends
     * @param onlineOnly true if you only want a list of online friends, else false.
	 * @return Message showing online friends
	 */
	@Override
	public String viewFriends(String name, boolean onlineOnly) {
                StringBuilder message = new StringBuilder();
		

		// get list of friends from FriendsManager
                HashSet<String> fullList = this.friendsManager.getMyAdded().get(name.toLowerCase());
                
                if (fullList == null) 
			return "You don't have any friends....\n";
                
		HashSet<String> flist = new HashSet<>();
                flist.addAll(fullList);
		
		// find online friends using flit and findPlayer from playerList
                HashSet<String> online = new HashSet<>();
                flist.forEach((str) -> {
                    Player p;
                    if ((p = this.playerList.findPlayer(str)) != null) {
                        online.add(str);
                    }
                });
                
                if(!online.isEmpty()){
                    message.append("Online friends:\n");
                    online.forEach(str -> message.append("  ").append(str).append("\n"));
                }
                
                //list all offline friends, if needed
                if(!onlineOnly){
                    flist.removeAll(online);
                    if(!flist.isEmpty()){
                        message.append("Offline friends:\n");
                        flist.forEach(str -> message.append("  ").append(str).append("\n"));
                    }
                }
                
                if(onlineOnly && online.isEmpty())
                    message.append("You have no online friends.");
                
		return message.toString();
	}

    @Override
    public void heartbeatCheck(String name){
        playerList.heartbeat(name);
    }
	
	/**
	 * Gets recovery question
	 * @param name User of recovery question 
	 * @param num Marks which question will be grabbed
	 * @return String of recovery question, null if user doesn't exist
	 */
	public String getQuestion(String name, int num) {
        Player player = this.playerList.findPlayer(name);
        if (player==null) {
        	PlayerAccountManager.AccountResponse resp = this.accountManager.getPlayer(name);
        	if(!resp.success())
        		return null;
        	player=resp.player;
        }
        if (player != null) {
			return player.getQuestion(num);
		} else {
			return null;
		}
	}
	
	public void addQuestion(String name, String question, String answer) {
		//PlayerAccountManager.AccountResponse resp = this.accountManager.getPlayer(name);
	
		//if(!resp.success())
			//return;
		Player player = this.playerList.findPlayer(name);
		if(player != null) {
			player.addQuestion(question, hash(answer));
		}
		
	}
	
	/**
	 * Gets recovery answer
	 * @param name User of recovery answer
	 * @param num Marks which answer will be grabbed
	 * @return String of recovery question, null if user doesn't exist
	 */
	public Boolean getAnswer(String name, int num, String answer) {
        Player player = this.playerList.findPlayer(name);
        if (player==null) {
        	PlayerAccountManager.AccountResponse resp = this.accountManager.getPlayer(name);
        	if(!resp.success())
        		return null;
        	player=resp.player;
        }
		if(player != null) {
			return player.getAnswer(num).equals(hash(answer));
		} else {
			return null;
		}
	}

	public Responses verifyPassword(String name, String password) {
		password = hash(password);
		PlayerAccountManager.AccountResponse resp = this.accountManager.getAccount(name, password);
		if (resp.success())
			return Responses.SUCCESS;
		return resp.error;
	}

	/**
	 * Resets passwords.
	 * 
	 * @param name Name of player getting password reset
	 * @param password New password to be saved
	 */
	public Responses resetPassword(String name, String password) {
		password = hash(password);
		PlayerAccountManager.AccountResponse resp = this.accountManager.getPlayer(name);
		if(!resp.success()) {
			return resp.error;
		}
		return accountManager.resetPassword(resp.player, password);
		
	}
	
	public void removeQuestion(String name, int num) {
		Player player = this.playerList.findPlayer(name);
		if(player != null) {
			player.removeQuestion(num);
		}
	}

	/**
	 * Toggles the RPS resolution of other players in same room
	 * @param name of Player that wants to toggle
	 */
	@Override
	public String toggleRPSChat(String player){
		Player playerToggle = this.playerList.findPlayer(player);
		String message = playerToggle.toggleResolution();
		return message;
		
	}

}
