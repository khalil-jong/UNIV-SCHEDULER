package ui;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Service d'envoi et de lecture d'emails via SMTP/IMAP.
 * Nécessite javax.mail.jar dans lib/ et ajouté au Build Path.
 */
public class EmailService {

    /**
     * Teste la connexion SMTP sans envoyer de message.
     */
    public static boolean testerConnexion(String host, String port,
                                           String user, String password) {
        try {
            Properties props = smtpProps(host, port);
            Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });
            Transport transport = session.getTransport("smtp");
            transport.connect(host, Integer.parseInt(port), user, password);
            transport.close();
            return true;
        } catch (Exception e) {
            System.err.println("Test SMTP échoué : " + e.getMessage());
            return false;
        }
    }

    /**
     * Envoie un email via SMTP.
     */
    public static boolean envoyerEmail(String host, String port,
                                        String user, String password,
                                        String destinataire, String sujet, String corps) {
        try {
            Properties props = smtpProps(host, port);
            Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                });
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(corps);
            Transport.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("Envoi email échoué : " + e.getMessage());
            return false;
        }
    }

    /**
     * Lit les 20 derniers emails de la boîte de réception via IMAP.
     */
    public static String lireEmails(String host, String port,
                                     String user, String password) {
        StringBuilder sb = new StringBuilder();
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.host", host);
            props.put("mail.imaps.port", port);
            props.put("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect(host, Integer.parseInt(port), user, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            int total = inbox.getMessageCount();
            int debut = Math.max(1, total - 19); // 20 derniers

            Message[] messages = inbox.getMessages(debut, total);
            for (int i = messages.length - 1; i >= 0; i--) {
                Message m = messages[i];
                sb.append("─────────────────────────────────\n");
                sb.append("De      : ").append(m.getFrom()[0]).append("\n");
                sb.append("Objet   : ").append(m.getSubject()).append("\n");
                sb.append("Date    : ").append(m.getSentDate()).append("\n");
                sb.append("Message :\n");
                try {
                    Object content = m.getContent();
                    if (content instanceof String) {
                        sb.append(((String)content).substring(0, Math.min(500, ((String)content).length())));
                    } else if (content instanceof Multipart) {
                        Multipart mp = (Multipart) content;
                        for (int j = 0; j < mp.getCount(); j++) {
                            BodyPart bp = mp.getBodyPart(j);
                            if (bp.isMimeType("text/plain")) {
                                String t = (String) bp.getContent();
                                sb.append(t.substring(0, Math.min(500, t.length())));
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) { sb.append("[Corps non lisible]"); }
                sb.append("\n\n");
            }
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            sb.append("❌ Erreur IMAP : ").append(e.getMessage()).append("\n");
            sb.append("Vérifiez les paramètres et que l'accès IMAP est activé dans votre compte email.");
        }
        return sb.toString();
    }

    private static Properties smtpProps(String host, String port) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", host);
        return props;
    }
}
