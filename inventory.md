# Items and Inventory

##Items in CSV - Feature 501
### User
All the items available in the game can be accessed by the following .CSV file:
[Items in .CSV file](./src/items.csv).
Each item is denoted on the file with its name, followed by the associated weight and price. The values represented in the .CSV reflect exactly the object properties of that item in the game. 

### Programmer
The .CSV file from above is utilized in 
[Item reading](./src/GameCore.java).
A .CSV file (comma-separated values) is a file structured in a way such that values are delimited with commas and line breaks. This helps when reading input from the file. Each line will represent another item, and the line can be tokenized into separate values per item by taking advantage of the commas. If more items are desired, the .CSV file can be edited by by adding a new line and entering the information of the item in the name,weight,price order (without spaces in between the commas).


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


##Item Weights - Feature 503
### User
The weights associated with each item is located in
[Items in .CSV file](./src/items.csv)
The weights for an item are determined by a generic category of light, medium, or heavy. Liight items all have the weight of 0.5. (for example, Gum and Phone would have the same weight of 0.5). Medium items like a Textbook or a Backpack are given weights of 5 or 10. A much heavier item like a Dog or a Human may have weights of 100 or 200. While the unit for weights are not pounds, the value of weight for an object indicates the general category the object would fall into for light, medium or heavy. Items can be sorted by weight during the game, if the user wishes to arrange their inventory from heaviest to lightest or lightest to heaviest is also available.

[Example of inventory with weights](./Weights.png)

### Programmer
To edit the weights, the developer only needs to change the first value after the comma following the relevant object's name. To change/manipulate the weight property of the object, first study the item object class.
[item](./src/items.java)
The weight is read from the .CSV and represented as a double. It can be accessed by the getWeight() function, which returns the double value of weight. A changeWeight(double weight) method can be used to set the weight.

