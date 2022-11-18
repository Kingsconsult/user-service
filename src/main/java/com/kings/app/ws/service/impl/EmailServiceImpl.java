package com.kings.app.ws.service.impl;

import java.io.File;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.kings.app.ws.email.EmailDetails;
import com.kings.app.ws.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	private JavaMailSender javaMailSender;
	
	@Value("${spring.mail.username}")
	private String sender;
	
	public String sendsimpleMail(EmailDetails details) {
		
		try {
			// Creating a simple mail message
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			
            // Setting up necessary details
			mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getMsgBody());
            mailMessage.setSubject(details.getSubject());
            
            // Sending the mail
            javaMailSender.send(mailMessage);
            return "Mail sent successfully...";
		}
		catch (Exception e) {
			return "Error while Sending Mail";
		}
		
	}
	
	public String sendMailWithAttachment(EmailDetails details) {
		
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper mimeMessaegHelper;
				
		try {
			// Setting multipart as true for attachments to be send
			mimeMessaegHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessaegHelper.setFrom(sender);
			mimeMessaegHelper.setTo(details.getRecipient());
			mimeMessaegHelper.setText(details.getMsgBody());
			mimeMessaegHelper.setSubject(details.getSubject());
			
			// Adding the attachment
			FileSystemResource file = new FileSystemResource(new File(details.getAttachment()));
			
			mimeMessaegHelper.addAttachment(file.getFilename(), file);
			
            // Sending the mail
            javaMailSender.send(mimeMessage);
            return "Mail sent successfully...";
		}
		catch (Exception e) {
			return "Error while Sending Mail";
		}
		
	}
}
