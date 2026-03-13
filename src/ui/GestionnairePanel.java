package ui;

import dao.CoursDAO;
import dao.SalleDAO;
import models.Cours;
import models.Salle;
import models.Utilisateur;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class GestionnairePanel {

    private Utilisateur utilisateur;
    private UnivSchedulerApp app;
    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public GestionnairePanel(Utilisateur utilisateur, UnivSchedulerApp app) {
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
        bar.setStyle("-fx-background-color: #27ae60;");
        bar.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("UNIV-SCHEDULER  |  Gestionnaire");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label userLabel = new Label("👤 " + utilisateur.getNomComplet());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13;");
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
        menu.setStyle("-fx-background-color: #2ecc71;");

        ajouterTitreMenu(menu, "VUE GÉNÉRALE");
        ajouterBouton(menu, "📊 Tableau de bord",    root, () -> new DashboardPanel().createPanel());
        ajouterBouton(menu, "📅 Calendrier des cours",root, () -> new CalendrierPanel().createPanel());
        ajouterBouton(menu, "🔔 Alertes & Conflits",  root, () -> new AlertesPanel().createPanel());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "EMPLOI DU TEMPS");
        ajouterBouton(menu, "📋 Gérer les EDT",       root, () -> new EmploiDuTempsGestionPanel().createPanel());
        ajouterBouton(menu, "👁 Voir un EDT (classe)", root, () -> new EmploiDuTempsViewPanel(null).createPanel());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "GESTION DES COURS");
        ajouterBouton(menu, "➕ Ajouter un cours",    root, () -> new AjouterCoursPanel().createPanel());
        ajouterBouton(menu, "📋 Liste des cours",     root, () -> creerListeCours());
        ajouterBouton(menu, "🏗 Infrastructures",     root, () -> new GestionInfraPanel().createPanel());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "OUTILS");
        ajouterBouton(menu, "🔍 Salles disponibles",  root, () -> new RechercheAvanceePanel().createPanel());
        ajouterBouton(menu, "📤 Export & Rapports",   root, () -> new ExportPanel().createPanel());

        return menu;
    }

    private void ajouterTitreMenu(VBox menu, String titre) {
        Label lbl = new Label(titre);
        lbl.setStyle("-fx-text-fill: #1a5c35; -fx-font-size: 10; -fx-font-weight: bold; -fx-padding: 10 5 2 5;");
        menu.getChildren().add(lbl);
    }

    private void ajouterBouton(VBox menu, String label, BorderPane root,
                                java.util.function.Supplier<javafx.scene.Parent> panneau) {
        Button btn = new Button(label);
        btn.setPrefWidth(190); btn.setPrefHeight(38);
        String sN = "-fx-background-color: transparent; -fx-text-fill: #1a5c35; -fx-font-size: 12; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;";
        String sH = "-fx-background-color: #27ae60;    -fx-text-fill: white;   -fx-font-size: 12; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;";
        btn.setStyle(sN);
        btn.setOnMouseEntered(e -> btn.setStyle(sH));
        btn.setOnMouseExited(e  -> btn.setStyle(sN));
        btn.setOnAction(e -> root.setCenter(panneau.get()));
        menu.getChildren().add(btn);
    }

    private ScrollPane creerListeCours() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        Label titre = new Label("📋 Gestion des Cours");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        TableView<Cours> table = new TableView<>();
        table.setPrefHeight(380);

        TableColumn<Cours, String> colMatiere = new TableColumn<>("Matière");
        colMatiere.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMatiere())); colMatiere.setPrefWidth(130);
        TableColumn<Cours, String> colEnseignant = new TableColumn<>("Enseignant");
        colEnseignant.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEnseignant())); colEnseignant.setPrefWidth(130);
        TableColumn<Cours, String> colClasse = new TableColumn<>("Classe");
        colClasse.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getClasse())); colClasse.setPrefWidth(100);
        TableColumn<Cours, String> colGroupe = new TableColumn<>("Groupe");
        colGroupe.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getGroupe())); colGroupe.setPrefWidth(80);
        TableColumn<Cours, String> colDate = new TableColumn<>("Date/Heure");
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDateDebut().format(formatter))); colDate.setPrefWidth(140);
        TableColumn<Cours, Integer> colDuree = new TableColumn<>("Durée(min)");
        colDuree.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDuree())); colDuree.setPrefWidth(90);
        TableColumn<Cours, String> colSalle = new TableColumn<>("Salle");
        colSalle.setCellValueFactory(c -> {
            Salle s = salleDAO.obtenirParId(c.getValue().getSalleId());
            return new javafx.beans.property.SimpleStringProperty(s != null ? s.getNumero() : "?");
        }); colSalle.setPrefWidth(80);

        TableColumn<Cours, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(110);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnSuppr = new Button("🗑 Supprimer");
            { btnSuppr.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
              btnSuppr.setOnAction(e -> {
                Cours cours = getTableView().getItems().get(getIndex());
                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce cours ?", ButtonType.YES, ButtonType.NO);
                Optional<ButtonType> r = a.showAndWait();
                if (r.isPresent() && r.get() == ButtonType.YES) {
                    coursDAO.supprimer(cours.getId());
                    table.setItems(FXCollections.observableArrayList(coursDAO.obtenirTous()));
                }
              });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); setGraphic(empty ? null : btnSuppr);
            }
        });

        table.getColumns().addAll(colMatiere, colEnseignant, colClasse, colGroupe, colDate, colDuree, colSalle, colAction);
        table.setItems(FXCollections.observableArrayList(coursDAO.obtenirTous()));

        panel.getChildren().addAll(titre, table);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }
}
