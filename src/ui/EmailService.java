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
     * Représente un email structuré.
     */
    public static class EmailEntry {
        public final String de;
        public final String objet;
        public final String date;
        public final String corps;
        public final boolean lu;

        public EmailEntry(String de, String objet, String date, String corps, boolean lu) {
            this.de    = de;
            this.objet = objet;
            this.date  = date;
            this.corps = corps;
            this.lu    = lu;
        }
    }

    /**
     * Lit les 20 derniers emails de la boîte de réception via IMAP.
     * Retourne une liste d'EmailEntry structurés.
     */
    public static java.util.List<EmailEntry> lireEmails(String host, String port,
                                                         String user, String password) {
        java.util.List<EmailEntry> result = new java.util.ArrayList<>();
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
            int debut = Math.max(1, total - 19);

            Message[] messages = inbox.getMessages(debut, total);
            for (int i = messages.length - 1; i >= 0; i--) {
                Message m = messages[i];
                String de    = m.getFrom() != null && m.getFrom().length > 0
                               ? m.getFrom()[0].toString() : "(expéditeur inconnu)";
                String objet = m.getSubject() != null ? m.getSubject() : "(sans objet)";
                String date  = m.getSentDate() != null
                               ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRENCH)
                                   .format(m.getSentDate())
                               : "";
                boolean lu   = m.isSet(javax.mail.Flags.Flag.SEEN);
                String corps;
                try {
                    Object content = m.getContent();
                    if (content instanceof String) {
                        String raw = (String) content;
                        corps = raw.substring(0, Math.min(600, raw.length()));
                    } else if (content instanceof Multipart) {
                        Multipart mp = (Multipart) content;
                        String found = "[Corps non lisible]";
                        for (int j = 0; j < mp.getCount(); j++) {
                            BodyPart bp = mp.getBodyPart(j);
                            if (bp.isMimeType("text/plain")) {
                                String t = (String) bp.getContent();
                                found = t.substring(0, Math.min(600, t.length()));
                                break;
                            }
                        }
                        corps = found;
                    } else {
                        corps = "[Contenu non textuel]";
                    }
                } catch (Exception ignored) {
                    corps = "[Corps non lisible]";
                }
                result.add(new EmailEntry(de, objet, date, corps, lu));
            }
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            result.add(new EmailEntry(
                "Erreur", "❌ Connexion IMAP impossible", "",
                "Détail : " + e.getMessage() + "\n\nVérifiez les paramètres et que l'accès IMAP est activé dans votre compte email.",
                false
            ));
        }
        return result;
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
