import java.util.LinkedList;


public class DormRoom extends Room{     
    
    
    private final LinkedList<Item> chest;
    DormRoom(int id, String room_type, String title, String description){
    		
    	
    	super(id, room_type, title, description);
    	this.chest = new LinkedList<>();
    	System.out.println("private room added: id: "+id+" room_type: "+room_type+" descript: "+description);
    	
    }
    
    
            

}   
