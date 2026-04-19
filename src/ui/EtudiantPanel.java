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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Message;
import models.Salle;
import models.Utilisateur;

public class EtudiantPanel {

    private Utilisateur      utilisateur;
    private UnivSchedulerApp app;
    private SalleDAO         salleDAO  = new SalleDAO();
    private EmploiDuTempsDAO edtDAO    = new EmploiDuTempsDAO();
    private ClasseDAO        classeDAO = new ClasseDAO();
    private MessageDAO       msgDAO    = new MessageDAO();
    private String           classeEtudiant = null;

    public EtudiantPanel(Utilisateur utilisateur, UnivSchedulerApp app) {
        this.utilisateur = utilisateur;
        this.app = app;
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

    private HBox creerTopBar() {
        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        String nom = utilisateur.getNomComplet() + (nonLus > 0 ? "   🔴 " + nonLus : "");
        return Design.topBar("Étudiant", nom, Design.ETU_PRIMARY, () -> app.afficherLogin());
    }

    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(2);
        menu.setPadding(new Insets(12, 10, 12, 10));
        menu.setPrefWidth(220);
        menu.setStyle("-fx-background-color: " + Design.ETU_MENU_BG + ";");

        // Avatar
        VBox avatar = new VBox(4);
        avatar.setAlignment(Pos.CENTER);
        avatar.setPadding(new Insets(14, 0, 16, 0));
        Label ico = new Label("🎓"); ico.setStyle("-fx-font-size: 28;");
        Label nom = new Label(utilisateur.getNomComplet());
        nom.setStyle("-fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold;");
        Label classe = classeEtudiant != null ? new Label(classeEtudiant) : new Label("Classe non définie");
        classe.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 10;");
        Label tag = new Label("ÉTUDIANT");
        tag.setStyle("-fx-text-fill: " + Design.ETU_ACCENT + "; -fx-font-size: 9; -fx-font-weight: bold;" +
            "-fx-padding: 2 8; -fx-background-color: rgba(243,156,18,0.20); -fx-background-radius: 10;");
        avatar.getChildren().addAll(ico, nom, classe, tag);
        menu.getChildren().add(avatar);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.12);");
        menu.getChildren().add(sep);

        menu.getChildren().add(Design.menuTitle("Mon Espace"));
        ajouterBtn(menu, "🏠  Accueil",              root, () -> creerAccueil(root));
        ajouterBtn(menu, "📅  Mon emploi du temps",  root, () -> creerMonEmploiDuTemps());

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: rgba(255,255,255,0.12);");
        menu.getChildren().add(sep2);

        menu.getChildren().add(Design.menuTitle("Services"));
        ajouterBtn(menu, "📨  Réserver une salle",   root, () -> new ReservationPanel(utilisateur).createPanel());
        ajouterBtn(menu, "🔧  Signaler un problème", root, () -> creerSignalement());
        ajouterBtn(menu, "🔍  Chercher salle libre",  root, () -> creerRechercheSalle());

        Separator sep3 = new Separator();
        sep3.setStyle("-fx-background-color: rgba(255,255,255,0.12);");
        menu.getChildren().add(sep3);

        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        String msgLabel = nonLus > 0 ? "📬  Messages  🔴 " + nonLus : "📬  Mes messages";
        ajouterBtn(menu, msgLabel, root, () -> creerBoiteReception());

