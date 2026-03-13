package ui;

import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.CoursDAO;
import dao.SalleDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import models.Cours;
import models.Salle;

public class EmploiDuTempsPanel {
    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private TableView<Cours> table;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @SuppressWarnings("unchecked")
	public VBox createPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Label titre = new Label("Emploi du Temps");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        // Formulaire de filtre
        GridPane gridFiltre = new GridPane();
        gridFiltre.setHgap(10);
        gridFiltre.setVgap(10);
        gridFiltre.setPadding(new Insets(10));
        gridFiltre.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        Label labelClasse = new Label("Filtrer par classe:");
        TextField tfClasse = new TextField();
        tfClasse.setPromptText("Ex: L2-Informatique");
        tfClasse.setPrefWidth(200);

        Button btnFiltrer = new Button("Filtrer");
        Button btnAfficherTous = new Button("Afficher tous");

        gridFiltre.add(labelClasse, 0, 0);
        gridFiltre.add(tfClasse, 1, 0);
        gridFiltre.add(btnFiltrer, 2, 0);
        gridFiltre.add(btnAfficherTous, 3, 0);

        // Tableau
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

        TableColumn<Cours, String> colSalle = new TableColumn<>("Salle");
        colSalle.setCellValueFactory(cellData -> {
            Salle salle = salleDAO.obtenirParId(cellData.getValue().getSalleId());
            String salleNum = (salle != null) ? salle.getNumero() : "Inconnue";
            return new javafx.beans.property.SimpleStringProperty(salleNum);
        });
        colSalle.setPrefWidth(80);

        TableColumn<Cours, String> colDate = new TableColumn<>("Date/Heure");
        colDate.setCellValueFactory(cellData -> {
            String dateStr = cellData.getValue().getDateDebut().format(formatter);
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        colDate.setPrefWidth(150);

        table.getColumns().addAll(colId, colMatiere, colEnseignant, colClasse, colSalle, colDate);

        // Actions des boutons
        btnFiltrer.setOnAction(e -> {
            String classe = tfClasse.getText();
            if (classe.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champ manquant");
                alert.setHeaderText("Veuillez entrer une classe");
                alert.setContentText("Entrez le nom de la classe à filtrer.");
                alert.showAndWait();
            } else {
                List<Cours> resultats = coursDAO.obtenirParClasse(classe);
                ObservableList<Cours> data = FXCollections.observableArrayList(resultats);
                table.setItems(data);
            }
        });

        btnAfficherTous.setOnAction(e -> chargerTousCours());

        panel.getChildren().addAll(titre, gridFiltre, table);

        // Charger tous les cours au démarrage
        chargerTousCours();

        return panel;
    }

    private void chargerTousCours() {
        ObservableList<Cours> data = FXCollections.observableArrayList(coursDAO.obtenirTous());
        table.setItems(data);
    }
}
