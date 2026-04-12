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
 * Panel du gestionnaire pour envoyer des messages internes
 * aux enseignants et étudiants (réponse réservation, alerte conflit, info générale…).
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
        tabs.setStyle("-fx-font-size: 13;");

        Tab tabIndiv  = new Tab("👤 Message individuel",   creerOngletIndividuel());
        Tab tabGroupe = new Tab("👥 Message groupé",       creerOngletGroupe());

        tabs.getTabs().addAll(tabIndiv, tabGroupe);
        VBox wrapper = new VBox(tabs); wrapper.setPadding(new Insets(14));
        ScrollPane scroll = new ScrollPane(wrapper); scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  ONGLET 1 — MESSAGE INDIVIDUEL
    // ════════════════════════════════════════════════════════════
    private VBox creerOngletIndividuel() {
        VBox panel = new VBox(16); panel.setPadding(new Insets(18));

        Label titre = new Label("👤 Envoyer un message à un utilisateur");
        titre.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label desc = new Label("Sélectionnez un destinataire parmi les enseignants ou étudiants enregistrés.");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

        // Sélection du rôle puis de la personne
        HBox selBox = new HBox(12); selBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("Enseignants", "Étudiants");
        cbRole.setValue("Enseignants"); cbRole.setPrefWidth(150);

        ComboBox<Utilisateur> cbDest = new ComboBox<>();
        cbDest.setPromptText("Sélectionner une personne"); cbDest.setPrefWidth(260);
        cbDest.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Utilisateur u, boolean e) {
                super.updateItem(u, e); setText(e || u == null ? null : u.getNomComplet() + " (" + u.getLogin() + ")");
            }
        });
        cbDest.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Utilisateur u, boolean e) {
                super.updateItem(u, e); setText(e || u == null ? "Sélectionner une personne" : u.getNomComplet());
            }
        });

        // Charger les utilisateurs selon le rôle choisi
        Runnable chargerUsers = () -> {
            String role = cbRole.getValue().equals("Enseignants") ? "ENSEIGNANT" : "ETUDIANT";
            cbDest.setItems(FXCollections.observableArrayList(userDAO.obtenirParRole(role)));
        };
        chargerUsers.run();
        cbRole.setOnAction(e -> chargerUsers.run());
        selBox.getChildren().addAll(new Label("Rôle :"), cbRole, new Label("Destinataire :"), cbDest);

        // Formulaire message
        VBox formBox = new VBox(10); formBox.setPadding(new Insets(14));
        formBox.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll(
            "✅ Validation de réservation",
            "❌ Refus de réservation",
            "⚠️ Alerte conflit horaire",
            "📋 Information emploi du temps",
            "🔧 Réponse signalement",
            "💬 Message général"
        );
        cbType.setValue("💬 Message général"); cbType.setPrefWidth(280);

        TextField tfSujet = new TextField(); tfSujet.setPromptText("Sujet du message"); tfSujet.setPrefWidth(400);
        TextArea taCorps  = new TextArea();  taCorps.setPromptText("Corps du message..."); taCorps.setPrefHeight(130); taCorps.setWrapText(true);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Type :"),    0, 0); grid.add(cbType,  1, 0);
        grid.add(new Label("Sujet :"),   0, 1); grid.add(tfSujet, 1, 1);
        grid.add(new Label("Message :"), 0, 2); grid.add(taCorps, 1, 2);

        Label msgEnvoi = new Label(""); msgEnvoi.setStyle("-fx-font-size:12;"); msgEnvoi.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer le message");
        btnEnvoyer.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-padding:9 22;-fx-font-weight:bold;-fx-font-size:13;");
        btnEnvoyer.setOnAction(e -> {
            if (cbDest.getValue() == null) { msgEnvoi.setText("⚠️ Sélectionnez un destinataire."); msgEnvoi.setStyle("-fx-text-fill:#e67e22;"); return; }
            if (tfSujet.getText().isEmpty() || taCorps.getText().isEmpty()) { msgEnvoi.setText("⚠️ Sujet et message sont obligatoires."); msgEnvoi.setStyle("-fx-text-fill:#e67e22;"); return; }

            Utilisateur dest = cbDest.getValue();
            String sujet = "[" + cbType.getValue().replaceAll("[^a-zA-ZÀ-ÿ /]","").trim() + "] " + tfSujet.getText().trim();
            Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, taCorps.getText().trim(), "GENERAL", false, null,
                dest.getRole());
            msg.setDestinataireId(dest.getId());
            try {
                msgDAO.envoyer(msg);
                msgEnvoi.setText("✅ Message envoyé à " + dest.getNomComplet() + ".");
                msgEnvoi.setStyle("-fx-text-fill:#27ae60;");
                tfSujet.clear(); taCorps.clear(); cbDest.setValue(null); cbType.setValue("💬 Message général");
            } catch (Exception ex) { msgEnvoi.setText("❌ " + ex.getMessage()); msgEnvoi.setStyle("-fx-text-fill:#e74c3c;"); }
        });

        formBox.getChildren().addAll(grid, btnEnvoyer, msgEnvoi);
        panel.getChildren().addAll(titre, desc, selBox, formBox);
        return panel;
    }

    // ════════════════════════════════════════════════════════════
    //  ONGLET 2 — MESSAGE GROUPÉ
    // ════════════════════════════════════════════════════════════
    private VBox creerOngletGroupe() {
        VBox panel = new VBox(16); panel.setPadding(new Insets(18));

        Label titre = new Label("👥 Envoyer un message groupé");
        titre.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label desc = new Label("Le message sera envoyé à tous les membres du groupe sélectionné.");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

        // Sélection du groupe cible
        HBox selBox = new HBox(12); selBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> cbGroupe = new ComboBox<>();
        cbGroupe.getItems().addAll("Tous les enseignants", "Tous les étudiants", "Tous les utilisateurs");
        cbGroupe.setValue("Tous les enseignants"); cbGroupe.setPrefWidth(240);

        Label lblNb = new Label("");
        lblNb.setStyle("-fx-font-size:12;-fx-text-fill:#7f8c8d;");

        cbGroupe.setOnAction(e -> {
            int nb = getDestinataires(cbGroupe.getValue()).size();
            lblNb.setText(nb + " destinataire(s)");
        });
        cbGroupe.fireEvent(new javafx.event.ActionEvent());
        selBox.getChildren().addAll(new Label("Groupe :"), cbGroupe, lblNb);

        // Formulaire
        VBox formBox = new VBox(10); formBox.setPadding(new Insets(14));
        formBox.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("⚠️ Alerte", "📋 Information", "💬 Message général", "🔔 Notification");
        cbType.setValue("📋 Information"); cbType.setPrefWidth(220);

        TextField tfSujet = new TextField(); tfSujet.setPromptText("Sujet"); tfSujet.setPrefWidth(400);
        TextArea  taCorps = new TextArea();  taCorps.setPromptText("Corps du message..."); taCorps.setPrefHeight(120); taCorps.setWrapText(true);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Type :"),    0, 0); grid.add(cbType,  1, 0);
        grid.add(new Label("Sujet :"),   0, 1); grid.add(tfSujet, 1, 1);
        grid.add(new Label("Message :"), 0, 2); grid.add(taCorps, 1, 2);

        Label msgEnvoi = new Label(""); msgEnvoi.setStyle("-fx-font-size:12;"); msgEnvoi.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer à tout le groupe");
        btnEnvoyer.setStyle("-fx-background-color:#2980b9;-fx-text-fill:white;-fx-padding:9 22;-fx-font-weight:bold;-fx-font-size:13;");
        btnEnvoyer.setOnAction(e -> {
            if (tfSujet.getText().isEmpty() || taCorps.getText().isEmpty()) { msgEnvoi.setText("⚠️ Sujet et message obligatoires."); msgEnvoi.setStyle("-fx-text-fill:#e67e22;"); return; }
            List<Utilisateur> dests = getDestinataires(cbGroupe.getValue());
            if (dests.isEmpty()) { msgEnvoi.setText("⚠️ Aucun destinataire trouvé."); return; }
            String sujet = "[Groupe] " + tfSujet.getText().trim();
            int ok = 0;
            for (Utilisateur dest : dests) {
                Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                    utilisateur.getRole(), sujet, taCorps.getText().trim(), "GENERAL", false, null, dest.getRole());
                msg.setDestinataireId(dest.getId());
                try { msgDAO.envoyer(msg); ok++; } catch (Exception ex) { System.err.println("Erreur envoi à " + dest.getNomComplet() + ": " + ex.getMessage()); }
            }
            msgEnvoi.setText("✅ Message envoyé à " + ok + "/" + dests.size() + " destinataire(s).");
            msgEnvoi.setStyle("-fx-text-fill:#27ae60;");
            tfSujet.clear(); taCorps.clear();
        });

        formBox.getChildren().addAll(grid, btnEnvoyer, msgEnvoi);
        panel.getChildren().addAll(titre, desc, selBox, formBox);
        return panel;
    }

    private List<Utilisateur> getDestinataires(String groupe) {
        if (groupe.contains("enseignant")) {
			return userDAO.obtenirParRole("ENSEIGNANT");
		}
        if (groupe.contains("étudiant")) {
			return userDAO.obtenirParRole("ETUDIANT");
		}
        // Tous
        List<Utilisateur> tous = new java.util.ArrayList<>();
        tous.addAll(userDAO.obtenirParRole("ENSEIGNANT"));
        tous.addAll(userDAO.obtenirParRole("ETUDIANT"));
        return tous;
    }
}
