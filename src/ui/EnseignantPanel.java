package ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dao.CoursDAO;
import dao.MessageDAO;
import dao.SalleDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
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
import models.Cours;
import models.Message;
import models.Salle;
import models.Utilisateur;

/**
 * Panel Enseignant — 3 fonctionnalités :
 *   1. Consulter son emploi du temps (grille hebdomadaire + liste)
 *   2. Réserver une salle ponctuellement (avec recherche temps réel des salles disponibles)
 *   3. Signaler un problème technique
 * + Boîte de réception (messages du gestionnaire)
 */
public class EnseignantPanel {

    private Utilisateur utilisateur;
    private UnivSchedulerApp app;
    private CoursDAO    coursDAO = new CoursDAO();
    private SalleDAO    salleDAO = new SalleDAO();
    private MessageDAO  msgDAO   = new MessageDAO();
    private DateTimeFormatter fmt    = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DateTimeFormatter hFmt   = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter jFmt   = DateTimeFormatter.ofPattern("EEE dd/MM", Locale.FRENCH);

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

    // ── Top bar ──────────────────────────────────────────────────
    private HBox creerTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color: #8e44ad;");
        bar.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("UNIV-SCHEDULER  |  Enseignant");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        // Badge non lus
        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        Label userLabel = new Label("👤 " + utilisateur.getNomComplet()
            + (nonLus > 0 ? "   🔴 " + nonLus + " msg" : ""));
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13;");
        Button btnDeco = new Button("Déconnexion");
        btnDeco.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnDeco.setOnAction(e -> app.afficherLogin());
        bar.getChildren().addAll(titre, spacer, userLabel, new Label("   "), btnDeco);
        return bar;
    }

    // ── Menu ─────────────────────────────────────────────────────
    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(5);
        menu.setPadding(new Insets(15));
        menu.setPrefWidth(215);
        menu.setStyle("-fx-background-color: #9b59b6;");

        // Badge non lus pour le menu
        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        String msgLabel = nonLus > 0 ? "📬 Mes messages  🔴 " + nonLus : "📬 Mes messages";

        String[][] items = {
            {"🏠 Accueil",                 "accueil"},
            {"📅 Mon emploi du temps",     "edt_grille"},

            {"📨 Réserver une salle",      "reservation"},
            {"🔧 Signaler un problème",    "signalement"},
            {msgLabel,                     "messages"}
        };

        for (String[] item : items) {
            Button btn = new Button(item[0]);
            btn.setPrefWidth(190); btn.setPrefHeight(40);
            String sN = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;";
            String sH = "-fx-background-color: #8e44ad;    -fx-text-fill: white; -fx-font-size: 12; -fx-alignment: CENTER-LEFT;";
            btn.setStyle(sN);
            btn.setOnMouseEntered(e -> btn.setStyle(sH));
            btn.setOnMouseExited(e  -> btn.setStyle(sN));
            btn.setOnAction(e -> {
                switch (item[1]) {
                    case "accueil":     root.setCenter(creerAccueil());             break;
                    case "edt_grille":  root.setCenter(new EmploiDuTempsViewPanel(utilisateur.getNomComplet(), true).createPanel()); break;

                    case "reservation": root.setCenter(creerReservation());         break;
                    case "signalement": root.setCenter(creerSignalement());         break;
                    case "messages":    root.setCenter(creerBoiteReception());      break;
                }
            });
            menu.getChildren().add(btn);
        }
        return menu;
    }

    // ════════════════════════════════════════════════════════════
    //  ACCUEIL
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerAccueil() {
        VBox panel = new VBox(20); panel.setPadding(new Insets(30));

        Label titre = new Label("Bonjour, " + utilisateur.getNomComplet() + " 👋");
        titre.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");
        Label role = new Label("Rôle : Enseignant");
        role.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        // Badge non lus
        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        if (nonLus > 0) {
            Label lMsg = new Label("📬 Vous avez " + nonLus + " message(s) non lu(s) du gestionnaire.");
            lMsg.setStyle("-fx-font-size: 13; -fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-padding: 8 12; -fx-background-color: #fdecea; -fx-border-color: #e74c3c; -fx-border-radius: 6; -fx-background-radius: 6;");
            panel.getChildren().addAll(titre, role, lMsg);
        } else {
            panel.getChildren().addAll(titre, role);
        }

        // Prochains cours
        Label titreCours = new Label("📋 Vos prochains cours :");
        titreCours.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        List<Cours> mesCours = coursDAO.obtenirParEnseignantAvecEDT(utilisateur.getNomComplet());
        List<Cours> aVenir   = mesCours.stream()
            .filter(c -> c.getDateDebut().isAfter(LocalDateTime.now()))
            .limit(5).collect(Collectors.toList());

        VBox listeCours = new VBox(8);
        if (aVenir.isEmpty()) {
            Label lbl = new Label("Aucun cours à venir prochainement.");
            lbl.setStyle("-fx-font-size:13;-fx-text-fill:#95a5a6;-fx-padding:10;");
            listeCours.getChildren().add(lbl);
        } else {
            for (Cours c : aVenir) {
                Salle s = salleDAO.obtenirParId(c.getSalleId());
                HBox carte = new HBox(15); carte.setPadding(new Insets(10, 15, 10, 15));
                carte.setStyle("-fx-background-color:#f0e6f6;-fx-border-color:#8e44ad;-fx-border-width:0 0 0 4;-fx-background-radius:4;");
                Label lH = new Label(c.getDateDebut().format(jFmt) + "\n" + c.getDateDebut().format(hFmt));
                lH.setStyle("-fx-font-weight:bold;-fx-font-size:12;-fx-text-fill:#8e44ad;-fx-min-width:75;");
                Label lM = new Label(c.getMatiere()); lM.setStyle("-fx-font-weight:bold;-fx-font-size:13;-fx-min-width:140;");
                Label lC = new Label("🎓 " + c.getClasse()); lC.setStyle("-fx-font-size:12;-fx-min-width:120;");
                Label lS = new Label("🏫 " + (s != null ? s.getNumero() : "?")); lS.setStyle("-fx-font-size:12;");
                carte.getChildren().addAll(lH, lM, lC, lS);
                listeCours.getChildren().add(carte);
            }
        }
        panel.getChildren().addAll(titreCours, listeCours);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }



    // ════════════════════════════════════════════════════════════
    //  RÉSERVATION DE SALLE (avec recherche de salles disponibles)
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerReservation() {
        VBox panel = new VBox(18); panel.setPadding(new Insets(20));
        Label titre = new Label("📨 Demander une Réservation de Salle");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        Label desc = new Label("Choisissez un créneau, recherchez les salles libres, puis envoyez votre demande au gestionnaire.");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #555;"); desc.setWrapText(true);

        // ── Étape 1 : choisir date/heure/durée et chercher ──
        VBox boxCherche = section("🔍 Étape 1 — Rechercher une salle disponible");
        GridPane gCherche = new GridPane(); gCherche.setHgap(12); gCherche.setVgap(10);

        DatePicker dp  = new DatePicker(LocalDate.now().plusDays(1));
        Spinner<Integer> spH   = new Spinner<>(7, 22, 8);   spH.setPrefWidth(80);
        Spinner<Integer> spMin = new Spinner<>(0, 59, 0, 5); spMin.setPrefWidth(80);
        Spinner<Integer> spDur = new Spinner<>(15,480,60,15);spDur.setPrefWidth(90);
        CheckBox chkV = new CheckBox("📽 Vidéoprojecteur");
        CheckBox chkT = new CheckBox("🖥 Tableau interactif");
        CheckBox chkC = new CheckBox("❄ Climatisation");
        Spinner<Integer> spCap = new Spinner<>(0, 500, 0);   spCap.setPrefWidth(80);
        Label lblCapNote = new Label("(0 = indifférent)");
        lblCapNote.setStyle("-fx-font-size:10;-fx-text-fill:#aaa;");

        gCherche.add(new Label("Date :"),          0,0); gCherche.add(dp,   1,0);
        gCherche.add(new Label("Heure début :"),   0,1); gCherche.add(new HBox(5,spH,new Label("h"),spMin,new Label("min")), 1,1);
        gCherche.add(new Label("Durée (min) :"),   0,2); gCherche.add(spDur, 1,2);
        gCherche.add(new Label("Capacité min :"),  0,3); gCherche.add(new HBox(6,spCap,lblCapNote), 1,3);
        gCherche.add(new Label("Équipements :"),   0,4); gCherche.add(new HBox(14,chkV,chkT,chkC), 1,4);

        Button btnChercher = new Button("🔍 Rechercher les salles libres");
        btnChercher.setStyle("-fx-background-color:#8e44ad;-fx-text-fill:white;-fx-padding:8 18;-fx-font-weight:bold;");
        Label lblResultat = new Label(""); lblResultat.setStyle("-fx-font-size:12;-fx-font-weight:bold;");

        // Tableau des salles disponibles
        TableView<Salle> tableSalles = new TableView<>();
        tableSalles.setPrefHeight(180);
        tableSalles.setPlaceholder(new Label("Lancez une recherche pour voir les salles disponibles."));

        TableColumn<Salle,String>  sNum  = new TableColumn<>("N°");     sNum.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getNumero())); sNum.setPrefWidth(60);
        TableColumn<Salle,String>  sBat  = new TableColumn<>("Bâtiment");sBat.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getBatiment())); sBat.setPrefWidth(130);
        TableColumn<Salle,Integer> sCap  = new TableColumn<>("Cap.");   sCap.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().getCapacite())); sCap.setPrefWidth(55);
        TableColumn<Salle,String>  sType = new TableColumn<>("Type");   sType.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getType())); sType.setPrefWidth(60);
        TableColumn<Salle,String>  sEq   = new TableColumn<>("Équipements"); sEq.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getEquipementsStr())); sEq.setPrefWidth(150);
        tableSalles.getColumns().addAll(sNum,sBat,sCap,sType,sEq);

        btnChercher.setOnAction(e -> {
            if (dp.getValue() == null) { lblResultat.setText("⚠️ Sélectionnez une date."); return; }
            LocalDateTime debut = LocalDateTime.of(dp.getValue(), LocalTime.of(spH.getValue(), spMin.getValue()));
            List<Salle> dispo = salleDAO.obtenirSallesDisponibles(debut, spDur.getValue());
            // Filtrer équipements et capacité
            if (spCap.getValue() > 0) {
				dispo = dispo.stream().filter(s -> s.getCapacite() >= spCap.getValue()).collect(Collectors.toList());
			}
            if (chkV.isSelected()) {
				dispo = dispo.stream().filter(Salle::isVideoprojecteur).collect(Collectors.toList());
			}
            if (chkT.isSelected()) {
				dispo = dispo.stream().filter(Salle::isTableauInteractif).collect(Collectors.toList());
			}
            if (chkC.isSelected()) {
				dispo = dispo.stream().filter(Salle::isClimatisation).collect(Collectors.toList());
			}
            tableSalles.setItems(FXCollections.observableArrayList(dispo));
            lblResultat.setText(dispo.isEmpty()
                ? "❌ Aucune salle disponible pour ce créneau."
                : "✅ " + dispo.size() + " salle(s) disponible(s) — cliquez pour sélectionner.");
            lblResultat.setStyle(dispo.isEmpty() ? "-fx-text-fill:#e74c3c;-fx-font-size:12;" : "-fx-text-fill:#27ae60;-fx-font-size:12;");
        });

        boxCherche.getChildren().addAll(gCherche, btnChercher, lblResultat, tableSalles);

        // ── Étape 2 : envoyer la demande ──
        VBox boxDemande = section("📤 Étape 2 — Envoyer la demande au gestionnaire");
        Label lblSalleChoisie = new Label("Aucune salle sélectionnée — cliquez sur une ligne ci-dessus.");
        lblSalleChoisie.setStyle("-fx-font-size:12;-fx-text-fill:#7f8c8d;-fx-padding:4;");

        TextField tfMotif = new TextField(); tfMotif.setPromptText("Ex: Cours supplémentaire, Soutenance, Réunion..."); tfMotif.setPrefWidth(380);
        TextArea  taComment = new TextArea(); taComment.setPromptText("Précisions (effectif, besoins spécifiques...)"); taComment.setPrefHeight(70); taComment.setWrapText(true);

        Label msgRes = new Label(""); msgRes.setStyle("-fx-font-size:12;"); msgRes.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer la demande");
        btnEnvoyer.setStyle("-fx-background-color:#8e44ad;-fx-text-fill:white;-fx-padding:9 20;-fx-font-weight:bold;");
        btnEnvoyer.setDisable(true); // activé quand salle sélectionnée

        // Clic sur salle → pré-remplir
        final Salle[] salleChoisie = {null};
        tableSalles.getSelectionModel().selectedItemProperty().addListener((obs,old,sel) -> {
            if (sel == null) {
				return;
			}
            salleChoisie[0] = sel;
            lblSalleChoisie.setText("🏫 Salle sélectionnée : " + sel.getNumero() + " — " + sel.getBatiment() + " (Cap: " + sel.getCapacite() + ")");
            lblSalleChoisie.setStyle("-fx-font-size:12;-fx-text-fill:#27ae60;-fx-font-weight:bold;");
            btnEnvoyer.setDisable(false);
        });

        btnEnvoyer.setOnAction(e -> {
            if (salleChoisie[0] == null || dp.getValue() == null) { msgRes.setText("⚠️ Sélectionnez une salle et une date."); msgRes.setStyle("-fx-text-fill:#e67e22;"); return; }
            if (tfMotif.getText().isEmpty()) { msgRes.setText("⚠️ Le motif est obligatoire."); msgRes.setStyle("-fx-text-fill:#e67e22;"); return; }
            String sujet = "[Réservation] " + tfMotif.getText().trim() + " — Salle " + salleChoisie[0].getNumero();
            String corps = "Demande de : " + utilisateur.getNomComplet() + " (Enseignant)\n\n"
                + "Salle demandée : " + salleChoisie[0].getNumero() + " — " + salleChoisie[0].getBatiment() + " (Cap: " + salleChoisie[0].getCapacite() + ")\n"
                + "Motif : " + tfMotif.getText().trim() + "\n"
                + "Date : " + dp.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n"
                + "Heure : " + String.format("%02dh%02d", spH.getValue(), spMin.getValue()) + "\n"
                + "Durée : " + spDur.getValue() + " min\n"
                + (taComment.getText().isEmpty() ? "" : "Commentaire : " + taComment.getText());
            Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, corps, "RESERVATION", false, null, "GESTIONNAIRE");
            try {
                msgDAO.envoyer(msg);
                msgRes.setText("✅ Demande envoyée. Le gestionnaire la recevra dans sa boîte de réception.");
                msgRes.setStyle("-fx-text-fill:#27ae60;");
                tfMotif.clear(); taComment.clear(); salleChoisie[0] = null; btnEnvoyer.setDisable(true);
                lblSalleChoisie.setText("Aucune salle sélectionnée.");
                lblSalleChoisie.setStyle("-fx-font-size:12;-fx-text-fill:#7f8c8d;");
                tableSalles.getSelectionModel().clearSelection();
            } catch (Exception ex) { msgRes.setText("❌ " + ex.getMessage()); msgRes.setStyle("-fx-text-fill:#e74c3c;"); }
        });

        GridPane gDemande = new GridPane(); gDemande.setHgap(10); gDemande.setVgap(10);
        gDemande.add(new Label("Motif :"),      0,0); gDemande.add(tfMotif,   1,0);
        gDemande.add(new Label("Précisions :"), 0,1); gDemande.add(taComment, 1,1);
        boxDemande.getChildren().addAll(lblSalleChoisie, gDemande, btnEnvoyer, msgRes);

        panel.getChildren().addAll(titre, desc, boxCherche, boxDemande);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  SIGNALEMENT
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerSignalement() {
        VBox panel = new VBox(15); panel.setPadding(new Insets(25));
        Label titre = new Label("🔧 Signaler un Problème Technique");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        Label desc = new Label("Votre signalement sera transmis directement au gestionnaire.");
        desc.setStyle("-fx-font-size:12;-fx-text-fill:#555;"); desc.setWrapText(true);

        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(16));
        grid.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        ComboBox<Salle> cbSalle = new ComboBox<>(FXCollections.observableArrayList(salleDAO.obtenirTous()));
        cbSalle.setPromptText("Sélectionner une salle"); cbSalle.setPrefWidth(280);
        cbSalle.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Salle s, boolean e){super.updateItem(s,e);setText(e||s==null?null:s.getNumero()+" — "+s.getBatiment());} });
        cbSalle.setButtonCell(new ListCell<>() { @Override protected void updateItem(Salle s, boolean e){super.updateItem(s,e);setText(e||s==null?"Sélectionner une salle":s.getNumero()+" — "+s.getBatiment());} });

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Problème électrique","Vidéoprojecteur défaillant","Tableau interactif défaillant","Climatisation","Mobilier cassé","Autre");
        cbType.setPromptText("Type de problème"); cbType.setPrefWidth(280);
        TextArea taDesc = new TextArea(); taDesc.setPromptText("Décrivez le problème..."); taDesc.setPrefHeight(100); taDesc.setWrapText(true);

        grid.add(new Label("Salle :"),     0,0); grid.add(cbSalle, 1,0);
        grid.add(new Label("Problème :"),  0,1); grid.add(cbType,  1,1);
        grid.add(new Label("Description :"),0,2);grid.add(taDesc,  1,2);

        Label msgS = new Label(""); msgS.setStyle("-fx-font-size:12;"); msgS.setWrapText(true);
        Button btnEnvoyer = new Button("📤 Envoyer au gestionnaire");
        btnEnvoyer.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-padding:10 20;-fx-font-weight:bold;");
        btnEnvoyer.setOnAction(e -> {
            if (cbSalle.getValue()==null||cbType.getValue()==null||taDesc.getText().isEmpty()) { msgS.setText("⚠️ Remplissez tous les champs."); msgS.setStyle("-fx-text-fill:#e67e22;"); return; }
            String sujet = "[Signalement] " + cbType.getValue() + " — Salle " + cbSalle.getValue().getNumero();
            String corps = "De : " + utilisateur.getNomComplet() + "\nSalle : " + cbSalle.getValue().getNumero() + " — " + cbSalle.getValue().getBatiment() + "\nProblème : " + cbType.getValue() + "\n\n" + taDesc.getText().trim();
            Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(), utilisateur.getRole(), sujet, corps, "RECLAMATION", false, null);
            try { msgDAO.envoyer(msg); msgS.setText("✅ Signalement envoyé."); msgS.setStyle("-fx-text-fill:#27ae60;"); cbSalle.setValue(null); cbType.setValue(null); taDesc.clear(); }
            catch (Exception ex) { msgS.setText("❌ "+ex.getMessage()); msgS.setStyle("-fx-text-fill:#e74c3c;"); }
        });
        panel.getChildren().addAll(titre, desc, grid, btnEnvoyer, msgS);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  BOÎTE DE RÉCEPTION
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerBoiteReception() {
        VBox panel = new VBox(14); panel.setPadding(new Insets(18));
        Label titre = new Label("📬 Mes Messages");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        Label lblStats = new Label(nonLus > 0 ? nonLus + " message(s) non lu(s)" : "Tous les messages sont lus.");
        lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:"+(nonLus>0?"#e74c3c":"#27ae60")+";");

        ObservableList<Message> items = FXCollections.observableArrayList(
            msgDAO.obtenirPourUtilisateur(utilisateur.getId()));
        TableView<Message> table = new TableView<>(items);
        table.setPrefHeight(270);
        table.setPlaceholder(new Label("Aucun message reçu du gestionnaire."));

        DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        TableColumn<Message,String> cLu    = new TableColumn<>("");      cLu.setCellValueFactory(c->new SimpleStringProperty(c.getValue().isLu()?"":"🔵")); cLu.setPrefWidth(28);
        TableColumn<Message,String> cSujet = new TableColumn<>("Sujet"); cSujet.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getSujet())); cSujet.setPrefWidth(310);
        TableColumn<Message,String> cDate  = new TableColumn<>("Date");  cDate.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getCreatedAt().format(fmtDate))); cDate.setPrefWidth(90);
        table.getColumns().addAll(cLu, cSujet, cDate);

        // Zone de lecture
        VBox zoneDetail = new VBox(8); zoneDetail.setPadding(new Insets(12));
        zoneDetail.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");
        zoneDetail.setVisible(false);
        Label lblTitre = new Label(""); lblTitre.setStyle("-fx-font-size:14;-fx-font-weight:bold;");
        Label lblMeta  = new Label(""); lblMeta.setStyle("-fx-font-size:12;-fx-text-fill:#7f8c8d;");
        TextArea taMsg = new TextArea(); taMsg.setEditable(false); taMsg.setPrefHeight(110); taMsg.setWrapText(true);
        zoneDetail.getChildren().addAll(lblTitre, lblMeta, taMsg);

        table.getSelectionModel().selectedItemProperty().addListener((obs,old,sel) -> {
            if (sel==null) { zoneDetail.setVisible(false); return; }
            lblTitre.setText(sel.getSujet());
            lblMeta.setText("De : " + sel.getExpediteurNom() + "   •   " + sel.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
            taMsg.setText(sel.getCorps());
            zoneDetail.setVisible(true);
            if (!sel.isLu()) { msgDAO.marquerLu(sel.getId()); sel.setLu(true); table.refresh();
                int n = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
                lblStats.setText(n>0?n+" message(s) non lu(s)":"Tous les messages sont lus.");
                lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:"+(n>0?"#e74c3c":"#27ae60")+";");
            }
        });

        HBox barBtn = new HBox(10);
        Button btnRefresh  = new Button("🔄 Rafraîchir");
        btnRefresh.setOnAction(e -> { items.setAll(msgDAO.obtenirPourUtilisateur(utilisateur.getId())); int n=msgDAO.compterNonLusPourUtilisateur(utilisateur.getId()); lblStats.setText(n>0?n+" non lu(s)":"Tous lus."); });
        Button btnTousLus  = new Button("✔ Tout marquer lu");
        btnTousLus.setOnAction(e -> { msgDAO.marquerTousLusPourUtilisateur(utilisateur.getId()); items.setAll(msgDAO.obtenirPourUtilisateur(utilisateur.getId())); table.refresh(); lblStats.setText("Tous les messages sont lus."); lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#27ae60;"); });
        barBtn.getChildren().addAll(btnRefresh, btnTousLus);

        panel.getChildren().addAll(titre, lblStats, barBtn, table, zoneDetail);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    private VBox section(String titreSection) {
        VBox box = new VBox(10); box.setPadding(new Insets(14));
        box.setStyle("-fx-border-color:#bdc3c7;-fx-background-color:white;-fx-border-radius:6;");
        Label lbl = new Label(titreSection); lbl.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");
        box.getChildren().add(lbl);
        return box;
    }

    private String inverserNom(String n) { String[] p=n.trim().split("\\s+",2); return p.length==2?p[1]+" "+p[0]:n; }
}
