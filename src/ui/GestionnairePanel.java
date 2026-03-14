package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Utilisateur;

public class GestionnairePanel {

    private Utilisateur      utilisateur;
    private UnivSchedulerApp app;

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
        menu.setPrefWidth(220);
        menu.setStyle("-fx-background-color: #2ecc71;");

        ajouterTitreMenu(menu, "VUE GÉNÉRALE");
        ajouterBouton(menu, "📊 Tableau de bord",     root, () -> new DashboardPanel().createPanel());
        ajouterBouton(menu, "📅 Calendrier des cours", root, () -> new CalendrierPanel().createPanel());
        ajouterBouton(menu, "🔔 Alertes & Conflits",   root, () -> new AlertesPanel().createPanel());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "PLANIFICATION");
        // Menu unifié : Classes + EDT + Cours ponctuels
        ajouterBouton(menu, "🎓 Classes, EDT & Cours",  root, () -> new GestionCoursEDTPanel().createPanel());
        ajouterBouton(menu, "👁 Voir un EDT (classe)",   root, () -> new EmploiDuTempsViewPanel(null).createPanel());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "MESSAGERIE");
        ajouterBouton(menu, "📬 Boîte de réception",   root, () -> new MessageriePanelGestionnaire().createPanel());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "INFRASTRUCTURES");
        ajouterBouton(menu, "🏗 Salles & Bâtiments",   root, () -> new GestionInfraPanel().createPanel());
        ajouterBouton(menu, "🔍 Salles disponibles",   root, () -> new RechercheAvanceePanel().createPanel());

        menu.getChildren().add(new Separator());
        ajouterTitreMenu(menu, "RAPPORTS");
        ajouterBouton(menu, "📤 Export Emplois du temps", root, () -> new ExportEdtPanel().createPanel());

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
        btn.setPrefWidth(195); btn.setPrefHeight(38);
        String sN = "-fx-background-color: transparent; -fx-text-fill: #1a5c35; -fx-font-size: 12; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;";
        String sH = "-fx-background-color: #27ae60;    -fx-text-fill: white;   -fx-font-size: 12; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;";
        btn.setStyle(sN);
        btn.setOnMouseEntered(e -> btn.setStyle(sH));
        btn.setOnMouseExited(e  -> btn.setStyle(sN));
        btn.setOnAction(e -> root.setCenter(panneau.get()));
        menu.getChildren().add(btn);
    }
}
