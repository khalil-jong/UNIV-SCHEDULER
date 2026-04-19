package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.Utilisateur;

public class UnivSchedulerApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("UNIV-SCHEDULER — Gestion des Salles et Emplois du Temps");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(760);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        afficherLogin();
        primaryStage.show();
    }

    public void afficherLogin() {
        LoginPanel loginPanel = new LoginPanel(this);
        Scene scene = new Scene(loginPanel.createPanel());
        primaryStage.setScene(scene);
    }

    public void ouvrirInterface(Utilisateur utilisateur) {
        javafx.scene.Parent root;
        switch (utilisateur.getRole()) {
            case "ADMIN":        root = new AdminPanel(utilisateur, this).createPanel();       break;
            case "GESTIONNAIRE": root = new GestionnairePanel(utilisateur, this).createPanel(); break;
            case "ENSEIGNANT":   root = new EnseignantPanel(utilisateur, this).createPanel();  break;
            case "ETUDIANT":     root = new EtudiantPanel(utilisateur, this).createPanel();    break;
            default: afficherLogin(); return;
        }
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
