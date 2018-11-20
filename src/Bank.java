import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	private static String log_filename = "bank.csv";
	
	public Bank() {
		this.ledger = new HashMap<String, HashMap<String, Object>>();
		
		// Read in the log_file
		try {
			BufferedReader br = new BufferedReader(new FileReader(log_filename));
			String line;
			
			// Reads in each line, and loads it into the ledger
			// Note, the file has no header row. Doesn't seem necessary because it
			// Should never be touched by human hands...
			while((line = br.readLine()) != null) {	
				String tokens[] = line.split(",");
				
				String n = tokens[0];						// Name
				Double m = Double.parseDouble(tokens[1]); 	// Money in acct.
				Long lc = Long.parseLong(tokens[2]);		// Last time interest was accrued
				
				this.loadPlayer(n, m, lc);
			}
			br.close();
		}
		
		catch (FileNotFoundException e) {
			try { 
				new File(log_filename).createNewFile();
			}
			catch (IOException ioe) {
				// I can't conceive of this failing, but if it does.. I dunno. Cry?
			}
		}
		catch (IOException e) {
			// This should really never happen. Java doesn't believe me
		}
	
		
	}
	
	/**
	 * Currently run any time a change is made (deposit/withdrawal). In practice, this would take an unreasonably long time. 
	 * This is not scalable at all, but that doesn't really matter right now. 
	 * @return True if successful save
	 */
	private boolean saveBank() {
		try {
			File f = new File(log_filename);
			f.createNewFile();
			FileWriter fw = new FileWriter(f);
			String line, m, lc;	// The line to write and the Double and Long values held in the internal HashMap
			
			for (String key : this.ledger.keySet()) {
				line = key + ",";
				line += String.valueOf(getMoney(key)) + ",";
				line += String.valueOf(getLastCheckin(key)) + "\n";
				
				fw.write(line);
			}
			fw.close();
			return true;
		} 
		catch (IOException e) { // Something has gone horribly wrong
			return false; 
		}
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
		
		this.saveBank();
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
		
		this.saveBank();
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
			
			this.saveBank();
			return interest - principal;
		}
		return 0;
	}
	
	/**
	 * Helper method to return a player's money
	 * @param name
	 * @return player's money
	 */
	private double getMoney(String name) {
		return (double) ledger.get(name).get(money);
	}
	
	/**
	 * Helper method to return the last time a player checked their interest
	 * @param name
	 * @return last unix time interest was compiled
	 */
	private long getLastCheckin(String name) {
		return (long) ledger.get(name).get(lastCheckin);
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
	
	private void loadPlayer(String name, Double m, Long lc) {
		HashMap<String, Object> newAcct = new HashMap<>();
		
		newAcct.put(money, m);
		newAcct.put(lastCheckin, lc);
		
		ledger.put(name, newAcct);
	}
	
}
