/**
 * Venmo. The new and improved way to send money to other players.
 * 
 * @author Team 4: Alaqeel
 *
 */
public class Venmo {
	
	/**
	 * Creates a new transaction and informs the recipient of it.
	 * The amount is rounded to the nearest two decimal places.
	 * If the transaction is successful, a message is sent to the recipient.
	 * A transaction summary (or error message) is returned to the sender.
	 * 
	 * @param from The sender. Must be non null.
	 * @param to The recipient. Must be non null.
	 * @param amount The amount to be sent.
	 * @return A transaction summary if it was valid. An error message otherwise.
	 */
	public static String send(Player from, Player to, float amount) {
		// Checks if the player is trying to send to themselves
		if (from.getName() == to.getName()) return "You can't Venmo yourself";
		
		// Rounds the amount to the nearest two decimal places
		double rounded = Math.round(amount * 100.0) /100.0;
		
		// Checks the following errors:
		// 1- if the amount is negative
		// 2- if the player is poor 
		if (rounded <= 0) return "Please enter a number that is higher that 0";
		if (from.getMoney() < rounded) return "You don't have enough money to complete the transaction.";

		// Takes the amount out of the sender's wallet.
		from.changeMoney(-rounded);
		// Adds the amount to the receipient's wallet.
		to.changeMoney(rounded);
		
		// Displays a message to the recipient
		to.getReplyWriter().println(String.format("Hooray! %s sent you $%.2f.\n", from.getName(), rounded));
		
		// logging the transaction
		System.out.printf("[Venmo] %s send %s $%.2f\n", from.getName(), to.getName(), rounded);
		
		// Generates and returns a transaction summary
		return String.format("You just Venmoed %s $%.2f.", to.getName(), rounded);
	}


	/**
	 * @return Instructions on how to use Venmo.
	 */
	public static String instructions() {
		return "To send money, type: venmo send [recepient] [amount]\n";
	}
}
