import java.util.LinkedList;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

public class DormRoom extends Room{     
    
    
    private LinkedList<Item> chest;
    DormRoom(int id, String room_type, String title, String description){
    		
    	
    	super(id, room_type, title, description);
    	this.chest = new LinkedList<>();
    	System.out.println("private room added: id: "+id+" room_type: "+room_type+" descript: "+description);
    	
    }

    public void setChest(LinkedList<Item> c){

        this.chest=c;
    } 
    /**gets link of chest in dorm Room
     *    
     * */
    public LinkedList<Item> getChest(){

        return this.chest;
    } 
    /**115 Jorge team 6
    *  this will  add an object to the chest 
    *  called from chestActions*/
    public String addObjectToChest(Item o){   
         chest.add(o);
        return "Item added";
      }
        
    /**115 Jorge team 6
     * this will remove a single instance of the given
     *  object from the chest if obj if is present
     *  called from chestAction */    

    public Item  removeObjectfromChest(String name){    

        for(Item o : this.chest) {
                String objToRemove = o.getName();
            if(objToRemove.equalsIgnoreCase(name)) {
		Item temp = o;
                this.chest.remove(o);
                return temp;
            }
        }
        return null;
    }

    /**  115 Jorge team 6;
    *  print chest content
    *  called from chestACtion*/
    public String printChest() {
    
        String content = "";
    
        if (chest.isEmpty() == true){
            return "Empty Chest";
        }

        for( Item i : chest ) { content += " "+i.name+" (" + i.weight + ") \n" ;}
    
        return "+++++++++++++CHEST-CONTENT++++++++++++\n"+
                content + 
                "##############################\n";

    }      
    
    /** 115 jorge team 6 
    * will get called from chestActions
    * it  will display the chest Menu
    * options  */
    public String chestMenu(){

       return
            "|--------Chest action Menu-----------|\n"+
            "|                                    |\n"+
            "|    'a': transfer pocket -> chest   |\n"+
            "|    'x': extract  chest -> pocket   |\n"+
            "|    'p': print chest content        |\n"+
            "|    'q': quit this menu             |\n"+
            "|------------------------------------|\n"
       ;
    } 
   /* chest operations: this can only get called when the player is in his private DormRoom, 
    *  prompts the player for an action to either add or remove items to or from the chest.  
    *s
    *  */
    public void chestActions(Player player){


        InputStreamReader keyReader = new InputStreamReader(System.in);
        BufferedReader keyInput = new BufferedReader(keyReader);
        boolean validInput = true;
        try {
                while(validInput) {
                    /* print the menu */
                    System.out.println( chestMenu() );
                    String input = keyInput.readLine();
                    input.toLowerCase();
                    switch(input) {
                            case "a":// add item to chest 
                                    System.out.println("Enter object name to transfer into the chest");
                                    input = keyInput.readLine();
                                    Item object = player.removeObjectFromInventory(input);
                                    if(object != null) {
                                        addObjectToChest(object);
                                        System.out.println( "You placed a " + input +" in the chest");
                                    }else{
                                       System.out.println("Object not found in your inventory"); 
                                       System.out.println("please type the exact name"); 
                                    }                                    
                                    break;
                            case "x"://extract item from chest into pocket
                                    System.out.println("xxxxxx");
                                    break;
                            case "p"://print chesto content
                                    printChest(); 
                                    break;
                            case "q":// quit this sub menu 
                                    validInput = false;
                                    break;
                            default:
                                    System.out.println("Please enter a valid input  )");
                    }//end switch 
              }//end while 
        }//end try blc
        catch(IOException e) {
              System.err.println("[CRITICAL ERROR] Error at reading any input properly.  Terminating the client now.");
              System.exit(-1);
        }


    }//end chest actions     

}   
