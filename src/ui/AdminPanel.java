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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

    private HBox creerTopBar() {
        return Design.topBar("Administrateur", utilisateur.getNomComplet(),
            Design.ADMIN_PRIMARY, () -> app.afficherLogin());
    }

    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(2);
        menu.setPadding(new Insets(12, 10, 12, 10));
        menu.setPrefWidth(225);
        menu.setStyle("-fx-background-color: " + Design.ADMIN_MENU_BG + ";");

        // Avatar
        VBox avatar = new VBox(4);
        avatar.setAlignment(Pos.CENTER);
        avatar.setPadding(new Insets(16, 0, 18, 0));
        Label ico  = new Label("👤");
        ico.setStyle("-fx-font-size: 30;");
        Label nom = new Label(utilisateur.getNomComplet());
        nom.setStyle("-fx-text-fill: white; -fx-font-size: 12; -fx-font-weight: bold;");
        Label tag = new Label("ADMINISTRATEUR");
        tag.setStyle(
            "-fx-text-fill: " + Design.ADMIN_ACCENT + ";" +
            "-fx-font-size: 9; -fx-font-weight: bold;" +
            "-fx-padding: 2 8; -fx-background-color: rgba(79,110,247,0.18);" +
            "-fx-background-radius: 10;"
        );
        avatar.getChildren().addAll(ico, nom, tag);
        menu.getChildren().add(avatar);

        Separator sep0 = new Separator();
        sep0.setStyle("-fx-background-color: rgba(255,255,255,0.10);");
        menu.getChildren().add(sep0);

        // ── ADMINISTRATION ───────────────────────────────────────────
        menu.getChildren().add(Design.menuTitle("Administration"));
        ajouterBouton(menu, "📊  Tableau de bord",    root, () -> new DashboardPanel().createPanel());
        ajouterBouton(menu, "👤  Administrateurs",     root, () -> creerGestionParRole("ADMIN"));
        ajouterBouton(menu, "🧑‍💼  Gestionnaires",    root, () -> creerGestionParRole("GESTIONNAIRE"));
        ajouterBouton(menu, "🧑‍🏫  Enseignants",      root, () -> creerGestionParRole("ENSEIGNANT"));
        ajouterBouton(menu, "🎓  Étudiants",           root, () -> creerGestionParRole("ETUDIANT"));

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: rgba(255,255,255,0.10);");
        menu.getChildren().add(sep1);

        // ── INFRASTRUCTURE ───────────────────────────────────────────
        menu.getChildren().add(Design.menuTitle("Infrastructure"));
        ajouterBouton(menu, "🏗  Bâtiments",  root, () -> creerGestionBatiments());
        ajouterBouton(menu, "🏫  Salles",      root, () -> creerGestionSalles());

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: rgba(255,255,255,0.10);");
        menu.getChildren().add(sep2);

        // ── NOTIFICATIONS ────────────────────────────────────────────
        menu.getChildren().add(Design.menuTitle("Notifications & Emails"));
        ajouterBouton(menu, "🔔  Alertes & Notifications", root, () -> new AlertesAdminPanel(utilisateur).createPanel());
        ajouterBouton(menu, "📧  Envoi / Réception email",  root, () -> new EmailGestionPanel().createPanel());

        return menu;
    }

    // ════════════════════════════════════════════════════════════════
    //  GESTION UTILISATEURS
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerGestionParRole(String role) {
        String titreRole = switch (role) {
            case "ADMIN"        -> "👤  Administrateurs";
            case "GESTIONNAIRE" -> "🧑‍💼  Gestionnaires";
            case "ENSEIGNANT"   -> "🧑‍🏫  Enseignants";
            case "ETUDIANT"     -> "🎓  Étudiants";
            default             -> role;
        };

        VBox panel = new VBox(18);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle(titreRole);

        // Tableau
        ObservableList<Utilisateur> items =
            FXCollections.observableArrayList(utilisateurDAO.obtenirParRole(role));
        TableView<Utilisateur> table = new TableView<>(items);
        table.setPrefHeight(240);
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e8ecf5;");
        table.setPlaceholder(new Label("Aucun " + role.toLowerCase() + " enregistré."));

        TableColumn<Utilisateur,String> cNom   = new TableColumn<>("Nom complet");
        cNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomComplet()));
        cNom.setPrefWidth(180);
        TableColumn<Utilisateur,String> cLogin = new TableColumn<>("Login");
        cLogin.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLogin()));
        cLogin.setPrefWidth(130);
        table.getColumns().addAll(cNom, cLogin);
        if (role.equals("ENSEIGNANT")) {
            TableColumn<Utilisateur,String> cMat = new TableColumn<>("Matière(s)");
            cMat.setCellValueFactory(cv -> new SimpleStringProperty(cv.getValue().getMatiere()));
            cMat.setPrefWidth(200);
            table.getColumns().add(cMat);
        }

        // ── Formulaire ──────────────────────────────────────────────
        final Utilisateur[] enCours = {null};

        Label lblMode = new Label("Mode : ➕  Ajout");
        lblMode.setStyle("-fx-font-size: 12; -fx-text-fill: " + Design.INFO + "; -fx-font-weight: bold;");

        VBox formBox = Design.section("✏️  Ajouter / Modifier un " + role.toLowerCase());

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(8, 0, 0, 0));

        TextField     tfNom    = styledField("Nom", 170);
        TextField     tfPrenom = styledField("Prénom", 170);
        TextField     tfLogin  = styledField("Login", 170);
        PasswordField pfMdp    = new PasswordField();
        pfMdp.setPromptText("Mot de passe (vide = inchangé)");
        pfMdp.setPrefWidth(280); pfMdp.setStyle(Design.INPUT_STYLE);

        TextField tfMatiere = styledField("Ex: Algorithmique, Réseaux...", 310);
        boolean isEnseignant = role.equals("ENSEIGNANT");

        grid.add(fLabel("Nom :"),          0, 0); grid.add(tfNom,    1, 0);
        grid.add(fLabel("Prénom :"),       2, 0); grid.add(tfPrenom, 3, 0);
        grid.add(fLabel("Login :"),        0, 1); grid.add(tfLogin,  1, 1);
        grid.add(fLabel("Mot de passe :"), 0, 2); grid.add(pfMdp,    1, 2, 3, 1);
        if (isEnseignant) {
            grid.add(fLabel("Matière(s) :"), 0, 3); grid.add(tfMatiere, 1, 3, 3, 1);
        }

        Label msgU = new Label(""); msgU.setWrapText(true);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            enCours[0] = sel;
            tfNom.setText(sel.getNom()); tfPrenom.setText(sel.getPrenom());
            tfLogin.setText(sel.getLogin()); pfMdp.clear();
            tfMatiere.setText(sel.getMatiere() != null ? sel.getMatiere() : "");
            lblMode.setText("Mode : ✏️  Modification de " + sel.getNomComplet());
            lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.WARNING + ";-fx-font-weight:bold;");
        });

        Button btnSave = Design.btnPrimary("💾  Enregistrer", Design.SUCCESS);
        btnSave.setOnAction(e -> {
            if (tfNom.getText().isEmpty() || tfPrenom.getText().isEmpty() || tfLogin.getText().isEmpty()) {
                setMsg(msgU, "⚠️  Nom, prénom et login sont obligatoires.", Design.WARNING); return;
            }
            try {
                if (enCours[0] != null) {
                    enCours[0].setNom(tfNom.getText().trim());
                    enCours[0].setPrenom(tfPrenom.getText().trim());
                    enCours[0].setLogin(tfLogin.getText().trim());
                    enCours[0].setMotDePasse(pfMdp.getText().trim());
                    if (isEnseignant) enCours[0].setMatiere(tfMatiere.getText().trim());
                    utilisateurDAO.modifier(enCours[0]);
                    setMsg(msgU, "✅  Utilisateur modifié.", Design.SUCCESS);
                } else {
                    if (pfMdp.getText().isEmpty()) { setMsg(msgU, "⚠️  Le mot de passe est obligatoire pour un nouvel utilisateur.", Design.WARNING); return; }
                    if (utilisateurDAO.loginExiste(tfLogin.getText().trim())) { setMsg(msgU, "❌  Ce login est déjà utilisé.", Design.DANGER); return; }
                    Utilisateur u = new Utilisateur();
                    u.setNom(tfNom.getText().trim()); u.setPrenom(tfPrenom.getText().trim());
                    u.setLogin(tfLogin.getText().trim()); u.setMotDePasse(pfMdp.getText().trim());
                    u.setRole(role);
                    if (isEnseignant) u.setMatiere(tfMatiere.getText().trim());
                    utilisateurDAO.ajouter(u);
                    setMsg(msgU, "✅  Utilisateur ajouté.", Design.SUCCESS);
                }
                items.setAll(utilisateurDAO.obtenirParRole(role));
                viderFormUser(tfNom, tfPrenom, tfLogin, pfMdp); tfMatiere.clear();
                enCours[0] = null; table.getSelectionModel().clearSelection();
                lblMode.setText("Mode : ➕  Ajout");
                lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");
            } catch (Exception ex) { setMsg(msgU, "❌  " + ex.getMessage(), Design.DANGER); }
        });

        Button btnAnnuler = Design.btnSecondary("✖  Annuler");
        btnAnnuler.setOnAction(e -> {
            viderFormUser(tfNom, tfPrenom, tfLogin, pfMdp); enCours[0] = null;
            tfMatiere.clear(); table.getSelectionModel().clearSelection();
            lblMode.setText("Mode : ➕  Ajout");
            lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");
            msgU.setText("");
        });

        Button btnSupprimer = Design.btnDanger("🗑  Supprimer");
        btnSupprimer.setOnAction(e -> {
            Utilisateur sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { setMsg(msgU, "⚠️  Sélectionnez un utilisateur dans le tableau.", Design.WARNING); return; }
            if (sel.getLogin().equals(utilisateur.getLogin())) { setMsg(msgU, "❌  Vous ne pouvez pas supprimer votre propre compte.", Design.DANGER); return; }
            Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + sel.getNomComplet() + " ?", ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if (r.isPresent() && r.get() == ButtonType.OK) {
                utilisateurDAO.supprimer(sel.getId());
                items.setAll(utilisateurDAO.obtenirParRole(role));
                viderFormUser(tfNom, tfPrenom, tfLogin, pfMdp); enCours[0] = null;
                setMsg(msgU, "✅  Utilisateur supprimé.", Design.SUCCESS);
            }
        });

        formBox.getChildren().addAll(lblMode, grid, new HBox(10, btnSave, btnAnnuler, btnSupprimer), msgU);
        panel.getChildren().addAll(titre, table, formBox);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════
    //  GESTION BÂTIMENTS
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerGestionBatiments() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");
        Label titre = Design.pageTitle("🏗  Gestion des Bâtiments");

        ObservableList<Batiment> items = FXCollections.observableArrayList(batimentDAO.obtenirTous());
        TableView<Batiment> table = new TableView<>(items);
        table.setPrefHeight(220);
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e8ecf5;");
        table.setPlaceholder(new Label("Aucun bâtiment enregistré."));

        TableColumn<Batiment,String>  cNom = new TableColumn<>("Nom");
        cNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom())); cNom.setPrefWidth(140);
        TableColumn<Batiment,String>  cLoc = new TableColumn<>("Localisation");
        cLoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocalisation())); cLoc.setPrefWidth(220);
        TableColumn<Batiment,Integer> cEtg = new TableColumn<>("Nb étages");
        cEtg.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getNombreEtages())); cEtg.setPrefWidth(90);
        table.getColumns().addAll(cNom, cLoc, cEtg);

        final Batiment[] enCours = {null};
        Label lblMode = new Label("Mode : ➕  Ajout");
        lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");

        VBox formBox = Design.section("✏️  Ajouter / Modifier un bâtiment");
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(8, 0, 0, 0));
        TextField tfNom = styledField("Ex: UFR-SET", 220);
        TextField tfLoc = styledField("Ex: Campus principal", 220);
        Spinner<Integer> spEtg = new Spinner<>(0, 30, 1);
        spEtg.setPrefWidth(90); spEtg.setEditable(true);
        Label noteEtg = new Label("(0 = rez-de-chaussée uniquement)");
        noteEtg.setStyle("-fx-font-size:11;-fx-text-fill:" + Design.TEXT_MUTED + ";");

        grid.add(fLabel("Nom du bâtiment :"), 0, 0); grid.add(tfNom, 1, 0);
        grid.add(fLabel("Localisation :"),     0, 1); grid.add(tfLoc, 1, 1);
        grid.add(fLabel("Nombre d'étages :"),  0, 2); grid.add(new HBox(8, spEtg, noteEtg), 1, 2);

        Label msg = new Label(""); msg.setWrapText(true);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            enCours[0] = sel;
            tfNom.setText(sel.getNom()); tfLoc.setText(sel.getLocalisation());
            spEtg.getValueFactory().setValue(sel.getNombreEtages());
            lblMode.setText("Mode : ✏️  Modification de " + sel.getNom());
            lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.WARNING + ";-fx-font-weight:bold;");
        });

        Button btnSave = Design.btnPrimary("💾  Enregistrer", Design.SUCCESS);
        btnSave.setOnAction(e -> {
            if (tfNom.getText().isEmpty()) { setMsg(msg, "⚠️  Le nom est obligatoire.", Design.WARNING); return; }
            try {
                if (enCours[0] != null) {
                    enCours[0].setNom(tfNom.getText().trim());
                    enCours[0].setLocalisation(tfLoc.getText().trim());
                    enCours[0].setNombreEtages(spEtg.getValue());
                    batimentDAO.modifier(enCours[0]);
                    setMsg(msg, "✅  Bâtiment modifié.", Design.SUCCESS);
                } else {
                    batimentDAO.ajouter(new Batiment(0, tfNom.getText().trim(), tfLoc.getText().trim(), spEtg.getValue()));
                    setMsg(msg, "✅  Bâtiment ajouté.", Design.SUCCESS);
                }
                items.setAll(batimentDAO.obtenirTous());
                tfNom.clear(); tfLoc.clear(); enCours[0] = null; table.getSelectionModel().clearSelection();
                lblMode.setText("Mode : ➕  Ajout");
                lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");
            } catch (Exception ex) { setMsg(msg, "❌  " + ex.getMessage(), Design.DANGER); }
        });

        Button btnSuppr = Design.btnDanger("🗑  Supprimer");
        btnSuppr.setOnAction(e -> {
            Batiment sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { setMsg(msg, "⚠️  Sélectionnez un bâtiment.", Design.WARNING); return; }
            long nbSalles = salleDAO.obtenirParBatiment(sel.getNom()).size();
            String warning = nbSalles > 0 ? "\n\n⚠️  " + nbSalles + " salle(s) seront aussi supprimées." : "";
            Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + sel.getNom() + " ?" + warning, ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if (r.isPresent() && r.get() == ButtonType.OK) {
                batimentDAO.supprimer(sel.getId());
                items.setAll(batimentDAO.obtenirTous());
                enCours[0] = null; table.getSelectionModel().clearSelection();
                setMsg(msg, "✅  Bâtiment" + (nbSalles > 0 ? " et ses " + nbSalles + " salle(s) supprimés." : " supprimé."), Design.SUCCESS);
            }
        });

        Button btnAnnuler = Design.btnSecondary("✖  Annuler");
        btnAnnuler.setOnAction(e -> {
            tfNom.clear(); tfLoc.clear(); enCours[0] = null; table.getSelectionModel().clearSelection();
            lblMode.setText("Mode : ➕  Ajout");
            lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");
            msg.setText("");
        });

        formBox.getChildren().addAll(lblMode, grid, new HBox(10, btnSave, btnAnnuler, btnSuppr), msg);
        panel.getChildren().addAll(titre, table, formBox);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════
    //  GESTION SALLES
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerGestionSalles() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");
        Label titre = Design.pageTitle("🏫  Gestion des Salles");

        // Filtre bâtiment
        HBox filtreBox = new HBox(10);
        filtreBox.setAlignment(Pos.CENTER_LEFT);
        filtreBox.setPadding(new Insets(10, 14, 10, 14));
        filtreBox.setStyle(Design.CARD_STYLE);
        Label lblFil = fLabel("Filtrer par bâtiment :");
        ComboBox<String> cbFiltreBat = new ComboBox<>();
        cbFiltreBat.getItems().add("Tous les bâtiments");
        cbFiltreBat.getItems().addAll(batimentDAO.obtenirNoms());
        cbFiltreBat.setValue("Tous les bâtiments");
        cbFiltreBat.setPrefWidth(240);
        filtreBox.getChildren().addAll(lblFil, cbFiltreBat);

        ObservableList<Salle> items = FXCollections.observableArrayList(salleDAO.obtenirTous());
        TableView<Salle> table = new TableView<>(items);
        table.setPrefHeight(220);
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e8ecf5;");
        table.setPlaceholder(new Label("Aucune salle enregistrée."));

        TableColumn<Salle,String>  cNum  = new TableColumn<>("N°");       cNum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumero())); cNum.setPrefWidth(55);
        TableColumn<Salle,String>  cBat  = new TableColumn<>("Bâtiment"); cBat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBatiment())); cBat.setPrefWidth(120);
        TableColumn<Salle,String>  cEtg  = new TableColumn<>("Étage");    cEtg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEtage())); cEtg.setPrefWidth(90);
        TableColumn<Salle,Integer> cCap  = new TableColumn<>("Cap.");      cCap.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCapacite())); cCap.setPrefWidth(55);
        TableColumn<Salle,String>  cType = new TableColumn<>("Type");     cType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType())); cType.setPrefWidth(60);
        TableColumn<Salle,String>  cEq   = new TableColumn<>("Équipements"); cEq.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEquipementsStr())); cEq.setPrefWidth(160);
        table.getColumns().addAll(cNum, cBat, cEtg, cCap, cType, cEq);

        cbFiltreBat.setOnAction(e -> {
            String v = cbFiltreBat.getValue();
            if (v == null || v.equals("Tous les bâtiments")) items.setAll(salleDAO.obtenirTous());
            else items.setAll(salleDAO.obtenirParBatiment(v));
        });

        final Salle[] enCours = {null};
        Label lblMode = new Label("Mode : ➕  Ajout");
        lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");

        VBox formBox = Design.section("✏️  Ajouter / Modifier une salle");
        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(8, 0, 0, 0));

        ComboBox<String> cbBat = new ComboBox<>();
        cbBat.getItems().addAll(batimentDAO.obtenirNoms());
        cbBat.setPromptText("Sélectionner un bâtiment"); cbBat.setPrefWidth(240);

        TextField tfNum = styledField("Ex: 1 (ou S01, Lab-3...)", 170);
        Label lblNumInfo = new Label(""); lblNumInfo.setStyle("-fx-font-size:11;-fx-text-fill:" + Design.TEXT_MUTED + ";"); lblNumInfo.setWrapText(true);

        ComboBox<String> cbEtage = new ComboBox<>();
        cbEtage.setPromptText("Sélectionner d'abord un bâtiment"); cbEtage.setPrefWidth(240);

        cbBat.setOnAction(e -> {
            cbEtage.getItems().clear();
            String nomBat = cbBat.getValue();
            if (nomBat == null) return;
            Batiment bat = batimentDAO.obtenirParNom(nomBat);
            if (bat == null) return;
            int nbEtages = bat.getNombreEtages();
            cbEtage.getItems().add("Rez-de-chaussée");
            for (int i = 1; i <= nbEtages; i++) cbEtage.getItems().add(i + (i == 1 ? "er étage" : "e étage"));
            cbEtage.setValue(cbEtage.getItems().get(0));
            List<String> existants = salleDAO.obtenirNumerosPourBatiment(nomBat);
            lblNumInfo.setText(existants.isEmpty() ? "Aucune salle dans ce bâtiment." : "Numéros utilisés : " + String.join(", ", existants));
        });

        TextField tfCap = styledField("Ex: 50", 80);
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("TD", "TP", "Amphi"); cbType.setPromptText("Type"); cbType.setPrefWidth(130);
        CheckBox chkV = new CheckBox("📽  Vidéoprojecteur");
        CheckBox chkT = new CheckBox("🖥  Tableau interactif");
        CheckBox chkC = new CheckBox("❄  Climatisation");

        grid.add(fLabel("Bâtiment :"),    0, 0); grid.add(cbBat,   1, 0, 2, 1);
        grid.add(fLabel("N° salle :"),    0, 1); grid.add(tfNum,   1, 1); grid.add(lblNumInfo, 2, 1);
        grid.add(fLabel("Étage :"),       0, 2); grid.add(cbEtage, 1, 2, 2, 1);
        grid.add(fLabel("Capacité :"),    0, 3); grid.add(tfCap,   1, 3);
        grid.add(fLabel("Type :"),        0, 4); grid.add(cbType,  1, 4);
        grid.add(fLabel("Équipements :"), 0, 5); grid.add(new HBox(14, chkV, chkT, chkC), 1, 5, 2, 1);

        Label msg = new Label(""); msg.setWrapText(true);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            enCours[0] = sel;
            cbBat.setValue(sel.getBatiment());
            cbBat.fireEvent(new javafx.event.ActionEvent());
            tfNum.setText(sel.getNumero()); cbEtage.setValue(sel.getEtage());
            tfCap.setText(String.valueOf(sel.getCapacite())); cbType.setValue(sel.getType());
            chkV.setSelected(sel.isVideoprojecteur()); chkT.setSelected(sel.isTableauInteractif()); chkC.setSelected(sel.isClimatisation());
            lblMode.setText("Mode : ✏️  Modification de " + sel.getNumero() + " — " + sel.getBatiment());
            lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.WARNING + ";-fx-font-weight:bold;");
        });

        Button btnSave = Design.btnPrimary("💾  Enregistrer", Design.SUCCESS);
        btnSave.setOnAction(e -> {
            if (cbBat.getValue() == null || tfNum.getText().isEmpty() || cbEtage.getValue() == null || cbType.getValue() == null) {
                setMsg(msg, "⚠️  Bâtiment, numéro, étage et type sont obligatoires.", Design.WARNING); return;
            }
            int cap;
            try { cap = Integer.parseInt(tfCap.getText().trim()); }
            catch (NumberFormatException ex) { setMsg(msg, "❌  Capacité invalide.", Design.DANGER); return; }

            Salle s = enCours[0] != null ? enCours[0] : new Salle();
            s.setNumero(tfNum.getText().trim()); s.setBatiment(cbBat.getValue());
            s.setEtage(cbEtage.getValue() != null ? cbEtage.getValue() : "Rez-de-chaussée");
            s.setCapacite(cap); s.setType(cbType.getValue());
            s.setVideoprojecteur(chkV.isSelected()); s.setTableauInteractif(chkT.isSelected()); s.setClimatisation(chkC.isSelected());
            try {
                if (enCours[0] != null) { salleDAO.modifier(s); setMsg(msg, "✅  Salle modifiée.", Design.SUCCESS); }
                else                    { salleDAO.ajouter(s);   setMsg(msg, "✅  Salle ajoutée.", Design.SUCCESS); }
                String fil = cbFiltreBat.getValue();
                items.setAll(fil == null || fil.equals("Tous les bâtiments") ? salleDAO.obtenirTous() : salleDAO.obtenirParBatiment(fil));
                String curFil = cbFiltreBat.getValue();
                cbFiltreBat.getItems().clear();
                cbFiltreBat.getItems().add("Tous les bâtiments");
                cbFiltreBat.getItems().addAll(batimentDAO.obtenirNoms());
                cbFiltreBat.setValue(curFil);
                viderFormSalle(cbBat, tfNum, cbEtage, tfCap, cbType, chkV, chkT, chkC, lblNumInfo);
                enCours[0] = null; table.getSelectionModel().clearSelection();
                lblMode.setText("Mode : ➕  Ajout");
                lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");
            } catch (Exception ex) { setMsg(msg, "❌  " + ex.getMessage(), Design.DANGER); }
        });

        Button btnSuppr = Design.btnDanger("🗑  Supprimer");
        btnSuppr.setOnAction(e -> {
            Salle sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { setMsg(msg, "⚠️  Sélectionnez une salle.", Design.WARNING); return; }
            Optional<ButtonType> r = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la salle " + sel.getNumero() + " (" + sel.getBatiment() + ") ?",
                ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if (r.isPresent() && r.get() == ButtonType.OK) {
                salleDAO.supprimer(sel.getId());
                String fil = cbFiltreBat.getValue();
                items.setAll(fil == null || fil.equals("Tous les bâtiments") ? salleDAO.obtenirTous() : salleDAO.obtenirParBatiment(fil));
                enCours[0] = null;
                setMsg(msg, "✅  Salle supprimée.", Design.SUCCESS);
            }
        });

        Button btnAnnuler = Design.btnSecondary("✖  Annuler");
        btnAnnuler.setOnAction(e -> {
            viderFormSalle(cbBat, tfNum, cbEtage, tfCap, cbType, chkV, chkT, chkC, lblNumInfo);
            enCours[0] = null; table.getSelectionModel().clearSelection();
            lblMode.setText("Mode : ➕  Ajout");
            lblMode.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.INFO + ";-fx-font-weight:bold;");
            msg.setText("");
        });

        formBox.getChildren().addAll(lblMode, grid, new HBox(10, btnSave, btnAnnuler, btnSuppr), msg);
        panel.getChildren().addAll(titre, filtreBox, table, formBox);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ─── Helpers ─────────────────────────────────────────────────────
    private void setMsg(Label lbl, String text, String color) {
        lbl.setText(text);
        lbl.setStyle("-fx-font-size:12;-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-padding:6 10;-fx-background-color:derive(" + color + ",85%);-fx-background-radius:6;");
    }

    private TextField styledField(String prompt, double w) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setPrefWidth(w); tf.setStyle(Design.INPUT_STYLE);
        return tf;
    }

    private Label fLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";-fx-min-width:130;");
        return lbl;
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

    private void ajouterBouton(VBox menu, String label, BorderPane root,
                                java.util.function.Supplier<javafx.scene.Parent> panneau) {
        Button btn = Design.menuBtn(label, Design.ADMIN_HOVER);
        btn.setOnAction(e -> root.setCenter(panneau.get()));
        menu.getChildren().add(btn);
    }
}
