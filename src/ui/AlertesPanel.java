package ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import dao.CoursDAO;
import dao.SalleDAO;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import models.Cours;
import models.Salle;

public class AlertesPanel {

    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DateTimeFormatter hf  = DateTimeFormatter.ofPattern("HH:mm");

    public ScrollPane createPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #f5f6fa;");

        Label titre = new Label("🔔 Alertes & Notifications");
        titre.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button btnActualiser = new Button("🔄 Actualiser");
        btnActualiser.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 18;");

        VBox contenuAlertes = new VBox(12);
        chargerAlertes(contenuAlertes);
        btnActualiser.setOnAction(e -> {
            contenuAlertes.getChildren().clear();
            chargerAlertes(contenuAlertes);
        });

        // ─── NOTIFICATION EMAIL SMTP ───
        VBox boxEmail = creerSection("📧 Notification Email (SMTP)");

        Label descEmail = new Label("Envoyez une notification email en cas de changement de salle.");
        descEmail.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
        descEmail.setWrapText(true);

        // Config SMTP (panneau rétractable)
        TitledPane configPane = new TitledPane();
        configPane.setText("⚙️ Configuration SMTP (à remplir une seule fois)");
        configPane.setExpanded(false);

        GridPane gridSMTP = new GridPane();
        gridSMTP.setHgap(10);
        gridSMTP.setVgap(10);
        gridSMTP.setPadding(new Insets(12));

        TextField     tfSmtpHost = new TextField("smtp.gmail.com");
        TextField     tfSmtpPort = new TextField("587");
        tfSmtpPort.setPrefWidth(80);
        TextField     tfSmtpUser = new TextField();
        tfSmtpUser.setPromptText("votre.email@gmail.com");
        PasswordField pfSmtpPass = new PasswordField();
        pfSmtpPass.setPromptText("mot de passe d'application Gmail");

        Label infoGmail = new Label(
            "💡 Pour Gmail : activez la validation en 2 étapes puis créez un " +
            "'Mot de passe d'application' dans vos paramètres Google.");
        infoGmail.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");
        infoGmail.setWrapText(true);

        gridSMTP.add(new Label("Serveur SMTP :"),     0, 0); gridSMTP.add(tfSmtpHost, 1, 0);
        gridSMTP.add(new Label("Port :"),             0, 1); gridSMTP.add(tfSmtpPort, 1, 1);
        gridSMTP.add(new Label("Email expéditeur :"), 0, 2); gridSMTP.add(tfSmtpUser, 1, 2);
        gridSMTP.add(new Label("Mot de passe :"),     0, 3); gridSMTP.add(pfSmtpPass, 1, 3);
        gridSMTP.add(infoGmail,                       0, 4, 2, 1);

        configPane.setContent(new VBox(gridSMTP));

        // Formulaire notification
        GridPane gridNotif = new GridPane();
        gridNotif.setHgap(10);
        gridNotif.setVgap(10);
        gridNotif.setPadding(new Insets(10));

        TextField tfEmailDest = new TextField(); tfEmailDest.setPromptText("email.dest@gmail.com"); tfEmailDest.setPrefWidth(280);
        TextField tfClasse    = new TextField(); tfClasse.setPromptText("Ex: L2-Informatique");
        TextField tfMatiere   = new TextField(); tfMatiere.setPromptText("Ex: Mathématiques");
        TextField tfAncSalle  = new TextField(); tfAncSalle.setPromptText("Ex: A101");
        TextField tfNouvSalle = new TextField(); tfNouvSalle.setPromptText("Ex: B201");
        TextField tfDate      = new TextField(); tfDate.setPromptText("Ex: Lundi 16/03 à 08h00");

        gridNotif.add(new Label("Email destinataire :"),  0, 0); gridNotif.add(tfEmailDest, 1, 0);
        gridNotif.add(new Label("Classe concernée :"),    0, 1); gridNotif.add(tfClasse,    1, 1);
        gridNotif.add(new Label("Matière :"),             0, 2); gridNotif.add(tfMatiere,   1, 2);
        gridNotif.add(new Label("Ancienne salle :"),      0, 3); gridNotif.add(tfAncSalle,  1, 3);
        gridNotif.add(new Label("Nouvelle salle :"),      0, 4); gridNotif.add(tfNouvSalle, 1, 4);
        gridNotif.add(new Label("Date/Heure du cours :"), 0, 5); gridNotif.add(tfDate,      1, 5);

