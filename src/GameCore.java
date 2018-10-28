
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.LinkedList;
import java.io.IOException;


/**
 *
 * @author Kevin
 */
public class GameCore implements GameCoreInterface {

	private final PlayerList playerList;
	private final Map map;
	private Ghoul ghoul;

	/**
	 * Creates a new GameCoreObject. Namely, creates the map for the rooms in the
	 * game, and establishes a new, empty, player list.
	 * 
	 * This is the main core that both the RMI and non-RMI based servers will
	 * interface with.
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
				String[] objects = { "Flower", "Textbook", "Phone", "Newspaper" };
				while (true) {
					try {
						Thread.sleep(rand.nextInt(60000));
						object = objects[rand.nextInt(objects.length)];
						room = map.randomRoom();
            
            try{
						  room.addObject(object);
              GameCore.this.broadcast(room, "You see a student rush past and drop a " + object + " on the ground.");
            }
            catch (IndexOutOfBoundsException e) {
              GameCore.this.broadcast(room, "You see a student rush past.");
            }

					} catch (InterruptedException ex) {
						Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		});

		// new thread awake and control the action of Ghoul.
		// team5 added in 10/13/2018
		Thread awakeDayGhoul = new Thread(new Runnable() {
			@Override
			public void run() {
				Random rand = new Random();
				Room room = map.randomRoom();
				ghoul = new Ghoul(room.getId());
				room.hasGhoul = true;
				GameCore.this.broadcast(room, "You see a Ghoul appear in this room");

				while (true) {
					try {
						// Ghoul move in each 10-15 seconds.
						Thread.sleep(12000 + rand.nextInt(5000));

						// make Ghoul walk to other room;
						GameCore.this.ghoulWander(ghoul, room);
						room.hasGhoul = false;
						GameCore.this.broadcast(room, "You see a Ghoul leave this room");
						room = map.findRoom(ghoul.getRoom());
						room.hasGhoul = true;
						GameCore.this.broadcast(room, "You see a Ghoul enter this room");


                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameObject.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
		
        objectThread.setDaemon(true);
        awakeDayGhoul.setDaemon(true);
        objectThread.start();
        awakeDayGhoul.start();
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
            this.broadcast(player, player.getName() + " just walked into the area.");
            Ghost g = new Ghost(player);
			    	g.start();
            player.getReplyWriter().println(this.map.findRoom(player.getCurrentRoom()).toString(playerList, player));
        } else {
            player.getReplyWriter().println(room.exitMessage(direction));
            return "You grumble a little and stop moving.";
        }
        return "You stop moving and begin to stand around again.";
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
                return "You bend over and pick up all objects on the ground.";
            }
            else {
                this.broadcast(player, player.getName() + " bends over to pick up something, but doesn't find anything.");
                return "You look around for objects but can't find any.";
            }
        }
        else {
            return null;
        }
    }       
	

    @Override
    public String challenge(String challenger, String challengee){
        Player playerChallenger = this.playerList.findPlayer(challenger);
        Player playerChallengee = this.playerList.findPlayer(challengee);
        playerChallengee.setChallenger(challenger);
        playerChallengee.setHasChallenge(true);
        if(playerChallenger != null && playerChallengee != null && playerChallenger != playerChallengee && playerChallenger.getCurrentRoom() == playerChallengee.getCurrentRoom()) {
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
        if(playerChallengee.getChallenger() != " " && playerChallengee.getHasChallenge() == true){
            playerChallengee.setChallenger(" ");
            playerChallengee.setHasChallenge(false); 
            if(playerChallenger != null && playerChallengee != null && playerChallenger != playerChallengee && playerChallenger.getCurrentRoom() == playerChallengee.getCurrentRoom()) {
                playerChallenger.getReplyWriter().println(playerChallengee.getName() + " accepts your challenge to a R-P-S");
                return "You accept " + playerChallenger.getName() + "\'s challenge to a R-P-S.";
            }
            else if(playerChallenger == playerChallengee)
                return "You can't challenge yourself to R-P-S.";
            else {
                return "This person is not in the same room as you or doesn't exist in the game.";
            }
        }
        return "You have not been challenged by " + playerChallenger.getName();
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
	public void ghoulWander(Ghoul g, Room room) {
		Random rand = new Random();
		int[] candinateRoom = new int[4];

		// easiest way to get all possible room;
		candinateRoom[0] = room.getLink(Direction.NORTH);
		candinateRoom[1] = room.getLink(Direction.SOUTH);
		candinateRoom[2] = room.getLink(Direction.WEST);
		candinateRoom[3] = room.getLink(Direction.EAST);

		// random walk.
		while (true) {
			int roomID = candinateRoom[rand.nextInt(4)];
			if (roomID != 0) {
				g.setRoom(roomID);
				return;
			}
		}
    }

	public String bribeGhoul(String playerName, String item){
		item = item.toLowerCase();
		Player player = playerList.findPlayer(playerName);
		Room room = this.map.findRoom(player.getCurrentRoom());
		if(player == null){
			return null;
		}
		if(room.hasGhoul){
			LinkedList<String> itemList = player.getCurrentInventory();
			boolean giveAble = false;
			for (String thing : itemList){
				if(thing.equalsIgnoreCase(item)){
					giveAble = itemList.remove(thing);
					break;			
				}
			}

			if(giveAble){
				try {
					GhoulLog myLog = new GhoulLog();
					myLog.glLog("GameCore","bribeGhoul", "Player" + " " + playerName + " has just given a " + item + " to the Ghoul");
				} catch (Exception e){
					e.printStackTrace();
				}
				
				ghoul.modifyAngryLevel(-1);
				int angryLv = ghoul.getAngryLevel();
				String message = "Ghoul gets " + item + ", " + "and its anger level decreases to " + angryLv + ".";
				return  message;
			}else{
				return "Do not have this item......";
			}
		}else{
			return "No Ghoul in this room";
		}
		
	}

	public String pokeGhoul(String playerName) {
		Player player = playerList.findPlayer(playerName);
		Room room = this.map.findRoom(player.getCurrentRoom());

		if (player != null) {
			if (!room.hasGhoul) {
				return "There is no ghoul in this room.";
			}

			try {
				GhoulLog myLog = new GhoulLog();
				myLog.glLog("GameCore","pokeGhoul", "Player" + " " + playerName + " has just poked the Ghoul");
			} catch (Exception e){
				e.printStackTrace();
			}
			
			ghoul.modifyAngryLevel(1);
			int angerLvl = ghoul.getAngryLevel();
			if (angerLvl >= 7) {
				ghoul.Drag(player);
				draggedToSpawn(player);
			}
			return ("Ghoul anger level has increased to " + angerLvl);
		} else {
			return null;
		}
	}

	//Same functionality as bribe_ghoul, not currently used
	public String giveToGhoul(String object, String playerName) {
		Player player = playerList.findPlayer(playerName);
		Room room = this.map.findRoom(player.getCurrentRoom());
		boolean isItem = false;
		
		if (player != null) {
			if (!room.hasGhoul) {
				return "There is no ghoul in this room.";
			}
			else {
				for (String s : player.getCurrentInventory()) {
					if (s.equals(object)) {
						isItem = true;
					} 
				}
				if (! isItem) {
					return "you don't have" + object + "!";
				}
				player.getCurrentInventory().remove(object);
				ghoul.modifyAngryLevel(-1);
				return("the ghoul is a little more calm");
			}
		}
		else {
			return "failed to give ghoul item.";
		}
	}

	/**
	 * Broadcasts a message to all other players in the same room as player.
	 * 
	 * @param player  Player initiating the action.
	 * @param message Message to broadcast.
	 */
	@Override
	public void broadcast(Player player, String message) {
		for (Player otherPlayer : this.playerList) {
			if (otherPlayer != player && otherPlayer.getCurrentRoom() == player.getCurrentRoom()) {
				otherPlayer.getReplyWriter().println(message);
			}
		}
	}

