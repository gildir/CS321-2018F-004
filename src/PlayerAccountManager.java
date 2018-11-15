import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerAccountManager {
	private File accountFolder;
	private HashSet<String> playerIds;
	private Logger logger;
	private HashMap<String, PlayerAccount> accountsInProcess;
	private HashSet<String> accountsMarkedToRemove;
	private Object accountLock = new Object();

	private Thread accountCleanup = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				synchronized (accountLock) {
					for (String s : accountsMarkedToRemove)
						PlayerAccountManager.this.forceUpdateAccountFile(accountsInProcess.remove(s));
					accountsMarkedToRemove.clear();
				}
				try {
					TimeUnit.SECONDS.sleep(30);
				} catch (InterruptedException e) {
				}
			}
		}
	});

	public void markAccount(String name) {
		synchronized (accountLock) {
			accountsMarkedToRemove.add(name);
		}
	}

	public PlayerAccountManager(String folderName) throws Exception {
		accountFolder = new File(folderName);
		if (accountFolder.exists() && !accountFolder.isDirectory())
			throw new Exception();
		if (!accountFolder.exists())
			accountFolder.mkdir();
		playerIds = new HashSet<>();
		for (File playerAccF : accountFolder.listFiles())
			if (playerAccF.isDirectory())
				playerIds.add(playerAccF.getName());
		System.out.printf("Found %d player accounts\n", playerIds.size());
		accountsInProcess = new HashMap<>();
		accountsMarkedToRemove = new HashSet<>();
		logger = Logger.getLogger(PlayerAccountManager.class.getName());
		accountCleanup.setDaemon(true);
		accountCleanup.start();
	}

	/**
	 * Creates a new player and account data. Returns the player.<br>
	 * <br>
	 * Possible Errors:<br>
	 * USERNAME_TAKEN<br>
	 * BAD_USERNAME_FORMAT<br>
	 * INTERNAL_SERVER_ERROR<br>
	 * 
	 * @param username Desired username for new account
	 * @param password Hashed password for account
	 * @return player
	 */
	public synchronized DataResponse<Player> createNewAccount(String username, String password) {
		String lower = username.toLowerCase();
		if (accountExists(lower))
			return new DataResponse<Player>(Responses.USERNAME_TAKEN);
		if (!lower.matches("^[a-zA-Z 0-9]{2,15}$"))
			return new DataResponse<Player>(Responses.BAD_USERNAME_FORMAT);
		File userDir = new File(accountFolder.getAbsolutePath() + "/" + lower);
		try {
			playerIds.add(lower);

			Player p = new Player(username);
			PlayerAccount a = new PlayerAccount(username, password);
			userDir.mkdir();
			writePlayerDataFile(p);
			writeAccountDataFile(a);
			return new DataResponse<Player>(p);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
			playerIds.remove(lower);
			userDir.delete();
			return new DataResponse<Player>(Responses.INTERNAL_SERVER_ERROR);
		}
	}

	private void writePlayerDataFile(Player p) throws Exception {
		File userDir = new File(accountFolder.getAbsolutePath() + "/" + p.getName().toLowerCase());
		FileOutputStream dataFile = new FileOutputStream(userDir.getAbsolutePath() + "/data.json");
		dataFile.write(JsonMarshaller.MARSHALLER.marshalIndent(p).getBytes());
		dataFile.close();
	}

	private void writeAccountDataFile(PlayerAccount a) throws Exception {
		File userDir = new File(accountFolder.getAbsolutePath() + "/" + a.getName().toLowerCase());
		FileOutputStream accountFile = new FileOutputStream(userDir.getAbsolutePath() + "/account.json");
		accountFile.write(JsonMarshaller.MARSHALLER.marshalIndent(a).getBytes());
		accountFile.close();
	}

	public void forceUpdatePlayerFile(Player p) {
		try {
			writePlayerDataFile(p);
		} catch (Exception e) {
		}
	}

	public void forceUpdateAccountFile(PlayerAccount a) {
		try {
			writeAccountDataFile(a);
		} catch (Exception e) {
		}
	}

	public boolean deleteAccount(String username) {
		username = username.toLowerCase();
		if (!playerIds.contains(username))
			return false;
		File userDir = new File(accountFolder.getAbsolutePath() + "/" + username);
		for (File f : userDir.listFiles())
			f.delete();
		if (!userDir.delete())
			return false;
		playerIds.remove(username);
		return true;
	}

	/**
	 * Returns the player data. This is why the password is necessary.<br>
	 * <br>
	 * Possible Errors:<br>
	 * NOT_FOUND<br>
	 * BAD_PASSWORD<Br>
	 * INTERNAL_SERVER_ERROR<br>
	 * 
	 * @param username
	 * @param password
	 * @return player
	 */
	public DataResponse<Player> getPlayer(String username, String password) {
		username = username.toLowerCase();
		if (!accountExists(username))
			return new DataResponse<Player>(Responses.NOT_FOUND);
		File userData = new File(accountFolder.getAbsolutePath() + "/" + username + "/data.json");
		File accountData = new File(accountFolder.getAbsolutePath() + "/" + username + "/account.json");
		if (!userData.exists() || !accountData.exists())
			return new DataResponse<Player>(Responses.INTERNAL_SERVER_ERROR);
		DataResponse<PlayerAccount> ar = getAccount(username);
		if (!ar.success())
			return new DataResponse<Player>(ar.error);
		PlayerAccount a = ar.data;
		if (a.verifyPassword(username, password) != Responses.SUCCESS)
			return new DataResponse<Player>(Responses.BAD_PASSWORD);
		Player p;
		try {
			p = JsonMarshaller.MARSHALLER.unmarshalFile(userData.getAbsolutePath(), Player.class);
		} catch (Exception e) {
			logger.log(Level.SEVERE, null, e);
			return new DataResponse<Player>(Responses.INTERNAL_SERVER_ERROR);
		}
		return new DataResponse<Player>(p);
	}

	/**
	 * Returns the player account, not the player. This contains password, reset,
	 * etc.<br>
	 * <br>
	 * Possible Errors:<br>
	 * NOT_FOUND<br>
	 * INTERNAL_SERVER_ERROR<br>
	 * 
	 * @param username
	 * @return playerAccount
	 */
	public DataResponse<PlayerAccount> getAccount(String username) {
		username = username.toLowerCase();
		if (!accountExists(username))
			return new DataResponse<PlayerAccount>(Responses.NOT_FOUND);
		File accountData = new File(accountFolder.getAbsolutePath() + "/" + username + "/account.json");
		if (!accountData.exists())
			return new DataResponse<PlayerAccount>(Responses.INTERNAL_SERVER_ERROR);
		PlayerAccount a;
		synchronized (accountLock) {
			a = accountsInProcess.get(username);
		}
		if (a == null)
			try {
				a = JsonMarshaller.MARSHALLER.unmarshalFile(accountData.getAbsolutePath(), PlayerAccount.class);
				synchronized (accountLock) {
					accountsInProcess.put(a.getName(), a);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, null, e);
				return new DataResponse<PlayerAccount>(Responses.INTERNAL_SERVER_ERROR);
			}
		return new DataResponse<PlayerAccount>(a);
	}

	public boolean accountExists(String username) {
		return playerIds.contains(username);
	}
}
