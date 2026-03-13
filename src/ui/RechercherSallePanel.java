package ui;

import dao.SalleDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import models.Salle;

public class RechercherSallePanel {
    private SalleDAO salleDAO = new SalleDAO();
    private TableView<Salle> table;

    @SuppressWarnings("unchecked")
	public VBox createPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Label titre = new Label("Rechercher une Salle");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        // Formulaire de recherche
        GridPane gridRecherche = new GridPane();
        gridRecherche.setHgap(10);
        gridRecherche.setVgap(10);
        gridRecherche.setPadding(new Insets(10));
        gridRecherche.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        Label labelCapacite = new Label("Capacité minimale:");
        Spinner<Integer> spinCapacite = new Spinner<>(0, 500, 50);
        spinCapacite.setPrefWidth(100);

        Button btnRechercher = new Button("Rechercher");

        gridRecherche.add(labelCapacite, 0, 0);
        gridRecherche.add(spinCapacite, 1, 0);
        gridRecherche.add(btnRechercher, 2, 0);

        // Tableau des résultats
        table = new TableView<>();
        table.setPrefHeight(350);

        TableColumn<Salle, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getId()));
        colId.setPrefWidth(50);

        TableColumn<Salle, String> colNumero = new TableColumn<>("Numéro");
        colNumero.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNumero()));
        colNumero.setPrefWidth(100);

        TableColumn<Salle, Integer> colCapacite = new TableColumn<>("Capacité");
        colCapacite.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCapacite()));
        colCapacite.setPrefWidth(100);

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

        // Action bouton rechercher
        btnRechercher.setOnAction(e -> {
            int capaciteMin = spinCapacite.getValue();
            ObservableList<Salle> resultats = FXCollections.observableArrayList(
                salleDAO.rechercherParCapacite(capaciteMin)
            );
            table.setItems(resultats);

            if (resultats.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Résultat de recherche");
                alert.setHeaderText("Aucune salle trouvée");
                alert.setContentText("Il n'y a pas de salle avec une capacité de " + capaciteMin + " ou plus.");
                alert.showAndWait();
            }
        });

        panel.getChildren().addAll(titre, gridRecherche, new Label("Résultats:"), table);

        return panel;
    }
}
