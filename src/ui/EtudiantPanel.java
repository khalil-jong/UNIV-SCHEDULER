package ui;

import dao.CoursDAO;
import dao.SalleDAO;
import dao.EmploiDuTempsDAO;
import models.Salle;
import models.Utilisateur;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.List;

public class EtudiantPanel {

    private Utilisateur utilisateur;
    private UnivSchedulerApp app;
    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private EmploiDuTempsDAO edtDAO = new EmploiDuTempsDAO();

    // La classe de l'étudiant — à adapter si tu as un champ "classe" dans Utilisateur
    // Pour l'instant : l'étudiant choisit sa classe au premier accès
    private String classeEtudiant = null;

    public EtudiantPanel(Utilisateur utilisateur, UnivSchedulerApp app) {
        this.utilisateur = utilisateur;
        this.app = app;
    }

    public BorderPane createPanel() {
        BorderPane root = new BorderPane();
        root.setTop(creerTopBar());
        root.setLeft(creerMenu(root));
        root.setCenter(creerAccueil(root));
        return root;
    }

    private HBox creerTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color: #e67e22;");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label("UNIV-SCHEDULER  |  Étudiant");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("👤 " + utilisateur.getNomComplet());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13;");

        Button btnDeco = new Button("Déconnexion");
        btnDeco.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        btnDeco.setOnAction(e -> app.afficherLogin());

