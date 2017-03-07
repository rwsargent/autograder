package autograder.mailer;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.inject.Inject;

import autograder.configuration.Configuration;

public class Mailer {
	
	private Properties mMailProps;
	
	private Configuration mConfig;
	
	@Inject
	public Mailer(Configuration configuration) {
		configureProperties();
	}
	
	public void sendMailWithAttachment(String recipient, String subject, String body, File attachmentFile) {
		Session session = Session.getInstance(mMailProps, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mConfig.smtpUsername, mConfig.smtpPassword);
			}
		});
		
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(mConfig.senderEmail));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			message.setSubject(subject);
			
			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			multipart.addBodyPart(messageBodyPart);
            
			messageBodyPart = new MimeBodyPart();
			DataSource attachment = new FileDataSource(attachmentFile);
			messageBodyPart.setDataHandler(new DataHandler(attachment));
	        messageBodyPart.setFileName(attachmentFile.getName());
	        multipart.addBodyPart(messageBodyPart);
	        
	        message.setContent(multipart);
            
	        Transport.send(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	private void configureProperties() {
		mMailProps = new Properties();
		mMailProps.put("mail.smtp.auth", "true");
		mMailProps.put("mail.smtp.starttls.enable", "true");
		mMailProps.put("mail.smtp.host", mConfig.smtpHost);
		mMailProps.put("mail.smtp.port", mConfig.smtpPort);
		mMailProps.put("mail.smtp.password", mConfig.smtpPassword);
		mMailProps.put("mail.smtp.username", mConfig.smtpUsername);
	}
}
