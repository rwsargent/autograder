package autograder.mailer;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import autograder.configuration.Configuration;

public class Mailer {

	Properties mMailProps;
	public Mailer() {
		configureProperties();
	}
	
	public void sendMailWithAttachment(String recipient, String text, String subject, String body, File attachmentFile) {
		Session session = Session.getDefaultInstance(mMailProps);
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(Configuration.getConfiguration().senderEmail));
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
            
            Configuration config = Configuration.getConfiguration();
            Transport transport = session.getTransport("smtp");
            transport.connect(config.smtpHost, config.senderEmail, config.smtpPassword);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	private void configureProperties() {
		Configuration config = Configuration.getConfiguration();
		mMailProps = new Properties();
		mMailProps.put("mail.smtp.auth", "true");
		mMailProps.put("mail.smtp.starttls.enable", "true");
		mMailProps.put("mail.smtp.host", config.smtpHost);
		mMailProps.put("mail.smtp.port", config.smtpPort);
		mMailProps.put("mail.smtp.password", config.smtpPassword);
		mMailProps.put("mail.smtp.username", config.smtpUsername);
	}
}
