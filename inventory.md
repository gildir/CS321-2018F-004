# Items and Inventory


## Item Object - Feature 502 - shull4
### User
Items in the game are bundled together to hold the item name, the item weight, and the item price.

### Developer
The overall item object is located at
[item](./src/items.java)
The items are custructed as a basic java object holding a string and two double values.  The constructor takes the values of these variables as parameters to create the object.  Functions avalible to the item object are to set and get all three variables, a toString to print the item out (name and weight), and a comparitor that allows for objects to be compaired to one another. Items can easily be expanded to hold other values. 

NOTE:  This is a deviation from the original core of the game, where items were labled as Strings only.  The Strings for items were changed to Item objects for the game inventory, the player inventory, and all assosiated calls to player functions that search for and returned Item Strings. This includes:  Pickup, Look, Inventory, and the random student drop.

## Item Drop Table and Master List - Feature 502 - shull4
### User
- This section is not required to be altered by the user.
### Developer
[Items in .CSV file](./src/items.csv)
The items for the game drop table are listed in the .CSV file above.  The items can be expanded on or subtracted from in .CSV form.  The .CSV file starts with the first row being a heading for the colloums.  The first colloum is the item name in the form of a String, the second colloum is the item weight (pounds) represented as a double, and the final colloum is the item value (for the store) as a double. The items must be written in this order, as it is hardcoded into the item read. Additional items may be added or current items may be removed, as long as all the fields for each item or added completely or removed completely. No blank lines are permitted in the .CSV.

Item reading can be found in 
[Item reading](./src/GameCore.java) 
This code is written to read the Item name, weight, and value (String/double/double).  This then constructs a new item object and adds it to the overall game drop table arraylist.  The heading row of the .CSV file is consumed and discarded. Any deviation to the item attributes (either adding or removing attributes) must be accounted for in the hard coding of reading the .CSV. 

Reading the .CSV uses java.util scanner, java.io exceptions, and java.io file.   These were imported into [GameCore](./src/GameCore.java)

NOTE:  If there is any IO error, the default item list will be loaded into the game drop table. This is found in the catch of the .CSV reading.


## Dropping items - Feature 505 - Jae-Moon Hwang
### User
In order to use the drop feature, it is very similar to the pickup feature. The keyword `drop` must be followed by an item name (e.g. `drop flower`). If there are multiple items of the same name, then only the first instance of the item is dropped.
### Developer
The code for the drop feature is very close to the code for the pickup feature. When a player initiates a drop, the name of the player and the name of the item are used as parameters. The parameters are passed through the drop method in GameCore. The method looks up which room the player is in and then tries to remove the named item from the player's inventory. If the named item is not found, it simply tells the player that they do not have any number of the named item. If an instance of the named item is found, then it removes the first instance of the named item from the player's inventory and tries to add it to the room's inventory.

## Sorting player's inventory - Feature F03 - Jae-Moon Hwang
### User
In order to intiate a sort on a player's inventory, use the keyword `sort`. Once initiated, the player will have the option to sort by three categories: name, weight, and price. After selecting the category, the player is then asked if they want to sort in an ascending order or descending order.
### Developer
The sort feature has a few pieces to its code. When the player initiates a sort, the player will be prompted on their client to sort by categories and ascending or descending order. The input it then concatenated into one string of two letters as both the category and order are indicated by one letter each. This string represents the mode of the sort and passes the string through the client to the sort method found in the Player class. If the user does not provide the correct format for sorting, the game will keep asking until it obtains valid inputs from the user. Name, price, and weight are sorted using their own comparator and can be found in the Player class. Name is sorted in lexicographical order based on java's String compareTo method. Both price and weight are sorted using normal means which are comparing the actual numerical values and using operators for comparison.
