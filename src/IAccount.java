import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interface for anything accessing player account functions, aka the gameobject
 * and gamecore
 */
public interface IAccount {
	public static interface Server {
		public Responses removeQuestion(String name, int num);

		public DataResponse<ArrayList<String>> getQuestions(String name);

		public Responses verifyAnswers(String name, ArrayList<String> answers);

		public Responses addRecoveryQuestion(String name, String question, String answer);

		public DataResponse<Long> getAccountAge(String name);

		public Responses verifyPassword(String name, String password);

		public Responses changePassword(String name, String password);
	}

	public static interface Client {
		/**
		 * Remove question by position in list. Returns the status of the removal.<br>
		 * <br>
		 * Possible Responses:<br>
		 * NOT_FOUND<br>
		 * INTERNAL_SERVER_ERROR<br>
		 * FAILURE<br>
		 * SUCCESS<br>
		 * 
		 * @param name
		 * @param num  - which question
		 * @return removedStatus
		 * @throws RemoteException
		 */
		public Responses removeQuestion(String name, int num) throws RemoteException;

		/**
		 * Returns either the questions or a status error<br>
		 * <br>
		 * Possible Errors:<br>
		 * NOT_FOUND<br>
		 * INTERNAL_SERVER_ERROR<br>
		 * 
		 * @param name
		 * @return questionsStatus
		 * @throws RemoteException
		 */
		public DataResponse<ArrayList<String>> getQuestions(String name) throws RemoteException;

		/**
		 * Returns whether the answers were correct or an error occurred.<br>
		 * <br>
		 * Possible Responses:<br>
		 * NOT_FOUND<br>
		 * INTERNAL_SERVER_ERROR<br>
		 * FAILURE<br>
		 * SUCCESS<br>
		 * 
		 * @param name
		 * @param answers
		 * @return verifiedStatus
		 * @throws RemoteException
		 */
		public Responses verifyAnswers(String name, ArrayList<String> answers) throws RemoteException;

		/**
		 * Returns the status of adding a recovery question.<br>
		 * <br>
		 * Possible Responses:<br>
		 * NOT_FOUND<br>
		 * INTERAL_SERVER_ERROR<br>
		 * FAILURE - bad question length<br>
		 * BAD_PASSWORD - need answer<br>
		 * SUCCESS<br>
		 * 
		 * @param name
		 * @param question
		 * @param answer
		 * @return addStatus
		 * @throws RemoteException
		 */
		public Responses addRecoveryQuestion(String name, String question, String answer) throws RemoteException;

		/**
		 * Returns either account age or error.<br>
		 * <br>
		 * Possible Errors:<br>
		 * NOT_FOUND<br>
		 * INTERAL_SERVER_ERROR<br>
		 * 
		 * @param name return ageStatus
		 * @throws RemoteException
		 */
		public DataResponse<Long> getAccountAge(String name) throws RemoteException;

		/**
		 * Returns the status of testing a password against current.<br>
		 * <br>
		 * Possible Responses:<br>
		 * NOT_FOUND<br>
		 * INTERNAL_SERVER_ERROR<br>
		 * FAILURE<br>
		 * SUCCESS<br>
		 * 
		 * @param name
		 * @param password
		 * @return verifyStatus
		 * @throws RemoteException
		 */
		public Responses verifyPassword(String name, String password) throws RemoteException;

		/**
		 * Returns the status of changing a password.<br>
		 * <br>
		 * Possible Responses:<br>
		 * NOT_FOUND<br>
		 * INTERNAL_SERVER_ERROR<br>
		 * SUCCESS<br>
		 * 
		 * @param name
		 * @param newPassword
		 * @return changeStatus
		 * @throws RemoteException
		 */
		public Responses changePassword(String name, String password) throws RemoteException;

	}
}
