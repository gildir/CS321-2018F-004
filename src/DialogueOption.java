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

public class DialogueOption {

    private final String prompt;
    private final String response;
    private String tag;
    private boolean useTag = false;
    private int currentDialogueId = 1;

    /**
     * This class represents a dialogue option between a player and an NPC
     *
     */
    public DialogueOption(String prompt, String response) {
        this.prompt = prompt;
        this.response = response;
    }

    public DialogueOption(String prompt, String tag, boolean useTag) {
        this.prompt = prompt;
        this.tag = tag;
        this.response = "";
        this.useTag = useTag;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getResponse(String npcName, int playerDialogueId) {
        if (!useTag)
        {
            return response;
        }
        else
        {
            return getResponseFromFile(npcName, playerDialogueId);
        }
    }

    public String getTag() {
        return tag;
    }

    public boolean usingTag() {
        return useTag;
    }

    public int getDialogueId()
    {
        return currentDialogueId;
    }

    public void changeDialogueId(int dialogueId) {
        currentDialogueId = dialogueId;
    }

    private String getResponseFromFile(String npcName, int playerDialogueId)
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

            String description;
            Element xmlElement;
            Element xmlDialogueElement;
            Element xmlDialogueIdElement;

            for (int i = 0; i < xmlNPC.getLength(); i++) {
                xmlElement = (Element) xmlNPC.item(i);

                if (npcName.equals(xmlElement.getAttribute("name")))
                {
                    xmlDialogueType = xmlElement.getElementsByTagName("dialogue_type");
                    for (int j = 0; j < xmlDialogueType.getLength(); j++)
                    {
                        xmlDialogueElement = (Element) xmlDialogueType.item(j);
                        if (tag.equals(xmlDialogueElement.getAttribute("type")))
                        {
                            xmlDialogueId = xmlDialogueElement.getElementsByTagName("dialogue_id");

                            for (int n = 0; n < xmlDialogueId.getLength(); n++) {
                                xmlDialogueIdElement = (Element) xmlDialogueId.item(n);
                                if (Integer.parseInt(xmlDialogueIdElement.getAttribute("id")) == playerDialogueId)
                                {
                                    return xmlDialogueIdElement.getTextContent();
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(DialogueOption.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "I have nothing to tell you about that";
    }
}