        Label msgEmail = new Label("");
        msgEmail.setStyle("-fx-font-size: 12;");
        msgEmail.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer la notification");
        btnEnvoyer.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

        btnEnvoyer.setOnAction(e -> {
            if (tfEmailDest.getText().isEmpty() || tfClasse.getText().isEmpty() ||
                tfMatiere.getText().isEmpty()   || tfAncSalle.getText().isEmpty() ||
                tfNouvSalle.getText().isEmpty()) {
                msgEmail.setText("⚠️ Remplissez tous les champs.");
                msgEmail.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12;");
                return;
            }
            if (tfSmtpUser.getText().isEmpty() || pfSmtpPass.getText().isEmpty()) {
                msgEmail.setText("⚠️ Configurez d'abord le serveur SMTP.");
                msgEmail.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                return;
            }
            envoyerEmailSMTP(
                tfSmtpHost.getText(), tfSmtpPort.getText(),
                tfSmtpUser.getText(), pfSmtpPass.getText(),
                tfEmailDest.getText(), tfClasse.getText(),
                tfMatiere.getText(), tfAncSalle.getText(),
                tfNouvSalle.getText(), tfDate.getText(), msgEmail
            );
        });

        boxEmail.getChildren().addAll(
            descEmail, configPane,
            new Label("Informations du changement :"),
            gridNotif, btnEnvoyer, msgEmail
        );

        panel.getChildren().addAll(titre, btnActualiser, contenuAlertes, new Separator(), boxEmail);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ── Chargement des alertes ────────────────────────────────────────
    private void chargerAlertes(VBox contenu) {
        List<String> conflits     = coursDAO.detecterConflits();
        List<Cours>  tousLesCours = coursDAO.obtenirTous();
        LocalDateTime maintenant  = LocalDateTime.now();

        // CONFLITS
        VBox secConflits = creerSection("⚠️ Conflits Horaires (" + conflits.size() + ")");
        if (conflits.isEmpty()) {
            secConflits.getChildren().add(
                carteAlerte("✅ Aucun conflit. L'emploi du temps est cohérent.", "#27ae60", "#eafaf1"));
        } else {
            for (String c : conflits) {
				secConflits.getChildren().add(carteAlerte(c, "#c0392b", "#fdecea"));
			}
        }
        contenu.getChildren().add(secConflits);

        // COURS DANS LES 2H
        List<Cours> prochains = tousLesCours.stream()
            .filter(c -> c.getDateDebut().isAfter(maintenant)
                      && c.getDateDebut().isBefore(maintenant.plusHours(2)))
            .sorted((a, b) -> a.getDateDebut().compareTo(b.getDateDebut()))
            .collect(Collectors.toList());

        VBox secProchains = creerSection("⏰ Cours dans les 2 prochaines heures (" + prochains.size() + ")");
        if (prochains.isEmpty()) {
            secProchains.getChildren().add(carteAlerte("Aucun cours imminent.", "#555", "#f5f6fa"));
        } else {
            for (Cours c : prochains) {
                Salle s = salleDAO.obtenirParId(c.getSalleId());
                secProchains.getChildren().add(carteAlerte(
                    "📚 " + c.getMatiere() + "  ·  " + c.getEnseignant() +
                    "  ·  " + c.getDateDebut().format(hf) + " → " + c.getDateFin().format(hf) +
                    "  ·  Salle " + (s != null ? s.getNumero() : "?") + "  ·  " + c.getClasse(),
                    "#1a5276", "#d6eaf8"));
            }
        }
        contenu.getChildren().add(secProchains);

        // COURS AUJOURD'HUI
        List<Cours> auj = tousLesCours.stream()
            .filter(c -> c.getDateDebut().toLocalDate().equals(LocalDate.now()))
            .sorted((a, b) -> a.getDateDebut().compareTo(b.getDateDebut()))
            .collect(Collectors.toList());

        VBox secAuj = creerSection("📅 Programme du jour (" + auj.size() + " cours)");
        if (auj.isEmpty()) {
            secAuj.getChildren().add(carteAlerte("Aucun cours planifié aujourd'hui.", "#555", "#f5f6fa"));
        } else {
            for (Cours c : auj) {
                Salle s   = salleDAO.obtenirParId(c.getSalleId());
                boolean passe = c.getDateFin().isBefore(maintenant);
                secAuj.getChildren().add(carteAlerte(
                    (passe ? "✓ " : "→ ") +
                    c.getDateDebut().format(hf) + " – " + c.getDateFin().format(hf) +
                    "  |  " + c.getMatiere() +
                    "  |  " + c.getEnseignant() +
                    "  |  Salle " + (s != null ? s.getNumero() : "?") +
                    "  |  " + c.getClasse(),
                    passe ? "#95a5a6" : "#7d6608",
                    passe ? "#f8f9fa"  : "#fef9e7"));
            }
        }
        contenu.getChildren().add(secAuj);
    }

