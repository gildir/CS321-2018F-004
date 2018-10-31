import java.util.LinkedList;
import java.util.List;

public class NPC {
    private final String name;
    private int room;
    private LinkedList<String> quests;


    public NPC(String name, int room, LinkedList<String> quests) {
        this.name = name;
        this.room = room;
        this.quests = quests;
    }


    public String getName() {
        return name;
    }

    public List<String> getQuests(){
        return quests;
    }

    @Override
    public String toString(){
        return "NPC " + getName();
    }
}
