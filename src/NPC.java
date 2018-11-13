import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NPC {
    private final String name;
    private int room;
    private LinkedList<String> quests;
    private ArrayList<DialogueOption> dialogueList;

    public NPC(String name, int room, LinkedList<String> quests, ArrayList<DialogueOption> dialogueList) {
        this.name = name;
        this.room = room;
        this.quests = quests;
        this.dialogueList = dialogueList;
        readXMLDialogue();
    }

    public String getName() {
        return name;
    }

    public List<String> getQuests(){
        return quests;
    }

    public ArrayList<DialogueOption> getDialogueList() {
        return dialogueList;
    }

    @Override
    public String toString(){
        return "NPC " + getName();
    }

    public boolean changeDialogueList(String dialogueTag, int changeTagId)
    {
        for (int i = 0; i < dialogueList.size(); i++) {
            if (dialogueList.get(i).usingTag() && dialogueList.get(i).getTag().equals(dialogueTag))
            {
                dialogueList.get(i).changeDialogueId(changeTagId);
                return true;
            }
        }
        return false;
    }

    public boolean incrementDialogueList(String dialogueTag)
    {
        for (int i = 0; i < dialogueList.size(); i++) {
            if (dialogueList.get(i).usingTag() && dialogueList.get(i).getTag().equals(dialogueTag))
            {
                dialogueList.get(i).changeDialogueId(dialogueList.get(i).getDialogueId() + 1);
                return true;
            }
        }
        return false;
    }

    public int getDialogueId(String dialogueTag)
    {
        for (int i = 0; i < dialogueList.size(); i++) {
            if (dialogueList.get(i).usingTag() && dialogueList.get(i).getTag().equals(dialogueTag))
            {
                return dialogueList.get(i).getDialogueId();
            }
        }
        
        return -1;
    }

    public void addToDialogueList(String dialogueTag, String prompt)
    {
        dialogueList.add(new DialogueOption(prompt, dialogueTag, true));
    }

    private void readXMLDialogue()
    {
        try {
            File commandFile = new File("./NPC_Dialogue.xml");
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(commandFile);

            document.getDocumentElement().normalize();
            NodeList xmlNPC = document.getElementsByTagName("npc");
            NodeList xmlDialogueType;
            NodeList xmlDialogueId;

            String prompt;
            String dialogueTag;
            Element xmlElement;
            Element xmlDialogueElement;
            Element xmlDialogueIdElement;

            for (int i = 0; i < xmlNPC.getLength(); i++) {
                xmlElement = (Element) xmlNPC.item(i);

                if (name.equals(xmlElement.getAttribute("name")))
                {
                    xmlDialogueType = xmlElement.getElementsByTagName("dialogue_type");
                    for (int j = 0; j < xmlDialogueType.getLength(); j++) 
                    {
                        xmlDialogueElement = (Element) xmlDialogueType.item(j);
                        prompt = xmlDialogueElement.getElementsByTagName("dialogue_prompt").item(0).getTextContent();
                        dialogueTag = xmlDialogueElement.getAttribute("type");

                        dialogueList.add(new DialogueOption(prompt,dialogueTag,true));
                    }

                    break;
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(NPC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
