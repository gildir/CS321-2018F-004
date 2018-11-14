import java.util.HashMap;
import java.util.Random;

/**
 * Venmo. The new and improved way to send money to other players.
 * This class employs the singleton design pattern.
 * 
 * @author Team 4: Alaqeel
 *
 */
public class Venmo {

    /**
     * The singleton instance of the Venmo class.
     */
    private static Venmo venmo = new Venmo();

    /**
     * The active transactions ledger.
     */
    private HashMap<String, Transaction> transactions;

    /**
     * A private constructor.
     * It creates a new transactions ledger.
     */
    private Venmo() {
        transactions = new HashMap<String, Transaction>();
    }

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
    public static String send(Player from, Player to, double amount) {
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
        // Adds the amount to the recipient's wallet.
        to.changeMoney(rounded);

        // Displays a message to the recipient
        to.getReplyWriter().println(String.format("Hooray! %s sent you $%.2f.\n", from.getName(), rounded));

        // logging the transaction
        System.out.printf("[Venmo] %s sent %s $%.2f\n", from.getName(), to.getName(), rounded);

        // Generates and returns a transaction summary
        return String.format("You just Venmoed %s $%.2f.", to.getName(), rounded);
    }

    /**
     * Creates a new transaction and informs the recipient of it.
     * 
     * @param from The sender
     * @param to The recipient
     * @param amount The amount to be sent
     * @return A transaction summary if it was valid. An error message otherwise.
     */
    public static String offer(Player from, Player to, double amount) {
        // Checks if the player is trying to send to themselves
        if (from.getName() == to.getName()) return "You can't Venmo yourself";

        // Rounds the amount to the nearest two decimal places
        double rounded = Math.round(amount * 100.0) /100.0;

        // Checks the following errors:
        // 1- if the amount is negative
        // 2- if the player is poor 
        if (rounded <= 0) return "Please enter a number that is higher that 0";
        if (from.getMoney() < rounded) return "You don't have enough money to complete the transaction.";

        // Creates a transaction ID
        String TranID;

        // source: https://www.baeldung.com/java-random-string
        // generates a random alphanumeric string of a specific length
        // modified by (Team 4: Alaqeel) to be alphanumeric
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

        // Takes the amount out of the sender's account
        from.changeMoney(-rounded);

        // Creates a new transaction and adds it to the ledger
        venmo.transactions.put(TranID, new Transaction(from, to, rounded, TranID));

        // The formatter of the message to be displayed to the recipient
        String format = "Hey %s!!\n"
                + "%s offered you $%.2f.\n"
                + "The transaction ID is: %s\n"
                + "To accept, type: venmo accept %s\n"
                + "To reject, type: venmo reject %s";

        // Displays a message to the user
        to.getReplyWriter().println(String.format(format, to.getName(), from.getName(), rounded, TranID, TranID, TranID));

        // logging the offer
        System.out.printf("[Venmo] %s - %s offered %s $%.2f\n", TranID, from.getName(), to.getName(), rounded);

        // Generates and returns a transaction summary
        return String.format("You just offered %s $%.2f. Transaction ID: %s", to.getName(), rounded, TranID);
    }

