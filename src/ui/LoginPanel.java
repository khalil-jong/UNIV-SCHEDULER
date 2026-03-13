package ui;

import dao.UtilisateurDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import models.Utilisateur;

public class LoginPanel {

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private UnivSchedulerApp app;

    public LoginPanel(UnivSchedulerApp app) {
        this.app = app;
    }

    public VBox createPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(60));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: #f0f4f8;");

        // Titre
        Label titre = new Label("UNIV-SCHEDULER");
        titre.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label sousTitre = new Label("Gestion des Salles et Emplois du Temps");
        sousTitre.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");

        // Carte de connexion
        VBox carte = new VBox(15);
        carte.setPadding(new Insets(30));
        carte.setMaxWidth(400);
        carte.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, #ccc, 10, 0, 0, 3);");

        Label labelConnexion = new Label("Connexion");
        labelConnexion.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Champ login
        Label labelLogin = new Label("Identifiant :");
        TextField tfLogin = new TextField();
        tfLogin.setPromptText("Entrez votre identifiant");
        tfLogin.setPrefHeight(38);

        // Champ mot de passe
        Label labelMdp = new Label("Mot de passe :");
        PasswordField pfMdp = new PasswordField();
        pfMdp.setPromptText("Entrez votre mot de passe");
        pfMdp.setPrefHeight(38);

        // Message d'erreur
        Label labelErreur = new Label("");
        labelErreur.setStyle("-fx-text-fill: red; -fx-font-size: 12;");

        // Bouton connexion
        Button btnConnexion = new Button("Se connecter");
        btnConnexion.setPrefWidth(340);
        btnConnexion.setPrefHeight(40);
        btnConnexion.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 5;");

        // Action connexion
        Runnable actionConnexion = () -> {
            String login = tfLogin.getText().trim();
            String mdp = pfMdp.getText().trim();

            if (login.isEmpty() || mdp.isEmpty()) {
                labelErreur.setText("Veuillez remplir tous les champs.");
                return;
            }

            Utilisateur user = utilisateurDAO.connecter(login, mdp);
            if (user != null) {
                app.ouvrirInterface(user);
            } else {
                labelErreur.setText("Identifiant ou mot de passe incorrect.");
                pfMdp.clear();
            }
        };

        btnConnexion.setOnAction(e -> actionConnexion.run());
        pfMdp.setOnAction(e -> actionConnexion.run());

        // Note comptes par défaut
        Label note = new Label("Comptes par défaut: admin/admin123");
        note.setStyle("-fx-font-size: 11; -fx-text-fill: #95a5a6;");

        carte.getChildren().addAll(labelConnexion, labelLogin, tfLogin, labelMdp, pfMdp, labelErreur, btnConnexion, note);
        panel.getChildren().addAll(titre, sousTitre, carte);

        return panel;
    }
}
