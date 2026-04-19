package ui;

import java.util.List;

import dao.MessageDAO;
import dao.UtilisateurDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Message;
import models.Utilisateur;

/**
 * Panel du gestionnaire pour envoyer des messages internes — redesigné.
 *   👤 Message individuel : destinataire ciblé
 *   👥 Message groupé     : tous enseignants / tous étudiants / tous
 *
 * Logique métier inchangée.
 */
public class EnvoiMessagePanel {

    private MessageDAO     msgDAO  = new MessageDAO();
    private UtilisateurDAO userDAO = new UtilisateurDAO();
    private Utilisateur    utilisateur;

    public EnvoiMessagePanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public ScrollPane createPanel() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-font-size: 13; -fx-tab-min-height: 36;");

        Tab tabIndiv  = new Tab("👤  Message individuel", creerOngletIndividuel());
        Tab tabGroupe = new Tab("👥  Message groupé",     creerOngletGroupe());
        tabs.getTabs().addAll(tabIndiv, tabGroupe);

        VBox wrapper = new VBox(tabs);
        wrapper.setPadding(new Insets(16));
        wrapper.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════
    //  ONGLET 1 — MESSAGE INDIVIDUEL
    // ════════════════════════════════════════════════════════════════
    private VBox creerOngletIndividuel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("👤  Envoyer un message individuel");
        Label desc  = Design.muted("Sélectionnez un destinataire parmi les enseignants ou étudiants enregistrés.");
        panel.getChildren().addAll(titre, desc);

        // ── Sélection destinataire ────────────────────────────────────
        VBox selCard = new VBox(12);
        selCard.setPadding(new Insets(14, 16, 14, 16));
        selCard.setStyle(Design.CARD_STYLE);

        HBox selBox = new HBox(12);
        selBox.setAlignment(Pos.CENTER_LEFT);

        Label lblRole = fl("Rôle :");
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("Enseignants", "Étudiants");
        cbRole.setValue("Enseignants"); cbRole.setPrefWidth(160);

