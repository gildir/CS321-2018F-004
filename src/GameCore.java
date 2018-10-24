


import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.LinkedList;




/**
 *
 * @author Kevin
 */
public class GameCore implements GameCoreInterface {
    private final PlayerList playerList;
    private final Map map;

    // Text file which logs player-world interactions
    protected DailyLogger dailyLogger;
    
    /**
     * Creates a new GameCoreObject.  Namely, creates the map for the rooms in the game,
     *  and establishes a new, empty, player list.
     * 
     * This is the main core that both the RMI and non-RMI based servers will interface with.
     */
    public GameCore() {
        
        // Generate the game map.
        map = new Map();
        this.dailyLogger = new DailyLogger();
        dailyLogger.write("SERVER STARTED");
        
        playerList = new PlayerList();
        
        Thread objectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Random rand = new Random();
                Room room;
                String object;
                String[] objects = {"Flower", "Textbook", "Phone", "Newspaper"};
                while(true) {
                    try {
                        Thread.sleep(rand.nextInt(60000));
                        object = objects[rand.nextInt(objects.length)];
                        room = map.randomRoom();
                        room.addObject(object);
                        
                        GameCore.this.broadcast(room, "You see a student rush past and drop a " + object + " on the ground.");
                        dailyLogger.write(object + " dropped at " + room.getTitle());

                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        objectThread.setDaemon(true);
        objectThread.start();
    }
    
    /**
     * Broadcasts a message to all other players in the same room as player.
     * @param player Player initiating the action.
     * @param message Message to broadcast.
     */   
    @Override
    public void broadcast(Player player, String message) {
        for(Player otherPlayer : this.playerList) {
            if(otherPlayer != player && otherPlayer.getCurrentRoom() == player.getCurrentRoom()) {
                otherPlayer.getReplyWriter().println(message);
            }
        }
    }
  
    /**
     * Broadcasts a message to all players in the specified room.
     * @param room Room to broadcast the message to.
     * @param message Message to broadcast.
     */   
    @Override
    public void broadcast(Room room, String message) {
        for(Player player : this.playerList) {
            if(player.getCurrentRoom() == room.getId()) {
                player.getReplyWriter().println(message);
            }
        }
    }
    
    /**
     * Returns the player with the given name or null if no such player.
     * @param name Name of the player to find.
     * @return Player found or null if none.
     */
    @Override
    public Player findPlayer(String name) {
        for(Player player : this.playerList) {
            if(player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }
    
    /**
     * Allows a player to join the game.  If a player with the same name (case-insensitive)
     *  is already in the game, then this returns false.  Otherwise, adds a new player of 
     *  that name to the game.  The next step is non-coordinated, waiting for the player
     *  to open a socket for message events not initiated by the player (ie. other player actions)
     * @param name
     * @return Player is player is added, null if player name is already registered to someone else
     */
    @Override
    public Player joinGame(String name) {
        // Check to see if the player of that name is already in game.
        Player newPlayer;
        if(this.playerList.findPlayer(name) == null) {
            // New player, add them to the list and return true.
            newPlayer = new Player(name);
            this.playerList.addPlayer(newPlayer);
            dailyLogger.write(newPlayer.getName() + " joined the server");
            
            // New player starts in a room.  Send a message to everyone else in that room,
            //  that the player has arrived.
            this.broadcast(newPlayer, newPlayer.getName() + " has arrived.");
            return newPlayer;
        }
        // A player of that name already exists.
        return null;
    }
   
    /**
     * Returns a look at the area of the specified player.
     * @param playerName Player Name
     * @return String representation of the current area the player is in.
     */
    @Override
    public String look(String playerName) {
        Player player = playerList.findPlayer(playerName);

        if(player != null) {        
            // Find the room the player is in.
            Room room = this.map.findRoom(player.getCurrentRoom());

            // Send a message to all other players in the room that this player is looking around.
            this.broadcast(player, player.getName() + " takes a look around.");
            dailyLogger.write(player.getName(), "LOOK", room.getTitle());

            // Return a string representation of the room state.
            return room.toString(this.playerList, player);
        }
        // No such player exists
        else {
            return null;
        }
    }        
    
    /**
     * Says "message" to everyone in the current area.
     * @param name Name of the player to speak
     * @param message Message to speak
     * @return Message showing success.
     */
    @Override
    public String say(String name, String message) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            this.broadcast(player, player.getName() + " says, \"" + message + "\"");
            dailyLogger.write(player.getName(), "SAY", message, map.findRoom(player.getCurrentRoom()).getTitle());
            return "You say, \"" + message + "\"";
        }
        else {
            return null;
        }
    }  
    
    /**
     * Attempts to walk towards <direction> 1 time.  If unable to make it all the way,
     *  a message will be returned.  Will display LOOK on any partial success.
     * @param name Name of the player to move
     * @param distance Number of rooms to move forward through.
     * @return Message showing success.
     */
    public String move(String name, Direction direction) {
        Player player = this.playerList.findPlayer(name);
        if(player == null) {
            return null;
        }
        Room room = map.findRoom(player.getCurrentRoom());
        if(room.canExit(direction)) {
            this.broadcast(player, player.getName() + " has walked off to the " + direction);
            player.getReplyWriter().println(room.exitMessage(direction));
            player.setCurrentRoom(room.getLink(direction));
            String logMessage = String.format("%s used command MOVE %s [moved from %s to %s]", player.getName(), direction.toString(), room.getTitle(), map.findRoom(player.getCurrentRoom()).getTitle());
            dailyLogger.write(logMessage);
            this.broadcast(player, player.getName() + " just walked into the area.");
            player.getReplyWriter().println(this.map.findRoom(player.getCurrentRoom()).toString(playerList, player));
        } else {
            String logMessage  = String.format("%s used command MOVE %s [unable to move in direction]", player.getName(), direction.toString());
            dailyLogger.write(logMessage);
            player.getReplyWriter().println(room.exitMessage(direction));
            return "You grumble a little and stop moving.";
        }
        return "You stop moving and begin to stand around again.";
    }
    
    /**
     * Attempts to pick up an object < target >. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @param target The case-insensitive name of the object to pickup.
     * @return Message showing success. 
     */    
    public String pickup(String name, String target) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            String object = room.removeObject(target);
            if(object != null) {
                player.addObjectToInventory(object);
                this.broadcast(player, player.getName() + " bends over to pick up a " + target + " that was on the ground.");
                dailyLogger.write(player.getName(), "PICKUP", target, room.getTitle());
                return "You bend over and pick up a " + target + ".";
            }
            else {
                this.broadcast(player, player.getName() + " bends over to pick up something, but doesn't seem to find what they were looking for.");
                dailyLogger.write(player.getName(), "PICKUP", target, room.getTitle());
                return "You look around for a " + target + ", but can't find one.";
            }
        }
        else {
            return null;
        }
    }       