    // ── Envoi SMTP ───────────────────────────────────────────────────
    private void envoyerEmailSMTP(String host, String portStr, String user, String pass,
                                   String dest, String classe, String matiere,
                                   String ancSalle, String nouvSalle, String date,
                                   Label msg) {
        msg.setText("⏳ Envoi en cours...");
        msg.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12;");

        new Thread(() -> {
            try {
                java.util.Properties props = new java.util.Properties();
                props.put("mail.smtp.auth",            "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host",            host);
                props.put("mail.smtp.port",            portStr);
                props.put("mail.smtp.ssl.trust",       host);

                String sujet =
                    "[UNIV-SCHEDULER] Changement de salle — " + matiere + " — Classe " + classe;
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
                    @Override
					protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(user, pass);
                    }
                };

                javax.mail.Session session = javax.mail.Session.getInstance(props, auth);
                javax.mail.internet.MimeMessage message =
                    new javax.mail.internet.MimeMessage(session);
                message.setFrom(new javax.mail.internet.InternetAddress(user));
                message.setRecipients(
                    javax.mail.Message.RecipientType.TO,
                    javax.mail.internet.InternetAddress.parse(dest));
                message.setSubject(sujet);
                // setText sur MimeMessage — aucune ambiguïté possible
                message.setText(corps, "UTF-8");

                javax.mail.Transport.send(message);

                javafx.application.Platform.runLater(() -> {
                    msg.setText("✅ Email envoyé à : " + dest);
                    msg.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
                });

            } catch (Throwable ex) {
                // Throwable couvre à la fois Exception et Error (NoClassDefFoundError)
                javafx.application.Platform.runLater(() -> {
                    String err = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                    String affichage;
                    if (err.contains("535") || err.contains("534")) {
                        affichage = "❌ Identifiants incorrects. Vérifiez votre mot de passe d'application Gmail.";
                    } else if (err.contains("Connection refused")) {
                        affichage = "❌ Connexion refusée. Vérifiez le serveur SMTP et le port.";
                    } else if (ex instanceof NoClassDefFoundError || err.contains("javax/mail")) {
                        affichage = "❌ javax.mail.jar manquant dans lib/. Voir lib/README.txt.";
                    } else {
                        affichage = "❌ Erreur : " + err;
                    }
                    msg.setText(affichage);
                    msg.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                });
            }
        }).start();
    }

    // ── Helpers UI ───────────────────────────────────────────────────
    private VBox creerSection(String titre) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 6; -fx-background-color: white;");
        Label lbl = new Label(titre);
        lbl.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        box.getChildren().add(lbl);
        return box;
    }

    private Label carteAlerte(String texte, String couleurTexte, String couleurFond) {
        Label lbl = new Label(texte);
        lbl.setStyle(
            "-fx-font-size: 12;" +
            "-fx-text-fill: " + couleurTexte + ";" +
            "-fx-padding: 8 12;" +
            "-fx-background-color: " + couleurFond + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;");
        lbl.setWrapText(true);
        return lbl;
    }
}
