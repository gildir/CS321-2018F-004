
/*import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
*/
import java.io.*;
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
import java.io.FileInputStream;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
/**
 *
 * @author Kevin
 */
public class GameCore implements GameCoreInterface {
    private final PlayerList playerList;
    private final Map map;
    protected DailyLogger dailyLogger;
    private HashMap<Integer,Shop> shoplist;
    private boolean isDay = true;
    private PrintWriter pw;
    private Bank bank;
    private ArrayList<Chatroom> chatrooms = new ArrayList<Chatroom>();
    private final Logger rpsLogger = Logger.getLogger("battles");
    private FileHandler rpsHandler;
    private boolean pickRPSToggle = false;
	private ArrayList<String> spirits = new ArrayList<>();

	// Accounts and Login
	private final PlayerAccountManager accountManager;
	private final Object loginLock = new Object();
	private final Object createAccountLock = new Object();
	private Logger playerLogger = Logger.getLogger("connections");
	private FriendsManager friendsManager;
	private final Object friendsLock = new Object();
	private ArrayList<Thread> allThreads = new ArrayList<>();
    
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

        map = new Map(worldFile);	//If  a map file is passed on the command line use that otherwise the default


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
		
		// sets up Venmo with the correct playerList and account manager
		Venmo.setup(playerList);
        		
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

                        try {
							room.addObject(object);
							GameCore.this.broadcast(room, "You see a student rush past and drop a " + object + " on the ground.");
						}
						catch (IndexOutOfBoundsException e){
							GameCore.this.broadcast(room, "You see a student rush past.");
						}

                      // were these added for testing/demoing?
                      //  room.addObject(object);
                      //  room.addObject(object);
                      //  room.addObject(object);
                      //  room.addObject(object);
                      //  room.addObject(object);


                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        objectThread.setDaemon(true);
        objectThread.setName("objectThread");

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
                 
                 //task 228, daily allowance checker thread
                 Thread allowanceThread = new Thread(new Runnable() {
                     @Override
                     public void run() {
                             while(true) {
                                 try {
                                	 Thread.sleep(600000); //checks to update all player allowance every 10 minutes
                                     //Thread.sleep(5000); //checks every 5 seconds (for testing/demo uncomment this and comment line above)
                                     
                                     long daysPlayed = 0; //will be used to calculate each players allowance
                                     //calculate each players given allowance
                                     for (Player player : playerList) {
                                    	 DataResponse<PlayerAccount> resp = GameCore.this.accountManager.getAccount(player.getName());
                                    	 if(!resp.success()) {
                                    		 System.err.println("Error getting account for allowence: "+player.getName());
                                    		 continue;
                                    	 }
                                    	 daysPlayed = ((System.currentTimeMillis() - resp.data.getAccountAge("").data)/86400000); //helps calculate how many days worth of allowance to give
                                    	 //daysPlayed = ((System.currentTimeMillis() - player.getAccountAge())/30000); //testing/demo alternative to the line above (day shortened to 30 seconds)
                                    	 if(daysPlayed!=player.getTotalPay()) //determines if player needs payment
                                    	 {
                                    		 player.getReplyWriter().println("Collecting your owed allowance of $" + String.format("%.2f", ((daysPlayed-player.getTotalPay())*10.0))); //prints how much player is getting
                                    		 player.changeMoney((daysPlayed-player.getTotalPay())*10.0); //calculates allowance owed to player
                                    		 player.setTotalPay(daysPlayed); //update TotalPay
                                    	 }
                                    	}
                                     
                                 } catch (InterruptedException ex) {
                                 }
                             }
                         }
                     });
                 
                 allowanceThread.setDaemon(true);
                 allowanceThread.setName("allowance");
        
               //Thread that rewards money to all players every 10 minutes (600,000 ms)
                 //Team 3, task 229
                 Thread rewardThread = new Thread(new Runnable() {
                     @Override
                     public void run() {
                    	 
                    	 final long rewardTime = 600000; //time that must elapse before rewarding players
                    	 //final long rewardTime = 10000; //10 seconds for testing and demo (comment the above line)
                    	 final long checkInterval = 2000; // Every 2 seconds, thread checks all players for if they meet requirements for payment at rewardTime. Increase this value for better performance
                    	 final double rewardIncrement = .01; //How much reward amount increments each time they are given
                             while(true) {
                                 try {
                                     Thread.sleep(checkInterval); //how often thread checks
                                     //check if server needs to add money to each player's account
                                     for (Player player : playerList) {
                                    	 if (player.getRewardProgress() >= rewardTime) {
                                    		 player.changeMoney(player.getRewardAmount());
                                    		 
                                    		 //Let the player know their wallet has increased (mainly for demo and testing purposes.)
                                    		 player.getReplyWriter().println("Your wallet grew by $" + String.format("%.2f", player.getRewardAmount()) + "!"); //message to player
                                    		 
                                    		 player.setRewardAmount(player.getRewardAmount() + rewardIncrement); //amount player is rewarded increments by rewardIncrement
                                    		 player.setRewardProgress(0); //reward given, reset progress.
                                    	 	}
                                    	 
                                    	 player.setRewardProgress(player.getRewardProgress() + checkInterval); //increase rewardProgress
                                    	}
                                     
                                 } catch (InterruptedException ex) {
                                 }
                             }
                         }
                     });
                 
                 rewardThread.setDaemon(true);
                 rewardThread.setName("reward");
                 rewardThread.start();
                 
                 
                // new thread awake and control the action of Ghoul.
                // this ghoul is always on the map, whether it is day or night
                Thread awakeDayGhoul = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random rand = new Random();
                        Room room = map.randomRoom();
                        Ghoul ghoul = new Ghoul(room.getId());
						room.addGhoul(ghoul);
						String ghoulName = "["+ghoul.getTrueName()+"]";
						GameCore.this.broadcast(room, "You see a ghoul named " + ghoulName + " appear in this room.");

                        while (true) {
                            try {
                                // Ghoul move in each ? seconds.
                                Thread.sleep(30000 + rand.nextInt(10000));

                                // make Ghoul walk to other room;
                                GameCore.this.ghoulWander(ghoul, room);
                                GameCore.this.broadcast(room, "You see a Ghoul named " + ghoulName + "leave this room.");
                                room = map.findRoom(ghoul.getRoom());
                                GameCore.this.broadcast(room, "You see a Ghoul named " + ghoulName + " enter this room.");
                            } catch (InterruptedException ex) {
                                Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                });
                awakeDayGhoul.setDaemon(true);
                awakeDayGhoul.setName("awakeDayGhoul");
				
