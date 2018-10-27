
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.*;

/**
 *
 * @author Kevin
 */
public class Player {
    public LinkedList<Item> currentInventory;
    private String name;
    private int currentRoom;
    private Direction currentDirection;
    private PrintWriter replyWriter = null;
    private DataOutputStream outputWriter = null;
    private DataInputStream inputWriter = null;
    private boolean inTrade = false;
    private boolean tradeRequested = false;
    private String tradePartner = "";

    public Player(String name) {
        this.currentRoom = 1;
        this.currentDirection = Direction.NORTH;
        this.name = name;
        this.currentInventory = new LinkedList<>();
    }
    
    public void turnLeft() {
        switch(this.currentDirection.toString()) {
            case "North":
                this.currentDirection = Direction.WEST;
                break;
            case "South":
                this.currentDirection = Direction.EAST;
                break;
            case "East":
                this.currentDirection = Direction.NORTH;
                break;
            case "West":
                this.currentDirection = Direction.SOUTH;
                break;                
        }
    }
    
    public void turnRight() {
        switch(this.currentDirection.toString()) {
            case "North":
                this.currentDirection = Direction.EAST;
                break;
            case "South":
                this.currentDirection = Direction.WEST;
                break;
            case "East":
                this.currentDirection = Direction.SOUTH;
                break;
            case "West":
                this.currentDirection = Direction.NORTH;
                break;                
        }
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<Item> getCurrentInventory() {
        return currentInventory;
    }

    public void setCurrentInventory(LinkedList<Item> currentInventory) {
        this.currentInventory = currentInventory;
    }
    
    public void addObjectToInventory(Item object) {
        this.currentInventory.add(object);
    }
   
    //removes the first instance 
    public Item removeObjectFromInventory(String target) {
        for(Item o : this.currentInventory) {
                String nameToRemove = o.getName();
            if(nameToRemove.equalsIgnoreCase(target)) {
		Item temp = o;
                this.currentInventory.remove(o);
                return temp;
            }
        }
        return null;
    }

    public void sortCurrentInventory(String modes) {
	switch(modes) {
		case "ni":
			Collections.sort(currentInventory, new ItemNameComparator());	
			break;
		case "nd":
			Collections.sort(currentInventory, new ItemNameComparator());
			Collections.reverse(currentInventory);
			break;
		case "wi":
			Collections.sort(currentInventory, new ItemWeightComparator());
			break;
		case "wd":
			Collections.sort(currentInventory, new ItemWeightComparator());
			Collections.reverse(currentInventory);
			break;
		case "pi":
			Collections.sort(currentInventory, new ItemPriceComparator());
			break;
		case "pd":
			Collections.sort(currentInventory, new ItemWeightComparator());
			Collections.reverse(currentInventory);
			break;	
		default:
			System.out.println("Please enter in valid input or use the correct format (n/w/p) -> (i/d)");
	}
    }
    
    public boolean hasTradeRequest(){
        return tradeRequested;
    }

    public void setTradeRequest(boolean val){
        tradeRequested = val;
    }

    public boolean isInTrade(){
        return inTrade;
    }
    public void setInTrade(boolean val){
        inTrade = val;
    }

    public String getTradePartner(){
        return tradePartner;
    }
    public void setTradePartner(String s){
        tradePartner = s;
    }



    public void setReplyWriter(PrintWriter writer) {
        this.replyWriter = writer;
    }
    
    public PrintWriter getReplyWriter() {
        return this.replyWriter;
    }
    
    public void setOutputWriter(DataOutputStream writer) {
        this.outputWriter = writer;
    }
    
    public DataOutputStream getOutputWriter() {
        return this.outputWriter;
    }
    
    public int getCurrentRoom() {
        return this.currentRoom;
    }
    
    public void setCurrentRoom(int room) {
        this.currentRoom = room;
    }
    
    public String getCurrentDirection() {
        return this.currentDirection.name();
    }
    
    public Direction getDirection() {
        return this.currentDirection;
    }
    
    public String viewInventory() {
        String result = "";
        if(this.currentInventory.isEmpty() == true) {
            return "nothing.";
        }
        else {
            for(Item obj : this.currentInventory) {
                result += " " + obj.name;
		result += (" (" + obj.weight + ") ");
            }
            result += ".";
        }
        return result;
    }

    @Override
    public String toString() {
        return "Player " + this.name + ": " + currentDirection.toString();
    }

    private static class ItemNameComparator implements Comparator<Item> {
	@Override
	public int compare(Item ItemA, Item ItemB) {
		return ItemA.getName().compareTo(ItemB.getName());
	}
    }

    private static class ItemWeightComparator implements Comparator<Item> {
	@Override
	public int compare(Item ItemA, Item ItemB) {
		if(ItemA.getWeight() > ItemB.getWeight()) {
			return 1;
		}
		else if(ItemA.getWeight() < ItemB.getWeight()) {
			return -1;
		}
		else {
			return 0;
		}
	}
    }

    private static class ItemPriceComparator implements Comparator<Item> {
	@Override
	public int compare(Item ItemA, Item ItemB) {
		if(ItemA.getPrice() > ItemB.getPrice()) {
			return 1;
		}
		else if(ItemA.getPrice() < ItemB.getPrice()) {
			return -1;
		}
		else {
			return 0;
		}
	}
    }
}
