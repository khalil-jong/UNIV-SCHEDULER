package ui;

import java.time.LocalDateTime;
import java.util.List;

import dao.CoursDAO;
import dao.SalleDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Cours;
import models.Salle;

public class AjouterCoursPanel {
    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();

    public VBox createPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));

        Label titre = new Label("Ajouter un Cours");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        TextField tfMatiere = new TextField();
        tfMatiere.setPromptText("Ex: Mathématiques");
        tfMatiere.setPrefWidth(200);

        TextField tfEnseignant = new TextField();
        tfEnseignant.setPromptText("Ex: Dr. Martin");
        tfEnseignant.setPrefWidth(200);

        TextField tfClasse = new TextField();
        tfClasse.setPromptText("Ex: L2-Informatique");
        tfClasse.setPrefWidth(200);

        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(200);

        Spinner<Integer> spinHeure = new Spinner<>(0, 23, 8);
        spinHeure.setPrefWidth(100);

        Spinner<Integer> spinMinute = new Spinner<>(0, 59, 0);
        spinMinute.setPrefWidth(100);

        Spinner<Integer> spinDuree = new Spinner<>(15, 480, 60);
        spinDuree.setPrefWidth(100);

        // Charger les salles
        List<Salle> salles = salleDAO.obtenirTous();
        ComboBox<Salle> cbSalle = new ComboBox<>(FXCollections.observableArrayList(salles));
        cbSalle.setPrefWidth(200);
        if (salles.isEmpty()) {
            cbSalle.setPromptText("Aucune salle disponible");
        }

        grid.add(new Label("Matière:"), 0, 0);
        grid.add(tfMatiere, 1, 0);
        grid.add(new Label("Enseignant:"), 0, 1);
        grid.add(tfEnseignant, 1, 1);
        grid.add(new Label("Classe:"), 0, 2);
        grid.add(tfClasse, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(datePicker, 1, 3);

        HBox timeBox = new HBox(5);
        timeBox.getChildren().addAll(
            new Label("H:"), spinHeure,
            new Label("Min:"), spinMinute
        );
        grid.add(new Label("Heure de début:"), 0, 4);
        grid.add(timeBox, 1, 4);

        grid.add(new Label("Durée (minutes):"), 0, 5);
        grid.add(spinDuree, 1, 5);
        grid.add(new Label("Salle:"), 0, 6);
        grid.add(cbSalle, 1, 6);

        Button btnAjouter = new Button("Ajouter");
        btnAjouter.setStyle("-fx-font-size: 12; -fx-padding: 10;");
        btnAjouter.setOnAction(e -> {
            // Vérification champs vides
            if (tfMatiere.getText().isEmpty() || tfEnseignant.getText().isEmpty() ||
                tfClasse.getText().isEmpty() || datePicker.getValue() == null ||
                cbSalle.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants",
                        "Remplissez tous les champs", "Tous les champs sont obligatoires.");
                return;
            }

            try {
                LocalDateTime dateTime = LocalDateTime.of(
                    datePicker.getValue(),
                    java.time.LocalTime.of(spinHeure.getValue(), spinMinute.getValue())
                );

                Cours cours = new Cours();
                cours.setMatiere(tfMatiere.getText().trim());
                cours.setEnseignant(tfEnseignant.getText().trim());
                cours.setClasse(tfClasse.getText().trim());
                cours.setDateDebut(dateTime);
                cours.setDuree(spinDuree.getValue());
                cours.setSalleId(cbSalle.getValue().getId());

                coursDAO.ajouter(cours);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Cours ajouté",
                        "Le cours a été ajouté avec succès!");

                // Réinitialiser le formulaire
                tfMatiere.clear();
                tfEnseignant.clear();
                tfClasse.clear();
                datePicker.setValue(null);
                spinHeure.getValueFactory().setValue(8);
                spinMinute.getValueFactory().setValue(0);
                spinDuree.getValueFactory().setValue(60);
                cbSalle.setValue(null);

            } catch (RuntimeException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur base de données",
                        "Impossible d'ajouter le cours",
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
