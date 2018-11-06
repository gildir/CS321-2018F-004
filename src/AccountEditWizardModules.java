import java.io.BufferedReader;

public class AccountEditWizardModules {
	protected static class ChangePasswordModule extends AccountEditWizard.AccountWizardModule {
		public ChangePasswordModule(BufferedReader stdin, GameObjectInterface obj, String playerName) {
			super(stdin, obj, playerName);
			this.moduleName = "Change Password";
		}

		public String getName() {
			return this.moduleName;
		}

		public void run() {
			System.out.println("Blah blah");
		}
	}
}
