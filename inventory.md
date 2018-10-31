# Items and Inventory - Team 1

## Items in CSV - Feature 501 - Saru
### User
All the items available in the game can be accessed by the following .CSV file:
[Items in .CSV file](./src/items.csv).
Each item is denoted on the file with its name, followed by the associated weight and price. The values represented in the .CSV reflect exactly the object properties of that item in the game. 

### Developer
The .CSV file from above is utilized in 
[Item reading](./src/GameCore.java).
A .CSV file (comma-separated values) is a file structured in a way such that values are delimited with commas and line breaks. This helps when reading input from the file. Each line will represent another item, and the line can be tokenized into separate values per item by taking advantage of the commas. If more items are desired, thn thee .CSV file can be edited by adding a new line and entering the information of the item in the name, weight, price order (without spaces in between the commas).

## Item Object - Feature 502 - shull4
### User
Items in the game are bundled together to hold the item's name, weight, and price.

### Developer
The overall item object is located at
[item](./src/items.java)
The items are custructed as a basic java object holding a string and two double values.  The constructor takes the values of these variables as parameters to create the object.  Functions avalible to the item object are to set and get all three variables and a toString to print the item out (name and weight). Items can easily be expanded to hold other values. 

NOTE:  This is a deviation from the original core of the game, where items were labeled as Strings only.  The Strings representing items were changed to Item objects for the game inventory, the player inventory, and all assosiated calls to player functions that search for and returned Item Strings. This includes:  Pickup, Look, Inventory, and the random student drop.

## Item Drop Table and Master List - Feature 502 - shull4
### User
- This section is not required to be altered by the user.
### Developer
[Items in .CSV file](./src/items.csv)
The items for the game drop table are listed in the .CSV file above.  The items can be expanded on or subtracted from in .CSV form.  The .CSV file starts with the first row being a heading for the colloums.  The first colloum is the item name in the form of a String, the second colloum is the item weight (pounds) represented as a double, and the final colloum is the item price (for the store) as a double. The items must be written in this order, as it is hardcoded into the item read. Additional items may be added or current items may be removed, as long as all the fields for each item or added completely or removed completely. No blank lines are permitted in the .CSV.

Item reading can be found in 
[Item reading](./src/GameCore.java) starting in line .  
This code is written to read the Item name, weight, and value (String/double/double).  This then constructs a new item object and adds it to the overall game drop table arraylist.


## Item Weights - Feature 503 - Saru
### User
The weights associated with each item is located in
[Items in .CSV file](./src/items.csv)
The weights for an item are determined by a generic category of light, medium, or heavy. Light items all have the weight of 0.5. (for example, Gum and Phone would have the same weight of 0.5). Medium items like a Textbook or a Backpack are given weights of 5 or 10. A much heavier item like a Dog or a Human may have weights of 100 or 200. While the unit for weights are not pounds, the value of weight for an object indicates the general category the object would fall into for light, medium or heavy. Items can be sorted by weight during the game, if the user wishes to arrange their inventory from heaviest to lightest or lightest to heaviest is also available.

[Example of inventory with weights](./Weights.png)

### Developer
To edit the weights, the developer only needs to change the first value after the comma following the relevant object's name. To change/manipulate the weight property of the object, first study the item object class.
[item](./src/items.java)
The weight is read from the .CSV and represented as a double. It can be accessed by the getWeight() function, which returns the double value of weight. A changeWeight(double weight) method can be used to set the weight.

[Item reading](./src/GameCore.java) 
This code is written to read the Item's name, weight, and price (String/double/double).  This then constructs a new item object and adds it to the overall game drop table arraylist.  The heading row of the .CSV file is consumed and discarded. Any deviation to the item attributes (either adding or removing attributes) must be accounted for in the hard coding of reading the .CSV. 

Reading the .CSV uses java.util scanner, java.io exceptions, and java.io file. java.util arraylist was used to store the items into the game item drop table.  These were imported into [GameCore](./src/GameCore.java)

NOTE:  If there is any IO error, the default item list will be loaded into the game drop table. This is found in the catch of the .CSV reading. Reading of the .CSV was a deviation to the original game code. The original item table is loaded in the case of an IO error. Additionally, the games drop table was changed from a static array to a arraylist. 

## Inventory Size Limit - Feature 504 - Mike
## User
Players inventory has been decreased from infinity to 10.
### Developer
The code for the inventory is located in [GameCore](./src/GameCore.java) under the String pickup method. An if statement restricts pickup to the inventory being less than 10, once 10 items in the inventory is reached, the pickup feature defaults to failure.


## Dropping items - Feature 505 - Jae-Moon Hwang
### User
In order to use the drop feature, it is very similar to the pickup feature. The keyword `drop` must be followed by an item name (e.g. `drop flower`). If there are multiple items of the same name, then only the first instance of the item is dropped.
### Developer
The code for the drop feature is very close to the code for the pickup feature. When a player initiates a drop, the name of the player and the name of the item are used as parameters. The parameters are passed through the drop method in GameCore. The method looks up which room the player is in and then tries to remove the named item from the player's inventory. If the named item is not found, it simply tells the player that they do not have any number of the named item. If an instance of the named item is found, then it removes the first instance of the named item from the player's inventory and tries to add it to the room's inventory.

## Offering an Item - Feature 506 - Mike
### User
Users are not able to offer an item to other players. Simply type `O` or `Offer`, target player's name, and name of item to offer (e.g. `Offer Player1 Sword`) and request will be made. The item is removed from your inventory until player either accpets or rejects your offer.

Players are not able to offer to someone not in your room, yourself, or players not in the game. Also, you are unable to offer items not currently held in your inventory.
### Developer
The code for the offer feature is located in GameCore and has many checks for validity. Checks for valid offer target, target player be co-located as offer initiator, and item being currently held by initiator. If any checks fail, the item is returned to the players inventory and the offer is cancelled.

## Trade Feature, Single Instance - Feature 509 - Mike
### User
A new trade feature is put into the game allow players to be engaged in one trade at a time. Trades are initiated and accepted/declined in real time and while in a trade, no other offers or requests can get through. To initiate a trade, follow directions in the menu or simply type `R_Trade Playerx`. Players will be prompted to accept or decline a trade and upon completion, can engage in new trades or gameplay.
### Developer
Code is implemented to request and accept/reject trade request within game. The methods used are `requestPlayer` and `playerResponse`. Requests and responses are issued by players to initiate trades. Players will have their `setInTrade` flag tripped while in a trade and it will be reset once trades are finished. Use of `getReplyWriter` to get responses from other players is used. Trade flags will be shell and logic code for the offer/accept/reject methods further in the game.

## Sorting player's inventory - Feature F03 - Jae-Moon Hwang
### User
In order to intiate a sort on a player's inventory, use the keyword `sort`. Once initiated, the player will have the option to sort by three categories: name, weight, and price. After selecting the category, the player is then asked if they want to sort in an ascending order or descending order.
### Developer
The sort feature has a few pieces to its code. When the player initiates a sort, the player will be prompted on their client to sort by categories and ascending or descending order. The input it then concatenated into one string of two letters as both the category and order are indicated by one letter each. This string represents the mode of the sort and passes the string through the client to the sort method found in the Player class. If the user does not provide the correct format for sorting, the game will keep asking until it obtains valid inputs from the user. Name, price, and weight are sorted using their own comparator and can be found in the Player class. Name is sorted in lexicographical order based on java's String compareTo method. Both price and weight are sorted using normal means which are comparing the actual numerical values and using operators for comparison.
