package es.udc.anpaproject.backend.model.exceptions;

@SuppressWarnings("serial")
public class IncorrectLoginException extends Exception {
	
	private String userName;
	private String password;

	public IncorrectLoginException() {
		
	}

	public IncorrectLoginException(String userName, String password) {
		
		this.userName = userName;
		this.password = password;
		
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}
	
}
