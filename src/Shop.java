
import java.util.LinkedList;
public abstract class Shop
{
	//Max of 10 items in this list
	private LinkedList<Object> inventory;
	//List of objects that want to be indemand for the shop
	private LinkedList<Object> inDemand;
	public Shop()
	{
		inventory = new LinkedList<Object>();
		inDemand = new LinkedList<Object>();
	}
	//In terms of the player buying items
	public abstract void buy(Object k);
	
	//In terms of the player selling items
	public abstract void sell(Object k);
	
	//prints the inventory of the shop class
	public abstract void printInv();
	
	//Prints the list of object in demand
	public abstract void printDem();
	
	//Menu for the player to be interacting with the shop 
	//use something like a switch statement
	public abstract void printMenu();
}
