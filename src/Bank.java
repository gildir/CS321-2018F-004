import java.util.HashMap;
/**
 * Bank stores a collection of ledgers in the following schema
 * { 
 * 		name: {
 * 			money: Double, 
 * 			lastCheckin: Long
 *		}
 *	}
 * @author Group 4 : King
 * 
 */

public class Bank {
	private HashMap<String, HashMap<String, Object>> ledger;
	private String lastCheckin = "lastCheckin", money = "money"; // Keys used in the ledger
	private static long fiveMins = 300000;
	
	public Bank() {
		this.ledger = new HashMap<String, HashMap<String, Object>>();
	}
	
	/**
	 * Determines if a player may withdraw an amount of money
	 * @param name The player's name
	 * @param value The amount they wish to withdraw
	 * @return True if they have enough money, false otherwise
	 */
	public boolean canWithdraw(String name, double value) {
		if (this.getMoney(name) >= value) {
			return true;
		} 
		return false;
	}
	
	/**
	 * Adds money to the player's bank account 
	 * @param name The name of the player
	 * @param value The amount of money they are depositing
	 * @return The player's updated account balance
	 */
	public double deposit(String name, double value) {
		if (!ledger.containsKey(name)) {
			addPlayer(name);
		}
		updateInterest(name);
		
		HashMap<String, Object> account = ledger.get(name);
		Double update = (Double) account.get(money) + (Double) value; // Don't use getMoney here so interest isn't calc'd again
		account.put(money, update);
		
		return (double) account.get(money);
	}
	
	/**
	 * Withdraws an amount of money from the players account 
	 * Assumes you have already checked canWithdraw() 
	 * @param name The name of the player
	 * @param value The amount they are withdrawing
	 * @return
	 */
	public String withdraw(String name, double value) {
		HashMap<String, Object> account = ledger.get(name);
		account.put(money, (Double) (getMoney(name)-value));
		
		return String.format("New account balance: $%.2f", getMoney(name));
	}
	
	/**
	 * Returns a string representation of the account balance and any interest accrued
	 * @param name The player's name
	 * @return A string representation of the player's bank account
	 */
	public String printAccount(String name) {
		if (!ledger.containsKey(name)) {
			addPlayer(name);
			return "Looks like it's your first time banking with us!\nWe took the liberty of making you an account";
		}
		
		double interest = updateInterest(name);
		double balance = getMoney(name);
		
		String ln = "+--------------------------+\n";
		String formatStr = 
				ln + "|      Bank Statement:     |\n" + 
				ln + "| Balance: $%14.2f |\n" + 
				ln + "| Interest accrued: $%5.2f |\n" +
				ln;
		
		return String.format(formatStr, balance, interest);
	}
	
	/**
	 * Calculates %0.01 interest accruing every 5 mins on a players account
	 * @param name The name of the player
	 * @return The amount of interest accrued (not the balance)
	 */
	private double updateInterest(String name) {
		HashMap<String, Object> account = ledger.get(name);
		
		double interest;
		double principal = getMoney(name);
		
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - (long) account.get(lastCheckin);
		
		int incriments = (int) (timePassed/fiveMins); // Truncates down
		
		if (incriments > 0) {
			// A = P(1 + r)^n
			interest = principal * (Math.pow((1 + 0.0001), incriments));
		
			// Update ledger
			account.put(money, interest);
			long updated = (Long) account.get(lastCheckin) + (incriments*fiveMins);
			account.put(lastCheckin, updated);
			
			return interest - principal;
		}
		return 0;
	}
	
	/**
	 * Helper method to return a player's money
	 * @param name
	 * @return
	 */
	private double getMoney(String name) {
		return (double) ledger.get(name).get(money);
	}
	
	/**
	 * If a player doesn't have an account, it automatically makes a new one for them with a $0 balance
	 * @param name The name of the player
	 */
	private void addPlayer(String name) {
		HashMap<String, Object> newAcct = new HashMap<>();
		
		newAcct.put(money, new Double(0));								// Initialize account to 0
		newAcct.put(lastCheckin, new Long(System.currentTimeMillis()));	//Initialize last update to now
		
		ledger.put(name, newAcct);
	}
	
}
