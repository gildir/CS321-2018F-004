import java.io.File;
import java.util.logging.*;
import java.io.IOException;

public class GhoulLog
{
	public Logger logger;
	public FileHandler fh;
	public GhoulLog() throws SecurityException, IOException{

	LogManager.getLogManager().reset();
	File f = new File("ghoulLog.txt");
	if(!f.exists()){
		f.createNewFile();
	}
	fh = new FileHandler("ghoulLog.txt", true);
	logger = Logger.getLogger("GhoulLog");
	logger.addHandler(fh);
	SimpleFormatter formatter = new SimpleFormatter();
	fh.setFormatter(formatter);
	
	
	}
	

	public void glLog(String class_Name, String method, String msg){
		try{
			logger.logp(Level.SEVERE, class_Name, method, msg);
		}catch (Exception e){}
		
	} 

}