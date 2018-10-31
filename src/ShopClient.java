import java.rmi.RemoteException;
import java.util.Scanner;
/**
 * Lets player interact with the shop. Many to 1 relationship with Shop class
 * @author Team 4: King
 *
 */
public class ShopClient {
	private String player;
	private int id;
	private GameObjectInterface remote;
	
	public ShopClient(String p, int s, GameObjectInterface remote) throws RemoteException {
		this.player = p;
		this.id = s;
		this.remote = remote;
		run();
	}
	
	private void run() throws RemoteException {
		// Put text parser here and call 
		// TODO run infinite loop waiting for input that breaks when player exits
		// or runs through switch statement triggering shop methods
		
	}
	
	//In terms of the player buying items
	public void buy(Object k) {}
	
	//In terms of the player selling items
	public void sell(String item) throws RemoteException{
		double val = remote.sellItem(this.player, this.id, item);
		if (val != 0) {
			System.out.printf("You have sold %s for $%.2f\n", item, val);
		}
		else {
			System.out.println("You must have a " + item + " in your inventory to sell one.");
		}
	}
}
