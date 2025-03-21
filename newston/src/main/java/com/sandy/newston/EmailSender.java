package com.sandy.newston;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
@Component
public class EmailSender {
    private static final Gson gson = new Gson();
    @Value("${sandy.newston.sender-email}")
    private String sender;
    @Value("${sandy.newston.sender-password}")
    private String password;
    @Value("${sandy.newston.receiver-email}")
    private String receiverStr;
    public void sendEmail(String subject, String body) {
        log.info("email:from[{}] to[{}].",sender,receiverStr);
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.163.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            JsonArray receiverJson = JsonParser.parseString(receiverStr).getAsJsonArray();
            InternetAddress[] receiverAddresses = new InternetAddress[receiverJson.size()];
            for (int i = 0; i < receiverJson.size(); i++) {
                receiverAddresses[i] = new InternetAddress(receiverJson.get(i).getAsString());
            }
            message.setRecipients(Message.RecipientType.TO, receiverAddresses);

            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            log.info("Email sent successfully");
        } catch (MessagingException e) {
            log.error("Email sending failed: " + e.getMessage());
        }
    }
}
