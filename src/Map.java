
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import java.util.HashMap;

/**
 * @author Kevin
 */

public class Map{   
        private final LinkedList<Room> map;

        public Map(String worldFile) {
                map = new LinkedList<>();
                try {
                        File mapFile = new File(worldFile);
                        Scanner mapIn = new Scanner(mapFile).useDelimiter(",|\\n|\\r\\n");

                        int numRooms, numExits;

                        String title, description, room_type;
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
                                room_type = mapIn.next();
                                title = mapIn.next();
                                mapIn.useDelimiter("\\S|\\s");
                                mapIn.next();
                                mapIn.useDelimiter("\\n|\\r\\n");
                                description = mapIn.next();

                                //                System.out.println("Adding Room " + id + " with Title " + title + ": " + description);


                                if(id == 1){
                                        LinkedList<String> quests = new LinkedList<>(Arrays.asList("quest1", "quest2", "quest3"));
                                        String questNPCName = "questNPC";
                                        ArrayList<DialogueOption> dialogue = new ArrayList<DialogueOption>(Arrays.asList(new DialogueOption("Hello quest NPC", "Hello adventurer"),
                                                    new DialogueOption("What is your name", "My name is " + questNPCName + ".")));
                                        HashMap<String, NPC> npcs = new HashMap<>();
                                        npcs.put(questNPCName, new NPC(questNPCName, 1, quests, dialogue));

                                        newRoom = new Room(id, room_type, title, description, npcs);
                                }
                                else {
                                        newRoom = new Room(id, room_type, title, description);
                                }

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
