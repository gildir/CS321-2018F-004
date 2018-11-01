# CS321-2018F-004
Our team is responsible for the shops and money aspects of the game. The shops are setup for client and server interactions, allowing for buying and selling, each item bought and sold being stored in the shop. Players buy and sell items with money, which they can also send to each other using Venmo.

&nbsp;



# Shops


## Shop Architecture
###### _by Isaiah_

### For Developers
In the code, there are two classes that control shops: the Shop class and the ShopClient class. The Shop class is the server-side class responsible for keeping track of things like which players are in the shop, what the shop's inventory is, and which items are "on sale" or "in demand". As this is the server-side class representation of the shop, there should only ever be one instance of it at a time unless more shops are added to the game.

The ShopClient class is a way for the player to interact with the server-side Shop class. Any time a player enters a shop, a ShopClient object is generated to listen to their commands and send them to the Shop class they are interacting with. Because neither class is serializable, the methods can only have Strings and primitive-type return values. The below UML diagram further illustrates the relationship between shops.

![Shop and ShopCLient CLass Diagram](shop_class_diagram.png?raw=true "Shop and ShopClient Class Diagram")
###### _Shop and ShopClient Class Diagram (Isaiah)_

### For Users
In the game, when you use the "shop" command, you walk up to the store kiosk and can buy or sell your items. Just like in real life, you will still hear the hustle and bustle going on around you, but the only commands you can use are the shop commands (interacting with the outside world would be rude to the clerk).  
  
The most useful commands you will want to remember are "buy \[item\]", "sell \[item\]" and "exit". This readme will elaborate on the uses and implimentations of those commands and more. 




## Buying
_by Isaiah_

### For Developers
The buy method requires a client to interact with the server. As such, it's done through the ShopClient class which sends a serializable string to the server that represents the item the user would like to buy as well as the user's unique name. On the server end, it makes sure that the item is in the store and that the user has enough amount of money for the transaction, and if they do, deducts the appropriate amount of money from the player's money field, adding the item to their inventory.   

### For Users
Once in the shop, type "buy \[item\]" to buy an item from the shop's inventory. So long as you can afford it, the item will then appear in your inventory. One thing to keep in mind is that the buy method is case sensitive, so make sure the name matches exactly. This is a bug that will be worked out in future releases.




## Selling
###### _by Isaiah_

### For Developers 
Selling works in much the same way as buying. It's initiated on the client side, but must interact with the server, so again, it must be done through a ShopClient. The ShopClient sends serializable information about the item the user would like to sell, as well as the user's name, and then adds the appropriate amount of money to their money field. It also checks that the user actually has the item they are attempting to sell, initially to avoid fraud, but in the advent of the Item class, to also avoid Null Pointer Exceptions. 

### For Users
Once in the shop, type the command "sell \[item\]" to sell in item in your inventory. If you have that item in your inventory, the store will tell you how much it was purchased for and add the appropriate amount of money to your wallet. Much like the "buy" command, be cautious. It is case sensitive and will alert you that you don't have an item if the name doesn't match exactly. Be  sure that you want to sell it, since you can't get it back without paying the shop's markup.

![A flowchart that represents the procedure of buying and selling.](Flow_of_infoBuyAdnSell.jpg?raw=true "Buy and sell")

###### _Buy and Sell Flowchart (Rish)_




## Shop Inventory
###### _by Riley_

### For Developers
The shop's inventory is implemented as a linked list in the Shop class server side, currently of type Object, as the Item class has yet to be created. Currently items are added as a string, so the inventory is essentially a list of strings. The list also currently has no explicit bounds, so as many items as can fit will be added to the inventory, depending on player activity.

An associated method getObjects() prints returns a string for printing of the shop's inventory, presented in a readable format listing it's name and price in an itemized list, each item and it's associated price printed on individual lines. If there aren't any items in the shop's inventory, an appropriate message is return instead.

This method is called in getShopInv in GameCore, which is used client side in ShopClient's getInv() method to allow players to enter the INVENTORY command to print a message containing the formatted string with items and prices.


### For Users
When you've entered a shop, type in the INVENTORY command (I or INV) to see a list of the items and associated price currently in the shop's inventory.

**Future Plans**
Future additions may switch this to a list of type Item, and to limit the size of this list to 10 Item objects, including only the last 10 objects added to the list, effectively a queue.

&nbsp;

&nbsp;




# Money
###### _by Riley_

### For Developers
Money is currently simply implemented as a field in the Player class of type float. This is to account for the whole dollars and cents that can comprise a players balance. This value is altered through simple getters and setters, getMoney() and setMoney(float m).

![Player Class Diagram re:Money](player_class_dia_rough_for_money.png?raw=true "Player Class Diagram re:Money")
###### _Player Class Diagram re:Money (Riley)_

The player's balance is printed out via the wallet command, parsed in GameClient to call wallet with the player's name. The wallet method in GameCore takes in a name and returns a string representation of the return value from getMoney(), culled to two decimal places for appropriate represenation of cents.

**Future Plans**

Future improvements may include representing money as individual bills and coins. Implementation to be determined.

### For Users
In the beginning of the game, you start off with a balance of $0.00. As you gain money from other players or through trading, you can see where your current balance is at using the wallet command.

To reiterate, you can gain money by finding items and then selling them to the shop, or by the philanthropy of a friend or kind aristocrat via Venmo. You can in turn spend this money on items in the shop, or grace other players with your money via Venmo.

&nbsp;

&nbsp;




# Venmo
###### _by Abdullah_

Venmo provides a way for users to exchange in-game money.

## Sending Money
### For Developers
Venmo has the following method: `public Static String send(Player from, Player to, float amount)` which has the following contract:

**Preconditions:**
- The sender `Player from` must be non null.
- The recipient `Player to` must be non null.

**Postconditions:**
- The amount is rounded to the nearest two decimals.
- The transaction is considered valid if the recipient is different from the sender, and the amount above 0, and the sender has enough money.
- If the transaction is valid, a success message will be printed to the recipient, and another success message will be returned to the sender.
- If the transaction is invalid, an error message will be returned to the sender (based on the order above).

![A flowchart that represents the procedure of sending money.](VenmoDiagram.png?raw=true "Venmo.send(\) Flowchart")
###### _Venmo Flowchart (Abdullah)_

### For Users
For unification and future proofing, usage instructions are provided by the following method:  
`public static String instructions()`  
When this method is called, it returns a String containing instructions on how to use Venmo as a player.


**Future Plans**

The future plans are to give the recipients to approve or reject an incoming transaction, and to change the usage instructions accordingly.

