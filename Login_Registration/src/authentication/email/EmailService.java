package authentication.email;

import authentication.Init;
import javafx.application.Platform;
import javafx.scene.text.Text;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    public void sendMail(String sendEmailTo,
                         String emailSubject,
                         String emailText,
                         Text warningText,
                         String emailSentMessage,
                         String emailFailedMessage) {

        Init init = new Init();

        String sendEmailFrom = init.getUsrSmtp();

        Properties props = System.getProperties();
        props.put("mail.smtp.host", init.getHstSmtp());
        props.put("mail.smtp.socketFactory.port", init.getPrtSmtp());
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", init.getPrtSmtp());

        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(init.getUsrSmtp(), init.getPssSmtp());
            }
        });

        try {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sendEmailFrom));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(sendEmailTo));
            message.setSubject(emailSubject);
            message.setText(emailText);

            Transport.send(message);
            if (warningText != null)
                Platform.runLater(() -> warningText.setText(emailSentMessage));

        } catch (MessagingException mex) {
            if (warningText != null)
                warningText.setText(emailFailedMessage);
        }
    }

}