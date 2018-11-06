import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class AccountEditWizard {

	@FunctionalInterface
	public static interface Action {
		public void run();
	}

	protected abstract static class AccountWizardModule {
		protected String moduleName;
		protected BufferedReader stdin;
		protected GameObjectInterface obj;
		protected String playerName;

		public AccountWizardModule(BufferedReader stdin, GameObjectInterface obj, String playerName) {
			this.stdin = stdin;
			this.obj = obj;
			this.playerName = playerName;
		}

		public abstract void run();

		public abstract String getName();
	}

	public static class TextMenu {
		private String name;
		private int count = 0;
		private ArrayList<String> names = new ArrayList<>();
		private ArrayList<Action> actions = new ArrayList<>();
		private BufferedReader stdin;
		private PrintStream stdout;

		public TextMenu(String name, BufferedReader stdin, PrintStream stdout) {
			this.name = name;
			this.stdin = stdin;
			this.stdout = stdout;
		}

		public TextMenu add(String name, Action action) {
			names.add(name);
			actions.add(action);
			count++;
			return this;
		}

		public void display() throws IOException {
			for (int i = 0; i < this.name.length(); i++)
				stdout.print("_");
			stdout.println();
			stdout.println(this.name);
			while (true) {
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
				actions.get(option - 1).run();
			}
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

	private ArrayList<AccountWizardModule> modules = new ArrayList<>();
	private ArrayList<Class<?>> moduleClasses = new ArrayList<>();
	{
		moduleClasses.add(AccountEditWizardModules.ChangePasswordModule.class);
	}

	private BufferedReader stdin;
	private GameObjectInterface obj;
	private String playerName;
	private PrintStream printStream;
	private StringBuilder bufferedString = new StringBuilder();
	private PrintStream stdout;

	public AccountEditWizard(BufferedReader stdin, GameObjectInterface obj, String playerName)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		this.stdin = stdin;
		this.obj = obj;
		this.playerName = playerName;

		for (Class<?> c : moduleClasses) {
			Constructor<?> con = c.getConstructor(BufferedReader.class, GameObjectInterface.class, String.class);
			modules.add((AccountWizardModule) con.newInstance(stdin, obj, playerName));
		}

		this.printStream = new PrintStream(new OutputStream() {
			@Override
			public void write(int arg0) throws IOException {
				bufferedString.append((char) arg0);
			}
		});
	}

	public void enter() throws IOException {
		if (modules.size() == 0) {
			System.out.println("Sorry, currently there are no spells this wizard can perform.");
			return;
		}
		stdout = System.out;
		System.setOut(printStream);
		TextMenu mainMenu = new TextMenu("Account Edit Wizard", stdin, stdout);
		for (AccountWizardModule m : modules)
			mainMenu.add(m.getName(), m::run);
		mainMenu.display();
		leave();
	}

	private void leave() {
		stdout.println("Here were the messages you missed while in the wizard:");
		stdout.println(bufferedString.toString());
		bufferedString.setLength(0);
		System.setOut(stdout);
		stdout = null;
	}
}
