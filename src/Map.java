
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

/**
 * @author Kevin
 */

public class Map{   
        private final LinkedList<Room> map;

        public Map(String worldFile) {
                map = new LinkedList<>();
                try {
                        File mapFile = new File(worldFile);	//Set the mapfile to be the file defined by worldFile
                        Scanner mapIn = new Scanner(mapFile).useDelimiter(",|\\n|\\r\\n");	//Set the tokens for parsing the map file

                        int numRooms, numExits;	//Stores the number of rooms and the number of exits

                        String title, description, room_type;	//Strings that will hold the data for the descriptions of the rooms
                        String message;
                        int id, link;

                        Direction exitId;

                        Room newRoom;	//
                        Exit newExit;

                        numRooms = Integer.parseInt(mapIn.nextLine());	//Read the number of rooms form the first line of the file
                        numExits = 4;	//set the number of potential exits as 4

                        for(int i = 0; i < numRooms; i++) {

                                mapIn.useDelimiter(",|\\n|\\r\\n"); 	//While the number of rooms created is less than the number of rooms defined in the file 
                                id = Integer.parseInt(mapIn.next());	//read the 5 lines in the file that define a room.
                                room_type = mapIn.next();
                                title = mapIn.next();
                                mapIn.useDelimiter("\\S|\\s");
                                mapIn.next();
                                mapIn.useDelimiter("\\n|\\r\\n");
                                description = mapIn.next();

                                //                System.out.println("Adding Room " + id + " with Title " + title + ": " + description);


                                if(id == 1){
                                        LinkedList<String> quests = new LinkedList<>(Arrays.asList("quest1", "quest2", "quest3"));	//Define the quests for the NPC
                                        newRoom = new Room(id, room_type, title, description, new LinkedList<>(Arrays.asList(
                                                            new NPC("Slartibartfast", 1, quests))));	//Rename the NPC as per sprint 5
                                }
                                else {
                                        newRoom = new Room(id, room_type, title, description);
                                }

                                for(int j = 0; j < numExits; j++) {

                                        mapIn.useDelimiter(",|\\n|\\r\\n");	//For each direction check and add exits as neccisary
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
    
    /**
     * @author Group 4: King
     * Checks that room the player is contains a shop
     * @param r The room in question
     * @return true if it's a shoppable room, false otherwise
     */
    public boolean isShoppable(Room r) {
    	if (r.getId() == 1) {	// Need to improve this if more shops are added
    		return true;
    	}
    	return false;
    }
}
