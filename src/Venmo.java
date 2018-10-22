/**
 * Venmo. The new and improved way to send money to other players.
 * 
 * @author Team 4: Alaqeel
 *
 */
public class Venmo {
	
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

		// Rounds the amount to two decimal places
		float rounded = (float) (Math.round(amount * 100.0) /100.0);

		// Takes the amount out of the sender's wallet.
		from.changeMoney(-rounded);
		// Adds the amount to the receipient's wallet.
		to.changeMoney(rounded);
		
		// Displays a message to the user
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
