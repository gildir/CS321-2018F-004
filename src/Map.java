
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

        public Map(String worldFile) {		//Name of the map passed on the command line as an argument.
                map = new LinkedList<>();
                try {
                        File mapFile = new File(worldFile);	//Opens the file containing the map csv
                        Scanner mapIn = new Scanner(mapFile).useDelimiter(",|\\n|\\r\\n");

                        int numRooms, numExits;

                        String title, description, room_type;	//Strings store the title of the room, a brief description of the room, and the type of room ( inside/outside)
                        String message, npcName;	
                        int id, link;	

                        Direction exitId;

                        Room newRoom;
                        Exit newExit;

                        numRooms = Integer.parseInt(mapIn.nextLine());	//Reads the fle parsing the csv file to create the rooms.
                        numExits = 4;	//Sets the number of exits at 4

                        mapIn.useDelimiter(",|\\n|\\r\\n");

			for(int i = 0; i < numRooms; i++) {

                                id = Integer.parseInt(mapIn.next());	//Walks through the file parsing lines to create the map.
                                room_type = mapIn.next();
                                title = mapIn.next();
                                mapIn.useDelimiter("\\S|\\s");
                                mapIn.next();
                                mapIn.useDelimiter("\\n|\\r\\n");
                                description = mapIn.next();

                                //                System.out.println("Adding Room " + id + " with Title " + title + ": " + description);



                                if(id == 1){ //If the room number is 1 then the quest NPC will be here 
                                        LinkedList<String> quests = new LinkedList<>(Arrays.asList("quest1", "quest2", "quest3"));//Lists the quests avalible at the NPC
                                        String questNPCName = "Slartibartfast";//Renamed the NPC 114 by cwells21
                                        ArrayList<DialogueOption> dialogue = new ArrayList<DialogueOption>();

                                        HashMap<String, NPC> npcs = new HashMap<>();
                                        npcs.put(questNPCName, new NPC(questNPCName, 1, quests, dialogue));

                                        newRoom = new Room(id, room_type, title, description, npcs);

                                }
                                else {
                                        newRoom = new Room(id, room_type, title, description);	//This room is not 1 and the quest NPC is not here
                                }

                                for(int j = 0; j < numExits; j++) {

                                        mapIn.useDelimiter(",|\\n|\\r\\n");	//using a comma delimited file to represent the data of the room.
                                        exitId = Direction.valueOf(mapIn.next());
                                        link = Integer.parseInt(mapIn.next());
                                        mapIn.useDelimiter("\\S|\\s");
                                        mapIn.next();
                                        mapIn.useDelimiter("\\n|\\r\\n");
                                        message = mapIn.next();

                                        //                    System.out.println("... Adding Exit " + exitId + " to " + link + ": " + message);
                                        newRoom.addExit(exitId, link, message);
                                }                
			        mapIn.useDelimiter(",|\\n|\\r\\n");	
                                //Check if the next value is an integer, if not, then get the name of an npc to add from the next value and add it to the last room
                                while(!mapIn.hasNextInt() && mapIn.hasNext())
                                {
                                        npcName = mapIn.next();
                                        newRoom.addNPC(npcName, id);
                                }
				map.add(newRoom);
                        }

                        mapIn.close();	//close the map file
                } catch (IOException | IllegalArgumentException ex) {
                        Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("[SHUTDOWN] Invalid File " + worldFile);	//Handles the case that the file is not formatted as a map.
	                      System.exit(-1);	//Report back to the OS of the improper exit of the game.

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

	//Blank display template for the display

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

	//Map function works for grid layout and some custom layouts.
	//Will not work on the mazes, then if you had a map it would not be much of a maze

	for(row = 0; row < 3; row ++)
	{
           for(col = 0; col < 5; col ++)
	   {

              if(nodeArr[row][col] != null){	//Walk through a grid of room starting from upper left to lower right dawing a room if one is connected

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

          while(exits.hasNext())	//While a next room exists procss north, south, east west...

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

          n = false;	//initialize the room to be a blank slate for the information to be added later

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
	  ghoul = room.hasGhoul();
	  shop = isShoppable(room);
	  id = room.getId();
       }
    }
}
