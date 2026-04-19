package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
//import javafx.application.Platform;
//import javafx.scene.layout.HBox;

/**
 * Panel partagé Envoi / Réception email.
 * Utilisé à la fois par l'Admin et le Gestionnaire.
 * CORRECTION : le Gestionnaire avait uniquement "Alertes & Conflits" dans ce menu,
 *              sans accès aux emails. Ce panel corrige cette incohérence.
 */
public class EmailGestionPanel {

    public ScrollPane createPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("📧  Envoi et Réception d'Emails");
        Label desc  = Design.muted("Configurez votre compte SMTP une seule fois, puis envoyez et lisez vos emails directement depuis l'application.");
        panel.getChildren().addAll(titre, desc);

        // ── Configuration SMTP ──────────────────────────────────────
        VBox boxConfig = Design.section("⚙️  Configuration SMTP");
        Label noteConfig = Design.muted(
            "Pour Gmail : activez la validation en 2 étapes et créez un « Mot de passe d'application » dédié.\n" +
            "Ce mot de passe remplace votre mot de passe Gmail habituel pour les applications tierces."
        );

        javafx.scene.layout.GridPane gSmtp = new javafx.scene.layout.GridPane();
        gSmtp.setHgap(12); gSmtp.setVgap(10); gSmtp.setPadding(new Insets(10, 0, 0, 0));

        TextField tfHost = styledField("smtp.gmail.com", 200);
        TextField tfPort = styledField("587", 80);
        TextField tfUser = styledField("", 260); tfUser.setPromptText("votre.email@gmail.com");
        PasswordField pfPass = new PasswordField();
        pfPass.setPromptText("Mot de passe d'application");
        pfPass.setPrefWidth(260); pfPass.setStyle(Design.INPUT_STYLE);

        gSmtp.add(fieldLabel("Serveur SMTP :"), 0, 0); gSmtp.add(tfHost, 1, 0);
        gSmtp.add(fieldLabel("Port :"),          0, 1); gSmtp.add(tfPort, 1, 1);
        gSmtp.add(fieldLabel("Email :"),         0, 2); gSmtp.add(tfUser, 1, 2);
        gSmtp.add(fieldLabel("Mot de passe :"),  0, 3); gSmtp.add(pfPass, 1, 3);

        Button btnTest = Design.btnPrimary("🔌  Tester la connexion", Design.INFO);
        Label msgSmtp  = new Label(""); msgSmtp.setStyle("-fx-font-size:12;"); msgSmtp.setWrapText(true);

        btnTest.setOnAction(e -> {
            if (tfUser.getText().isEmpty() || pfPass.getText().isEmpty()) {
                msgSmtp.setText("⚠️  Remplissez l'email et le mot de passe.");
                msgSmtp.setStyle("-fx-text-fill: " + Design.WARNING + "; -fx-font-size:12;"); return;
            }
            msgSmtp.setText("🔄 Test en cours..."); msgSmtp.setStyle("-fx-font-size:12;");
            new Thread(() -> {
                boolean ok = EmailService.testerConnexion(
                    tfHost.getText().trim(), tfPort.getText().trim(),
                    tfUser.getText().trim(), pfPass.getText());
                javafx.application.Platform.runLater(() -> {
                    if (ok) {
                        msgSmtp.setText("✅  Connexion SMTP réussie.");
                        msgSmtp.setStyle("-fx-text-fill: " + Design.SUCCESS + "; -fx-font-size:12; -fx-font-weight:bold;");
                    } else {
                        msgSmtp.setText("❌  Échec — vérifiez les paramètres et votre mot de passe d'application.");
                        msgSmtp.setStyle("-fx-text-fill: " + Design.DANGER + "; -fx-font-size:12; -fx-font-weight:bold;");
                    }
                });
            }).start();
        });

        boxConfig.getChildren().addAll(noteConfig, gSmtp, btnTest, msgSmtp);

        // ── Envoi d'email ────────────────────────────────────────────
        VBox boxEnvoi = Design.section("📤  Envoyer un email");
        javafx.scene.layout.GridPane gEnvoi = new javafx.scene.layout.GridPane();
        gEnvoi.setHgap(12); gEnvoi.setVgap(10); gEnvoi.setPadding(new Insets(10, 0, 0, 0));

        TextField tfTo    = styledField("", 360); tfTo.setPromptText("destinataire@example.com");
        TextField tfSujet = styledField("", 360); tfSujet.setPromptText("Objet du message");
        TextArea  taCorps = new TextArea();
        taCorps.setPromptText("Corps du message..."); taCorps.setPrefHeight(120);
        taCorps.setWrapText(true); taCorps.setStyle(Design.INPUT_STYLE);

        gEnvoi.add(fieldLabel("À :"),     0, 0); gEnvoi.add(tfTo,    1, 0);
        gEnvoi.add(fieldLabel("Objet :"), 0, 1); gEnvoi.add(tfSujet, 1, 1);
        gEnvoi.add(fieldLabel("Corps :"), 0, 2); gEnvoi.add(taCorps, 1, 2);

