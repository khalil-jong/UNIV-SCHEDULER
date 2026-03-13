package ui;

import dao.CoursDAO;
import dao.SalleDAO;
import models.Cours;
import models.Salle;
import models.Utilisateur;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Interface ETUDIANT : consulter emploi du temps de sa classe, rechercher salle libre
public class EtudiantPanel {

    private Utilisateur utilisateur;
    private UnivSchedulerApp app;
    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EtudiantPanel(Utilisateur utilisateur, UnivSchedulerApp app) {
        this.utilisateur = utilisateur;
        this.app = app;
    }

    public BorderPane createPanel() {
        BorderPane root = new BorderPane();
        root.setTop(creerTopBar());
        root.setLeft(creerMenu(root));
        root.setCenter(creerAccueil());
        return root;
    }

    private HBox creerTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color: #e67e22;");
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titre = new Label("UNIV-SCHEDULER  |  Étudiant");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("👤 " + utilisateur.getNomComplet());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13;");

        Button btnDeconnexion = new Button("Déconnexion");
        btnDeconnexion.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        btnDeconnexion.setOnAction(e -> app.afficherLogin());

        bar.getChildren().addAll(titre, spacer, userLabel, new Label("   "), btnDeconnexion);
        return bar;
    }

    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(5);
        menu.setPadding(new Insets(15));
        menu.setPrefWidth(200);
        menu.setStyle("-fx-background-color: #f39c12;");

        String[] labels = {"🏠 Accueil", "📅 Emploi du temps", "🔍 Chercher salle libre"};

        for (String label : labels) {
            Button btn = new Button(label);
            btn.setPrefWidth(175);
            btn.setPrefHeight(40);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;"));

            btn.setOnAction(e -> {
                switch (label) {
                    case "🏠 Accueil": root.setCenter(creerAccueil()); break;
                    case "📅 Emploi du temps": root.setCenter(creerEmploiDuTemps()); break;
                    case "🔍 Chercher salle libre": root.setCenter(creerRechercherSalle()); break;
                }
            });
            menu.getChildren().add(btn);
        }
        return menu;
    }

    private VBox creerAccueil() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));

        Label titre = new Label("Bonjour, " + utilisateur.getNomComplet() + " 👋");
        titre.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        Label role = new Label("Rôle : Étudiant  |  Consultation uniquement");
        role.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        Label info = new Label("Utilisez le menu à gauche pour consulter votre emploi du temps\nou chercher une salle libre pour étudier.");
        info.setStyle("-fx-font-size: 13; -fx-padding: 15; -fx-background-color: #fef9e7; -fx-border-color: #f39c12; -fx-border-radius: 5;");
        info.setWrapText(true);

        panel.getChildren().addAll(titre, role, info);
        return panel;
    }

    private VBox creerEmploiDuTemps() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));

        Label titre = new Label("Emploi du Temps par Classe");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        HBox filtreBox = new HBox(10);
        filtreBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField tfClasse = new TextField();
        tfClasse.setPromptText("Ex: L2-Informatique");
        tfClasse.setPrefWidth(200);

        Button btnFiltrer = new Button("🔍 Rechercher");
        btnFiltrer.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 8 15;");

        filtreBox.getChildren().addAll(new Label("Votre classe :"), tfClasse, btnFiltrer);

        TableView<Cours> table = new TableView<>();
        table.setPrefHeight(380);

        TableColumn<Cours, String> colMatiere = new TableColumn<>("Matière");
        colMatiere.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMatiere()));
        colMatiere.setPrefWidth(130);

        TableColumn<Cours, String> colEnseignant = new TableColumn<>("Enseignant");
        colEnseignant.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEnseignant()));
        colEnseignant.setPrefWidth(130);

        TableColumn<Cours, String> colDate = new TableColumn<>("Date/Heure");
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDateDebut().format(formatter)));
        colDate.setPrefWidth(150);

        TableColumn<Cours, Integer> colDuree = new TableColumn<>("Durée (min)");
        colDuree.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDuree()));
        colDuree.setPrefWidth(100);

        TableColumn<Cours, String> colSalle = new TableColumn<>("Salle");
        colSalle.setCellValueFactory(c -> {
            Salle s = salleDAO.obtenirParId(c.getValue().getSalleId());
            return new javafx.beans.property.SimpleStringProperty(s != null ? s.getNumero() : "?");
        });
        colSalle.setPrefWidth(80);

        table.getColumns().addAll(colMatiere, colEnseignant, colDate, colDuree, colSalle);

        btnFiltrer.setOnAction(e -> {
            String classe = tfClasse.getText().trim();
            if (classe.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Entrez votre classe.", ButtonType.OK).showAndWait();
                return;
            }
            List<Cours> cours = coursDAO.obtenirParClasse(classe);
            table.setItems(FXCollections.observableArrayList(cours));
            if (cours.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "Aucun cours trouvé pour la classe : " + classe, ButtonType.OK).showAndWait();
            }
        });

        panel.getChildren().addAll(titre, filtreBox, table);
        return panel;
    }

    private VBox creerRechercherSalle() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));

        Label titre = new Label("Rechercher une Salle Libre pour Étudier");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        HBox filtreBox = new HBox(10);
        filtreBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lCap = new Label("Capacité minimale :");
        Spinner<Integer> spinCap = new Spinner<>(1, 500, 10);
        spinCap.setPrefWidth(100);

        Button btnChercher = new Button("🔍 Chercher");
        btnChercher.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-padding: 8 15;");

        filtreBox.getChildren().addAll(lCap, spinCap, btnChercher);

        TableView<Salle> table = new TableView<>();
        table.setPrefHeight(350);

        TableColumn<Salle, String> colNumero = new TableColumn<>("Numéro");
        colNumero.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNumero()));
        colNumero.setPrefWidth(100);

        TableColumn<Salle, Integer> colCapacite = new TableColumn<>("Capacité");
        colCapacite.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getCapacite()));
        colCapacite.setPrefWidth(80);

        TableColumn<Salle, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getType()));
        colType.setPrefWidth(80);

        TableColumn<Salle, String> colBatiment = new TableColumn<>("Bâtiment");
        colBatiment.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBatiment()));
        colBatiment.setPrefWidth(100);

        TableColumn<Salle, String> colEtage = new TableColumn<>("Étage");
        colEtage.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEtage()));
        colEtage.setPrefWidth(80);

        table.getColumns().addAll(colNumero, colCapacite, colType, colBatiment, colEtage);

        btnChercher.setOnAction(e -> {
            List<Salle> salles = salleDAO.rechercherParCapacite(spinCap.getValue());
            table.setItems(FXCollections.observableArrayList(salles));
            if (salles.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "Aucune salle avec cette capacité.", ButtonType.OK).showAndWait();
            }
        });

        panel.getChildren().addAll(titre, filtreBox, table);
        return panel;
    }
}
