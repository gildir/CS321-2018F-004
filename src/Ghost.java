import java.util.Random;

public class Ghost extends Thread{
		String speech = "sample speech";
		Player p;
		
		public Ghost(Player p){
			this.p = p;
		}

		@Override
		public void run() {
			Random rn = new Random();
			int i = rn.nextInt(100 - 1 + 1) + 1;
			if (i >= 15) {
			p.getReplyWriter().println("A ghost has appeared");
			p.getReplyWriter().println(this.speech);
			try {
				Thread.currentThread().sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			p.getReplyWriter().println("the ghost disappears");
			}
		}
	}