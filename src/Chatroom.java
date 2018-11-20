import java.util.ArrayList;

public class Chatroom {
	private ArrayList<Player> members = null;
	private ArrayList<Player> invited = null;
	private String chatName = "";
	
	public Chatroom(Player player, String chatName) {
		this.members = new ArrayList<Player>();
		this.invited = new ArrayList<Player>();
		this.addMember(player);
		this.setName(chatName);
	}
	
    public String getName() {
        return chatName;
    }

    public void setName(String chatName) {
        this.chatName = chatName;
    }
    
    public ArrayList<Player> getMembers() {
        return members;
    }

    public void addMember(Player player) {
		members.add(player);
    }
    
    public void removeMember(Player player) {
		members.remove(player);
    }
    
    public ArrayList<Player> getInvited() {
        return invited;
    }

    public void addInvited(Player player) {
		invited.add(player);
    }
    
    public void removeInvited(Player player) {
		invited.remove(player);
    }

}