	/**
	 * Broadcasts a message to all players in the specified room.
	 * 
	 * @param room    Room to broadcast the message to.
	 * @param message Message to broadcast.
	 */
	@Override
	public void broadcast(Room room, String message) {
		for (Player player : this.playerList) {
			if (player.getCurrentRoom() == room.getId()) {
				player.getReplyWriter().println(message);
			}
		}
	}

	/**
	 * Returns the player with the given name or null if no such player.
	 * 
	 * @param name Name of the player to find.
	 * @return Player found or null if none.
	 */
	@Override
	public Player findPlayer(String name) {
		for (Player player : this.playerList) {
			if (player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}

	/**
	 * Allows a player to join the game. If a player with the same name
	 * (case-insensitive) is already in the game, then this returns false.
	 * Otherwise, adds a new player of that name to the game. The next step is
	 * non-coordinated, waiting for the player to open a socket for message events
	 * not initiated by the player (ie. other player actions)
	 * 
	 * @param name
	 * @return Player is player is added, null if player name is already registered
	 *         to someone else
	 */
	@Override
	public Player joinGame(String name) {
		// Check to see if the player of that name is already in game.
		Player newPlayer;
		if (this.playerList.findPlayer(name) == null) {
			// New player, add them to the list and return true.
			newPlayer = new Player(name);
			this.playerList.addPlayer(newPlayer);

			// New player starts in a room. Send a message to everyone else in that room,
			// that the player has arrived.
			this.broadcast(newPlayer, newPlayer.getName() + " has arrived.");
			return newPlayer;
		}
		// A player of that name already exists.
		return null;
	}

	/**
	 * Returns a look at the area of the specified player.
	 * 
	 * @param playerName Player Name
	 * @return String representation of the current area the player is in.
	 */
	@Override
	public String look(String playerName) {
		Player player = playerList.findPlayer(playerName);

		if (player != null) {
			// Find the room the player is in.
			Room room = this.map.findRoom(player.getCurrentRoom());

			// Send a message to all other players in the room that this player is looking
			// around.
			this.broadcast(player, player.getName() + " takes a look around.");

			// Return a string representation of the room state.
			// return room.toString(this.playerList, player);
			// modified in 2018.10.17, which for player can look ghoul.
			if (room.hasGhoul) {
				String watchGhoul = "\n\nTHERE IS A GHOUL IN THE ROOM!!!!!!\n\n";
				return room.toString(this.playerList, player) + watchGhoul;
			} else {
				return room.toString(this.playerList, player);
			}
		}
		// No such player exists
		else {
			return null;
		}
	}

	/**
	 * Turns the player left.
	 * 
	 * @param name Player Name
	 * @return String message of the player turning left.
	 */
  @Deprecated
	@Override
	public String left(String name) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			// Compel the player to turn left 90 degrees.
			player.turnLeft();

			// Send a message to every other player in the room that the player has turned
			// left.
			this.broadcast(player, player.getName() + " turns to the left.");

			// Return a string back to the calling function with an update.
			return "You turn to the left to face " + player.getCurrentDirection();
		} else {
			return null;
		}
	}

