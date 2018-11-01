/**
 * Keeps track of items in the shop on the 
 */

//import java.util.Iterator;
import java.util.LinkedList;
public class Shop
{
	//Max of 10 items in this list
	private LinkedList<Item> inventory;
	
	//List of objects that want to be indemand for the shop
	private LinkedList<Item> inDemand;
	
	// List of players in this shop
	private PlayerList playerlist;

	private String description;

	private String title;
	
	
	public Shop(String name, String desc)
	{
		this.inventory = new LinkedList<Item>();
		this.inDemand = new LinkedList<Item>();
		this.playerlist = new PlayerList();
		this.description = desc;
		this.title = name;
	}
	
	//get method to get inventory linkedlist
	public LinkedList<Item> getInven()
	{
		return this.inventory;
	}
	//used to add methods to the linked list
	public void add(Item k) {
		this.inventory.add(k);
	}
	
	//used to remove items form the linked list
	public void remove(Object k)
	{
		this.inventory.remove(k);
	}
	//prints the inventory of the shop class
	public void printInv() {}
	
	//Prints the list of object in demand
	public void printDem() {}
	
	public void addPlayer(Player p) {
		playerlist.addPlayer(p);
	}	
	
	public void removePlayer(Player p) {
		// Why does add take a player object and remove take a name?? --IK
		playerlist.removePlayer(p.getName()); 
	}

	/**
	 * @author Team 4: Alaqeel
	 * @return The tag line of the shop
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * @author Team 4: Alaqeel
	 * @return The shop name
	 */
	public String getTitle() {
        return this.title;
    }
	
	public String toString() {
        
		// white spaces around the billboard
		String billboard = "Welcome to " + this.getTitle(); 
		
		// shop header
		String result = ".-----------------------------------.\n";
        result += 		"|"+ strCenter(billboard, 35) + "|\n";
        result += 		".-----------------------------------.\n";
        result += strCenter(this.getDescription(), 37) + "\n";
        
        result += "\n";
        
        // catalog
        result += this.getObjects();
        
        // players names
//        String players = this.getPlayers();
//        if (players.length() > 1) result += "You are here by yourself.\n";
//        else {
//        	result += "You are here along with:\n";
//        	result += players;
//        }
//        result += players;
        
        result += "\n";
        result += "Players in the area: " + this.getPlayers() + "\n";

        result += "How can we help you?\n";
        return result;
    }
	
	/**
	 * @author Team 4: Alaqeel
	 * 
	 * returns a list of the players, separated by comma and using the Oxford comma.
	 * 
	 * @param players
	 * @return list of players
	 */
	public String getPlayers() {
		String result = "";
		
		int i = 0;
		for (Player p : this.playerlist) {
			result += String.format("%s, ", p.toString());
			i++;
		}
		
		// removes the comma at the end of the line
		if (i > 0) result = result.substring(0, result.length()-2);
		
		// fixes the oxford comma
		if (i > 1) {
			String temp = result.substring(0, result.lastIndexOf(", "));
			temp += " and ";
			temp += result.substring(result.lastIndexOf(", ")+2);
			return temp + "\n";
		}
		return result + "\n";
		
	}
	
	
	/**
	 * @author Team 4: Alaqeel
	 * 
	 * Iterates through the list of the objects and creates a table populated with object names and prices.
	 * @return table of the objects
	 */
	public String getObjects() {
		
		if (inventory.size() == 0) {
			return "We usually have a huge catalog.\n"
					+ "Unfortunately, we are currently out of stock.\n"
					+ "Please come again soon!";
		}
		
		int itemLen = 15, countLen = 2, f1 = 3, f2 = 2, priceField = f1 + f2 + 2;
		int menuWidth = itemLen + countLen + f1 + f2 + 6 + 2; // 6 = column padding, 2 = currency + decimal point
		
		// String formats for consistency
		String format = "%-" + countLen +"s | %-" + itemLen + "s | $%-" + f1 + "." + f2 + "f\n";
		String headerFormat = "%-" + countLen +"s | %-" + itemLen + "s | %-" + priceField + "s\n";
		
		// generates menu separator
		String separator = "";
		for (int s = 0; s < menuWidth; s++) separator += "-";
		
		
		// menu header
		String menu = "We sell:\n";
		menu += "...................\n";
		menu += String.format(headerFormat, "#", "Item", "Price");
		
		menu += separator + "\n";
		
		// adding menu items
		int i = 1;
		for (Item obj : inventory) {
			double price = obj.price; // TODO: replace with price getter
			
			if (this.inDemand.contains(obj)) price *= 1.2; // change price according to demand list
			String item = obj.toString();
			
			// handles items with long names
			if (item.length() > itemLen) {
				menu += String.format(format, i++, item.substring(0,itemLen), price);
				for (int j = 1; j <= item.length() % 15; j--) {
					menu += String.format(format, "", item.substring((itemLen*j)+1 ,itemLen*(j+1)), "");
				}
			}
			// names that aren't long
			else menu += String.format(format, i++, item, price);
		}
		
		menu += separator + "\n";
		
		return menu;
	}
	
	/**
	 * @author Team 4: Alaqeel
	 * Centers a string of text in a provided column length
	 * @param str String to be centered
	 * @param len Column length
	 * @return A string of width len with str centered in it
	 */
	private String strCenter(String str, int len) {
		String result = str;
		// spaces before
        int i = (len - result.length()) / 2;
        for (; i > 0; i--) result = " " + result;
        // spaces after
        i = len - result.length();
        for (; i > 0; i--) result = result + " ";
        
        return result;
	}
}
