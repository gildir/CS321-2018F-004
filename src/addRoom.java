import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;

//import LinkedList.Node;

/****************************************************************************
 * @author Christopher Wells
 * cwells21@masonlive.gmu.edu
 * CS 321
 * George Mason University
 * G-00260513
 * 10/10/2018
 * 
 * File Name: addRoom.java
*@version 1.0 Early Beta
*@since 2018/11/7
* Description: This class is responsible for the generation of game maps. The
* class offers three different methods for the creation of maps.  The first is
* the manual entry of information, that s then formatted into the csv format
* to be read by the game engine.   The second is the automated generation of
* a grid style world.  The final method is the dungeon engine.  It is intended 
* that after the generation of the generic map that a design team will then 
* come in and add the test descriptions to the rooms, and designate inside and
* outside.  
* 
***************************************************************************/
 


public class addRoom{

	//Private class variables and constants
	private FileInputStream inputFile = null;	//Sets a the stream for the input file for the case when appending to the file 
    private FileOutputStream outputFile = null;	//Sets the output stream for the file defaulting to Output.csv
    
    private String inputFileName = "";	//String for the customer to enter the name of the string
    private String outputFileName = "";	//String for the outputting of the csv file
    
    private static final String north = "NORTH";	//String constant for defining the North direction in the file
    private static final String east = "EAST";		//String for defining the East direction in the csv file 
    private static final String south = "SOUTH";	//String for defining the South direction in the csv file
    private static final String west = "WEST";		//String for defining the west direction in the csv file
    private static final String outdoorDesignation = "outside";	//File designation of rooms that are outside
    private static final String indoorDesignation = "inside";	//Designation of the inside room for the csv
    private int menuItem = 0;
    
    
    
    /**
	 * <h1> Room Node </h1>
	 * Defines a node that represents a room in the game.  
	 * These nodes are linked to form a list of rooms
	 * @version 1.0 Beta
	 * @since 2018/11/7
	 * @param none
	 * @return none
	 * O(1)
	 */
    private class Node
	{
		Node NextNode;
		
	    private int roomID = 1;
	    private String roomDescription;
	    private char indorOutdoor;
	    
	    private int northConnecting = 0;
	    private String northDescription;
	    private int eastConnecting = 0;
	    private String eastDescription;
	    private int southConnecting = 0;
	    private String southDescription;
	    private int westConnecting = 0;
	    private String westDescription;
	    
	    private int x_val = 0;
	    private int y_val = 0;
		
	}
	
    private int numberOfRooms = 0;
    private Node head = null;
    private Node current = head;
    
    
    
    
    //Constructors
    
    
    /**
	 * <h1> Add Room Constructor </h1>
	 * Currently only constructor active.   
	 * Might add future constructors if needed
	 * @version 1.0 Beta
	 * @since 2018/11/7
	 * @param none
	 * @return Formatted CSV file formatted for the game engine.
     * @throws IOException 
	 * 
	 */
    addRoom() throws IOException
    {
    	
    	this.menuItem = this.displayFirstMenu();	//Calls method to display the options for generating a map
    	this.selectGenerator( this.menuItem);
    	
    }
    
    
    
    
    
    //Getter Methods
    
    /**
   	 * <h1> Display First Menu </h1>
   	 * Displays a menu t the user of the ways to generate a map  
   	 * returns this information back to the constructor
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param none
   	 * @return Int: value of the item selected from the list.
   	 * 
   	 */
    private int displayFirstMenu()
    {
    	int selection = 0;
    	String input = "";
    	Scanner scanner = new Scanner(System.in);
    	System.out.println("Welcome to the map generator, please select from the options below or press 0 to exit");
    	System.out.println("1. Start a new csv file and manually insert the data.");
    	System.out.println("2. Append to an existing csv file and manually enter data.");
    	System.out.println("3. Generate a generic grid map.");
    	System.out.println("4. Generate a dungeon style maze map.");
    	
    	
    	
    	input = scanner.nextLine();
    	selection = Integer.parseInt(input);
    	//System.out.println("Read a: " + selection);
    	
    	if  ( ( selection > 4) || ( selection < 0))
    	{
    		System.out.println("ERROR: SELECTION OUTSIDE OF ACCEPTED RANGE");
    		System.exit(-1);
    	}
    	//scanner.close();
    	return selection;
    }
    
    
    
    
    
    
    
    
    //Setter Methods
    
    /**
   	 * <h1> Define Input File Name </h1>
   	 * Allows for the defining of the name of the file for input  
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param String; Name of the input file in string format
   	 * @return none
   	 * 
   	 */
    private void defineInputFileName(String name)
    {
    	this.inputFileName = name;
    }
    
    /**
   	 * <h1> Define Output File Name </h1>
   	 * Allows for the defining of the name of the file for output  
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param String; Name of the output file in string format
   	 * @return none
   	 * 
   	 */
    private void defineOutputFileName(String name)
    {
    	this.outputFileName = name;
    }
    
