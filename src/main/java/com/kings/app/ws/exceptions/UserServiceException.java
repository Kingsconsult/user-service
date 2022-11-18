package com.kings.app.ws.exceptions;

public class UserServiceException extends RuntimeException{
	
	private static final long serialVersionUID = 5118387296819546256L;

	public UserServiceException(String message) 
	{
		super(message);
	}

}
