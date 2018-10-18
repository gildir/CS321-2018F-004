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
		System.out.println("Hurray, you shopped!");
	}
}
