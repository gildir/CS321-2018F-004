/**
 * Lets player interact with the shop. Many to 1 relationship with Shop class
 * @author Team 4: King
 *
 */
public class ShopClient {
	private Player player;
	private Shop shop;
	
	public ShopClient(Player p, Shop s) {
		this.player = p;
		this.shop = s;
		run();
	}
	
	private void run() {
		// Put text parser here and call 
		this.shop.addPlayer(this.player);
		
		System.out.println(this.shop);
		
		// TODO run infinite loop waiting for input that breaks when player exits
		// or runs through switch statement triggering shop methods
		
		this.shop.removePlayer(this.player);
	}
	
	//In terms of the player buying items
	public void buy(Object k) {}
	
	//In terms of the player selling items
	public void sell(Object k) {}
}
