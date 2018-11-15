import java.io.Serializable;

public class DataResponse<T> implements Serializable {
	private static final long serialVersionUID = -4461919513402487806L;

	public T data = null;
	public Responses error = null;

	public DataResponse(T data) {
		this.data = data;
	}

	public DataResponse(Responses error) {
		this.error = error;
	}

	public boolean success() {
		return error == null;
	}
}
