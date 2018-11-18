/**
 * Keeps track of items in the shop on the 
 */

//import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;
import java.io.File;
import java.util.Random;

public class Shop
{
	//Max of 10 items in this list
	private LinkedList<Item> inventory;
	
	//List of objects that want to be in demand for the shop
	private LinkedList<Item> inDemand;
	
	// List of players in this shop
	private PlayerList playerlist;

	// List of all items in the game
	private ArrayList<Item> objects;

	private String description;

	private String title;


		
	public Shop(String name, String desc)
	{
		this.inventory = new LinkedList<Item>();
		this.inDemand = new LinkedList<Item>();
		this.playerlist = new PlayerList();
		this.description = desc;
		this.title = name;


		//populate game items list from items.csv
		this.objects = new ArrayList<Item>();

        try
        {
            double inWeight = 0;
            double inValue = 0;
            String inName = "";
            String inDisc = "";
            String inFlavor = "";

            Scanner scanner = new Scanner(new File("./items.csv"));
            scanner.nextLine();
            scanner.useDelimiter(",|\\r\\n|\\n|\\r");

            while(scanner.hasNext())
            {
                inName = scanner.next();
                inWeight = Double.parseDouble(scanner.next().replace(",", ""));
                inValue = Double.parseDouble(scanner.next().replace("\\r\\n|\\r|\\n", ""));
                inDisc = scanner.next();
                inFlavor = scanner.next().replace("\\r\\n|\\r|\\n", "");

                Item newItem = new Item(inName, inWeight, inValue, inDisc, inFlavor);

                this.objects.add(newItem);
            }
        }
        //if borked, populate with original items
        catch(IOException e)
        {
            this.objects.add(new Item("Flower", 1.0, 0.0, null, null));
            this.objects.add(new Item("Textbook", 10.3, 5.2, null, null));
            this.objects.add(new Item("Phone", 2.9, 1.0, null, null));
            this.objects.add(new Item("Newspaper", 10.0, 9.0, null, null));
        }


        Random rand = new Random();

        //populate inDemand with initial items (2 items for now)
        for (int x = 0; x < 2; x++){
        	this.inDemand.add(objects.get(rand.nextInt(objects.size())));	
        }
	}
	
	//get method to get inventory linkedlist
	public LinkedList<Item> getInven()
	{
		return this.inventory;
	}

	//get method to get inventory linkedlist
	public LinkedList<Item> getDemand()
	{
		return this.inDemand;
	}

	//used to add methods to the linked list
	public void add(Item k) {
		if(this.inventory.size() >= 10)
		{
			this.inventory.pop();
		}
		this.inventory.add(k);
	}

	//adds a random item to inDemand list
	public void addDemandRand(){
		Random rand = new Random();
		this.inDemand.add(objects.get(rand.nextInt(objects.size())));
	}

	//used to remove items form the linked list
	public void remove(Object k)
	{
		this.inventory.remove(k);
	}

	//removes item from inDemand
	public void removeDemand(Item k)
	{
		this.inDemand.remove(k);
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
	 * @author Team 4: Alaqeel, Mistry
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
        result += this.getObjects(0);
        
        // players names
       String players = this.getPlayers();
        if (players.isEmpty()) result += "\nYou are here by yourself.\n";
        else {
        	result += "\nYou are here along with: ";
        	result += players;
        }
        
        result += "\n";

        result += "How can we help you?\n";
        return result;
    }
	
	/**
	 * @author Team 4: Alaqeel, Mistry
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
			if(i == 0)
			{
				result += p.getName();
			}
			else
			{
				result += ", " + p.getName();
			}
			i++;
		}
		if(i == 1)
		{
			return "";
		}
		return result + "!\n";
	}
	
	/**
	 * @author Team 4: Alaqeel/Keesling
	 * 
	 * Iterates through the list of the objects and creates a table populated with object names and prices.
	 * @param listType specify list type, 0=inventory 1=inDemand.
	 * @return table of the objects
	 */
	public String getObjects(int listType) {
		LinkedList<Item> list = new LinkedList<Item>();

		// Choose which list type
		if (listType == 0){
			list = this.inventory;
		}
		else if (listType == 1){
			list = this.inDemand;
		}

		// If list is empty
		if (list.size() == 0) {
			if (listType == 0){
				return "\nWe usually have a huge catalog.\n"
					+ "Unfortunately, we are currently out of stock.\n"
					+ "Please come again soon!";	
			}
			else if (listType == 1){
				return "\nThere's nothing in demand!\n"
					+ "If the shop runs out of an item, check back to see if it's in demand.";
			}
		}
		
		int itemLen = 15, countLen = 2, f1 = 3, f2 = 2, priceField = f1 + f2 + 2;
		int menuWidth = itemLen + countLen + f1 + f2 + 6 + 2; // 6 = column padding, 2 = currency + decimal point
		
		// String formats for consistency
		String format = "%-" + countLen +"s | %-" + itemLen + "s | $%-" + f1 + "." + f2 + "f\n";
		String headerFormat = "%-" + countLen +"s | %-" + itemLen + "s | %-" + priceField + "s\n";
		
		// generates menu separator
		String separator = "";
		for (int s = 0; s < menuWidth; s++) separator += "-";
		

		String menu = "";

		// Menu header changes per list type
		if (listType == 0){
			menu += "We sell:\n";
			menu += "...................\n";
			menu += String.format(headerFormat, "#", "Item", "Price");
			
			menu += separator + "\n";
		}
		else if (listType ==1){
			menu += "Items in demand:\n";
			menu += "...................\n";
			menu += String.format(headerFormat, "#", "Item", "Our Offer");
			
			menu += separator + "\n";
		}

		// adding menu items
		int i = 1;
		for (Item obj : list) {
			double price = 0;

			// inv get 20% markup
			if (listType == 0){
				price = obj.getPrice() + (obj.getPrice()*.2);
			}
			// inDem gives offers double item price
			else if (listType == 1){
				price = obj.getPrice()*2;
			}
			
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