    /**
     * Attempts to pick up all objects in the room. Will return a message on any success or failure.
     * @param name Name of the player to move
     * @return Message showing success. 
     */    
    public String pickupAll(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            LinkedList<String> objects = room.removeAllObjects();
            if(objects != null && objects.size() > 0) {
                for (String object : objects)
                {
                    player.addObjectToInventory(object);
                }
                this.broadcast(player, player.getName() + " bends over to pick up all objects that were on the ground.");
                dailyLogger.write(player.getName(), "PICKUPALL", room.getTitle());
                return "You bend over and pick up all objects on the ground.";
            }
            else {
                this.broadcast(player, player.getName() + " bends over to pick up something, but doesn't find anything.");
                dailyLogger.write(player.getName(), "PICKUPALL", room.getTitle());
                return "You look around for objects but can't find any.";
            }
        }
        else {
            return null;
        }
    }       

    /**
     * Attempts to erase the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardErase(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            room.whiteboardErase();
            this.broadcast(player, player.getName() + " erases the text on the whiteboard.");
            return "You erase the text on the whiteboard.";
        }
        else {
            return null;
        }
    }

    /**
     * Attempts to read the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardRead(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            Room room = map.findRoom(player.getCurrentRoom());
            String text = room.getWhiteboardText();

            this.broadcast(player, player.getName() + " reads the text on the whiteboard.");
            if (text != null && !text.isEmpty()) {
                return "The whiteboard says: \"" + text + "\".";
            }
            else {
                return "The whiteboard is empty";
            }

        }
        else {
            return null;
        }
    }

    /**
     * Attempts to  the whiteboard in the room. Will return a message on any success or failure.
     * @param name Name of the player to erase the whiteboard
     * @param text Text to write on the whiteboard
     * @return Message showing success. 
     */    
    public String whiteboardWrite(String name, String text) {
        try {
            Player player = this.playerList.findPlayer(name);
            if(player != null) {
                Room room = map.findRoom(player.getCurrentRoom());
                boolean success = room.addWhiteboardText(text);
                if (success) { 
                    this.broadcast(player, player.getName() + " writes on the whiteboard.");
                    return "You write text on the whiteboard.";
                }
                else {
                    this.broadcast(player, player.getName() + " tries to write on the whiteboard, but it's full.");
                    return "You try to write text on the whiteboard, but there's not enough space.";
                }
            }
            else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Returns a string representation of all objects you are carrying.
     * @param name Name of the player to move
     * @return Message showing success.
     */    
    @Override
    public String inventory(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            this.broadcast(player, "You see " + player.getName() + " looking through their pockets.");
            dailyLogger.write(player.getName(), "INVENTORY", map.findRoom(player.getCurrentRoom()).getTitle());
            return "You look through your pockets and see" + player.viewInventory();
        }
        else {
            return null;
        }
    }    

     /**
     * Leaves the game.
     * @param name Name of the player to leave
     * @return Player that was just removed.
     */    
    @Override
    public Player leave(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            this.broadcast(player, "You see " + player.getName() + " heading off to class.");
            this.playerList.removePlayer(name);
            dailyLogger.write(player.getName() + " logged out");
            return player;
        }
        return null;
    }
 
}