        bar.getChildren().addAll(titre, spacer, userLabel, new Label("   "), btnDeco);
        return bar;
    }

    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(5);
        menu.setPadding(new Insets(15));
        menu.setPrefWidth(210);
        menu.setStyle("-fx-background-color: #f39c12;");

        String[][] items = {
            {"🏠 Accueil",               "accueil"},
            {"📅 Mon emploi du temps",   "edt"},
            {"🔍 Chercher salle libre",  "salle"}
        };

        for (String[] item : items) {
            Button btn = new Button(item[0]);
            btn.setPrefWidth(185); btn.setPrefHeight(40);
            String sN = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;";
            String sH = "-fx-background-color: #e67e22;    -fx-text-fill: white; -fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;";
            btn.setStyle(sN);
            btn.setOnMouseEntered(e -> btn.setStyle(sH));
            btn.setOnMouseExited(e  -> btn.setStyle(sN));
            btn.setOnAction(e -> {
                switch (item[1]) {
                    case "accueil": root.setCenter(creerAccueil(root)); break;
                    case "edt":     root.setCenter(creerMonEmploiDuTemps()); break;
                    case "salle":   root.setCenter(creerRechercherSalle()); break;
                }
            });
            menu.getChildren().add(btn);
        }
        return menu;
    }

    // ── Accueil ───────────────────────────────────────────────────────
    private ScrollPane creerAccueil(BorderPane root) {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));

        Label titre = new Label("Bonjour, " + utilisateur.getNomComplet() + " 👋");
        titre.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        Label role = new Label("Rôle : Étudiant  —  Consultation de votre emploi du temps");
        role.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        // Sélection de classe si pas encore choisie
        if (classeEtudiant == null) {
            VBox boxChoix = new VBox(10);
            boxChoix.setPadding(new Insets(18));
            boxChoix.setStyle("-fx-background-color: #fef9e7; -fx-border-color: #f39c12; -fx-border-radius: 8; -fx-background-radius: 8;");

            Label lChoix = new Label("📚 Sélectionnez votre classe pour voir votre emploi du temps :");
            lChoix.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

            javafx.scene.control.ComboBox<String> cbClasse = new javafx.scene.control.ComboBox<>();
            List<String> classes = edtDAO.obtenirToutesLesClasses();
            if (classes.isEmpty()) {
                // Fallback : classes depuis les cours planifiés
                classes = coursDAO.obtenirToutesLesClasses();
            }
            cbClasse.getItems().addAll(classes);
            cbClasse.setPromptText("Choisir ma classe...");
            cbClasse.setPrefWidth(250);

            Button btnValider = new Button("✅ Valider");
            btnValider.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 8 18; -fx-font-weight: bold;");
            btnValider.setOnAction(e -> {
                if (cbClasse.getValue() != null) {
                    classeEtudiant = cbClasse.getValue();
                    root.setCenter(creerMonEmploiDuTemps());
                }
            });

            HBox btnBox = new HBox(10, cbClasse, btnValider);
            btnBox.setAlignment(Pos.CENTER_LEFT);
            boxChoix.getChildren().addAll(lChoix, btnBox);
            panel.getChildren().addAll(titre, role, boxChoix);
        } else {
            Label lClasse = new Label("📚 Votre classe : " + classeEtudiant);
            lClasse.setStyle("-fx-font-size: 14; -fx-text-fill: #e67e22; -fx-font-weight: bold;");

            Button btnChangerClasse = new Button("Changer de classe");
            btnChangerClasse.setStyle("-fx-background-color: transparent; -fx-text-fill: #e67e22; -fx-underline: true;");
            btnChangerClasse.setOnAction(e -> { classeEtudiant = null; root.setCenter(creerAccueil(root)); });

            Label info = new Label("Utilisez le menu à gauche pour consulter votre emploi du temps ou chercher une salle libre.");
            info.setStyle("-fx-font-size: 13; -fx-padding: 15; -fx-background-color: #fef9e7; -fx-border-color: #f39c12; -fx-border-radius: 5;");
            info.setWrapText(true);

            panel.getChildren().addAll(titre, role, lClasse, btnChangerClasse, info);
        }

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ── Emploi du temps (grille hebdomadaire) ─────────────────────────
    private ScrollPane creerMonEmploiDuTemps() {
        if (classeEtudiant == null) {
            // Pas encore de classe choisie → retourner un message
            VBox vb = new VBox(new Label("Veuillez d'abord choisir votre classe depuis l'accueil."));
            vb.setPadding(new Insets(30));
            ScrollPane sp = new ScrollPane(vb); sp.setFitToWidth(true);
            return sp;
        }
        // Afficher la grille hebdomadaire pour cette classe
        return new EmploiDuTempsViewPanel(classeEtudiant).createPanel();
    }

    // ── Recherche salle libre ─────────────────────────────────────────
    private ScrollPane creerRechercherSalle() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titre = new Label("🔍 Rechercher une Salle Libre");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        HBox filtreBox = new HBox(10);
        filtreBox.setAlignment(Pos.CENTER_LEFT);
        Label lCap = new Label("Capacité minimale :");
        Spinner<Integer> spinCap = new Spinner<>(1, 500, 10);
        spinCap.setPrefWidth(100);
        Button btnChercher = new Button("🔍 Chercher");
        btnChercher.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 8 15;");
        filtreBox.getChildren().addAll(lCap, spinCap, btnChercher);

        TableView<Salle> table = new TableView<>();
        table.setPrefHeight(350);

        TableColumn<Salle, String> colNum = new TableColumn<>("Numéro");
        colNum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumero())); colNum.setPrefWidth(90);
        TableColumn<Salle, Integer> colCap = new TableColumn<>("Capacité");
        colCap.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCapacite())); colCap.setPrefWidth(80);
        TableColumn<Salle, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType())); colType.setPrefWidth(75);
        TableColumn<Salle, String> colBat = new TableColumn<>("Bâtiment");
        colBat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBatiment())); colBat.setPrefWidth(110);
        TableColumn<Salle, String> colEquip = new TableColumn<>("Équipements");
        colEquip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEquipementsStr())); colEquip.setPrefWidth(140);

        table.getColumns().addAll(colNum, colCap, colType, colBat, colEquip);

        btnChercher.setOnAction(e -> {
            List<Salle> salles = salleDAO.rechercherParCapacite(spinCap.getValue());
            table.setItems(FXCollections.observableArrayList(salles));
            if (salles.isEmpty())
                new Alert(Alert.AlertType.INFORMATION, "Aucune salle avec capacité ≥ " + spinCap.getValue(), ButtonType.OK).showAndWait();
        });

        panel.getChildren().addAll(titre, filtreBox, table);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }
}
