package ui;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import dao.CoursDAO;
import dao.SalleDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Cours;

public class CoursPanel {
    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private TableView<Cours> table;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public VBox createPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Label titre = new Label("Liste des Cours");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<Cours, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        colId.setPrefWidth(50);

        TableColumn<Cours, String> colMatiere = new TableColumn<>("Matière");
        colMatiere.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMatiere()));
        colMatiere.setPrefWidth(120);

        TableColumn<Cours, String> colEnseignant = new TableColumn<>("Enseignant");
        colEnseignant.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEnseignant()));
        colEnseignant.setPrefWidth(120);

        TableColumn<Cours, String> colClasse = new TableColumn<>("Classe");
        colClasse.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getClasse()));
        colClasse.setPrefWidth(100);

        TableColumn<Cours, String> colDate = new TableColumn<>("Date/Heure");
        colDate.setCellValueFactory(cellData -> {
            String dateStr = cellData.getValue().getDateDebut().format(formatter);
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        colDate.setPrefWidth(150);

        TableColumn<Cours, Integer> colDuree = new TableColumn<>("Durée (min)");
        colDuree.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDuree()));
        colDuree.setPrefWidth(90);

        table.getColumns().addAll(colId, colMatiere, colEnseignant, colClasse, colDate, colDuree);

        Button btnActualiser = new Button("Actualiser");
        btnActualiser.setOnAction(e -> actualiserTableau());

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setOnAction(e -> supprimerCours());

        Button btnModifier = new Button("Modifier");
        btnModifier.setOnAction(e -> modifierCours());

        HBox hboxBoutons = new HBox(10);
        hboxBoutons.getChildren().addAll(btnActualiser, btnSupprimer, btnModifier);

        panel.getChildren().addAll(titre, table, hboxBoutons);
        actualiserTableau();
        return panel;
    }

    private void actualiserTableau() {
        ObservableList<Cours> data = FXCollections.observableArrayList(coursDAO.obtenirTous());
        table.setItems(data);
    }

    private void supprimerCours() {
        Cours selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun cours sélectionné",
                    "Veuillez sélectionner un cours à supprimer.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le cours");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce cours ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                coursDAO.supprimer(selected.getId());
                actualiserTableau();
            } catch (RuntimeException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression impossible", ex.getMessage());
            }
        }
    }

    private void modifierCours() {
        Cours selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun cours sélectionné",
                    "Veuillez sélectionner un cours à modifier.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier un cours");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField tfMatiere = new TextField(selected.getMatiere());
        TextField tfEnseignant = new TextField(selected.getEnseignant());
        TextField tfClasse = new TextField(selected.getClasse());
        TextField tfDuree = new TextField(String.valueOf(selected.getDuree()));

        grid.add(new Label("Matière:"), 0, 0);
        grid.add(tfMatiere, 1, 0);
        grid.add(new Label("Enseignant:"), 0, 1);
        grid.add(tfEnseignant, 1, 1);
        grid.add(new Label("Classe:"), 0, 2);
        grid.add(tfClasse, 1, 2);
        grid.add(new Label("Durée (min):"), 0, 3);
        grid.add(tfDuree, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Validation durée
            int duree;
            try {
                duree = Integer.parseInt(tfDuree.getText().trim());
                if (duree <= 0) {
					throw new NumberFormatException();
				}
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Durée invalide",
                        "La durée doit être un nombre entier positif.");
                return;
            }
            try {
                selected.setMatiere(tfMatiere.getText().trim());
                selected.setEnseignant(tfEnseignant.getText().trim());
                selected.setClasse(tfClasse.getText().trim());
                selected.setDuree(duree);
                coursDAO.modifier(selected);
                actualiserTableau();
            } catch (RuntimeException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Modification impossible", ex.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