    /**
   	 * <h1> Open Input File </h1>
   	 * Method opens the file as an input  
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param none
   	 * @return none
   	 * @throws Exception if the file can not be opened
   	 */
    private void openInputFile()
    {
    	String in = this.inputFileName;
    	try
    	{
    		inputFile = new FileInputStream(in);
    	}
    	catch(Exception e)
    	{
    		System.out.println("ERROR IN THE OPENING OF THE INPUT FILE");
    		System.exit(-1);
    	}
    }
	
    /**
   	 * <h1> Open Output File </h1>
   	 * Method opens the file as an output  
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param none
   	 * @return none
   	 * @throws Exception if the file can not be opened
   	 */
    private void openOutputFile()
    {
    	try
    	{
    		outputFile = new FileOutputStream(outputFileName);
    	}
    	catch(Exception e)
    	{
    		System.out.println("ERROR IN THE OPENING OF THE INPUT FILE");
    		System.exit(-1);
    	}
    }
    
    
    /**
   	 * <h1> Add Node To List </h1>
   	 * Internal method that adds each room description to a linked list until ready to
   	 * write to the file.
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param Node; The room to be added to the list
   	 * @return none
   	 * 
   	 */
    private void addNodeToList(Node temp) 
	{
		// TODO Auto-generated method stub
		if ( this.head == null)	//If this is the first room to be added then this will be placed at the head of the list.
		{
			this.head = temp;
			this.current = temp;
		}
		else	//Otherwise append it to the end of the list.
		{
			this.current.NextNode = temp;
			this.current = temp;
		}
		this.numberOfRooms++;
		
	}
    
    
    /**
   	 * <h1> Program Exit Method </h1>
   	 * Allows the user to select to exit the program 
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param int; representing selection made by the user.
   	 * @return none
   	 * 
   	 */
    private void programExit()
    {
    	System.out.println("Happy trails......................................................");
    	System.exit(0);
    }
    
    //Internal Methods
    
    /**
   	 * <h1> Select Generator </h1>
   	 * Selects the appropriate program execution path based on user
   	 * input.  
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param int; representing selection made by the user.
   	 * @return none
     * @throws IOException 
   	 * 
   	 */
    private void selectGenerator(int sel) throws IOException
    {
    	//System.out.println(sel);
    	switch(sel)
    	{
    		case 0:			programExit();	//Calls method to exit.
    						break;
    		case 1:			manualGenerator();	//Calls the method for the manual generator for the map
    						break;
    		case 2:			manualAppend();	//Calls the method to allow the user to append manually entered data to a file
    						break;
    		case 3:			generateGrid();	//Calls method to generate a generic grid map
    						break;
    		case 4:			generateDugeon();	//Calls the method to generate a maze
    						break;
    		default:		generateError();	//
    	
    	}
    }
    
    /**
   	 * <h1> Generate Error </h1>
   	 * This method generates an error message and starts the menu again
   	 * @version 1.0 Beta
   	 * @since 2018/11/9
   	 * @param none
   	 * @return none
     *  
   	 * 
   	 */
    private void generateError()
    {
    	System.out.println("YOUE HAVE REACHED THIS IN ERROR PLEASE TRY READING THE MENU AND MAKING A SELECTION");
    	this.displayFirstMenu();
    }
   
    
    /**
   	 * <h1> Manual Generator </h1>
   	 * This is the root method for allowing the user to enter the data directly
   	 * into the csv format. 
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param none
   	 * @return none
     * @throws IOException 
   	 * 
   	 */
    private void manualGenerator() throws IOException
    {
    	this.getOutputFileName();	//Call method to get the name of the file to output to
    	this.openOutputFile(); 		//Opens the output file for writing
    	
    	this.getInputFromUser(); 	//Calls method to get information from the user about rooms for the map.
    	this.printToFile();  		//Writes the linked list to the file specified by the user
    	this.programExit();
    	//this.displayFirstMenu();	//Return to the main menu 
    }
    
    /**
   	 * <h1> Get Output File Name </h1>
   	 * This method prompts the user for the name of the file for the map to be written to. 
   	 * The method then sets the class element input file name to be this string.
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param none
   	 * @return none
   	 * 
   	 */
    private void getOutputFileName()
    {
    	String name = "Output.csv"; 	//Holds the name of the file for the map to be output to.
    	Scanner scanner = new Scanner(System.in);	//Read the name of the file from the user input
    	
    	System.out.println("Enter the name of the file for the map to be saved to: ");
    	
    	name = scanner.nextLine();
    	
    	
    	//scanner.close(); 			//Closes the scanner to save on resources */
    	this.outputFileName = name;
    }
    
    /**
   	 * <h1> Get Input File Name From User </h1>
   	 * This method prompts the user for the name of the file to be appended to.
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param none
   	 * @return none
   	 * 
   	 */
    private void getInputFileNameFromUser()
    {
    	String name = ""; 	//Holds the name of the file for the map to be input from.
    	
    	System.out.println("Enter the name of the file to be read from: ");
    	System.out.println("Note: the names need to be different the original is preserved as a backup");
    	Scanner scanner = new Scanner(System.in);	//Read the name of the file from the user input
    	if ( scanner.hasNext())
    	{
    		name = scanner.nextLine();
    	}
    	else
    	{
    		System.out.println("MENU READ ERROR");
    		System.exit(-1);
    	}
    	
    	//scanner.close(); 			//Closes the scanner to save on resources
    	this.inputFileName = name;
    }
    
