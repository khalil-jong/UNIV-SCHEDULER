package ui;

import java.util.Optional;

import dao.CoursDAO;
import dao.SalleDAO;
import dao.UtilisateurDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Utilisateur;

public class AdminPanel {

    private Utilisateur utilisateur;
    private UnivSchedulerApp app;
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private CoursDAO coursDAO = new CoursDAO();

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
        menu.setPrefWidth(210);
        menu.setStyle("-fx-background-color: #34495e;");

        ajouterTitreMenu(menu, "ADMINISTRATION");
        ajouterBouton(menu, "📊 Tableau de bord", root, () -> new DashboardPanel().createPanel());
        ajouterBouton(menu, "👥 Utilisateurs", root, () -> creerGestionUtilisateurs());
        ajouterBouton(menu, "📈 Export & Rapports", root, () -> new ExportPanel().createPanel());
        ajouterBouton(menu, "🔔 Alertes", root, () -> new AlertesPanel().createPanel());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "PLANIFICATION");
        ajouterBouton(menu, "📋 Tous les cours", root, () -> new CoursPanel().createPanel());
        ajouterBouton(menu, "📅 Calendrier", root, () -> new CalendrierPanel().createPanel());
        ajouterBouton(menu, "🔍 Recherche salles", root, () -> new RechercheAvanceePanel().createPanel());

        return menu;
    }

    private void ajouterTitreMenu(VBox menu, String titre) {
        Label lbl = new Label(titre);
        lbl.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10; -fx-font-weight: bold; -fx-padding: 10 5 2 5;");
        menu.getChildren().add(lbl);
    }

    private void ajouterBouton(BorderPane root, String label, BorderPane r, java.util.function.Supplier<javafx.scene.Parent> panneau) {
        Button btn = new Button(label);
        btn.setPrefWidth(185);
        btn.setPrefHeight(38);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;"));
        btn.setOnAction(e -> r.setCenter(panneau.get()));
        root.getLeft(); // keep reference
    }

    // méthode helper correcte
    private void ajouterBouton(VBox menu, String label, BorderPane root, java.util.function.Supplier<javafx.scene.Parent> panneau) {
        Button btn = new Button(label);
        btn.setPrefWidth(185);
        btn.setPrefHeight(38);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;"));
        btn.setOnAction(e -> root.setCenter(panneau.get()));
        menu.getChildren().add(btn);
    }

    private ScrollPane creerGestionUtilisateurs() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        Label titre = new Label("Gestion des Utilisateurs");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        TableView<Utilisateur> table = new TableView<>();
        table.setPrefHeight(320);

        TableColumn<Utilisateur, String> colNom = new TableColumn<>("Nom complet");
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNomComplet()));
        colNom.setPrefWidth(160);
        TableColumn<Utilisateur, String> colLogin = new TableColumn<>("Login");
        colLogin.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLogin()));
        colLogin.setPrefWidth(120);
        TableColumn<Utilisateur, String> colRole = new TableColumn<>("Rôle");
        colRole.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getRole()));
        colRole.setPrefWidth(130);

        table.getColumns().addAll(colNom, colLogin, colRole);
        table.setItems(FXCollections.observableArrayList(utilisateurDAO.obtenirTous()));

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(12));
        grid.setStyle("-fx-border-color: #ddd; -fx-background-color: #fafafa; -fx-border-radius: 4;");

        TextField tfNom = new TextField(); tfNom.setPromptText("Nom");
        TextField tfPrenom = new TextField(); tfPrenom.setPromptText("Prénom");
        TextField tfLogin = new TextField(); tfLogin.setPromptText("Login");
        PasswordField pfMdp = new PasswordField(); pfMdp.setPromptText("Mot de passe");
        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("ADMIN", "GESTIONNAIRE", "ENSEIGNANT", "ETUDIANT");
        cbRole.setPromptText("Rôle");

        grid.add(new Label("Nom:"), 0, 0); grid.add(tfNom, 1, 0);
        grid.add(new Label("Prénom:"), 2, 0); grid.add(tfPrenom, 3, 0);
        grid.add(new Label("Login:"), 0, 1); grid.add(tfLogin, 1, 1);
        grid.add(new Label("Mot de passe:"), 2, 1); grid.add(pfMdp, 3, 1);
        grid.add(new Label("Rôle:"), 0, 2); grid.add(cbRole, 1, 2);

        Button btnAjouter = new Button("➕ Ajouter");
        btnAjouter.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 8 15;");
        btnAjouter.setOnAction(e -> {
            if (tfNom.getText().isEmpty() || tfPrenom.getText().isEmpty() || tfLogin.getText().isEmpty() || pfMdp.getText().isEmpty() || cbRole.getValue() == null) {
                alerte(Alert.AlertType.WARNING, "Remplissez tous les champs."); return;
            }
            if (utilisateurDAO.loginExiste(tfLogin.getText().trim())) {
                alerte(Alert.AlertType.ERROR, "Ce login est déjà utilisé."); return;
            }
            try {
                Utilisateur u = new Utilisateur();
                u.setNom(tfNom.getText().trim()); u.setPrenom(tfPrenom.getText().trim());
                u.setLogin(tfLogin.getText().trim()); u.setMotDePasse(pfMdp.getText().trim());
                u.setRole(cbRole.getValue());
                utilisateurDAO.ajouter(u);
                table.setItems(FXCollections.observableArrayList(utilisateurDAO.obtenirTous()));
                tfNom.clear(); tfPrenom.clear(); tfLogin.clear(); pfMdp.clear(); cbRole.setValue(null);
                alerte(Alert.AlertType.INFORMATION, "Utilisateur ajouté avec succès.");
            } catch (RuntimeException ex) { alerte(Alert.AlertType.ERROR, ex.getMessage()); }
        });

        Button btnSupprimer = new Button("🗑 Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 15;");
        btnSupprimer.setOnAction(e -> {
            Utilisateur sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alerte(Alert.AlertType.WARNING, "Sélectionnez un utilisateur."); return; }
            if (sel.getLogin().equals(utilisateur.getLogin())) { alerte(Alert.AlertType.ERROR, "Vous ne pouvez pas supprimer votre propre compte."); return; }
            Optional<ButtonType> res = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + sel.getNomComplet() + " ?", ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                utilisateurDAO.supprimer(sel.getId());
                table.setItems(FXCollections.observableArrayList(utilisateurDAO.obtenirTous()));
            }
        });

        HBox boutons = new HBox(10, btnAjouter, btnSupprimer);
        Label lblForm = new Label("Ajouter un compte :");
        lblForm.setStyle("-fx-font-weight: bold;");
        panel.getChildren().addAll(titre, table, boutons, lblForm, grid);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private void alerte(Alert.AlertType type, String msg) {
        Alert a = new Alert(type); a.setHeaderText(msg); a.showAndWait();
    }
}
