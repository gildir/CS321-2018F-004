import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
    
// Class to handle the creation and modification of a text file which logs player-world interactions

public class DailyLogger{
    public String date;
    public String filename;
    public File f;


    // Creates log file in logs folder with title "log [date].txt"
    public DailyLogger(){
        date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        File dir = new File("./logs/");
        if(!dir.exists()){
            dir.mkdir();
        }
        filename = new String("log " + date + ".txt");
        this.f = new File("logs/" + filename);
        if (!f.exists()){
            try{
                // String timestamp = new SimpleDateFormat("'['MM-dd-yyyy hh:mm:ss']'").format(new Date()); 
                f.createNewFile();
                PrintWriter writer = new PrintWriter(new FileOutputStream(f, false));
                writer.println("Daily Log for " + date);
                writer.flush();
                writer.close();
            } catch (IOException e){};
        }
    }

    // For writing custom messages to the log file
    public void write(String message){
        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream(f, true));
            String timestamp = new SimpleDateFormat("'['MM-dd-yyyy hh:mm:ss']'").format(new Date()); 
            writer.printf("%s - %s%n", timestamp, message);
            writer.flush();
            writer.close();
        } catch (IOException e){};
    }

    // For commands like LOOK that only have one argument
    public void write(String name, String command, String room){
        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream(f, true));
            String timestamp = new SimpleDateFormat("'['MM-dd-yyyy hh:mm:ss']'").format(new Date()); 
            writer.printf("%s - %s used command %S @ %s%n", timestamp, name, command, room);
            writer.flush();
            writer.close();
        } catch (IOException e){};
    }

    // For commands like PICKUP which have one or more arguments after the initial command argument
    public void write(String name, String command, String args, String room){
        try{
            PrintWriter writer = new PrintWriter(new FileOutputStream(f, true));
            String timestamp = new SimpleDateFormat("'['MM-dd-yyyy hh:mm:ss']'").format(new Date());
            writer.printf("%s - %s used command %S %s @ %s%n", timestamp, name, command, args, room);
            writer.flush();
            writer.close();
        } catch (IOException e){};
    }
}