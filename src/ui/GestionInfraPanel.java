package ui;

import dao.BatimentDAO;
import dao.SalleDAO;
import models.Batiment;
import models.Salle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.Optional;

// Gestion des bâtiments + salles + équipements
public class GestionInfraPanel {

    private BatimentDAO batimentDAO = new BatimentDAO();
    private SalleDAO salleDAO = new SalleDAO();

    public ScrollPane createPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));

        Label titre = new Label("🏗️ Gestion des Infrastructures");
        titre.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabBat = new Tab("🏢 Bâtiments", creerOngletBatiments());
        Tab tabSalle = new Tab("🏫 Salles & Équipements", creerOngletSalles());

        tabs.getTabs().addAll(tabBat, tabSalle);
        panel.getChildren().addAll(titre, tabs);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ─── ONGLET BÂTIMENTS ───────────────────────────────────────────
    private VBox creerOngletBatiments() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));

        TableView<Batiment> table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<Batiment, String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colNom.setPrefWidth(150);

        TableColumn<Batiment, String> colLoc = new TableColumn<>("Localisation");
        colLoc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLocalisation()));
        colLoc.setPrefWidth(200);

        TableColumn<Batiment, Integer> colEtages = new TableColumn<>("Étages");
        colEtages.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getNombreEtages()));
        colEtages.setPrefWidth(80);

        table.getColumns().addAll(colNom, colLoc, colEtages);
        rafraichirBatiments(table);

        // Formulaire ajout
        Label lblForm = new Label("Ajouter un bâtiment :");
        lblForm.setStyle("-fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-color: #fafafa;");

        TextField tfNom = new TextField(); tfNom.setPromptText("Ex: Bâtiment A"); tfNom.setPrefWidth(180);
        TextField tfLoc = new TextField(); tfLoc.setPromptText("Ex: Campus Nord"); tfLoc.setPrefWidth(180);
        Spinner<Integer> spinEtages = new Spinner<>(1, 20, 3); spinEtages.setPrefWidth(100);

        grid.add(new Label("Nom :"), 0, 0); grid.add(tfNom, 1, 0);
        grid.add(new Label("Localisation :"), 0, 1); grid.add(tfLoc, 1, 1);
        grid.add(new Label("Nombre d'étages :"), 0, 2); grid.add(spinEtages, 1, 2);

        Button btnAjouter = new Button("➕ Ajouter");
        btnAjouter.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 16;");
        btnAjouter.setOnAction(e -> {
            if (tfNom.getText().isEmpty() || tfLoc.getText().isEmpty()) {
                alert(Alert.AlertType.WARNING, "Champs manquants", "Remplissez tous les champs.");
                return;
            }
            try {
                Batiment b = new Batiment();
                b.setNom(tfNom.getText().trim());
                b.setLocalisation(tfLoc.getText().trim());
                b.setNombreEtages(spinEtages.getValue());
                batimentDAO.ajouter(b);
                rafraichirBatiments(table);
                tfNom.clear(); tfLoc.clear();
                alert(Alert.AlertType.INFORMATION, "Succès", "Bâtiment ajouté avec succès.");
            } catch (RuntimeException ex) {
                alert(Alert.AlertType.ERROR, "Erreur", ex.getMessage());
            }
        });

        Button btnSupprimer = new Button("🗑 Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16;");
        btnSupprimer.setOnAction(e -> {
            Batiment sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alert(Alert.AlertType.WARNING, "Attention", "Sélectionnez un bâtiment."); return; }
            Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + sel.getNom() + " ?", ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                try { batimentDAO.supprimer(sel.getId()); rafraichirBatiments(table); }
                catch (RuntimeException ex) { alert(Alert.AlertType.ERROR, "Erreur", ex.getMessage()); }
            }
        });

        HBox boutons = new HBox(10, btnAjouter, btnSupprimer);
        panel.getChildren().addAll(table, boutons, lblForm, grid);
        return panel;
    }

    private void rafraichirBatiments(TableView<Batiment> t) {
        t.setItems(FXCollections.observableArrayList(batimentDAO.obtenirTous()));
    }

    // ─── ONGLET SALLES & ÉQUIPEMENTS ──────────────────────────────────
    private VBox creerOngletSalles() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));

        // Tableau des salles avec équipements
        TableView<Salle> table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<Salle, String> colNum = new TableColumn<>("Numéro");
        colNum.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNumero()));
        colNum.setPrefWidth(80);

        TableColumn<Salle, String> colBat = new TableColumn<>("Bâtiment");
        colBat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBatiment()));
        colBat.setPrefWidth(120);

        TableColumn<Salle, String> colEtage = new TableColumn<>("Étage");
        colEtage.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEtage()));
        colEtage.setPrefWidth(80);

        TableColumn<Salle, Integer> colCap = new TableColumn<>("Capacité");
        colCap.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCapacite()));
        colCap.setPrefWidth(80);

        TableColumn<Salle, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getType()));
        colType.setPrefWidth(70);

        TableColumn<Salle, String> colEquip = new TableColumn<>("Équipements");
        colEquip.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEquipementsStr()));
        colEquip.setPrefWidth(130);

        table.getColumns().addAll(colNum, colBat, colEtage, colCap, colType, colEquip);
        rafraichirSalles(table);

        // Formulaire ajout salle avec équipements
        Label lblForm = new Label("Ajouter une salle :");
        lblForm.setStyle("-fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-color: #fafafa;");

        TextField tfNumero = new TextField(); tfNumero.setPromptText("Ex: A101");
        TextField tfBatiment = new TextField(); tfBatiment.setPromptText("Ex: Bâtiment A");
        TextField tfEtage = new TextField(); tfEtage.setPromptText("Ex: 1er étage");
        TextField tfCapacite = new TextField(); tfCapacite.setPromptText("Ex: 50");
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("TD", "TP", "Amphi");
        cbType.setPromptText("Type");

        // Équipements (checkboxes)
        CheckBox chkVideo = new CheckBox("📽 Vidéoprojecteur");
        CheckBox chkTI = new CheckBox("🖥 Tableau interactif");
        CheckBox chkClim = new CheckBox("❄ Climatisation");
        HBox equips = new HBox(15, chkVideo, chkTI, chkClim);

        grid.add(new Label("Numéro :"), 0, 0); grid.add(tfNumero, 1, 0);
        grid.add(new Label("Bâtiment :"), 2, 0); grid.add(tfBatiment, 3, 0);
        grid.add(new Label("Étage :"), 0, 1); grid.add(tfEtage, 1, 1);
        grid.add(new Label("Capacité :"), 2, 1); grid.add(tfCapacite, 3, 1);
        grid.add(new Label("Type :"), 0, 2); grid.add(cbType, 1, 2);
        grid.add(new Label("Équipements :"), 0, 3); grid.add(equips, 1, 3, 3, 1);

        Button btnAjouter = new Button("➕ Ajouter la salle");
        btnAjouter.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 16;");
        btnAjouter.setOnAction(e -> {
            if (tfNumero.getText().isEmpty() || tfBatiment.getText().isEmpty() ||
                tfEtage.getText().isEmpty() || tfCapacite.getText().isEmpty() || cbType.getValue() == null) {
                alert(Alert.AlertType.WARNING, "Champs manquants", "Remplissez tous les champs obligatoires.");
                return;
            }
            int cap;
            try { cap = Integer.parseInt(tfCapacite.getText().trim()); if (cap <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { alert(Alert.AlertType.ERROR, "Erreur", "La capacité doit être un nombre positif."); return; }
            try {
                Salle s = new Salle();
                s.setNumero(tfNumero.getText().trim());
                s.setBatiment(tfBatiment.getText().trim());
                s.setEtage(tfEtage.getText().trim());
                s.setCapacite(cap);
                s.setType(cbType.getValue());
                s.setVideoprojecteur(chkVideo.isSelected());
                s.setTableauInteractif(chkTI.isSelected());
                s.setClimatisation(chkClim.isSelected());
                salleDAO.ajouter(s);
                rafraichirSalles(table);
                tfNumero.clear(); tfBatiment.clear(); tfEtage.clear(); tfCapacite.clear();
                cbType.setValue(null); chkVideo.setSelected(false); chkTI.setSelected(false); chkClim.setSelected(false);
                alert(Alert.AlertType.INFORMATION, "Succès", "Salle ajoutée avec succès !");
            } catch (RuntimeException ex) { alert(Alert.AlertType.ERROR, "Erreur", ex.getMessage()); }
        });

        Button btnSupprimer = new Button("🗑 Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 16;");
        btnSupprimer.setOnAction(e -> {
            Salle sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une salle."); return; }
            Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la salle " + sel.getNumero() + " ?", ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                try { salleDAO.supprimer(sel.getId()); rafraichirSalles(table); }
                catch (RuntimeException ex) { alert(Alert.AlertType.ERROR, "Erreur", ex.getMessage()); }
            }
        });

        HBox boutons = new HBox(10, btnAjouter, btnSupprimer);
        panel.getChildren().addAll(table, boutons, lblForm, grid);
        return panel;
    }

    private void rafraichirSalles(TableView<Salle> t) {
        t.setItems(FXCollections.observableArrayList(salleDAO.obtenirTous()));
    }

    private void alert(Alert.AlertType type, String titre, String msg) {
        Alert a = new Alert(type); a.setTitle(titre); a.setHeaderText(titre); a.setContentText(msg); a.showAndWait();
    }
}