				// new thread awake and control the action of nightGhoul
				// nightGhoul just activity in night. It wil affected by the day-night cycle
				Thread awakeNightGhoul = new Thread(new Runnable() {
					@Override
					public void run() {
						Random rand = new Random();
						Room room = map.randomRoom();
						Ghoul ghoul = new Ghoul(0);
						String ghoulName = "["+ghoul.getTrueName()+"]";
						while(true) {
							try {
								while(isDay) {
									//check if ghoul stay in some room; if so, expel it.
									if(ghoul.getRoom() != 0) {
										room.removeGhoul(ghoul);
										ghoul.setRoom(0);
										GameCore.this.broadcast(room, "You see a night Ghoul named " + ghoulName + " disappear under sunshine.");
									}
									Thread.sleep(10); //block the thread, because it is in the daytime
								}
								
								//check if ghoul already have their room; if not, give some room to it
								if(ghoul.getRoom() == 0) {
									room.addGhoul(ghoul);
									GameCore.this.broadcast(room, "You see a night Ghoul named " + ghoulName + " appear in this room.");
								}
								
								Thread.sleep(15000 + rand.nextInt(8000)); //move faster than dayGhoul
								// make ghoul walk to other room
								GameCore.this.ghoulWander(ghoul, room);
								GameCore.this.broadcast(room, "You see a night Ghoul named " + ghoulName + " leave this room.");
								room = map.findRoom(ghoul.getRoom());
								GameCore.this.broadcast(room, "You see a night Ghoul named " + ghoulName + " enter this room.");
							} catch(InterruptedException ex) {
								Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					}
				});
				awakeNightGhoul.setDaemon(true);
				awakeNightGhoul.setName("awakeNightGhoul");
				
				
				// make day/night cycle
				Thread dayNightCycle = new Thread(new Runnable() {
					boolean night = false;
					@Override
					public void run() {
						while(true) {
							try {
								isDay = true;
								GameCore.this.broadcast("Morning!");
								Thread.sleep(180000);
								isDay = false;
								GameCore.this.broadcast("Night!");
								Thread.sleep(180000);
							} catch(InterruptedException ex) {
								Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					}
				});
				dayNightCycle.setDaemon(true);
				dayNightCycle.setName("dayNightCycle");
				
				// Thread that controls the random appearance of spirits in the map
				Thread spiritThread = new Thread(new Runnable() {
					@Override
					public void run() {
						Random rand = new Random();
						Room room = map.randomRoom();
						String spirit = null;
						
						//Add all spirit types here
						spirits.add("happy");
						spirits.add("sad");
						spirits.add("scary");
						spirits.add("spooky");
						spirits.add("sleepy");
						spirits.add("angry");
						spirits.add("annoying");
						spirits.add("calm");
						spirits.add("cheerful");
						spirits.add("silly");
						spirits.add("energetic");
						spirits.add("excited");
						spirits.add("bored");
						spirits.add("mysterious");
						spirits.add("curious");
						spirits.add("dizzy");
						spirits.add("hungry");
						spirits.add("lazy");
						spirits.add("relaxed");
						spirits.add("lonely");
						
						while(true) {
							try {
								//A random spirit will appear in a random room every 20-30 seconds
								Thread.sleep(5000);
								
								
								//remove previously spawned spirit
								if(room.hasSpirit()) {
									GameCore.this.broadcast(room, "The " + spirit + " spirit has disappeared.");
									room.removeSpirit();
								}
								
								//add new spirit to random room
								spirit = spirits.get(rand.nextInt(spirits.size()));
								room = map.randomRoom();
								room.addSpirit(spirit);
								GameCore.this.broadcast(room, "A " + spirit + " spirit has appeared in the room.");
						
							} catch (InterruptedException ex) {
								Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					}
				});
				spiritThread.setDaemon(true);
				spiritThread.setName("spiritThread");

                allThreads.add(hbThread);
                allThreads.add(objectThread);
                allThreads.add(awakeDayGhoul);
				allThreads.add(awakeNightGhoul);
				allThreads.add(dayNightCycle);
				allThreads.add(spiritThread);
                allThreads.add(allowanceThread);
                
                hbThread.start();
                objectThread.start();
                awakeDayGhoul.start();
				awakeNightGhoul.start();
				dayNightCycle.start();
				spiritThread.start();
                allowanceThread.start();
            }
    
    protected void shutdown() {
    	for(Player p : playerList)
    		p.getReplyWriter().println("!SHUTDOWN");
    	for(Thread t : allThreads)
    		t.interrupt();
    	friendsManager.shutdown();
    	accountManager.shutdown();
    }

	/**
	* let ghoul Wander to nearby room; help method, ignore it.
	* @param g the ghoul that need to wander
	* @param room The room that exist ghoul at first (before ghoul start to wander). 
	*/
	public void ghoulWander(Ghoul g, Room room) {
		Random rand = new Random();
		LinkedList<Room> candidateRoom = room.getNearByRoom(map);
		int size = candidateRoom.size();
		
		room.removeGhoul(g);
		//random walk
		room = candidateRoom.get(rand.nextInt(size));
		
		g.setRoom(room.getId());
		room.addGhoul(g);
    }
	
	/**
	 * The player plays RPS against ghoul to avoid being dragged
	 * @param playerName the player playing RPS
	 * @param option the RPS choice of the player
	 */
	public String ghoulRPS(String playername, String option) {
		String message = "";
		Player player = this.playerList.findPlayer(playername);
		if ((player.getInBattle()) || player.getChallengedGhoul() == null) {
			return "Not in a battle with a ghoul";
		}
		player.setInBattle(true);
		Ghoul ghoul = player.getChallengedGhoul();
		String[] rpschoices = new String[3];
		rpschoices[0] = "ROCK";
		rpschoices[1] = "PAPER";
		rpschoices[2] = "SCISSORS";
		Random rand = new Random();
		int i = rand.nextInt(3);
			switch (option.toUpperCase()) {
			case "ROCK":
				if (rpschoices[i].equals("PAPER")) {
					message = "Ghoul" + " wins with " + rpschoices[i];
					ghoul.modifyAngryLevel(-1);
					draggedToSpawn(player);
					player.setInBattle(false);
					player.setChallengedGhoul(null);
					ghoul.setChallenger(null);
					ghoul.setEngaged(false);
				} else if (rpschoices[i].equals("ROCK")) {
					message = "It is a tie.";
					ghoul.modifyAngryLevel(-1);
					draggedToSpawn(player);
					player.setInBattle(false);
					player.setChallengedGhoul(null);
					ghoul.setChallenger(null);
					ghoul.setEngaged(false);
				} else {
					message = player.getName() + " wins with rock!";
					player.setInBattle(false);
					player.setChallengedGhoul(null);
					ghoul.setChallenger(null);
					ghoul.setEngaged(false);
				}
				return message;
			case "PAPER":
				if (rpschoices[i].equals("SCISSORS")) {
					message = "Ghoul" + " wins with " + rpschoices[i];
					ghoul.modifyAngryLevel(-1);
					draggedToSpawn(player);
					player.setInBattle(false);
					player.setChallengedGhoul(null);
					ghoul.setChallenger(null);
					ghoul.setEngaged(false);
				} else if (rpschoices[i].equals("PAPER")) {
					message = "It is a tie.";
					ghoul.modifyAngryLevel(-1);
					draggedToSpawn(player);
					player.setInBattle(false);
					player.setChallengedGhoul(null);
					ghoul.setChallenger(null);
					ghoul.setEngaged(false);
				} else {
					message = player.getName() + " wins with paper!";
					player.setInBattle(false);
					player.setChallengedGhoul(null);
					ghoul.setChallenger(null);
					ghoul.setEngaged(false);
				}
				return message;
			case "SCISSORS":
				if (rpschoices[i].equals("ROCK")) {
					message = "Ghoul" + " wins with " + rpschoices[i];
					ghoul.modifyAngryLevel(-1);
					draggedToSpawn(player);
					player.setInBattle(false);
					player.setChallengedGhoul(null);
					ghoul.setChallenger(null);
					ghoul.setEngaged(false);
				} else if (rpschoices[i].equals("SCISSORS")) {
					ghoul.modifyAngryLevel(-1);
					draggedToSpawn(player);
					player.setInBattle(false);
					player.setChallengedGhoul(null);
					ghoul.setChallenger(null);
					ghoul.setEngaged(false);
					message = "It is a tie";
				} else {
					message = player.getName() + " wins with scissors!";
					player.setInBattle(false);
					player.setChallengedGhoul(null);
					ghoul.setChallenger(null);
					ghoul.setEngaged(false);
				}
				return message;
			default:
				break;
			}
		return "Not in any battles with a ghoul";
 		// player.setChallengedGhoul(challengedGhoul);
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
            shoplist.get(room.getId()).addPlayer(player);
    		return room.getId();
    	}
    	return -1;
    }

    /**
     * updates the playlist in the Shop
     * @param name Name of the player
     * @return void
     */
    public void shopLeft(String name)
    {
        Player player = this.playerList.findPlayer(name);
        Room room = map.findRoom(player.getCurrentRoom());
        shoplist.get(room.getId()).removePlayer(player);
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
            //check to see if the item is in demand

            for (Item x : s.getDemand()){
                if (x.getName().compareToIgnoreCase(removed.getName()) == 0){
                    //remove and replace the in demand item
                    s.removeDemand(x);
                    s.addDemandRand();

                    value = removed.getPrice()*2; //player gets double item's price
                    player.changeMoney(value);
                    s.add(removed); //add sold item to shop's inv
                    return value;
                }
            }
            value = removed.getPrice();
            
            s.add(removed); //add sold item to shop's inv
            
            player.changeMoney(value);            
        }
        return value;
    }
    
	/**
	 * Bribe the ghoul in the current room
	 * @param playerName Player name
	 * @param item item's name, which will be throw. 
	 * @return String message of ghoul
	 */
	public String bribeGhoul(String playerName, String item){
		item = item.toLowerCase();
		Player player = playerList.findPlayer(playerName);
		Room room = this.map.findRoom(player.getCurrentRoom());
		Item object = player.removeObjectFromInventory(item);
		if(player == null){
			return null;
		}
		if(room.hasGhoul()){
			LinkedList<Ghoul> ghouls = room.getGhouls();
 			if (ghouls.isEmpty()) {
				return "There is no ghoul in this room.";
			}
 			Random rand = new Random();
 			Ghoul ghoul = ghouls.get(rand.nextInt(ghouls.size()));
			
			if (ghoul.isEngaged()) {
				return "Ghoul is currently engaged and not paying attention to you";
			}
			
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
				String message = "Ghoul [" + ghoul.getTrueName() + "] gets " + item + ", " + "and its anger level decreases to " + angryLv + ".";
				return  message;
			}else{
				return "Do not have this item......";
			}
		}else{
			return "No Ghoul in this room";
		}

	}

	/**
	 * Pokes the ghoul in the current room
	 * @param playerName Player name
	 * @return String message of ghoul
	 */
	public String pokeGhoul(String playerName) {
		Player player = playerList.findPlayer(playerName);
		Room room = this.map.findRoom(player.getCurrentRoom());

		if (player != null) {
			if (!room.hasGhoul()) {
				return "There is no ghoul in this room.";
			}

			try {
				GhoulLog myLog = new GhoulLog();
				myLog.glLog("GameCore","pokeGhoul", "Player" + " " + playerName + " has just poked the Ghoul");
				player.addPoke();
			} catch (Exception e){
				e.printStackTrace();
			}

			//random select ghoul in the room; 
 			LinkedList<Ghoul> ghouls = room.getGhouls();
 			if (ghouls.isEmpty()) {
				return "There is no ghoul in this room.";
			}
 			Random rand = new Random();
 			Ghoul ghoul = ghouls.get(rand.nextInt(ghouls.size()));

			if (ghoul.isEngaged()) {
				return "Ghoul is currently engaged and not paying attention to you";
			}
			
			ghoul.modifyAngryLevel(1);
			int angerLvl = ghoul.getAngryLevel();
			if (angerLvl >= 7) {
				ghoul.setInputTimer(LocalDateTime.now());
				ghoul.setChallenger(player);
				ghoul.setEngaged(true);
				player.setChallengedGhoul(ghoul);
				player.getReplyWriter().println("The ghoul is planning to drag you! You have 5 seconds to FIGHT it w/ R-P-S!");
				Runnable timeElapsed = new Runnable() {
					@Override
					public void run() {
						boolean onTime = false;
						while(ChronoUnit.SECONDS.between(ghoul.getInputTimer(), LocalDateTime.now()) <= 10) {
							if(player.getInBattle()) {
								onTime = true;
								break;
							}
						}
						if(!onTime) {
							player.getReplyWriter().println("Failed to accept the fight...");
							ghoul.modifyAngryLevel(-1);
							draggedToSpawn(player);
							player.setInBattle(false);
							player.setChallengedGhoul(null);
							ghoul.setChallenger(null);
							ghoul.setEngaged(false);
						}
					}
				};
				Thread t = new Thread(timeElapsed);
				t.start();
			}
			return ("Ghoul [" + ghoul.getTrueName() + "] anger level has increased to " + angerLvl);
		} else {
			return null;
		}
	}
	
	/**
	 * Captures the spirit in the current room
	 * @param playerName Player name
	 * @return String message of spirit capture success or failure
	 */
	public String capture(String playerName) {
		Player player = playerList.findPlayer(playerName);
		Room room = this.map.findRoom(player.getCurrentRoom());
		
		if(player != null) {
			
			if(!room.hasSpirit()) {
				return "There is no spirit in this room.";
			}
			String curSpirit = room.getSpirit();
			room.removeSpirit();
			
			
			try {
				int numPlayersFinished = 0;
				boolean playerFound = false;
				boolean playerCaughtSpirit = false;
				String fileContents = "";
				String newFileContents = "";
				
				File spiritFile = new File("capturedSpirits.txt");
				if(spiritFile.createNewFile()) { // file does not exist yet
					fileContents = "0:"+playerName+","+curSpirit;
					// Write contents to file
					BufferedWriter bw = new BufferedWriter(new FileWriter("capturedSpirits.txt"));
					bw.write(fileContents);
					bw.close();
					return "You have captured a " + curSpirit + " spirit.";
				}
				
				Scanner spScan = new Scanner(spiritFile);
				if(spScan.hasNextLine()) {
					fileContents = spScan.nextLine();
					String[] playerCaptures = fileContents.split(":");
					
					// Store count of players who have captured all spirits
					if(playerCaptures.length > 0) {
						try {
							numPlayersFinished = Integer.parseInt(playerCaptures[0]);
						} catch(NumberFormatException ex) {
							ex.printStackTrace();
						}
					}
					
					// Search for current player in file contents
					for(int i=1; i<playerCaptures.length; i++) {
						newFileContents += playerCaptures[i];
						String[] curSplit = playerCaptures[i].split(",");
						if(curSplit.length > 0 && curSplit[0].equals(playerName)) {
							playerFound = true;
							for(int j=1; j<curSplit.length; j++) {
								if(curSplit[j].equals(curSpirit)) {
									playerCaughtSpirit = true;
								}
							}
							if(!playerCaughtSpirit) {
								newFileContents += ","+curSpirit;
								if(curSplit.length == spirits.size()) { // player has just captured all spirits
									double reward = 100 - (numPlayersFinished*10);
									if(reward < 20) reward = 20;
									player.changeMoney(reward);
									numPlayersFinished++;
								}
							}
						}
						newFileContents += ":";
					}
					
					// Player isn't in file
					if(!playerFound) {
						newFileContents += playerName+","+curSpirit+":";
					}
					
					newFileContents = numPlayersFinished+":"+newFileContents.substring(0,newFileContents.length()-1);
					// Write modified contents to file
					BufferedWriter bw = new BufferedWriter(new FileWriter("capturedSpirits.txt"));
					bw.write(newFileContents);
					bw.close();
				}
			} catch(IOException ex) {
				ex.printStackTrace();
			}
			return "You have captured a " + curSpirit + " spirit.";
		} else {
			return null;
		}
	}
	
	/**
	 * Lists all spirits the player has captured.
	 * @param playerName Player name
	 * @return String list of spirits the player has captured
	 * @throws RemoteException
	 */
	public String spiritListAll(String playerName){
		Player player = playerList.findPlayer(playerName);
		
		if(player == null) {
			return null;
		}
		
		String allSpirits = "Spirits captured: ";
		try {
			File spiritFile = new File("capturedSpirits.txt");
			Scanner spScan = new Scanner(spiritFile);
			
			//File doesn't exist or has no contents
			if(!spScan.hasNextLine()) {
				return "You haven't captured any spirits";
			}
			
			//File does exist and has contents
			String contents = spScan.nextLine();
			String[] playerCaptures = contents.split(":");
			for(int i=1; i<playerCaptures.length; i++) {
				String[] curSplit = playerCaptures[i].split(",");
				// Found desired player
				if(curSplit[0].equals(playerName)) {
					for(int j=1; j<curSplit.length; j++) {
						allSpirits += curSplit[j] + ", ";
					}
					return allSpirits.substring(0,allSpirits.length()-2);
				}
			}
			
			// Desired player not in file
			return "You haven't captured any spirits";
			
		} catch(IOException ex) {
			//ex.printStackTrace();
			return "You haven't captured any spirits";
		}
	}
	
	/**
	 * Lists all spirits the player has not captured.
	 * @param playerName Player name
	 * @return String list of spirits the player has not captured
	 * @throws RemoteException
	 */
	public String spiritListMissing(String playerName){
		Player player = playerList.findPlayer(playerName);
		
		if(player == null) {
			return null;
		}
		
		String allSpirits = "Spirits needed: ";
		try {
			File spiritFile = new File("capturedSpirits.txt");
			Scanner spScan = new Scanner(spiritFile);
			
			//File doesn't exist or has no contents
			if(!spScan.hasNextLine()) {
				for(String sp : this.spirits) {
					allSpirits += sp + ", ";
				}
				return allSpirits.substring(0, allSpirits.length()-2);
			}
			
			//File does exist and has contents
			String contents = spScan.nextLine();
			String[] playerCaptures = contents.split(":");
			for(int i=1; i<playerCaptures.length; i++) {
				String[] curSplit = playerCaptures[i].split(",");
				// Found desired player
				if(curSplit[0].equals(playerName)) {
					for(int j=0; j<spirits.size(); j++) {
						boolean found = false;
						for(int k=1; k<curSplit.length; k++) {
							if(spirits.get(j).equals(curSplit[k])) {
								found = true;
							}
						}
						if(!found) {
							allSpirits += spirits.get(j) + ", ";
						}
					}
					if(allSpirits.length() == 16) {
						return "All spirits have been captured.";
					} else {
						return allSpirits.substring(0,allSpirits.length()-2);
					}
				}
			}
			
			// Desired player not in file
			for(String sp : this.spirits) {
				allSpirits += sp + ", ";
			}
			return allSpirits.substring(0, allSpirits.length()-2);
			
		} catch(IOException ex) {
			//ex.printStackTrace();
			for(String sp : this.spirits) {
				allSpirits += sp + ", ";
			}
			return allSpirits.substring(0, allSpirits.length()-2);
		}
	}
		
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
    			s.ping(player, item);
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
		String player2s;
		double amount;
		// Executes the relevant commands
		switch(tokens.remove(0).toUpperCase()) {
			case "SEND": // sending a transaction
				if (tokens.isEmpty()) return "Specify recipient and amount. Type \"venmo help\" to learn more.";
				player2s = tokens.remove(0);
				// checks if player is sending to themselves
                if (name.equals(player2s)) return "You can't Venmo yourself";
				// gets the object of the receiving player
				Player player2 = this.playerList.findPlayer(player2s);
				// checks that the name is correct and that the player is online
                if (player2 == null) {
                    if (accountManager.accountExists(player2s)) return "The player is offline. You can mail them the money instead.\nType \"venmo help\" to learn more."; 
                    else return "Incorrect player name. Type \"venmo help\" to learn more."; 
                }
                // checks if user entered a transaction amount
				if (tokens.isEmpty()) return "Specify transaction amount. Type \"venmo help\" to learn more.";
				
				// checks if the player entered a valid number
				try {
					amount = Float.parseFloat(tokens.remove(0));
				} catch (NumberFormatException e) {
					return "Please enter a valid number. Type \"venmo help\" to learn more.";
				}
				return Venmo.send(player1, player2, amount);
			case "MAIL": // offering a transaction
                if (tokens.isEmpty()) return "Specify recipient and amount. Type \"venmo help\" to learn more.";
                // gets the object of the receiving player
                player2s = tokens.remove(0);
                // checks if player is sending to themselves:
                if (name.equals(player2s)) return "You can't Venmo yourself";
                // checks that the name is correct
                if (!accountManager.accountExists(player2s)) return "Incorrect player name. Type \"venmo help\" to learn more."; 
                // checks if user entered a transaction amount
                if (tokens.isEmpty()) return "Specify transaction amount. Type \"venmo help\" to learn more.";
                
                // checks if the player entered a valid number
                try {
                    amount = Float.parseFloat(tokens.remove(0));
                } catch (NumberFormatException e) {
                    return "Please enter a valid number. Type \"venmo help\" to learn more.";
                }
                return Venmo.mail(player1, player2s, amount);
			case "ACCEPT": // accepting a transaction
                if (tokens.isEmpty()) return "Enter the transaction ID. Type \"venmo help\" to learn more.";
                return Venmo.accept(player1, tokens.remove(0));
			case "REJECT": // rejecting a transaction
                if (tokens.isEmpty()) return "Enter the transaction ID. Type \"venmo help\" to learn more.";
                return Venmo.reject(player1, tokens.remove(0));
			case "MAILBOX": // listing pending transactions
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
		return s.getObjects(0);
    }

    /**
     * Returns a Shop's "In Demand" inventory as a formatted string
     * @param id The shop ID
     * @return A formatted string representing the Shop's "In Demand" inventory
     */
    public String getShopDemInv(int id) {
        Shop s = this.shoplist.get(new Integer(id));
        return s.getObjects(1);
    }

    /**
     * Picks up multiple items of the name type
     * @param name name of the the player
     * @param target name of the item
     * @param amount amount of items to pickup
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
	 * Broadcasts a message to all room. 
	 * @param message Message to broadcast.
	 */
	public void broadcast(String message) {
		for (Player player : this.playerList) {
			dailyLogger.write(message);
			String newMessage = player.filterMessage(message);
			player.getReplyWriter().println(newMessage);
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
                // Check to see if the player of that name is already in game.
                if (isPlayerOnline(name))
                        return null;
                        
		synchronized (loginLock) {
			// Check to see if the player of that name is already in game.
			Player player = this.playerList.findPlayer(name);
			if (player != null)
				return null;
			DataResponse<Player> resp = accountManager.getPlayer(name, password);
			if (!resp.success())
				return null;
			player = resp.data;
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
			DataResponse<Player> resp = accountManager.createNewAccount(name, password);
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
			String roomInfo = "";
			if (room.hasGhoul()) {
				roomInfo = "\n\nGhoul ";
				for(Ghoul g : room.getGhouls()) {
					roomInfo += "["+g.getTrueName()+"], ";
				}
				roomInfo += " in the room!\n";
			}
			if(isDay) {
				roomInfo += "\nIt is day time!\n";
			} else {
				roomInfo += "\nIt is night time!\n";
			}
			
			return room.toString(this.playerList, player) + roomInfo;
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
		    String tempMessage = message;
		    if(player.getRathskellerStatus()) {
			tempMessage = translateRathskeller(message);
		    }
		    for (Player otherPlayer : this.playerList)
		        if (otherPlayer != player && otherPlayer.getCurrentRoom() == player.getCurrentRoom())
		            otherPlayer.messagePlayer(player, "says", tempMessage);
            	chatLog(player, 0, tempMessage, "Room " + player.getCurrentRoom());
            	return player.getMessage() + "say, " + tempMessage;

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
		//checks if the item is a rathskeller bottle, special action if so
		if(usedItem.getName().equals("Rathskeller_Bottle")) {
			player.drinkRathskeller();
			this.broadcast(player, player.getName() + " used " + usedItem.getName());
			return "You have used the item, and it's time to get funky.";
		}
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

    //helper method for parsing messages when a player uses a rathskeller bottle
    /**
     * Used for the Rathskeller Bottle Feature; should never be accessed outside this class
     * Takes the player message, makes it all lower case, and randomly uppercases words
     * Also replaces all punctuation with '!' or '?'
     * @param message the message typed by the player
     * @return the translated string
     */
    private String translateRathskeller(String message) {
	String newMessage = message;
	newMessage  = newMessage.toLowerCase();
	int length = newMessage.length() - 1;
	Random rand = new Random(System.nanoTime());
	String[] parsedMessage;
	String delim = " ";
	parsedMessage = newMessage.split(delim);
	int numUpper = rand.nextInt(parsedMessage.length);

	for(int x = 0; x < parsedMessage.length; x++) {
		char[] tempStr = parsedMessage[x].toCharArray();
		for(int y = 0; y < tempStr.length; y++) {
			char temp = tempStr[y];
			if(temp == ',' || temp == '.' || temp == '!' || temp == '?' || temp == ':' || temp == ';')
			{
				int flag = rand.nextInt() % 2;
				if(flag == 1)
				{
					tempStr[y] = '!';
				}
				else
				{
					tempStr[y] = '?';
				}
			}			
		}
		parsedMessage[x] = new String(tempStr);
	}
	
	for(int x = 0; x < numUpper; x++)
	{
		int index = rand.nextInt(parsedMessage.length - 1);
		String temp = (parsedMessage[index]).toUpperCase();
		parsedMessage[index] = temp;
	}
	
	newMessage = "";
	for(int x = 0; x < parsedMessage.length; x++) {
		if(x == parsedMessage.length - 1) {
			newMessage = newMessage + parsedMessage[x];
		}
		else {
			newMessage = newMessage + parsedMessage[x] + " ";
		}
	}

	return newMessage;
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

    /**
     * Challenge another player to rps
     * @param playerChallenger Name of the player who is initiating the challenge
     * @param playerChallenged Name of the player who is being challenged
     * @return String of feedback from player's attempt to challenge
     */ 
    @Override
    public String challenge(String playerChallenger, String playerChallenged, String sRounds){
      Player player = this.playerList.findPlayer(playerChallenger);
      Player opponent = this.playerList.findPlayer(playerChallenged);
      int rounds = 0;
      if(opponent == null || player == null){
        return "This player does not exist in the game or is not online.";
      }
      if(opponent.getHasChallenge()){
        opponent.getReplyWriter().println("You have a challenge that has not been responded to yet.");
        return "This Player already has a challenge that needs to be responded to.";
      }
      if(player.getInBattle()){
        return "You are already in a R-P-S battle.";
      }
      if(opponent.getInBattle()){
        return opponent.getName() + " is already in a R-P-S battle.";
      }
      if(player != opponent && player.getCurrentRoom() == opponent.getCurrentRoom()) {
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
        }
        if(rounds != 1 && rounds != 3 && rounds != 5){
            return "This is an invalid number of rounds, please choose from 1, 3, or 5 rounds: ";
        }

        opponent.setChallenger(playerChallenger);
        player.setChallenger(playerChallenged);
        opponent.setHasChallenge(true);
        opponent.setRounds(rounds);
        opponent.getReplyWriter().println(player.getName() + " challenges you to a R-P-S Battle for " + rounds + " rounds. Do you accept?");

        return "You challenged " + opponent.getName() + " to a R-P-S Battle for " + rounds + " rounds.";
      }
      else if(player == opponent)
        return "You can't challenge yourself to R-P-S.";
      else {
        return "This person is not in the same room as you or doesn't exist in the game.";
      }
    }

    /** 
     * Accept a challenge to rps
     * @param playerChallenged Name of the player who is being challenged
     * @param playerChallenged Name of the player who is initiating the challenge
     * @param sRounds Number of rounds the player wants to accept
     * @return String of feedback from player's attempt to accept
     */ 
    @Override
    public String accept(String playerChallenged, String playerChallenger, String sRounds){
      Player player = this.playerList.findPlayer(playerChallenger);
      Player opponent = this.playerList.findPlayer(playerChallenged);
      int rounds = 0;
      if(opponent == null || player == null){
        return "This player does not exist in the game or is not online.";
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
      //System.out.println(sRounds + " rounds " + playerChallengee.getRounds());
      //System.out.println(sRounds.equals(playerChallengee.getRounds()));
      if(!(opponent.getRounds() == rounds) ){
        return "You did not accept the challenge for the same amount of rounds, either accept with " + opponent.getRounds() + " rounds or reject the challenge";
      }
      if(opponent.getChallenger().equals(player.getName()) && opponent.getHasChallenge()){
        if(player != opponent && player.getCurrentRoom() == opponent.getCurrentRoom()) {
          player.setRounds(rounds);
          opponent.setRounds(rounds);
          player.getReplyWriter().println(opponent.getName() + " accepts your challenge to a R-P-S for " + rounds + " rounds");
          opponent.setHasChallenge(false);
          opponent.setInBattle(true);
          player.setInBattle(true);
          opponent.getReplyWriter().println("You accept " + player.getName() + "\'s challenge to a R-P-S for " + rounds + " rounds");
          player.getReplyWriter().println("Entering Round\nPick rock, paper, or scissors: ");
          return "Entering Round\nPick rock, paper, or scissors: ";
        }
        else
        {
          return "This person is not in the same room as you or doesn't exist in the game.";
        }
      }
      else if(player == opponent){
        return "You can't challenge yourself to R-P-S.";
      }
      else{
        return "You have not been challenged by " + player.getName();
      }
    }

    /**  
     * Reject another player's challenge to rps
     * @param playerChallenged Name of the player who is being challenged
     * @param playerChallenger Name of the player who is initiating the challenge
     * @return String of feedback from player's attempt to reject the challenge
     */ 
    @Override
    public String reject(String playerChallenged, String playerChallenger){
      Player player = this.playerList.findPlayer(playerChallenger);
      Player opponent = this.playerList.findPlayer(playerChallenged);
      if(opponent == null || player == null){
        return "This player does not exist in the game or is not online.";
      }
      if(opponent.getChallenger().equals(player.getName()) && opponent.getHasChallenge()){
        if(player != opponent && player.getCurrentRoom() == opponent.getCurrentRoom()) {
          opponent.setChallenger(" ");
          player.setChallenger(" ");
          opponent.setHasChallenge(false);
          player.getReplyWriter().println(opponent.getName() + " rejects your challenge to a R-P-S");
          return "You reject " + player.getName() + "\'s challenge to a R-P-S.";
        }
	else{
	  return "This person is not in the same room as you or doesn't exist in the game.";
	}
      }
      else if(player == opponent){
        return "You can't challenge yourself to R-P-S.";
      }
      else{
        return "You have not been challenged by " + player.getName();
      }
    }

    //call when want to log completed battle
    private void rpsLog(String winner, String loser, String status, String winnerPick, String loserPick){
	    rpsLogger.info(winner + " " + status + " against " + loser + "\n" + 
              winner +  " pick " + winnerPick + ", " + loser +  " pick " + loserPick + "\n");
    }
    //initializes rpsLogger
    private void rpsLogger(){
      try{
        rpsHandler = new FileHandler("battles.log", true);
      }
      catch(IOException e){
	System.out.println("Error opening battles.log");
      }
        SimpleFormatter simpleformat = new SimpleFormatter();
        rpsHandler.setFormatter(simpleformat);
        rpsLogger.setLevel(Level.ALL);
        rpsLogger.setUseParentHandlers(false);
        rpsLogger.addHandler(rpsHandler);
    }
    /**  
     * Pick rock, paper or scissors in an rps battle
     * @param name Name of the player
     * @param option What the player picked
     * @return String of feedback from picking
     */ 
    @Override
    public String pickRPS(String name,  String option){
      Player player = this.playerList.findPlayer(name);
      Player opponent = this.playerList.findPlayer(player.getChallenger());
      pickRPSToggle = true;

      if(player.getInBattle() == true && player.getRounds() > 0){
        if(player.getOption().equals("ROCK") || player.getOption().equals("PAPER") || player.getOption().equals("SCISSORS")){
          return "You already pick rock, paper or scissors. You picked " + player.getOption();
        }
        player.setOption(option);
        opponent.setChallengerOption(option);
        String winner = "";

        if(opponent.getOption().equals("ROCK") || opponent.getOption().equals("PAPER") || opponent.getOption().equals("SCISSORS")){
          player.setRounds(player.getRounds() - 1);
          opponent.setRounds(opponent.getRounds()-1);
          switch(player.getOption()) {
            case "ROCK":
              player.getReplyWriter().println("You chose ROCK.");
              if (opponent.getOption().equals("PAPER")) {
                opponent.getReplyWriter().println("You chose PAPER.");
                player.getReplyWriter().println(opponent.getName() + " chose PAPER: You lose.");
                opponent.getReplyWriter().println(player.getName() + " chose ROCK: You win.");
                winner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + opponent.getName() + " won this round.";
                opponent.setWins(opponent.getWins()+1);
                opponent.setRPSwins(opponent.getRPSwins()+1);
                player.setRPSloss(player.getRPSloss()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(player.getName(), opponent.getName(), "wins", player.getOption(), opponent.getOption());
	      }		      
              else if (opponent.getOption().equals("ROCK")){
                opponent.getReplyWriter().println("You chose ROCK.");
                player.getReplyWriter().println(opponent.getName() + " chose ROCK: It is a tie.");
                opponent.getReplyWriter().println(player.getName() + " chose ROCK: It is a tie.");
                winner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: It is a tie this round.";
                opponent.setRPSties(opponent.getRPSties()+1);
                player.setRPSties(player.getRPSties()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(player.getName(), opponent.getName(), "ties", player.getOption(), opponent.getOption());
	      }
              else {
                opponent.getReplyWriter().println("You chose SCISSORS.");
                player.getReplyWriter().println(opponent.getName() + " chose SCISSORS: You win.");
                opponent.getReplyWriter().println(player.getName() + " chose ROCK: You lose.");
                winner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won this round.";
                player.setWins(player.getWins()+1);
		player.setRPSwins(player.getRPSwins()+1);
		opponent.setRPSloss(opponent.getRPSloss()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(opponent.getName(), player.getName(), "wins", opponent.getOption(), player.getOption());
              }
              if(player.getRounds() > 0){
                player.setOption("");
                opponent.setOption("");
                opponent.getReplyWriter().println("You are entering the next round with a score of " + opponent.getWins() + " to " + player.getWins() + "\nPick rock, paper, or scissors: ");
                player.getReplyWriter().println("You are entering the next round with a score of " + player.getWins() + " to " + opponent.getWins() + "\nPick rock, paper, or scissors: ");
                //player.setRounds(player.getRounds() - 1);
              }
              else{

                int p1Win = player.getWins();
                int p2Win = opponent.getWins();
                if(p1Win > p2Win){
                    String winner2 = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
		    pickRPSToggle = false;
		    player.addRpsVictory();//Win counter for Main Questline
                }
                else if(p2Win > p1Win){
                    String winner2 = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + opponent.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
		    pickRPSToggle = false;
		    opponent.addRpsVictory();//Win counter for Main Questline
                }
                else{
                    String noWinner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: They tied in the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), noWinner);
		    pickRPSToggle = false;
                }

                player.setInBattle(false);
                player.setChallenger(" ");
                player.setOption("");
                player.setWins(0);
                opponent.setChallenger(" ");
                opponent.setInBattle(false);
                opponent.setOption("");
                opponent.setWins(0);
              }
              break;
            case "PAPER":
              player.getReplyWriter().println("You chose PAPER.");
              if (opponent.getOption().equals("SCISSORS")) {
                opponent.getReplyWriter().println("You chose SCISSORS.");
                player.getReplyWriter().println(opponent.getName() + " chose SCISSORS: You lose.");
                opponent.getReplyWriter().println(player.getName() + " chose PAPER: You win.");
                winner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + opponent.getName() + " won this round.";
                opponent.setWins(opponent.getWins()+1);
                opponent.setRPSwins(opponent.getRPSwins()+1);
                player.setRPSloss(player.getRPSloss()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(opponent.getName(), player.getName(), "wins", opponent.getOption(), player.getOption());
              	
	      }
              else if (opponent.getOption().equals("PAPER")){
                opponent.getReplyWriter().println("You chose PAPER.");
                player.getReplyWriter().println(opponent.getName() + " chose PAPER: It is a tie.");
                opponent.getReplyWriter().println(player.getName() + " chose PAPER: It is a tie.");
                winner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: It is a tie this round.";
                opponent.setRPSties(opponent.getRPSties()+1);
                player.setRPSties(player.getRPSties()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(player.getName(), opponent.getName(), "ties", player.getOption(), opponent.getOption());
	      }
              else {
                opponent.getReplyWriter().println("You chose ROCK.");
                player.getReplyWriter().println(opponent.getName() + " chose ROCK: You win.");
                opponent.getReplyWriter().println(player.getName() + " chose PAPER: You lose.");
                winner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won this round.";
                player.setWins(player.getWins()+1);
		player.setRPSwins(player.getRPSwins()+1);
		opponent.setRPSloss(opponent.getRPSloss()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(player.getName(), opponent.getName(), "wins", player.getOption(), opponent.getOption());
              	
              }
              if(player.getRounds() > 0){

                player.setOption("");
                opponent.setOption("");
                opponent.getReplyWriter().println("You are entering the next round with a score of " + opponent.getWins() + " to " + player.getWins() + "\nPick rock, paper, or scissors: ");
                player.getReplyWriter().println("You are entering the next round with a score of " + player.getWins() + " to " + opponent.getWins() + "\nPick rock, paper, or scissors: ");
                //player.setRounds(player.getRounds() - 1);
              }
              else{

                int p1Win = player.getWins();
                int p2Win = opponent.getWins();
                if(p1Win > p2Win){
                    String winner2 = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
                    pickRPSToggle = false;
		}
                else if(p2Win > p1Win){
                    String winner2 = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + opponent.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
		    pickRPSToggle = false;
                }
                else{
                    String noWinner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: They tied in the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), noWinner);
		    pickRPSToggle = false;
                }

                player.setInBattle(false);
                player.setChallenger(" ");
                player.setOption("");
                player.setWins(0);
                opponent.setChallenger(" ");
                opponent.setInBattle(false);
                opponent.setOption("");
                opponent.setWins(0);
              }
              break;
            case "SCISSORS":
              player.getReplyWriter().println("You chose SCISSORS.");
              if (opponent.getOption().equals("ROCK")) {
                opponent.getReplyWriter().println("You chose ROCK.");
                player.getReplyWriter().println(opponent.getName() + " chose ROCK: You lose.");
                opponent.getReplyWriter().println(player.getName() + " chose SCISSORS: You win.");
                winner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + opponent.getName() + " won this round.";
                opponent.setWins(opponent.getWins()+1);
                opponent.setRPSwins(opponent.getRPSwins()+1);
                player.setRPSloss(player.getRPSloss()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(opponent.getName(), player.getName(), "wins", opponent.getOption(), player.getOption());
	      }
              else if (opponent.getOption().equals("SCISSORS")){
                opponent.getReplyWriter().println("You chose SCISSORS.");
                player.getReplyWriter().println(opponent.getName() + " chose SCISSORS: It is a tie.");
                opponent.getReplyWriter().println(player.getName() + " chose SCISSORS: It is a tie.");
                winner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: It is a tie this round.";
		opponent.setRPSties(opponent.getRPSties()+1);
		player.setRPSties(player.getRPSties()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(opponent.getName(), player.getName(), "ties", opponent.getOption(), player.getOption());
	      }
              else {
                opponent.getReplyWriter().println("You chose PAPER.");
                player.getReplyWriter().println(opponent.getName() + " chose PAPER: You win.");
                opponent.getReplyWriter().println(player.getName() + " chose SCISSORS: You lose.");
                winner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won this round.";
                player.setWins(player.getWins()+1);
		player.setRPSwins(player.getRPSwins()+1);
		opponent.setRPSloss(opponent.getRPSloss()+1);
                this.broadcast(map.findRoom(player.getCurrentRoom()), winner);
		rpsLog(player.getName(), opponent.getName(), "wins", player.getOption(), opponent.getOption());
              	
              }
              if(player.getRounds() > 0){

                player.setOption("");
                opponent.setOption("");
                opponent.getReplyWriter().println("You are entering the next round with a score of " + opponent.getWins() + " to " + player.getWins() + "\nPick rock, paper, or scissors: ");
                player.getReplyWriter().println("You are entering the next round with a score of " + player.getWins() + " to " + opponent.getWins() + "\nPick rock, paper, or scissors: ");
                //player.setRounds(player.getRounds() - 1);
              }
              else{

                int p1Win = player.getWins();
                int p2Win = opponent.getWins();
                if(p1Win > p2Win){
                    String winner2 = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + player.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
		    pickRPSToggle = false;
                }
                else if(p2Win > p1Win){
                    String winner2 = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: " + opponent.getName() + " won the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), winner2);
		    pickRPSToggle = false;
                }
                else{
                    String noWinner = player.getName() + " challenged " + opponent.getName() + " to a Rock Paper Scissors Battle: They tied in the tournament with a final score of " + p1Win + " - " + p2Win + ".";
                    this.broadcast(map.findRoom(player.getCurrentRoom()), noWinner);
		    pickRPSToggle = false;
                }

                player.setInBattle(false);
                player.setChallenger(" ");
                player.setOption("");
                player.setWins(0);
                opponent.setChallenger(" ");
                opponent.setInBattle(false);
                opponent.setOption("");
                opponent.setWins(0);
              }
              break;
            default:
              break;
          }
        }
        //player.setRounds(player.getRounds() - 1);
        //opponent.setRounds(opponent.getRounds()-1);
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
			player.setRewardAmount(0.1);//task 229, clear reward increment as specified by task
			player.setRewardProgress(0);//task 229, resets time until reward as specified by task 
			this.playerList.removePlayer(name);
            connectionLog(false, player.getName());
            this.accountManager.forceUpdatePlayerFile(player);
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
	String tempMessage = message;
	if(srcPlayer.getRathskellerStatus()) {
		tempMessage = translateRathskeller(message);
	}
        if (dstPlayer == null)
            return "Player " + dstName + " not found.";
        if (!dstPlayer.messagePlayer(srcPlayer, "whispers", tempMessage))
            return "Player " + dstPlayer.getName() + " is ignoring you.";
        dstPlayer.setLastPlayer(srcName);
        chatLog(srcPlayer, 1, tempMessage, dstPlayer.getName());
        return srcPlayer.getMessage() + "whisper to " + dstPlayer.getName() + ", " + tempMessage;
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
     * Create a new chatroom
     * @param playerName Name of the player creating the chatroom
     * @param chatName Name of the chatroom
     * @return Message showing success
     * @throws RemoteException
     */
    public String makeChat(String playerName, String chatName) {
    	Player creator = this.playerList.findPlayer(playerName);
    	for (Chatroom chat:chatrooms) {
    		if (chat.getName().equals(chatName.toUpperCase())) {
    			return "This chatroom already exists.";
    		}
    	}
    	Chatroom newChat = new Chatroom(creator, chatName.toUpperCase());
    	chatrooms.add(newChat);
    	return "Chatroom " + chatName.toUpperCase() + " created.";
    }
    
    /**
     * Invite a player to your current chatroom.
     * @param srcPlayer Name of player sending the invite
     * @param dstPlayer Name of player receiving the invite
     * @return Message showing success
     * @throws RemoteException
     */
    public String invChat(String srcPlayer, String dstPlayer, String chatName) {
    	Player sender = this.playerList.findPlayer(srcPlayer);
    	Player invitee = this.playerList.findPlayer(dstPlayer);
        if (invitee == null) {
            return "Player " + dstPlayer + " not found.";
        }
        if (srcPlayer.equals(dstPlayer)) {
        	return "You can't invite yourself to a chat.";
        }
        for (Chatroom chat: chatrooms) {
        	if (chat.getName().equals(chatName.toUpperCase())) {
        		if (!chat.getMembers().contains(sender)) {
        			return "You are not in the chatroom [" + chatName.toUpperCase() + "]";
        		}
            	if (chat.getMembers().contains(invitee)) {
            		return dstPlayer + " is already in the chatroom [" + chatName.toUpperCase() + "]";
            	}
            	if (chat.getInvited().contains(invitee)) {
            		return dstPlayer + " is already invited to the chatroom [" + chatName.toUpperCase() + "]";
            	}
        		String message = "Hey! Feel free to join the chatroom [" + chatName.toUpperCase() + "]";
        		whisper(srcPlayer, dstPlayer, message);
        		chat.addInvited(invitee);
        		return "You invited " + dstPlayer + " to join [" + chatName.toUpperCase() + "]";
        	}
        }
    	return "You are trying to invite " + dstPlayer + " to a non-existent chatroom [" + chatName.toUpperCase() + "]";
    }
    
    /**
     * Join a player's chatroom
     * @param srcPlayer Name of player joining
     * @param dstPlayer Name of player in the target chatroom
     * @return Message showing success
     * @throws RemoteException
     */
    public String joinChat(String srcPlayer, String chatName) {
    	Player joining = this.playerList.findPlayer(srcPlayer);
    	Chatroom chatToJoin = null;
    	for (Chatroom chat:chatrooms) {
    		if (chat.getName().equals(chatName.toUpperCase())) {
    			chatToJoin = chat;
    		}
    	}
    	if (chatToJoin == null) {
    		return "Chatroom [" + chatName.toUpperCase() + "] does not exist.";
    	}
    	if (chatToJoin.getMembers().contains(joining)) {
    		return "You are already in chatroom [" + chatName.toUpperCase() + "]";
    	}
    	if (!chatToJoin.getInvited().contains(joining)) {
    		return "You were not invited to join chatroom [" + chatName.toUpperCase() + "]";
    	}
    	chatToJoin.addMember(joining);
    	chatToJoin.removeInvited(joining);
        return "You joined chatroom [" + chatName.toUpperCase() + "]";
    }
    
    /**
     * Leave a chatroom
     * @param srcPlayer Name of player leaving
     * @param chatName Name of chatroom to leave
     * @return Message showing success
     * @throws RemoteException
     */
    public String leaveChat(String srcPlayer, String chatName) {
    	Player leaving = this.playerList.findPlayer(srcPlayer);
    	Chatroom chatToLeave = null;
    	for (Chatroom chat:chatrooms) {
    		if (chat.getName().equals(chatName.toUpperCase())) {
    			chatToLeave = chat;
    		}
    	}
    	if (chatToLeave == null) {
    		return "Chatroom [" + chatName.toUpperCase() + "] does not exist.";
    	}
    	if (!chatToLeave.getMembers().contains(leaving)) {
    		return "You are not in chatroom [" + chatName.toUpperCase() + "]";
    	}
    	chatToLeave.removeMember(leaving);
    	if (chatToLeave.getMembers().size() == 0) {
    		chatrooms.remove(chatToLeave);
    		chatToLeave = null;
    	}
        return "You left chatroom [" + chatName.toUpperCase() + "]";
    }
    
    /**
     * Check if chatroom exists
     * @return boolean showing success
     * @throws RemoteException
     */
    public boolean checkChat(String command) {
    	for (Chatroom chat:chatrooms) {
    		if (chat.getName().equals(command)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Message a chatroom
     * @param srcPlayer Name of player sending the message
     * @param message The message to be sent
     * @param chatName The name of the chat to send the message to
     * @return Message showing success
     * @throws RemoteException
     */
    public String messageChat(String srcPlayer, String message, String chatName) {
		Player player = this.playerList.findPlayer(srcPlayer);
		Chatroom chatToMessage = null;
    	for (Chatroom chat:chatrooms) {
            if (chat.getName().equals(chatName.toUpperCase())) {
            	chatToMessage = chat;
               	if (!chat.getMembers().contains(player)) {
               		return "You are not in the chatroom [" + chatName.toUpperCase() + "]";
               	}
    		}
    	}
    	if (chatToMessage == null) {
    		return "You are trying to message a non-existent chatroom [" + chatName.toUpperCase() + "]";
    	}
		if (player != null) {
		    for (Player otherPlayer : chatToMessage.getMembers()) {
		        if (otherPlayer != player) {
		            otherPlayer.messagePlayer(player, "messages chatroom [" + chatName.toUpperCase() + "]", message);
		        }
		    }
            chatLog(player, 0, message, "Chatroom " + chatName);
            return player.getMessage() + "message, " + message + " to chatroom [" + chatName.toUpperCase() + "]";

		} else {
			return null;
		}
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
	    String tempMessage = message;
	    if(player.getRathskellerStatus()) {
		tempMessage = translateRathskeller(message);
	    }
            for(Player otherPlayer : this.playerList) {
                if(otherPlayer != player)
                    otherPlayer.messagePlayer(player, "shouts", tempMessage);
            }
                    chatLog(player, 2, tempMessage,"Everyone");
            return player.getMessage() + "shout, " + tempMessage;
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

    /**  
     * NPC in the Main Quad that teaches players how to play rps
     * @param player Name of the player that wants to be taught
     * @return String of teachings from the NPC or error message if not in Main Quad
     */  @Override
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
	 * Remove question by position in list. Returns the status of the removal.<br>
	 * <br>
	 * Possible Responses:<br>
	 * NOT_FOUND<br>
	 * INTERNAL_SERVER_ERROR<br>
	 * FAILURE<br>
	 * SUCCESS<br>
	 * 
	 * @param name
	 * @param num - which question
	 * @return removedStatus
	 */
	@Override
	public Responses removeQuestion(String name, int num) {
		DataResponse<PlayerAccount> account = accountManager.getAccount(name);
		if (!account.success())
			return account.error;
		accountManager.markAccount(name);
		return account.data.removeQuestion(name, num);
	}

	/**
	 * Returns either the questions or a status error<br>
	 * <br>
	 * Possible Errors:<br>
	 * NOT_FOUND<br>
	 * INTERNAL_SERVER_ERROR<br>
	 * 
	 * @param name
	 * @return questionsStatus
	 */
	@Override
	public DataResponse<ArrayList<String>> getQuestions(String name) {
		DataResponse<PlayerAccount> account = accountManager.getAccount(name);
		if (!account.success())
			return new DataResponse<>(account.error);
		accountManager.markAccount(name);
		return account.data.getQuestions(name);
	}

	/**
	 * Returns whether the answers were correct or an error occurred.<br>
	 * <br>
	 * Possible Responses:<br>
	 * NOT_FOUND<br>
	 * INTERNAL_SERVER_ERROR<br>
	 * FAILURE<br>
	 * SUCCESS<br>
	 * 
	 * @param name
	 * @param answers
	 * @return verifiedStatus
	 */
	@Override
	public Responses verifyAnswers(String name, ArrayList<String> answers) {
		DataResponse<PlayerAccount> account = accountManager.getAccount(name);
		if (!account.success())
			return account.error;
		accountManager.markAccount(name);
		return account.data.verifyAnswers(name, answers);
	}

	/**
	 * Returns the status of adding a recovery question.<br>
	 * <br>
	 * Possible Responses:<br>
	 * NOT_FOUND<br>
	 * INTERAL_SERVER_ERROR<br>
	 * FAILURE      - bad question length<br>
	 * BAD_PASSWORD - need answer<br>
	 * SUCCESS<br>
	 * 
	 * @param name
	 * @param question
	 * @param answer
	 * @return addStatus
	 */
	@Override
	public Responses addRecoveryQuestion(String name, String question, String answer) {
		DataResponse<PlayerAccount> account = accountManager.getAccount(name);
		if (!account.success())
			return account.error;
		accountManager.markAccount(name);
		return account.data.addRecoveryQuestion(name, question, answer);
	}

	/**
	 * Returns either account age or error.<br>
	 * <br>
	 * Possible Errors:<br>
	 * NOT_FOUND<br>
	 * INTERAL_SERVER_ERROR<br>
	 * 
	 * @param name
	 * @return ageStatus
	 */
	@Override
	public DataResponse<Long> getAccountAge(String name) {
		DataResponse<PlayerAccount> account = accountManager.getAccount(name);
		if (!account.success())
			return new DataResponse<Long>(account.error);
		accountManager.markAccount(name);
		return account.data.getAccountAge(name);
	}

	/**
	 * Returns the status of testing a password against current.<br>
	 * <br>
	 * Possible Responses:<br>
	 * NOT_FOUND<br>
	 * INTERNAL_SERVER_ERROR<br>
	 * FAILURE<br>
	 * SUCCESS<br>
	 * 
	 * @param name
	 * @param password
	 * @return verifyStatus
	 */
	@Override
	public Responses verifyPassword(String name, String password) {
		DataResponse<PlayerAccount> account = accountManager.getAccount(name);
		if (!account.success())
			return account.error;
		accountManager.markAccount(name);
		return account.data.verifyPassword(name, password);
	}

	/**
	 * Returns the status of changing a password.<br>
	 * <br>
	 * Possible Responses:<br>
	 * NOT_FOUND<br>
	 * INTERNAL_SERVER_ERROR<br>
	 * SUCCESS<br>
	 * 
	 * @param name
	 * @param newPassword
	 * @return changeStatus
	 */
	@Override
	public Responses changePassword(String name, String password) {
		DataResponse<PlayerAccount> account = accountManager.getAccount(name);
		if (!account.success())
			return account.error;
		accountManager.markAccount(name);
		return account.data.changePassword(name, password);
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

    @Override
    public boolean isPlayerOnline(String name) {
        return this.playerList.findPlayer(name) != null;
    }

	@Override
	public String listAllPlayers(){
		String allNames = "";
		HashSet<Player> allPlayerIds = accountManager.getListPlayers();
		for(Player player : allPlayerIds){
			allNames += player.getName() + ", ";
		}
		allNames = allNames.substring(0, allNames.length()-2);
		return allNames;
	}

	@Override
	public String rankings(String ranks, String userOption){
		HashSet<Player> allPlayerIds = accountManager.getListPlayers();
		ArrayList<Player> rankingScores = new ArrayList<Player>();
		ArrayList<String> allPlayerNames = new ArrayList<String>();
		//System.out.println(allPlayerIds);
		for(Player playerID: allPlayerIds){

			//Player playerRPS = this.playerList.findPlayer(playerID);
			//rankingScores.add(playerID.getName());
			//System.out.println(playerID.getName());
			//System.out.println(playerID.getRPSwins());
			//System.out.println(playerID.getRPSloss());
			//System.out.println(playerID.getRPSties());
			int totalGames = playerID.getRPSwins()+playerID.getRPSloss()+playerID.getRPSties();
			System.out.println(totalGames);
			double rankingScore = ((playerID.getRPSwins()+(.5 * playerID.getRPSties()))/(1 + playerID.getRPSloss()))*totalGames;
			playerID.setPlayerRankingScore(rankingScore);
			rankingScores.add(playerID);
			allPlayerNames.add(playerID.getName());
			//String rankingScoreString = Double.toString(rankingScore);
			//rankingScores.add(Integer.toString(totalGames));
			
			
			//System.out.println(playerRPS.getRPSwins());
		}

		for(int i = 0; i < rankingScores.size(); i++){
			for(int j = rankingScores.size()-1; j > i; j--){
				if(rankingScores.get(i).getPlayerRankingScore() < rankingScores.get(j).getPlayerRankingScore()){
					Player tmp = rankingScores.get(i);
					rankingScores.set(i, rankingScores.get(j));
					rankingScores.set(j, tmp);
				}
			}
		}
		
		//Giving players their titles
		for(int i = 0; i < rankingScores.size(); i++){
			if(i == 0){
				rankingScores.get(i).setRankingTitle("The Grand Poobah");
			}
			else if(i == 1){
				rankingScores.get(i).setRankingTitle("GrandMaster");
			}
			else if(i == 2){
				rankingScores.get(i).setRankingTitle("Master");
			}
			else if(i == 3){
				rankingScores.get(i).setRankingTitle("Darth");
			}
			else if(i == 4){
				rankingScores.get(i).setRankingTitle("Elite");
			}
			else{
				rankingScores.get(i).setRankingTitle("Casual");
			}
		}
		
		if(userOption.equals("top5")){
			//then return top 5 rankings with their titles
			//getRankingTitle()
			String top5 = "THE TOP 5 RPS BATTLERS:\n";
			for(int i = 0; i < 5; i++){
				top5 += Integer.toString(i+1) + ". " + rankingScores.get(i).getRankingTitle() + " " + rankingScores.get(i).getName() + "\n";
			}
			return top5;
		}
		else if(allPlayerNames.contains(userOption)){
			//then reutnr specific user ranking score with his/her title
			String userTitleRank = "";
			for(Player player: rankingScores){
				if(player.getName().equals(userOption)){
					userTitleRank = player.getRankingTitle() + " " + player.getName();
					break;
				}
			}
			return userTitleRank;
		}
		else{
			return "This user doesn't exist or incorrect input";
		}
		
			
		
	}


}
