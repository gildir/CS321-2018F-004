import java.util.ArrayList;

public class Chatroom {
	private ArrayList<String> members = null;
	private ArrayList<String> invited = null;
	private String chatName = "";
	
	public Chatroom(String playerName, String chatName) {
		members = new ArrayList<String>();
		invited = new ArrayList<String>();
		members.add(playerName);
		this.setName(chatName);
	}
	
    public String getName() {
        return chatName;
    }

    public void setName(String chatName) {
        this.chatName = chatName;
    }
    
    public ArrayList<String> getMembers() {
        return members;
    }

    public void addMember(String playerName) {
		members.add(playerName);
    }
    
    public void removeMember(String playerName) {
		members.remove(playerName);
    }
    
    public ArrayList<String> getInvited() {
        return invited;
    }

    public void addInvited(String playerName) {
		invited.add(playerName);
    }

}
