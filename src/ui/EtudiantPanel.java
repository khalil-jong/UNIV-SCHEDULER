package ui;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import dao.ClasseDAO;
import dao.EmploiDuTempsDAO;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
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
import models.Message;
import models.Salle;
import models.Utilisateur;

/**
 * Panel Étudiant — 2 fonctionnalités :
 *   1. Consulter l'emploi du temps de sa classe (grille hebdomadaire)
 *   2. Rechercher une salle libre pour étude (avec filtres)
 * + Boîte de réception (messages du gestionnaire)
 */
public class EtudiantPanel {

    private Utilisateur      utilisateur;
    private UnivSchedulerApp app;
    private SalleDAO         salleDAO = new SalleDAO();
    private EmploiDuTempsDAO edtDAO   = new EmploiDuTempsDAO();
    private ClasseDAO        classeDAO= new ClasseDAO();
    private MessageDAO       msgDAO   = new MessageDAO();
    private String           classeEtudiant = null;

    public EtudiantPanel(Utilisateur utilisateur, UnivSchedulerApp app) {
        this.utilisateur = utilisateur;
        this.app = app;
        // Pré-remplir la classe depuis le profil utilisateur (évite la sélection manuelle)
        if (utilisateur.hasClasse()) {
            this.classeEtudiant = utilisateur.getClasse();
        }
    }

    public BorderPane createPanel() {
        BorderPane root = new BorderPane();
        root.setTop(creerTopBar());
        root.setLeft(creerMenu(root));
        root.setCenter(creerAccueil(root));
        return root;
    }

