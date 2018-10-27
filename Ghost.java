import java.util.Random;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;


public class Ghost extends Thread{
		String speech = "sample speech";
		Player p;
		ArrayList<String> sayings;
		
		public Ghost(Player p){
			this.p = p;
			this.sayings = new ArrayList<String>();
			loadSayings();
		}

		@Override
		public void run() {
			Random rn = new Random();
			int i = rn.nextInt(100 - 1 + 1) + 1;
			if (i >= 75) {
				p.getReplyWriter().println("A ghost has appeared");
				int sayingIndex = rn.nextInt(this.sayings.size());
				p.getReplyWriter().println(this.sayings.get(sayingIndex));
				try {
					Thread.currentThread().sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				p.getReplyWriter().println("the ghost disappears");
			}
		}
		
		private void loadSayings() {
			File file = new File("ghost_sayings.txt");
			try {
				Scanner scan = new Scanner(file);
				while(scan.hasNextLine()) {
					this.sayings.add(scan.nextLine());
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}