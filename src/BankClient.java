import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Client-side class to interact with Bank object
 * @author Team 4 : King
 *
 */
public class BankClient {
	private String name;
	private GameObjectInterface remote;
	private boolean inBank;
	
	/**
	 * Constructor for client-side bank object
	 * @param name The player's name
	 * @param r
	 */
	public BankClient(String name, GameObjectInterface r) {
		this.name = name;
		this.remote = r;
		this.inBank = true;
		
		run();
	}
	
	private void run() {
		this.printMenu();
		
		InputStreamReader keyboardReader = new InputStreamReader(System.in);
        BufferedReader keyboardInput = new BufferedReader(keyboardReader);
        String keyboardStatement;
		
        // Ripped from GameClient
        while(this.inBank) {
            try {
                keyboardStatement = keyboardInput.readLine();
                this.parseInput(keyboardStatement);
            } catch (IOException ex) {
                System.err.println("[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
                System.exit(-1);
            }
        }
	}
	
	private void printMenu() {
		String ln = "+-----------------------------------------+\n";
		System.out.println(
				ln +
				"|  Welcome to the Patriot Center Bank!    |\n" +
				"|  (Formerly the Eagle Arena Bank)        |\n" +
				ln +
				"|  Commands:                              |\n" +
				ln + 
				"|  VIEW  -     View your account balances |\n" +
				ln + 
				"|  DEPOSIT X - Deposits $X or any number  |\n" +
				"|              that you input             |\n" +
				ln + 
				"|  WITHDRAW X - Withdraws $X, or any      |\n" +
				"|               number that you input     |\n" +
				ln +
				"|  LEAVE  -    Exit the bank and return   |\n" +
				"|              to the game                |\n" +
				ln +
				"|  HELP   -    Display this menu          |\n" +
				ln
		);
	}
	
	private void parseInput(String input) throws RemoteException {
		// Ripped from GameClient
		StringTokenizer commandTokens = new StringTokenizer(input);
        ArrayList<String> tokens = new ArrayList<>();
        while(commandTokens.hasMoreTokens() == true) {
            tokens.add(commandTokens.nextToken());
        }

        if(tokens.isEmpty()) {
            System.out.println("The keyboard input had no commands.");
            return;
        }

        switch(tokens.remove(0).toUpperCase()) {
        	case "V":
        	case "VIEW":
        		System.out.println(remote.bankCmdRunner("printAccount", name, ""));
        		break;
        		
        	case "D":
        	case "DEPOSIT":
            	if (tokens.isEmpty()) {
            		System.out.println("You need to provide an amount to deposit!");
            	}
            	else {
            		System.out.println(remote.bankCmdRunner("deposit", name, tokens.remove(0)));
            	} 
            	break;
        	
        	case "W":
        	case "WITHDRAW":
        	case "WITHDRAWL": // I always spell it wrong, sue me
            	if (tokens.isEmpty()) {
            		System.out.println("You need to provide an amount to withdraw!");
            	}
            	else {
            		System.out.println(remote.bankCmdRunner("withdraw", name, tokens.remove(0)));
            	} 
            	break;
            	
        	case "Q":
        	case "QUIT":
        	case "L":
        	case "LEAVE":
        		System.out.println("You exit the bank");
        		this.inBank = false;
        		break;
        	
        	case "H":
        	case "HELP":
        		this.printMenu();
        		break;
        		
        	default:
        		System.out.println("I'm sorry, I didn't understand your command, type HELP to see a menu");
        }
	}
}
