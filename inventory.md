# Items and Inventory

## Item Object - Feature 502
### User
Items in the game are bundled together to hold the item name, the item weight, and the item price.

### Programmer
The overall item object is located at
[item](./src/items.java)
The items are custructed as a basic java object holding a string and two double values.  The constructor takes the values of these variables as parameters to create the object.  Functions avalible to the item object are to set and get all three variables, a toString to print the item out (name and weight), and a comparitor that allows for objects to be compaired to one another. Items can easily be expanded to hold other values. 

## Item Drop Table and Master List - Feature 502
### User
- This section is not required to be altered by the user.
### Programmer
[Items in .CSV file](./src/items.csv)
The items for the game drop table are listed in the .CSV file above.  The items can be expanded on or subtracted from in .CSV form.  The .CSV file starts with the first row being a heading for the colloums.  The first colloum is the item name in the form of a String, the second colloum is the item weight (pounds) represented as a double, and the final colloum is the item value (for the store) as a double. The items must be written in this order, as it is hardcoded into the item read. 

Item reading can be found in 
[Item reading](./src/GameCore.java) starting in line .  
This code is written to read the Item name, weight, and value (String/double/double).  This then constructs a new item object and adds it to the overall game drop table arraylist. 
