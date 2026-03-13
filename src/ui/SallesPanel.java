package ui;

import dao.SalleDAO;
import models.Salle;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Optional;

public class SallesPanel {
    private SalleDAO salleDAO = new SalleDAO();
    private TableView<Salle> table;

    public VBox createPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Label titre = new Label("Liste des Salles");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<Salle, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        colId.setPrefWidth(50);

        TableColumn<Salle, String> colNumero = new TableColumn<>("Numéro");
        colNumero.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNumero()));
        colNumero.setPrefWidth(100);

        TableColumn<Salle, Integer> colCapacite = new TableColumn<>("Capacité");
        colCapacite.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCapacite()));
        colCapacite.setPrefWidth(80);

        TableColumn<Salle, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        colType.setPrefWidth(80);

        TableColumn<Salle, String> colBatiment = new TableColumn<>("Bâtiment");
        colBatiment.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBatiment()));
        colBatiment.setPrefWidth(100);

        TableColumn<Salle, String> colEtage = new TableColumn<>("Étage");
        colEtage.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEtage()));
        colEtage.setPrefWidth(80);

        table.getColumns().addAll(colId, colNumero, colCapacite, colType, colBatiment, colEtage);

        Button btnActualiser = new Button("Actualiser");
        btnActualiser.setOnAction(e -> actualiserTableau());

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setOnAction(e -> supprimerSalle());

        Button btnModifier = new Button("Modifier");
        btnModifier.setOnAction(e -> modifierSalle());

        HBox hboxBoutons = new HBox(10);
        hboxBoutons.getChildren().addAll(btnActualiser, btnSupprimer, btnModifier);

        panel.getChildren().addAll(titre, table, hboxBoutons);
        actualiserTableau();
        return panel;
    }

    private void actualiserTableau() {
        ObservableList<Salle> data = FXCollections.observableArrayList(salleDAO.obtenirTous());
        table.setItems(data);
    }

    private void supprimerSalle() {
        Salle selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune salle sélectionnée",
                    "Veuillez sélectionner une salle à supprimer.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la salle");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la salle " + selected.getNumero() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                salleDAO.supprimer(selected.getId());
                actualiserTableau();
            } catch (RuntimeException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression impossible", ex.getMessage());
            }
        }
    }

    private void modifierSalle() {
        Salle selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune salle sélectionnée",
                    "Veuillez sélectionner une salle à modifier.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier une salle");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField tfNumero = new TextField(selected.getNumero());
        TextField tfCapacite = new TextField(String.valueOf(selected.getCapacite()));
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("TD", "TP", "Amphi");
        cbType.setValue(selected.getType());
        TextField tfBatiment = new TextField(selected.getBatiment());
        TextField tfEtage = new TextField(selected.getEtage());

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

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Validation capacité
            int capacite;
            try {
                capacite = Integer.parseInt(tfCapacite.getText().trim());
                if (capacite <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Capacité invalide",
                        "La capacité doit être un nombre entier positif.");
                return;
            }
            try {
                selected.setNumero(tfNumero.getText().trim());
                selected.setCapacite(capacite);
                selected.setType(cbType.getValue());
                selected.setBatiment(tfBatiment.getText().trim());
                selected.setEtage(tfEtage.getText().trim());
                salleDAO.modifier(selected);
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
