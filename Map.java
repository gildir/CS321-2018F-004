
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

/**
 *
 * @author Kevin
 */
public class Map{   
    private final LinkedList<Room> map;
    
    public Map() {
        map = new LinkedList<>();
        try {
            File mapFile = new File("./rooms.csv");
            Scanner mapIn = new Scanner(mapFile).useDelimiter(",|\\n|\\r\\n");
 
	    int numRooms, numExits;

            String title, description;
            String message;
            int id, link;
            
            Direction exitId;
            
            Room newRoom;
            Exit newExit;
            
            numRooms = Integer.parseInt(mapIn.nextLine());
	    numExits = 4;

            for(int i = 0; i < numRooms; i++) {
               
		mapIn.useDelimiter(",|\\n|\\r\\n"); 
                id = Integer.parseInt(mapIn.next());
                title = mapIn.next();
		mapIn.useDelimiter("\\S|\\s");
		mapIn.next();
		mapIn.useDelimiter("\\n|\\r\\n");
                description = mapIn.next();

//                System.out.println("Adding Room " + id + " with Title " + title + ": " + description);

		newRoom = new Room(id, title, description);
               
                for(int j = 0; j < numExits; j++) {
                    
	            mapIn.useDelimiter(",|\\n|\\r\\n");
                    exitId = Direction.valueOf(mapIn.next());
                    link = Integer.parseInt(mapIn.next());
                    mapIn.useDelimiter("\\S|\\s");
                    mapIn.next();
		    mapIn.useDelimiter("\\n|\\r\\n");
                    message = mapIn.next();

//                    System.out.println("... Adding Exit " + exitId + " to " + link + ": " + message);
                    newRoom.addExit(exitId, link, message);
                }                
                
                map.add(newRoom);
            }
        mapIn.close();
        } catch (IOException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Room findRoom(int roomId) {
        for(Room room : this.map) {
            if(room.getId() == roomId) {
                return room;
            }
        }
        return null;
    }
    
    public Room randomRoom() {
        Random rand = new Random();
        return map.get(rand.nextInt(map.size()));
    }
}
