package ui;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import dao.ClasseDAO;
import dao.CoursDAO;
import dao.EmploiDuTempsDAO;
import dao.SalleDAO;
import dao.UtilisateurDAO;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Classe;
import models.EmploiDuTemps;
import models.Salle;
import models.Utilisateur;

/**
 * Panel UNIFIÉ Gestionnaire — redesigné pour cohérence visuelle totale.
 *   - Onglet 1 : Gestion des Classes (CRUD)
 *   - Onglet 2 : Emploi du temps (créneaux hebdomadaires)
 *
 * Logique métier inchangée — seul le design est retravaillé.
 */
public class GestionCoursEDTPanel {

    private ClasseDAO        classeDAO  = new ClasseDAO();
    private CoursDAO         coursDAO   = new CoursDAO();
    private EmploiDuTempsDAO edtDAO     = new EmploiDuTempsDAO();
    private SalleDAO         salleDAO   = new SalleDAO();
    private UtilisateurDAO   userDAO    = new UtilisateurDAO();
    private DateTimeFormatter fmt       = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ScrollPane createPanel() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle(
            "-fx-font-size: 13;" +
            "-fx-tab-min-height: 36;" +
            "-fx-background-color: " + Design.BG_LIGHT + ";"
        );

        Tab tabClasses = new Tab("🎓  Classes",            creerOngletClasses());
        Tab tabEDT     = new Tab("📋  Emploi du temps",    creerOngletEDT());
        tabs.getTabs().addAll(tabClasses, tabEDT);

        VBox wrapper = new VBox(tabs);
        wrapper.setPadding(new Insets(16));
        wrapper.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ═══════════════════════════════════════════════════════════════
    //  ONGLET 1 — CLASSES
    // ═══════════════════════════════════════════════════════════════
    private VBox creerOngletClasses() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("🎓  Gestion des Classes");
        Label desc  = Design.muted("Créez et gérez les classes. La suppression retire aussi tous les créneaux EDT associés.");
        panel.getChildren().addAll(titre, desc);

