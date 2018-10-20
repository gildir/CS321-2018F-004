//import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Random;

/**
 * @author Team 4: Alaqeel
 *
 */
public class Venmo {
	
	private HashMap<String, Transaction> transactions;
	
	public Venmo() {
		transactions = new HashMap<String, Transaction>();
	}
	
	public String send(Player from, Player to, float amount) {
		// TODO: 
		// javadocs 
		// fix transaction ID
		
		if (from.getName() == to.getName()) return "You can't Venmo yourself";
		if (amount <= 0) return "Please enter a number that is higher that 0";
		if (from.getMoney() < amount) return "You don't have enough money to complete the transaction.";
		
		String TranID;
		
		// source: https://www.baeldung.com/java-random-string
		int leftLimit = 97; // letter 'a'
	    int rightLimit = 122; // letter 'z'
	    int targetStringLength = 10;
	    Random random = new Random();
	    StringBuilder buffer = new StringBuilder(targetStringLength);
	    for (int i = 0; i < targetStringLength; i++) {
	        int randomLimitedInt = leftLimit + (int) 
	          (random.nextFloat() * (rightLimit - leftLimit + 1));
	        buffer.append((char) randomLimitedInt);
	    }
	    TranID = buffer.toString();
	    
	    float rounded = (float) (Math.round(amount * 100.0) /100.0);
	    from.changeMoney(-rounded);
	    
	    Transaction tran = new Transaction(from, to, rounded);
	    String format = "Hey %s!!\n"
	    		+ "%s sent you $%.2f.\n"
	    		+ "The transaction ID is: %s\n"
	    		+ "To accept, type: venmo accept [transaction ID]\n"
	    		+ "To reject, type: venmo reject [transaction ID]";
	    to.getReplyWriter().println(String.format(format, to.getName(), from.getName(), rounded, TranID));
		
	    transactions.put(TranID, tran);
		
		
		return String.format("You just Venmoed %s $%.2f. Transaction ID: %s", to.getName(), rounded, TranID);
	}
	
	public String accept(Player to, String TranID) {
		Transaction tran = transactions.get(TranID);
		
		if (tran == null || tran.to != to) return "You are not authorized to accept this transaction.";
		
		float amount = tran.amount;
		to.changeMoney(amount);
		
		Player from = tran.from;
		
		String format = "**Venmo update**\n"
				+ "%s has accepted your transaction of $%.2f.";
		
		from.getReplyWriter().println(String.format(format, to.getName(), amount));
		
		transactions.remove(TranID);
		
		format = "Awesome! $%.2f are now added to your account.\n"
				+ "You now have: $.2f\n"
				+ "Go get yourself something nice.";
		return String.format(format, amount, to.getMoney());
	}
	
	public String reject(Player to, String TranID) {
		Transaction tran = transactions.get(TranID);
		
		if (tran == null || tran.to != to) return "You are not authorized to accept this transaction.";

		float amount = tran.amount;
		Player from = tran.from;
		from.changeMoney(amount);
		String format = "**Venmo update**\n"
				+ "%s has rejected your transaction of $%.2f."
				+ "The money is now added back to your account.\n"
				+ "You now have: $%.2f";
		from.getReplyWriter().println(String.format(format, to.getName(), amount, from.getMoney()));
		transactions.remove(TranID);
		
		return String.format("Transaction rejected. $%.2f are now back at %s's account", amount, from.getName());
	}
	
	public static String instructions() {
		return    "1- To send money, type: venmo send [recepient] [amount]\n"
				+ "2- To accept a money transfer, type: venmo accept [transaction ID]\n"
				+ "3- To reject a money transfer, type: venmo reject [transaction ID]";
	}
	
	private class Transaction {
		public Player from;
		public Player to;
		public float amount;
		
		public Transaction(Player from, Player to, float amount) {
			this.from = from;
			this.to = to;
			this.amount = amount;
		}
	}

}
