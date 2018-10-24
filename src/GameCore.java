


import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kevin
 */
public class GameCore implements GameCoreInterface {
    private final PlayerList playerList;
    private final Map map;
    
    /**
     * Creates a new GameCoreObject.  Namely, creates the map for the rooms in the game,
     *  and establishes a new, empty, player list.
     * 
     * This is the main core that both the RMI and non-RMI based servers will interface with.
     */
    public GameCore() {
        
        // Generate the game map.
        map = new Map();
        
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

            // Return a string representation of the room state.
            return room.toString(this.playerList, player);
        }
        // No such player exists
        else {
            return null;
        }
    }        
   
    /**
     * Turns the player left.
     * @param name Player Name
     * @return String message of the player turning left.
     */
    @Override
    public String left(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            // Compel the player to turn left 90 degrees.
            player.turnLeft();
            
            // Send a message to every other player in the room that the player has turned left.
            this.broadcast(player, player.getName() + " turns to the left.");
            
            // Return a string back to the calling function with an update.
            return "You turn to the left to face " + player.getCurrentDirection();
        }
        else {
            return null;
        }
    }
    
    /**
     * Turns the player right.
     * @param name Player Name
     * @return String message of the player turning right.
     */
    @Override
    public String right(String name) {
        Player player = this.playerList.findPlayer(name);
        if(player != null) {
            // Compel the player to turn left 90 degrees.
            player.turnRight();
            
            // Send a message to every other player in the room that the player has turned right.
            this.broadcast(player, player.getName() + " turns to the right.");
            
            // Return a string back to the calling function with an update.
            return "You turn to the right to face " + player.getCurrentDirection();
        }
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
            return "You say, \"" + message + "\"";
        }
        else {
            return null;
        }
    }  
    
    /**
     * Attempts to walk forward < distance > times.  If unable to make it all the way,
     *  a message will be returned.  Will display LOOK on any partial success.
     * @param name Name of the player to move
     * @param distance Number of rooms to move forward through.
     * @return Message showing success.
     */
    public String move(String name, int distance) {
        Player player = this.playerList.findPlayer(name);
        if(player == null || distance <= 0) {
            return null;
        }
        Room room;
        while(distance-- != 0) {
            room = map.findRoom(player.getCurrentRoom());
            if(room.canExit(player.getDirection())) {
                this.broadcast(player, player.getName() + " has walked off to the " + player.getCurrentDirection());
                player.getReplyWriter().println(room.exitMessage(player.getDirection()));
                player.setCurrentRoom(room.getLink(player.getDirection()));
                this.broadcast(player, player.getName() + " just walked into the area.");
                player.getReplyWriter().println(this.map.findRoom(player.getCurrentRoom()).toString(playerList, player));
            }
            else {
                player.getReplyWriter().println(room.exitMessage(player.getDirection()));
                return "You grumble a little and stop moving.";
            }
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
                return "You bend over and pick up a " + target + ".";
            }
            else {
                this.broadcast(player, player.getName() + " bends over to pick up something, but doesn't seem to find what they were looking for.");
                return "You look around for a " + target + ", but can't find one.";
            }
        }
        else {
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
            return "You look through your pockets and see" + player.viewInventory();
        }
        else {
            return null;
        }
    }    

    @Override
    public String challenge(String challenger, String challengee){
        Player playerChallenger = this.playerList.findPlayer(challenger);
        Player playerChallengee = this.playerList.findPlayer(challengee);
	if(playerChallengee == null || playerChallenger == null){
             return "This player does not exist in the game.";
	}
	if(playerChallenger.getInBattle() == true){
	     return "You are already in a R-P-S battle.";
	}
  if(playerChallengee.getInBattle()){
          return "This player is already in a R-P-S battle";
  }
	if(playerChallengee.getInBattle() == true){
	     return playerChallengee.getName() + " is already in a R-P-S battle.";
	}
        if(playerChallenger != playerChallengee && playerChallenger.getCurrentRoom() == playerChallengee.getCurrentRoom()) {
            playerChallengee.setChallenger(challenger);
            playerChallenger.setChallenger(challengee);
            playerChallengee.setHasChallenge(true);
            playerChallengee.getReplyWriter().println(playerChallenger.getName() + " challenges you to a R-P-S");
            return "You challenged " + playerChallengee.getName() + " to a R-P-S.";
         }
        else if(playerChallenger == playerChallengee)
            return "You can't challenge yourself to R-P-S.";
         else {
             return "This person is not in the same room as you or doesn't exist in the game.";
         }
    }

    @Override
    public String accept(String challengee, String challenger){
        Player playerChallenger = this.playerList.findPlayer(challenger);
        Player playerChallengee = this.playerList.findPlayer(challengee);
	      if(playerChallengee == null || playerChallenger == null){
		      return "This player does not exist in the game.";
        }
        if(playerChallengee.getChallenger().equals(playerChallenger.getName()) && playerChallengee.getHasChallenge() == true){
            if(playerChallenger != playerChallengee && playerChallenger.getCurrentRoom() == playerChallengee.getCurrentRoom()) {
                playerChallenger.getReplyWriter().println(playerChallengee.getName() + " accepts your challenge to a R-P-S. \nPick rock, paper, or scissors: ");
                playerChallengee.setHasChallenge(false);
		playerChallengee.setInBattle(true);
		playerChallenger.setInBattle(true);
                return "You accept " + playerChallenger.getName() + "\'s challenge to a R-P-S. \nPick rock, paper, or scissors: ";
            }
            else 
	    {
                return "This person is not in the same room as you or doesn't exist in the game.";
            }
        }
	else if(playerChallenger == playerChallengee){
		return "You can't challenge yourself to R-P-S.";
        }
	else{
		return "You have not been challenged by " + playerChallenger.getName();
	}
    }

    @Override
    public String reject(String challengee, String challenger){
	Player playerChallenger = this.playerList.findPlayer(challenger);
	Player playerChallengee = this.playerList.findPlayer(challengee);
	if(playerChallengee == null || playerChallenger == null){
		return "This player does not exist in the game.";
        }
	if(playerChallengee.getChallenger().equals(playerChallenger.getName()) && playerChallengee.getHasChallenge() == true){
            if(playerChallenger != playerChallengee && playerChallenger.getCurrentRoom() == playerChallengee.getCurrentRoom()) {
                playerChallengee.setChallenger(" ");
                playerChallenger.setChallenger(" ");
                playerChallengee.setHasChallenge(false);
                playerChallengee.getReplyWriter().println(playerChallengee.getName() + " rejects your challenge to a R-P-S");
                return "You reject " + playerChallenger.getName() + "\'s challenge to a R-P-S.";
            }
            else {
                return "This person is not in the same room as you or doesn't exist in the game.";
            }
        }
	else if(playerChallenger == playerChallengee){
		return "You can't challenge yourself to R-P-S.";
        }
	else{
                return "You have not been challenged by " + playerChallenger.getName();
        }
    }	

    @Override
    public String pickRPS(String name,  String option){
        Player player = this.playerList.findPlayer(name);
        Player challengee = this.playerList.findPlayer(player.getChallenger());
        
        if(player.getHasChallenge() == true){
          if(player.getOption().equals("ROCK") || player.getOption().equals("PAPER") || player.getOption().equals("SCISSORS")){
            return "You already pick rock, paper or scissors. You picked " + player.getOption();
          }
          player.setOption(option);
          challengee.setChallengerOption(option);
          String message = "You picked " + option;

          if(challengee.getOption().equals("ROCK") || challengee.getOption().equals("PAPER") || challengee.getOption().equals("SCISSORS")){
            switch(player.getOption()) {
              case "ROCK":
                if (challengee.getOption().equals("PAPER")) {
                  message = challengee.getName() + " wins with " + challengee.getOption();
                } 
                else if (challengee.getOption().equals("ROCK")){
                  message = "It is a tie.";
                }
                else {
                  message = player.getName() + " wins with " + player.getOption();
                }
                challengee.getReplyWriter().println(message);
                player.setInBattle(false);
                player.setChallenger(" ");
                player.setOption("");
                challengee.setChallenger(" ");
                challengee.setInBattle(false);
                challengee.setOption("");
                break;
              case "PAPER":
                if (challengee.getOption().equals("SCISSORS")) {
                  message = challengee.getName() + " wins with " + challengee.getOption();
                } 
                else if (challengee.getOption().equals("PAPER")){
                  message = "It is a tie.";
                }
                else {
                  message = player.getName() + " wins with " + player.getOption();
                }
                challengee.getReplyWriter().println(message);
                player.setInBattle(false);
                player.setChallenger(" ");
                player.setOption("");
                challengee.setChallenger(" ");
                challengee.setInBattle(false);
                player.setOption("");
                break;
              case "SCISSORS":
                if (challengee.getOption().equals("ROCK")) {
                  message = challengee.getName() + " wins with " + challengee.getOption();
                } 
                else if (challengee.getOption().equals("SCISSORS")){
                  message = "It is a tie";
                }
                else {
                  message = player.getName() + " wins with " + player.getOption();
                }
                challengee.getReplyWriter().println(message);
                player.setInBattle(false);
                player.setChallenger(" ");
                player.setOption("");
                challengee.setChallenger(" ");
                challengee.setInBattle(false);
                challengee.setOption("");
                break;
              default:
                break;
            }
          }
          return message;
        }
        else
          return "You are not in a R-P-S challenge.";
    }

    @Override
     public String teach(String player){
         String message = "Here is the Heirarchy of power in R-P-S:\n\tRock beats Scissors\n\tScissors beats Paper\n\tPaper beats Rock\n\nCHALLENGE <name>: \tIf you challenge someone, you must wait for them to accept or reject\nACCEPT/REJECT <name>: \tIf you have been challenge, you must accept or reject the challenge\nYou may not be challenged while in a R-P-S battle\n";
         return message;
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
            return player;
        }
        return null;
    }       
}
