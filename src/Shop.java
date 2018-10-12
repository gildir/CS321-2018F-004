
import java.util.LinkedList;
public class Shop
{
	//Max of 10 items in this list
	private LinkedList<Object> inventory;
	
	//List of objects that want to be indemand for the shop
	private LinkedList<Object> inDemand;
	
	// List of players in this shop
	private PlayerList playerlist;
	
	
	public Shop()
	{
		this.inventory = new LinkedList<Object>();
		this.inDemand = new LinkedList<Object>();
		this.playerlist = new PlayerList();
	}
	
	//In terms of the player buying items
	public void buy(Object k) {}
	
	//In terms of the player selling items
	public void sell(Object k) {}
	
	//used to add methods to the linked list
	public void add(Object k) {}
	
	//prints the inventory of the shop class
	public void printInv() {}
	
	//Prints the list of object in demand
	public void printDem() {}
	
	//Menu for the player to be interacting with the shop 
	//use something like a switch statement
	public void printMenu() {}
}
