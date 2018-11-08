import java.io.BufferedReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class AccountEditWizard extends Wizard.BlockingWizard {

	private ArrayList<Class<?>> moduleClasses = new ArrayList<>();
	{
	}

	public AccountEditWizard(BufferedReader stdin, GameObjectInterface obj, String playerName)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		super(stdin, obj, playerName, "Account Edit Wizard");
		addModules(moduleClasses.toArray(new Class<?>[0]));
	}

}