	/**
	 * Turns the player right.
	 * 
	 * @param name Player Name
	 * @return String message of the player turning right.
	 */
  @Deprecated
	@Override
	public String right(String name) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			// Compel the player to turn left 90 degrees.
			player.turnRight();

			// Send a message to every other player in the room that the player has turned
			// right.
			this.broadcast(player, player.getName() + " turns to the right.");

			// Return a string back to the calling function with an update.
			return "You turn to the right to face " + player.getCurrentDirection();
		} else {
			return null;
		}
	}

	/**
	 * Says "message" to everyone in the current area.
	 * 
	 * @param name    Name of the player to speak
	 * @param message Message to speak
	 * @return Message showing success.
	 */
	@Override
	public String say(String name, String message) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			this.broadcast(player, player.getName() + " says, \"" + message + "\"");
			return "You say, \"" + message + "\"";
		} else {
			return null;
		}
	}

	public String draggedToSpawn(Player player) {
		if (player == null) {
			return null;
		}
		this.broadcast(player, player.getName() + "has been knocked out by the ghoul!");
		player.setCurrentRoom(1);
		this.broadcast(player, player.getName() + " woke up in the area");
		player.getReplyWriter().println(this.map.findRoom(player.getCurrentRoom()).toString(playerList, player));
		if (player.getCurrentInventory().size() > 0) {
			Random rn = new Random();
			int rand = rn.nextInt((player.getCurrentInventory().size() - 1) - 0 + 1) + 0;
			player.getCurrentInventory().remove(player.getCurrentInventory().get(rand));
		}
		return "You wake up and find yourself at the clock tower, with a lighter burden.";
	}

	/**
	 * Attempts to walk forward < distance > times. If unable to make it all the
	 * way, a message will be returned. Will display LOOK on any partial success.
	 * 
	 * @param name     Name of the player to move
	 * @param distance Number of rooms to move forward through.
	 * @return Message showing success.
	 */
  @Deprecated
	public String move(String name, int distance) {
		Player player = this.playerList.findPlayer(name);
		if (player == null || distance <= 0) {
			return null;
		}
		Room room;
		while (distance-- != 0) {
			room = map.findRoom(player.getCurrentRoom());
			if (room.canExit(player.getDirection())) {
				this.broadcast(player, player.getName() + " has walked off to the " + player.getCurrentDirection());
				player.getReplyWriter().println(room.exitMessage(player.getDirection()));
				player.setCurrentRoom(room.getLink(player.getDirection()));
				this.broadcast(player, player.getName() + " just walked into the area.");
				Ghost g = new Ghost(player);
				g.start();
				player.getReplyWriter()
						.println(this.map.findRoom(player.getCurrentRoom()).toString(playerList, player));
			} else {
				player.getReplyWriter().println(room.exitMessage(player.getDirection()));
				return "You grumble a little and stop moving.";
			}
		}
		return "You stop moving and begin to stand around again.";
	}

	/**
	 * Attempts to pick up an object < target >. Will return a message on any
	 * success or failure.
	 * 
	 * @param name   Name of the player to move
	 * @param target The case-insensitive name of the object to pickup.
	 * @return Message showing success.
	 */
	public String pickup(String name, String target) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			Room room = map.findRoom(player.getCurrentRoom());
			String object = room.removeObject(target);
			if (object != null) {
				player.addObjectToInventory(object);
				this.broadcast(player,
						player.getName() + " bends over to pick up a " + target + " that was on the ground.");
				return "You bend over and pick up a " + target + ".";
			} else {
				this.broadcast(player, player.getName()
						+ " bends over to pick up something, but doesn't seem to find what they were looking for.");
				return "You look around for a " + target + ", but can't find one.";
			}
		} else {
			return null;
		}
	}

	/**
	 * Returns a string representation of all objects you are carrying.
	 * 
	 * @param name Name of the player to move
	 * @return Message showing success.
	 */
	@Override
	public String inventory(String name) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			this.broadcast(player, "You see " + player.getName() + " looking through their pockets.");
			return "You look through your pockets and see" + player.viewInventory();
		} else {
			return null;
		}
	}

	/**
	 * Leaves the game.
	 * 
	 * @param name Name of the player to leave
	 * @return Player that was just removed.
	 */
	@Override
	public Player leave(String name) {
		Player player = this.playerList.findPlayer(name);
		if (player != null) {
			this.broadcast(player, "You see " + player.getName() + " heading off to class.");
			this.playerList.removePlayer(name);
			return player;
		}
		return null;
	}
}
