package ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import dao.CoursDAO;
import dao.SalleDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Cours;
import models.Salle;

/**
 * Panel Alertes & Notifications — redesigné.
 * Affiche conflits horaires, cours imminents, programme du jour
 * et formulaire de notification email SMTP.
 * Logique métier inchangée.
 */
public class AlertesPanel {

    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DateTimeFormatter hf  = DateTimeFormatter.ofPattern("HH:mm");

    public ScrollPane createPanel() {
        VBox panel = new VBox(22);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        // ── En-tête ──────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label titre = Design.pageTitle("🔔  Alertes & Notifications");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnActualiser = Design.btnSecondary("🔄  Actualiser");
        header.getChildren().addAll(titre, spacer, btnActualiser);
        panel.getChildren().add(header);

        // ── Zone des alertes dynamiques ───────────────────────────────
        VBox contenuAlertes = new VBox(16);
        chargerAlertes(contenuAlertes);
        btnActualiser.setOnAction(e -> {
            contenuAlertes.getChildren().clear();
            chargerAlertes(contenuAlertes);
        });
        panel.getChildren().add(contenuAlertes);

        panel.getChildren().add(new Separator());

        // ── Notification Email SMTP ───────────────────────────────────
        VBox emailSection = Design.section("📧  Notification par Email (SMTP)");

        Label descEmail = Design.muted("Envoyez une notification email en cas de changement de salle. Configurez d'abord votre serveur SMTP.");

        // Config SMTP rétractable
        TitledPane configPane = new TitledPane();
        configPane.setText("⚙️  Configuration SMTP (à remplir une seule fois)");
        configPane.setExpanded(false);
        configPane.setStyle("-fx-font-size: 12;");

        GridPane gridSMTP = new GridPane();
        gridSMTP.setHgap(14); gridSMTP.setVgap(12);
        gridSMTP.setPadding(new Insets(14));

        TextField     tfSmtpHost = sf("smtp.gmail.com", 240); tfSmtpHost.setText("smtp.gmail.com");
        TextField     tfSmtpPort = sf("587", 80);             tfSmtpPort.setText("587");
        TextField     tfSmtpUser = sf("votre.email@gmail.com", 280);
        PasswordField pfSmtpPass = new PasswordField();
        pfSmtpPass.setPromptText("Mot de passe d'application Gmail");
        pfSmtpPass.setStyle(Design.INPUT_STYLE); pfSmtpPass.setPrefWidth(280);

        Label infoGmail = new Label("💡  Pour Gmail : activez la validation en 2 étapes puis créez un « Mot de passe d'application » dans vos paramètres Google.");
        infoGmail.setStyle("-fx-font-size:11;-fx-text-fill:" + Design.TEXT_MUTED + ";");
        infoGmail.setWrapText(true);

        gridSMTP.add(fl("Serveur SMTP :"),     0, 0); gridSMTP.add(tfSmtpHost, 1, 0);
        gridSMTP.add(fl("Port :"),             0, 1); gridSMTP.add(tfSmtpPort, 1, 1);
        gridSMTP.add(fl("Email expéditeur :"), 0, 2); gridSMTP.add(tfSmtpUser, 1, 2);
        gridSMTP.add(fl("Mot de passe :"),     0, 3); gridSMTP.add(pfSmtpPass, 1, 3);
        gridSMTP.add(infoGmail,                0, 4, 2, 1);

        configPane.setContent(new VBox(12, gridSMTP));

        // Formulaire notification
        VBox notifForm = new VBox(12);
        notifForm.setPadding(new Insets(4, 0, 0, 0));

        Label lblInfos = Design.sectionTitle("📋  Informations du changement");

        GridPane gridNotif = new GridPane();
        gridNotif.setHgap(14); gridNotif.setVgap(12);
        gridNotif.setPadding(new Insets(10, 0, 4, 0));

        TextField tfEmailDest = sf("email.dest@gmail.com", 300);
        TextField tfClasse    = sf("Ex: L2-Informatique", 220);
        TextField tfMatiere   = sf("Ex: Mathématiques", 220);
        TextField tfAncSalle  = sf("Ex: A101", 150);
        TextField tfNouvSalle = sf("Ex: B201", 150);
        TextField tfDate      = sf("Ex: Lundi 16/03 à 08h00", 280);

        gridNotif.add(fl("Email destinataire :"),  0, 0); gridNotif.add(tfEmailDest, 1, 0);
        gridNotif.add(fl("Classe concernée :"),    0, 1); gridNotif.add(tfClasse,    1, 1);
        gridNotif.add(fl("Matière :"),             0, 2); gridNotif.add(tfMatiere,   1, 2);
        gridNotif.add(fl("Ancienne salle :"),      0, 3); gridNotif.add(tfAncSalle,  1, 3);
        gridNotif.add(fl("Nouvelle salle :"),      0, 4); gridNotif.add(tfNouvSalle, 1, 4);
        gridNotif.add(fl("Date/Heure cours :"),    0, 5); gridNotif.add(tfDate,      1, 5);

        Label msgEmail = new Label(""); msgEmail.setWrapText(true);

        Button btnEnvoyer = Design.btnPrimary("📤  Envoyer la notification", Design.WARNING);
        btnEnvoyer.setOnAction(e -> {
            if (tfEmailDest.getText().isEmpty() || tfClasse.getText().isEmpty() ||
                tfMatiere.getText().isEmpty()   || tfAncSalle.getText().isEmpty() ||
                tfNouvSalle.getText().isEmpty()) {
                setMsg(msgEmail, "⚠️  Remplissez tous les champs.", Design.WARNING); return;
            }
            if (tfSmtpUser.getText().isEmpty() || pfSmtpPass.getText().isEmpty()) {
                setMsg(msgEmail, "⚠️  Configurez d'abord le serveur SMTP.", Design.DANGER); return;
            }
            envoyerEmailSMTP(
                tfSmtpHost.getText(), tfSmtpPort.getText(),
                tfSmtpUser.getText(), pfSmtpPass.getText(),
                tfEmailDest.getText(), tfClasse.getText(),
                tfMatiere.getText(), tfAncSalle.getText(),
                tfNouvSalle.getText(), tfDate.getText(), msgEmail
            );
        });

        notifForm.getChildren().addAll(lblInfos, gridNotif, btnEnvoyer, msgEmail);
        emailSection.getChildren().addAll(descEmail, configPane, notifForm);
        panel.getChildren().add(emailSection);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ── Chargement des alertes ────────────────────────────────────────
    private void chargerAlertes(VBox contenu) {
        List<String> conflits    = coursDAO.detecterConflits();
        List<Cours>  tousLesCours = coursDAO.obtenirTous();
        LocalDateTime maintenant  = LocalDateTime.now();

        // CONFLITS
        VBox secConflits = Design.section("⚠️  Conflits Horaires (" + conflits.size() + ")");
        if (!conflits.isEmpty()) {
            for (String c : conflits) {
				secConflits.getChildren().add(carteInfo(c, Design.DANGER, "#fdecea"));
			}
            contenu.getChildren().add(secConflits);
        }

        // COURS DANS LES 2H
        List<Cours> prochains = tousLesCours.stream()
            .filter(c -> c.getDateDebut().isAfter(maintenant)
                      && c.getDateDebut().isBefore(maintenant.plusHours(2)))
            .sorted((a, b) -> a.getDateDebut().compareTo(b.getDateDebut()))
            .collect(Collectors.toList());

        VBox secProchains = Design.section("⏰  Cours dans les 2 prochaines heures (" + prochains.size() + ")");
        if (prochains.isEmpty()) {
            secProchains.getChildren().add(carteInfo("Aucun cours imminent.", Design.TEXT_MUTED, Design.BG_LIGHT));
        } else {
            for (Cours c : prochains) {
                Salle s = salleDAO.obtenirParId(c.getSalleId());
                secProchains.getChildren().add(carteInfo(
                    "📚  " + c.getMatiere() + "  ·  " + c.getEnseignant() +
                    "  ·  " + c.getDateDebut().format(hf) + " → " + c.getDateFin().format(hf) +
                    "  ·  Salle " + (s != null ? s.getNumero() : "?") + "  ·  " + c.getClasse(),
                    "#1a5276", "#d6eaf8"
                ));
            }
        }
        contenu.getChildren().add(secProchains);

        // COURS AUJOURD'HUI
        List<Cours> auj = tousLesCours.stream()
            .filter(c -> c.getDateDebut().toLocalDate().equals(LocalDate.now()))
            .sorted((a, b) -> a.getDateDebut().compareTo(b.getDateDebut()))
            .collect(Collectors.toList());

        VBox secAuj = Design.section("📅  Programme du jour (" + auj.size() + " cours)");
        if (auj.isEmpty()) {
            secAuj.getChildren().add(carteInfo("Aucun cours planifié aujourd'hui.", Design.TEXT_MUTED, Design.BG_LIGHT));
        } else {
            for (Cours c : auj) {
                Salle   s     = salleDAO.obtenirParId(c.getSalleId());
                boolean passe = c.getDateFin().isBefore(maintenant);
                secAuj.getChildren().add(carteInfo(
                    (passe ? "✓  " : "→  ") +
                    c.getDateDebut().format(hf) + " – " + c.getDateFin().format(hf) +
                    "  |  " + c.getMatiere() + "  |  " + c.getEnseignant() +
                    "  |  Salle " + (s != null ? s.getNumero() : "?") + "  |  " + c.getClasse(),
                    passe ? Design.TEXT_MUTED : "#7d6608",
                    passe ? Design.BG_LIGHT   : "#fef9e7"
                ));
            }
        }
        contenu.getChildren().add(secAuj);
    }

    // ── Envoi SMTP (logique inchangée) ────────────────────────────────
    private void envoyerEmailSMTP(String host, String portStr, String user, String pass,
                                   String dest, String classe, String matiere,
                                   String ancSalle, String nouvSalle, String date, Label msg) {
        setMsg(msg, "⏳  Envoi en cours…", Design.INFO);

        new Thread(() -> {
            try {
                java.util.Properties props = new java.util.Properties();
                props.put("mail.smtp.auth",            "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host",            host);
                props.put("mail.smtp.port",            portStr);
                props.put("mail.smtp.ssl.trust",       host);

                String sujet = "[UNIV-SCHEDULER] Changement de salle — " + matiere + " — Classe " + classe;
                String corps =
                    "Bonjour,\n\n" +
                    "Nous vous informons d'un changement de salle :\n\n" +
                    "   Matière         : " + matiere   + "\n" +
                    "   Classe          : " + classe    + "\n" +
                    "   Date/Heure      : " + date      + "\n" +
                    "   Ancienne salle  : " + ancSalle  + "\n" +
                    "   Nouvelle salle  : " + nouvSalle + "\n\n" +
                    "Veuillez vous présenter dans la nouvelle salle.\n\n" +
                    "Cordialement,\nL'administration — UNIV-SCHEDULER";

                javax.mail.Authenticator auth = new javax.mail.Authenticator() {
                    @Override protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(user, pass);
                    }
                };

                javax.mail.Session session = javax.mail.Session.getInstance(props, auth);
                javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(session);
                message.setFrom(new javax.mail.internet.InternetAddress(user));
                message.setRecipients(javax.mail.Message.RecipientType.TO,
                    javax.mail.internet.InternetAddress.parse(dest));
                message.setSubject(sujet);
                message.setText(corps, "UTF-8");
                javax.mail.Transport.send(message);

                javafx.application.Platform.runLater(() ->
                    setMsg(msg, "✅  Email envoyé à : " + dest, Design.SUCCESS));

            } catch (Throwable ex) {
                javafx.application.Platform.runLater(() -> {
                    String err = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                    String affichage;
                    if (err.contains("535") || err.contains("534")) {
						affichage = "❌  Identifiants incorrects. Vérifiez votre mot de passe d'application Gmail.";
					} else if (err.contains("Connection refused")) {
						affichage = "❌  Connexion refusée. Vérifiez le serveur SMTP et le port.";
					} else if (ex instanceof NoClassDefFoundError || err.contains("javax/mail")) {
						affichage = "❌  javax.mail.jar manquant dans lib/. Voir lib/README.txt.";
					} else {
						affichage = "❌  Erreur : " + err;
					}
                    setMsg(msg, affichage, Design.DANGER);
                });
            }
        }).start();
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private Label carteInfo(String texte, String couleurTexte, String couleurFond) {
        Label lbl = new Label(texte);
        lbl.setStyle(
            "-fx-font-size:12;-fx-text-fill:" + couleurTexte + ";" +
            "-fx-padding:8 12;-fx-background-color:" + couleurFond + ";" +
            "-fx-border-radius:6;-fx-background-radius:6;"
        );
        lbl.setWrapText(true);
        return lbl;
    }

    private void setMsg(Label lbl, String text, String color) {
        lbl.setText(text);
        lbl.setStyle("-fx-font-size:12;-fx-text-fill:" + color + ";-fx-font-weight:bold;" +
            "-fx-padding:6 10;-fx-background-color:derive(" + color + ",85%);-fx-background-radius:6;");
    }

    private TextField sf(String prompt, double w) {
        TextField tf = new TextField();
        tf.setPromptText(prompt); tf.setPrefWidth(w); tf.setStyle(Design.INPUT_STYLE);
        return tf;
    }

    private Label fl(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";-fx-min-width:160;");
        return lbl;
    }
}
