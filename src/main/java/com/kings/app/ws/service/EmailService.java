package com.kings.app.ws.service;

import com.kings.app.ws.email.EmailDetails;

public interface  EmailService {
	
	String sendsimpleMail(EmailDetails emailDetails);
	
    String sendMailWithAttachment(EmailDetails details);
}
