import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;

//import LinkedList.Node;

/*
 * Programmer: Christopher Wells
 * G-00260513
 * 10/10/2018
 * 
 * 	The primary function of the class is to write the CSV file that will be used in the creation of the world.
 * The program will later be interfaced with an automated algorithm to provide input to this class.
 */



public class addRoom{

	//File IO variables
	private FileInputStream inputFile = null;
    private FileOutputStream outputFile = null;
    
    private String inputFileName = "";
    private String outputFileName = "";
    
    private static final String north = "NORTH";
    private static final String east = "EAST";
    private static final String south = "SOUTH";
    private static final String west = "WEST";
    private static final String outdoorDesignation = "outside";
    private static final String indoorDesignation = "inside";
    
    //Room Definition Variables
    private class Node<T>
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
	    
	    
		
	}
	
    private int numberOfRooms = 0;
    
    private Node head = null;
    private Node current = head;
    //Constructors
    
    addRoom()
    {
    	
    }
    
    addRoom( String infile, String outfile) throws IOException
    {
    	this.defineOutputFileName(infile);
    	this.defineOutputFileName(outfile);
    	
    	this.openInputFile();
    	this.openOutputFile();
    	
    	this.readAndAppend();
    	this.appendFromUser();
    	this.printToFile();
    }
    
    
    
    private void readAndAppend() throws FileNotFoundException
    {
    	File file = new File("/home/christopher/workspace/CS321_Game/test.csv"); 
    	  
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
				this.numberOfRooms++;
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
    
    addRoom( String outfile) throws IOException
    {
    	
    	this.defineOutputFileName(outfile);
    	
    	this.autoMapBuild();
    	this.openOutputFile();
    	//this.appendFromUser();
    	this.printToFile();
    }
    
    
    private void autoMapBuild()
    {
    	for ( int a = 1; a <= 100; a++)
    	{
    		for ( int b = 1; b <= 200; b++)
    		{
    			char outdoor = 'o';
    			int connectionNum;		
    			this.numberOfRooms++;
    			Node temp = new Node();
    			temp.roomDescription = "Room Description";
    			temp.roomID = this.numberOfRooms;
    			temp.indorOutdoor = 'o';
    			temp.northDescription = "North Discription";
    			temp.eastDescription = "East Description";
    			temp.southDescription = "South Descripton";
    			temp.westDescription = "West Description";
    			if ( (a-1) < 1)
    			{
    				temp.northConnecting = 0;
    			}
    			else
    			{
    				temp.northConnecting = (a-1);
    			}
    			if ( (b+1) > 200)
    			{
    				temp.eastConnecting = 0;
    			}
    			else
    			{
    				temp.eastConnecting = (b+1);
    			}
    			if ((a+1) > 100)
    			{
    				temp.southConnecting = 0;
    			}
    			else
    			{
    				temp.southConnecting = (a+1);
    			}
    			if ( (b -1) < 1)
    			{
    				temp.westConnecting = 0;
    			}
    			else
    			{
    				temp.westConnecting = (a-1);
    			}
    			temp.NextNode = null;
    			this.addNodeToList(temp);
    			System.out.println("working " + this.numberOfRooms);
    			
    			
    		}
    	}
    }
    
    
    
    
    private void printToFile() throws IOException {
		// TODO Auto-generated method stub
    	String output = "";
    	
    	//Header for the File
    	output = Integer.toString(this.numberOfRooms);	//Add the number of rooms to the beginning of the file
    	output += '\n';	//Add a line return for the parser
    	byte[] strToBytes = output.getBytes(); //Convert the output to byte format for the writer to write to a file
		this.outputFile.write(strToBytes);	//Write the string to a file
		output = "";	//Clear the output string for the next string of input
		
		while ( this.head != null)
		{
			//Setup for the First line defining the room information
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
			//this.outputFile.write(strToBytes);	//Write to the file
			
			head = head.NextNode;	//Move to the next node
		}
		outputFile.close();
			
	}
    
    

	//Internal methods 
    private void defineInputFileName(String name)
    {
    	this.inputFileName = name;
    }
    
    private void defineOutputFileName(String name)
    {
    	this.outputFileName = name;
    }
    
    private void openInputFile()
    {
    	try
    	{
    		inputFile = new FileInputStream("test.csv");
    	}
    	catch(Exception e)
    	{
    		System.out.println("ERROR IN THE OPENING OF THE INPUT FILE");
    		System.exit(-1);
    	}
    }
	
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
    
    //IO methods
    
    public void copyFromFile()
    {
    	
    }
    
    public void appendFromFile()
    {
    	
    }
    
    public void appendFromUser()
    {
    	this.getInputFromUser();
    }
    
    private void getInputFromUser() 
    {
		char contInput = 'y';
		
		char outdoor = 'o';
		int connectionNum;
		
		
		
		while ( (contInput == 'Y') || (contInput == 'y'))
		{
			Scanner scanner = new Scanner(System.in);
			this.numberOfRooms++;
			Node temp = new Node();
			System.out.println("Enter a description of the room:");
			String lineInput = scanner.nextLine();
			temp.roomDescription = lineInput;
			lineInput = "";
			temp.roomID = this.numberOfRooms;
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
			scanner.reset();
		}
		
	}

	private void addNodeToList(Node temp) 
	{
		// TODO Auto-generated method stub
		if ( this.head == null)
		{
			this.head = temp;
			this.current = temp;
		}
		else
		{
			this.current.NextNode = temp;
			this.current = temp;
		}
		
	}

	public void appendFromAutomated()
    {
    	
    }
}
