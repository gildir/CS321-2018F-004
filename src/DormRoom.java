import java.util.LinkedList;

/****************************************************************************
 * @author Jorge
 * 
 * CS 321
 * George Mason University
 * 
 * 11/16/2018
 * 
 * File Name: DormRoom.java
*@version 1.0 Early Beta
*@since 2018/11/7
* Description: This class is resposible for creating an indvidual room for
each player in the game.  The room will serves as a location for the chest,
which will be a point where the player to save items in the game.
* 
***************************************************************************/
 
public class DormRoom extends Room{     //Extends the room class 
    
    
    private final LinkedList<Item> chest;	//The location the player will be able to store items in the game
    DormRoom(int id, String room_type, String title, String description){
    		
    	
    	super(id, room_type, title, description);	//creates the players room 
    	this.chest = new LinkedList<>();	//Chest is a linked list of stored items for the player
    	System.out.println("private room added: id: "+id+" room_type: "+room_type+" descript: "+description);
    	
    }
    
    
            

}   
