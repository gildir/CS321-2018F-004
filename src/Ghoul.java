import java.util.Random;

/*
* Requirement #704: As a game designer, I want a ghoul to wander around the map so that the game is more interesting.
*/


public class Ghoul{

	private int angryLevel;
	private int currentRoom;

	//Summon a new Ghoul. New Ghoul will always very claim.
	public Ghoul(int room){
		this.currentRoom = room;
		this.angryLevel = 0;
	}	

	public int getAngryLevel(){
		return this.angryLevel;
	}

	public int getRoom(){
		return this.currentRoom;
	}
	public void setRoom(int room){
		this.currentRoom = room;
	}

}
