 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashSet;
import java.util.Scanner;

import java.lang.System; //used for use item and title feature

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.*;


/**
 *
 * @author Kevin
 */
public class GameClient {
    // Control flag for running the game.
    private boolean runGame;

    // Remote object for RMI server access
    protected GameObjectInterface remoteGameInterface;
    
    // Members for running the remote receive connection (for non-managed events)
    private boolean runListener;
    protected ServerSocket remoteListener;
    private Thread remoteOutputThread;  
    
    // Members related to the player in the game.
    protected String playerName;

    private String lastCommand;

	private AccountEditWizard accountEditWizard;
    
    //used for timing the title feature
    private long startTime;

    /** 
     * Main class for running the game client.
     */
    public GameClient(String host) {
        this.runGame = true;
        boolean nameSat = false;
        
	//title feature
	startTime = 0;

        //please modify the xml to add more commands
        showIntroduction();
        showCommand();
        


        // Set up for keyboard input for local commands.
        InputStreamReader keyboardReader = new InputStreamReader(System.in);
        BufferedReader keyboardInput = new BufferedReader(keyboardReader);
        String keyboardStatement;

        try {
            // Establish RMI connection with the server
            System.setSecurityManager(new SecurityManager());
            String strName = "rmi://"+host+"/GameService";
            remoteGameInterface = (GameObjectInterface) Naming.lookup(strName);

            // User may either log in or create an account
            // Account names are trimmed and unique ignoring case
			int loginFailCount=0;
            while(nameSat == false) {
				try {
					System.out.println("Logging in or creating account?");
					String mode;
					do {
						System.out.print("(L/C)> ");
						mode = keyboardInput.readLine().toUpperCase().trim();
					} while (!(mode.equals("L") || mode.equals("C")));
					System.out.print("Username: ");
					this.playerName = keyboardInput.readLine().trim();
					System.out.print("Password: ");
					String pass = new String(System.console().readPassword()); //task 221 hides password
					switch (mode) {
					case "L":
                        //First check if any user by that name is currently logged on
                        if(remoteGameInterface.isPlayerOnline(this.playerName)){
                            System.out.println("This account is alreay logged in.\n");
                            break;
                        }
                                                    
						nameSat = remoteGameInterface.joinGame(this.playerName, pass);
						if (!nameSat) {
							System.out.println("Username and password combination invalid");
							if(++loginFailCount==3) {
								String temp;
								if((temp = resetPassword(keyboardInput)) != null)
									nameSat = remoteGameInterface.joinGame(this.playerName, temp);
							}
						}
						break;
					case "C":
						loginFailCount=0;
						Responses resp = remoteGameInterface.createAccountAndJoinGame(playerName, pass);
						switch (resp) {
						case BAD_USERNAME_FORMAT:
							System.out
									.println("This is a bad user name. Please use only spaces, numbers, and letters, with 2-15 characters.");
							break;
						case USERNAME_TAKEN:
							System.out.println("Sorry but this username was already taken.");
							break;
						case UNKNOWN_FAILURE:
							System.out.println("The server experienced an unknown failure.");
							break;
						case SUCCESS:
							nameSat = true;
							break;
						default:
							System.out.println("Unknown server behavior");
							break;
						}
						if (!nameSat)
							System.out.println();

					}

				} catch (IOException ex) {
					System.err.println(
							"[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
					System.exit(-1);
				}
			}

            // Player has joined, now start up the remote socket.
            this.runListener = true;
            remoteOutputThread = new Thread(new GameClient.ReplyRemote(host));
            remoteOutputThread.setDaemon(true);
            remoteOutputThread.start();
            
            //Sends a heartbeat every 10 seconds
            Thread hbThread = new Thread(new Runnable() {
                @Override
                public void run() {
                        while(true) {
                            try {
                                Thread.sleep(10000);
                                try{
                                    remoteGameInterface.heartbeatCheck(playerName);
                                }catch(RemoteException re) {
                                    System.err.println("[CRITICAL ERROR] There was a severe error with the RMI mechanism.");
                                    System.err.println("[CRITICAL ERROR] Code: " + re);
                                    System.exit(-1);
                                } 
                            } catch (InterruptedException ex) {
                            }
                        }
                    }
                });
            
            hbThread.setDaemon(true);
            hbThread.setName("heartbeat");
            hbThread.start();
            

            // 409 Word Filter
            readWordFilterFile();
            // 413 Prefix
            readPrefixFromFile();


			accountEditWizard = new AccountEditWizard(keyboardInput, System.out, remoteGameInterface, this.playerName);

			accountEditWizard = new AccountEditWizard(keyboardInput, System.out, remoteGameInterface, this.playerName);
			
			// check venmo mailbox. Team4: Alaqeel
			remoteGameInterface.checkVenmoMail(this.playerName);
			
            // Collect input for the game.
            while(runGame) {
                try {
                    keyboardStatement = keyboardInput.readLine();
                    parseInput(keyboardStatement);
                } catch (IOException ex) {
                    System.err.println("[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
                    System.exit(-1);
                }
            }                
        } catch (NotBoundException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch(RemoteException re) {
            System.err.println("[CRITICAL ERROR] There was a severe error with the RMI mechanism.");
            System.err.println("[CRITICAL ERROR] Code: " + re);
            System.exit(-1);
        } catch (NoSuchMethodException e) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, e);
        } catch (SecurityException e) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, e);
        } catch (InstantiationException e) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, e);
        } catch (IllegalAccessException e) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, e);
        } catch (IllegalArgumentException e) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, e);
        } catch (InvocationTargetException e) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    // Helper for Features 4XX - Chat System
    /**
     * Method to decorate messages intended for use with the chat system.
     * @param msgTokens User input words to decorate into a "message".
     * @return "message" to be sent by the user
     */
    private String parseMessage(ArrayList<String> msgTokens) {
        //TODO: Note - Tokenizer currently trims out multiple spaces - bug or feature?
        StringBuilder msgBuilder = new StringBuilder();
        while (!msgTokens.isEmpty()) {
            msgBuilder.append(msgTokens.remove(0));
            if (!msgTokens.isEmpty())
                msgBuilder.append(" ");
        }
        return msgBuilder.toString();
    }

    /**
     * Simple method to parse the local input and remotely execute the RMI commands.
     * @param input
     */
    private void parseInput(String input) {
        boolean reply;
       
	//removes player titles after a certain time
	if(startTime != 0) {
		long timeElapsed = System.nanoTime() - startTime;
		double timeElapsedInSeconds = ((double)timeElapsed) / 1000000000.0;
		if(timeElapsedInSeconds > 20.0) {
			try {
				remoteGameInterface.removePlayerTitle(this.playerName);
				startTime = 0;
			}
			catch(RemoteException e) {
				System.out.println("Title removing went wrong");
			}
		}
	}

        // First, tokenize the raw input.
        StringTokenizer commandTokens = new StringTokenizer(input);
        ArrayList<String> tokens = new ArrayList<>();
        while(commandTokens.hasMoreTokens() == true) {
            tokens.add(commandTokens.nextToken());
        }

        if(tokens.isEmpty()) {
            System.out.println("The keyboard input had no commands.");
            return;
        }

        String message = "";

        String command = tokens.remove(0).toUpperCase();
        //for redo old messages
        String commandCheck = input.toUpperCase();

        try {
            switch(command) {
                
                case "CHEST": //115 chest implementation
                    //this command will only work when the player is in his dormRoom

                        if (remoteGameInterface.chest(this.playerName,"check","").equals("not in dorm room")) {
                            System.out.println("You must be in your dorm to use this command.");
                            return;
                        }
                        //from here on player is in the dormroom
                        
                        //System.out.println(remoteGameInterface.chest(this.playerName));   
                        InputStreamReader keyRdr = new InputStreamReader(System.in);
                        BufferedReader keyIn = new BufferedReader(keyRdr);
                        boolean valid = true;
                        try {
                                while(valid) {
                                    /* print the menu */
                                    //this.broadcast( player, droom.chestMenu() );
                                    System.out.println(remoteGameInterface.chest(this.playerName,"menu","") );
                                    input = keyIn.readLine();
                                    input.toLowerCase();
                                    switch(input) {
                                            case "a":// add item to chest 
                                                    System.out.println("Enter object name to transfer into the chest");
                                                    input = keyIn.readLine();
                                                    //Item object = player.removeObjectFromInventory(input);
                                                    //if(object != null) {
                                                    if(remoteGameInterface.chest(this.playerName,"a",input).equals("Item added")){
                                                        System.out.println( "You placed a " + input +" in the chest");
                                                    }else{
                                                       System.out.println("Object not found in your inventory"); 
                                                       System.out.println("please type the exact name"); 
                                                    }    
                                                    break;
                                            case "x"://extract item from chest into pocket
                                                    //System.out.println("xxxxxx");
                                                    System.out.println("Enter object name to transfer from the chest");
                                                    input = keyIn.readLine();
                                                    //System.out.println(remoteGameInterface.chest(this.playerName,"x",input)); 
                                                    if(remoteGameInterface.chest(this.playerName,"x",input).equals("ok")){
                                                        System.out.println( "You placed a " + input +" in your pockets");
                                                    }else{
                                                       System.out.println("Object not found in your inventory"); 
                                                       System.out.println("please type the exact name"); 
                                                    }    
                                                    break;
                                            case "p"://print chest content
                                                    System.out.println(remoteGameInterface.chest(this.playerName,"p", ""));  
                                                    break;
                                            case "q":// quit this sub menu 
                                                    System.out.println("exiting chest menu......done"); 
                                                    valid = false;
                                                    break;
                                            default:
                                                    System.out.println("Please enter a valid input value");
                                    }//end switch 
                              }//end while 
                        }//end try blc
                        catch(IOException e) { 
                              System.err.println("[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
                              System.exit(-1);
                        }    

                    break;//end chest case              
                case "LOOK":
                    System.out.println(remoteGameInterface.look(this.playerName));   
                    break;
                case "LEFT":
                    System.out.println(remoteGameInterface.left(this.playerName));
                    break;
                case "RIGHT":
                    System.out.println(remoteGameInterface.right(this.playerName));
                    break;
                case "PICKUPALL":
                System.out.println(remoteGameInterface.pickupAll(this.playerName));
                    break;
                case "SAY":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to say something in order to SAY.");
                    }
                    else {
                        while(tokens.isEmpty() == false) {
                            message += tokens.remove(0);
                            if(tokens.isEmpty() == false) {
                                message += " ";
                            }
                        }                        
                        System.out.println(remoteGameInterface.say(this.playerName, message));
                    }
                    break;
                case "TALK":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide an NPC's name to talk to.");
                    } 
                    else {
                        boolean done = false;
                        String npcName = tokens.remove(0);
                    			//Integrated team 6 talk command with tutorial quest talk command, due to a late merge on the tutorial quest command
			//Checks if the NPC is implemented for team 6 Main Story compatability, if true, calls team 6 talk command
			//Both NPCs can exist and be talked to with the same command
			if(remoteGameInterface.checkNPCValidity(this.playerName, npcName.toUpperCase())){
			    System.out.println(remoteGameInterface.talk(this.playerName, npcName.toUpperCase()));
                        }else{
                            String output = remoteGameInterface.talkNpc(this.playerName, npcName);
                            if (output == null) {
                                System.out.println("Named NPC not in room");
                            }
                            else {
                                System.out.println("Dialogue Options: enter the number of the option to select it, or done to exit.");
                                System.out.print(output);
                                Scanner scan = new Scanner(System.in);
                                while (!done) {
                                    String line = scan.nextLine();
                                    if (line.equalsIgnoreCase("done")) {
                                        done = true;
                                    }
                                    else {
                                        int dialogueChoice = Integer.parseInt(line)-1;
                                        System.out.println(remoteGameInterface.selectNPCDialogueOption(this.playerName, npcName, dialogueChoice));
                                        System.out.println("Dialogue Options: enter the number of the option to select it, or done to exit.");
                                    }            
		    
				}
                            }
                    }
                    }
                    break;
		case "JOURNAL":
		    System.out.println(remoteGameInterface.journal(this.playerName));
		    break;
                case "MOVE":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide a direction to move.");
                    } else {
                        Direction dir = Direction.toValue(tokens.remove(0));
                        if(dir!=null) {
                            System.out.println(remoteGameInterface.move(this.playerName, dir));
                        }
                        else{
                            System.out.println("Direction for the move command is invalid");
                        }
                    }
                    break;
                case "SHOUT":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to say something in order to SHOUT.");
                    }
                    else {
                        while(tokens.isEmpty() == false) {
                            message += tokens.remove(0);
                            if(tokens.isEmpty() == false) {
                                message += " ";
                            }
                        }
                        System.out.println(remoteGameInterface.shout(this.playerName, message));
                    }
                    break;
                case "ONLINE":
                    System.out.println(remoteGameInterface.showPlayers());
                    break;
                case "W":
                case "WHISPER":
                    if (tokens.isEmpty()) {
                        System.err.println("You need to provide a player to whisper.");
                    }
                    else if (tokens.size() < 2) {
                        System.err.println("You need to provide a message to whisper.");
                    }
                    else {
                        String dstPlayerName = tokens.remove(0).toLowerCase();
                        message = parseMessage(tokens);
                        System.out.println(remoteGameInterface.whisper(this.playerName, dstPlayerName, message));
                    }
                    break;
                case "R":
                case "REPLY":
                    if (tokens.isEmpty()) {
                        System.err.println("You need to provide a message.");
                    }
                    else {
                        message = parseMessage(tokens);
                        System.out.println(remoteGameInterface.quickReply(this.playerName, message));
                    }
                    break;
                case "MAKECHAT":
                    if (tokens.isEmpty()) {
                        System.err.println("You need to provide chatroom name.");
                    }
                    else if (tokens.size() > 1) {
                        System.err.println("The chat name has to be one word.");
                    }
                    else {
                    	String chatName = tokens.remove(0).toLowerCase();
                        System.out.println(remoteGameInterface.makeChat(this.playerName, chatName));
                    }
                    break;
                case "INVCHAT":
                    if (tokens.isEmpty()) {
                        System.err.println("You need to provide a player to invite.");
                    }
                    else if (tokens.size() < 2) {
                        System.err.println("You need to provide a chatroom to invite the player to.");
                    }
                    else {
                    	String playerName = tokens.remove(0).toLowerCase();
                    	String chatName = tokens.remove(0);
                        System.out.println(remoteGameInterface.invChat(this.playerName, playerName, chatName));
                    }
                    break;
                case "JOINCHAT":
                    if (tokens.isEmpty()) {
                        System.err.println("You need to provide a chatroom name.");
                    }
                    else {
                    	String chatName = tokens.remove(0).toLowerCase();
                        System.out.println(remoteGameInterface.joinChat(this.playerName, chatName));
                    }
                    break;
                case "LEAVECHAT":
                    if (tokens.isEmpty()) {
                        System.err.println("You need to provide a chatroom name.");
                    }
                    else {
                    	String chatName = tokens.remove(0).toLowerCase();
                        System.out.println(remoteGameInterface.leaveChat(this.playerName, chatName));
                    }
                    break;
                case "IGNORE":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide a player to ignore");
                    }
                    else {
                        System.out.println(remoteGameInterface.ignorePlayer(this.playerName, tokens.remove(0)));
                    }
                    break;
                case "IGNORELIST":
                    System.out.println(remoteGameInterface.getIgnoredPlayersList(this.playerName));
                    break;
                case "UNIGNORE":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide a player to unignore");
                    }
                    else {
                        System.out.println(remoteGameInterface.unIgnorePlayer(this.playerName, tokens.remove(0)));
                    }
                    break;
                 case "JOKE":
                     System.out.println((remoteGameInterface.say(this.playerName, ("Here's a joke for you: " + remoteGameInterface.joke("jokes.txt")))));
                     break;
                case "REDO":
                    if(lastCommand==null)
                    {
                        System.out.println("No command to redo");
                        break;
                    }
                    parseInput(lastCommand);
                    break;
		case "USE":
		    if(tokens.isEmpty()) {
			System.err.println("You need to provide an item to use.");
		    }
		    else if(tokens.size() != 1) {
			System.err.println("You can only use one item at a time.");
		    }
		    else {
		    	System.out.println(remoteGameInterface.useItem(this.playerName, tokens.remove(0).toLowerCase()));
			startTime = System.nanoTime();
		    }
		    break;
	
		case "TITLE": {
		   if(!tokens.isEmpty()) {
			System.err.println("What are you even trying to do?");
		   }
		   else {
			String title = remoteGameInterface.getPlayerTitle(this.playerName);
			if(title.equals("")) {
				System.out.println("You do not have a title");
			}
			else {
				System.out.println(this.playerName + " the " + title);
			}
		   }
		   break;
		}

		case "O":
		    
		case "OFFER":

		    if (tokens.isEmpty()){
			System.err.println("You need to provide an item to offer.");
		    }
		    else if (tokens.size() < 2) { 
			System.err.println("You need to type for.");
		    }
		    else if (tokens.size() < 3) {
			System.err.println("You need to pick an item from their inventory");
		    }
		    else {
			String message1 = tokens.remove(0).toLowerCase();
			String junk = tokens.remove(0).toLowerCase();
			//String message2 = tokens.remove(0).toLowerCase();
			System.out.println(remoteGameInterface.offer(this.playerName, message1, junk, tokens.remove(0)));
		    }
		    break;

        case "O_REPLY":

        case "OFFER_REPLY:":

            if (tokens.isEmpty()){
                System.err.println("You need to provide a response. Type 'accept' or 'reject'");
            }
            String response = tokens.remove(0).toLowerCase();
            if ( !(response.equals("accept") || response.equals("reject")) ){ 
                System.err.println("Your response must be to accept or reject");
            }
            else{
                if(response.equals("accept")){
                    System.out.println(remoteGameInterface.offerReply(this.playerName, true));
                }
                else if(response.equals("reject")){
                    System.out.println(remoteGameInterface.offerReply(this.playerName, false));
                }
            }
            break;

		case "EXAMINE":
			if(tokens.isEmpty())
			{
				System.err.println("You need to provide a item to look at");
			}
			else if(tokens.size() != 1)
			{
				System.err.println("Only one item can be examined at a time.");
			}
			else
			{
				System.out.println(remoteGameInterface.examine(this.playerName, tokens.remove(0)));
			}
			break;
                case "PICKUP":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide an object to pickup.");
                    }
                    else {
                        String itemName = tokens.remove(0);
                        if(tokens.isEmpty())
                        {
                            System.out.println(remoteGameInterface.pickup(this.playerName, itemName));
                        }
                        else
                        {
                            String numberOfItemsString = tokens.remove(0);
                            if(IsNumber(numberOfItemsString))
                            {
                                int numberOfItems = Integer.parseInt(numberOfItemsString);
                                System.out.println(remoteGameInterface.pickup(this.playerName,itemName,numberOfItems));
                            }
                            else
                            {
                                System.out.println("third parameter must be a number");
                            }
                            System.out.println();
                        }
                    }
                    break;
                case "INVENTORY":
                    System.out.println(remoteGameInterface.inventory(this.playerName));
                    break; 
                case "VENMO": // Team 4: Alaqeel
                    System.out.println(remoteGameInterface.venmo(this.playerName, tokens));
                    break;   
                case "SHOP":
                	int shopId = remoteGameInterface.shop(this.playerName); 
                	if (shopId != -1) {
                		System.out.println("You enter the shop");
                		new ShopClient(this.playerName, shopId, remoteGameInterface);
                        remoteGameInterface.shopLeft(this.playerName);
                	}
                	else {
                		System.out.println("There is no shop here");
                	}
                	break;
                case "BANK":
                	int bankId = remoteGameInterface.bank(this.playerName); 
                	if (bankId != -1) {
                		new BankClient(this.playerName, remoteGameInterface);
                		System.out.println(remoteGameInterface.look(this.playerName));  // When player leaves print look screen
                	}
                	else {
                		System.out.println("There is no bank here");
                	}
                case "WALLET":
                    System.out.println(remoteGameInterface.wallet(this.playerName));
                    break;               
        case "R_TRADE":
                    if(tokens.isEmpty()) {
                            System.err.println("You need to provide the name of the player that you want to trade with");
                    }
                    else{
                        remoteGameInterface.requestPlayer(this.playerName, tokens.remove(0));
                    }
                    break;

        case "A_TRADE":
                    if(tokens.isEmpty()) {
                            System.err.println("You need to provide the name of the player you are accepting");
                    }
                    else{
                        System.out.println(remoteGameInterface.playerResponse(this.playerName, tokens.remove(0)));
                    }
            break;
                case "POKE_GHOUL":
                    System.out.println(remoteGameInterface.pokeGhoul(this.playerName));
                    break;
                case "BRIBE_GHOUL":
                    if(tokens.isEmpty()){
                        System.err.println("You need to provide an item to give Ghoul.");
                    }else{
                        System.out.println(remoteGameInterface.bribeGhoul(this.playerName, tokens.remove(0)));
            }
        break;
		case "FIGHT":
				if (tokens.isEmpty()) {
					System.err.println("You need to provide either rock, paper or scissors");
				} else {
					String options = tokens.remove(0);
					options = options.toUpperCase();
					if (options.equals("ROCK") || options.equals("PAPER") || options.equals("SCISSORS"))
						System.out.println(remoteGameInterface.ghoulRPS(this.playerName, options));
					else
						System.out.println("Invalid option.");
				}
				break;
		case "CAPTURE":
				System.out.println(remoteGameInterface.capture(this.playerName));
				break;
		case "SPIRIT_LIST_ALL":
				System.out.println(remoteGameInterface.spiritListAll(this.playerName));
				break;
		case "SPIRIT_LIST_MISSING":
				System.out.println(remoteGameInterface.spiritListMissing(this.playerName));
				break;
        case "DROP":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide an object to drop.");
                    }
                    else {
                        System.out.println(remoteGameInterface.drop(this.playerName, tokens.remove(0)));
                    }
                    break;
                case "WHITEBOARD":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide an argument to the WHITEBOARD command.");
                    }
                    else {
                        switch(tokens.remove(0).toUpperCase()) {
                            case "ERASE":
                                System.out.println(remoteGameInterface.whiteboardErase(this.playerName));
                                break;
                            case "READ":
                                System.out.println(remoteGameInterface.whiteboardRead(this.playerName));
                                break;
                            case "WRITE":
                                if (tokens.isEmpty()) { 
                                    System.err.println("You need to provide an argument to the WHITEBOARD WRITE command");
                                }
                                else {
                                    System.out.println(remoteGameInterface.whiteboardWrite(this.playerName, tokens.remove(0)));
                                }
                                break;
                            default:
                                System.err.println("Invalid argument provided to WHITEBOARD command.");
                                break;
                        }
                    }
                    break;        
		case "SORT":
	            InputStreamReader keyReader = new InputStreamReader(System.in);
        	    BufferedReader keyInput = new BufferedReader(keyReader);
		    boolean validInput = true;
		    String mode = "";
		    try {
		    	while(validInput) {
				System.out.println("Sort by name, weight, or price? (n/w/p)");
	                	String option1 = keyInput.readLine();
        	        	System.out.println("Increasing or decreasing order? (i/d)");
                		String option2 = keyInput.readLine();

                		option1.toLowerCase();
                		option2.toLowerCase();

        	        	mode = option1 + option2;
	
                		switch(mode) {
                        		case "ni":
                                		validInput = false;
                                		break;
                        		case "nd":
                                		validInput = false;
                                		break;
                        		case "wi":
                                		validInput = false;
                                		break;
                        		case "wd":
                                		validInput = false;
                                		break;
                        		case "pi":
                                		validInput = false;
                                		break;
                        		case "pd":
                                		validInput = false;
                                		break;
                        		default:
                	                	System.out.println("Please enter in valid input or use the correct format (n/w/p) -> (i/d)");	
			    		}
			   }
	    	    }
		    catch(IOException e) {
 	                   System.err.println("[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
       	 	           System.exit(-1);		
		    }	    
		    System.out.println(remoteGameInterface.sort(this.playerName, mode));
		    break;		    
                case "QUIT":
		    remoteGameInterface.removePlayerTitle(this.playerName);
                    remoteGameInterface.leave(this.playerName);
                    runListener = false;
                    break;
                case "DELETE":
                     remoteGameInterface.deleteAccount(this.playerName);
                     runListener = false;
                     break;
                case "HELP":
                    String categoryName = null;
                    String commandName = null;
                    if (!tokens.isEmpty())
                    {
                        categoryName = tokens.remove(0).toUpperCase();
                    }

                    if (!tokens.isEmpty())
                    {
                        commandName = tokens.remove(0).toUpperCase();
                    }
                    showCommand(categoryName, commandName);
                    break;
                case "ADDCOMMAND":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide a custom command to add.");
                    } else {
                        String customCommand = tokens.remove(0).toUpperCase();
                        addCustomCommand(customCommand);
                    }
                    break;
                case "REMOVECOMMAND":
                    removeCustomCommand();
                    break;
                case "CUSTOMHELP":
                    showCustomCommands();
                    break;
		case "MAP":
                    System.out.println(remoteGameInterface.showMap(this.playerName));
		    break;
                case "CHALLENGE":
                    if(tokens.isEmpty()){
                      System.err.println("You need to provide a name and number of rounds.");
                    }
                    else{
                        if(tokens.size() < 2){
                          System.err.println("You need to provide the number of rounds.");
                        }
                        else{

                            String option1 = tokens.remove(0);
                            String option2 = tokens.remove(0);
                            //System.out.println(option1 + " \t\t" + option2);
                            System.out.println(remoteGameInterface.challenge(this.playerName, option1, option2));//tokens.remove(0), tokens.remove(0)));
                        }
                    }
                    break;
                case "ACCEPT":
                    if(tokens.isEmpty()){
                      System.err.println("You need to provide a name and number of rounds.");
                    }
                    else{
                        if(tokens.size() < 2){
                          System.err.println("You need to provide the number of rounds.");
                        }
                        else{

                            String option1 = tokens.remove(0);
                            String option2 = tokens.remove(0);
                            //System.out.println(option1 + " \t\t" + option2);
                            System.out.println(remoteGameInterface.accept(this.playerName, option1, option2));//tokens.remove(0), tokens.remove(0)));
                        }
                    }
                    break;
                case "REJECT":
                    if(tokens.isEmpty()){
                      System.err.println("You need to provide a name.");
                    }
                    else{
                      System.out.println(remoteGameInterface.reject(this.playerName, tokens.remove(0)));
                    }
                    break;
                case "PICK":
                    if(tokens.isEmpty()){
                      System.err.println("You need to provide either rock, paper or scissors");
                    }
                    else{
                      String options = tokens.remove(0);
                      options = options.toUpperCase();
                      if(options.equals("ROCK") || options.equals("PAPER") || options.equals("SCISSORS"))
                        System.out.println(remoteGameInterface.pickRPS(this.playerName, options));
                      else
                        System.out.println("Invalid option.");
                    }
                    break;
                case "TEACH":
                    System.out.println(remoteGameInterface.teach(this.playerName));
                    break;
	        case "ACCOUNT":
		    try {
			accountEditWizard.enter();
		    } catch (Exception e) {
			System.out.println("It appears the wizards wand broke. Probably a Weasley...");
		    }
		    break;
		case "TOGGLERPSCHAT":
		    System.out.println(remoteGameInterface.toggleRPSChat(this.playerName));
		    break;
		case "LISTPLAYERS":
		    System.out.println(remoteGameInterface.listAllPlayers());
		    break;
		case "RANK TOP5":
		    System.out.println(remoteGameInterface.rankings(this.playerName,"top5"));
		    break;
		case "RANK":
		    if(tokens.isEmpty()){
			System.err.println("You need to provide a username");
		    }
		    else{
			System.out.println(remoteGameInterface.rankings(this.playerName, tokens.remove(0)));
		    }
		    break;
                case "FRIENDS":
                    String sub;
                    if(tokens.isEmpty())
                        sub = "";
                    else
                        sub = tokens.remove(0).toUpperCase();

                    if (sub.isEmpty()){
                            System.out.println("\nProvide FRIENDS subcommand");
                            System.out.println("  ADD name     - Adds a player to your friends list");
                            System.out.println("  ALL          - List all your friends");
                            System.out.println("  REMOVE name  - Removes a player from your friends list");
                            System.out.println("  ONLINE       - Lists your friends who are currently online");
                            break;
                    }

                    String name;
                    if(tokens.isEmpty())
                        name = "";
                    else
                        name = tokens.remove(0);
                    
                    while (!tokens.isEmpty())
                            name += " " + tokens.remove(0);

                    switch (sub) {
                    case "ONLINE":
                        System.out.println(remoteGameInterface.viewFriends(this.playerName, true));
                        break;
                    case "ALL":
                        System.out.println(remoteGameInterface.viewFriends(this.playerName, false));
                        break;        
                    case "ADD":
                        if(name.isEmpty()){
                            System.out.println("You need to provide the name of the player you want to add");
                            break;
                        }

                            switch (remoteGameInterface.addFriend(this.playerName, name)) {
                            case INTERNAL_SERVER_ERROR:
                                    System.out.println("There was an internal server error with your account");
                                    break;
                            case NOT_FOUND:
                                    System.out.println("The player you were trying to add could not be found");
                                    break;
                            case EXISTS:
                                    System.out.println("You already have this friend added");
                                    break;
                            case SILLY:
                                    System.out.println("Sorry, you cannot add yourself as a friend. Go make some");
                                    break;
                            case SUCCESS:
                                    System.out.println(name + " was added to your friends list!");
                                    break;
                            default:
                                    System.out.println("Unknown server behavior");
                            }
                            break;
                    case "REMOVE":
                        if(name.isEmpty()){
                            System.out.println("You need to provide the name of the player you want to remove");
                            break;
                        }
                            switch (remoteGameInterface.removeFriend(this.playerName, name)) {
                            case INTERNAL_SERVER_ERROR:
                                    System.out.println("There was an internal server error with your account");
                                    break;
                            case NOT_FOUND:
                                    System.out.println("This player was not on your friends list");
                                    break;
                            case SILLY:
                                    System.out.println("Removing yourself? Who am I to judge");
                                    break;
                            case SUCCESS:
                                    System.out.println(name + " was removed from your friends list");
                                    break;
                            default:
                                    System.out.println("Unknown server behavior");
                            }
                            break;
                    }
                    break;
                default:
                	if (remoteGameInterface.checkChat(command)) {
                        if (tokens.isEmpty()) {
                            System.err.println("You need to provide a message.");
                        }
                        else {
                            message = parseMessage(tokens);
                            System.out.println(remoteGameInterface.messageChat(this.playerName, message, command));
                        }
                	}
                    //If command does not match with any, see if it is custom command
                	else if (!executeCustomCommand(command, tokens)) {
                        System.out.println("Invalid Command, Enter \"help\" to get help");
                    }
                    break;
            }
            if(!commandCheck.equals("REDO")) {
                this.lastCommand = commandCheck;
            }
        } catch (RemoteException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Prompts the user through a dialogue tree to give them the option to reset their password
	 * @return password
     * @throws IOException 
     */
    public String resetPassword(BufferedReader stdin)  {
    	System.out.println("Reset password?");
    	String temp;
    	for(;;) {
    		System.out.print("(Y/N)> ");
    		try {
    		temp = stdin.readLine().toUpperCase().trim();
    		}catch(Exception e) {
    			System.out.println("Sorry, there was an error reading user input");
    			return null;
    		}
    		switch(temp) {
    		case"Y":
    			break;
    		case "N":
    			return null;
    		default:
    			continue;
    		}
    		break;
    	}
    	try {
	    	if(remoteGameInterface.isPlayerOnline(this.playerName)) {
	    		System.out.println("This account is currently logged in");
	    		return null;
	    	}
    	} catch(RemoteException e) {
    		System.out.println("Remote exception checking to see if this account is already logged in. Will not reset password.");
    		return null;
    	}
    	DataResponse<ArrayList<String>> questions;
    	try {
    		questions = remoteGameInterface.getQuestions(this.playerName);
			if(!questions.success()) {
				switch(questions.error) {
				case NOT_FOUND:
					System.out.println("This username was not found");
					break;
				case INTERNAL_SERVER_ERROR:
					System.out.println("Sorry, it appears the server is experiencing difficulties");
					break;
				default:
				}
				return null;
			}
    	} catch(RemoteException e) {
    		System.out.println("There was a problem contacting the server to get the reset quetsions");
    		return null;
    	}
    	if (questions.data.size()==0) {
    		System.out.println("You have no reset questions");
    		return null;
    	}
    	ArrayList<String> answers = new ArrayList<String>(questions.data.size());
    	for(String question : questions.data) {
    		System.out.println(question);
    		System.out.print("> ");
			answers.add(new String(System.console().readPassword()));
		}
    	System.out.print("New password: ");
    	String password = new String(System.console().readPassword());
     	try {
			Responses resp = remoteGameInterface.changePassword(this.playerName, password);
			switch(resp) {
			case NOT_FOUND:
				System.out.println("Somehow your account couldnt be found");
				break;
			case INTERNAL_SERVER_ERROR:
				System.out.println("The server experienced an error changing your password");
				break;
			case SUCCESS:
				System.out.println("Password changed!");
				return password;
			default:
				System.out.println("Unexpected server behavior");
			}
    	} catch (RemoteException e) {
			System.out.println("There was an error contacting the server");
			return null;
		}
    	System.out.println();
    	return null;
    }
    
    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("[SHUTDOWN] .. This program requires one argument. Run as java -Djava.security.policy=game.policy GameClient hostname");
            System.exit(-1);
        }
        
        System.out.println("[STARTUP] Game Client Now Starting...");
        new GameClient(args[0]);
    }

    /*If no parameter has been given for showCommand, pass in null to showCommand.
     *This will cause showCommand to print every commands available in game
     */
    private void showCommand()
    {
        showCommand(null, null);
    }

    //Shows every command available in game
    private void showCommand(String commandCategory, String commandToShow)
    {
        try {
            File commandFile = new File("./help.xml");
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(commandFile);

            document.getDocumentElement().normalize();
            NodeList xmlCategory = document.getElementsByTagName("help_category");
            NodeList xmlCommands;

            String category;
            String description;
            String commandName;
            Element xmlElement;
            Element xmlCommandElement;

            System.out.println("The game allows you to use the following commands:");

            //Get every command category from xml file first
            for (int i = 0; i < xmlCategory.getLength(); i++) {
                xmlElement = (Element) xmlCategory.item(i);

                category = xmlElement.getAttribute("category");
                
                //Print every catgory if no category given or only specific given category
                if (commandCategory == null || category.equals(commandCategory))
                {
                    System.out.println("In category " + category);

                    xmlCommands = xmlElement.getElementsByTagName("help");
                    //Then get every command from each category
                    for (int j = 0; j < xmlCommands.getLength(); j++)
                    {
                        xmlCommandElement = (Element) xmlCommands.item(j);
                        commandName = xmlCommandElement.getAttribute("command");

                        if (commandToShow == null || commandName.equals(commandToShow))
                        {
                            description = xmlCommandElement.getElementsByTagName("description").item(0).getTextContent();
                            System.out.println(description);
                        }

                        if (commandToShow != null)
                        {
                            break;
                        }
                    }

                    System.out.println("");
                    if (commandCategory != null)
                    {
                        break;
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Shows the introduction of the game
    private void showIntroduction()
    {
        try {
            File commandFile = new File("./help.xml");
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(commandFile);

            document.getDocumentElement().normalize();
            NodeList xmlCommands = document.getElementsByTagName("introduction");

            String description;
            Element xmlElement;

            xmlElement = (Element) xmlCommands.item(0);
            description = xmlElement.getElementsByTagName("description").item(0).getTextContent();
            System.out.println(description);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Adds a custom command to the custom command file. 
     * Gets command value from the user on the command line.
     * @param customCommandName The name of the custom command to add.
     */
    private void addCustomCommand(String customCommandName)
    {
        InputStreamReader keyboardReader = new InputStreamReader(System.in);
        BufferedReader keyboardInput = new BufferedReader(keyboardReader);
        String commandToExecute;

        try {
            File customCommandFile = new File("./CommandShortcut.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document;
            Element root;

            //If no xml file CommandShortcut exists, create one
            if (!customCommandFile.exists())
            {
                document = dBuilder.newDocument();

                root = document.createElement("CustomCommand");
                document.appendChild(root);
            //Else use already existing xml file CommandShortcut
            } else {
                document = dBuilder.parse(customCommandFile);

                NodeList rootElements = document.getElementsByTagName("CustomCommand");
                root = (Element) rootElements.item(0);
            }

            NodeList customCommands = document.getElementsByTagName("Command");

            Element cCommand;

            //Get every custom commands to check if user is trying to use already existing custom command name
            for (int i = 0; i < customCommands.getLength(); i++) {
                cCommand = (Element) customCommands.item(i);

                if (customCommandName.equals(cCommand.getAttribute("name"))) {
                    System.out.println("There already exists a custom command with a name " + customCommandName);
                    return;
                }
            }

            //Get command to bind to custom command
            System.out.println("Enter the command you wish to bind to " + customCommandName + ":");
            commandToExecute = keyboardInput.readLine();

            //Add elements to xml file
            Element command = document.createElement("Command");
            root.appendChild(command);

            Attr attr = document.createAttribute("name");
            attr.setValue(customCommandName);
            command.setAttributeNode(attr);

            Element bindCommand = document.createElement("CommandToExecute");
            bindCommand.appendChild(document.createTextNode(commandToExecute));
            command.appendChild(bindCommand);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("./CommandShortcut.xml"));

            //Add indentation to xml file for readability
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(domSource, streamResult);

            System.out.println("Command " + customCommandName + " has been added.");

        } catch (ParserConfigurationException | TransformerException | SAXException | IOException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Executes custom command listed in custom command file
     * @param commandName Name of custom command previously bound and stored in custom command file.
     * @param parameters ArrayList of parameters for the custom command
     * @return true if custom command was executed succesfully, false otherwise
     */
    private boolean executeCustomCommand(String commandName, ArrayList<String> parameters) {
        try {
            File customCommandFile = new File("./CommandShortcut.xml");

            //If no custom command exists yet, return false
            if (!customCommandFile.exists())
            {
                return false;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(customCommandFile);

            NodeList customCommands = document.getElementsByTagName("Command");

            Element cCommand;

            //Check if custom command with commandName exists
            for (int i = 0; i < customCommands.getLength(); i++) {
                cCommand = (Element) customCommands.item(i);

                //If there is a custom command, use command binded to perform next aciton
                if (commandName.equals(cCommand.getAttribute("name").toUpperCase())) {
                    String bindCommand = cCommand.getElementsByTagName("CommandToExecute").item(0).getTextContent();
                    for (int j = 0; j < parameters.size(); j++)
                    {
                        bindCommand += " " + parameters.get(j).toUpperCase();
                    }

                    parseInput(bindCommand);
                    return true;
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Removes custom command. The name of the command to remove
     * is is read from the command line.
     */
    private void removeCustomCommand() {
        try {
            File customCommandFile = new File("./CommandShortcut.xml");

            if (!showCustomCommands()) {
                return;
            }

            InputStreamReader keyboardReader = new InputStreamReader(System.in);
            BufferedReader keyboardInput = new BufferedReader(keyboardReader);
            String commandName;

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(customCommandFile);

            NodeList customCommands = document.getElementsByTagName("Command");

            Element cCommand;

            System.out.println("\nEnter the custom command to delete:");
            commandName = keyboardInput.readLine().toUpperCase();

            //Check if custom command with commandName exists
            for (int i = 0; i < customCommands.getLength(); i++) {
                cCommand = (Element) customCommands.item(i);

                //If there is a custom command, remove that command
                if (commandName.equals(cCommand.getAttribute("name").toUpperCase())) {
                    cCommand.getParentNode().removeChild(cCommand);
                    System.out.println("Custom command " + commandName + " has been deleted");
                    break;
                }

                if (i == (customCommands.getLength() - 1)) {
                    System.out.println("No custom command " + commandName + " was found");
                }
            }

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File("./CommandShortcut.xml"));

            transformer.transform(domSource, streamResult);

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Lists custom commands from custom command file
     * @return true if custom commands were listed succesfully, false otherwise
     */
    private boolean showCustomCommands()
    {
        try {
            File customCommandFile = new File("./CommandShortcut.xml");

            //If no custom command exists yet, return false
            if (!customCommandFile.exists())
            {
                System.out.println("There is no custom command configured");
                return false;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(customCommandFile);

            NodeList customCommands = document.getElementsByTagName("Command");

            if (customCommands.getLength() == 0)
            {
                System.out.println("There is no custom command configured");
                return false;
            }

            System.out.println("Here are the list of custom commands currently available:");

            Element cCommand;

            //Show every custom commands
            for (int i = 0; i < customCommands.getLength(); i++) {
                cCommand = (Element) customCommands.item(i);

                System.out.println(cCommand.getAttribute("name").toUpperCase() + " - " + cCommand.getElementsByTagName("CommandToExecute").item(0).getTextContent());
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    /**
     * Inner class to handle remote message input to this program.  
     *  - Runs as a separate thread.  Interrupt it to kill it.
     *  - Spawns multiple threads, one for each remote connection.
     */
    public class ReplyRemote implements Runnable {
        private String host;
        
        public ReplyRemote(String host) {
            this.host = host;
        }
        
        @Override
        public void run() {
            // This thread is interruptable, which will allow it to clean up before
            
            // Attempt communcations with the server.
            try (Socket remoteMessageSocket = new Socket(host, 13500)) {
                
                // Get stream reader and writer. 
                //  Writer is only used once, to register this socket with a player.
                //  Otherwise, this is read only to receive non-locally generated event notifications.
                BufferedReader remoteReader = new BufferedReader(new InputStreamReader(remoteMessageSocket.getInputStream()));
                PrintWriter remoteWriter = new PrintWriter(remoteMessageSocket.getOutputStream(), true);
                
                // Register the socket with the player.
                remoteWriter.println(GameClient.this.playerName);
                remoteReader.readLine();

                // As long as this program is running, print all messages directly to output.
                String message;
                while(runListener == true) {
                    message = remoteReader.readLine();
                    if(message == null||message.equals("!SHUTDOWN")) {
                        System.err.println("The remote server has closed its connection!  Shutting down.");
                        System.exit(-1);
                    }
                    System.out.println(message);
                }                
            
                // Close the socket
                remoteMessageSocket.close();
            } catch(ConnectException ex) {
                System.err.println("[FAILURE] The connection has been refused.");
                System.err.println("          As this communication is critical, terminating the process.");
                System.exit(-1);
            } catch (IOException ex) {
                Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
    }    



    // Begin Feature 409 Word Filter

    /**
     * Reads a list of words from file, adds them to this player's list of words filtered from chat.
     *
     */
    private void readWordFilterFile() {

        HashSet<String> words = new HashSet<String>();
        String filename = "FilteredWordsList-" + playerName + ".txt";

        try {
            File filteredWordsFile = new File(filename);
            if(!filteredWordsFile.exists()) { filteredWordsFile.createNewFile(); }
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();

            while (line != null) {
                String word = line.toLowerCase();
                words.add(word);
                line = br.readLine();
            }

            remoteGameInterface.setPlayerFilteredWords(this.playerName, words);
            br.close();

        } catch(IOException i) {
            System.err.print("\nI/O Exception thrown while attempting to read from filtered words File!\n");
        }
    }

    private static boolean IsNumber(String Number)
    {
        return Number.chars().allMatch(Character::isDigit);
    }

    //End Feature 409 Word Filter

    //Begin 413 Prefix
    private void readPrefixFromFile() {
        String filename = "ChatPrefixFile-" + playerName + ".txt";

        try {
            File filteredWordsFile = new File(filename);
            if(!filteredWordsFile.exists()) { filteredWordsFile.createNewFile(); }
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();

            if(line != null && !line.equals("")) { remoteGameInterface.setPlayerChatPrefix(this.playerName, line); }
            br.close();

        } catch(IOException i) {
            System.err.print("\nI/O Exception thrown while attempting to read from filtered words File!\n");
        }
    }



    //End 413 Prefix
}
