


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.StringBuilder;
import java.util.Scanner;
import java.io.File;
import java.util.ArrayList;
import java.io.FileNotFoundException;

/**
 *
 * @author Kevin
 */
public class GameCore implements GameCoreInterface {
    private final PlayerList playerList;
    private final Map map;
    private PrintWriter pw;

    /**
     * Creates a new GameCoreObject.  Namely, creates the map for the rooms in the game,
     *  and establishes a new, empty, player list.
     *
     * This is the main core that both the RMI and non-RMI based servers will interface with.
     */
    public GameCore() throws IOException {

        // Generate the game map.
        map = new Map();

        playerList = new PlayerList();

        pw = new PrintWriter(new FileWriter("chatlog.txt"));
        pw.flush();
        pw.close();

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
            if(otherPlayer != player && !otherPlayer.isIgnoring(player) && otherPlayer.getCurrentRoom() == player.getCurrentRoom()) {
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
            String newMessage = player.filterMessage(message);

            if(player.getCurrentRoom() == room.getId()) {
                //player.getReplyWriter().println(message);
                player.getReplyWriter().println(newMessage);
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
//            this.broadcast(player, player.getName() + " says, \"" + message + "\"");
            try {
                chatLog(player, 0, "\""+message+"\"", "Room " + player.getCurrentRoom());
            } catch (IOException e) {
                System.out.println("Failed to log chat");
            }
            this.sayToAll(message, player);
            String newMessage = player.filterMessage(message);
            return "You say, \"" + newMessage + "\"";
        }
        else {
            return null;
        }
    }

    /**
     * Whispers "message" to a specific player.
     * @param srcName Name of the player to speak
     * @param dstName Name of the player to receive
     * @param message Message to speak
     * @return Message showing success
     */
    public String whisper(String srcName, String dstName, String message){
        Player srcPlayer = this.playerList.findPlayer(srcName);
        Player dstPlayer = this.playerList.findPlayer(dstName);
        String returnMessage;
        if (dstPlayer == null)
            returnMessage = "Player " + dstName + " not found.";
        else if (srcPlayer == null)
            returnMessage = "Message failed, check connection to server.";
        else if (dstPlayer.isIgnoring(srcPlayer))
            returnMessage = "Player " + dstPlayer.getName() + " is ignoring you.";
        else {
            dstPlayer.setLastPlayer(srcName);
//            dstPlayer.getReplyWriter().println(srcPlayer.getName() + " whispers you, " + message);
            dstPlayer.printMessage(srcPlayer, message.substring(1, message.length()-1), "whispers");
            String srcMessage = srcPlayer.filterMessage(message.substring(1, message.length()-1));
//            returnMessage = "You whisper to " + dstPlayer.getName() + ", " + message;
            returnMessage = "You whisper to " + dstPlayer.getName() + ", \"" + srcMessage + "\"";

             try {
                chatLog(srcPlayer, 1, message, dstPlayer.getName());
            } catch (IOException e) {
                System.out.println("Failed to log chat");
            }
        }
        return returnMessage;
    }

    /**
     * Reply "message" to last whisper.
     * @param srcName Name of the player to speak
     * @param message Message to speak
     * @return Message showing success
     */
    public String quickReply(String srcName, String message) {
        Player srcPlayer = this.playerList.findPlayer(srcName);
        Player dstPlayer = this.playerList.findPlayer(srcPlayer.getLastPlayer());
        String returnMessage;
        if (dstPlayer == null)
            returnMessage = "No whisper to reply to.";
        else if (srcPlayer == null)
            returnMessage = "Message failed, check connection to server.";
        else {
        	returnMessage = this.whisper(srcName,dstPlayer.getName(),message);
        }
        return returnMessage;
    }

   /**
     * Player ignores further messages from another Player
     * @param srcName Player making the ignore request
     * @param dstName Player to be ignored
     * @return Message showing success
     */
    public String ignorePlayer(String srcName, String dstName) {
        Player srcPlayer = this.playerList.findPlayer(srcName);
        Player dstPLayer = this.playerList.findPlayer(dstName);
        String returnMessage;
        if (dstPLayer == null)
            returnMessage = "Player " + dstName + " not found.";
        else if (srcPlayer == null)
            returnMessage = "Ignore failed, check connection to server.";
        else if (srcPlayer.getName() == dstPLayer.getName())
            returnMessage = "You cannot ignore yourself! <no matter how hard you try>";
        else if (srcPlayer.isIgnoring(dstPLayer))
            returnMessage = "You're already ignoring " + dstPLayer.getName() + "!";
        else {
            srcPlayer.ignorePlayer(dstPLayer);
            returnMessage = "You're now ignoring " + dstPLayer.getName() + ".";
        }
        return returnMessage;
    }

    /**
     * Player unIgnores further messages from another Player
     * @param srcName Player making the unIgnore request
     * @param dstName Player to be unIgnored
     * @return Message showing success
     */
    public String unIgnorePlayer(String srcName, String dstName) {
        Player srcPlayer = this.playerList.findPlayer(srcName);
        Player dstPLayer = this.playerList.findPlayer(dstName);
        String returnMessage;
        if (dstPLayer == null)
            returnMessage = "Player " + dstName + " not found.";
        else if (srcPlayer == null)
            returnMessage = "Unignore failed, check connection to server.";
        else if (srcPlayer.getName() == dstPLayer.getName())
            returnMessage = "You never ignored yourself in the first place";
        else if (!srcPlayer.isIgnoring(dstPLayer))
            returnMessage = "You aren't ignoring " + dstPLayer.getName() + "!";
        else {
            srcPlayer.unIgnorePlayer(dstPLayer);
            returnMessage = "You're no longer ignoring " + dstPLayer.getName() + ".";
        }
        return returnMessage;
    }

    /**
     * Player displays the list of players that are being ignored
     * @param name Player who's list is being targeted
     * @return The list of players being ignored
     */
    public String getIgnoredPlayersList(String name) {
        Player player = this.playerList.findPlayer(name);
        String returnMessage;
        if(player != null){
            returnMessage = player.getIgnoredPlayersList();
        }else{
            returnMessage = "Error: Could not find player. Check server connection status";
        }
        return returnMessage;
    }

    // Feature 410: Joke
    /**
     * Tells a joke to the room. Reads local "chat config" file
     * that keeps a list of jokes, one per line. The command
     * chooses a random joke.
     * NOTE: Importing Scanners, File, ArrayList, Random, and
     * FileNotFoundException for this method.
     * @param filename the "chat config" file to read the joke from.
     * */
    public String joke(String filename){
      File file = new File(filename);
      try{
      Scanner sc = new Scanner(file);
      // using ArrayList to store jokes in file for randomization.
      ArrayList<String> joke = new ArrayList<String>();

      while (sc.hasNextLine()){
        joke.add(sc.nextLine());
      }

      sc.close();
      Random r = new Random();
      return joke.get(r.nextInt(joke.size()));
      }
      catch (FileNotFoundException e){
        return ("File not found. Please add a joke.");
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

    //Feature 411. Shout
    /**
     * Shouts "message" to everyone in the current area.
     * @param name Name of the player to speak
     * @param message Message to speak
     * @return Message showing success.
     */
    @Override
    public String shout(String name, String message) {
        Player player = this.playerList.findPlayer(name);
        if(player != null){
            for(Player otherPlayer : this.playerList) {
                if(otherPlayer != player && !otherPlayer.isIgnoring(player)) {
                    String newMessage = otherPlayer.filterMessage(message);
                    otherPlayer.getReplyWriter().println(player.getName() + " shouts, \"" + newMessage + "\"");
                }
            }
             try {
                    chatLog(player, 2, "\""+message+"\"", "Everyone");
                } catch (IOException e) {
                    System.out.println("Failed to log chat");
                }
            return "You shout, \"" + message + "\"";
        } else {
            return null;
        }
    }

    private void chatLog(Player player, int chatType, String message, String target) throws IOException {
        pw = new PrintWriter(new FileWriter("chatlog.txt", true));
        String type = "";
        String msg;
        switch(chatType) {
            case 0:
                type = "SAID";
                break;
            case 1:
                type = "WHISPERED";
                break;
            case 2:
                type = "SHOUTED";
                break;
        }
        msg = "PLAYER [" + player.getName() + "] " + type + " (" + message + ") to [" + target + "]\n";
        pw.write(msg);
        pw.flush();
        pw.close();
        return;
    }

    /**
     * Player says 'message' to everyone in the room.
     * @param message Message to send.
     * @param player Player to speak.
     */
    public void sayToAll(String message, Player player) {
        for(Player otherPlayer : this.playerList) {
            if(otherPlayer != player && !otherPlayer.isIgnoring(player) && otherPlayer.getCurrentRoom() == player.getCurrentRoom()) {
                otherPlayer.printMessage(player, message, "says");
            }
        }
    }
}
