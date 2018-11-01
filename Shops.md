# CS321-2018F-004

## Shops in general (for devs)
(Isaiah)
In the code, there are two classes that control shops: the Shop class and the ShopClient class. The Shop class is the server-side class responsible for keeping track of things like which players are in the shop, what the shop's inventory is, and which items are "on sale" or "in demand". As this is the server-side class representation of the shop, there should only ever be one instance of it at a time unless more shops are added to the game.  
The ShopClient class is a way for the player to interact with the server-side Shop class. Any time a player enters a shop, a ShopClient object is generated to listen to their commands and send them to the Shop class they are interacting with. Because neither class is serializable, the methods can only have Strings and primitive-type return values. The below UML diagram further illustrates the relationship between shops.  
![https://imgur.com/VztiLwI](https://imgur.com/VztiLwI "Shop and ShopClient UML Diagram")

## Buy

## Sell

## Inventory
