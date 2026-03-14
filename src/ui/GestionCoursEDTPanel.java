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
 * Panel UNIFIÉ Gestionnaire :
 *   - Onglet 1 : Gestion des Classes (CRUD)
 *   - Onglet 2 : Emploi du temps (créneaux hebdomadaires) + tableau récap
 *   - Onglet 3 : Cours ponctuels (ajout automatique dans l'EDT si même classe/matière)
 *
 * Quand un cours ponctuel est ajouté → un créneau EDT est créé automatiquement
 * pour la semaine correspondante, visible dans le calendrier ET dans l'EDT étudiant/enseignant.
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
        tabs.setStyle("-fx-font-size: 13;");

        Tab tabClasses  = new Tab("🎓 Classes",       creerOngletClasses());
        Tab tabEDT      = new Tab("📋 Emploi du temps",creerOngletEDT());

        tabs.getTabs().addAll(tabClasses, tabEDT);

        VBox wrapper = new VBox(tabs);
        wrapper.setPadding(new Insets(14));
        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        return scroll;
    }


    
    //  ONGLET 1 — CLASSES

    private VBox creerOngletClasses() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(18));

        Label titre = new Label("🎓 Gestion des Classes");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        ObservableList<Classe> items = FXCollections.observableArrayList(classeDAO.obtenirTous());

        TableView<Classe> table = new TableView<>(items);
        table.setPrefHeight(260);
        table.setPlaceholder(new Label("Aucune classe définie."));

        TableColumn<Classe,String> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom())); colNom.setPrefWidth(150);
        TableColumn<Classe,String> colFil = new TableColumn<>("Filière");
        colFil.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFiliere())); colFil.setPrefWidth(120);
        TableColumn<Classe,String> colNiv = new TableColumn<>("Niveau");
        colNiv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNiveau())); colNiv.setPrefWidth(100);
        TableColumn<Classe,Integer> colEff = new TableColumn<>("Effectif");
        colEff.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getEffectif())); colEff.setPrefWidth(70);
        TableColumn<Classe,Void> colAct = new TableColumn<>("Action"); colAct.setPrefWidth(100);
        colAct.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("🗑 Supprimer");
            { btn.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-font-size:11;");
              btn.setOnAction(e -> {
                Classe cl = getTableView().getItems().get(getIndex());
                if (confirmer("Supprimer la classe \""+cl.getNom()+"\" ?")) {
                    classeDAO.supprimer(cl.getId()); items.setAll(classeDAO.obtenirTous());
                }
              });
            }
            @Override protected void updateItem(Void v,boolean empty){super.updateItem(v,empty);setGraphic(empty?null:btn);}
        });
        table.getColumns().addAll(colNom, colFil, colNiv, colEff, colAct);

        // Formulaire ajout/modification
        Label lblForm = new Label("➕ Ajouter / Modifier une classe :");
        lblForm.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        TextField tfNom     = new TextField(); tfNom.setPromptText("Ex: L2-Informatique"); tfNom.setPrefWidth(220);
        TextField tfFiliere = new TextField(); tfFiliere.setPromptText("Ex: Informatique");
        TextField tfNiveau  = new TextField(); tfNiveau.setPromptText("Ex: Licence 2");
        Spinner<Integer> spEff = new Spinner<>(0, 500, 30); spEff.setPrefWidth(100);

        grid.add(new Label("Nom :"),      0,0); grid.add(tfNom,    1,0);
        grid.add(new Label("Filière :"),  0,1); grid.add(tfFiliere,1,1);
        grid.add(new Label("Niveau :"),   0,2); grid.add(tfNiveau, 1,2);
        grid.add(new Label("Effectif :"), 0,3); grid.add(spEff,    1,3);

        Label msgCl = new Label(""); msgCl.setStyle("-fx-font-size:12;");

        // Pré-remplir en cliquant sur la table
        table.getSelectionModel().selectedItemProperty().addListener((obs,old,sel) -> {
            if (sel!=null) { tfNom.setText(sel.getNom()); tfFiliere.setText(sel.getFiliere());
                tfNiveau.setText(sel.getNiveau()); spEff.getValueFactory().setValue(sel.getEffectif()); }
        });

        Button btnSave = new Button("💾 Enregistrer");
        btnSave.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-padding:8 20;-fx-font-weight:bold;");
        btnSave.setOnAction(e -> {
            if (tfNom.getText().isEmpty()) { msgCl.setText("⚠️ Le nom est obligatoire."); return; }
            Classe sel = table.getSelectionModel().getSelectedItem();
            Classe cl  = new Classe(sel!=null?sel.getId():0, tfNom.getText().trim(),
                tfFiliere.getText().trim(), tfNiveau.getText().trim(), spEff.getValue());
            try {
                if (sel!=null) {
					classeDAO.modifier(cl);
				} else {
					classeDAO.ajouter(cl);
				}
                items.setAll(classeDAO.obtenirTous());
                table.getSelectionModel().clearSelection();
                tfNom.clear(); tfFiliere.clear(); tfNiveau.clear();
                msgCl.setText("✅ Classe enregistrée."); msgCl.setStyle("-fx-text-fill:#27ae60;-fx-font-size:12;");
            } catch (Exception ex) { msgCl.setText("❌ "+ex.getMessage()); msgCl.setStyle("-fx-text-fill:#e74c3c;-fx-font-size:12;"); }
        });

        panel.getChildren().addAll(titre, table, lblForm, grid, btnSave, msgCl);
        return panel;
    }

 
    //  ONGLET 2 — EMPLOI DU TEMPS HEBDOMADAIRE
 
    private VBox creerOngletEDT() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(18));

        Label titre = new Label("📋 Emploi du Temps Hebdomadaire");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        // Filtre classe
        HBox filtreBox = new HBox(10);
        filtreBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> cbClasseFiltre = new ComboBox<>();
        rechargerClasses(cbClasseFiltre);
        cbClasseFiltre.setPromptText("-- Choisir une classe --");
        cbClasseFiltre.setPrefWidth(220);

        ObservableList<EmploiDuTemps> items = FXCollections.observableArrayList();
        TableView<EmploiDuTemps> table = new TableView<>(items);
        table.setPrefHeight(270); table.setPlaceholder(new Label("Aucun créneau — sélectionnez une classe."));

        cbClasseFiltre.setOnAction(e -> {
            String v = cbClasseFiltre.getValue();
            if (v!=null) {
				items.setAll(edtDAO.obtenirParClasse(v));
			}
        });

        TableColumn<EmploiDuTemps,String> cJour = new TableColumn<>("Jour");
        cJour.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomJour())); cJour.setPrefWidth(85);
        TableColumn<EmploiDuTemps,String> cHeure = new TableColumn<>("Horaire");
        cHeure.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHeureDebut()+" → "+c.getValue().getHeureFin())); cHeure.setPrefWidth(115);
        TableColumn<EmploiDuTemps,String> cMat = new TableColumn<>("Matière");
        cMat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMatiere())); cMat.setPrefWidth(140);
        TableColumn<EmploiDuTemps,String> cType = new TableColumn<>("Type");
        cType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeCours())); cType.setPrefWidth(55);
        TableColumn<EmploiDuTemps,String> cEns = new TableColumn<>("Enseignant");
        cEns.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEnseignant())); cEns.setPrefWidth(140);
        TableColumn<EmploiDuTemps,String> cSalle = new TableColumn<>("Salle");
        cSalle.setCellValueFactory(c -> { Salle s = salleDAO.obtenirParId(c.getValue().getSalleId());
            return new SimpleStringProperty(s!=null?s.getNumero():"?"); }); cSalle.setPrefWidth(80);
        TableColumn<EmploiDuTemps,Void> cAct = new TableColumn<>("Action"); cAct.setPrefWidth(90);
        cAct.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("🗑 Suppr.");
            { btn.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-font-size:11;");
              btn.setOnAction(e -> {
                EmploiDuTemps edt = getTableView().getItems().get(getIndex());
                if (confirmer("Supprimer ce créneau ?")) { edtDAO.supprimer(edt.getId()); items.setAll(edtDAO.obtenirParClasse(cbClasseFiltre.getValue())); }
              });
            }
            @Override protected void updateItem(Void v,boolean empty){super.updateItem(v,empty);setGraphic(empty?null:btn);}
        });
        table.getColumns().addAll(cJour,cHeure,cMat,cType,cEns,cSalle,cAct);

        filtreBox.getChildren().addAll(new Label("Classe :"), cbClasseFiltre);

        // Formulaire ajout créneau EDT
        Label lblF = new Label("➕ Ajouter un créneau à l'emploi du temps :");
        lblF.setStyle("-fx-font-size:13;-fx-font-weight:bold;");

        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        ComboBox<String> cbClasse  = new ComboBox<>(); rechargerClasses(cbClasse); cbClasse.setPromptText("Classe"); cbClasse.setPrefWidth(200);
        TextField tfMatiere = new TextField(); tfMatiere.setPromptText("Ex: Algorithmique"); tfMatiere.setPrefWidth(200);

        // Enseignant : ComboBox des enseignants enregistrés
        ComboBox<Utilisateur> cbEns = new ComboBox<>(FXCollections.observableArrayList(userDAO.obtenirParRole("ENSEIGNANT")));
        cbEns.setPromptText("Sélectionner un enseignant"); cbEns.setPrefWidth(230);
        cbEns.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Utilisateur u,boolean e){super.updateItem(u,e);setText(e||u==null?null:u.getNomComplet());} });
        cbEns.setButtonCell(new ListCell<>() { @Override protected void updateItem(Utilisateur u,boolean e){super.updateItem(u,e);setText(e||u==null?"Sélectionner un enseignant":u.getNomComplet());} });

        ComboBox<Salle> cbSalle = new ComboBox<>(FXCollections.observableArrayList(salleDAO.obtenirTous()));
        cbSalle.setPromptText("Salle"); cbSalle.setPrefWidth(200);
        cbSalle.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Salle s,boolean e){super.updateItem(s,e);setText(e||s==null?null:s.getNumero()+" ("+s.getBatiment()+", cap:"+s.getCapacite()+")");} });
        cbSalle.setButtonCell(new ListCell<>() { @Override protected void updateItem(Salle s,boolean e){super.updateItem(s,e);setText(e||s==null?"Salle":s.getNumero()+" - "+s.getBatiment());} });

        ComboBox<String> cbJour = new ComboBox<>();
        cbJour.getItems().addAll("Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"); cbJour.setValue("Lundi");
        Spinner<Integer> spH = new Spinner<>(7,21,8); spH.setPrefWidth(75);
        Spinner<Integer> spM = new Spinner<>(0,59,0); spM.setPrefWidth(75);
        Spinner<Integer> spD = new Spinner<>(30,300,90); spD.setPrefWidth(85);
        ComboBox<String> cbType = new ComboBox<>(); cbType.getItems().addAll("CM","TD","TP"); cbType.setValue("CM");

        grid.add(new Label("Classe :"),        0,0); grid.add(cbClasse,  1,0);
        grid.add(new Label("Matière :"),        0,1); grid.add(tfMatiere, 1,1);
        grid.add(new Label("Enseignant :"),     0,2); grid.add(cbEns,     1,2);
        grid.add(new Label("Salle :"),          0,3); grid.add(cbSalle,   1,3);
        grid.add(new Label("Jour :"),           0,4); grid.add(cbJour,    1,4);
        grid.add(new Label("Heure début :"),    0,5); grid.add(new HBox(4,new Label("h"),spH,new Label("min"),spM),1,5);
        grid.add(new Label("Durée (min) :"),    0,6); grid.add(spD,       1,6);
        grid.add(new Label("Type :"),           0,7); grid.add(cbType,    1,7);

        Label msgEDT = new Label(""); msgEDT.setStyle("-fx-font-size:12;"); msgEDT.setWrapText(true);

        Button btnAjout = new Button("✅ Ajouter le créneau");
        btnAjout.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-padding:8 20;-fx-font-weight:bold;");
        btnAjout.setOnAction(e -> {
            if (cbClasse.getValue()==null||tfMatiere.getText().isEmpty()||cbEns.getValue()==null||cbSalle.getValue()==null) {
                msgEDT.setText("⚠️ Remplissez tous les champs obligatoires."); msgEDT.setStyle("-fx-text-fill:#e67e22;-fx-font-size:12;"); return;
            }
            int jourIdx = cbJour.getItems().indexOf(cbJour.getValue())+1;
            LocalTime hDebut = LocalTime.of(spH.getValue(), spM.getValue());
            int duree = spD.getValue(); int salleId = cbSalle.getValue().getId();
            String nomEns = cbEns.getValue().getNomComplet();

            if (edtDAO.salleOccupee(salleId,jourIdx,hDebut,duree,-1)) {
                msgEDT.setText("❌ Salle "+cbSalle.getValue().getNumero()+" déjà occupée ce créneau."); msgEDT.setStyle("-fx-text-fill:#e74c3c;-fx-font-size:12;"); return;
            }
            if (edtDAO.enseignantOccupe(nomEns,jourIdx,hDebut,duree,-1)) {
                msgEDT.setText("❌ "+nomEns+" a déjà un cours ce créneau."); msgEDT.setStyle("-fx-text-fill:#e74c3c;-fx-font-size:12;"); return;
            }
            EmploiDuTemps edt = new EmploiDuTemps(0, cbClasse.getValue(), tfMatiere.getText().trim(), nomEns, salleId, jourIdx, hDebut, duree, cbType.getValue());
            edtDAO.ajouter(edt);

            // Créer aussi un cours ponctuel pour la semaine courante
            // Le créneau EDT devient visible dans l'EDT ET dans la liste des cours de l'enseignant
            // On utilise le prochain jour correspondant (lundi de la semaine courante + jourIdx-1)
            java.time.LocalDate lundi = java.time.LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            java.time.LocalDate jourDate = lundi.plusDays(jourIdx - 1);
            java.time.LocalDateTime debutCours = jourDate.atTime(hDebut);
            models.Cours coursSync = new models.Cours(0,
                tfMatiere.getText().trim(), nomEns, cbClasse.getValue(), "",
                debutCours, duree, salleId);
            coursDAO.ajouter(coursSync);

            if (cbClasseFiltre.getValue()!=null && cbClasseFiltre.getValue().equals(cbClasse.getValue())) {
				items.setAll(edtDAO.obtenirParClasse(cbClasse.getValue()));
			}
            rechargerClasses(cbClasse); rechargerClasses(cbClasseFiltre);
            msgEDT.setText("✅ Créneau ajouté dans l'EDT, le calendrier et la liste des cours de " + nomEns + "."); msgEDT.setStyle("-fx-text-fill:#27ae60;-fx-font-size:12;");
        });

        panel.getChildren().addAll(titre, filtreBox, table, lblF, grid, btnAjout, msgEDT);
        return panel;
    }


    // Helpers
    private void rechargerClasses(ComboBox<String> cb) {
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
}
