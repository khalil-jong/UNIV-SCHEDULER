package ui;

import java.util.Optional;

import dao.BatimentDAO;
import dao.SalleDAO;
import dao.UtilisateurDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Batiment;
import models.Salle;
import models.Utilisateur;

public class AdminPanel {

    private Utilisateur    utilisateur;
    private UnivSchedulerApp app;
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private SalleDAO       salleDAO       = new SalleDAO();
    private BatimentDAO    batimentDAO    = new BatimentDAO();

    public AdminPanel(Utilisateur utilisateur, UnivSchedulerApp app) {
        this.utilisateur = utilisateur;
        this.app = app;
    }

    public BorderPane createPanel() {
        BorderPane root = new BorderPane();
        root.setTop(creerTopBar());
        root.setLeft(creerMenu(root));
        root.setCenter(new DashboardPanel().createPanel());
        return root;
    }

    private HBox creerTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color: #2c3e50;");
        bar.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("UNIV-SCHEDULER  |  Administrateur");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label userLabel = new Label("👤 " + utilisateur.getNomComplet());
        userLabel.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 13;");
        Button btnDeco = new Button("Déconnexion");
        btnDeco.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnDeco.setOnAction(e -> app.afficherLogin());
        bar.getChildren().addAll(titre, spacer, userLabel, new Label("   "), btnDeco);
        return bar;
    }

    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(3);
        menu.setPadding(new Insets(12));
        menu.setPrefWidth(215);
        menu.setStyle("-fx-background-color: #34495e;");

        ajouterTitreMenu(menu, "ADMINISTRATION");
        ajouterBouton(menu, "📊 Tableau de bord",           root, () -> new DashboardPanel().createPanel());
        ajouterBouton(menu, "👥 Gestion des utilisateurs",  root, () -> creerGestionUtilisateurs());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "INFRASTRUCTURE");
        ajouterBouton(menu, "🏗 Bâtiments & Salles",        root, () -> creerGestionInfra());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "NOTIFICATIONS");
        ajouterBouton(menu, "🔔 Alertes & Notifications",   root, () -> new AlertesAdminPanel(utilisateur).createPanel());

        return menu;
    }

    // ════════════════════════════════════════════════════════════
    //  Gestion des utilisateurs
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerGestionUtilisateurs() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        Label titre = new Label("👥 Gestion des Utilisateurs");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        TableView<Utilisateur> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<Utilisateur,String> colNom = new TableColumn<>("Nom complet");
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomComplet())); colNom.setPrefWidth(160);
        TableColumn<Utilisateur,String> colLogin = new TableColumn<>("Login");
        colLogin.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLogin())); colLogin.setPrefWidth(120);
        TableColumn<Utilisateur,String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole())); colRole.setPrefWidth(120);
        table.getColumns().addAll(colNom, colLogin, colRole);
        table.setItems(FXCollections.observableArrayList(utilisateurDAO.obtenirTous()));

        // Formulaire
        Label lblForm = new Label("➕ Ajouter un compte :");
        lblForm.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.setStyle("-fx-border-color: #ddd; -fx-background-color: #fafafa; -fx-border-radius: 4;");

        TextField     tfNom    = new TextField(); tfNom.setPromptText("Nom");       tfNom.setPrefWidth(160);
        TextField     tfPrenom = new TextField(); tfPrenom.setPromptText("Prénom"); tfPrenom.setPrefWidth(160);
        TextField     tfLogin  = new TextField(); tfLogin.setPromptText("Login");   tfLogin.setPrefWidth(160);
        PasswordField pfMdp    = new PasswordField(); pfMdp.setPromptText("Mot de passe"); pfMdp.setPrefWidth(160);
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("ADMIN","GESTIONNAIRE","ENSEIGNANT","ETUDIANT"); cbRole.setPromptText("Rôle");

        grid.add(new Label("Nom :"),        0,0); grid.add(tfNom,    1,0);
        grid.add(new Label("Prénom :"),     2,0); grid.add(tfPrenom, 3,0);
        grid.add(new Label("Login :"),      0,1); grid.add(tfLogin,  1,1);
        grid.add(new Label("Mot de passe :"),2,1);grid.add(pfMdp,    3,1);
        grid.add(new Label("Rôle :"),       0,2); grid.add(cbRole,   1,2);

        Label msgU = new Label(""); msgU.setStyle("-fx-font-size: 12;");

        Button btnAjouter = new Button("➕ Ajouter");
        btnAjouter.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 15;");
        btnAjouter.setOnAction(e -> {
            if (tfNom.getText().isEmpty()||tfPrenom.getText().isEmpty()||tfLogin.getText().isEmpty()||pfMdp.getText().isEmpty()||cbRole.getValue()==null) {
                msgU.setText("⚠️ Remplissez tous les champs."); msgU.setStyle("-fx-text-fill:#e67e22;"); return; }
            if (utilisateurDAO.loginExiste(tfLogin.getText().trim())) {
                msgU.setText("❌ Ce login est déjà utilisé."); msgU.setStyle("-fx-text-fill:#e74c3c;"); return; }
            try {
                Utilisateur u = new Utilisateur();
                u.setNom(tfNom.getText().trim()); u.setPrenom(tfPrenom.getText().trim());
                u.setLogin(tfLogin.getText().trim()); u.setMotDePasse(pfMdp.getText().trim());
                u.setRole(cbRole.getValue());
                utilisateurDAO.ajouter(u);
                table.setItems(FXCollections.observableArrayList(utilisateurDAO.obtenirTous()));
                tfNom.clear(); tfPrenom.clear(); tfLogin.clear(); pfMdp.clear(); cbRole.setValue(null);
                msgU.setText("✅ Utilisateur ajouté."); msgU.setStyle("-fx-text-fill:#27ae60;");
            } catch (RuntimeException ex) { msgU.setText("❌ "+ex.getMessage()); }
        });

        Button btnSupprimer = new Button("🗑 Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 15;");
        btnSupprimer.setOnAction(e -> {
            Utilisateur sel = table.getSelectionModel().getSelectedItem();
            if (sel==null) { msgU.setText("⚠️ Sélectionnez un utilisateur."); return; }
            if (sel.getLogin().equals(utilisateur.getLogin())) { msgU.setText("❌ Vous ne pouvez pas supprimer votre propre compte."); return; }
            Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer "+sel.getNomComplet()+" ?", ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if (res.isPresent()&&res.get()==ButtonType.OK) {
                utilisateurDAO.supprimer(sel.getId());
                table.setItems(FXCollections.observableArrayList(utilisateurDAO.obtenirTous()));
                msgU.setText("✅ Utilisateur supprimé."); msgU.setStyle("-fx-text-fill:#27ae60;");
            }
        });

        panel.getChildren().addAll(titre, table, new HBox(10, btnAjouter, btnSupprimer), lblForm, grid, msgU);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  Gestion Bâtiments & Salles (infrastructure)
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerGestionInfra() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab tabBat   = new Tab("🏗 Bâtiments", creerOngletBatiments());
        Tab tabSalle = new Tab("🏫 Salles",    creerOngletSalles());
        tabs.getTabs().addAll(tabBat, tabSalle);
        VBox wrapper = new VBox(tabs); wrapper.setPadding(new Insets(12));
        ScrollPane scroll = new ScrollPane(wrapper); scroll.setFitToWidth(true);
        return scroll;
    }

    private VBox creerOngletBatiments() {
        VBox panel = new VBox(12); panel.setPadding(new Insets(16));

        TableView<Batiment> table = new TableView<>(); table.setPrefHeight(240);
        TableColumn<Batiment,String>  cNom = new TableColumn<>("Nom");   cNom.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getNom()));          cNom.setPrefWidth(140);
        TableColumn<Batiment,String>  cLoc = new TableColumn<>("Localisation"); cLoc.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getLocalisation())); cLoc.setPrefWidth(180);
        TableColumn<Batiment,Integer> cEtg = new TableColumn<>("Étages"); cEtg.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().getNombreEtages())); cEtg.setPrefWidth(70);
        table.getColumns().addAll(cNom,cLoc,cEtg);
        table.setItems(FXCollections.observableArrayList(batimentDAO.obtenirTous()));

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");
        TextField tfNom = new TextField(); tfNom.setPromptText("Ex: Bâtiment A");    tfNom.setPrefWidth(200);
        TextField tfLoc = new TextField(); tfLoc.setPromptText("Ex: Campus principal"); tfLoc.setPrefWidth(200);
        Spinner<Integer> spEtg = new Spinner<>(0,20,2); spEtg.setPrefWidth(90);
        grid.add(new Label("Nom :"),         0,0); grid.add(tfNom,  1,0);
        grid.add(new Label("Localisation :"),0,1); grid.add(tfLoc,  1,1);
        grid.add(new Label("Étages :"),      0,2); grid.add(spEtg,  1,2);

        Label msg = new Label(""); msg.setStyle("-fx-font-size:12;");

        Button btnAjout = new Button("➕ Ajouter");
        btnAjout.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-padding:8 16;");
        btnAjout.setOnAction(e -> {
            if (tfNom.getText().isEmpty()) { msg.setText("⚠️ Le nom est obligatoire."); return; }
            Batiment b = new Batiment(0, tfNom.getText().trim(), tfLoc.getText().trim(), spEtg.getValue());
            try { batimentDAO.ajouter(b); table.setItems(FXCollections.observableArrayList(batimentDAO.obtenirTous()));
                tfNom.clear(); tfLoc.clear(); msg.setText("✅ Bâtiment ajouté."); msg.setStyle("-fx-text-fill:#27ae60;");
            } catch (Exception ex) { msg.setText("❌ "+ex.getMessage()); }
        });

        Button btnSuppr = new Button("🗑 Supprimer");
        btnSuppr.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-padding:8 16;");
        btnSuppr.setOnAction(e -> {
            Batiment sel = table.getSelectionModel().getSelectedItem();
            if (sel==null) { msg.setText("⚠️ Sélectionnez un bâtiment."); return; }
            Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION,"Supprimer "+sel.getNom()+" ?",ButtonType.OK,ButtonType.CANCEL).showAndWait();
            if (r.isPresent()&&r.get()==ButtonType.OK) { batimentDAO.supprimer(sel.getId()); table.setItems(FXCollections.observableArrayList(batimentDAO.obtenirTous())); msg.setText("✅ Supprimé."); }
        });

        panel.getChildren().addAll(new Label("Ajouter / Gérer les bâtiments :"), grid, new HBox(10,btnAjout,btnSuppr), msg, table);
        return panel;
    }

    private VBox creerOngletSalles() {
        VBox panel = new VBox(12); panel.setPadding(new Insets(16));

        final Salle[] enCours = {null};

        TableView<Salle> table = new TableView<>(); table.setPrefHeight(240);
        TableColumn<Salle,String>  cNum  = new TableColumn<>("Numéro");   cNum.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getNumero()));      cNum.setPrefWidth(80);
        TableColumn<Salle,String>  cBat  = new TableColumn<>("Bâtiment"); cBat.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getBatiment()));   cBat.setPrefWidth(120);
        TableColumn<Salle,Integer> cCap  = new TableColumn<>("Capacité"); cCap.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().getCapacite())); cCap.setPrefWidth(70);
        TableColumn<Salle,String>  cType = new TableColumn<>("Type");     cType.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getType()));      cType.setPrefWidth(65);
        TableColumn<Salle,String>  cEq   = new TableColumn<>("Équipements"); cEq.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getEquipementsStr())); cEq.setPrefWidth(160);
        table.getColumns().addAll(cNum,cBat,cCap,cType,cEq);
        table.setItems(FXCollections.observableArrayList(salleDAO.obtenirTous()));

        Label lblMode = new Label("Mode : ➕ Ajout"); lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#3498db;-fx-font-weight:bold;");

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");
        TextField tfNum  = new TextField(); tfNum.setPromptText("Ex: A101");          tfNum.setPrefWidth(160);
        TextField tfBat  = new TextField(); tfBat.setPromptText("Ex: Bâtiment A");    tfBat.setPrefWidth(160);
        TextField tfEtg  = new TextField(); tfEtg.setPromptText("Ex: 1er étage");     tfEtg.setPrefWidth(160);
        TextField tfCap  = new TextField(); tfCap.setPromptText("Ex: 50");            tfCap.setPrefWidth(80);
        ComboBox<String> cbType = new ComboBox<>(); cbType.getItems().addAll("TD","TP","Amphi"); cbType.setPromptText("Type");
        CheckBox chkV = new CheckBox("📽 Vidéoprojecteur");
        CheckBox chkT = new CheckBox("🖥 Tableau interactif");
        CheckBox chkC = new CheckBox("❄ Climatisation");
        grid.add(new Label("Numéro :"),    0,0); grid.add(tfNum,  1,0); grid.add(new Label("Bâtiment :"),2,0); grid.add(tfBat,3,0);
        grid.add(new Label("Étage :"),     0,1); grid.add(tfEtg,  1,1); grid.add(new Label("Capacité :"),2,1); grid.add(tfCap,3,1);
        grid.add(new Label("Type :"),      0,2); grid.add(cbType, 1,2);
        grid.add(new Label("Équipements :"),0,3); grid.add(new HBox(14,chkV,chkT,chkC),1,3,3,1);

        table.getSelectionModel().selectedItemProperty().addListener((obs,old,sel) -> {
            if (sel==null) {
				return;
			} enCours[0]=sel;
            tfNum.setText(sel.getNumero()); tfBat.setText(sel.getBatiment());
            tfEtg.setText(sel.getEtage()); tfCap.setText(String.valueOf(sel.getCapacite()));
            cbType.setValue(sel.getType());
            chkV.setSelected(sel.isVideoprojecteur()); chkT.setSelected(sel.isTableauInteractif()); chkC.setSelected(sel.isClimatisation());
            lblMode.setText("Mode : ✏️ Modification de "+sel.getNumero()); lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#e67e22;-fx-font-weight:bold;");
        });

        Label msg = new Label(""); msg.setStyle("-fx-font-size:12;"); msg.setWrapText(true);

        Button btnAjout  = new Button("➕ Ajouter");
        btnAjout.setStyle("-fx-background-color:#3498db;-fx-text-fill:white;-fx-padding:8 14;");
        btnAjout.setOnAction(e -> {
            int cap; try { cap=Integer.parseInt(tfCap.getText().trim()); } catch(NumberFormatException ex){ msg.setText("❌ Capacité invalide."); return; }
            if (tfNum.getText().isEmpty()||tfBat.getText().isEmpty()||cbType.getValue()==null) { msg.setText("⚠️ Champs obligatoires manquants."); return; }
            Salle s=new Salle(); s.setNumero(tfNum.getText().trim()); s.setBatiment(tfBat.getText().trim());
            s.setEtage(tfEtg.getText().trim()); s.setCapacite(cap); s.setType(cbType.getValue());
            s.setVideoprojecteur(chkV.isSelected()); s.setTableauInteractif(chkT.isSelected()); s.setClimatisation(chkC.isSelected());
            try { salleDAO.ajouter(s); table.setItems(FXCollections.observableArrayList(salleDAO.obtenirTous()));
                viderSalle(tfNum,tfBat,tfEtg,tfCap,cbType,chkV,chkT,chkC); enCours[0]=null;
                msg.setText("✅ Salle ajoutée."); msg.setStyle("-fx-text-fill:#27ae60;");
            } catch(Exception ex) { msg.setText("❌ "+ex.getMessage()); }
        });

        Button btnModif  = new Button("✏️ Modifier");
        btnModif.setStyle("-fx-background-color:#e67e22;-fx-text-fill:white;-fx-padding:8 14;");
        btnModif.setOnAction(e -> {
            if (enCours[0]==null) { msg.setText("⚠️ Cliquez d'abord sur une salle."); return; }
            int cap; try { cap=Integer.parseInt(tfCap.getText().trim()); } catch(NumberFormatException ex){ msg.setText("❌ Capacité invalide."); return; }
            Salle s=enCours[0]; s.setNumero(tfNum.getText().trim()); s.setBatiment(tfBat.getText().trim());
            s.setEtage(tfEtg.getText().trim()); s.setCapacite(cap); s.setType(cbType.getValue());
            s.setVideoprojecteur(chkV.isSelected()); s.setTableauInteractif(chkT.isSelected()); s.setClimatisation(chkC.isSelected());
            try { salleDAO.modifier(s); table.setItems(FXCollections.observableArrayList(salleDAO.obtenirTous()));
                viderSalle(tfNum,tfBat,tfEtg,tfCap,cbType,chkV,chkT,chkC); enCours[0]=null;
                lblMode.setText("Mode : ➕ Ajout"); lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#3498db;-fx-font-weight:bold;");
                msg.setText("✅ Salle modifiée."); msg.setStyle("-fx-text-fill:#27ae60;");
            } catch(Exception ex) { msg.setText("❌ "+ex.getMessage()); }
        });

        Button btnSuppr  = new Button("🗑 Supprimer");
        btnSuppr.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-padding:8 14;");
        btnSuppr.setOnAction(e -> {
            Salle sel=table.getSelectionModel().getSelectedItem();
            if (sel==null) { msg.setText("⚠️ Sélectionnez une salle."); return; }
            Optional<ButtonType> r=new Alert(Alert.AlertType.CONFIRMATION,"Supprimer "+sel.getNumero()+" ?",ButtonType.OK,ButtonType.CANCEL).showAndWait();
            if (r.isPresent()&&r.get()==ButtonType.OK) { salleDAO.supprimer(sel.getId()); table.setItems(FXCollections.observableArrayList(salleDAO.obtenirTous())); enCours[0]=null; msg.setText("✅ Salle supprimée."); msg.setStyle("-fx-text-fill:#27ae60;"); }
        });

        panel.getChildren().addAll(table, lblMode, new Label("Ajouter / Modifier :"), grid, new HBox(10,btnAjout,btnModif,btnSuppr), msg);
        return panel;
    }

    // ── Helpers ──
    private void viderSalle(TextField n,TextField b,TextField e,TextField c,ComboBox<String> t,CheckBox v,CheckBox ti,CheckBox cl){
        n.clear();b.clear();e.clear();c.clear();t.setValue(null);v.setSelected(false);ti.setSelected(false);cl.setSelected(false);
    }

    private void ajouterTitreMenu(VBox menu, String titre) {
        Label lbl = new Label(titre);
        lbl.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10; -fx-font-weight: bold; -fx-padding: 10 5 2 5;");
        menu.getChildren().add(lbl);
    }

    private void ajouterBouton(VBox menu, String label, BorderPane root,
                                java.util.function.Supplier<javafx.scene.Parent> panneau) {
        Button btn = new Button(label);
        btn.setPrefWidth(190); btn.setPrefHeight(38);
        String sN = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;";
        String sH = "-fx-background-color: #2c3e50;    -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;";
        btn.setStyle(sN);
        btn.setOnMouseEntered(e -> btn.setStyle(sH));
        btn.setOnMouseExited(e  -> btn.setStyle(sN));
        btn.setOnAction(e -> root.setCenter(panneau.get()));
        menu.getChildren().add(btn);
    }
}
