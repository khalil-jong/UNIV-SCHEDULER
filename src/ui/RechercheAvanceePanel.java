package ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import dao.CoursDAO;
import dao.SalleDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Salle;

// Recherche avancée de salles disponibles avec filtres
public class RechercheAvanceePanel {

    private SalleDAO salleDAO = new SalleDAO();
    private CoursDAO coursDAO = new CoursDAO();

    public ScrollPane createPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        Label titre = new Label("🔍 Recherche de Salles Disponibles");
        titre.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // === FILTRES ===
        VBox filtresBox = new VBox(12);
        filtresBox.setPadding(new Insets(15));
        filtresBox.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 6; -fx-background-color: white; -fx-background-radius: 6;");

        Label lblFiltres = new Label("Critères de recherche");
        lblFiltres.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(12);

        // Date et heure
        DatePicker datePicker = new DatePicker(LocalDate.now());
        Spinner<Integer> spinHeure = new Spinner<>(7, 22, LocalTime.now().getHour());
        spinHeure.setPrefWidth(90);
        Spinner<Integer> spinMinute = new Spinner<>(0, 59, 0);
        spinMinute.setPrefWidth(90);
        Spinner<Integer> spinDuree = new Spinner<>(30, 480, 60);
        spinDuree.setPrefWidth(100);

        HBox heureBox = new HBox(8, new Label("H:"), spinHeure, new Label("Min:"), spinMinute);
        heureBox.setAlignment(Pos.CENTER_LEFT);

        // Capacité
        Spinner<Integer> spinCap = new Spinner<>(0, 500, 0);
        spinCap.setPrefWidth(100);

        // Type
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Tous", "TD", "TP", "Amphi");
        cbType.setValue("Tous");
        cbType.setPrefWidth(120);

        // Équipements
        CheckBox chkVideo = new CheckBox("📽 Vidéoprojecteur");
        CheckBox chkTI = new CheckBox("🖥 Tableau interactif");
        CheckBox chkClim = new CheckBox("❄ Climatisation");
        HBox equipBox = new HBox(15, chkVideo, chkTI, chkClim);

        grid.add(new Label("📅 Date :"), 0, 0); grid.add(datePicker, 1, 0);
        grid.add(new Label("🕐 Heure de début :"), 0, 1); grid.add(heureBox, 1, 1);
        grid.add(new Label("⏱ Durée (minutes) :"), 0, 2); grid.add(spinDuree, 1, 2);
        grid.add(new Label("👥 Capacité minimale :"), 0, 3); grid.add(spinCap, 1, 3);
        grid.add(new Label("🚪 Type de salle :"), 0, 4); grid.add(cbType, 1, 4);
        grid.add(new Label("🔧 Équipements requis :"), 0, 5); grid.add(equipBox, 1, 5);

        Button btnMaintenant = new Button("⚡ Disponible maintenant");
        btnMaintenant.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 18; -fx-font-weight: bold;");

        Button btnRechercher = new Button("🔍 Rechercher");
        btnRechercher.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 18; -fx-font-weight: bold;");

        Button btnReset = new Button("🔄 Réinitialiser");
        btnReset.setStyle("-fx-padding: 10 18;");

        HBox boutons = new HBox(10, btnMaintenant, btnRechercher, btnReset);

        filtresBox.getChildren().addAll(lblFiltres, grid, boutons);

        // === RÉSULTATS ===
        Label lblResultats = new Label("Résultats :");
        lblResultats.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TableView<Salle> table = new TableView<>();
        table.setPrefHeight(320);
        table.setPlaceholder(new Label("Lancez une recherche pour voir les salles disponibles."));

        TableColumn<Salle, String> colNum = new TableColumn<>("Numéro");
        colNum.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNumero()));
        colNum.setPrefWidth(90);

        TableColumn<Salle, String> colBat = new TableColumn<>("Bâtiment");
        colBat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBatiment()));
        colBat.setPrefWidth(120);

        TableColumn<Salle, String> colEtage = new TableColumn<>("Étage");
        colEtage.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEtage()));
        colEtage.setPrefWidth(90);

        TableColumn<Salle, Integer> colCap = new TableColumn<>("Capacité");
        colCap.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCapacite()));
        colCap.setPrefWidth(80);

        TableColumn<Salle, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getType()));
        colType.setPrefWidth(70);

        TableColumn<Salle, String> colEquip = new TableColumn<>("Équipements");
        colEquip.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEquipementsStr()));
        colEquip.setPrefWidth(140);

        table.getColumns().addAll(colNum, colBat, colEtage, colCap, colType, colEquip);

        Label lblInfo = new Label("");
        lblInfo.setStyle("-fx-font-size: 12; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        // Actions
        btnMaintenant.setOnAction(e -> {
            datePicker.setValue(LocalDate.now());
            spinHeure.getValueFactory().setValue(LocalTime.now().getHour());
            spinMinute.getValueFactory().setValue(0);
            List<Salle> dispo = salleDAO.obtenirSallesDisponibles(LocalDateTime.now(), spinDuree.getValue());
            dispo = filtrerParCriteres(dispo, spinCap.getValue(), cbType.getValue(), chkVideo.isSelected(), chkTI.isSelected(), chkClim.isSelected());
            table.setItems(FXCollections.observableArrayList(dispo));
            lblInfo.setText(dispo.size() + " salle(s) disponible(s) en ce moment.");
        });

        btnRechercher.setOnAction(e -> {
            if (datePicker.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Sélectionnez une date.", ButtonType.OK).showAndWait();
                return;
            }
            LocalDateTime debut = LocalDateTime.of(datePicker.getValue(),
                LocalTime.of(spinHeure.getValue(), spinMinute.getValue()));
            List<Salle> dispo = salleDAO.obtenirSallesDisponibles(debut, spinDuree.getValue());
            dispo = filtrerParCriteres(dispo, spinCap.getValue(), cbType.getValue(), chkVideo.isSelected(), chkTI.isSelected(), chkClim.isSelected());
            table.setItems(FXCollections.observableArrayList(dispo));
            lblInfo.setText(dispo.size() + " salle(s) disponible(s) pour ce créneau.");
        });

        btnReset.setOnAction(e -> {
            datePicker.setValue(LocalDate.now());
            spinHeure.getValueFactory().setValue(8);
            spinMinute.getValueFactory().setValue(0);
            spinDuree.getValueFactory().setValue(60);
            spinCap.getValueFactory().setValue(0);
            cbType.setValue("Tous");
            chkVideo.setSelected(false); chkTI.setSelected(false); chkClim.setSelected(false);
            table.setItems(FXCollections.observableArrayList());
            lblInfo.setText("");
        });

        panel.getChildren().addAll(titre, filtresBox, lblResultats, lblInfo, table);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private List<Salle> filtrerParCriteres(List<Salle> salles, int capMin, String type,
                                            boolean video, boolean ti, boolean clim) {
        return salles.stream()
            .filter(s -> s.getCapacite() >= capMin)
            .filter(s -> type.equals("Tous") || s.getType().equals(type))
            .filter(s -> !video || s.isVideoprojecteur())
            .filter(s -> !ti || s.isTableauInteractif())
            .filter(s -> !clim || s.isClimatisation())
            .collect(java.util.stream.Collectors.toList());
    }
}
