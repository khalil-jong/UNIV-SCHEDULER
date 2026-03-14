package ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dao.CoursDAO;
import dao.SalleDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Cours;
import models.Salle;
import models.Utilisateur;

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
            {"🏠 Accueil",                    "accueil"},
            {"📅 Mon EDT hebdomadaire",        "edt_grille"},
            {"📋 Liste de mes cours",          "edt"},
            {"📨 Demander une réservation",    "reservation"},
            {"🔧 Signaler un problème",        "signalement"}
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
                    case "accueil":    root.setCenter(creerAccueil());                                            break;
                    case "edt_grille": root.setCenter(new EmploiDuTempsViewPanel(utilisateur.getNomComplet(), true).createPanel()); break;
                    case "edt":        root.setCenter(creerMonEmploiDuTemps());                                         break;
                    case "reservation":root.setCenter(new ReservationPanel(utilisateur).createPanel());                 break;
                    case "signalement":root.setCenter(creerSignalement());                                              break;
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

        List<Cours> mesCours = coursDAO.obtenirParEnseignantAvecEDT(utilisateur.getNomComplet());
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

        // Fusion cours ponctuels + créneaux EDT de l'enseignant
        List<Cours> mesCours = coursDAO.obtenirParEnseignantAvecEDT(utilisateur.getNomComplet());

        Label infoLabel = new Label("Cours et créneaux EDT associés à : " + utilisateur.getNomComplet()
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


    // ── Signalement de problème ───────────────────────────────────────
    private ScrollPane creerSignalement() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));

        Label titre = new Label("🔧 Signaler un Problème Technique");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Label desc = new Label("Votre signalement sera transmis directement au gestionnaire dans sa boîte de réception.");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
        desc.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-border-color: #ddd; -fx-background-color: white; -fx-border-radius: 6;");

        ComboBox<Salle> cbSalle = new ComboBox<>(FXCollections.observableArrayList(salleDAO.obtenirTous()));
        cbSalle.setPromptText("Sélectionner une salle");
        cbSalle.setPrefWidth(280);
        cbSalle.setCellFactory(lv -> new javafx.scene.control.ListCell<Salle>() {
            @Override protected void updateItem(Salle s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getNumero() + " — " + s.getBatiment());
            }
        });
        cbSalle.setButtonCell(new javafx.scene.control.ListCell<Salle>() {
            @Override protected void updateItem(Salle s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? "Sélectionner une salle" : s.getNumero() + " — " + s.getBatiment());
            }
        });

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll(
            "Problème électrique", "Vidéoprojecteur défaillant",
            "Tableau interactif défaillant", "Climatisation", "Mobilier cassé", "Autre");
        cbType.setPromptText("Type de problème");
        cbType.setPrefWidth(280);

        TextArea taDesc = new TextArea();
        taDesc.setPromptText("Décrivez le problème en détail...");
        taDesc.setPrefHeight(100);
        taDesc.setPrefWidth(280);
        taDesc.setWrapText(true);

        grid.add(new Label("Salle concernée :"),  0, 0); grid.add(cbSalle, 1, 0);
        grid.add(new Label("Type de problème :"), 0, 1); grid.add(cbType,  1, 1);
        grid.add(new Label("Description :"),      0, 2); grid.add(taDesc,  1, 2);

        Label msgSignal = new Label("");
        msgSignal.setStyle("-fx-font-size: 12;");
        msgSignal.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer au gestionnaire");
        btnEnvoyer.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");
        btnEnvoyer.setOnAction(e -> {
            if (cbSalle.getValue() == null || cbType.getValue() == null || taDesc.getText().isEmpty()) {
                msgSignal.setText("⚠️ Remplissez tous les champs.");
                msgSignal.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12;");
                return;
            }
            String sujet = "[Signalement] " + cbType.getValue()
                + " — Salle " + cbSalle.getValue().getNumero();
            String corps = "Signalement de : " + utilisateur.getNomComplet() + "\n\n"
                + "Salle      : " + cbSalle.getValue().getNumero() + " — " + cbSalle.getValue().getBatiment() + "\n"
                + "Problème   : " + cbType.getValue() + "\n"
                + "Description :\n" + taDesc.getText().trim();
            models.Message msg = new models.Message(
                0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, corps, "RECLAMATION", false, null);
            try {
                new dao.MessageDAO().envoyer(msg);
                msgSignal.setText("✅ Signalement envoyé au gestionnaire.\nSalle : "
                    + cbSalle.getValue().getNumero() + " — " + cbType.getValue());
                msgSignal.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
                cbSalle.setValue(null); cbType.setValue(null); taDesc.clear();
            } catch (Exception ex) {
                msgSignal.setText("❌ Erreur lors de l'envoi : " + ex.getMessage());
                msgSignal.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
            }
        });

        panel.getChildren().addAll(titre, desc, grid, btnEnvoyer, msgSignal);
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
