import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RoomConnectivityVerifier {
    public static boolean verifyConnectivity(String worldFile){
        Map map = new Map(worldFile);
        Room startingRoom = map.findRoom(1);
        Queue<Room> queue = new LinkedList<>();
        queue.add(startingRoom);
        HashSet<Room> visited = new HashSet<>();
        while (!queue.isEmpty()){
            Room currentRoom = queue.poll();
//            currentRoom.getExits()
            for(Direction direction: Direction.values()){
                int connectedRoomID = currentRoom.getLink(direction);
                if(connectedRoomID == 0){
                    continue;
                }
                Room connectedRoom = map.findRoom(connectedRoomID);
                if(!visited.contains(connectedRoom)){
                    queue.add(connectedRoom);
                    visited.add(connectedRoom);
                }
            }
        }
        HashSet<Room> allRooms = new HashSet<>();
        IntStream stream = IntStream.range(1, Integer.MAX_VALUE);
        allRooms.addAll(stream.parallel().mapToObj((int i) -> (map.findRoom(i))).filter((Room r) -> r != null).collect(Collectors.toSet()));
        return visited.equals(allRooms);
    }

    public static void main(String[] args){
        if(args.length != 1){
            System.out.println("improper arguments, should be run with");
            System.out.println("*insert proper usage here");
            System.exit(-1);
        }
        boolean connectivityStatus = verifyConnectivity(args[0]);
        if(connectivityStatus){
            System.out.println("the rooms are all connected");
        }
        else{
            System.out.println("there are unconnected rooms");
        }
    }
}
