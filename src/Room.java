import java.util.LinkedList;
import java.util.HashMap;
import java.lang.StringBuilder;
import java.lang.IllegalArgumentException; 

/**
 *
 * @author Kevin
 */
public class Room {
    private final int id;
    private final String title;
    private final String room_type;
    private final String description;
    private final LinkedList<Item> objects;
    private final LinkedList<Exit> exits;



    private static final int MAX_WHITEBOARD_LEN = 120;
    private StringBuilder whiteboard;
    //list of NPCs in a room, list in case additional NPCs are added to the game
    private final HashMap<String, NPC> npcs;
    //add tem state check for ghoul
    public boolean hasGhoul = false;

    
    public Room(int id, String room_type, String title, String description) {
        this.objects = new LinkedList<>();
        this.exits = new LinkedList<>();        
        this.whiteboard = new StringBuilder(MAX_WHITEBOARD_LEN);
        
        this.id = id;
        this.title = title;
        this.description = description;
        this.room_type = room_type;
        this.npcs = new HashMap<>();
    }

    public Room(int id, String room_type, String title, String description, HashMap<String, NPC> npcs) {
        this.objects = new LinkedList<>();
        this.exits = new LinkedList<>();
        this.whiteboard = new StringBuilder(MAX_WHITEBOARD_LEN);
        this.id = id;
        this.title = title;
	    this.room_type = room_type;
        this.description = description;
        this.npcs = npcs;
    }
    
    public String toString(PlayerList playerList, Player player) {
        String result = ".-------------------------+----------------------\n";
        result += "| " + this.getTitle() + ", this room is "+this.getRoomType() + "\n";
        result += ".-------------------------+----------------------\n";
        result += this.getDescription() + "\n";
        result += "...................\n";
        result += "NPCs in the area: " + this.getNPCs().keySet() + "\n";
        result += "Objects in the area: " + this.getObjects() + "\n";
        result += "Players in the area: " + this.getPlayers(playerList) + "\n";
        result += "You see paths in these directions: " + this.getExits() + "\n";
        result += "...................\n";
        result += "You are facing: " + player.getCurrentDirection() + "\n";
        return result;
    }
    
    
    public int getId() {
        return this.id;
    }
    
    public String getExits() {
        String result = "";
        for(Exit exit : this.exits) {
            if(exit.getRoom() > 0) {
                result += exit.getDirection().name() + " ";
            }
        }
        return result;
    }
    
    public void addExit(Direction direction, int room, String message) {
        exits.add(new Exit(direction, room, message));
    }
    
    public boolean canExit(Direction direction) {
        for(Exit exit : this.exits) {
            if(exit.getDirection() == direction) {
                return exit.getRoom() != 0;
            }
        }
        return false;
    }
    
    public String exitMessage(Direction direction) {
        for(Exit exit : this.exits) {
            if(exit.getDirection() == direction) {
                return exit.getMessage();
            }
        }
        return null;
    }
    
    public int getLink(Direction direction) {
        for(Exit exit : this.exits) {
            if(exit.getDirection() == direction) {
                int link = exit.getRoom();
		if(link < 0)
		   link = -link;
		return link;
            }
        }
        return 0; 
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public String getRoomType() {
        return this.room_type;
    }
    
    public String getObjects() {
        if(this.objects.isEmpty()) {
            return "None.";
        }
        else {
		String ret = "";
		for(Item obj : this.objects) {
			ret += " " + obj.toString();
		}
		return ret;
        }
    }

    public HashMap<String, NPC> getNPCs() {
        return this.npcs;
    }
    
    public void addObject(Item obj) {
        if(this.objects.size() < 5) {
            this.objects.add(obj);
        }
        else{
            throw new IndexOutOfBoundsException("Can not add more objects, objects is at capacity");
        }
    }

    public Item removeObject(String target) {
        for(Item obj : this.objects) {
            String nameToRemove = obj.name;
            if(nameToRemove.equalsIgnoreCase(target)) {
                this.objects.remove(obj);
                return obj;
            }
        }
        return null;
    }

    public LinkedList<Item> removeAllObjects()
    {
        LinkedList<Item> newList = new LinkedList<Item>();
        while(!this.objects.isEmpty())
        {
            newList.add(objects.get(0));
            this.objects.remove(0);
        }  
        return newList; 
    }

    /**
     *  This method returns the current whiteboard text
     *   
     *  @return Current text on whiteboard
     * 
     */
    public String getWhiteboardText() {
        return whiteboard.toString();
    }

    /**
     *  This method adds text to the whiteboard
     *
     *  @param Text to add to whiteboard
     *   
     *  @return true if text added to whiteboard; false if whiteboard is full 
     * 
     */
    public boolean addWhiteboardText(String textToAdd) {

        if (textToAdd == null) { 
            throw new IllegalArgumentException("Text can't be null");
        }

        if (textToAdd.length() + whiteboard.length() > MAX_WHITEBOARD_LEN) {
            return false;
        }
        else {
            whiteboard.append(textToAdd);
            return true;
        }
    }

    /**
     *  This method erases the whiteboard
     *
     */
    public void whiteboardErase() {
        whiteboard.setLength(0);
    }


    

    public String getPlayers(PlayerList players) {
        String localPlayers = "";
        for(Player player : players) {
            System.err.println("Checking to see if " + player.getName() + " in room " + player.getCurrentRoom() + " is in this room (" + this.id + ")");
            if(player.getCurrentRoom() == this.id) {
                localPlayers += player.getName() + " ";
            }
        }
        if(localPlayers.equals("")) {
            return "None.";
        }
        else {
            return localPlayers;
        }
    }
}
