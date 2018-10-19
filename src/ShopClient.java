import java.rmi.RemoteException;

/**
 * Lets player interact with the shop. Many to 1 relationship with Shop class
 * @author Team 4: King
 *
 */
public class ShopClient {
	private String player;
	private int id;
	private GameObjectInterface remote;
//	private Shop shop;
	
	public ShopClient(String p, int s, GameObjectInterface remote) throws RemoteException {
		this.player = p;
		this.id = s;
		this.remote = remote;
		run();
	}
	
	private void run() throws RemoteException {
		// Put text parser here and call 
		//System.out.println(remote.getShopStr(this.id));
		
		// TODO run infinite loop waiting for input that breaks when player exits
		// or runs through switch statement triggering shop methods
	
	}
	
	//In terms of the player buying items
	public void buy(Object k) {}
	
	//In terms of the player selling items
	public void sell(Object k) {}
}
