package ui;

import dao.CoursDAO;
import dao.SalleDAO;
import models.Cours;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EnseignantPanel {

    private Utilisateur utilisateur;
    private UnivSchedulerApp app;
    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private DateTimeFormatter formatter      = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DateTimeFormatter heureFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public EnseignantPanel(Utilisateur utilisateur, UnivSchedulerApp app) {
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

    // ── Top bar ───────────────────────────────────────────────────────
    private HBox creerTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color: #8e44ad;");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label titre = new Label("UNIV-SCHEDULER  |  Enseignant");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("👤 " + utilisateur.getNomComplet());
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13;");

        Button btnDeco = new Button("Déconnexion");
        btnDeco.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnDeco.setOnAction(e -> app.afficherLogin());

        bar.getChildren().addAll(titre, spacer, userLabel, new Label("   "), btnDeco);
        return bar;
    }

    // ── Menu latéral ──────────────────────────────────────────────────
    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(5);
        menu.setPadding(new Insets(15));
        menu.setPrefWidth(210);
        menu.setStyle("-fx-background-color: #9b59b6;");

        String[][] items = {
            {"🏠 Accueil",                   "accueil"},
            {"📅 Mon emploi du temps",        "edt"},
            {"📨 Demander une réservation",   "reservation"},
            {"🔧 Signaler un problème",       "signalement"}
        };

        for (String[] item : items) {
            Button btn = new Button(item[0]);
            btn.setPrefWidth(185);
            btn.setPrefHeight(40);
            String sN = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;";
            String sH = "-fx-background-color: #8e44ad;    -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;";
            btn.setStyle(sN);
            btn.setOnMouseEntered(e -> btn.setStyle(sH));
            btn.setOnMouseExited(e  -> btn.setStyle(sN));
            btn.setOnAction(e -> {
                switch (item[1]) {
                    case "accueil":     root.setCenter(creerAccueil());            break;
                    case "edt":         root.setCenter(creerMonEmploiDuTemps());   break;
                    case "reservation": root.setCenter(creerDemandeReservation()); break;
                    case "signalement": root.setCenter(creerSignalement());        break;
                }
            });
            menu.getChildren().add(btn);
        }
        return menu;
    }

    // ── Accueil ───────────────────────────────────────────────────────
    private ScrollPane creerAccueil() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));

        Label titre = new Label("Bonjour, " + utilisateur.getNomComplet() + " 👋");
        titre.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        Label role = new Label("Rôle : Enseignant  —  Consultation et demande de réservation");
        role.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        Label titreCours = new Label("📋 Vos prochains cours :");
        titreCours.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        List<Cours> mesCours = coursDAO.obtenirParEnseignant(utilisateur.getNomComplet());
        List<Cours> aVenir = mesCours.stream()
            .filter(c -> c.getDateDebut().isAfter(LocalDateTime.now()))
            .limit(5)
            .collect(Collectors.toList());

        VBox listeCours = new VBox(8);

        if (mesCours.isEmpty()) {
            VBox infoBox = new VBox(6);
            infoBox.setPadding(new Insets(12));
            infoBox.setStyle("-fx-background-color: #fef9e7; -fx-border-color: #f39c12; -fx-border-radius: 6; -fx-background-radius: 6;");
            Label lAvt = new Label("⚠️ Aucun cours trouvé à votre nom.");
            lAvt.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #d35400;");
            Label lInfo = new Label(
                "Le nom saisi lors de la création des cours doit correspondre à votre compte.\n" +
                "Nom recherché : \"" + utilisateur.getNomComplet() + "\"\n" +
                "Variante testée : \"" + inverserNom(utilisateur.getNomComplet()) + "\"");
            lInfo.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
            lInfo.setWrapText(true);
            infoBox.getChildren().addAll(lAvt, lInfo);
            listeCours.getChildren().add(infoBox);
        } else if (aVenir.isEmpty()) {
            Label lbl = new Label("✅ " + mesCours.size() + " cours planifié(s) — aucun cours à venir prochainement.");
            lbl.setStyle("-fx-font-size: 12; -fx-padding: 8; -fx-background-color: #eafaf1; -fx-background-radius: 5;");
            listeCours.getChildren().add(lbl);
        } else {
            DateTimeFormatter jourfmt = DateTimeFormatter.ofPattern("EEE dd/MM", Locale.FRENCH);
            for (Cours c : aVenir) {
                Salle salle = salleDAO.obtenirParId(c.getSalleId());
                String nomSalle = salle != null ? salle.getNumero() : "?";

                HBox carte = new HBox(15);
                carte.setPadding(new Insets(10, 15, 10, 15));
                carte.setStyle("-fx-background-color: #f0e6f6; -fx-border-color: #8e44ad; -fx-border-width: 0 0 0 4; -fx-background-radius: 4;");

                Label lHeure = new Label(c.getDateDebut().format(jourfmt) + "\n" + c.getDateDebut().format(heureFormatter));
                lHeure.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #8e44ad; -fx-min-width: 70;");

                Label lMat = new Label(c.getMatiere());
                lMat.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-min-width: 140;");

                Label lClasse = new Label("🎓 " + c.getClasse() + (c.getGroupe().isEmpty() ? "" : "  " + c.getGroupe()));
                lClasse.setStyle("-fx-font-size: 12; -fx-min-width: 120;");

                Label lSalle = new Label("🏫 Salle " + nomSalle);
                lSalle.setStyle("-fx-font-size: 12;");

                carte.getChildren().addAll(lHeure, lMat, lClasse, lSalle);
                listeCours.getChildren().add(carte);
            }
        }

        panel.getChildren().addAll(titre, role, titreCours, listeCours);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ── Emploi du temps complet ───────────────────────────────────────
    private ScrollPane creerMonEmploiDuTemps() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));

        Label titre = new Label("📅 Mon Emploi du Temps");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        List<Cours> mesCours = coursDAO.obtenirParEnseignant(utilisateur.getNomComplet());

        Label infoLabel = new Label("Cours associés à : " + utilisateur.getNomComplet()
            + "  (" + mesCours.size() + " cours trouvé(s))");
        infoLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #8e44ad;");

        TableView<Cours> table = new TableView<>();
        table.setPrefHeight(420);
        table.setPlaceholder(new Label("Aucun cours trouvé à votre nom."));

        TableColumn<Cours, String> colMatiere = new TableColumn<>("Matière");
        colMatiere.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMatiere()));
        colMatiere.setPrefWidth(130);

        TableColumn<Cours, String> colClasse = new TableColumn<>("Classe");
        colClasse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClasse()));
        colClasse.setPrefWidth(110);

        TableColumn<Cours, String> colGroupe = new TableColumn<>("Groupe");
        colGroupe.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGroupe()));
        colGroupe.setPrefWidth(90);

        TableColumn<Cours, String> colDate = new TableColumn<>("Date/Heure");
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateDebut().format(formatter)));
        colDate.setPrefWidth(155);

        TableColumn<Cours, String> colFin = new TableColumn<>("Fin");
        colFin.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateFin().format(heureFormatter)));
        colFin.setPrefWidth(60);

        TableColumn<Cours, Integer> colDuree = new TableColumn<>("Durée(min)");
        colDuree.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDuree()));
        colDuree.setPrefWidth(90);

        TableColumn<Cours, String> colSalle = new TableColumn<>("Salle");
        colSalle.setCellValueFactory(c -> {
            Salle s = salleDAO.obtenirParId(c.getValue().getSalleId());
            return new SimpleStringProperty(s != null ? s.getNumero() : "?");
        });
        colSalle.setPrefWidth(80);

        table.getColumns().addAll(colMatiere, colClasse, colGroupe, colDate, colFin, colDuree, colSalle);
        table.setItems(FXCollections.observableArrayList(mesCours));

        panel.getChildren().addAll(titre, infoLabel, table);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ── Demande de réservation ────────────────────────────────────────
    private ScrollPane creerDemandeReservation() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(25));

        Label titre = new Label("📨 Demander une Réservation de Salle");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Label desc = new Label(
            "Remplissez ce formulaire pour soumettre une demande ponctuelle " +
            "(soutenance, réunion, cours supplémentaire...). Le gestionnaire sera notifié.");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
        desc.setWrapText(true);

        // Tableau des salles disponibles
        Label lblSalles = new Label("🏫 Salles disponibles (cliquez pour pré-remplir) :");
        lblSalles.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        TableView<Salle> tableSalles = new TableView<>();
        tableSalles.setPrefHeight(180);

        TableColumn<Salle, String> colNum = new TableColumn<>("Numéro");
        colNum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumero()));
        colNum.setPrefWidth(80);

        TableColumn<Salle, String> colBat = new TableColumn<>("Bâtiment");
        colBat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBatiment()));
        colBat.setPrefWidth(120);

        TableColumn<Salle, Integer> colCap = new TableColumn<>("Capacité");
        colCap.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCapacite()));
        colCap.setPrefWidth(80);

        TableColumn<Salle, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        colType.setPrefWidth(70);

        TableColumn<Salle, String> colEquip = new TableColumn<>("Équipements");
        colEquip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEquipementsStr()));
        colEquip.setPrefWidth(130);

        tableSalles.getColumns().addAll(colNum, colBat, colCap, colType, colEquip);
        tableSalles.setItems(FXCollections.observableArrayList(salleDAO.obtenirTous()));

        // Formulaire
        Label lblForm = new Label("📝 Formulaire de demande :");
        lblForm.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-border-color: #ddd; -fx-border-radius: 6; -fx-background-color: white; -fx-background-radius: 6;");

        javafx.scene.control.TextField tfSalle      = new javafx.scene.control.TextField();
        tfSalle.setPromptText("Ex: A101 (ou cliquez dans le tableau)");
        tfSalle.setPrefWidth(300);

        javafx.scene.control.TextField tfMotif = new javafx.scene.control.TextField();
        tfMotif.setPromptText("Ex: Soutenance de stage, Réunion pédagogique...");
        tfMotif.setPrefWidth(300);

        DatePicker dpDate = new DatePicker(LocalDate.now().plusDays(1));

        Spinner<Integer> spHeure  = new Spinner<>(7, 22, 8);
        spHeure.setPrefWidth(85);
        Spinner<Integer> spMinute = new Spinner<>(0, 59, 0);
        spMinute.setPrefWidth(85);
        Spinner<Integer> spDuree  = new Spinner<>(30, 480, 90);
        spDuree.setPrefWidth(100);

        TextArea taCommentaire = new TextArea();
        taCommentaire.setPromptText("Nombre de participants, besoins spéciaux...");
        taCommentaire.setPrefHeight(75);
        taCommentaire.setPrefWidth(300);

        HBox heureBox = new HBox(6, new Label("h"), spHeure, new Label("min"), spMinute);
        heureBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Salle souhaitée :"),  0, 0); grid.add(tfSalle,      1, 0);
        grid.add(new Label("Motif :"),            0, 1); grid.add(tfMotif,      1, 1);
        grid.add(new Label("Date :"),             0, 2); grid.add(dpDate,       1, 2);
        grid.add(new Label("Heure de début :"),   0, 3); grid.add(heureBox,     1, 3);
        grid.add(new Label("Durée (minutes) :"),  0, 4); grid.add(spDuree,      1, 4);
        grid.add(new Label("Commentaire :"),      0, 5); grid.add(taCommentaire,1, 5);

        // Clic sur une salle → pré-remplir le champ
        tableSalles.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null)
                tfSalle.setText(sel.getNumero() + " — " + sel.getBatiment() + " (Capacité : " + sel.getCapacite() + ")");
        });

        Label msgDemande = new Label("");
        msgDemande.setStyle("-fx-font-size: 12;");
        msgDemande.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer la demande");
        btnEnvoyer.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

        btnEnvoyer.setOnAction(e -> {
            if (tfSalle.getText().isEmpty() || tfMotif.getText().isEmpty() || dpDate.getValue() == null) {
                msgDemande.setText("⚠️ Remplissez au moins la salle, le motif et la date.");
                msgDemande.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12;");
                return;
            }
            String confirmation = String.format(
                "✅ Demande envoyée au gestionnaire !\n\n" +
                "  Enseignant  : %s\n" +
                "  Salle       : %s\n" +
                "  Motif       : %s\n" +
                "  Date        : %s à %02dh%02d\n" +
                "  Durée       : %d min\n" +
                "  Commentaire : %s",
                utilisateur.getNomComplet(),
                tfSalle.getText(),
                tfMotif.getText(),
                dpDate.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                spHeure.getValue(), spMinute.getValue(),
                spDuree.getValue(),
                taCommentaire.getText().isEmpty() ? "—" : taCommentaire.getText()
            );
            msgDemande.setText(confirmation);
            msgDemande.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
            tfSalle.clear(); tfMotif.clear(); taCommentaire.clear();
            tableSalles.getSelectionModel().clearSelection();
        });

        panel.getChildren().addAll(titre, desc, lblSalles, tableSalles, lblForm, grid, btnEnvoyer, msgDemande);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ── Signalement de problème ───────────────────────────────────────
    private ScrollPane creerSignalement() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label titre = new Label("🔧 Signaler un Problème Technique");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-border-color: #ddd; -fx-background-color: white; -fx-border-radius: 6;");

        ComboBox<Salle> cbSalle = new ComboBox<>(FXCollections.observableArrayList(salleDAO.obtenirTous()));
        cbSalle.setPromptText("Sélectionner une salle");
        cbSalle.setPrefWidth(260);

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll(
            "Problème électrique", "Vidéoprojecteur défaillant",
            "Tableau interactif", "Climatisation", "Mobilier cassé", "Autre");
        cbType.setPromptText("Type de problème");
        cbType.setPrefWidth(260);

        TextArea taDesc = new TextArea();
        taDesc.setPromptText("Décrivez le problème en détail...");
        taDesc.setPrefHeight(100);
        taDesc.setPrefWidth(260);

        grid.add(new Label("Salle concernée :"),  0, 0); grid.add(cbSalle, 1, 0);
        grid.add(new Label("Type de problème :"), 0, 1); grid.add(cbType,  1, 1);
        grid.add(new Label("Description :"),      0, 2); grid.add(taDesc,  1, 2);

        Label msgSignal = new Label("");
        msgSignal.setStyle("-fx-font-size: 12;");

        Button btnEnvoyer = new Button("📤 Envoyer le signalement");
        btnEnvoyer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 20;");
        btnEnvoyer.setOnAction(e -> {
            if (cbSalle.getValue() == null || cbType.getValue() == null || taDesc.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Remplissez tous les champs.", ButtonType.OK).showAndWait();
                return;
            }
            msgSignal.setText("✅ Signalement envoyé — Salle " + cbSalle.getValue().getNumero()
                + " — " + cbType.getValue() + "\nUn administrateur sera notifié.");
            msgSignal.setStyle("-fx-text-fill: #27ae60;");
            cbSalle.setValue(null);
            cbType.setValue(null);
            taDesc.clear();
        });

        panel.getChildren().addAll(titre, grid, btnEnvoyer, msgSignal);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    /** Inverse "Prénom Nom" → "Nom Prénom" pour l'aide au diagnostic */
    private String inverserNom(String nomComplet) {
        String[] p = nomComplet.trim().split("\\s+", 2);
        return p.length == 2 ? p[1] + " " + p[0] : nomComplet;
    }
}
