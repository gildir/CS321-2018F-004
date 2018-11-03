import java.util.HashMap;
/**
 * Bank stores a collection of ledgers in the following schema
 * { 
 * 		name: {
 * 			money: Integer, 
 * 			lastCheckin: long
 *		}
 *	}
 * @author Group 4 : King
 * 
 */

public class Bank {
	private HashMap<String, HashMap<String, Long>> ledger;
	
	public Bank() {
		this.ledger = new HashMap<String, HashMap<String, Long>>();
	}
	
	/**
	 * This decodes the commands given to the bank and sends them to 
	 * their appropriate method. It must always return a String 
	 * 
	 * @author Group 4 : King
	 * @param cmd_id : 	the ID coresponding to a particular bank method. 
	 * 					Mapped to a user friendly string in BankClient for 
	 * 					ease of reference
	 * @param name	:	The name of the user interacting with the Bank
	 * @param cmd	: 	Any extra arguments that may be needed (may be Null)
	 * @return		:  	A string denoting information about the method that was
	 * 					just run's success
	 */
	public String command(String cmd, String name, String args) {
		switch (cmd) {
			case "toString":	
				return this.toString(name);
			case "addPlayer":
				return this.addPlayer(name, args);
			case "deposit":
				return this.deposit(name, args);
			case "withdraw":
				return this.withdraw(name, args);
			case "printAccount":
				return this.printAccount(name);
				
		}
		return "";
	}
	
	private String toString(String name) {
		return "";
	}
	
	private String addPlayer(String name, String args) {
		return "";
	}
	
	private String deposit(String name, String args) {
		return "";
	}
	
	private String withdraw(String name, String args) {
		return "";
	}
	
	private String printAccount(String name) {
		return "";
	}
}
