import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Standard wizard that displays a list of options for the user to select from
 */
public abstract class Wizard {

	private BufferedReader stdin;
	private GameObjectInterface obj;
	private String playerName;
	private PrintStream stdout;
	private TextMenu mainMenu;

	/**
	 * @param stdin
	 * @param stdout
	 * @param obj
	 * @param playerName
	 * @param wizardName
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Wizard(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName,
			String wizardName) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.stdin = stdin;
		this.stdout = stdout;
		this.obj = obj;
		this.playerName = playerName;

		mainMenu = new Wizard.TextMenu(wizardName, stdin, stdout);
	}

	/**
	 * Use this instead of System.out
	 * 
	 * @return printStream
	 */
	protected PrintStream out() {
		return this.stdout;
	}

	/**
	 * Add modules using this.. Returns self for factor like usage
	 * 
	 * @param classes
	 * @return wizard
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Wizard addModules(Class<?>... classes) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for (Class<?> c : classes) {
			Constructor<?> con = c.getConstructor(BufferedReader.class, PrintStream.class, GameObjectInterface.class,
					String.class);
			Wizard.WizardModule m = (Wizard.WizardModule) con.newInstance(stdin, stdout, obj, playerName);
			mainMenu.add(m.getListName(), m::run);
		}
		return this;
	}

	/**
	 * This displays the wizard
	 * 
	 * @throws IOException
	 */
	public void enter() throws IOException {
		if (mainMenu.actions.size() == 0) {
			stdout.println("Sorry, currently there are no spells this wizard can perform.");
			leave();
			return;
		}
		mainMenu.display();
		leave();
	}

	/**
	 * To be filled out. Any actions to take on wizard exit
	 */
	protected abstract void leave();

	@FunctionalInterface
	public static interface Action {
		public void run() throws java.lang.Exception;
	}

	/**
	 * Abstract module to be added to a wizard. This would be an option in a list
	 */
	protected abstract static class WizardModule {
		protected String listName;
		protected BufferedReader stdin;
		protected PrintStream stdout;
		protected GameObjectInterface obj;
		protected String playerName;

		/**
		 * @param stdin
		 * @param stdout
		 * @param obj
		 * @param playerName
		 */
		public WizardModule(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName) {
			this.stdin = stdin;
			this.stdout = stdout;
			this.obj = obj;
			this.playerName = playerName;
		}

		/**
		 * To be filled out. What to do when the option is selected
		 * 
		 * @throws java.lang.Exception
		 */
		public abstract void run() throws java.lang.Exception;

		/**
		 * To be filled out. What to display in the wizard list
		 * 
		 * @return
		 */
		public abstract String getListName();
	}

	/**
	 * The meat of the wizard. This contains the actions and the logic for selection
	 * actions
	 */
	public static class TextMenu {
		private String name;
		private int count = 0;
		private ArrayList<String> names = new ArrayList<>();
		private ArrayList<Action> actions = new ArrayList<>();
		private BufferedReader stdin;
		private PrintStream stdout;

		/**
		 * @param name
		 * @param stdin
		 * @param stdout
		 */
		public TextMenu(String name, BufferedReader stdin, PrintStream stdout) {
			this.name = name;
			this.stdin = stdin;
			this.stdout = stdout;
		}

		/**
		 * Adds an item to the menu
		 * 
		 * @param name
		 * @param action
		 * @return
		 */
		public TextMenu add(String name, Action action) {
			names.add(name);
			actions.add(action);
			count++;
			return this;
		}

		/**
		 * Displays the options and waits for a selection
		 * 
		 * @throws IOException
		 */
		public void display() throws IOException {
			while (true) {
				printHeader();
				printOptions();
				stdout.print("> ");
				String line = stdin.readLine().trim();
				int option;
				try {
					option = Integer.parseInt(line);
				} catch (Exception e) {
					stdout.println("Enter a number please...");
					continue;
				}
				if (option <= 0 || option > count + 1) {
					stdout.println("That was not one of the options...");
					continue;
				} else if (option == count + 1)
					break;
				try {
					actions.get(option - 1).run();
				} catch (Exception e) {
					stdout.println("There was a problem casting this spell");
				}
			}
		}

		private void printHeader() {
			for (int i = 0; i < this.name.length(); i++)
				stdout.print("_");
			stdout.println();
			stdout.println(this.name);
		}

		private void printOptions() {
			stdout.println();
			int i = 0;
			for (i = 0; i < count; i++) {
				stdout.printf("%" + ((count + "").length()) + "d. %s\n", i + 1, names.get(i));
			}
			stdout.println(i + 1 + ". Leave");
		}
	}

	/**
	 * Type of wizard that acts completely as expected. If wizard wasn't abstract it
	 * would be this
	 */
	public static class SimpleWizard extends Wizard {

		/**
		 * @param stdin
		 * @param stdout
		 * @param obj
		 * @param playerName
		 * @param wizardName
		 * @throws NoSuchMethodException
		 * @throws SecurityException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws IllegalArgumentException
		 * @throws InvocationTargetException
		 */
		public SimpleWizard(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName,
				String wizardName) throws NoSuchMethodException, SecurityException, InstantiationException,
				IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			super(stdin, stdout, obj, playerName, wizardName);
		}

		@Override
		protected void leave() {
		}

	}

	/**
	 * This type of wizard will steal System.out and replace it with a buffer. It
	 * will then pass the real stdout to any subwizards. When leaving it prints the
	 * buffer and returns stdout
	 */
	public static class BlockingWizard extends Wizard {
		private StringBuilder bufferedString = new StringBuilder();
		private PrintStream bufferedStream;

		/**
		 * @param stdin
		 * @param stdout
		 * @param obj
		 * @param playerName
		 * @param wizardName
		 * @throws NoSuchMethodException
		 * @throws SecurityException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws IllegalArgumentException
		 * @throws InvocationTargetException
		 */
		public BlockingWizard(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName,
				String wizardName) throws NoSuchMethodException, SecurityException, InstantiationException,
				IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			super(stdin, stdout, obj, playerName, wizardName);
			bufferedStream = new PrintStream(new OutputStream() {
				@Override
				public void write(int arg0) throws IOException {
					bufferedString.append((char) arg0);
				}
			});
		}

		@Override
		public void enter() throws IOException {
			System.setOut(bufferedStream);
			super.enter();
		}

		@Override
		protected void leave() {
			// This isn't a perfect solution...
			if (bufferedString.length() > 0) {
				out().println("Here were the messages you missed while in the wizard:");
				out().println(bufferedString.toString());
				bufferedString.setLength(0);
			}
			System.setOut(out());
		}
	}
}
