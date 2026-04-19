package ui;

import dao.UtilisateurDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Utilisateur;

public class LoginPanel {

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private UnivSchedulerApp app;

    public LoginPanel(UnivSchedulerApp app) {
        this.app = app;
    }

    public StackPane createPanel() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a1f3c, #2d3561, #1e2547);");

        VBox center = new VBox(0);
        center.setAlignment(Pos.CENTER);
        center.setMaxWidth(440);

        // ── En-tête logo ─────────────────────────────────────────────
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 32, 0));

        Label logo = new Label("🎓");
        logo.setStyle("-fx-font-size: 52;");
        Label titre = new Label("UNIV-SCHEDULER");
        titre.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sousTitre = new Label("Gestion des Salles & Emplois du Temps");
        sousTitre.setStyle("-fx-font-size: 12; -fx-text-fill: rgba(255,255,255,0.6);");
        header.getChildren().addAll(logo, titre, sousTitre);

        // ── Carte formulaire ──────────────────────────────────────────
        VBox carte = new VBox(16);
        carte.setPadding(new Insets(36, 40, 36, 40));
        carte.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.30), 28, 0, 0, 8);"
        );

        Label titleCarte = new Label("Connexion");
        titleCarte.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        Label subtitle = new Label("Accédez à votre espace de gestion universitaire");
        subtitle.setStyle("-fx-font-size: 12; -fx-text-fill: #8395a7;");
        subtitle.setWrapText(true);

        // Login
        VBox loginBox = new VBox(5);
        Label lblLogin = new Label("Identifiant");
        lblLogin.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        TextField tfLogin = new TextField();
        tfLogin.setPromptText("Votre identifiant");
        tfLogin.setPrefHeight(42);
        tfLogin.setStyle("-fx-background-color: #f8f9fe; -fx-border-color: #d8e1f0; -fx-border-radius: 7; -fx-background-radius: 7; -fx-padding: 6 12; -fx-font-size: 13;");
        loginBox.getChildren().addAll(lblLogin, tfLogin);

        // Mot de passe
        VBox mdpBox = new VBox(5);
        Label lblMdp = new Label("Mot de passe");
        lblMdp.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1a1f3c;");
        PasswordField pfMdp = new PasswordField();
        pfMdp.setPromptText("Votre mot de passe");
        pfMdp.setPrefHeight(42);
        pfMdp.setStyle("-fx-background-color: #f8f9fe; -fx-border-color: #d8e1f0; -fx-border-radius: 7; -fx-background-radius: 7; -fx-padding: 6 12; -fx-font-size: 13;");
        mdpBox.getChildren().addAll(lblMdp, pfMdp);

        // Erreur
        Label labelErreur = new Label("");
        labelErreur.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-padding: 8 12; -fx-background-color: #fdecea; -fx-background-radius: 6;");
        labelErreur.setWrapText(true);
        labelErreur.setVisible(false);
        labelErreur.setManaged(false);

        // Bouton connexion dégradé
        Button btnConnexion = new Button("Se connecter  →");
        btnConnexion.setPrefWidth(Double.MAX_VALUE);
        btnConnexion.setPrefHeight(44);
        String btnStyle = "-fx-background-color: linear-gradient(to right, #4f6ef7, #667eea); -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
        String btnHover = "-fx-background-color: linear-gradient(to right, #3d5ce6, #5568d9); -fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
        btnConnexion.setStyle(btnStyle);
        btnConnexion.setOnMouseEntered(e -> btnConnexion.setStyle(btnHover));
        btnConnexion.setOnMouseExited(e  -> btnConnexion.setStyle(btnStyle));

        // ── Action ───────────────────────────────────────────────────
        Runnable actionConnexion = () -> {
            String login = tfLogin.getText().trim();
            String mdp   = pfMdp.getText().trim();
            if (login.isEmpty() || mdp.isEmpty()) {
                labelErreur.setText("⚠️  Veuillez remplir tous les champs.");
                labelErreur.setVisible(true); labelErreur.setManaged(true); return;
            }
            Utilisateur user = utilisateurDAO.connecter(login, mdp);
            if (user != null) {
                app.ouvrirInterface(user);
            } else {
                labelErreur.setText("❌  Identifiant ou mot de passe incorrect.");
                labelErreur.setVisible(true); labelErreur.setManaged(true);
                pfMdp.clear();
            }
        };

        btnConnexion.setOnAction(e -> actionConnexion.run());
        pfMdp.setOnAction(e -> actionConnexion.run());
        tfLogin.setOnAction(e -> pfMdp.requestFocus());

        carte.getChildren().addAll(titleCarte, subtitle, loginBox, mdpBox, labelErreur, btnConnexion);
        center.getChildren().addAll(header, carte);
        root.getChildren().add(center);
        StackPane.setAlignment(center, Pos.CENTER);
        return root;
    }
}