        // ── Tableau ──────────────────────────────────────────────────
        ObservableList<Classe> items = FXCollections.observableArrayList(classeDAO.obtenirTous());
        TableView<Classe> table = new TableView<>(items);
        table.setPrefHeight(250);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + Design.BORDER + ";");
        table.setPlaceholder(new Label("Aucune classe définie."));

        TableColumn<Classe, String>  colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        TableColumn<Classe, String>  colFil = new TableColumn<>("Filière");
        colFil.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFiliere()));
        TableColumn<Classe, String>  colNiv = new TableColumn<>("Niveau");
        colNiv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNiveau()));
        TableColumn<Classe, Integer> colEff = new TableColumn<>("Effectif");
        colEff.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getEffectif()));
        colEff.setMaxWidth(80);

        TableColumn<Classe, Void> colAct = new TableColumn<>("Action");
        colAct.setMaxWidth(110);
        colAct.setCellFactory(col -> new TableCell<>() {
            final Button btn = Design.btnDanger("🗑  Supprimer");
            {
                btn.setPadding(new Insets(5, 10, 5, 10));
                btn.setStyle(btn.getStyle() + "-fx-font-size: 11;");
                btn.setOnAction(e -> {
                    Classe cl = getTableView().getItems().get(getIndex());
                    if (confirmer("Supprimer la classe \"" + cl.getNom() + "\" ?\n\n" +
                            "⚠️  Tous les cours et créneaux EDT de cette classe seront aussi supprimés.")) {
                        classeDAO.supprimer(cl.getId());
                        items.setAll(classeDAO.obtenirTous());
                        rechargerToutesLesComboBox();
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(colNom, colFil, colNiv, colEff, colAct);

        // ── Formulaire ajout/modification ────────────────────────────
        VBox formBox = Design.section("✏️  Ajouter / Modifier une classe");

        Label lblMode = new Label("Mode : ➕  Ajout");
        lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 4, 0));

        TextField tfNom     = sf("Ex: L2-Informatique", 220);
        TextField tfFiliere = sf("Ex: Informatique", 200);
        TextField tfNiveau  = sf("Ex: Licence 2", 180);
        Spinner<Integer> spEff = new Spinner<>(0, 500, 30);
        spEff.setPrefWidth(100);
        spEff.setEditable(true);

        grid.add(fl("Nom :"),       0, 0); grid.add(tfNom,     1, 0);
        grid.add(fl("Filière :"),   0, 1); grid.add(tfFiliere, 1, 1);
        grid.add(fl("Niveau :"),    0, 2); grid.add(tfNiveau,  1, 2);
        grid.add(fl("Effectif :"),  0, 3); grid.add(spEff,     1, 3);

        Label msgCl = new Label(""); msgCl.setWrapText(true);

        // Pré-remplir en cliquant sur la table
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                tfNom.setText(sel.getNom());
                tfFiliere.setText(sel.getFiliere());
                tfNiveau.setText(sel.getNiveau());
                spEff.getValueFactory().setValue(sel.getEffectif());
                lblMode.setText("Mode : ✏️  Modification de " + sel.getNom());
                lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.WARNING + ";-fx-font-weight:bold;");
            }
        });

        Button btnSave    = Design.btnPrimary("💾  Enregistrer", Design.SUCCESS);
        Button btnAnnuler = Design.btnSecondary("✖  Annuler");

        btnAnnuler.setOnAction(e -> {
            table.getSelectionModel().clearSelection();
            tfNom.clear(); tfFiliere.clear(); tfNiveau.clear();
            msgCl.setText("");
            lblMode.setText("Mode : ➕  Ajout");
            lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");
        });

        btnSave.setOnAction(e -> {
            if (tfNom.getText().isEmpty()) {
                setMsg(msgCl, "⚠️  Le nom est obligatoire.", Design.WARNING); return;
            }
            Classe sel = table.getSelectionModel().getSelectedItem();
            Classe cl  = new Classe(
                sel != null ? sel.getId() : 0,
                tfNom.getText().trim(), tfFiliere.getText().trim(),
                tfNiveau.getText().trim(), spEff.getValue()
            );
            try {
                if (sel != null) {
					classeDAO.modifier(cl);
				} else {
					classeDAO.ajouter(cl);
				}
                items.setAll(classeDAO.obtenirTous());
                rechargerToutesLesComboBox();
                table.getSelectionModel().clearSelection();
                tfNom.clear(); tfFiliere.clear(); tfNiveau.clear();
                lblMode.setText("Mode : ➕  Ajout");
                lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");
                setMsg(msgCl, "✅  Classe enregistrée.", Design.SUCCESS);
            } catch (Exception ex) {
                setMsg(msgCl, "❌  " + ex.getMessage(), Design.DANGER);
            }
        });

        formBox.getChildren().addAll(lblMode, grid, new HBox(10, btnSave, btnAnnuler), msgCl);
        panel.getChildren().addAll(table, formBox);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════
    //  ONGLET 2 — EMPLOI DU TEMPS HEBDOMADAIRE
    // ═══════════════════════════════════════════════════════════════
    private VBox creerOngletEDT() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("📋  Emploi du Temps Hebdomadaire");
        Label desc  = Design.muted("Gérez les créneaux récurrents de chaque classe. Sélectionnez une classe pour voir et modifier son emploi du temps.");
        panel.getChildren().addAll(titre, desc);

        // ── Filtre classe ─────────────────────────────────────────────
        VBox filtreCard = new VBox(8);
        filtreCard.setPadding(new Insets(14, 16, 14, 16));
        filtreCard.setStyle(Design.CARD_STYLE);

        HBox filtreBox = new HBox(12);
        filtreBox.setAlignment(Pos.CENTER_LEFT);
        Label lblFil = fl("Classe :");
        ComboBox<String> cbClasseFiltre = new ComboBox<>();
        rechargerClasses(cbClasseFiltre);
        cbClasseFiltre.setPromptText("Sélectionner une classe…");
        cbClasseFiltre.setPrefWidth(240);

        Label lblNbCreneaux = new Label("");
        lblNbCreneaux.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";");

        filtreBox.getChildren().addAll(lblFil, cbClasseFiltre, lblNbCreneaux);
        filtreCard.getChildren().add(filtreBox);
        panel.getChildren().add(filtreCard);

        // ── Tableau EDT ───────────────────────────────────────────────
        ObservableList<EmploiDuTemps> items = FXCollections.observableArrayList();
        TableView<EmploiDuTemps> table = new TableView<>(items);
        table.setPrefHeight(260);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + Design.BORDER + ";");
        table.setPlaceholder(new Label("Sélectionnez une classe pour voir son emploi du temps."));

        cbClasseFiltre.setOnAction(e -> {
            String v = cbClasseFiltre.getValue();
            if (v != null) {
                items.setAll(edtDAO.obtenirParClasse(v));
                lblNbCreneaux.setText("→  " + items.size() + " créneau(x)");
            }
        });

        TableColumn<EmploiDuTemps, String> cJour  = new TableColumn<>("Jour");
        cJour.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomJour()));
        TableColumn<EmploiDuTemps, String> cHeure = new TableColumn<>("Horaire");
        cHeure.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getHeureDebut() + " → " + c.getValue().getHeureFin()));
        TableColumn<EmploiDuTemps, String> cMat   = new TableColumn<>("Matière");
        cMat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMatiere()));
        TableColumn<EmploiDuTemps, String> cType  = new TableColumn<>("Type");
        cType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeCours()));
        cType.setMaxWidth(60);
        TableColumn<EmploiDuTemps, String> cEns   = new TableColumn<>("Enseignant");
        cEns.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEnseignant()));
        TableColumn<EmploiDuTemps, String> cSalle = new TableColumn<>("Salle");
        cSalle.setCellValueFactory(c -> {
            Salle s = salleDAO.obtenirParId(c.getValue().getSalleId());
            return new SimpleStringProperty(s != null ? s.getNumero() : "?");
        });
        cSalle.setMaxWidth(80);

        TableColumn<EmploiDuTemps, Void> cAct = new TableColumn<>("Action");
        cAct.setMaxWidth(100);
        cAct.setCellFactory(col -> new TableCell<>() {
            final Button btn = Design.btnDanger("🗑  Suppr.");
            {
                btn.setPadding(new Insets(4, 10, 4, 10));
                btn.setStyle(btn.getStyle() + "-fx-font-size: 11;");
                btn.setOnAction(e -> {
                    EmploiDuTemps edt = getTableView().getItems().get(getIndex());
                    if (confirmer("Supprimer ce créneau ?")) {
                        edtDAO.supprimer(edt.getId());
                        items.setAll(edtDAO.obtenirParClasse(cbClasseFiltre.getValue()));
                        lblNbCreneaux.setText("→  " + items.size() + " créneau(x)");
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(cJour, cHeure, cMat, cType, cEns, cSalle, cAct);
        panel.getChildren().add(table);

        // ── Formulaire ajout créneau EDT ──────────────────────────────
        VBox formBox = Design.section("➕  Ajouter un créneau à l'emploi du temps");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 4, 0));

        ComboBox<String> cbClasse = new ComboBox<>();
        rechargerClasses(cbClasse);
        cbClasse.setPromptText("Classe"); cbClasse.setPrefWidth(220);

        TextField tfMatiere = sf("Ex: Algorithmique", 200);

        // Enseignants
        ComboBox<Utilisateur> cbEns = new ComboBox<>(
            FXCollections.observableArrayList(userDAO.obtenirParRole("ENSEIGNANT")));
        cbEns.setPromptText("Sélectionner un enseignant"); cbEns.setPrefWidth(240);
        cbEns.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Utilisateur u, boolean e) {
                super.updateItem(u, e); setText(e || u == null ? null : u.getNomComplet());
            }
        });
        cbEns.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Utilisateur u, boolean e) {
                super.updateItem(u, e);
                setText(e || u == null ? "Sélectionner un enseignant" : u.getNomComplet());
            }
        });

        // Salles
        ComboBox<Salle> cbSalle = new ComboBox<>(
            FXCollections.observableArrayList(salleDAO.obtenirTous()));
        cbSalle.setPromptText("Salle"); cbSalle.setPrefWidth(240);
        cbSalle.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Salle s, boolean e) {
                super.updateItem(s, e);
                setText(e || s == null ? null : s.getNumero() + " (" + s.getBatiment() + ", cap:" + s.getCapacite() + ")");
            }
        });
        cbSalle.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Salle s, boolean e) {
                super.updateItem(s, e);
                setText(e || s == null ? "Salle" : s.getNumero() + " - " + s.getBatiment());
            }
        });

        ComboBox<String> cbJour = new ComboBox<>();
        cbJour.getItems().addAll("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi");
        cbJour.setValue("Lundi"); cbJour.setPrefWidth(140);

        Spinner<Integer> spH  = new Spinner<>(7, 21, 8); spH.setPrefWidth(78);  spH.setEditable(true);
        Spinner<Integer> spM  = new Spinner<>(0, 59, 0); spM.setPrefWidth(78);  spM.setEditable(true);
        Spinner<Integer> spD  = new Spinner<>(30, 300, 90); spD.setPrefWidth(88); spD.setEditable(true);

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("CM", "TD", "TP"); cbType.setValue("CM"); cbType.setPrefWidth(100);

        HBox heureBox = new HBox(6,
            spH, new Label("h"), spM, new Label("min"));
        heureBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(fl("Classe :"),        0, 0); grid.add(cbClasse,  1, 0);
        grid.add(fl("Matière :"),       0, 1); grid.add(tfMatiere, 1, 1);
        grid.add(fl("Enseignant :"),    0, 2); grid.add(cbEns,     1, 2);
        grid.add(fl("Salle :"),         0, 3); grid.add(cbSalle,   1, 3);
        grid.add(fl("Jour :"),          0, 4); grid.add(cbJour,    1, 4);
        grid.add(fl("Heure début :"),   0, 5); grid.add(heureBox,  1, 5);
        grid.add(fl("Durée (min) :"),   0, 6); grid.add(spD,       1, 6);
        grid.add(fl("Type :"),          0, 7); grid.add(cbType,    1, 7);

        Label msgEDT = new Label(""); msgEDT.setWrapText(true);

        Button btnAjout = Design.btnPrimary("✅  Ajouter le créneau", Design.GEST_ACCENT);
        btnAjout.setOnAction(e -> {
            if (cbClasse.getValue() == null || tfMatiere.getText().isEmpty()
                    || cbEns.getValue() == null || cbSalle.getValue() == null) {
                setMsg(msgEDT, "⚠️  Remplissez tous les champs obligatoires.", Design.WARNING);
                return;
            }
            int jourIdx = cbJour.getItems().indexOf(cbJour.getValue()) + 1;
            LocalTime hDebut = LocalTime.of(spH.getValue(), spM.getValue());
            int duree = spD.getValue();
            int salleId = cbSalle.getValue().getId();
            String nomEns = cbEns.getValue().getNomComplet();

            if (edtDAO.salleOccupee(salleId, jourIdx, hDebut, duree, -1)) {
                setMsg(msgEDT, "❌  Salle " + cbSalle.getValue().getNumero() + " déjà occupée ce créneau.", Design.DANGER);
                return;
            }
            if (edtDAO.enseignantOccupe(nomEns, jourIdx, hDebut, duree, -1)) {
                setMsg(msgEDT, "❌  " + nomEns + " a déjà un cours ce créneau.", Design.DANGER);
                return;
            }
            EmploiDuTemps edt = new EmploiDuTemps(0, cbClasse.getValue(),
                tfMatiere.getText().trim(), nomEns, salleId, jourIdx, hDebut, duree, cbType.getValue());
            edtDAO.ajouter(edt);

            // Créer aussi un cours ponctuel pour la semaine courante
            java.time.LocalDate lundi    = java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            java.time.LocalDate jourDate = lundi.plusDays(jourIdx - 1);
            java.time.LocalDateTime debutCours = jourDate.atTime(hDebut);
            models.Cours coursSync = new models.Cours(0,
                tfMatiere.getText().trim(), nomEns, cbClasse.getValue(), "",
                debutCours, duree, salleId);
            coursDAO.ajouter(coursSync);

            if (cbClasseFiltre.getValue() != null && cbClasseFiltre.getValue().equals(cbClasse.getValue())) {
                items.setAll(edtDAO.obtenirParClasse(cbClasse.getValue()));
                lblNbCreneaux.setText("→  " + items.size() + " créneau(x)");
            }
            rechargerClasses(cbClasse);
            rechargerClasses(cbClasseFiltre);
            setMsg(msgEDT, "✅  Créneau ajouté dans l'EDT, le calendrier et la liste des cours de " + nomEns + ".", Design.SUCCESS);
        });

        formBox.getChildren().addAll(grid, btnAjout, msgEDT);
        panel.getChildren().add(formBox);
        return panel;
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    private final java.util.List<ComboBox<String>> toutesLesComboBoxClasse = new java.util.ArrayList<>();

    private void rechargerToutesLesComboBox() {
        for (ComboBox<String> cb : toutesLesComboBoxClasse) {
            String cur = cb.getValue();
            cb.getItems().setAll(classeDAO.obtenirNomsClasses());
            if (cur != null && !cb.getItems().contains(cur)) {
				cb.setValue(null);
			}
        }
    }

    private void rechargerClasses(ComboBox<String> cb) {
        if (!toutesLesComboBoxClasse.contains(cb)) {
			toutesLesComboBoxClasse.add(cb);
		}
        String cur = cb.getValue();
        cb.getItems().setAll(classeDAO.obtenirNomsClasses());
        if (cur != null && cb.getItems().contains(cur)) {
			cb.setValue(cur);
		}
    }

    private boolean confirmer(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.YES;
    }

    private void setMsg(Label lbl, String text, String color) {
        lbl.setText(text);
        lbl.setStyle("-fx-font-size:12;-fx-text-fill:" + color + ";-fx-font-weight:bold;" +
            "-fx-padding:6 10;-fx-background-color:derive(" + color + ",85%);-fx-background-radius:6;");
    }

    private TextField sf(String prompt, double w) {
        TextField tf = new TextField();
        tf.setPromptText(prompt); tf.setPrefWidth(w); tf.setStyle(Design.INPUT_STYLE);
        return tf;
    }

    private Label fl(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";-fx-min-width:130;");
        return lbl;
    }
}