    /**
   	 * <h1> Get Input From User </h1>
   	 * This method does the manual entry form the user into a linked list of room definitions
   	 * @version 1.0 Beta
   	 * @since 2018/11/7
   	 * @param none
   	 * @return none
   	 * 
   	 */
    private void getInputFromUser() 
    {
		char contInput = 'y';	//Holds the answer for whether there is more rooms to be added to the list.
		
		char outdoor = 'o';		//Holds the flag of the room being indoors or outdoors.
		int connectionNum;	//Temporary variable for holding the information as to what rooms are connected.
		
		
		
		while ( (contInput == 'Y') || (contInput == 'y'))	//While there are still rooms keep going
		{
			Scanner scanner = new Scanner(System.in);
			//this.numberOfRooms++;
			Node temp = new Node();
			System.out.println("Enter a description of the room:");
			String lineInput = scanner.nextLine();
			temp.roomDescription = lineInput;
			lineInput = "";
			temp.roomID = (this.numberOfRooms + 1);
			System.out.println("Is the room indor or outdoor i,o");
			outdoor = scanner.nextLine().charAt(0);
			
			if ( ( outdoor == 'i') || (outdoor == 'I'))
			{
				temp.indorOutdoor = 'i';
			}
			else
			{
				temp.indorOutdoor = 'o';
			}
			System.out.println("Enter a description of the north:");
			lineInput = scanner.nextLine();
			temp.northDescription = lineInput;
			lineInput = "";
			System.out.println("Enter a description of the east:");
			lineInput = scanner.nextLine();
			temp.eastDescription = lineInput;
			lineInput = "";
			System.out.println("Enter a description of the south:");
			lineInput = scanner.nextLine();
			temp.southDescription = lineInput;
			lineInput = "";
			System.out.println("Enter a description of the west:");
			lineInput = scanner.nextLine();
			temp.westDescription = lineInput;
			lineInput = "";
			System.out.println("Enter the room that connects to the north or 0 for no connection:");
			connectionNum = scanner.nextInt();
			temp.northConnecting = connectionNum;
			System.out.println("Enter the room that connects to the east or 0 for no connection:");
			connectionNum = scanner.nextInt();
			temp.eastConnecting = connectionNum;
			System.out.println("Enter the room that connects to the south or 0 for no connection:");
			connectionNum = scanner.nextInt();
			temp.southConnecting = connectionNum;
			System.out.println("Enter the room that connects to the west or 0 for no connection:");
			connectionNum = scanner.nextInt();
			temp.westConnecting = connectionNum;
			temp.NextNode = null;
			this.addNodeToList(temp);
			System.out.println("Is there another room?");
			lineInput = scanner.next();
			contInput = lineInput.charAt(0);
			//scanner.close();
		}
		
	}
    
    
    /**
   	 * <h1> Manual Append </h1>
   	 * This method calls the methods to get the file names, open the files, call the append method, and exit the program.
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param none
   	 * @return none
   	 * 
   	 */
    private void manualAppend() throws FileNotFoundException
    {
    	this.getInputFileNameFromUser();
    	this.getOutputFileName();
    	this.openInputFile();
    	this.openOutputFile();
    	this.readAndAppend();
    	this.programExit();
    	//this.displayFirstMenu();
    }
  
