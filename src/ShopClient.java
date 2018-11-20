import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Lets player interact with the shop. Many to 1 relationship with Shop class
 * @author Team 4: King/Keesling
 *
 */
public class ShopClient {
	private String player;
	private Integer id;
	private GameObjectInterface remote;
	private boolean shopping;
	
	public ShopClient(String p, int s, GameObjectInterface remote) throws RemoteException {
		this.player = p;
		this.id = new Integer(s);
		this.remote = remote;
		this.shopping = true;
		run();
	}
	
	/**
	 * @author Team 4: King
	 * Parses user input to call functions. This mostly sets things up for the helper methods
	 * @throws RemoteException
	 */
	private void run() throws RemoteException {
		// Display shop specific greeting
		System.out.println(remote.getShopStr(this.id));

        // Copy and pasted from GameClient
        // Set up for keyboard input for local commands.
        InputStreamReader keyboardReader = new InputStreamReader(System.in);
        BufferedReader keyboardInput = new BufferedReader(keyboardReader);
        String keyboardStatement;

        while(this.shopping) {
            try {
                keyboardStatement = keyboardInput.readLine();
                this.parseInput(keyboardStatement);
            } catch (IOException ex) {
                System.err.println("[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
                System.exit(-1);
            }
        }
	}
	
	/**
	 * Parses user input while they're shopping
	 * @param input The string the user just typed
	 * @throws RemoteException 
	 */
	private void parseInput(String input) throws RemoteException {
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

        switch(tokens.remove(0).toUpperCase()) {
        	case "B":
            case "BUY":
            	if (tokens.isEmpty()) {
            		System.out.println("You need to provide an item to buy");
            		break;
            	} 
            	else {
            		this.buy(tokens.remove(0));
            		break;
            	}
            	
            case "S":
            case "SELL":
            	if (tokens.isEmpty()) {
            		System.out.println("Please provide an item to sell");
            		break;
            	}
            	else {
            		this.sell(tokens.remove(0));
            		break;
            	}
            	
            case "I":
            case "INV":
            case "INVENTORY":
            	System.out.println(this.getInv());
            	break;
            	
            case "D":
            case "DEM":
            case "DEMAND":
            	System.out.println(this.getDemInv());
            	break;
            	
            case "Q":
            case "QUIT":
            case "L":
            case "LEAVE":
            	this.shopping = false;
            	System.out.println("You leave the shop.");
            	return;	// Don't print instructions after you leave the store
            case "H":
            case "HELP":
                this.printMenu();
                break;
            default:
                System.out.println("Type HELP to see what you can do in this shop.");
        }
	}
	
	public void printMenu() {
		String ln =         "+----------------------------------+\n";
        System.out.printf(
                        ln                                           +
                            "| BUY [item] - - - Buy an item     |\n" +
                        ln                                           +
                            "| SELL [item]  - - Sell an item    |\n" +
                        ln                                           +
                            "| INVENTORY  - - - View the shop's |\n" +
                            "|                  goods           |\n" +
                        ln                                           +
                            "| DEMAND - - - - - View what items |\n" +
                            "|                  are in demand   |\n" +
                        ln                                           +
                            "| LEAVE  - - - - - Leave the shop  |\n" +
                        ln                                           +
                            "| HELP - - - - - - Show this menu  |\n" +
                        ln
        );
	}
	
	//In terms of the player buying items
	public void buy(String item) throws RemoteException {
		System.out.println(remote.buyItem(this.player, this.id, item));
	}
	
	//In terms of the player selling items
	public void sell(String item) throws RemoteException {
		double val = remote.sellItem(this.player, this.id, item);
		if (val != 0) {
			System.out.printf("Here's $%.2f for your %s.\n", val, item);
		}
		else {
			System.out.println("Hey! I don't see no " + item + ". You can't fool me!");
		}
	}
	
	public String getInv() throws RemoteException {
		return remote.getShopInv(this.id);
	}

    public String getDemInv() throws RemoteException {
        return remote.getShopDemInv(this.id);
    }
}
