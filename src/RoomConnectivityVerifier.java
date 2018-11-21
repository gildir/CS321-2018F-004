import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

/****************************************************************************
 * @author Shane
 * 
 * CS 321
 * George Mason University
 * 
 * 10/10/2018
 * 
 * File Name: RoomConnectivityVerifier
*@version 1.0 Early Beta
*@since 2018/11/7
* Description: This class verifies that all rooms are connnected and tere
are no rooms that serve as a black hole.  This is important in the cases
of the maze and the manual generated maps.  
* 
***************************************************************************/
 

public class RoomConnectivityVerifier {
    public static HashSet<Integer> verifyConnectivity(String worldFile){	//Takes a string that is the name of the file to be checked
        Map map = new Map(worldFile);	//Opens the map file
        Room startingRoom = map.findRoom(1);	//starts with room 1
        Queue<Room> queue = new LinkedList<>();	//holds a queue of rooms visited
        queue.add(startingRoom);	
        HashSet<Room> visited = new HashSet<>();	//create a hash table of the rooms visited
        while (!queue.isEmpty()){	//while there are rooms in the queue
            Room currentRoom = queue.poll();	//move to the current room
            for(Direction direction: Direction.values()){	//iterate through the rooms in a breadth first traversal of the rooms
                int connectedRoomID = currentRoom.getLink(direction);	//as rooms are vsited check them off the list
                if(connectedRoomID == 0 || connectedRoomID > 100000){
                    continue;
                }
                Room connectedRoom = map.findRoom(connectedRoomID);
                if(!visited.contains(connectedRoom)){
                    queue.add(connectedRoom);
                    visited.add(connectedRoom);
	       }
            }
        }
        HashSet<Integer> visitedIDs = new HashSet<>();
        for(Room room: visited){
            visitedIDs.add(room.getId());	//traverse though the visited rooms
        }
        return visitedIDs;
//        HashSet<Room> allRooms = new HashSet<>();
//        IntStream stream = IntStream.range(1, Integer.MAX_VALUE);
//        HashSet<Integer> difference = new HashSet<>();
//        for(Room room: allRooms){
//            if(!visited.contains(room)){
//                difference.add(room.getId());
//            }
//        }
//        System.out.println(difference);
////        difference.removeAll(visited);
////        System.out.println(difference);
//        allRooms.addAll(stream.parallel().mapToObj((int i) -> (map.findRoom(i))).filter((Room r) -> r != null).collect(Collectors.toSet()));
//        return visited.equals(allRooms);
    }

    public static void main(String[] args){
        if(args.length != 1){
            System.out.println("improper arguments, should be run with");
            System.out.println("java RoomConnectivityVerifier <worldFile>");
            System.exit(-1);
        }
//        HashSet<Room> visitedRooms = verifyConnectivity(args[0]);
//        HashSet<Integer> roomIDs = new HashSet<>();
//        for(Room room: visitedRooms){
//            roomIDs.add(room.getId());
//        }
//        System.out.println(roomIDs);
        HashSet<Integer> roomIDs = verifyConnectivity(args[0]);
        Map map = new Map(args[0]);
        HashSet<Integer> allRoomIds = new HashSet<>();
        for(int i=1; i < 100000; i++){
            Room room = map.findRoom(i);
            if(room == null){
//                System.out.println("no room with ID: " + i);
                continue;
            }
            allRoomIds.add(room.getId());
//            if(!visitedRooms.contains(room)){
//            try{
//            if(!roomIDs.contains(room.getId())){
//                System.out.println("room: " + room.getId() + " is unconnected");
//            }
        }
	Room room = map.findRoom(100000);
        if(room != null){
            allRoomIds.add(room.getId());
	}

        if(roomIDs.equals(allRoomIds)){
            System.out.println("the rooms are all connected");
        }
        else{	//in the case that not all of the rooms are checked off the list report the rooms in the list not visited.
            System.out.println("there are unconnected rooms");
	    System.out.println("Expected " + allRoomIds.size() + " rooms");
	    System.out.println("Found " + roomIDs.size() + " rooms");
            for(Integer i: allRoomIds){
                if(!roomIDs.contains(i)) {
                    System.out.println("room " + i + " is unconnected");
                }
            }
        }
    }
}
