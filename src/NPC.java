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
}
