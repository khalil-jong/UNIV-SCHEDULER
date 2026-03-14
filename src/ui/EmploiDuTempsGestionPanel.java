package ui;

import java.time.LocalTime;
import java.util.List;

import dao.EmploiDuTempsDAO;
import dao.SalleDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.EmploiDuTemps;
import models.Salle;

/**
 * Panneau GESTIONNAIRE : créer / supprimer des créneaux d'emploi du temps.
 * Accessible via GestionnairePanel → menu "📋 Emploi du temps".
 */
public class EmploiDuTempsGestionPanel {

    private EmploiDuTempsDAO edtDAO = new EmploiDuTempsDAO();
    private SalleDAO salleDAO = new SalleDAO();

    private ObservableList<EmploiDuTemps> items = FXCollections.observableArrayList();
    private TableView<EmploiDuTemps> table = new TableView<>();

    // Filtre courant
    private String classeFiltre = null;

    public ScrollPane createPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(22));

        // ── Titre ──
        Label titre = new Label("📋 Gestion des Emplois du Temps");
        titre.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label desc = new Label("Définissez l'organisation hebdomadaire des cours par classe : matière, enseignant, salle, jour et horaire.");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        desc.setWrapText(true);

        // ── Filtre classe ──
        HBox filtreBox = new HBox(10);
        filtreBox.setAlignment(Pos.CENTER_LEFT);
        Label lblFiltre = new Label("Afficher la classe :");
        ComboBox<String> cbFiltre = new ComboBox<>();
        cbFiltre.getItems().addAll(edtDAO.obtenirToutesLesClasses());
        if (!cbFiltre.getItems().isEmpty()) {
			cbFiltre.setValue(cbFiltre.getItems().get(0));
		}
        cbFiltre.setPromptText("-- Choisir une classe --");
        cbFiltre.setPrefWidth(200);
        cbFiltre.setOnAction(e -> {
            String v = cbFiltre.getValue();
            classeFiltre = (v == null) ? null : v;
            chargerTable();
        });
        Button btnRefresh = new Button("🔄");
        btnRefresh.setOnAction(e -> {
            String cur = cbFiltre.getValue();
            cbFiltre.getItems().clear();
            cbFiltre.getItems().addAll(edtDAO.obtenirToutesLesClasses());
            if (cbFiltre.getItems().contains(cur)) {
				cbFiltre.setValue(cur);
			} else if (!cbFiltre.getItems().isEmpty()) {
				cbFiltre.setValue(cbFiltre.getItems().get(0));
			}
            classeFiltre = cbFiltre.getValue();
            chargerTable();
        });
        filtreBox.getChildren().addAll(lblFiltre, cbFiltre, btnRefresh);

        // ── Tableau ──
        construireTable();
        chargerTable();

        // ── Formulaire d'ajout ──
        VBox formBox = creerFormulaire(cbFiltre);

        panel.getChildren().addAll(titre, desc, filtreBox, table, new Separator(), formBox);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ── Construction du TableView ─────────────────────────────────────
    private void construireTable() {
        table.setPrefHeight(300);
        table.setPlaceholder(new Label("Aucun créneau défini."));
        table.setItems(items);

        TableColumn<EmploiDuTemps, String> colClasse = new TableColumn<>("Classe");
        colClasse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClasse()));
        colClasse.setPrefWidth(120);

        TableColumn<EmploiDuTemps, String> colJour = new TableColumn<>("Jour");
        colJour.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomJour()));
        colJour.setPrefWidth(80);

        TableColumn<EmploiDuTemps, String> colHeure = new TableColumn<>("Heure");
        colHeure.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getHeureDebut().toString() + " → " + c.getValue().getHeureFin().toString()));
        colHeure.setPrefWidth(115);

        TableColumn<EmploiDuTemps, String> colMatiere = new TableColumn<>("Matière");
        colMatiere.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMatiere()));
        colMatiere.setPrefWidth(130);

        TableColumn<EmploiDuTemps, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeCours()));
        colType.setPrefWidth(55);

        TableColumn<EmploiDuTemps, String> colEns = new TableColumn<>("Enseignant");
        colEns.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEnseignant()));
        colEns.setPrefWidth(130);

        TableColumn<EmploiDuTemps, String> colSalle = new TableColumn<>("Salle");
        colSalle.setCellValueFactory(c -> {
            Salle s = salleDAO.obtenirParId(c.getValue().getSalleId());
            return new SimpleStringProperty(s != null ? s.getNumero() + " (" + s.getBatiment() + ")" : "?");
        });
        colSalle.setPrefWidth(130);

        TableColumn<EmploiDuTemps, Integer> colDuree = new TableColumn<>("Durée");
        colDuree.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDuree()));
        colDuree.setPrefWidth(60);

        TableColumn<EmploiDuTemps, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(90);
        colAction.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            private final Button btnSuppr = new Button("🗑 Suppr.");
            {
                btnSuppr.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
                btnSuppr.setOnAction(e -> {
                    EmploiDuTemps edt = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Supprimer le créneau \"" + edt.getMatiere() + "\" — " + edt.getNomJour() + " à " + edt.getHeureDebut() + " ?",
                        ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.YES) {
                            edtDAO.supprimer(edt.getId());
                            chargerTable();
                        }
                    });
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btnSuppr);
            }
        });

        table.getColumns().addAll(colClasse, colJour, colHeure, colMatiere, colType, colEns, colSalle, colDuree, colAction);
    }

    private void chargerTable() {
        List<EmploiDuTemps> data = (classeFiltre != null)
            ? edtDAO.obtenirParClasse(classeFiltre)
            : new java.util.ArrayList<>();
        items.setAll(data);
    }

    // ── Formulaire d'ajout ────────────────────────────────────────────
    private VBox creerFormulaire(ComboBox<String> cbFiltreExternal) {
        VBox form = new VBox(14);
        form.setPadding(new Insets(16));
        form.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-background-color: white; -fx-background-radius: 8;");

        Label lblForm = new Label("➕ Ajouter un créneau");
        lblForm.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(12);

        // Champs
        TextField tfClasse    = new TextField(); tfClasse.setPromptText("Ex: L2-Informatique"); tfClasse.setPrefWidth(200);
        TextField tfMatiere   = new TextField(); tfMatiere.setPromptText("Ex: Algorithmique");  tfMatiere.setPrefWidth(200);
        TextField tfEnseignant= new TextField(); tfEnseignant.setPromptText("Ex: Martin Jean");  tfEnseignant.setPrefWidth(200);

        ComboBox<Salle> cbSalle = new ComboBox<>(FXCollections.observableArrayList(salleDAO.obtenirTous()));
        cbSalle.setPromptText("Sélectionner une salle");
        cbSalle.setPrefWidth(220);
        cbSalle.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Salle s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getNumero() + " — " + s.getBatiment() + " (Cap: " + s.getCapacite() + ")");
            }
        });
        cbSalle.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Salle s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "Sélectionner une salle" : s.getNumero() + " — " + s.getBatiment());
            }
        });

        ComboBox<String> cbJour = new ComboBox<>();
        cbJour.getItems().addAll("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi");
        cbJour.setValue("Lundi");

        Spinner<Integer> spHeure  = new Spinner<>(7, 21, 8);  spHeure.setPrefWidth(80);
        Spinner<Integer> spMinute = new Spinner<>(0, 59, 0);  spMinute.setPrefWidth(80);
        Spinner<Integer> spDuree  = new Spinner<>(30, 300, 90); spDuree.setPrefWidth(90);

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("CM", "TD", "TP");
        cbType.setValue("CM");

        HBox heureBox = new HBox(6, new Label("h"), spHeure, new Label("min"), spMinute);
        heureBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Classe :"),        0, 0); grid.add(tfClasse,     1, 0);
        grid.add(new Label("Matière :"),       0, 1); grid.add(tfMatiere,    1, 1);
        grid.add(new Label("Enseignant :"),    0, 2); grid.add(tfEnseignant, 1, 2);
        grid.add(new Label("Salle :"),         0, 3); grid.add(cbSalle,      1, 3);
        grid.add(new Label("Jour :"),          0, 4); grid.add(cbJour,       1, 4);
        grid.add(new Label("Heure début :"),   0, 5); grid.add(heureBox,     1, 5);
        grid.add(new Label("Durée (min) :"),   0, 6); grid.add(spDuree,      1, 6);
        grid.add(new Label("Type de cours :"), 0, 7); grid.add(cbType,       1, 7);

        Label msgLabel = new Label("");
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 12;");

        Button btnAjouter = new Button("✅ Ajouter le créneau");
        btnAjouter.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 22; -fx-font-weight: bold;");

        btnAjouter.setOnAction(e -> {
            // Validation
            if (tfClasse.getText().isEmpty() || tfMatiere.getText().isEmpty()
                    || tfEnseignant.getText().isEmpty() || cbSalle.getValue() == null) {
                msgLabel.setText("⚠️ Remplissez tous les champs obligatoires.");
                msgLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12;");
                return;
            }
            int jourIdx = cbJour.getItems().indexOf(cbJour.getValue()) + 1; // 1=Lundi
            LocalTime heureDebut = LocalTime.of(spHeure.getValue(), spMinute.getValue());
            int duree = spDuree.getValue();
            int salleId = cbSalle.getValue().getId();
            String enseignant = tfEnseignant.getText().trim();

            // Vérifier disponibilité salle
            if (edtDAO.salleOccupee(salleId, jourIdx, heureDebut, duree, -1)) {
                msgLabel.setText("❌ La salle " + cbSalle.getValue().getNumero()
                    + " est déjà occupée ce créneau dans l'emploi du temps.");
                msgLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                return;
            }
            // Vérifier disponibilité enseignant
            if (edtDAO.enseignantOccupe(enseignant, jourIdx, heureDebut, duree, -1)) {
                msgLabel.setText("❌ L'enseignant " + enseignant + " a déjà un créneau à ce moment.");
                msgLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                return;
            }

            EmploiDuTemps edt = new EmploiDuTemps(0,
                tfClasse.getText().trim(), tfMatiere.getText().trim(),
                enseignant, salleId, jourIdx, heureDebut, duree, cbType.getValue());

            try {
                edtDAO.ajouter(edt);
                msgLabel.setText("✅ Créneau ajouté : " + edt.getMatiere() + " — " + cbJour.getValue()
                    + " à " + heureDebut + " (" + duree + " min)");
                msgLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
                // Réinitialiser
                tfClasse.clear(); tfMatiere.clear(); tfEnseignant.clear();
                cbSalle.setValue(null); cbJour.setValue("Lundi");
                // Rafraîchir le filtre et le tableau
                String classeAjoutee = edt.getClasse();
                cbFiltreExternal.getItems().clear();
                cbFiltreExternal.getItems().addAll(edtDAO.obtenirToutesLesClasses());
                if (cbFiltreExternal.getItems().contains(classeAjoutee)) {
					cbFiltreExternal.setValue(classeAjoutee);
				} else if (!cbFiltreExternal.getItems().isEmpty()) {
					cbFiltreExternal.setValue(cbFiltreExternal.getItems().get(0));
				}
                classeFiltre = cbFiltreExternal.getValue();
                chargerTable();
            } catch (Exception ex) {
                msgLabel.setText("❌ Erreur : " + ex.getMessage());
                msgLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
            }
        });

        form.getChildren().addAll(lblForm, grid, btnAjouter, msgLabel);
        return form;
    }
}