    /**
   	 * <h1> Print to File </h1>
   	 * This method takes the linked list of room definitions and writes then to the file
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param none
   	 * @return none
   	 * 
   	 */
    private void printToFile() throws IOException {
 		// TODO Auto-generated method stub
     	String output = "";
     	
     	//Header for the File
     	output = Integer.toString(this.numberOfRooms);	//Add the number of rooms to the beginning of the file
     	output += '\n';	//Add a line return for the parser
     	byte[] strToBytes = output.getBytes(); //Convert the output to byte format for the writer to write to a file
     	//System.out.println(this.outputFileName);
 		this.outputFile.write(strToBytes);	//Write the string to a file
 		output = "";	//Clear the output string for the next string of input
 		
 		while ( this.head != null)
 		{
 			//Setup for the First line defining the room information
 			output = "";
 			output += Integer.toString(this.head.roomID);	//add the room id to the beginning of the line
 			output += ',';
 			if ( this.head.indorOutdoor == 'i')
 			{
 				output += this.indoorDesignation;	//if the room is defined as indoors, add the tag to the line
 				output += ',';	//Comma delimited file setup
 			}
 			else
 			{
 				output += this.outdoorDesignation;	//if the room is outdoor add the string definition of the csv
 				output += ',';	//add the comma separator
 			}
 			output += this.head.roomDescription;	//add the description of the room to the string
 			output += ',';	//
 			//output += this.head.indorOutdoor;	//old format changed 10/19/2018 by Jorge to implement the Indoor Outdoor when entering a room feature
 			output += '\n';
 			strToBytes = output.getBytes();	//Convert the data into bytes to be sent to the file writer
 			this.outputFile.write(strToBytes);	//append the output to the file.
 			
 			//Setup and write the North definition of the room
 			output = "";	//clear the string for the next output setup
 			output += this.north;	//Writes the word NORTH in the output string
 			output += ',';
 			output += Integer.toString(this.head.northConnecting);	//Writes the room number that connects to the north, 0 if there i none
 			output += ",";
 			output += this.head.northDescription;	//Writes the description of the north side of the room
 			output += '\n';
 			strToBytes = output.getBytes();	//Convert the string to bytes to output to the file.
 			this.outputFile.write(strToBytes);	//Write the data to a file
 			
 			//Setup and write the south definition of the file
 			output = "";
 			output += this.south;	//Write the Word "SOUTH" to the output string
 			output += ",";
 			output += Integer.toString(this.head.southConnecting);	//Write the room connected to the south, or 0 if there is no room connected
 			output += ",";
 			output += this.head.southDescription;	//Write the description of the south side of the room 
 			output += '\n';
 			strToBytes = output.getBytes();	//convert the string to bytes to be output to the file
 			this.outputFile.write(strToBytes);	//Write the data to the file
 			
 			//Setup and write the east of definition of the file
 			output = "";	
 			output += this.east;	//Write the word "EAST" the string 
 			output += ",";
 			output += Integer.toString(this.head.eastConnecting);	//Write the connecting room to the east if one exists.
 			output += ",";
 			output += this.head.eastDescription;	//Description of the east side of the room
 			output += '\n';
 			strToBytes = output.getBytes();	//Convert the string to bytes to be written to the file.
 			this.outputFile.write(strToBytes);	//Write the data to a file
 			
 			//Setup and write the west definition of the file
 			output = "";
 			output += this.west;	//Write the word "WEST" to the string
 			output += ",";
 			output += Integer.toString(this.head.westConnecting);	//Define the room connecting to the west of the room
 			output += ",";
 			output += this.head.westDescription;	//Describe the west
 			output += '\n';
 			strToBytes = output.getBytes();
 			this.outputFile.write(strToBytes);	//Write to the file
 			output = "";
 			head = head.NextNode;	//Move to the next node
 		}
 		outputFile.close();
 			
 	}
     