        Label lblDest = fl("Destinataire :");
        ComboBox<Utilisateur> cbDest = new ComboBox<>();
        cbDest.setPromptText("Sélectionner une personne…"); cbDest.setPrefWidth(280);
        cbDest.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Utilisateur u, boolean e) {
                super.updateItem(u, e);
                setText(e || u == null ? null : u.getNomComplet() + " (" + u.getLogin() + ")");
            }
        });
        cbDest.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Utilisateur u, boolean e) {
                super.updateItem(u, e);
                setText(e || u == null ? "Sélectionner une personne…" : u.getNomComplet());
            }
        });

        Runnable chargerUsers = () -> {
            String role = cbRole.getValue().equals("Enseignants") ? "ENSEIGNANT" : "ETUDIANT";
            cbDest.setItems(FXCollections.observableArrayList(userDAO.obtenirParRole(role)));
        };
        chargerUsers.run();
        cbRole.setOnAction(e -> chargerUsers.run());

        selBox.getChildren().addAll(lblRole, cbRole, lblDest, cbDest);
        selCard.getChildren().add(selBox);
        panel.getChildren().add(selCard);

        // ── Formulaire message ────────────────────────────────────────
        VBox formBox = Design.section("✉️  Composer le message");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 4, 0));

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll(
            "✅  Validation de réservation",
            "❌  Refus de réservation",
            "⚠️  Alerte conflit horaire",
            "📋  Information emploi du temps",
            "🔧  Réponse signalement",
            "💬  Message général"
        );
        cbType.setValue("💬  Message général"); cbType.setPrefWidth(310);

        TextField tfSujet = sf("Objet du message…", 420);
        TextArea  taCorps = new TextArea();
        taCorps.setPromptText("Corps du message…");
        taCorps.setPrefHeight(140); taCorps.setWrapText(true);
        taCorps.setStyle(Design.INPUT_STYLE);

        grid.add(fl("Type :"),    0, 0); grid.add(cbType,  1, 0);
        grid.add(fl("Sujet :"),   0, 1); grid.add(tfSujet, 1, 1);
        grid.add(fl("Message :"), 0, 2); grid.add(taCorps, 1, 2);

        Label msgEnvoi = new Label(""); msgEnvoi.setWrapText(true);

        Button btnEnvoyer = Design.btnPrimary("📤  Envoyer le message", Design.GEST_ACCENT);
        btnEnvoyer.setOnAction(e -> {
            if (cbDest.getValue() == null) {
                setMsg(msgEnvoi, "⚠️  Sélectionnez un destinataire.", Design.WARNING); return;
            }
            if (tfSujet.getText().isEmpty() || taCorps.getText().isEmpty()) {
                setMsg(msgEnvoi, "⚠️  Sujet et message sont obligatoires.", Design.WARNING); return;
            }
            Utilisateur dest = cbDest.getValue();
            String sujet = "[" + cbType.getValue().replaceAll("[^a-zA-ZÀ-ÿ /]", "").trim() + "] " + tfSujet.getText().trim();
            Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, taCorps.getText().trim(), "GENERAL", false, null, dest.getRole());
            msg.setDestinataireId(dest.getId());
            try {
                msgDAO.envoyer(msg);
                setMsg(msgEnvoi, "✅  Message envoyé à " + dest.getNomComplet() + ".", Design.SUCCESS);
                tfSujet.clear(); taCorps.clear(); cbDest.setValue(null); cbType.setValue("💬  Message général");
            } catch (Exception ex) {
                setMsg(msgEnvoi, "❌  " + ex.getMessage(), Design.DANGER);
            }
        });

        formBox.getChildren().addAll(grid, btnEnvoyer, msgEnvoi);
        panel.getChildren().add(formBox);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════
    //  ONGLET 2 — MESSAGE GROUPÉ
    // ════════════════════════════════════════════════════════════════
    private VBox creerOngletGroupe() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("👥  Envoyer un message groupé");
        Label desc  = Design.muted("Le message sera envoyé à tous les membres du groupe sélectionné.");
        panel.getChildren().addAll(titre, desc);

        // ── Sélection groupe ──────────────────────────────────────────
        VBox selCard = new VBox(12);
        selCard.setPadding(new Insets(14, 16, 14, 16));
        selCard.setStyle(Design.CARD_STYLE);

        HBox selBox = new HBox(12);
        selBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cbGroupe = new ComboBox<>();
        cbGroupe.getItems().addAll("Tous les enseignants", "Tous les étudiants", "Tous les utilisateurs");
        cbGroupe.setValue("Tous les enseignants"); cbGroupe.setPrefWidth(280);

        Label lblNb = new Label("");
        lblNb.setStyle(
            "-fx-font-size:12;-fx-font-weight:bold;" +
            "-fx-text-fill:" + Design.GEST_ACCENT + ";" +
            "-fx-padding: 4 10; -fx-background-color: #e8faf5; -fx-background-radius: 6;"
        );

        cbGroupe.setOnAction(e -> {
            int nb = getDestinataires(cbGroupe.getValue()).size();
            lblNb.setText("→  " + nb + " destinataire(s)");
        });
        cbGroupe.fireEvent(new javafx.event.ActionEvent());

        selBox.getChildren().addAll(fl("Groupe :"), cbGroupe, lblNb);
        selCard.getChildren().add(selBox);
        panel.getChildren().add(selCard);

        // ── Formulaire message ────────────────────────────────────────
        VBox formBox = Design.section("✉️  Composer le message groupé");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 4, 0));

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("⚠️  Alerte", "📋  Information", "💬  Message général", "🔔  Notification");
        cbType.setValue("📋  Information"); cbType.setPrefWidth(240);

        TextField tfSujet = sf("Objet du message groupé…", 420);
        TextArea  taCorps = new TextArea();
        taCorps.setPromptText("Corps du message…");
        taCorps.setPrefHeight(130); taCorps.setWrapText(true);
        taCorps.setStyle(Design.INPUT_STYLE);

        grid.add(fl("Type :"),    0, 0); grid.add(cbType,  1, 0);
        grid.add(fl("Sujet :"),   0, 1); grid.add(tfSujet, 1, 1);
        grid.add(fl("Message :"), 0, 2); grid.add(taCorps, 1, 2);

        Label msgEnvoi = new Label(""); msgEnvoi.setWrapText(true);

        Button btnEnvoyer = Design.btnPrimary("📤  Envoyer à tout le groupe", Design.GEST_ACCENT);
        btnEnvoyer.setOnAction(e -> {
            if (tfSujet.getText().isEmpty() || taCorps.getText().isEmpty()) {
                setMsg(msgEnvoi, "⚠️  Sujet et message sont obligatoires.", Design.WARNING); return;
            }
            List<Utilisateur> dests = getDestinataires(cbGroupe.getValue());
            if (dests.isEmpty()) { setMsg(msgEnvoi, "⚠️  Aucun destinataire trouvé.", Design.WARNING); return; }
            String sujet = "[Groupe] " + tfSujet.getText().trim();
            int ok = 0;
            for (Utilisateur dest : dests) {
                Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                    utilisateur.getRole(), sujet, taCorps.getText().trim(), "GENERAL", false, null, dest.getRole());
                msg.setDestinataireId(dest.getId());
                try { msgDAO.envoyer(msg); ok++; }
                catch (Exception ex) { System.err.println("Erreur envoi à " + dest.getNomComplet() + ": " + ex.getMessage()); }
            }
            setMsg(msgEnvoi, "✅  Message envoyé à " + ok + "/" + dests.size() + " destinataire(s).", Design.SUCCESS);
            tfSujet.clear(); taCorps.clear();
        });

        formBox.getChildren().addAll(grid, btnEnvoyer, msgEnvoi);
        panel.getChildren().add(formBox);
        return panel;
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private List<Utilisateur> getDestinataires(String groupe) {
        if (groupe.contains("enseignant")) {
			return userDAO.obtenirParRole("ENSEIGNANT");
		}
        if (groupe.contains("étudiant")) {
			return userDAO.obtenirParRole("ETUDIANT");
		}
        List<Utilisateur> tous = new java.util.ArrayList<>();
        tous.addAll(userDAO.obtenirParRole("ENSEIGNANT"));
        tous.addAll(userDAO.obtenirParRole("ETUDIANT"));
        return tous;
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
        lbl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";-fx-min-width:100;");
        return lbl;
    }
}
