package ui;

import dao.SalleDAO;
import models.Salle;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AjouterSallePanel {
    private SalleDAO salleDAO = new SalleDAO();

    public VBox createPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));

        Label titre = new Label("Ajouter une Salle");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        TextField tfNumero = new TextField();
        tfNumero.setPromptText("Ex: A101");
        tfNumero.setPrefWidth(200);

        TextField tfCapacite = new TextField();
        tfCapacite.setPromptText("Ex: 50");
        tfCapacite.setPrefWidth(200);

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("TD", "TP", "Amphi");
        cbType.setPrefWidth(200);

        TextField tfBatiment = new TextField();
        tfBatiment.setPromptText("Ex: Bâtiment A");
        tfBatiment.setPrefWidth(200);

        TextField tfEtage = new TextField();
        tfEtage.setPromptText("Ex: 1er étage");
        tfEtage.setPrefWidth(200);

        grid.add(new Label("Numéro:"), 0, 0);
        grid.add(tfNumero, 1, 0);
        grid.add(new Label("Capacité:"), 0, 1);
        grid.add(tfCapacite, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(cbType, 1, 2);
        grid.add(new Label("Bâtiment:"), 0, 3);
        grid.add(tfBatiment, 1, 3);
        grid.add(new Label("Étage:"), 0, 4);
        grid.add(tfEtage, 1, 4);

        Button btnAjouter = new Button("Ajouter");
        btnAjouter.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        btnAjouter.setOnAction(e -> {
            // Vérification champs vides
            if (tfNumero.getText().isEmpty() || tfCapacite.getText().isEmpty() ||
                cbType.getValue() == null || tfBatiment.getText().isEmpty() ||
                tfEtage.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants",
                        "Remplissez tous les champs", "Tous les champs sont obligatoires.");
                return;
            }

            // Vérification capacité numérique
            int capacite;
            try {
                capacite = Integer.parseInt(tfCapacite.getText().trim());
                if (capacite <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                        "Capacité invalide", "La capacité doit être un nombre entier positif.");
                return;
            }

            // Ajout en base
            try {
                Salle salle = new Salle();
                salle.setNumero(tfNumero.getText().trim());
                salle.setCapacite(capacite);
                salle.setType(cbType.getValue());
                salle.setBatiment(tfBatiment.getText().trim());
                salle.setEtage(tfEtage.getText().trim());

                salleDAO.ajouter(salle);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Salle ajoutée",
                        "La salle " + salle.getNumero() + " a été ajoutée avec succès!");

                // Réinitialiser le formulaire
                tfNumero.clear();
                tfCapacite.clear();
                cbType.setValue(null);
                tfBatiment.clear();
                tfEtage.clear();

            } catch (RuntimeException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur base de données",
                        "Impossible d'ajouter la salle",
                        ex.getMessage() != null ? ex.getMessage() : "Erreur inconnue. Vérifiez la connexion MySQL.");
            }
        });

        panel.getChildren().addAll(titre, grid, btnAjouter);
        return panel;
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