    // ── Top bar ──────────────────────────────────────────────────
    private HBox creerTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color: #e67e22;");
        bar.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("UNIV-SCHEDULER  |  Étudiant");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        Label userLabel = new Label("👤 " + utilisateur.getNomComplet()
            + (nonLus > 0 ? "   🔴 " + nonLus + " msg" : ""));
        userLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13;");
        Button btnDeco = new Button("Déconnexion");
        btnDeco.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        btnDeco.setOnAction(e -> app.afficherLogin());
        bar.getChildren().addAll(titre, spacer, userLabel, new Label("   "), btnDeco);
        return bar;
    }

    // ── Menu ─────────────────────────────────────────────────────
    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(5);
        menu.setPadding(new Insets(15));
        menu.setPrefWidth(215);
        menu.setStyle("-fx-background-color: #f39c12;");

        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        String msgLabel = nonLus > 0 ? "📬 Mes messages  🔴 " + nonLus : "📬 Mes messages";

        String[][] items = {
            {"🏠 Accueil",               "accueil"},
            {"📅 Mon emploi du temps",   "edt"},
            {"📨 Réserver une salle",    "reservation"},
            {"🔧 Signaler un problème",  "signalement"},
            {"🔍 Chercher salle libre",  "salle"},
            {msgLabel,                   "messages"}
        };

        for (String[] item : items) {
            Button btn = new Button(item[0]);
            btn.setPrefWidth(190); btn.setPrefHeight(40);
            String sN = "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;";
            String sH = "-fx-background-color: #e67e22;    -fx-text-fill: white; -fx-font-size: 13; -fx-alignment: CENTER-LEFT; -fx-font-weight: bold;";
            btn.setStyle(sN);
            btn.setOnMouseEntered(e -> btn.setStyle(sH));
            btn.setOnMouseExited(e  -> btn.setStyle(sN));
            btn.setOnAction(e -> {
                switch (item[1]) {
                    case "accueil":   root.setCenter(creerAccueil(root));        break;
                    case "edt":       root.setCenter(creerMonEmploiDuTemps());   break;
                    case "reservation": root.setCenter(new ReservationPanel(utilisateur).createPanel()); break;
                    case "signalement": root.setCenter(creerSignalement());      break;
                    case "salle":     root.setCenter(creerRechercheSalle());     break;
                    case "messages":  root.setCenter(creerBoiteReception());     break;
                }
            });
            menu.getChildren().add(btn);
        }
        return menu;
    }

    // ════════════════════════════════════════════════════════════
    //  ACCUEIL
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerAccueil(BorderPane root) {
        VBox panel = new VBox(20); panel.setPadding(new Insets(30));
        Label titre = new Label("Bonjour, " + utilisateur.getNomComplet() + " 👋");
        titre.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");
        Label role = new Label("Rôle : Étudiant");
        role.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        // Badge non lus
        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        if (nonLus > 0) {
            Label lMsg = new Label("📬 Vous avez " + nonLus + " message(s) non lu(s) du gestionnaire.");
            lMsg.setStyle("-fx-font-size:13;-fx-text-fill:#e74c3c;-fx-font-weight:bold;-fx-padding:8 12;-fx-background-color:#fdecea;-fx-border-color:#e74c3c;-fx-border-radius:6;-fx-background-radius:6;");
            panel.getChildren().addAll(titre, role, lMsg);
        } else {
            panel.getChildren().addAll(titre, role);
        }

        // Sélection de classe
        if (classeEtudiant == null) {
            VBox boxChoix = new VBox(10); boxChoix.setPadding(new Insets(18));
            boxChoix.setStyle("-fx-background-color:#fef9e7;-fx-border-color:#f39c12;-fx-border-radius:8;-fx-background-radius:8;");
            Label lChoix = new Label("📚 Sélectionnez votre classe :");
            lChoix.setStyle("-fx-font-size:14;-fx-font-weight:bold;");
            ComboBox<String> cbClasse = new ComboBox<>();
            List<String> classes = classeDAO.obtenirNomsClasses();
            if (classes.isEmpty()) {
				classes = edtDAO.obtenirToutesLesClasses();
			}
            cbClasse.getItems().addAll(classes);
            cbClasse.setPromptText("Choisir ma classe..."); cbClasse.setPrefWidth(250);
            Button btnVal = new Button("✅ Valider");
            btnVal.setStyle("-fx-background-color:#e67e22;-fx-text-fill:white;-fx-padding:8 18;-fx-font-weight:bold;");
            btnVal.setOnAction(e -> { if (cbClasse.getValue()!=null) { classeEtudiant=cbClasse.getValue(); root.setCenter(creerMonEmploiDuTemps()); } });
            boxChoix.getChildren().addAll(lChoix, new HBox(10,cbClasse,btnVal));
            panel.getChildren().add(boxChoix);
        } else {
            Label lCl = new Label("📚 Votre classe : " + classeEtudiant);
            lCl.setStyle("-fx-font-size:14;-fx-text-fill:#e67e22;-fx-font-weight:bold;");
            Button btnChanger = new Button("Changer de classe");
            btnChanger.setStyle("-fx-background-color:transparent;-fx-text-fill:#e67e22;-fx-underline:true;");
            btnChanger.setOnAction(e -> { classeEtudiant=null; root.setCenter(creerAccueil(root)); });
            panel.getChildren().addAll(lCl, btnChanger);
        }
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  EMPLOI DU TEMPS
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerMonEmploiDuTemps() {
        if (classeEtudiant == null) {
            VBox vb = new VBox(new Label("Choisissez votre classe depuis l'accueil."));
            vb.setPadding(new Insets(30));
            ScrollPane sp = new ScrollPane(vb); sp.setFitToWidth(true); return sp;
        }
        return new EmploiDuTempsViewPanel(classeEtudiant).createPanel();
    }

    // ════════════════════════════════════════════════════════════
    //  SIGNALEMENT
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerSignalement() {
        VBox panel = new VBox(15); panel.setPadding(new Insets(25));
        Label titre = new Label("🔧 Signaler un Problème Technique");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        Label desc = new Label("Votre signalement sera transmis directement au gestionnaire dans sa boîte de réception.");
        desc.setStyle("-fx-font-size:12;-fx-text-fill:#555;"); desc.setWrapText(true);

        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(16));
        grid.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        ComboBox<Salle> cbSalle = new ComboBox<>(FXCollections.observableArrayList(salleDAO.obtenirTous()));
        cbSalle.setPromptText("Sélectionner une salle"); cbSalle.setPrefWidth(280);
        cbSalle.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Salle s, boolean e) {
                super.updateItem(s, e); setText(e || s == null ? null : s.getNumero() + " — " + s.getBatiment());
            }
        });
        cbSalle.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Salle s, boolean e) {
                super.updateItem(s, e); setText(e || s == null ? "Sélectionner une salle" : s.getNumero() + " — " + s.getBatiment());
            }
        });

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Problème électrique", "Vidéoprojecteur défaillant",
            "Tableau interactif défaillant", "Climatisation", "Mobilier cassé", "Autre");
        cbType.setPromptText("Type de problème"); cbType.setPrefWidth(280);

        TextArea taDesc = new TextArea(); taDesc.setPromptText("Décrivez le problème en détail...");
        taDesc.setPrefHeight(100); taDesc.setWrapText(true);

        grid.add(new Label("Salle :"),      0, 0); grid.add(cbSalle, 1, 0);
        grid.add(new Label("Problème :"),   0, 1); grid.add(cbType,  1, 1);
        grid.add(new Label("Description :"),0, 2); grid.add(taDesc,  1, 2);

        Label msgS = new Label(""); msgS.setStyle("-fx-font-size:12;"); msgS.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer au gestionnaire");
        btnEnvoyer.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-padding:10 20;-fx-font-weight:bold;");
        btnEnvoyer.setOnAction(e -> {
            if (cbSalle.getValue() == null || cbType.getValue() == null || taDesc.getText().isEmpty()) {
                msgS.setText("⚠️ Remplissez tous les champs."); msgS.setStyle("-fx-text-fill:#e67e22;"); return;
            }
            String sujet = "[Signalement] " + cbType.getValue() + " — Salle " + cbSalle.getValue().getNumero();
            String corps = "Signalement de : " + utilisateur.getNomComplet() + " (Étudiant)\n"
                + "Salle : " + cbSalle.getValue().getNumero() + " — " + cbSalle.getValue().getBatiment() + "\n"
                + "Problème : " + cbType.getValue() + "\n\n" + taDesc.getText().trim();
            models.Message msg = new models.Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, corps, "RECLAMATION", false, null);
            try {
                msgDAO.envoyer(msg);
                msgS.setText("✅ Signalement envoyé au gestionnaire.");
                msgS.setStyle("-fx-text-fill:#27ae60;");
                cbSalle.setValue(null); cbType.setValue(null); taDesc.clear();
            } catch (Exception ex) { msgS.setText("❌ " + ex.getMessage()); msgS.setStyle("-fx-text-fill:#e74c3c;"); }
        });

        panel.getChildren().addAll(titre, desc, grid, btnEnvoyer, msgS);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  RECHERCHE SALLE LIBRE POUR ÉTUDE
    // ════════════════════════════════════════════════════════════
    private ScrollPane creerRechercheSalle() {
        VBox panel = new VBox(18); panel.setPadding(new Insets(20));
        Label titre = new Label("🔍 Rechercher une Salle Libre pour Étude");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        Label desc = new Label("Trouvez une salle disponible maintenant ou à une heure donnée.");
        desc.setStyle("-fx-font-size:12;-fx-text-fill:#555;");

        // Filtres
        VBox boxFiltres = new VBox(10); boxFiltres.setPadding(new Insets(14));
        boxFiltres.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        // Bouton "Maintenant"
        Button btnMaintenant = new Button("⚡ Salles libres maintenant");
        btnMaintenant.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-padding:8 18;-fx-font-weight:bold;");

        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(10);
        Spinner<Integer> spH   = new Spinner<>(7,22,LocalTime.now().getHour()); spH.setPrefWidth(75);
        Spinner<Integer> spMin = new Spinner<>(0,59,0,5); spMin.setPrefWidth(75);
        Spinner<Integer> spDur = new Spinner<>(15,300,60,15); spDur.setPrefWidth(85);
        Spinner<Integer> spCap = new Spinner<>(1,500,1); spCap.setPrefWidth(85);
        Label lblCapNote = new Label("(nombre de personnes)"); lblCapNote.setStyle("-fx-font-size:10;-fx-text-fill:#aaa;");
        ComboBox<String> cbType = new ComboBox<>(); cbType.getItems().addAll("Tous","TD","TP","Amphi"); cbType.setValue("Tous"); cbType.setPrefWidth(110);
        CheckBox chkV = new CheckBox("📽 Vidéoprojecteur");
        CheckBox chkT = new CheckBox("🖥 Tableau interactif");
        CheckBox chkC = new CheckBox("❄ Climatisation");

        grid.add(new Label("Heure :"),          0,0); grid.add(new HBox(5,spH,new Label("h"),spMin,new Label("min")), 1,0);
        grid.add(new Label("Durée (min) :"),    0,1); grid.add(spDur,   1,1);
        grid.add(new Label("Nb de personnes :"),0,2); grid.add(new HBox(6,spCap,lblCapNote), 1,2);
        grid.add(new Label("Type de salle :"),  0,3); grid.add(cbType,  1,3);
        grid.add(new Label("Équipements :"),    0,4); grid.add(new HBox(14,chkV,chkT,chkC), 1,4);

        Button btnChercher = new Button("🔍 Rechercher");
        btnChercher.setStyle("-fx-background-color:#e67e22;-fx-text-fill:white;-fx-padding:8 18;-fx-font-weight:bold;");

        boxFiltres.getChildren().addAll(btnMaintenant, new Separator(), grid, btnChercher);

        // Résultats
        Label lblRes = new Label(""); lblRes.setStyle("-fx-font-size:12;-fx-font-weight:bold;"); lblRes.setWrapText(true);
        TableView<Salle> table = new TableView<>();
        table.setPrefHeight(300);
        table.setPlaceholder(new Label("Lancez une recherche pour voir les salles disponibles."));

        TableColumn<Salle,String>  cNum  = new TableColumn<>("N°");       cNum.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getNumero())); cNum.setPrefWidth(60);
        TableColumn<Salle,String>  cBat  = new TableColumn<>("Bâtiment"); cBat.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getBatiment())); cBat.setPrefWidth(130);
        TableColumn<Salle,String>  cEtg  = new TableColumn<>("Étage");    cEtg.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getEtage())); cEtg.setPrefWidth(90);
        TableColumn<Salle,Integer> cCap  = new TableColumn<>("Cap.");     cCap.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().getCapacite())); cCap.setPrefWidth(55);
        TableColumn<Salle,String>  cType = new TableColumn<>("Type");     cType.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getType())); cType.setPrefWidth(60);
        TableColumn<Salle,String>  cEq   = new TableColumn<>("Équipements"); cEq.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getEquipementsStr())); cEq.setPrefWidth(160);
        table.getColumns().addAll(cNum,cBat,cEtg,cCap,cType,cEq);

        // Logique de recherche
        Runnable chercher = () -> {
            LocalDateTime debut = LocalDateTime.now().withHour(spH.getValue()).withMinute(spMin.getValue()).withSecond(0);
            List<Salle> dispo = salleDAO.obtenirSallesDisponibles(debut, spDur.getValue());
            dispo = dispo.stream().filter(s -> s.getCapacite() >= spCap.getValue()).collect(Collectors.toList());
            if (!cbType.getValue().equals("Tous")) {
				dispo = dispo.stream().filter(s->s.getType().equals(cbType.getValue())).collect(Collectors.toList());
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
            table.setItems(FXCollections.observableArrayList(dispo));
            lblRes.setText(dispo.isEmpty()
                ? "❌ Aucune salle disponible pour ces critères."
                : "✅ " + dispo.size() + " salle(s) disponible(s).");
            lblRes.setStyle(dispo.isEmpty() ? "-fx-text-fill:#e74c3c;" : "-fx-text-fill:#27ae60;");
        };

        btnMaintenant.setOnAction(e -> {
            spH.getValueFactory().setValue(LocalTime.now().getHour());
            spMin.getValueFactory().setValue(0);
            chercher.run();
        });
        btnChercher.setOnAction(e -> chercher.run());

        // Tooltip sur chaque salle sélectionnée
        table.getSelectionModel().selectedItemProperty().addListener((obs,old,sel) -> {
            if (sel==null) {
				return;
			}
            Tooltip tip = new Tooltip("Salle " + sel.getNumero() + " — " + sel.getBatiment() + "\nCapacité: "+sel.getCapacite()+" | Type: "+sel.getType()+"\nÉquipements: "+sel.getEquipementsStr());
            Tooltip.install(table, tip);
        });

        panel.getChildren().addAll(titre, desc, boxFiltres, lblRes, table);
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
        Label lblStats = new Label(nonLus>0?nonLus+" message(s) non lu(s)":"Tous les messages sont lus.");
        lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:"+(nonLus>0?"#e74c3c":"#27ae60")+";");

        ObservableList<Message> items = FXCollections.observableArrayList(
            msgDAO.obtenirPourUtilisateur(utilisateur.getId()));
        TableView<Message> table = new TableView<>(items);
        table.setPrefHeight(270);
        table.setPlaceholder(new Label("Aucun message reçu."));

        DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        TableColumn<Message,String> cLu    = new TableColumn<>("");      cLu.setCellValueFactory(c->new SimpleStringProperty(c.getValue().isLu()?"":"🔵")); cLu.setPrefWidth(28);
        TableColumn<Message,String> cSujet = new TableColumn<>("Sujet"); cSujet.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getSujet())); cSujet.setPrefWidth(330);
        TableColumn<Message,String> cDate  = new TableColumn<>("Date");  cDate.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getCreatedAt().format(fmtDate))); cDate.setPrefWidth(90);
        table.getColumns().addAll(cLu, cSujet, cDate);

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
                int n=msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
                lblStats.setText(n>0?n+" non lu(s)":"Tous lus."); lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:"+(n>0?"#e74c3c":"#27ae60")+";");
            }
        });

        Button btnRefresh = new Button("🔄 Rafraîchir");
        btnRefresh.setOnAction(e -> { items.setAll(msgDAO.obtenirPourUtilisateur(utilisateur.getId())); int n=msgDAO.compterNonLusPourUtilisateur(utilisateur.getId()); lblStats.setText(n>0?n+" non lu(s)":"Tous lus."); });
        Button btnTousLus = new Button("✔ Tout marquer lu");
        btnTousLus.setOnAction(e -> { msgDAO.marquerTousLusPourUtilisateur(utilisateur.getId()); items.setAll(msgDAO.obtenirPourUtilisateur(utilisateur.getId())); lblStats.setText("Tous les messages sont lus."); lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#27ae60;"); });

        panel.getChildren().addAll(titre, lblStats, new HBox(10,btnRefresh,btnTousLus), table, zoneDetail);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }
}
