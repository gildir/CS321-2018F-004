import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class RoomConnectivityVerifier {
    public static HashSet<Integer> verifyConnectivity(String worldFile){
        Map map = new Map(worldFile);
        Room startingRoom = map.findRoom(1);
        Queue<Room> queue = new LinkedList<>();
        queue.add(startingRoom);
        HashSet<Room> visited = new HashSet<>();
        while (!queue.isEmpty()){
            Room currentRoom = queue.poll();
            for(Direction direction: Direction.values()){
                int connectedRoomID = currentRoom.getLink(direction);
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
            visitedIDs.add(room.getId());
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
        else{
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
