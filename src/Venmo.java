//import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Random;

/**
 * Venmo. The new and improved way to send money to other players.
 * 
 * The class work as an escrow account.
 * When a player makes a transaction request, the money is taken from his wallet and put on escrow.
 * When the recipient accepts the transaction, the money is taken from escrow and put in their wallet.
 * When the recipient rejects the transaction, the money is taken from escrow and put back in the sender's wallet.
 * 
 * @author Team 4: Alaqeel
 *
 */
public class Venmo {

	/**
	 * The active transactions ledger.
	 */
	private HashMap<String, Transaction> transactions;

	/**
	 * Initializes Venmo and creates a new ledger.  
	 */
	public Venmo() {
		transactions = new HashMap<String, Transaction>();
	}

	/**
	 * Creates a new transaction and informs the recipient of it.
	 * 
	 * @param from The sender
	 * @param to The recipient
	 * @param amount The amount to be sent
	 * @return A transaction summary if it was valid. An error message otherwise.
	 */
	public String send(Player from, Player to, float amount) {
		// Checks the following errors:
		// 1- if the player is sending to themselves
		// 2- if the amount is negative
		// 3- if the player is poor
		if (from.getName() == to.getName()) return "You can't Venmo yourself";
		if (amount <= 0) return "Please enter a number that is higher that 0";
		if (from.getMoney() < amount) return "You don't have enough money to complete the transaction.";

		// Creates a transaction ID
		String TranID;
		
		// source: https://www.baeldung.com/java-random-string - modified by Team 4: Alaqeel
		// generates a random alphanumeric string of specific length
		int[] leftLimit = {(int) 'A', (int) 'a', (int) '0' }; 
		int[] rightLimit = {(int) 'Z', (int) 'z', (int) '9' };
		int len = 10; // the length of the random string
		Random random = new Random();
		StringBuilder buffer = new StringBuilder(len); // using a mutable object to save space
		int randomLimitedInt;
		for (int i = 0; i < len; i++) {
			int ran = random.nextInt(3);
			randomLimitedInt = leftLimit[ran] + (int) 
					(random.nextFloat() * (rightLimit[ran] - leftLimit[ran] + 1));
			buffer.append((char) randomLimitedInt);
		}
		// stores the random string in the Transaction ID
		TranID = buffer.toString();

		// Rounds the amount to two decimal places
		float rounded = (float) (Math.round(amount * 100.0) /100.0);

		// Takes the amount out of the sender's account
		from.changeMoney(-rounded);

		// Creates a new transaction and adds it to the ledger
		transactions.put(TranID, new Transaction(from, to, rounded));
		
		// The formatter of the message to be displayed to the recipient
		String format = "Hey %s!!\n"
				+ "%s sent you $%.2f.\n"
				+ "The transaction ID is: %s\n"
				+ "To accept, type: venmo accept %s\n"
				+ "To reject, type: venmo reject %s";
		
		// Displays a message to the user
		to.getReplyWriter().println(String.format(format, to.getName(), from.getName(), rounded, TranID, TranID, TranID));
		
		// Generates and returns a transaction summary
		return String.format("You just Venmoed %s $%.2f. Transaction ID: %s", to.getName(), rounded, TranID);
	}

	/**
	 * Allows the recipient of a transaction to accept it.
	 * When successful, informs the sender, and returns a summary message to the recipient.
	 * 
	 * @param to The recipient player. Must be non-null.
	 * @param TranID The transaction ID.
	 * @return Transaction summary, if successful. Error message, otherwise.
	 */
	public String accept(Player to, String TranID) {
		// retrieves the transaction ID
		Transaction tran = transactions.get(TranID);
		
		// Returns an error message if the transaction ID is incorrect, or if the called isn't the recipients.
		if (tran == null || tran.to != to) return "You are not authorized to accept this transaction.";
		
		// adds the money to the recipient's wallet.
		float amount = tran.amount;
		to.changeMoney(amount);
		
		Player from = tran.from;
		
		// Generates and displays an update message to the sender.
		String format = "**Venmo update**\n"
				+ "%s has accepted your transaction of $%.2f.";
		from.getReplyWriter().println(String.format(format, to.getName(), amount));
		
		// removes the transaction from the ledger
		transactions.remove(TranID);
		
		// Generates and returns a summary message to the recipient.
		format = "Awesome! $%.2f are now added to your wallet.\n"
				+ "You now have: $%.2f\n"
				+ "Go get yourself something nice.";
		return String.format(format, amount, to.getMoney());
	}

	/**
	 * Allows the recipient of a transaction to reject it.
	 * When successful, informs the sender, and returns a summary message to the recipient.
	 * 
	 * @param to The recipient player. Must be non-null.
	 * @param TranID The transaction ID.
	 * @return Transaction summary, if successful. Error message, otherwise.
	 */
	public String reject(Player to, String TranID) {
		// retrieves the transaction ID
		Transaction tran = transactions.get(TranID);
				
		// Returns an error message if the transaction ID is incorrect, or if the called isn't the recipients.
		if (tran == null || tran.to != to) return "You are not authorized to accept this transaction.";
				
		// adds the money to the sender's wallet.
		float amount = tran.amount;
		Player from = tran.from;
		from.changeMoney(amount);
		
		// Generates and displays an update message to the sender.
		String format = "**Venmo update**\n"
				+ "%s has rejected your transaction of $%.2f."
				+ "The money is now added back to your wallet.\n"
				+ "You now have: $%.2f";
		from.getReplyWriter().println(String.format(format, to.getName(), amount, from.getMoney()));
		transactions.remove(TranID);
		
		// Generates and returns a summary message to the recipient.
		return String.format("Transaction rejected. $%.2f are now back at %s's wallet", amount, from.getName());
	}

	/**
	 * @return Instructions on how to use Venmo.
	 */
	public static String instructions() {
		return    "1- To send money, type: venmo send [recepient] [amount]\n"
				+ "2- To accept a money transfer, type: venmo accept [transaction ID]\n"
				+ "3- To reject a money transfer, type: venmo reject [transaction ID]";
	}

	/**
	 * A representation of the transactions.
	 * 
	 * @author Team 4: Alaqeel
	 *
	 */
	private class Transaction {
		/**
		 * Sender
		 */
		public Player from;
		/**
		 * Recipient
		 */
		public Player to;
		/**
		 * Amount
		 */
		public float amount;

		/**
		 * Creates a new transaction object
		 * 
		 * @param from Sender player
		 * @param to Recipient player
		 * @param amount Amount of transaction
		 */
		public Transaction(Player from, Player to, float amount) {
			this.from = from;
			this.to = to;
			this.amount = amount;
		}
	}

}
