# World Map Documentation

## Team 6


### 101 Convert the rooms.xml to rooms.csv file

This is the csv layout that was converted into a csv file format .

* FORMAT
* numRooms
* roomId,type,title,desc
* direction,exitId,desc
* direction,exitId,desc
* direction,exitId,desc
* direction,exitId,desc

#### Specify World File in Command Line Argument
When running the runServer script, you can specify which .csv file to open to generate the server world. Using the command “runServer.(sh/bat) (file name)”, the server will pass the open the file specified and begin parsing through to generate the world. If the file is not found or is invalid, the server will shutdown. You can run the server without any command line arguments, in which it will load the default world rooms.csv.

The script takes the command line argument and passes it to GameServer where it checks if an argument was passed, and passes either the argument given or the default rooms.csv to GameObject, which passes it directly to GameCore to generate the map. The Map class checks if the file exists, and then begins parsing through it. If at any time it receives invalid input, it will throw an exception and shutdown the server.

![alt text](https://github.com/cwells21/CS321-2018F-004/blob/world_documentation/src/WorldFileReadMe.png)

### 103 Add Rooms to the World (Christopher Wells)
There are three requirements addressed by this update to the game.  The first is the ability for a program to walk a user through setting up rooms for the game.  This is done through a text based interface that asks the user to enter descriptions and relevant details about the room being constructed.  The second requirement addressed by the update is to allow a user to generate a generic grid of interconnected rooms.  The third requirement is for an interface for a algorithm to interface with the update and add rooms in a yet to be defined way.
In the original design the game had three rooms. The first part of the update provides a text based interface that asks the user to enter relevant data about each room. This is allows the user direct control of how the world is layed out.  The primary implementation of this would be in game worlds that do not follow a grid or maze based design.  The update then formats the input into the correct CSV format. 
The second part allows for the rapid generation of generic rooms in a connected grid format.  This allows for the creation of large world maps that have little variation in the rooms.  The user can then just edit the text descriptions to make the world more interesting.  The user can also delete connections and change room definitions in the CSV file to allow for more variation.
The last goal in the implementation of this update is to provide the interface for an algorithm to create much more interesting world file.  
These changes are through a Java program called “addRooms.java”, this program can be easily modified and compled to perform different roles based on user requirements.  It has constructors t allow for user input and saving into a file, reading from a file and appending user input to the file, and auto generating an output file with no user input.  All of these are in the supported CSV format.

![alt text](https://github.com/cwells21/CS321-2018F-004/blob/world_documentation/src/addRoom_UML.jpg)


### 104 Add Feature to Allow for Indoor and Outdoor Rooms (Christopher Wells)
This update required the modification of the csv file to accomidate the addition of an outdoor value.  This then had to be incorporated into the GamcCore object. This addition was to allow for the map to have both indoor and outdoor rooms. This addition of indoor and outdoor rooms should make the game more interesting for the user.  The question then arose as to how we relay the type of room to the user. Is the user supposed to ask if the room was insde or outside. In the end the client decided on having the room declared as it was being entered. The final veriation of this is to represent the inside and utside of the buldings and campus of GMU.  

![alt text](https://github.com/cwells21/CS321-2018F-004/blob/world_documentation/src/105_RoomTypeDisplay.jpg)

### 105_RoomTypeDisplay:     (jorge)
The world in this game gets generated from a csv file. The first parameter in the file is the total number of rooms tol be created. Each room that gets created has a room ID specifier, followed by some other parameters. I chose to add a new room type descriptor as the second parameter after the room ID number. The added tag looks like: “indoor” ,“outdoor”. It will get loaded by the parser and then get used to initialize the room type field inside each room. I modified the Room.java class to account for this change. This features modified the room objects by adding a field used to describe the room type for each player. Up to this point the game displayed the room name by making a call to the toString method for the room Class. I modified this call to be able to insert the room type. Now the room type gets displayed right after the room name as soon as  a player steps inside the room. 

### 106 Make an Algorithm to create new Worlds
This is a pending update to the game.  The algorithm is already been researched, but has not been implemented.  The algorithm chosen wll create dungeon and maze style maps for the game.  

### 107 Slow Down the Rate at Which Student NPC’s run Through the Room (jorge)
The rate at which objects get dropped is controlled by a hardcoded value in GameCore, this value was updated  from 60000 to 900000 , however the frequency with which objects get dropped also depends on the number of rooms. Since later features will include more rooms this value should probably be a value to get specified in the csv file. Also this was a temporary change due the Ghosts and Ghouls team having to override it. This feature should be something to come back to once all the other features have been implemented especially the ones mentioned above. 

![alt text](https://github.com/cwells21/CS321-2018F-004/blob/world_documentation/src/107SlowDownDropRate.jpg)

### 108 Create an in Game Map to Assist in Navigating the World
In the near future, a feature to display an in game map of nearby rooms will be added. A player will be able to type the command “map”, and the game will generate a 3X5 map of nearby rooms and exits. The map show each room you can walk to, and displays exit possible exit out of the room. The actual game map is not guaranteed to be a linear grid, for example, moving east and then west does not guarantee you end up in the room you started in, so visible exits show only that an exit exists in that direction, not necessarily that it goes to the room displayed.



### 109 Item Cap on the Number of Elements dropped in the Room (Shane)
The number of items that could be dropped in a room was limited to 5, but NPCs that are passing by would continue to “drop items” in a room that is already filled to capacity, although no new items dropped while the room is full will appear in the room. This issue was addressed by throwing an index out of bounds exception when adding items to a room that is at capacity. This solution may be used by the team with the task of handling players dropping items to address the similar issue of players dropping items when the room is at capacity, as the out of bounds exception will be thrown if the players attempt to drop items in a room at its item capacity. Should the item capacity of rooms be adjusted in the future, this change should remain unaffected, as the exception is thrown if adding the object fails the check on the item capacity of the room in question.

### 110 Quest NPC in the clock tower room (Shane)
There is a quest NPC in the clock tower room whose job is to give quests to the players, but since the quest system has not been implemented yet, the quest NPC serves no purpose other than its presence. In adding a quest NPC to the clock tower room, a NPC class was created which may be used by other teams in the creation of any NPCs they need to accomplish their goals, such as the pvp team for their rock, paper, scissors tutor NPC. Currently the NPC class only has a name, list of quests, and room they are located in. NPCs may be used as an intermediary between the players and the game system, or provide more interaction with the world through practice pvp, talking, quests and other potential methods of interaction. In the current implementation the quest NPC is placed in the room with an id of 1, as that is the clock tower room, but in later iterations of the game it may be desirable to have the quest NPC in other rooms or have multiple quest npcs for different areas/parts of the game.

![alt text](https://github.com/cwells21/CS321-2018F-004/blob/world_documentation/src/NPC_UML.jpg)


