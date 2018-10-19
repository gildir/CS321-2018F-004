/**
 * Lets player interact with the shop. Many to 1 relationship with Shop class
 * @author Team 4: King
 *
 */
public class ShopClient {
	private String player;
	private int shopId;
	private GameObjectInterface remote;
	
	public ShopClient(String p, int s, GameObjectInterface remote) {
		this.player = p;
		this.shopId = s;
		this.remote = remote;
		run();
	}
	
	private void run() {
		// Put text parser here and call 
		
		System.out.println("It worked!");
		
		// TODO run infinite loop waiting for input that breaks when player exits
		// or runs through switch statement triggering shop methods
	
	}
	
	//In terms of the player buying items
	public void buy(Object k) {}
	
	//In terms of the player selling items
	public void sell(Object k) {}
}
