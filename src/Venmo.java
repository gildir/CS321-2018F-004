import java.util.HashMap;
import java.util.HashSet;
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
     * The pending transactions ledger (in the mail).
     */
    private HashMap<String, HashSet<Mail>> mailbox;    
    private PlayerAccountManager PAM;
    private PlayerList playerList;

    /**
     * A private constructor.
     * It creates a new transactions ledger.
     */
    private Venmo() {
        transactions = new HashMap<String, Transaction>();
        mailbox = new HashMap<String, HashSet<Mail>>();
    }
    
    public static void setup(PlayerAccountManager p, PlayerList l) {
        if (venmo.PAM == null) venmo.PAM = p;
        if (venmo.playerList == null) venmo.playerList = l;
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
     * @param to The recipient. It has to be a correct player name.
     * @param amount The amount to be sent
     * @return A transaction summary if it was valid. An error message otherwise.
     */
    public static String mail(Player from, String to, double amount) {
        // Checks if the player is trying to send to themselves
        if (from.getName().equals(to)) return "You can't Venmo yourself";

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
        venmo.transactions.put(TranID, new Transaction(from.getName(), to, rounded, TranID));

        // The formatter of the message to be displayed to the recipient
        String format;
        
        String message;
        
        Player pTo = venmo.playerList.findPlayer(to);
        // if the player is online
        if (pTo != null) {
            format = "Hey %s!!\n"
                    + "%s offered you $%.2f.\n"
                    + "The transaction ID is: %s\n"
                    + "To accept, type: venmo accept %s\n"
                    + "To reject, type: venmo reject %s";
            message = String.format(format, to, from.getName(), rounded, TranID, TranID, TranID);
            pTo.getReplyWriter().println(message);
        }
        else {
            if (venmo.mailbox.get(to) == null) venmo.mailbox.put(to, new HashSet<Mail>());
            format = "%s mailed you $%.2f. The transaction ID is: %s";
            message = String.format(format, to, from.getName(), rounded, TranID);
            venmo.mailbox.get(to).add(new Mail(message, 0));
        }

        // logging the offer
        System.out.printf("[Venmo] %s - %s mailed %s $%.2f\n", TranID, from.getName(), to, rounded);

        // Generates and returns a transaction summary
        return String.format("You just mailed %s $%.2f. Transaction ID: %s", to, rounded, TranID);
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
        if (tran == null || !tran.to.equals(to.getName())) return "You are not authorized to accept this offer.";

        // adds the money to the recipient's wallet.
        double amount = tran.amount;
        to.changeMoney(amount);

        // Generates and displays an update message to the sender.
        String format;
        String message;
        
        Player from = venmo.playerList.findPlayer(tran.from);
        if (from != null) {
            format = "**Venmo update**\n"
                    + "%s has accepted your offer of $%.2f.";
            message = String.format(format, to.getName(), amount);
            from.getReplyWriter().println(message);
        }
        else {
            if (venmo.mailbox.get(tran.from) == null) venmo.mailbox.put(tran.from, new HashSet<Mail>());
            format = "%s has accepted your offer of $%.2f.";
            message = String.format(format, to.getName(), amount);
            venmo.mailbox.get(tran.from).add(new Mail(message, 0));
        }
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
        if (tran == null || !tran.to.equals(to.getName())) return "You are not authorized to accept this transaction.";

        // adds the money to the sender's wallet.
        double amount = tran.amount;        

        // Generates and displays an update message to the sender.
        String format;
        String message;
       
        Player from = venmo.playerList.findPlayer(tran.from);
        if (from != null) {
            from.changeMoney(amount);
            format = "**Venmo update**\n"
                    + "%s has rejected your transaction of $%.2f."
                    + "The money is now back in your wallet.\n"
                    + "You now have: $%.2f";  
            message = String.format(format, to.getName(), amount, from.getMoney());
            
            from.getReplyWriter().printf("%s\nYou now have: $%.2f", message, from.getMoney() );
        }
        else {
            if (venmo.mailbox.get(tran.from) == null) venmo.mailbox.put(tran.from, new HashSet<Mail>());
            format = "%s has rejected your transaction of $%.2f. The money is now back in your wallet."; 
            message = String.format(format, to.getName(), amount);
            venmo.mailbox.get(tran.from).add(new Mail(message, amount));
        }
        
        venmo.transactions.remove(TranID);
        
        // logging the offer
        System.out.printf("[Venmo] %s - %s rejected %s's mail of $%.2f\n", TranID, to.getName(), from.getName(), amount);

        // Generates and returns a summary message to the recipient.
        return String.format("Transaction rejected. $%.2f is now going back to %s.", amount, from.getName());
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
            if (t.to.equals(p.getName()))  // to find transactions where the caller is the recipient.
                in.append(String.format(informat, ++i, t.from, t.amount, t.id));
            else if (t.from.equals(p.getName())) // to find transactions where the caller is the sender.
                out.append(String.format(outformat, ++o, t.to, t.amount, t.id));
        }
        result.append("Incoming Offers:\n" + (in.length() == 0? " None\n" : in));
        result.append("Outgoing Offers:\n" + (out.length() == 0? " None\n" : out));

        return result.toString();
    }
    
    public static void checkMail(String name) {
        // if the mailbox is empty, return
        if (!venmo.mailbox.containsKey(name)) return;
        // get player object
        Player player = venmo.playerList.findPlayer(name);
        
        // String to hold the mail
        StringBuilder messages = new StringBuilder();
        messages.append("Venmo mailbox updates:\n");
        // Wallet for the rejected money
        double amount = 0;
        // to count the mail
        int i = 0;
        // retrieve mail and sort it
        for (Mail m : venmo.mailbox.get(name)) {
            messages.append(i + "- " + m.message + "\n");
            amount += m.amount;
        }
        // give the player their money back
        if (amount > 0) player.changeMoney(amount);
        // give the player their messages
        player.getReplyWriter().println(messages);
        
        // destroy mailbox
        venmo.mailbox.remove(name);
    }

    /**
     * @return Instructions on how to use Venmo.
     */
    public static String instructions() {
        return    "1- To send money, type: venmo send [recipient] [amount]\n"
                + "2- To mail money, type: venmo mail [recipient] [amount]\n"
                + "2- To accept a mailed transaction, type: venmo accept [transaction ID]\n"
                + "3- To reject a mailed transaction, type: venmo reject [transaction ID]\n"
                + "4- To check your Venmo mailbox, type: venmo mailbox";
    }

    /**
     * A type to hold the transactions.
     * 
     * @author Team 4: Alaqeel
     *
     */
    private static class Transaction {
        /**
         * Sender
         */
        public String from;
        /**
         * Recipient
         */
        public String to;
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
        public Transaction(String from, String to, double amount, String id) {
            this.from = from;
            this.to = to;
            this.amount = amount;
            this.id = id;
        }
    }
    
    /**
     * A type to hold the mail
     * 
     * @author Team4: Alaqeel
     *
     */
    private static class Mail {
        public String message;
        public double amount;
        
        public Mail(String m, double a) {
            this.message = m;
            this.amount = a;
        }
    }
}
