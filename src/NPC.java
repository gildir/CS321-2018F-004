import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NPC {
    private final String name;
    private int room;
    private ArrayList<String> introDialogues;
    private ArrayList<String> contDialogues;
    private ArrayList<String> doneDialogues;
    private boolean validDialogue;


    public NPC(String name, int room) {
        this.name = name;
        this.room = room;
        validDialogue = setDialogues("./NPCDialogues/" + name + "/Dialogue.xml");
    }


    public String getName() {
        return name;
    }

    @Override
    public String toString(){
        return "NPC " + getName();
    }

    public boolean checkValidDialogue(){return validDialogue;}

    private boolean setDialogues(String fileName){
	try{
	    introDialogues = new ArrayList<>();
	    contDialogues = new ArrayList<>();
       	    doneDialogues = new ArrayList<>();

            File dialFile = new File(fileName);
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(dialFile);
       
            document.getDocumentElement().normalize();
            NodeList xmlDial = document.getElementsByTagName("dialogue");

	    String dialogue;
	    Element dialogueElement;
	    int id;

	    introDialogues.add(0,null);
            contDialogues.add(0,null);
            doneDialogues.add(0,null);
	    
	    for(int i = 0; i < xmlDial.getLength(); i ++){
                dialogueElement = (Element) xmlDial.item(i);
		id = Integer.parseInt(dialogueElement.getAttribute("id"));
                for(int j = introDialogues.size(); j <= id; j ++){
                    introDialogues.add(j,null);
                    contDialogues.add(j,null);
                    doneDialogues.add(j,null);
                }

		if(id != -1){
                    dialogue = dialogueElement.getElementsByTagName("intro").item(0).getTextContent();
	            introDialogues.set(id,dialogue);

	            dialogue = dialogueElement.getElementsByTagName("cont").item(0).getTextContent();
                    contDialogues.set(id,dialogue);

	            dialogue = dialogueElement.getElementsByTagName("done").item(0).getTextContent();
                    doneDialogues.set(id,dialogue);
		}else{
		    dialogue = dialogueElement.getElementsByTagName("intro").item(0).getTextContent();
                    introDialogues.set(0,dialogue);
		}
	    }
        } catch (IOException ex1){
            System.out.println("[WORLD CREATION] Invalid Or No Dialogue For NPC: " + name + " In Room ID: " + room);
	    return false;
	} catch (ParserConfigurationException | SAXException ex2) {
            Logger.getLogger(NPC.class.getName()).log(Level.SEVERE, null, ex2);
	} 
	return true;
    }

    public String talk(Player player){
	if(!validDialogue)
            return name + " looks at you, and says nothing";
        int progress = player.getProgress();
	int dialId = (progress / 3)+1;
	String dial = "";
	switch(progress % 3)
	{
            case 0:
	        dial = introDialogues.get(dialId);
		if(dial != null)
                    player.advanceQuest();
		break;
            case 1:
                dial = contDialogues.get(dialId);
		break;
            case 2:
		dial = doneDialogues.get(dialId);
	}
        if(dial != null){
            return dial;
	}else{
	    return introDialogues.get(0);
	}
    }
}