    /**
   	 * <h1> Manual Append </h1>
   	 * This method takes an input and allows the user to add data to it and then saves the data in a new file
   	 * Note: this system was tested, but due to file naming and working directories is not advised.
   	 * After completion the user is returned to the main menu
   	 * @version 1.0 Beta
   	 * @since 2018/11/7

   	 * @param none
   	 * @return none
   	 * 
   	 */
    private void readAndAppend() throws FileNotFoundException
    {
    	File file = new File(this.inputFileName); 
    	  
    	BufferedReader br = new BufferedReader(new FileReader(file)); 
    	  
    	  String working = ""; 
    	  String description = "";
    	  String tempWorking = "";
    	  try 
    	  {
			if ( (working = br.readLine()) == null)
			{
				//This is the original number of Rooms in the file
				//since this will be overwritten by the addition of rooms it will be ignored
			}
			StringTokenizer st = new StringTokenizer(working, ",");
		     
    		//Begin the parsing of the data in the file.  
    		 
			do{
				working = br.readLine();
				st = new StringTokenizer(working, ",");
				System.out.println(working);
				System.out.println(st.countTokens());
				tempWorking += st.nextToken();	//Read and discard original room number
				//System.out.println(tempWorking);
				tempWorking = "";
				//System.out.println(st.countTokens());
				//System.out.println();
				tempWorking += st.nextToken();
				Node temp = new Node();
				//this.numberOfRooms++;
			    while (st.hasMoreTokens()) 
			    {
			    	
			    	if ( tempWorking.compareTo(this.indoorDesignation) == 0)
			        {
			        	temp.indorOutdoor = 'i';
			        }
			        else
			        {
			        	temp.indorOutdoor = 'o';
			        }
			    	while ( st.hasMoreElements())
			    	{
			    		description += st.nextToken();
			    	}
			    	temp.roomDescription = description;
			    	if((working = br.readLine()) != null)
			    	{
			    		st = new StringTokenizer(working, ",");
			    		System.out.println(working);
						System.out.println(st.countTokens());
			    		tempWorking = "";
			    		tempWorking = st.nextToken();	//Disregard the direction description, formatted data
			    		tempWorking = "";
			    		description = "";
			    		tempWorking = st.nextToken();
			    		temp.northConnecting = Integer.parseUnsignedInt(tempWorking);
			    		tempWorking = "";
			    		while ( st.hasMoreElements())
				    	{
				    		description += st.nextToken();
				    	}
			    		temp.northDescription = description;
			    	}
			    	else
			    	{
			    		System.out.println("FAILURE IN READING THE NORTH DATA");
			    		System.exit(-1);
			    	}
			    	if((working = br.readLine()) != null)
			    	{
			    		st = new StringTokenizer(working, ",");
			    		System.out.println(working);
						System.out.println(st.countTokens());
			    		tempWorking = "";
			    		tempWorking = st.nextToken();	//Disregard the direction description, formatted data
			    		tempWorking = "";
			    		description = "";
			    		tempWorking = st.nextToken();
			    		temp.southConnecting = Integer.parseUnsignedInt(tempWorking);
			    		tempWorking = "";
			    		while ( st.hasMoreElements())
				    	{
				    		description += st.nextToken();
				    	}
			    		temp.southDescription = description;
			    	}
			    	else
			    	{
			    		System.out.println("FAILURE IN READING THE SOUTH DATA");
			    		System.exit(-1);
			    	}
			    	if((working = br.readLine()) != null)
			    	{
			    		st = new StringTokenizer(working, ",");
			    		System.out.println(working);
						System.out.println(st.countTokens());
			    		tempWorking = "";
			    		tempWorking = st.nextToken();	//Disregard the direction description, formatted data
			    		tempWorking = "";
			    		description = "";
			    		tempWorking = st.nextToken();
			    		temp.eastConnecting = Integer.parseUnsignedInt(tempWorking);
			    		tempWorking = "";
			    		while ( st.hasMoreElements())
				    	{
				    		description += st.nextToken();
				    	}
			    		temp.eastDescription = description;
			    	}
			    	else
			    	{
			    		System.out.println("FAILURE IN READING THE EAST DATA");
			    		System.exit(-1);
			    	}
			    	if((working = br.readLine()) != null)
			    	{
			    		st = new StringTokenizer(working, ",");
			    		System.out.println(working);
						System.out.println(st.countTokens());
			    		tempWorking = "";
			    		tempWorking = st.nextToken();	//Disregard the direction description, formatted data
			    		tempWorking = "";
			    		description = "";
			    		tempWorking = st.nextToken();
			    		temp.westConnecting = Integer.parseUnsignedInt(tempWorking);
			    		tempWorking = "";
			    		while ( st.hasMoreElements())
				    	{
				    		description += st.nextToken();
				    	}
			    		temp.westDescription = description;
			    	}
			    	else
			    	{
			    		//System.out.println("FAILURE IN READING THE West DATA");
			    		//System.exit(-1);
			    	}
			    	this.addNodeToList(temp);
			    }
			        
			   }while ((working = br.readLine()) != null);
			    
			    
    	  } catch (IOException e) {
			// TODO Auto-generated catch block
    		  System.out.println("ERROR IN THE READING OF THE FILE");
    		  e.printStackTrace();
		}
    	    
    }
    
    /**
   	 * <h1> Generate Grid </h1>
   	 * This method creates a grid of generic rooms.  
   	 * Then the game environment designer will edit the file to have details
   	 * In future versions a GUI will be added for editing the rooms.
   	 * @version 1.0 Beta
   	 * @since 2018/11/9
   	 * @param none
   	 * @return none
     * @throws IOException 
   	 * 
   	 */
    private void generateGrid() throws IOException
    {
    	this.getOutputFileName();
    	this.openOutputFile();
    	this.getAutoMapSize();
    	this.printToFile();
    	//this.displayFirstMenu();
    	this.programExit();
    }
 
    /**
   	 * <h1> getAutoMapSize </h1>
   	 * This method gets the size of the map and calls the generator
   	 * @version 1.0 Beta
   	 * @since 2018/11/9
   	 * @param none
   	 * @return none
   	 * 
   	 */
    private void getAutoMapSize()
    {
    	int rows = 0;
    	int col = 0;
    	//this.getOutputFileName();
    	Scanner scanner = new Scanner(System.in);
    	System.out.println("Enter the number of columns: ");
    	col = scanner.nextInt();
    	System.out.println("Enter the number of rows: ");
    	rows = scanner.nextInt();
    	//scanner.close();
    	
    	if ( ( col < 0) || (rows < 0))
    	{
    		System.out.println("ERROR VALUES OUTSIDE OF ACCEPTED RANGE");
    		System.exit(-1);
    	}
    	
    	this.autoMapBuild(rows, col);
    	
    	
    }
    
    
    /**
   	 * <h1> Auto Map Build </h1>
   	 * This method does the work for the generate grid method.
   	 * The goal of this method is to assist in the construction of grid based maps
   	 * For example GMU campus. It forms a rectangle grid.
   	 * @version 1.0 Beta
   	 * @since 2018/11/9
   	 * @param int rows; defines the number of rows in the map: int col; defines the number of colums
   	 * @return none
   	 * 
   	 */
    private void autoMapBuild( int rows, int col)
    {
    	
    	
    	
    	
    	for ( int a = 1; a <= rows; a++)
    	{
    		for ( int b = 1; b <= col; b++)
    		{
    			//char outdoor = 'o';
    			//int connectionNum = 0;		
    			//this.numberOfRooms++;
    			Node temp = new Node();
    			temp.roomDescription = "Room Description";
    			temp.roomID = (this.numberOfRooms+ 1);
    			temp.indorOutdoor = 'o';
    			temp.northDescription = "North Discription";
    			temp.eastDescription = "East Description";
    			temp.southDescription = "South Descripton";
    			temp.westDescription = "West Description";
    			if ( (a-1) < 1)	//If this s the top row then there is no room connected to the north
    			{
    				temp.northConnecting = 0;
    			}
    			else
    			{
    				temp.northConnecting = (((a-1)*col) + b); //Else define it as the room to the immediate north of the current room.
    			}
    			if ( (b+1) > col)	//If this is the last room in the column then this cannot be connected to the east.
    			{
    				temp.eastConnecting = 0;
    			}
    			else
    			{
    				temp.eastConnecting = (this.numberOfRooms + 2);	//Else there will be a room connected to the east.
    			}
    			if ((a+1) > rows)	//If this is the last row on the map then there will be no rooms connected to the south
    			{
    				temp.southConnecting = 0;
    			}
    			else
    			{
    				temp.southConnecting = (((a)*col) + b);	//Else there is a room to connect to the south
    			}
    			if ( (b - 1) < 1)	//If this room is in the first column then there can not be a room to the west.
    			{
    				temp.westConnecting = 0;
    			}
    			else
    			{
    				temp.westConnecting = (this.numberOfRooms); //Else there is a room and it connects to the previous room in the map.
    			}
    			temp.NextNode = null;
    			this.addNodeToList(temp);
    			System.out.println("working " + this.numberOfRooms);
    			
    			
    		}
    	}
    }
    