        Label msgEnvoi = new Label(""); msgEnvoi.setStyle("-fx-font-size:12;"); msgEnvoi.setWrapText(true);
        Button btnEnvoyer = Design.btnPrimary("📤  Envoyer", "#8e44ad");
        btnEnvoyer.setOnAction(e -> {
            if (tfTo.getText().isEmpty() || tfSujet.getText().isEmpty() || taCorps.getText().isEmpty()) {
                msgEnvoi.setText("⚠️  Destinataire, objet et corps sont obligatoires.");
                msgEnvoi.setStyle("-fx-text-fill:" + Design.WARNING + ";-fx-font-size:12;"); return;
            }
            if (tfUser.getText().isEmpty() || pfPass.getText().isEmpty()) {
                msgEnvoi.setText("⚠️  Configurez d'abord le compte SMTP ci-dessus.");
                msgEnvoi.setStyle("-fx-text-fill:" + Design.WARNING + ";-fx-font-size:12;"); return;
            }
            msgEnvoi.setText("🔄 Envoi en cours..."); msgEnvoi.setStyle("-fx-font-size:12;");
            new Thread(() -> {
                boolean ok = EmailService.envoyerEmail(
                    tfHost.getText().trim(), tfPort.getText().trim(),
                    tfUser.getText().trim(), pfPass.getText(),
                    tfTo.getText().trim(), tfSujet.getText().trim(), taCorps.getText());
                javafx.application.Platform.runLater(() -> {
                    if (ok) {
                        msgEnvoi.setText("✅  Email envoyé à " + tfTo.getText());
                        msgEnvoi.setStyle("-fx-text-fill:" + Design.SUCCESS + ";-fx-font-size:12;-fx-font-weight:bold;");
                        tfTo.clear(); tfSujet.clear(); taCorps.clear();
                    } else {
                        msgEnvoi.setText("❌  Échec — vérifiez la configuration SMTP.");
                        msgEnvoi.setStyle("-fx-text-fill:" + Design.DANGER + ";-fx-font-size:12;-fx-font-weight:bold;");
                    }
                });
            }).start();
        });

        boxEnvoi.getChildren().addAll(gEnvoi, btnEnvoyer, msgEnvoi);

        // ── Réception IMAP ───────────────────────────────────────────
        VBox boxImap = Design.section("📥  Réception d'emails (IMAP)");
        Label noteImap = Design.muted(
            "Serveur IMAP Gmail : imap.gmail.com — Port 993 (SSL).\n" +
            "Utilisez le même email et mot de passe d'application que ci-dessus."
        );

        javafx.scene.layout.GridPane gImap = new javafx.scene.layout.GridPane();
        gImap.setHgap(12); gImap.setVgap(10); gImap.setPadding(new Insets(10, 0, 0, 0));
        TextField tfImapHost = styledField("imap.gmail.com", 200);
        TextField tfImapPort = styledField("993", 80);
        gImap.add(fieldLabel("Serveur IMAP :"), 0, 0); gImap.add(tfImapHost, 1, 0);
        gImap.add(fieldLabel("Port :"),          0, 1); gImap.add(tfImapPort, 1, 1);

        Button btnLire  = Design.btnPrimary("📥  Lire les emails reçus", Design.SUCCESS);
        TextArea taEmails = new TextArea();
        taEmails.setEditable(false); taEmails.setPrefHeight(160); taEmails.setWrapText(true);
        taEmails.setPromptText("Les emails reçus s'afficheront ici...");
        taEmails.setStyle(Design.INPUT_STYLE);
        Label msgImap = new Label(""); msgImap.setStyle("-fx-font-size:12;"); msgImap.setWrapText(true);

        btnLire.setOnAction(e -> {
            if (tfUser.getText().isEmpty() || pfPass.getText().isEmpty()) {
                msgImap.setText("⚠️  Configurez d'abord le compte SMTP/IMAP ci-dessus.");
                msgImap.setStyle("-fx-text-fill:" + Design.WARNING + ";-fx-font-size:12;"); return;
            }
            msgImap.setText("🔄 Récupération des emails...");
            new Thread(() -> {
                String contenu = EmailService.lireEmails(
                    tfImapHost.getText().trim(), tfImapPort.getText().trim(),
                    tfUser.getText().trim(), pfPass.getText());
                javafx.application.Platform.runLater(() -> {
                    taEmails.setText(contenu);
                    msgImap.setText(contenu.isEmpty() ? "Aucun email trouvé." : "✅  Emails chargés.");
                    msgImap.setStyle("-fx-text-fill:" + Design.SUCCESS + ";-fx-font-size:12;-fx-font-weight:bold;");
                });
            }).start();
        });

        boxImap.getChildren().addAll(noteImap, gImap, btnLire, taEmails, msgImap);

        panel.getChildren().addAll(boxConfig, boxEnvoi, boxImap);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    private TextField styledField(String def, double w) {
        TextField tf = new TextField(def);
        tf.setPrefWidth(w);
        tf.setStyle(Design.INPUT_STYLE);
        return tf;
    }

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + Design.TEXT_DARK + "; -fx-min-width: 130;");
        return lbl;
    }
}
