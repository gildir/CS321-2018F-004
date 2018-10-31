 

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;

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
    
    /** 
     * Main class for running the game client.
     */
    public GameClient(String host) {
        this.runGame = true;
        boolean nameSat = false;

        
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

            // Start by remotely executing the joinGame method.  
            //   Lets the player choose a name and checks it with the server.  If the name is
            //    already taken or the user doesn't like their input, they can choose again.
            while(nameSat == false) {
                try {
                    System.out.println("Please enter a name for your player.");
                    System.out.print("> ");
                    this.playerName = keyboardInput.readLine();
                    System.out.println("Welcome, " + this.playerName + ". Are you sure you want to use this name?");
                    System.out.print("(Y/N) >");
                    if(keyboardInput.readLine().equalsIgnoreCase("Y")) {
                        // Attempt to join the server
                        if(remoteGameInterface.joinGame(this.playerName) == false) {
                            System.out.println("I'm sorry, " + this.playerName + ", but someone else is already logged in with your name. Please pick another.");
                        }
                        else {
                            nameSat = true;
                        }
                    }
                } catch (IOException ex) {
                    System.err.println("[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
                    System.exit(-1);
                }
            }

            // Player has joined, now start up the remote socket.
            this.runListener = true;
            remoteOutputThread = new Thread(new GameClient.ReplyRemote(host));
            remoteOutputThread.setDaemon(true);
            remoteOutputThread.start();

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
        }        
    }
    
    /** 
     * Simple method to parse the local input and remotely execute the RMI commands.
     * @param input 
     */
    private void parseInput(String input) {
        boolean reply;
        
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
        //for redo old messages
        String command = input.toUpperCase();

        try {
            switch(tokens.remove(0).toUpperCase()) {

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
                case "HELP":
                showCommand();
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
                case "MOVE":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide a distance in order to move.");
                    }
                    else {
                        System.out.println(remoteGameInterface.move(this.playerName, Integer.parseInt(tokens.remove(0))));
                    }
                    break;
                case "REDO":
                    if(lastCommand==null)
                    {
                        System.out.println("No command to redo");
                        break;
                    }
                    parseInput(lastCommand);
                    break;

		
		case "O":
		    
		case "OFFER":

		    if (tokens.isEmpty()){
			System.err.println("You need to provide a player to offer.");
		    }
		    else if (tokens.size() < 2) { 
			System.err.println("You need to provide an item to offer.");
		    }
		    else {
			String dstPlayerName = tokens.remove(0).toLowerCase();
			System.out.println(remoteGameInterface.offer(this.playerName, dstPlayerName, tokens.remove(0)));
		    }
		    break;

                case "PICKUP":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide an object to pickup.");
                    }
                    else {
                        System.out.println(remoteGameInterface.pickup(this.playerName, tokens.remove(0)));
                    }
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
		case "DROP":
                    if(tokens.isEmpty()) {
                        System.err.println("You need to provide an object to drop.");
                    }
                    else {
                        System.out.println(remoteGameInterface.drop(this.playerName, tokens.remove(0)));
                    }
                    break;
                case "INVENTORY":
                    System.out.println(remoteGameInterface.inventory(this.playerName));
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
                    remoteGameInterface.leave(this.playerName);
                    runListener = false;
                    break;
                default:
                    System.out.println("Invalid Command, Enter \"help\" to get help");
                    break;
            }
            if(!command.equals("REDO")) {
                this.lastCommand = command;
            }
        } catch (RemoteException ex) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        showCommand(null);
    }

    //Shows every command available in game
    private void showCommand(String commandToShow)
    {
        try {
            File commandFile = new File("./help.xml");
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(commandFile);

            document.getDocumentElement().normalize();
            NodeList xmlCommands = document.getElementsByTagName("help");

            String description;
            Element xmlElement;

            System.out.println("The game allows you to use the following commands:");

            //Get every commands from xml file and print them
            for (int i = 0; i < xmlCommands.getLength(); i++) {
                xmlElement = (Element) xmlCommands.item(i);

                description = xmlElement.getElementsByTagName("description").item(0).getTextContent();

                if ( !description.equals("") ){
                    System.out.println(description);
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
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                    if(message == null) {
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
    
}
