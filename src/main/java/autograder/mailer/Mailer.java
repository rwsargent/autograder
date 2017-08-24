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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import autograder.configuration.Configuration;

public class Mailer {
	
	private Properties mailProps;
	private Configuration config;
	private static final Logger LOGGER = LoggerFactory.getLogger(Mailer.class);
	
	@Inject
	public Mailer(Configuration configuration) {
		configureProperties();
	}
	
	public void sendMail(String recipient, String subject, String body) {
		LOGGER.info("Sending email to " + recipient);
		Session session = getSession();
		
		MimeMessage message = new MimeMessage(session);
		try{
			setMessageInformation(recipient, subject, message);
			Multipart multipart = setBody(body);
			message.setContent(multipart);
	        Transport.send(message);
		} catch (MessagingException e) {
			LOGGER.error(e.getMessage());
		}
	}
	
	public void sendMailWithAttachment(String recipient, String subject, String body, File attachmentFile) {
		LOGGER.info("Sending email to " + recipient);
		Session session = getSession();
		MimeMessage message = new MimeMessage(session);
		try {
			setMessageInformation(recipient, subject, message);
			
			Multipart multipart = setBody(body);
			addAttachment(attachmentFile, multipart);
	        
	        message.setContent(multipart);
            
	        Transport.send(message);
		} catch (MessagingException e) {
			LOGGER.error(e.getMessage());
		}
	}

	private void setMessageInformation(String recipient, String subject, MimeMessage message)
			throws MessagingException, AddressException {
		message.setFrom(new InternetAddress(config.senderEmail));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		message.setSubject(subject);
	}

	private void addAttachment(File attachmentFile, Multipart multipart) throws MessagingException {
		BodyPart messageBodyPart;
		messageBodyPart = new MimeBodyPart();
		DataSource attachment = new FileDataSource(attachmentFile);
		messageBodyPart.setDataHandler(new DataHandler(attachment));
		messageBodyPart.setFileName(attachmentFile.getName());
		multipart.addBodyPart(messageBodyPart);
	}

	private Multipart setBody(String body) throws MessagingException {
		Multipart multipart = new MimeMultipart();
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(body);
		multipart.addBodyPart(messageBodyPart);
		return multipart;
	}

	private Session getSession() {
		Session session = Session.getInstance(mailProps, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(config.smtpUsername, config.smtpPassword);
			}
		});
		return session;
	}
	
	private void configureProperties() {
		mailProps = new Properties();
		mailProps.put("mail.smtp.auth", "true");
		mailProps.put("mail.smtp.starttls.enable", "true");
		mailProps.put("mail.smtp.host", config.smtpHost);
		mailProps.put("mail.smtp.port", config.smtpPort);
		mailProps.put("mail.smtp.password", config.smtpPassword);
		mailProps.put("mail.smtp.username", config.smtpUsername);
	}
}
