import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * All modules used by the recovery questions sub-wizard
 */
public class RecoveryQuestionsWizardModules {
	protected static class AddQuestionModule extends Wizard.WizardModule {

		public AddQuestionModule(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj, String playerName) {
			super(stdin, stdout, obj, playerName);
			this.listName = "Add Recovery Question";
		}

		@Override
		public void run() throws Exception {
			String question;
			String answer;
			Boolean test;
			stdout.println("Please enter the recovery question you would like to add");
			stdout.print(">");
			question = stdin.readLine().trim();
			if (question.length() < 4) {
				test = true;
				while (test) {
					stdout.println("Please submit a question that is at least 4 characters");
					stdout.print("> ");
					question = stdin.readLine().trim();
					if (question.length() >= 4) {
						test = false;
					}
				}
			}
			stdout.println("Please enter the answer");
			stdout.print("> ");
			answer = new String(System.console().readPassword()).trim().toLowerCase(); // task 221 hides password
			if (answer.length() < 4) {
				test = true;
				while (test) {
					stdout.println("Please submit an answer that is at least 4 characters");
					stdout.print("> ");
					answer = new String(System.console().readPassword()).trim().toLowerCase(); // task 221 hides
																								// password
					if (answer.length() >= 4) {
						test = false;
					}
				}
			}
			this.obj.addRecoveryQuestion(this.playerName, question, answer);
		}

		@Override
		public String getListName() {
			return this.listName;
		}

	}

	protected static class RemoveQuestionModule extends Wizard.WizardModule {

		public RemoveQuestionModule(BufferedReader stdin, PrintStream stdout, GameObjectInterface obj,
				String playerName) {
			super(stdin, stdout, obj, playerName);
			this.listName = "Remove Recovery Question";
		}

		@Override
		public void run() throws Exception {
			// TODO q
			DataResponse<ArrayList<String>> questions = obj.getQuestions(playerName);
			if (!questions.success()) {
				switch (questions.error) {
				case NOT_FOUND:
					stdout.println("The server couldn't find your account?");
					break;
				case INTERNAL_SERVER_ERROR:
					stdout.println("The server encountered an internal error");
					break;
				default:
					stdout.println("Unexpected server behavior");
				}
				return;
			}
			if (questions.data.size() == 0) {
				stdout.println("You have no recovery questions set");
				return;
			}
			int num = 0;
			String input;
			for (int i = 0; i < questions.data.size(); i++)
				stdout.println(i + 1 + " " + questions.data.get(i));
			stdout.println("Please select the number of the question you wish to remove");
			while (true) {
				stdout.print("> ");
				input = stdin.readLine().trim();
				try {
					num = Integer.parseInt(input);
				} catch (Exception e) {
					stdout.println("Please enter a number. -1 to quit");
					continue;
				}
				if (num == -1)
					return;
				if (num > questions.data.size() || num <= 0) {
					stdout.println("That was not one of the options. -1 to quit");
					continue;
				}
				break;
			}
			switch (this.obj.removeQuestion(this.playerName, num - 1)) {
			case NOT_FOUND:
				stdout.println("The server could not find your account?");
				break;
			case INTERNAL_SERVER_ERROR:
				stdout.println("The server is experiencing issues doing this");
				break;
			case FAILURE:
				stdout.println("The server said this isnt a question?");
				break;
			case SUCCESS:
				stdout.println("Question " + num + " has been removed");
				break;
			default:
				stdout.println("Unknown server behavior");
			}
		}

		@Override
		public String getListName() {
			return this.listName;
		}

	}

}
