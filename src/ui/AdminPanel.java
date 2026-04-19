package ui;

import java.util.List;
import java.util.Optional;

import dao.BatimentDAO;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
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

    private Utilisateur      utilisateur;
    private UnivSchedulerApp app;
    private UtilisateurDAO   utilisateurDAO = new UtilisateurDAO();
    private SalleDAO         salleDAO       = new SalleDAO();
    private BatimentDAO      batimentDAO    = new BatimentDAO();

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

    // ── Top bar ──────────────────────────────────────────────────
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

    // ── Menu ─────────────────────────────────────────────────────
    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(3);
        menu.setPadding(new Insets(12));
        menu.setPrefWidth(220);
        menu.setStyle("-fx-background-color: #34495e;");

        ajouterTitreMenu(menu, "ADMINISTRATION");
        ajouterBouton(menu, "📊 Tableau de bord",          root, () -> new DashboardPanel().createPanel());
        ajouterBouton(menu, "👤 Administrateurs",           root, () -> creerGestionParRole("ADMIN"));
        ajouterBouton(menu, "🧑‍💼 Gestionnaires",           root, () -> creerGestionParRole("GESTIONNAIRE"));
        ajouterBouton(menu, "🧑‍🏫 Enseignants",             root, () -> creerGestionParRole("ENSEIGNANT"));
        ajouterBouton(menu, "🎓 Étudiants",                root, () -> creerGestionParRole("ETUDIANT"));

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "INFRASTRUCTURE");
        ajouterBouton(menu, "🏗 Bâtiments",               root, () -> creerGestionBatiments());
        ajouterBouton(menu, "🏫 Salles",                   root, () -> creerGestionSalles());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "NOTIFICATIONS");
        ajouterBouton(menu, "🔔 Alertes & Notifications",  root, () -> new AlertesAdminPanel(utilisateur).createPanel());
        ajouterBouton(menu, "📧 Envoi / Réception email",  root, () -> creerGestionEmail());

        return menu;
    }

    // ════════════════════════════════════════════════════════════
    //  GESTION DES UTILISATEURS — par rôle
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerGestionParRole(String role) {
        String titreRole = switch (role) {
            case "ADMIN"        -> "👤 Administrateurs";
            case "GESTIONNAIRE" -> "🧑‍💼 Gestionnaires";
            case "ENSEIGNANT"   -> "🧑‍🏫 Enseignants";
            case "ETUDIANT"     -> "🎓 Étudiants";
            default             -> role;
        };

        VBox panel = new VBox(14);
        panel.setPadding(new Insets(20));

        Label titre = new Label(titreRole);
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        // Tableau
        ObservableList<Utilisateur> items =
            FXCollections.observableArrayList(utilisateurDAO.obtenirParRole(role));
        TableView<Utilisateur> table = new TableView<>(items);
        table.setPrefHeight(280);
        table.setPlaceholder(new Label("Aucun " + role.toLowerCase() + " enregistré."));

        TableColumn<Utilisateur,String> cNom   = new TableColumn<>("Nom complet");
        cNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomComplet())); cNom.setPrefWidth(170);
        TableColumn<Utilisateur,String> cLogin = new TableColumn<>("Login");
        cLogin.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLogin())); cLogin.setPrefWidth(120);
        table.getColumns().addAll(cNom, cLogin);
        if (role.equals("ENSEIGNANT")) {
            TableColumn<Utilisateur,String> cMat = new TableColumn<>("Matière(s)");
            cMat.setCellValueFactory(cv -> new SimpleStringProperty(cv.getValue().getMatiere())); cMat.setPrefWidth(200);
            table.getColumns().add(cMat);
        }

        // ── Formulaire Ajouter / Modifier ──
        final Utilisateur[] enCours = {null};

        Label lblMode = new Label("Mode : ➕ Ajout");
        lblMode.setStyle("-fx-font-size: 12; -fx-text-fill: #3498db; -fx-font-weight: bold;");

        Label lblForm = new Label("Ajouter / Modifier un " + role.toLowerCase() + " :");
        lblForm.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        TextField     tfNom    = new TextField(); tfNom.setPromptText("Nom");       tfNom.setPrefWidth(160);
        TextField     tfPrenom = new TextField(); tfPrenom.setPromptText("Prénom"); tfPrenom.setPrefWidth(160);
        TextField     tfLogin  = new TextField(); tfLogin.setPromptText("Login");   tfLogin.setPrefWidth(160);
        PasswordField pfMdp    = new PasswordField();
        pfMdp.setPromptText("Mot de passe (laisser vide = inchangé)"); pfMdp.setPrefWidth(220);

        // Champ matière — affiché uniquement pour les enseignants
        TextField tfMatiere = new TextField();
        tfMatiere.setPromptText("Ex: Algorithmique, Réseaux...");
        tfMatiere.setPrefWidth(300);
        boolean isEnseignant = role.equals("ENSEIGNANT");

        grid.add(new Label("Nom :"),          0, 0); grid.add(tfNom,    1, 0);
        grid.add(new Label("Prénom :"),       2, 0); grid.add(tfPrenom, 3, 0);
        grid.add(new Label("Login :"),        0, 1); grid.add(tfLogin,  1, 1);
        grid.add(new Label("Mot de passe :"), 0, 2); grid.add(pfMdp,    1, 2, 3, 1);
        if (isEnseignant) {
            grid.add(new Label("Matière(s) :"), 0, 3); grid.add(tfMatiere, 1, 3, 3, 1);
        }

        Label msgU = new Label(""); msgU.setStyle("-fx-font-size: 12;"); msgU.setWrapText(true);

        // Clic sur une ligne → pré-remplir pour modification
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) {
				return;
			}
            enCours[0] = sel;
            tfNom.setText(sel.getNom()); tfPrenom.setText(sel.getPrenom());
            tfLogin.setText(sel.getLogin()); pfMdp.clear();
            tfMatiere.setText(sel.getMatiere() != null ? sel.getMatiere() : "");
            lblMode.setText("Mode : ✏️ Modification de " + sel.getNomComplet());
            lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#e67e22;-fx-font-weight:bold;");
        });

        Button btnSave = new Button("💾 Enregistrer");
        btnSave.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-padding:8 16;-fx-font-weight:bold;");
        btnSave.setOnAction(e -> {
            if (tfNom.getText().isEmpty() || tfPrenom.getText().isEmpty() || tfLogin.getText().isEmpty()) {
                msgU.setText("⚠️ Nom, prénom et login sont obligatoires."); msgU.setStyle("-fx-text-fill:#e67e22;"); return;
            }
            try {
                if (enCours[0] != null) {
                    // Modification
                    enCours[0].setNom(tfNom.getText().trim());
                    enCours[0].setPrenom(tfPrenom.getText().trim());
                    enCours[0].setLogin(tfLogin.getText().trim());
                    enCours[0].setMotDePasse(pfMdp.getText().trim()); // vide = inchangé
                    if (isEnseignant) {
						enCours[0].setMatiere(tfMatiere.getText().trim());
					}
                    utilisateurDAO.modifier(enCours[0]);
                    msgU.setText("✅ Utilisateur modifié."); msgU.setStyle("-fx-text-fill:#27ae60;");
                } else {
                    // Ajout
                    if (pfMdp.getText().isEmpty()) { msgU.setText("⚠️ Le mot de passe est obligatoire pour un nouvel utilisateur."); return; }
                    if (utilisateurDAO.loginExiste(tfLogin.getText().trim())) { msgU.setText("❌ Ce login est déjà utilisé."); msgU.setStyle("-fx-text-fill:#e74c3c;"); return; }
                    Utilisateur u = new Utilisateur();
                    u.setNom(tfNom.getText().trim()); u.setPrenom(tfPrenom.getText().trim());
                    u.setLogin(tfLogin.getText().trim()); u.setMotDePasse(pfMdp.getText().trim());
                    u.setRole(role);
                    if (isEnseignant) {
						u.setMatiere(tfMatiere.getText().trim());
					}
                    utilisateurDAO.ajouter(u);
                    msgU.setText("✅ Utilisateur ajouté."); msgU.setStyle("-fx-text-fill:#27ae60;");
                }
                items.setAll(utilisateurDAO.obtenirParRole(role));
                viderFormUser(tfNom, tfPrenom, tfLogin, pfMdp);
                tfMatiere.clear();
                enCours[0] = null; table.getSelectionModel().clearSelection();
                lblMode.setText("Mode : ➕ Ajout"); lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#3498db;-fx-font-weight:bold;");
            } catch (Exception ex) { msgU.setText("❌ " + ex.getMessage()); msgU.setStyle("-fx-text-fill:#e74c3c;"); }
        });

        Button btnAnnuler = new Button("✖ Annuler");
        btnAnnuler.setStyle("-fx-background-color:#95a5a6;-fx-text-fill:white;-fx-padding:8 14;");
        btnAnnuler.setOnAction(e -> {
            viderFormUser(tfNom, tfPrenom, tfLogin, pfMdp); enCours[0] = null;
            tfMatiere.clear();
            table.getSelectionModel().clearSelection();
            lblMode.setText("Mode : ➕ Ajout"); lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#3498db;-fx-font-weight:bold;");
            msgU.setText("");
        });

        Button btnSupprimer = new Button("🗑 Supprimer");
        btnSupprimer.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-padding:8 14;");
        btnSupprimer.setOnAction(e -> {
            Utilisateur sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgU.setText("⚠️ Sélectionnez un utilisateur dans le tableau."); return; }
            if (sel.getLogin().equals(utilisateur.getLogin())) { msgU.setText("❌ Vous ne pouvez pas supprimer votre propre compte."); return; }
            Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + sel.getNomComplet() + " ?", ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if (r.isPresent() && r.get() == ButtonType.OK) {
                utilisateurDAO.supprimer(sel.getId());
                items.setAll(utilisateurDAO.obtenirParRole(role));
                viderFormUser(tfNom, tfPrenom, tfLogin, pfMdp); enCours[0] = null;
                msgU.setText("✅ Utilisateur supprimé."); msgU.setStyle("-fx-text-fill:#27ae60;");
            }
        });

        panel.getChildren().addAll(titre, table, lblMode, lblForm, grid,
            new HBox(10, btnSave, btnAnnuler, btnSupprimer), msgU);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  GESTION BÂTIMENTS
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerGestionBatiments() {
        VBox panel = new VBox(14); panel.setPadding(new Insets(18));
        Label titre = new Label("🏗 Gestion des Bâtiments");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        ObservableList<Batiment> items = FXCollections.observableArrayList(batimentDAO.obtenirTous());
        TableView<Batiment> table = new TableView<>(items);
        table.setPrefHeight(250);
        table.setPlaceholder(new Label("Aucun bâtiment enregistré."));

        TableColumn<Batiment,String>  cNom = new TableColumn<>("Nom");
        cNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom())); cNom.setPrefWidth(140);
        TableColumn<Batiment,String>  cLoc = new TableColumn<>("Localisation");
        cLoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocalisation())); cLoc.setPrefWidth(200);
        TableColumn<Batiment,Integer> cEtg = new TableColumn<>("Nb étages");
        cEtg.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getNombreEtages())); cEtg.setPrefWidth(80);
        table.getColumns().addAll(cNom, cLoc, cEtg);

        // Formulaire
        final Batiment[] enCours = {null};
        Label lblMode = new Label("Mode : ➕ Ajout");
        lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#3498db;-fx-font-weight:bold;");

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");
        TextField tfNom = new TextField(); tfNom.setPromptText("Ex: UFR-SET");        tfNom.setPrefWidth(220);
        TextField tfLoc = new TextField(); tfLoc.setPromptText("Ex: Campus principal"); tfLoc.setPrefWidth(220);
        Spinner<Integer> spEtg = new Spinner<>(0, 30, 1); spEtg.setPrefWidth(90);
        spEtg.setEditable(true);
        grid.add(new Label("Nom du bâtiment :"), 0, 0); grid.add(tfNom,  1, 0);
        grid.add(new Label("Localisation :"),     0, 1); grid.add(tfLoc,  1, 1);
        grid.add(new Label("Nombre d'étages :"),  0, 2); grid.add(spEtg,  1, 2);
        Label noteEtg = new Label("(0 = rez-de-chaussée uniquement)");
        noteEtg.setStyle("-fx-font-size:11;-fx-text-fill:#aaa;");
        grid.add(noteEtg, 2, 2);

        Label msg = new Label(""); msg.setStyle("-fx-font-size:12;"); msg.setWrapText(true);

        // Clic → pré-remplir
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) {
				return;
			}
            enCours[0] = sel;
            tfNom.setText(sel.getNom()); tfLoc.setText(sel.getLocalisation());
            spEtg.getValueFactory().setValue(sel.getNombreEtages());
            lblMode.setText("Mode : ✏️ Modification de " + sel.getNom());
            lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#e67e22;-fx-font-weight:bold;");
        });

        Button btnSave = new Button("💾 Enregistrer");
        btnSave.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-padding:8 16;-fx-font-weight:bold;");
        btnSave.setOnAction(e -> {
            if (tfNom.getText().isEmpty()) { msg.setText("⚠️ Le nom est obligatoire."); return; }
            try {
                if (enCours[0] != null) {
                    enCours[0].setNom(tfNom.getText().trim());
                    enCours[0].setLocalisation(tfLoc.getText().trim());
                    enCours[0].setNombreEtages(spEtg.getValue());
                    batimentDAO.modifier(enCours[0]);
                    msg.setText("✅ Bâtiment modifié."); msg.setStyle("-fx-text-fill:#27ae60;");
                } else {
                    batimentDAO.ajouter(new Batiment(0, tfNom.getText().trim(), tfLoc.getText().trim(), spEtg.getValue()));
                    msg.setText("✅ Bâtiment ajouté."); msg.setStyle("-fx-text-fill:#27ae60;");
                }
                items.setAll(batimentDAO.obtenirTous());
                tfNom.clear(); tfLoc.clear(); enCours[0] = null; table.getSelectionModel().clearSelection();
                lblMode.setText("Mode : ➕ Ajout"); lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#3498db;-fx-font-weight:bold;");
            } catch (Exception ex) { msg.setText("❌ " + ex.getMessage()); msg.setStyle("-fx-text-fill:#e74c3c;"); }
        });

        Button btnSuppr = new Button("🗑 Supprimer");
        btnSuppr.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-padding:8 14;");
        btnSuppr.setOnAction(e -> {
            Batiment sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msg.setText("⚠️ Sélectionnez un bâtiment."); return; }
            long nbSalles = salleDAO.obtenirParBatiment(sel.getNom()).size();
            String warning = nbSalles > 0
                ? "\n\n⚠️ " + nbSalles + " salle(s) de ce bâtiment seront aussi supprimées."
                : "";
            Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + sel.getNom() + " ?" + warning, ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if (r.isPresent() && r.get() == ButtonType.OK) {
                batimentDAO.supprimer(sel.getId());
                items.setAll(batimentDAO.obtenirTous());
                enCours[0] = null; table.getSelectionModel().clearSelection();
                msg.setText("✅ Bâtiment" + (nbSalles > 0 ? " et ses " + nbSalles + " salle(s) supprimés." : " supprimé."));
                msg.setStyle("-fx-text-fill:#27ae60;");
            }
        });

        Button btnAnnuler = new Button("✖ Annuler");
        btnAnnuler.setStyle("-fx-background-color:#95a5a6;-fx-text-fill:white;-fx-padding:8 14;");
        btnAnnuler.setOnAction(e -> {
            tfNom.clear(); tfLoc.clear(); enCours[0] = null; table.getSelectionModel().clearSelection();
            lblMode.setText("Mode : ➕ Ajout"); lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#3498db;-fx-font-weight:bold;");
            msg.setText("");
        });

        panel.getChildren().addAll(titre, table, lblMode,
            new Label("Ajouter / Modifier un bâtiment :"), grid,
            new HBox(10, btnSave, btnAnnuler, btnSuppr), msg);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  GESTION SALLES
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerGestionSalles() {
        VBox panel = new VBox(14); panel.setPadding(new Insets(18));
        Label titre = new Label("🏫 Gestion des Salles");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        // ── Filtre par bâtiment ──
        HBox filtreBox = new HBox(10); filtreBox.setAlignment(Pos.CENTER_LEFT);
        Label lblFil = new Label("Bâtiment :");
        ComboBox<String> cbFiltreBat = new ComboBox<>();
        cbFiltreBat.getItems().add("Tous les bâtiments");
        cbFiltreBat.getItems().addAll(batimentDAO.obtenirNoms());
        cbFiltreBat.setValue("Tous les bâtiments");
        cbFiltreBat.setPrefWidth(220);
        filtreBox.getChildren().addAll(lblFil, cbFiltreBat);

        // Tableau
        ObservableList<Salle> items = FXCollections.observableArrayList(salleDAO.obtenirTous());
        TableView<Salle> table = new TableView<>(items);
        table.setPrefHeight(250);
        table.setPlaceholder(new Label("Aucune salle enregistrée."));

        TableColumn<Salle,String>  cNum  = new TableColumn<>("N°");
        cNum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumero())); cNum.setPrefWidth(55);
        TableColumn<Salle,String>  cBat  = new TableColumn<>("Bâtiment");
        cBat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBatiment())); cBat.setPrefWidth(130);
        TableColumn<Salle,String>  cEtg  = new TableColumn<>("Étage");
        cEtg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEtage())); cEtg.setPrefWidth(90);
        TableColumn<Salle,Integer> cCap  = new TableColumn<>("Cap.");
        cCap.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCapacite())); cCap.setPrefWidth(55);
        TableColumn<Salle,String>  cType = new TableColumn<>("Type");
        cType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType())); cType.setPrefWidth(60);
        TableColumn<Salle,String>  cEq   = new TableColumn<>("Équipements");
        cEq.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEquipementsStr())); cEq.setPrefWidth(160);
        table.getColumns().addAll(cNum, cBat, cEtg, cCap, cType, cEq);

        // Filtre bâtiment → recharger le tableau
        cbFiltreBat.setOnAction(e -> {
            String v = cbFiltreBat.getValue();
            if (v == null || v.equals("Tous les bâtiments")) {
				items.setAll(salleDAO.obtenirTous());
			} else {
				items.setAll(salleDAO.obtenirParBatiment(v));
			}
        });

        // ── Formulaire ──
        final Salle[] enCours = {null};
        Label lblMode = new Label("Mode : ➕ Ajout");
        lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#3498db;-fx-font-weight:bold;");

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        // Bâtiment → ComboBox des bâtiments existants
        ComboBox<String> cbBat = new ComboBox<>();
        cbBat.getItems().addAll(batimentDAO.obtenirNoms());
        cbBat.setPromptText("Sélectionner un bâtiment"); cbBat.setPrefWidth(220);

        // Numéro de salle (saisie libre mais dépend du bâtiment)
        TextField tfNum = new TextField(); tfNum.setPromptText("Ex: 1 (ou S01, Lab-3...)"); tfNum.setPrefWidth(160);
        Label lblNumInfo = new Label("");
        lblNumInfo.setStyle("-fx-font-size:11;-fx-text-fill:#7f8c8d;"); lblNumInfo.setWrapText(true);

        // Étage → ComboBox dynamique selon le nombre d'étages du bâtiment choisi
        ComboBox<String> cbEtage = new ComboBox<>();
        cbEtage.setPromptText("Sélectionner d'abord un bâtiment"); cbEtage.setPrefWidth(220);

        // Quand le bâtiment change → mettre à jour les étages + info numéros existants
        cbBat.setOnAction(e -> {
            cbEtage.getItems().clear();
            String nomBat = cbBat.getValue();
            if (nomBat == null) {
				return;
			}
            Batiment bat = batimentDAO.obtenirParNom(nomBat);
            if (bat == null) {
				return;
			}
            int nbEtages = bat.getNombreEtages();
            if (nbEtages == 0) {
                cbEtage.getItems().add("Rez-de-chaussée");
            } else {
                cbEtage.getItems().add("Rez-de-chaussée");
                for (int i = 1; i <= nbEtages; i++) {
                    cbEtage.getItems().add(i + (i == 1 ? "er étage" : "e étage"));
                }
            }
            cbEtage.setValue(cbEtage.getItems().get(0));
            // Afficher les numéros déjà pris dans ce bâtiment
            List<String> existants = salleDAO.obtenirNumerosPourBatiment(nomBat);
            lblNumInfo.setText(existants.isEmpty()
                ? "Aucune salle dans ce bâtiment pour l'instant."
                : "Numéros déjà utilisés : " + String.join(", ", existants));
        });

        TextField tfCap = new TextField(); tfCap.setPromptText("Ex: 50"); tfCap.setPrefWidth(80);
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("TD","TP","Amphi"); cbType.setPromptText("Type de salle"); cbType.setPrefWidth(130);
        CheckBox chkV = new CheckBox("📽 Vidéoprojecteur");
        CheckBox chkT = new CheckBox("🖥 Tableau interactif");
        CheckBox chkC = new CheckBox("❄ Climatisation");

        grid.add(new Label("Bâtiment :"),    0, 0); grid.add(cbBat,   1, 0, 2, 1);
        grid.add(new Label("N° salle :"),    0, 1); grid.add(tfNum,   1, 1); grid.add(lblNumInfo, 2, 1);
        grid.add(new Label("Étage :"),       0, 2); grid.add(cbEtage, 1, 2, 2, 1);
        grid.add(new Label("Capacité :"),    0, 3); grid.add(tfCap,   1, 3);
        grid.add(new Label("Type :"),        0, 4); grid.add(cbType,  1, 4);
        grid.add(new Label("Équipements :"), 0, 5); grid.add(new HBox(14, chkV, chkT, chkC), 1, 5, 2, 1);

        Label msg = new Label(""); msg.setStyle("-fx-font-size:12;"); msg.setWrapText(true);

        // Clic tableau → pré-remplir formulaire
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) {
				return;
			}
            enCours[0] = sel;
            // Déclencher d'abord la mise à jour des étages
            cbBat.setValue(sel.getBatiment());
            cbBat.fireEvent(new javafx.event.ActionEvent());
            tfNum.setText(sel.getNumero());
            cbEtage.setValue(sel.getEtage());
            tfCap.setText(String.valueOf(sel.getCapacite()));
            cbType.setValue(sel.getType());
            chkV.setSelected(sel.isVideoprojecteur());
            chkT.setSelected(sel.isTableauInteractif());
            chkC.setSelected(sel.isClimatisation());
            lblMode.setText("Mode : ✏️ Modification de " + sel.getNumero() + " — " + sel.getBatiment());
            lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#e67e22;-fx-font-weight:bold;");
        });

        Button btnSave = new Button("💾 Enregistrer");
        btnSave.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-padding:8 16;-fx-font-weight:bold;");
        btnSave.setOnAction(e -> {
            if (cbBat.getValue() == null || tfNum.getText().isEmpty()
                    || cbEtage.getValue() == null || cbType.getValue() == null) {
                msg.setText("⚠️ Bâtiment, numéro, étage et type sont obligatoires."); msg.setStyle("-fx-text-fill:#e67e22;"); return;
            }
            int cap; try { cap = Integer.parseInt(tfCap.getText().trim()); }
            catch (NumberFormatException ex) { msg.setText("❌ Capacité invalide."); return; }

            Salle s = enCours[0] != null ? enCours[0] : new Salle();
            s.setNumero(tfNum.getText().trim());
            s.setBatiment(cbBat.getValue());
            s.setEtage(cbEtage.getValue() != null ? cbEtage.getValue() : "Rez-de-chaussée");
            s.setCapacite(cap); s.setType(cbType.getValue());
            s.setVideoprojecteur(chkV.isSelected()); s.setTableauInteractif(chkT.isSelected()); s.setClimatisation(chkC.isSelected());

            try {
                if (enCours[0] != null) { salleDAO.modifier(s); msg.setText("✅ Salle modifiée."); }
                else                    { salleDAO.ajouter(s);   msg.setText("✅ Salle ajoutée."); }
                msg.setStyle("-fx-text-fill:#27ae60;");

                // Rafraîchir tableau selon filtre actif
                String fil = cbFiltreBat.getValue();
                items.setAll(fil == null || fil.equals("Tous les bâtiments")
                    ? salleDAO.obtenirTous() : salleDAO.obtenirParBatiment(fil));
                // Rafraîchir ComboBox filtre (si nouveau bâtiment)
                String curFil = cbFiltreBat.getValue();
                cbFiltreBat.getItems().clear();
                cbFiltreBat.getItems().add("Tous les bâtiments");
                cbFiltreBat.getItems().addAll(batimentDAO.obtenirNoms());
                cbFiltreBat.setValue(curFil);

                viderFormSalle(cbBat, tfNum, cbEtage, tfCap, cbType, chkV, chkT, chkC, lblNumInfo);
                enCours[0] = null; table.getSelectionModel().clearSelection();
                lblMode.setText("Mode : ➕ Ajout"); lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#3498db;-fx-font-weight:bold;");
            } catch (Exception ex) { msg.setText("❌ " + ex.getMessage()); msg.setStyle("-fx-text-fill:#e74c3c;"); }
        });

        Button btnSuppr = new Button("🗑 Supprimer");
        btnSuppr.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-padding:8 14;");
        btnSuppr.setOnAction(e -> {
            Salle sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msg.setText("⚠️ Sélectionnez une salle."); return; }
            Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la salle " + sel.getNumero() + " (" + sel.getBatiment() + ") ?",
                ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if (r.isPresent() && r.get() == ButtonType.OK) {
                salleDAO.supprimer(sel.getId());
                String fil = cbFiltreBat.getValue();
                items.setAll(fil == null || fil.equals("Tous les bâtiments")
                    ? salleDAO.obtenirTous() : salleDAO.obtenirParBatiment(fil));
                enCours[0] = null;
                msg.setText("✅ Salle supprimée."); msg.setStyle("-fx-text-fill:#27ae60;");
            }
        });

        Button btnAnnuler = new Button("✖ Annuler");
        btnAnnuler.setStyle("-fx-background-color:#95a5a6;-fx-text-fill:white;-fx-padding:8 14;");
        btnAnnuler.setOnAction(e -> {
            viderFormSalle(cbBat, tfNum, cbEtage, tfCap, cbType, chkV, chkT, chkC, lblNumInfo);
            enCours[0] = null; table.getSelectionModel().clearSelection();
            lblMode.setText("Mode : ➕ Ajout"); lblMode.setStyle("-fx-font-size:12;-fx-text-fill:#3498db;-fx-font-weight:bold;");
            msg.setText("");
        });

        panel.getChildren().addAll(titre, filtreBox, table, lblMode,
            new Label("Ajouter / Modifier une salle :"), grid,
            new HBox(10, btnSave, btnAnnuler, btnSuppr), msg);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  EMAIL
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerGestionEmail() {
        VBox panel = new VBox(20); panel.setPadding(new Insets(20));

        Label titre = new Label("📧 Envoi et Réception d'Emails");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        // ── Configuration SMTP ──
        VBox boxConfig = section("⚙️ Configuration SMTP (Gmail, Outlook, etc.)");
        Label noteConfig = new Label("Remplissez ces paramètres une seule fois. Pour Gmail, activez la validation en 2 étapes et créez un 'Mot de passe d'application'.");
        noteConfig.setStyle("-fx-font-size: 12; -fx-text-fill: #555;"); noteConfig.setWrapText(true);

        GridPane gSmtp = new GridPane(); gSmtp.setHgap(10); gSmtp.setVgap(8); gSmtp.setPadding(new Insets(10));
        TextField tfHost = new TextField("smtp.gmail.com"); tfHost.setPrefWidth(200);
        TextField tfPort = new TextField("587"); tfPort.setPrefWidth(80);
        TextField tfUser = new TextField(); tfUser.setPromptText("votre.email@gmail.com"); tfUser.setPrefWidth(250);
        PasswordField pfPass = new PasswordField(); pfPass.setPromptText("Mot de passe d'application"); pfPass.setPrefWidth(250);

        gSmtp.add(new Label("Serveur SMTP :"), 0, 0); gSmtp.add(tfHost, 1, 0);
        gSmtp.add(new Label("Port :"),          0, 1); gSmtp.add(tfPort, 1, 1);
        gSmtp.add(new Label("Email :"),         0, 2); gSmtp.add(tfUser, 1, 2);
        gSmtp.add(new Label("Mot de passe :"),  0, 3); gSmtp.add(pfPass, 1, 3);

        Button btnTest = new Button("🔌 Tester la connexion");
        btnTest.setStyle("-fx-background-color:#3498db;-fx-text-fill:white;-fx-padding:7 14;");
        Label msgSmtp = new Label(""); msgSmtp.setStyle("-fx-font-size:12;"); msgSmtp.setWrapText(true);

        btnTest.setOnAction(e -> {
            if (tfUser.getText().isEmpty() || pfPass.getText().isEmpty()) {
                msgSmtp.setText("⚠️ Remplissez l'email et le mot de passe."); return;
            }
            msgSmtp.setText("🔄 Test en cours...");
            new Thread(() -> {
                boolean ok = EmailService.testerConnexion(
                    tfHost.getText().trim(), tfPort.getText().trim(),
                    tfUser.getText().trim(), pfPass.getText());
                javafx.application.Platform.runLater(() -> {
                    if (ok) { msgSmtp.setText("✅ Connexion SMTP réussie."); msgSmtp.setStyle("-fx-text-fill:#27ae60;-fx-font-size:12;"); }
                    else    { msgSmtp.setText("❌ Échec — vérifiez les paramètres et votre mot de passe d'application."); msgSmtp.setStyle("-fx-text-fill:#e74c3c;-fx-font-size:12;"); }
                });
            }).start();
        });

        boxConfig.getChildren().addAll(noteConfig, gSmtp, btnTest, msgSmtp);

        // ── Envoi d'email ──
        VBox boxEnvoi = section("📤 Envoyer un email");
        TextField tfTo      = new TextField(); tfTo.setPromptText("destinataire@example.com"); tfTo.setPrefWidth(350);
        TextField tfSujet   = new TextField(); tfSujet.setPromptText("Objet du message"); tfSujet.setPrefWidth(350);
        TextArea  taCorps   = new TextArea();  taCorps.setPromptText("Corps du message..."); taCorps.setPrefHeight(120); taCorps.setWrapText(true);
        Label msgEnvoi = new Label(""); msgEnvoi.setStyle("-fx-font-size:12;"); msgEnvoi.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer");
        btnEnvoyer.setStyle("-fx-background-color:#8e44ad;-fx-text-fill:white;-fx-padding:8 20;-fx-font-weight:bold;");
        btnEnvoyer.setOnAction(e -> {
            if (tfTo.getText().isEmpty() || tfSujet.getText().isEmpty() || taCorps.getText().isEmpty()) {
                msgEnvoi.setText("⚠️ Destinataire, objet et corps sont obligatoires."); return;
            }
            if (tfUser.getText().isEmpty() || pfPass.getText().isEmpty()) {
                msgEnvoi.setText("⚠️ Configurez d'abord le compte SMTP ci-dessus."); return;
            }
            msgEnvoi.setText("🔄 Envoi en cours...");
            new Thread(() -> {
                boolean ok = EmailService.envoyerEmail(
                    tfHost.getText().trim(), tfPort.getText().trim(),
                    tfUser.getText().trim(), pfPass.getText(),
                    tfTo.getText().trim(), tfSujet.getText().trim(), taCorps.getText());
                javafx.application.Platform.runLater(() -> {
                    if (ok) {
                        msgEnvoi.setText("✅ Email envoyé à " + tfTo.getText()); msgEnvoi.setStyle("-fx-text-fill:#27ae60;-fx-font-size:12;");
                        tfTo.clear(); tfSujet.clear(); taCorps.clear();
                    } else {
                        msgEnvoi.setText("❌ Échec de l'envoi — vérifiez la configuration SMTP."); msgEnvoi.setStyle("-fx-text-fill:#e74c3c;-fx-font-size:12;");
                    }
                });
            }).start();
        });

        GridPane gEnvoi = new GridPane(); gEnvoi.setHgap(10); gEnvoi.setVgap(8); gEnvoi.setPadding(new Insets(10));
        gEnvoi.add(new Label("À :"),     0, 0); gEnvoi.add(tfTo,    1, 0);
        gEnvoi.add(new Label("Objet :"), 0, 1); gEnvoi.add(tfSujet, 1, 1);
        gEnvoi.add(new Label("Corps :"), 0, 2); gEnvoi.add(taCorps, 1, 2);

        boxEnvoi.getChildren().addAll(gEnvoi, btnEnvoyer, msgEnvoi);

        // ── Note IMAP ──
        VBox boxImap = section("📥 Réception d'emails (IMAP)");
        Label noteImap = new Label(
            "La réception d'emails nécessite la bibliothèque javax.mail.jar.\n" +
            "Assurez-vous qu'elle est dans votre dossier lib/ et ajoutée au Build Path.\n\n" +
            "Configuration IMAP Gmail : imap.gmail.com — Port 993 (SSL).\n" +
            "Les emails reçus via IMAP s'afficheront ici après configuration.");
        noteImap.setStyle("-fx-font-size: 12; -fx-text-fill: #555;"); noteImap.setWrapText(true);

        TextField tfImapHost = new TextField("imap.gmail.com"); tfImapHost.setPrefWidth(200);
        TextField tfImapPort = new TextField("993"); tfImapPort.setPrefWidth(80);
        Button btnLireMails = new Button("📥 Lire les emails reçus");
        btnLireMails.setStyle("-fx-background-color:#1abc9c;-fx-text-fill:white;-fx-padding:7 14;");
        TextArea taEmails = new TextArea(); taEmails.setEditable(false); taEmails.setPrefHeight(150); taEmails.setWrapText(true);
        taEmails.setPromptText("Les emails reçus s'afficheront ici...");
        Label msgImap = new Label(""); msgImap.setStyle("-fx-font-size:12;"); msgImap.setWrapText(true);

        btnLireMails.setOnAction(e -> {
            if (tfUser.getText().isEmpty() || pfPass.getText().isEmpty()) {
                msgImap.setText("⚠️ Configurez d'abord le compte SMTP/IMAP ci-dessus."); return;
            }
            msgImap.setText("🔄 Récupération des emails...");
            new Thread(() -> {
                String contenu = EmailService.lireEmails(
                    tfImapHost.getText().trim(), tfImapPort.getText().trim(),
                    tfUser.getText().trim(), pfPass.getText());
                javafx.application.Platform.runLater(() -> {
                    taEmails.setText(contenu);
                    msgImap.setText(contenu.isEmpty() ? "Aucun email trouvé." : "✅ Emails chargés.");
                    msgImap.setStyle("-fx-text-fill:#27ae60;-fx-font-size:12;");
                });
            }).start();
        });

        GridPane gImap = new GridPane(); gImap.setHgap(10); gImap.setVgap(8); gImap.setPadding(new Insets(10));
        gImap.add(new Label("Serveur IMAP :"), 0, 0); gImap.add(tfImapHost, 1, 0);
        gImap.add(new Label("Port :"),          0, 1); gImap.add(tfImapPort, 1, 1);

        boxImap.getChildren().addAll(noteImap, gImap, btnLireMails, taEmails, msgImap);

        panel.getChildren().addAll(titre, boxConfig, boxEnvoi, boxImap);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    // ── Helpers ──
    private VBox section(String titreSection) {
        VBox box = new VBox(8); box.setPadding(new Insets(14));
        box.setStyle("-fx-border-color:#bdc3c7;-fx-border-radius:6;-fx-background-color:white;");
        Label lbl = new Label(titreSection);
        lbl.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");
        box.getChildren().add(lbl);
        return box;
    }

    private void viderFormUser(TextField n, TextField p, TextField l, PasswordField m) {
        n.clear(); p.clear(); l.clear(); m.clear();
    }

    private void viderFormSalle(ComboBox<String> bat, TextField num, ComboBox<String> etg,
                                 TextField cap, ComboBox<String> type,
                                 CheckBox v, CheckBox t, CheckBox c, Label info) {
        bat.setValue(null); num.clear(); etg.getItems().clear();
        etg.setPromptText("Sélectionner d'abord un bâtiment");
        cap.clear(); type.setValue(null); v.setSelected(false); t.setSelected(false); c.setSelected(false);
        info.setText("");
    }

    private void ajouterTitreMenu(VBox menu, String titre) {
        Label lbl = new Label(titre);
        lbl.setStyle("-fx-text-fill:#95a5a6;-fx-font-size:10;-fx-font-weight:bold;-fx-padding:10 5 2 5;");
        menu.getChildren().add(lbl);
    }

    private void ajouterBouton(VBox menu, String label, BorderPane root,
                                java.util.function.Supplier<javafx.scene.Parent> panneau) {
        Button btn = new Button(label);
        btn.setPrefWidth(194); btn.setPrefHeight(36);
        String sN = "-fx-background-color:transparent;-fx-text-fill:white;-fx-font-size:12;-fx-alignment:CENTER-LEFT;";
        String sH = "-fx-background-color:#2c3e50;-fx-text-fill:white;-fx-font-size:12;-fx-alignment:CENTER-LEFT;";
        btn.setStyle(sN);
        btn.setOnMouseEntered(e -> btn.setStyle(sH));
        btn.setOnMouseExited(e  -> btn.setStyle(sN));
        btn.setOnAction(e -> root.setCenter(panneau.get()));
        menu.getChildren().add(btn);
    }
}
