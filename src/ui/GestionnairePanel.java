package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
        return Design.topBar("Gestionnaire", utilisateur.getNomComplet(),
            Design.GEST_PRIMARY, () -> app.afficherLogin());
    }

    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(2);
        menu.setPadding(new Insets(12, 10, 12, 10));
        menu.setPrefWidth(225);
        menu.setStyle("-fx-background-color: " + Design.GEST_MENU_BG + ";");

        // Avatar utilisateur
        VBox avatar = new VBox(4);
        avatar.setAlignment(Pos.CENTER);
        avatar.setPadding(new Insets(16, 0, 18, 0));
        Label ico = new Label("🧑‍💼");
        ico.setStyle("-fx-font-size: 30;");
        Label nom = new Label(utilisateur.getNomComplet());
        nom.setStyle("-fx-text-fill: white; -fx-font-size: 12; -fx-font-weight: bold;");
        Label roleTag = new Label("GESTIONNAIRE");
        roleTag.setStyle(
            "-fx-text-fill: " + Design.GEST_ACCENT + ";" +
            "-fx-font-size: 9; -fx-font-weight: bold;" +
            "-fx-padding: 2 8; -fx-background-color: rgba(0,184,148,0.18);" +
            "-fx-background-radius: 10;"
        );
        avatar.getChildren().addAll(ico, nom, roleTag);
        menu.getChildren().add(avatar);

        Separator sep0 = new Separator();
        sep0.setStyle("-fx-background-color: rgba(255,255,255,0.12);");
        menu.getChildren().add(sep0);

        // ── VUE GÉNÉRALE ─────────────────────────────────────────────
        menu.getChildren().add(Design.menuTitle("Vue Générale"));
        ajouterBouton(menu, "📊  Tableau de bord",      root, () -> new DashboardPanel().createPanel());
        ajouterBouton(menu, "📅  Calendrier des cours",  root, () -> new CalendrierPanel().createPanel());

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-padding: 4 0;");
        menu.getChildren().add(sep1);

        // ── PLANIFICATION ────────────────────────────────────────────
        menu.getChildren().add(Design.menuTitle("Planification"));
        ajouterBouton(menu, "🎓  Classes, EDT & Cours",  root, () -> new GestionCoursEDTPanel().createPanel());
        ajouterBouton(menu, "👁  Voir un EDT (classe)",   root, () -> new EmploiDuTempsViewPanel(null).createPanel());
        ajouterBouton(menu, "🏫  Salles disponibles",     root, () -> new SallesDisponiblesPanel().createPanel());

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-padding: 4 0;");
        menu.getChildren().add(sep2);

        // ── MESSAGERIE & COMMUNICATION ────────────────────────────────
        // CORRECTION : "Alertes & Conflits" → "Envoi / Réception email"
        //              pour être cohérent avec l'interface Admin
        menu.getChildren().add(Design.menuTitle("Messagerie & Communication"));
        ajouterBouton(menu, "📬  Boîte de réception",      root, () -> new MessageriePanelGestionnaire(utilisateur).createPanel());
        ajouterBouton(menu, "📤  Envoyer un message",       root, () -> new EnvoiMessagePanel(utilisateur).createPanel());
        ajouterBouton(menu, "📧  Envoi / Réception email",  root, () -> creerGestionEmail(root));

        Separator sep3 = new Separator();
        sep3.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-padding: 4 0;");
        menu.getChildren().add(sep3);

        // ── RAPPORTS ─────────────────────────────────────────────────
        menu.getChildren().add(Design.menuTitle("Rapports"));
        ajouterBouton(menu, "📤  Export Emplois du temps",  root, () -> new ExportEdtPanel().createPanel());

        return menu;
    }

    /**
     * Panel Email identique à celui de l'Admin — partagé via EmailGestionPanel.
     * Le gestionnaire a aussi besoin d'envoyer/recevoir des emails.
     */
    private javafx.scene.Parent creerGestionEmail(BorderPane root) {
        return new EmailGestionPanel().createPanel();
    }

    private void ajouterBouton(VBox menu, String label, BorderPane root,
                                java.util.function.Supplier<javafx.scene.Parent> panneau) {
        Button btn = Design.menuBtn(label, Design.GEST_HOVER);
        btn.setOnAction(e -> root.setCenter(panneau.get()));
        menu.getChildren().add(btn);
    }
}