    /**
   	 * <h1> Generate Dugeon </h1>
   	 * This method calls the other methods that are required to maze the maze style maps.
   	 * The goal of this method is to assist in the construction of maze based maps
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param nones
   	 * @return none
   	 * 
   	 */
    private void generateDugeon() throws IOException
    {
    	int rooms = 0;
    	
    	this.getOutputFileName();
    	this.openOutputFile();
    	rooms = this.getNumberOfRooms();
    	this.generateMaze(rooms);
    	this.printToFile();
    	this.programExit();
    	//this.displayFirstMenu();
    }
    
    /**
   	 * <h1> Get Number of Rooms </h1>
   	 * This method gets the number of rooms in the maze from the user.
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param nones
   	 * @return none
   	 * 
   	 */
    private int getNumberOfRooms()
    {
    	int rooms = 0;
    	
    	
    	Scanner scanner = new Scanner(System.in);
    	System.out.println("Enter the number of rooms: ");
    	rooms = scanner.nextInt();
    	
    	//scanner.close();
    	
    	if ( rooms < 1)
    	{
    		System.out.println("ERROR VALUES OUTSIDE OF ACCEPTED RANGE");
    		System.exit(-1);
    	}
    	
    	return rooms;
    }
    
    /**
   	 * <h1> Generate Maze </h1>
   	 * This method generates the maze map.  
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param int; number of rooms in the maze
   	 * @return none
   	 * 
   	 */
    private void generateMaze( int rooms)
    {
    	int direction = 0; //stores the current direction of travel, values 1-4 
    	int distance = 0;
    	int current_x = 0;
    	int current_y = 0;
    	
    	
    	while ( this.numberOfRooms < rooms)
    	{
    		Node temp = new Node();
        	temp.roomID = (this.numberOfRooms + 1);
        	direction = this.getDirection();
        	if ( temp.roomID == 1)
        	{
        		//This sets the location of the first room that the map will be built on
        		this.newMazeRoom( temp, 0, 0, 0, 0, 0, 0);
        		System.out.println("Added room: " + temp.roomID);
        	}
        	else
        	{
        		//Get a random direction
        		//Get a distance to travel in that direction
        		System.out.println("attempting to add room: " + temp.roomID);
        		
        		//direction = this.getDirection();
        			boolean impact = false;	//Determine if the new room will be at the location of an existing room
        			this.checkForConnectingRooms( temp, current_x, current_y);
        			//System.out.println("Current x:" + current_x + " Current y: " + current_y + " Direction:" + direction);
        			//Section checks to see if there is a room in the direction that a new room needs to be placed
        			switch (direction)
        			{
        			case 1:		impact = ( temp.northConnecting != 0);
        						break;
        						
        			case 2:		impact = ( temp.eastConnecting !=0);
        						break;
        			
        			case 3:		impact = ( temp.southConnecting != 0);
        						break;
        				
        			case 4:		impact = ( temp.westConnecting != 0);
        						break;
        							
        			default:	this.generateError();
        			}
        			//System.out.println("There is an impact: " + impact);
        			//If there is no room add one
        			if ( !impact)
        			{
        				switch (direction)
        				{
        				case 1:		this.newMazeRoom(temp, temp.northConnecting, temp.eastConnecting, (temp.roomID - 1), temp.westConnecting, current_x, (current_y + 1));
        							current_y++;
        							//this.checkforBackwardLinks();
        							break;
        				case 2:		this.newMazeRoom(temp, temp.northConnecting, temp.eastConnecting, temp.southConnecting, (temp.roomID - 1), (current_x+1), current_y);
        							current_x++;
        							//this.checkforBackwardLinks();
        							break;
        				case 3:		this.newMazeRoom(temp, (temp.roomID - 1), temp.eastConnecting, temp.southConnecting, temp.westConnecting, current_x, (current_y - 1));
									current_y--;
									//this.checkforBackwardLinks();
									break;
        				case 4:		this.newMazeRoom(temp, temp.northConnecting, (temp.roomID - 1), temp.southConnecting, temp.westConnecting, (current_x-1), current_y);
									current_x--;
									//this.checkforBackwardLinks();
									break;
						default:	System.out.println("Maze ERROR");
									this.generateError();
        				}
        			}
        			else
        			{
        				//If there is a room then we need to walk through the existing room and add on the other side.
        				switch (direction)
        				{
        				case 1:		current_y = this.findEmptyNorth( temp, current_x, current_y);
        							this.newMazeRoom(temp, temp.northConnecting, temp.eastConnecting, (temp.roomID - 1), temp.westConnecting, current_x, (current_y + 1));
        							current_y++;
        							//this.checkforBackwardLinks();
        							break;
        				case 2:		current_y = this.findEmptyEast( temp, current_x, current_y);
        							this.newMazeRoom(temp, temp.northConnecting, temp.eastConnecting, temp.southConnecting, (temp.roomID - 1), (current_x+1), current_y);
        							current_x++;
        							//this.checkforBackwardLinks();
        							break;
        				case 3:		current_y = this.findEmptySouth( temp, current_x, current_y);
        							this.newMazeRoom(temp, (temp.roomID - 1), temp.eastConnecting, temp.southConnecting, temp.westConnecting, current_x, (current_y - 1));
									current_y--;
									//this.checkforBackwardLinks();
									break;
        				case 4:		current_x = this.findEmptyWest(  temp, current_x, current_y);
        							this.newMazeRoom(temp, temp.northConnecting, (temp.roomID - 1), temp.southConnecting, temp.westConnecting, (current_x-1), current_y);
									current_x--;
									//this.checkforBackwardLinks();
									break;
						default:	System.out.println("Maze ERROR");
									this.generateError();
        				}
        			}
        			
        		}
        	
        	}
    		
    		this.checkforBackwardLinks();
    		
    }
    	
