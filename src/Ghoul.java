import java.util.Random;

/*
* Requirement #704: As a game designer, I want a ghoul to wander around the map so that the game is more interesting.
*/


public class Ghoul{

	private int angryLevel; //Measure of a ghoul's anger on a scale of 0(calm) to 10(furious)
	private int currentRoom;
	private final int id;
	private static int totalGhoul = 0;
	private String ghoulName = null;

	//Summon a new Ghoul. New Ghoul will always very claim.
	public Ghoul(int room){
		Random rand = new Random();
		this.currentRoom = room;
		this.angryLevel = 0;
		this.id = Ghoul.totalGhoul;
		Ghoul.totalGhoul += 1;	
		int magic = rand.nextInt(0xfffffff)+this.id;
		String ghoulName = "";		
		while(magic > 0){
			//totally 94 chars can be used in ascii; #33 - #126
			char a = (char)(33 + (magic % 94));
			magic = magic / 94;
			ghoulName += a;
		}
		this.ghoulName = ghoulName;
	}

	public int getAngryLevel(){
		return this.angryLevel;
	}

	public int getRoom(){
		return this.currentRoom;
	}

	public int getId(){
		return this.id;
	}

	public boolean equals(Ghoul g){
		return (this.id == g.getId());
	}

	public String getTrueName(){
		return this.ghoulName;
	}

	public void setRoom(int room){
		this.currentRoom = room;
	}
	
	public void modifyAngryLevel(int amount) {
		this.angryLevel += amount;

		//add some error check......
		if(this.angryLevel > 10){
			this.angryLevel = 10;
		}else if (this.angryLevel < 0){
			this.angryLevel = 0;
		}
		
		return; 
	}
	
	public void Drag(Player p){
		if (this.angryLevel >= 7) {
		p.getReplyWriter().println("A Ghoul is about to drag you! Try to calm it down!");
		try {
			Thread.currentThread().sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		p.getReplyWriter().println("The Ghoul knocks you unconscious!");
		modifyAngryLevel(-1);
		
		}
	}

}
