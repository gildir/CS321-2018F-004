
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
        private LinkedList<Room> map;

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
                                        ArrayList<DialogueOption> dialogue = new ArrayList<DialogueOption>();

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
                } catch (IOException | IllegalArgumentException ex) {
                        Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("[SHUTDOWN] Invalid File " + worldFile);
	                      System.exit(-1);
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
	Room ret = null;
	do{
        ret = map.get(rand.nextInt(map.size()));
	}while(ret.getId() >= 100000);
	return ret;
    }
    
    public void addRoom(Room room) {
    	
    	map.add(room);
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

    /**
     * @author Group 6: Ryan
     * Displays a map of nearby rooms. Assumes a grid layout, though the actual layout may be different
     * @param baseId The room to center the map on
     * @return String representation of a map
     */
    public String asciiMap(int baseId)
    {
        Node[][] nodeArr = new Node[3][5];
        String result = "";
	int row = 1;
	int col = 2;
	Room current;

	setExits(row,col,nodeArr,baseId);

	result += "   ______________________________________________\n";
	result += "   |ASCII Map - Displaying rooms near you!      |\n";
	result += "   | X = You  $ = Shop   # = Inside | = Outside |\n";
	result += "   | G = Ghoul  -+ = Exit  -0 = Exit to Off Map |\n";
	result += "   |                                            |\n";
	result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                      X                     |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |                                            |\n";
        result += "   |____________________________________________|\n";
	
	for(row = 0; row < 3; row ++)
	{
           for(col = 0; col < 5; col ++)
	   {
              if(nodeArr[row][col] != null){
                 current = this.findRoom(nodeArr[row][col].id);
		 //System.out.println("Printing Room [" + row +"][" + col + "]");
	         result = result.substring(0,309 + (250*row) +(8*col)) + "___" +result.substring(312 + (250*row) + (8*col),result.length());
	         if(!nodeArr[row][col].inside){
	            result = result.substring(0,358 + (250*row) +(8*col)) +"|   |" +result.substring(363 + (250*row) + (8*col),result.length());
	            result = result.substring(0,408 + (250*row) +(8*col)) +"|___|" +result.substring(413 + (250*row) + (8*col),result.length());
                 }
	         else{
	            result = result.substring(0,358 + (250*row) +(8*col)) +"#   #" +result.substring(363 + (250*row) + (8*col),result.length());
                    result = result.substring(0,408 + (250*row) +(8*col)) +"#___#" +result.substring(413 + (250*row) + (8*col),result.length());
	         }
	         if(row == 1 && col == 2){
                    result = result.substring(0,626) + "X" + result.substring(627,result.length());
	         }
	         if(nodeArr[row][col].n){
	            if(row > 0 && nodeArr[row-1][col] != null && current.getLink(Direction.valueOf("NORTH")) == nodeArr[row-1][col].id)
                       result = result.substring(0,209 + (250*row) + (8*col)) + "+" + result.substring(210 + (250*row) + (8*col),result.length());
		    else
		       result = result.substring(0,209 + (250*row) + (8*col)) + "0" + result.substring(210 + (250*row) + (8*col),result.length());
	   	    result = result.substring(0,259 + (250*row) + (8*col)) + "|" + result.substring(260 + (250*row) + (8*col),result.length());
	         }
	         if(nodeArr[row][col].s){
	            if(row < nodeArr.length-1 && nodeArr[row+1][col] != null && current.getLink(Direction.valueOf("SOUTH")) == nodeArr[row+1][col].id)
                       result = result.substring(0,511 + (250*row) + (8*col)) + "+" + result.substring(512 + (250*row) + (8*col),result.length());
		    else
		       result = result.substring(0,511 + (250*row) + (8*col)) + "0" + result.substring(512 + (250*row) + (8*col),result.length());
		    result = result.substring(0,461 + (250*row) + (8*col)) + "|" + result.substring(462 + (250*row) + (8*col),result.length());
		 }
	         if(nodeArr[row][col].w){
	            if(col > 0 && nodeArr[row][col-1] != null && current.getLink(Direction.valueOf("WEST")) == nodeArr[row][col-1].id)
                       result = result.substring(0,405 + (250*row) + (8*col)) + "+-" + result.substring(407 + (250*row) + (8*col),result.length());
		    else
		       result = result.substring(0,405 + (250*row) + (8*col)) + "0-" + result.substring(407 + (250*row) + (8*col),result.length());
                 }
	         if(nodeArr[row][col].e){
	            if(col < (nodeArr[1].length - 1) && nodeArr[row][col+1] != null && current.getLink(Direction.valueOf("EAST")) == nodeArr[row][col+1].id)
                       result = result.substring(0,364 + (250*row) + (8*col)) + "-+" + result.substring(366 + (250*row) + (8*col),result.length());
		    else
		       result = result.substring(0,364 + (250*row) + (8*col)) + "-0" + result.substring(366 + (250*row) + (8*col),result.length());
                 }
                 if(nodeArr[row][col].ghoul){
                    result = result.substring(0,409 + (250*row) + (8*col)) + "G" + result.substring(410 + (250*row) + (8*col),result.length());
                 }
                 if(nodeArr[row][col].shop){
                    result = result.substring(0,411 + (250*row) + (8*col)) + "$" + result.substring(412 + (250*row) + (8*col),result.length());
                 }
	      }
	   }
	}	
	return result;
    }

    /**
     * @author Group 6: Ryan
     * Helper method to recursively set the exit values for all the nodes in an node array
     * @param row Row index of the current node
     * @param col Column index of the current node
     * @param nodeArr Array of nodes to set the exits for
     * @param roomId The room id for the corresponding node
     */
    private void setExits(int row, int col, Node[][] nodeArr,int roomId)
    {

       if(roomId != 0 && row >= 0 && col >= 0 && row < nodeArr.length && col < nodeArr[0].length && nodeArr[row][col] == null && roomId != 100001)
	       //Exit conditions: Out of bounds, already initialized, or no path into
       {
          //System.out.println("Setting exits for room [" + row + "][" + col + "]");
	  Room room = this.findRoom(roomId);
	  nodeArr[row][col] = new Node(room);
	  Scanner exits = new Scanner(room.getExits()).useDelimiter(" ");
          while(exits.hasNext())
          {
             switch(exits.next())
             {
                case "NORTH":
                   setExits(row - 1, col, nodeArr, room.getLink(Direction.valueOf("NORTH")));
                   break;
                case "SOUTH":
                   setExits(row + 1, col, nodeArr, room.getLink(Direction.valueOf("SOUTH")));
                   break;
                case "EAST":
                   setExits(row, col + 1, nodeArr, room.getLink(Direction.valueOf("EAST")));
                   break;
                case "WEST":
                   setExits(row, col - 1, nodeArr, room.getLink(Direction.valueOf("WEST")));
                   break;
             }
          }
       }
    }

    /**
     * @author Group 6: Ryan
     * A node class containing information about a room
     */
    private class Node
    {
       public boolean n, s, e, w;//Exits going (north | south | east | west)
       public boolean inside, ghoul, shop;
       public int id;       

       public Node()
       {
          n = false;
	  s = false;
	  e = false;
	  w = false;
	  inside = false;
	  ghoul = false;
	  shop = false;
	  id = 0;
       }

       public Node(Room room)
       {
	  n = false;
	  s = false;
	  e = false;
	  w = false;
	  Scanner exits = new Scanner(room.getExits()).useDelimiter(" ");
	  while(exits.hasNext())
	  {
             switch(exits.next())
	     {
                case "NORTH":
		   n = true;
		   break;
		case "SOUTH":
		   s = true;
		   break;
		case "EAST":
		   e = true;
		   break;
		case "WEST":
		   w = true;
		   break;
	     }
	  }
	  inside = room.getRoomType().equals("inside");
	  ghoul = room.hasGhoul;
	  shop = isShoppable(room);
	  id = room.getId();
       }
    }
}