    /**
   	 * <h1> Find Empty North </h1>
   	 * Step through existing rooms until we find a place to add the new room
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param Node; the node we are adding, int; x position f node, int; y position
   	 * @return none
   	 * 
   	 */	
    private int findEmptyNorth( Node n, int x, int y)
    {
    	Node current = head;
    	boolean test = false;
    	int new_y = y;
    	while (current != null)
    	{
    		System.out.println(current.roomID);
    		if ( ( current.x_val == x) && (current.y_val == new_y))
    		{
    			if ( current.northConnecting == 0)
    			{
    				return new_y;
    			}
    			else
    			{
    				new_y++;
    				current = head;
    			}
    		}
    		current = current.NextNode;
    	}
    	return new_y;
    }
    
    /**
   	 * <h1> Find Empty East </h1>
   	 * Step through existing rooms until we find a place to add the new room
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param Node; the node we are adding, int; x position f node, int; y position
   	 * @return none
   	 * 
   	 */	
    private int findEmptyEast( Node n, int x, int y)
    {
    	Node current = head;
    	boolean test = false;
    	int new_x = x;
    	while (current != null)
    	{
    		System.out.println(current.roomID);
    		if ( ( current.x_val == new_x) && (current.y_val == y))
    		{
    			if ( current.eastConnecting == 0)
    			{
    				return new_x;
    			}
    			else
    			{
    				new_x++;
    				current = head;
    			}
    		}
    		current = current.NextNode;
    	}
    	return new_x;
    }
    
    /**
   	 * <h1> Find Empty South </h1>
   	 * Step through existing rooms until we find a place to add the new room
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param Node; the node we are adding, int; x position f node, int; y position
   	 * @return none
   	 * 
   	 */	
    private int findEmptySouth( Node n, int x, int y)
    {
    	Node current = head;
    	boolean test = false;
    	int new_y = y;
    	while (current != null)
    	{
    		System.out.println(current.roomID);
    		if ( ( current.x_val == x) && (current.y_val == new_y))
    		{
    			if ( current.southConnecting == 0)
    			{
    				return new_y;
    			}
    			else
    			{
    				new_y--;
    				current = head;
    			}
    		}
    		current = current.NextNode;
    	}
    	return new_y;
    }
    
    /**
   	 * <h1> Find Empty West </h1>
   	 * Step through existing rooms until we find a place to add the new room
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param Node; the node we are adding, int; x position f node, int; y position
   	 * @return none
   	 * 
   	 */	
    private int findEmptyWest( Node n, int x, int y)
    {
    	Node current = head;
    	boolean test = false;
    	int new_x = x;
    	while (current != null)
    	{
    		System.out.println(current.roomID);
    		if ( ( current.x_val == new_x) && (current.y_val == y))
    		{
    			if ( current.westConnecting == 0)
    			{
    				return new_x;
    			}
    			else
    			{
    				new_x--;
    				current = head;
    			}
    		}
    		current = current.NextNode;
    	}
    	return new_x;
    }
    