        return menu;
    }

    private void ajouterBtn(VBox menu, String label, BorderPane root,
                             java.util.function.Supplier<javafx.scene.Parent> p) {
        Button btn = Design.menuBtn(label, Design.ETU_HOVER);
        btn.setOnAction(e -> root.setCenter(p.get()));
        menu.getChildren().add(btn);
    }

    // ════════════════════════════════════════════════════════════════
    //  ACCUEIL
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerAccueil(BorderPane root) {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        // Bannière de bienvenue
        HBox welcome = new HBox(16);
        welcome.setPadding(new Insets(22, 24, 22, 24));
        welcome.setAlignment(Pos.CENTER_LEFT);
        welcome.setStyle(
            "-fx-background-color: linear-gradient(to right, " + Design.ETU_PRIMARY + ", " + Design.ETU_MENU_BG + ");" +
            "-fx-background-radius: 14;"
        );
        Label ico = new Label("🎓"); ico.setStyle("-fx-font-size: 36;");
        VBox info = new VBox(4);
        Label greet = new Label("Bonjour, " + utilisateur.getNomComplet() + " 👋");
        greet.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sub = new Label("Étudiant" + (classeEtudiant != null ? "  •  " + classeEtudiant : "  •  Classe non définie"));
        sub.setStyle("-fx-font-size: 13; -fx-text-fill: rgba(255,255,255,0.75);");
        info.getChildren().addAll(greet, sub);
        welcome.getChildren().addAll(ico, info);
        panel.getChildren().add(welcome);

        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        if (nonLus > 0) {
            Label lMsg = new Label("📬  Vous avez " + nonLus + " message(s) non lu(s) du gestionnaire.");
            lMsg.setStyle("-fx-font-size:13;-fx-text-fill:#e74c3c;-fx-font-weight:bold;-fx-padding:10 14;-fx-background-color:#fdecea;-fx-border-color:#e74c3c;-fx-border-radius:8;-fx-background-radius:8;");
            panel.getChildren().add(lMsg);
        }

        // Sélection / affichage classe
        if (classeEtudiant == null) {
            VBox boxChoix = new VBox(12);
            boxChoix.setPadding(new Insets(20));
            boxChoix.setStyle(Design.CARD_STYLE);
            Label lChoix = new Label("📚  Sélectionnez votre classe pour accéder à votre emploi du temps :");
            lChoix.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";");
            ComboBox<String> cbClasse = new ComboBox<>();
            List<String> classes = classeDAO.obtenirNomsClasses();
            if (classes.isEmpty()) {
				classes = edtDAO.obtenirToutesLesClasses();
			}
            cbClasse.getItems().addAll(classes);
            cbClasse.setPromptText("Choisir ma classe..."); cbClasse.setPrefWidth(260);
            Button btnVal = Design.btnPrimary("✅  Valider", Design.ETU_ACCENT);
            btnVal.setOnAction(e -> {
                if (cbClasse.getValue() != null) {
                    classeEtudiant = cbClasse.getValue();
                    root.setCenter(creerMonEmploiDuTemps());
                }
            });
            boxChoix.getChildren().addAll(lChoix, new HBox(10, cbClasse, btnVal));
            panel.getChildren().add(boxChoix);
        } else {
            HBox classeBox = new HBox(12);
            classeBox.setPadding(new Insets(14, 18, 14, 18));
            classeBox.setAlignment(Pos.CENTER_LEFT);
            classeBox.setStyle(Design.CARD_STYLE);
            Label lCl = new Label("📚  Votre classe : " + classeEtudiant);
            lCl.setStyle("-fx-font-size:14;-fx-text-fill:" + Design.ETU_ACCENT + ";-fx-font-weight:bold;");
            Button btnChanger = new Button("Changer");
            btnChanger.setStyle("-fx-background-color:transparent;-fx-text-fill:" + Design.TEXT_MUTED + ";-fx-underline:true;-fx-cursor:hand;");
            btnChanger.setOnAction(e -> { classeEtudiant = null; root.setCenter(creerAccueil(root)); });
            Region sp = new javafx.scene.layout.Region(); HBox.setHgrow(sp, javafx.scene.layout.Priority.ALWAYS);
            classeBox.getChildren().addAll(lCl, sp, btnChanger);
            panel.getChildren().add(classeBox);
        }

        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    private ScrollPane creerMonEmploiDuTemps() {
        if (classeEtudiant == null) {
            VBox vb = new VBox(new Label("Choisissez votre classe depuis l'accueil."));
            vb.setPadding(new Insets(30));
            ScrollPane sp = new ScrollPane(vb); sp.setFitToWidth(true); return sp;
        }
        return new EmploiDuTempsViewPanel(classeEtudiant).createPanel();
    }

    // ════════════════════════════════════════════════════════════════
    //  SIGNALEMENT
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerSignalement() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");
        Label titre = Design.pageTitle("🔧  Signaler un Problème Technique");
        Label desc  = Design.muted("Votre signalement sera transmis directement au gestionnaire dans sa boîte de réception.");
        panel.getChildren().addAll(titre, desc);

        VBox formBox = Design.section("📝  Formulaire de signalement");
        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(10,0,0,0));

        ComboBox<Salle> cbSalle = new ComboBox<>(FXCollections.observableArrayList(salleDAO.obtenirTous()));
        cbSalle.setPromptText("Sélectionner une salle"); cbSalle.setPrefWidth(300);
        cbSalle.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(Salle s, boolean e){super.updateItem(s,e);setText(e||s==null?null:s.getNumero()+" — "+s.getBatiment());} });
        cbSalle.setButtonCell(new ListCell<>() { @Override protected void updateItem(Salle s, boolean e){super.updateItem(s,e);setText(e||s==null?"Sélectionner une salle":s.getNumero()+" — "+s.getBatiment());} });

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Problème électrique","Vidéoprojecteur défaillant","Tableau interactif défaillant","Climatisation","Mobilier cassé","Autre");
        cbType.setPromptText("Type de problème"); cbType.setPrefWidth(300);
        TextArea taDesc = new TextArea(); taDesc.setPromptText("Décrivez le problème en détail..."); taDesc.setPrefHeight(100); taDesc.setWrapText(true); taDesc.setStyle(Design.INPUT_STYLE);

        grid.add(fl("Salle :"),      0,0); grid.add(cbSalle, 1,0);
        grid.add(fl("Problème :"),   0,1); grid.add(cbType,  1,1);
        grid.add(fl("Description :"),0,2); grid.add(taDesc,  1,2);

        Label msgS = new Label(""); msgS.setWrapText(true);
        Button btnEnvoyer = Design.btnDanger("📤  Envoyer au gestionnaire");
        btnEnvoyer.setOnAction(e -> {
            if (cbSalle.getValue()==null||cbType.getValue()==null||taDesc.getText().isEmpty()) { setMsg(msgS,"⚠️  Remplissez tous les champs.",Design.WARNING); return; }
            String sujet = "[Signalement] " + cbType.getValue() + " — Salle " + cbSalle.getValue().getNumero();
            String corps = "Signalement de : " + utilisateur.getNomComplet() + " (Étudiant)\nSalle : " + cbSalle.getValue().getNumero() + " — " + cbSalle.getValue().getBatiment() + "\nProblème : " + cbType.getValue() + "\n\n" + taDesc.getText().trim();
            models.Message msg = new models.Message(0, utilisateur.getId(), utilisateur.getNomComplet(), utilisateur.getRole(), sujet, corps, "RECLAMATION", false, null);
            try { msgDAO.envoyer(msg); setMsg(msgS,"✅  Signalement envoyé.",Design.SUCCESS); cbSalle.setValue(null); cbType.setValue(null); taDesc.clear(); }
            catch (Exception ex) { setMsg(msgS,"❌  "+ex.getMessage(),Design.DANGER); }
        });
        formBox.getChildren().addAll(grid, btnEnvoyer, msgS);
        panel.getChildren().add(formBox);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════
    //  RECHERCHE SALLE LIBRE
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerRechercheSalle() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");
        Label titre = Design.pageTitle("🔍  Rechercher une Salle Libre");
        Label desc  = Design.muted("Trouvez une salle disponible maintenant ou à une heure donnée.");
        panel.getChildren().addAll(titre, desc);

        VBox boxFiltres = Design.section("⚙️  Critères de recherche");
        Button btnMaintenant = Design.btnPrimary("⚡  Salles libres maintenant", Design.SUCCESS);

        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(12); grid.setPadding(new Insets(10,0,0,0));
        Spinner<Integer> spH   = new Spinner<>(7,22,LocalTime.now().getHour()); spH.setPrefWidth(75);
        Spinner<Integer> spMin = new Spinner<>(0,59,0,5); spMin.setPrefWidth(75);
        Spinner<Integer> spDur = new Spinner<>(15,300,60,15); spDur.setPrefWidth(85);
        Spinner<Integer> spCap = new Spinner<>(1,500,1); spCap.setPrefWidth(85);
        ComboBox<String> cbType = new ComboBox<>(); cbType.getItems().addAll("Tous","TD","TP","Amphi"); cbType.setValue("Tous"); cbType.setPrefWidth(110);
        CheckBox chkV = new CheckBox("📽  Vidéoprojecteur");
        CheckBox chkT = new CheckBox("🖥  Tableau interactif");
        CheckBox chkC = new CheckBox("❄  Climatisation");

        grid.add(fl("Heure :"),           0,0); grid.add(new HBox(5,spH,new Label("h"),spMin,new Label("min")), 1,0);
        grid.add(fl("Durée (min) :"),     0,1); grid.add(spDur, 1,1);
        grid.add(fl("Nb de personnes :"), 0,2); grid.add(spCap, 1,2);
        grid.add(fl("Type de salle :"),   0,3); grid.add(cbType, 1,3);
        grid.add(fl("Équipements :"),     0,4); grid.add(new HBox(14,chkV,chkT,chkC), 1,4);

        Button btnChercher = Design.btnPrimary("🔍  Rechercher", Design.ETU_ACCENT);
        Separator sepBtn = new Separator(); sepBtn.setStyle("-fx-background-color:rgba(0,0,0,0.08);");
        boxFiltres.getChildren().addAll(btnMaintenant, sepBtn, grid, btnChercher);

        Label lblRes = new Label(""); lblRes.setStyle("-fx-font-size:12;-fx-font-weight:bold;"); lblRes.setWrapText(true);
        TableView<Salle> table = new TableView<>();
        table.setPrefHeight(300);
        table.setStyle("-fx-border-color:#e8ecf5;-fx-border-radius:8;");
        table.setPlaceholder(new Label("Lancez une recherche pour voir les salles disponibles."));

        TableColumn<Salle,String>  cNum  = new TableColumn<>("N°");       cNum.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getNumero())); cNum.setPrefWidth(60);
        TableColumn<Salle,String>  cBat  = new TableColumn<>("Bâtiment"); cBat.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getBatiment())); cBat.setPrefWidth(130);
        TableColumn<Salle,String>  cEtg  = new TableColumn<>("Étage");    cEtg.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getEtage())); cEtg.setPrefWidth(90);
        TableColumn<Salle,Integer> cCap  = new TableColumn<>("Cap.");     cCap.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().getCapacite())); cCap.setPrefWidth(55);
        TableColumn<Salle,String>  cType = new TableColumn<>("Type");     cType.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getType())); cType.setPrefWidth(60);
        TableColumn<Salle,String>  cEq   = new TableColumn<>("Équipements"); cEq.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getEquipementsStr())); cEq.setPrefWidth(160);
        table.getColumns().addAll(cNum,cBat,cEtg,cCap,cType,cEq);

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
            lblRes.setText(dispo.isEmpty() ? "❌  Aucune salle disponible pour ces critères." : "✅  " + dispo.size() + " salle(s) disponible(s).");
            lblRes.setStyle(dispo.isEmpty() ? "-fx-text-fill:"+Design.DANGER+";" : "-fx-text-fill:"+Design.SUCCESS+";");
        };

        btnMaintenant.setOnAction(e -> { spH.getValueFactory().setValue(LocalTime.now().getHour()); spMin.getValueFactory().setValue(0); chercher.run(); });
        btnChercher.setOnAction(e -> chercher.run());

        table.getSelectionModel().selectedItemProperty().addListener((obs,old,sel) -> {
            if (sel==null) {
				return;
			}
            Tooltip.install(table, new Tooltip("Salle "+sel.getNumero()+" — "+sel.getBatiment()+"\nCap: "+sel.getCapacite()+" | Type: "+sel.getType()+"\n"+sel.getEquipementsStr()));
        });

        panel.getChildren().addAll(boxFiltres, lblRes, table);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════
    //  BOÎTE DE RÉCEPTION
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerBoiteReception() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");
        Label titre = Design.pageTitle("📬  Mes Messages");

        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        Label lblStats = new Label(nonLus>0?"🔵  "+nonLus+" message(s) non lu(s)":"✅  Tous les messages sont lus.");
        lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;-fx-background-radius:8;-fx-text-fill:"+(nonLus>0?Design.DANGER:Design.SUCCESS)+";-fx-background-color:"+(nonLus>0?"#fdecea":"#e8faf5")+";");

        ObservableList<Message> items = FXCollections.observableArrayList(msgDAO.obtenirPourUtilisateur(utilisateur.getId()));
        TableView<Message> table = new TableView<>(items);
        table.setPrefHeight(260);
        table.setStyle("-fx-border-color:#e8ecf5;-fx-border-radius:8;");
        table.setPlaceholder(new Label("Aucun message reçu."));

        DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        TableColumn<Message,String> cLu    = new TableColumn<>("");     cLu.setCellValueFactory(c->new SimpleStringProperty(c.getValue().isLu()?"":"🔵")); cLu.setPrefWidth(30);
        TableColumn<Message,String> cSujet = new TableColumn<>("Sujet");cSujet.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getSujet())); cSujet.setPrefWidth(340);
        TableColumn<Message,String> cDate  = new TableColumn<>("Date"); cDate.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getCreatedAt().format(fmtDate))); cDate.setPrefWidth(90);
        table.getColumns().addAll(cLu, cSujet, cDate);

        VBox zoneDetail = new VBox(10);
        zoneDetail.setPadding(new Insets(14));
        zoneDetail.setStyle(Design.SECTION_STYLE);
        zoneDetail.setVisible(false);
        Label lblTitre = new Label(""); lblTitre.setStyle("-fx-font-size:14;-fx-font-weight:bold;");
        Label lblMeta  = new Label(""); lblMeta.setStyle("-fx-font-size:12;-fx-text-fill:"+Design.TEXT_MUTED+";");
        TextArea taMsg = new TextArea(); taMsg.setEditable(false); taMsg.setPrefHeight(110); taMsg.setWrapText(true); taMsg.setStyle(Design.INPUT_STYLE);
        zoneDetail.getChildren().addAll(lblTitre, lblMeta, taMsg);

        table.getSelectionModel().selectedItemProperty().addListener((obs,old,sel) -> {
            if (sel==null) { zoneDetail.setVisible(false); return; }
            lblTitre.setText(sel.getSujet());
            lblMeta.setText("De : "+sel.getExpediteurNom()+"   •   "+sel.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
            taMsg.setText(sel.getCorps()); zoneDetail.setVisible(true);
            if (!sel.isLu()) {
                msgDAO.marquerLu(sel.getId()); sel.setLu(true); table.refresh();
                int n=msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
                lblStats.setText(n>0?"🔵  "+n+" non lu(s)":"✅  Tous lus.");
                lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;-fx-background-radius:8;-fx-text-fill:"+(n>0?Design.DANGER:Design.SUCCESS)+";-fx-background-color:"+(n>0?"#fdecea":"#e8faf5")+";");
            }
        });

        Button btnRefresh = Design.btnSecondary("🔄  Rafraîchir");
        btnRefresh.setOnAction(e -> items.setAll(msgDAO.obtenirPourUtilisateur(utilisateur.getId())));
        Button btnTousLus = Design.btnPrimary("✔  Tout marquer lu", Design.SUCCESS);
        btnTousLus.setOnAction(e -> {
            msgDAO.marquerTousLusPourUtilisateur(utilisateur.getId());
            items.setAll(msgDAO.obtenirPourUtilisateur(utilisateur.getId()));
            lblStats.setText("✅  Tous les messages sont lus.");
            lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;-fx-background-radius:8;-fx-text-fill:"+Design.SUCCESS+";-fx-background-color:#e8faf5;");
        });

        panel.getChildren().addAll(titre, lblStats, new HBox(10,btnRefresh,btnTousLus), table, zoneDetail);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    private void setMsg(Label lbl, String text, String color) {
        lbl.setText(text);
        lbl.setStyle("-fx-font-size:12;-fx-text-fill:"+color+";-fx-font-weight:bold;-fx-padding:6 10;-fx-background-color:derive("+color+",85%);-fx-background-radius:6;");
    }
    private Label fl(String text) { Label lbl = new Label(text); lbl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:"+Design.TEXT_DARK+";-fx-min-width:120;"); return lbl; }
}
