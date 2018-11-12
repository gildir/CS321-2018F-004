import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

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
            if (dialogueList.get(i).usingTag() && dialogueList.get(i).getTag() == dialogueTag)
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
            if (dialogueList.get(i).usingTag() && dialogueList.get(i).getTag() == dialogueTag)
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
            if (dialogueList.get(i).usingTag() && dialogueList.get(i).getTag() == dialogueTag)
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
}