    /**
     * Allows the recipient of a transaction to accept it.
     * When successful, informs the sender, and returns a summary message to the recipient.
     * 
     * @param to The recipient player. Must be non-null.
     * @param TranID The transaction ID.
     * @return Transaction summary, if successful. Error message, otherwise.
     */
    public static String accept(Player to, String TranID) {
        // retrieves the transaction ID
        Transaction tran = venmo.transactions.get(TranID);

        // Returns an error message if the transaction ID is incorrect, or if the called isn't the recipients.
        if (tran == null || tran.to != to) return "You are not authorized to accept this offer.";

        // adds the money to the recipient's wallet.
        double amount = tran.amount;
        to.changeMoney(amount);

        Player from = tran.from;

        // Generates and displays an update message to the sender.
        String format = "**Venmo update**\n"
                + "%s has accepted your offer of $%.2f.";
        from.getReplyWriter().println(String.format(format, to.getName(), amount));

        // removes the transaction from the ledger
        venmo.transactions.remove(TranID);

        // logging the offer
        System.out.printf("[Venmo] %s - %s accepted %s's offer of $%.2f\n", TranID, to.getName(), from.getName(), amount);

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
    public static String reject(Player to, String TranID) {
        // retrieves the transaction ID
        Transaction tran = venmo.transactions.get(TranID);

        // Returns an error message if the transaction ID is incorrect, or if the called isn't the recipients.
        if (tran == null || tran.to != to) return "You are not authorized to accept this transaction.";

        // adds the money to the sender's wallet.
        double amount = tran.amount;
        Player from = tran.from;
        from.changeMoney(amount);

        // Generates and displays an update message to the sender.
        String format = "**Venmo update**\n"
                + "%s has rejected your transaction of $%.2f."
                + "The money is now back in your wallet.\n"
                + "You now have: $%.2f";
        from.getReplyWriter().println(String.format(format, to.getName(), amount, from.getMoney()));
        venmo.transactions.remove(TranID);
        
        // logging the offer
        System.out.printf("[Venmo] %s - %s rejected %s's offer of $%.2f\n", TranID, to.getName(), from.getName(), amount);

        // Generates and returns a summary message to the recipient.
        return String.format("Transaction rejected. $%.2f is now back in %s's wallet", amount, from.getName());
    }

    /**
     * Returns a string that contains all the pending transactions a user have.
     * Each transaction will be in a separate line, and will have:
     * <ol>
     *  <li>The sender</li>
     *  <li>The Amount</li>
     *  <li>The Transaction ID</li>
     * </ol>
     * 
     * @param to The recipient.
     * @return The list of transactions, or a nice message if no pending offers.
     */
    public static String list(Player p) {
        StringBuilder result = new StringBuilder(), out = new StringBuilder(), in = new StringBuilder();
        String informat = "%d- From: %s, Amount: $%.2f, Transaction ID: %s\n";
        String outformat = "%d- To: %s, Amount: $%.2f, Transaction ID: %s\n";
        int i = 0, o = 0;
        // Iterates through the transactions ledger and find matches
        // if a match is found, parse the log and concatenate it with the String
        for (Transaction t : venmo.transactions.values()){
            if (t.to == p)  // to find transactions where the caller is the recipient.
                in.append(String.format(informat, ++i, t.from.getName(), t.amount, t.id));
            else if (t.from == p) // to find transactions where the caller is the sender.
                out.append(String.format(outformat, ++o, t.to.getName(), t.amount, t.id));
        }
        result.append("Incoming Offers:\n" + (in.length() == 0? " None\n" : in));
        result.append("Outgoing Offers:\n" + (out.length() == 0? " None\n" : out));

        return result.toString();
    }

    /**
     * @return Instructions on how to use Venmo.
     */
    public static String instructions() {
        return    "1- To send money, type: venmo send [recipient] [amount]\n"
                + "2- To offer money, type: venmo offer [recipient] [amount]\n"
                + "2- To accept a money transfer, type: venmo accept [transaction ID]\n"
                + "3- To reject a money transfer, type: venmo reject [transaction ID]\n"
                + "4- To list your pending transactions, type: venmo list";
    }

    /**
     * A representation of the transactions.
     * 
     * @author Team 4: Alaqeel
     *
     */
    private static class Transaction {
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
        public double amount;
        /**
         * Transaction ID
         */
        public String id;

        /**
         * Creates a new transaction object
         * 
         * @param from Sender player
         * @param to Recipient player
         * @param amount Amount of transaction
         */
        public Transaction(Player from, Player to, double amount, String id) {
            this.from = from;
            this.to = to;
            this.amount = amount;
            this.id = id;
        }
    }
}