    /**
   	 * <h1> Check for Backward Links </h1>
   	 * Step through the rooms and look for links from the next node to the previous
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param none
   	 * @return none
   	 * 
   	 */	
    private void checkforBackwardLinks()
    {
    	Node current = head;
    	
    	while ( current != null)
    	{
    		this.checkForConnectingRooms(current, current.x_val, current.y_val);
    		current = current.NextNode;
    	}
    }
    	
    	
    	
    	
    	
    	
  
    
    /**
   	 * <h1> Check for Connected Rooms </h1>
   	 * Checks the node for nodes that are next to the current node and connects them.
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param Node; the node we are checking, int; x position f node, int; y position
   	 * @return none
   	 * 
   	 */	
    private void checkForConnectingRooms( Node n, int x, int y)
    {
    	this.checkForRoomNorth( n, x, (y+1));
    	this.checkForRoomEast( n, (x+1), y);
    	this.checkForRoomSouth( n, x, (y-1));
    	this.checkForRoomWest( n, (x-1), y);
    }
    
    /**
   	 * <h1> Check for Connected Rooms  North</h1>
   	 * Checks the node for nodes that are next to the current node and connects them.
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param Node; the node we are checking, int; x position f node, int; y position
   	 * @return none
   	 * 
   	 */	
    private void checkForRoomNorth( Node n, int x, int y)
    {
    	Node current = head;
    	while ( current != null)
    	{
    		if ( (current.x_val == x ) && (current.y_val == y))
    		{
    			
    			n.northConnecting = current.roomID;
    			current = current.NextNode;
    		}
    		else
    		{
    			current = current.NextNode;
    		}
    	}
    }
    
    /**
   	 * <h1> Check for Connected Rooms East</h1>
   	 * Checks the node for nodes that are next to the current node and connects them.
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param Node; the node we are checking, int; x position f node, int; y position
   	 * @return none
   	 * 
   	 */
    private void checkForRoomEast( Node n, int x, int y)
    {
    	Node current = head;
    	while ( current != null)
    	{
    		if ( (current.x_val == x ) && (current.y_val == y))
    		{
    			n.eastConnecting = current.roomID;
    			current = current.NextNode;
    		}
    		else
    		{
    			current = current.NextNode;
    		}
    	}
    }
    
    /**
   	 * <h1> Check for Connected Rooms South</h1>
   	 * Checks the node for nodes that are next to the current node and connects them.
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param Node; the node we are checking, int; x position f node, int; y position
   	 * @return none
   	 * 
   	 */
    private void checkForRoomSouth( Node n, int x, int y)
    {
    	Node current = head;
    	while ( current != null)
    	{
    		if ( (current.x_val == x ) && (current.y_val == y))
    		{
    			n.southConnecting = current.roomID;
    			current = current.NextNode;
    		}
    		else
    		{
    			current = current.NextNode;
    		}
    	}
    }
    
    /**
   	 * <h1> Check for Connected Rooms West</h1>
   	 * Checks the node for nodes that are next to the current node and connects them.
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param Node; the node we are checking, int; x position f node, int; y position
   	 * @return none
   	 * 
   	 */
    private void checkForRoomWest( Node n, int x, int y)
    {
    	Node current = head;
    	while ( current != null)
    	{
    		if ( (current.x_val == x ) && (current.y_val == y))
    		{
    			n.westConnecting = current.roomID;
    			current = current.NextNode;
    		}
    		else
    		{
    			current = current.NextNode;
    		}
    	}
    }
    
    /**
   	 * <h1> New Maze Room</h1>
   	 * Adds a generic node to the lst at a given locaton.
   	 * 
   	 * @version 1.0 Beta
   	 * @since 2018/11/10
   	 * @param Node; the node we are adding, int, int, int, int, int, int, n e s w x y
   	 * @return none
   	 * 
   	 */
    private void newMazeRoom( Node n, int north, int east, int south, int west, int x, int y)
	{
    	System.out.println("Added room: " + n.roomID);
    	n.roomDescription = "Generic Room";
		n.northDescription = "Generic North";
		n.northConnecting = north;
		n.eastDescription = "Generic East";
		n.eastConnecting = east;
		n.southDescription = "Generic South";
		n.southConnecting = south;
		n.westDescription = "Generic West";
		n.westConnecting = west;
		n.indorOutdoor = 'o';
		n.x_val = x;
		n.y_val = y;
		this.addNodeToList(n);
		
	}
    
    private int getDirection()
    {
    	int random = (int )(Math.random() * 4 + 1);
    	return random;
    }
   
    private int getDistance()
    {
    	int random = (int )(Math.random() * 42 + 1);	
    	return random;
    }
}
