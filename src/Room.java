import java.util.LinkedList;
import java.lang.StringBuilder;
import java.lang.IllegalArgumentException; 

/**
 *
 * @author Kevin
 */
public class Room {
    private final int id;
    private final String title;
    private final String description;
    private final LinkedList<String> objects;
    private final LinkedList<Exit> exits;

    private static final int MAX_WHITEBOARD_LEN = 120;
    private StringBuilder whiteboard;
    
    public Room(int id, String title, String description) {
        this.objects = new LinkedList<>();
        this.exits = new LinkedList<>();        
        this.whiteboard = new StringBuilder(MAX_WHITEBOARD_LEN);
        
        this.id = id;
        this.title = title;
        this.description = description;
    }
    
    public String toString(PlayerList playerList, Player player) {
        String result = ".-------------------------\n";
        result += "| " + this.getTitle() + "\n";
        result += "-------------------------\n";
        result += this.getDescription() + "\n";
        result += "...................\n";
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
            if(exit.getRoom() != 0) {
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
                return exit.getRoom();
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
    
    public String getObjects() {
        if(this.objects.isEmpty()) {
            return "None.";
        }
        else {
            return this.objects.toString();
        }
    }
    
    public void addObject(String obj) {
        if(this.objects.size() < 5) {
            this.objects.add(obj);
        }
    }
    
    public String removeObject(String target) {
        for(String obj : this.objects) {
            if(obj.equalsIgnoreCase(target)) {
                this.objects.remove(obj);
                return obj;
            }
        }
        return null;
    }

    /**
     *  This method removes all objects from the room and returns a linked list of all objects removed from the room.
     *   
     *  @return LinkedList containing all objects removed from the room
     * 
     */
    public LinkedList<String> removeAllObjects()
    {
        LinkedList<String> removedObjects = new LinkedList<>(this.objects);
        this.objects.clear();
        return removedObjects;
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
    public void eraseWhiteboard() {
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
