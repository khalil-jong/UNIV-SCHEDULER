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
import javafx.scene.control.Separator;
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

public class EnseignantPanel {

    private Utilisateur      utilisateur;
    private UnivSchedulerApp app;
    private CoursDAO    coursDAO = new CoursDAO();
    private SalleDAO    salleDAO = new SalleDAO();
    private MessageDAO  msgDAO   = new MessageDAO();
    private DateTimeFormatter hFmt = DateTimeFormatter.ofPattern("HH:mm");
    private DateTimeFormatter jFmt = DateTimeFormatter.ofPattern("EEE dd/MM", Locale.FRENCH);

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

    private HBox creerTopBar() {
        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        String nom = utilisateur.getNomComplet() + (nonLus > 0 ? "   🔴 " + nonLus : "");
        return Design.topBar("Enseignant", nom, Design.ENS_PRIMARY, () -> app.afficherLogin());
    }

    private VBox creerMenu(BorderPane root) {
        VBox menu = new VBox(2);
        menu.setPadding(new Insets(12, 10, 12, 10));
        menu.setPrefWidth(220);
        menu.setStyle("-fx-background-color: " + Design.ENS_MENU_BG + ";");

        // Avatar
        VBox avatar = new VBox(4);
        avatar.setAlignment(Pos.CENTER);
        avatar.setPadding(new Insets(14, 0, 16, 0));
        Label ico = new Label("🧑‍🏫");
        ico.setStyle("-fx-font-size: 28;");
        Label nom = new Label(utilisateur.getNomComplet());
        nom.setStyle("-fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold;");
        Label matiere = utilisateur.getMatiere() != null && !utilisateur.getMatiere().isEmpty()
            ? new Label(utilisateur.getMatiere()) : new Label("Enseignant");
        matiere.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 10;");
        Label tag = new Label("ENSEIGNANT");
        tag.setStyle("-fx-text-fill: " + Design.ENS_ACCENT + "; -fx-font-size: 9; -fx-font-weight: bold;" +
            "-fx-padding: 2 8; -fx-background-color: rgba(155,89,182,0.20); -fx-background-radius: 10;");
        avatar.getChildren().addAll(ico, nom, matiere, tag);
        menu.getChildren().add(avatar);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.12);");
        menu.getChildren().add(sep);

        menu.getChildren().add(Design.menuTitle("Mon Espace"));
        ajouterBtn(menu, "🏠  Accueil",                root, () -> creerAccueil());
        ajouterBtn(menu, "📅  Mon emploi du temps",    root, () -> new EmploiDuTempsViewPanel(utilisateur.getNomComplet(), true).createPanel());

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: rgba(255,255,255,0.12);");
        menu.getChildren().add(sep2);

        menu.getChildren().add(Design.menuTitle("Actions"));
        ajouterBtn(menu, "📨  Réserver une salle",     root, () -> creerReservation());
        ajouterBtn(menu, "🔧  Signaler un problème",   root, () -> creerSignalement());

        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        String msgLabel = nonLus > 0 ? "📬  Messages  🔴 " + nonLus : "📬  Mes messages";
        ajouterBtn(menu, msgLabel, root, () -> creerBoiteReception());

        return menu;
    }

    private void ajouterBtn(VBox menu, String label, BorderPane root,
                             java.util.function.Supplier<javafx.scene.Parent> p) {
        Button btn = Design.menuBtn(label, Design.ENS_HOVER);
        btn.setOnAction(e -> root.setCenter(p.get()));
        menu.getChildren().add(btn);
    }

    // ════════════════════════════════════════════════════════════════
    //  ACCUEIL
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerAccueil() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        // En-tête de bienvenue
        HBox welcome = new HBox(16);
        welcome.setPadding(new Insets(22, 24, 22, 24));
        welcome.setAlignment(Pos.CENTER_LEFT);
        welcome.setStyle(
            "-fx-background-color: linear-gradient(to right, " + Design.ENS_PRIMARY + ", " + Design.ENS_MENU_BG + ");" +
            "-fx-background-radius: 14;"
        );
        Label ico = new Label("🧑‍🏫"); ico.setStyle("-fx-font-size: 36;");
        VBox info = new VBox(4);
        Label greet = new Label("Bonjour, " + utilisateur.getNomComplet() + " 👋");
        greet.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sub = new Label("Enseignant" + (utilisateur.getMatiere() != null && !utilisateur.getMatiere().isEmpty() ? "  •  " + utilisateur.getMatiere() : ""));
        sub.setStyle("-fx-font-size: 13; -fx-text-fill: rgba(255,255,255,0.75);");
        info.getChildren().addAll(greet, sub);
        welcome.getChildren().addAll(ico, info);
        panel.getChildren().add(welcome);

        // Badge non-lus
        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        if (nonLus > 0) {
            Label lMsg = new Label("📬  Vous avez " + nonLus + " message(s) non lu(s) du gestionnaire.");
            lMsg.setStyle("-fx-font-size:13;-fx-text-fill:#e74c3c;-fx-font-weight:bold;-fx-padding:10 14;" +
                "-fx-background-color:#fdecea;-fx-border-color:#e74c3c;-fx-border-radius:8;-fx-background-radius:8;");
            panel.getChildren().add(lMsg);
        }

        // Prochains cours
        VBox sectionCours = Design.section("📋  Vos prochains cours");
        List<Cours> mesCours = coursDAO.obtenirParEnseignantAvecEDT(utilisateur.getNomComplet());
        List<Cours> aVenir   = mesCours.stream()
            .filter(c -> c.getDateDebut().isAfter(LocalDateTime.now())).limit(5).collect(Collectors.toList());

        if (aVenir.isEmpty()) {
            Label vide = new Label("Aucun cours à venir prochainement.");
            vide.setStyle("-fx-font-size:13;-fx-text-fill:" + Design.TEXT_MUTED + ";-fx-padding:10;");
            sectionCours.getChildren().add(vide);
        } else {
            for (Cours c : aVenir) {
                Salle s = salleDAO.obtenirParId(c.getSalleId());
                HBox carte = new HBox(16);
                carte.setPadding(new Insets(12, 16, 12, 16));
                carte.setAlignment(Pos.CENTER_LEFT);
                carte.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: " + Design.ENS_ACCENT + ";" +
                    "-fx-border-width: 0 0 0 4;" +
                    "-fx-border-radius: 0 8 8 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);"
                );
                Label lH = new Label(c.getDateDebut().format(jFmt) + "\n" + c.getDateDebut().format(hFmt));
                lH.setStyle("-fx-font-weight:bold;-fx-font-size:12;-fx-text-fill:" + Design.ENS_ACCENT + ";-fx-min-width:80;");
                Label lM = new Label(c.getMatiere()); lM.setStyle("-fx-font-weight:bold;-fx-font-size:13;-fx-min-width:150;");
                Label lC = new Label("🎓  " + c.getClasse()); lC.setStyle("-fx-font-size:12;-fx-min-width:130;");
                Label lS = new Label("🏫  " + (s != null ? s.getNumero() : "?")); lS.setStyle("-fx-font-size:12;");
                carte.getChildren().addAll(lH, lM, lC, lS);
                sectionCours.getChildren().add(carte);
            }
        }
        panel.getChildren().add(sectionCours);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════
    //  RÉSERVATION
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerReservation() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");
        Label titre = Design.pageTitle("📨  Demande de Réservation de Salle");
        Label desc  = Design.muted("Choisissez un créneau, cherchez les salles libres, puis envoyez la demande au gestionnaire.");
        panel.getChildren().addAll(titre, desc);

        // Étape 1 : recherche
        VBox boxCherche = Design.section("🔍  Étape 1 — Rechercher une salle disponible");
        GridPane gC = new GridPane(); gC.setHgap(14); gC.setVgap(12); gC.setPadding(new Insets(10,0,0,0));

        DatePicker dp = new DatePicker(LocalDate.now().plusDays(1));
        Spinner<Integer> spH   = new Spinner<>(7, 22, 8); spH.setPrefWidth(80);
        Spinner<Integer> spMin = new Spinner<>(0, 59, 0, 5); spMin.setPrefWidth(80);
        Spinner<Integer> spDur = new Spinner<>(15, 480, 60, 15); spDur.setPrefWidth(90);
        CheckBox chkV = new CheckBox("📽  Vidéoprojecteur");
        CheckBox chkT = new CheckBox("🖥  Tableau interactif");
        CheckBox chkC = new CheckBox("❄  Climatisation");
        Spinner<Integer> spCap = new Spinner<>(0, 500, 0); spCap.setPrefWidth(80);

        gC.add(fl("Date :"),         0,0); gC.add(dp,   1,0);
        gC.add(fl("Heure début :"),  0,1); gC.add(new HBox(5,spH,new Label("h"),spMin,new Label("min")), 1,1);
        gC.add(fl("Durée (min) :"),  0,2); gC.add(spDur, 1,2);
        gC.add(fl("Capacité min :"), 0,3); gC.add(new HBox(6,spCap,new Label("(0 = indifférent)")), 1,3);
        gC.add(fl("Équipements :"),  0,4); gC.add(new HBox(14,chkV,chkT,chkC), 1,4);

        Button btnChercher = Design.btnPrimary("🔍  Rechercher les salles libres", Design.ENS_ACCENT);
        Label lblResultat = new Label(""); lblResultat.setStyle("-fx-font-size:12;-fx-font-weight:bold;");

        TableView<Salle> tableSalles = new TableView<>();
        tableSalles.setPrefHeight(180);
        tableSalles.setPlaceholder(new Label("Lancez une recherche pour voir les salles disponibles."));
        tableSalles.setStyle("-fx-border-color:#e8ecf5;-fx-border-radius:8;");

        TableColumn<Salle,String>  sNum  = new TableColumn<>("N°");       sNum.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getNumero())); sNum.setPrefWidth(60);
        TableColumn<Salle,String>  sBat  = new TableColumn<>("Bâtiment"); sBat.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getBatiment())); sBat.setPrefWidth(130);
        TableColumn<Salle,Integer> sCap  = new TableColumn<>("Cap.");      sCap.setCellValueFactory(c->new SimpleObjectProperty<>(c.getValue().getCapacite())); sCap.setPrefWidth(55);
        TableColumn<Salle,String>  sType = new TableColumn<>("Type");     sType.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getType())); sType.setPrefWidth(60);
        TableColumn<Salle,String>  sEq   = new TableColumn<>("Équipements"); sEq.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getEquipementsStr())); sEq.setPrefWidth(150);
        tableSalles.getColumns().addAll(sNum,sBat,sCap,sType,sEq);

        btnChercher.setOnAction(e -> {
            if (dp.getValue() == null) { lblResultat.setText("⚠️  Sélectionnez une date."); return; }
            LocalDateTime debut = LocalDateTime.of(dp.getValue(), LocalTime.of(spH.getValue(), spMin.getValue()));
            List<Salle> dispo = salleDAO.obtenirSallesDisponibles(debut, spDur.getValue());
            if (spCap.getValue() > 0) dispo = dispo.stream().filter(s -> s.getCapacite() >= spCap.getValue()).collect(Collectors.toList());
            if (chkV.isSelected()) dispo = dispo.stream().filter(Salle::isVideoprojecteur).collect(Collectors.toList());
            if (chkT.isSelected()) dispo = dispo.stream().filter(Salle::isTableauInteractif).collect(Collectors.toList());
            if (chkC.isSelected()) dispo = dispo.stream().filter(Salle::isClimatisation).collect(Collectors.toList());
            tableSalles.setItems(FXCollections.observableArrayList(dispo));
            lblResultat.setText(dispo.isEmpty()
                ? "❌  Aucune salle disponible pour ce créneau."
                : "✅  " + dispo.size() + " salle(s) disponible(s) — cliquez pour sélectionner.");
            lblResultat.setStyle(dispo.isEmpty()
                ? "-fx-text-fill:" + Design.DANGER + ";-fx-font-size:12;"
                : "-fx-text-fill:" + Design.SUCCESS + ";-fx-font-size:12;");
        });
        boxCherche.getChildren().addAll(gC, btnChercher, lblResultat, tableSalles);

        // Étape 2 : envoi
        VBox boxDemande = Design.section("📤  Étape 2 — Envoyer la demande au gestionnaire");
        Label lblSalleChoisie = new Label("Aucune salle sélectionnée — cliquez sur une ligne ci-dessus.");
        lblSalleChoisie.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";-fx-padding:6 10;-fx-background-color:#f8f9fe;-fx-background-radius:6;");

        TextField tfMotif = sf("Ex: Cours supplémentaire, Soutenance...", 380);
        TextArea  taComment = new TextArea(); taComment.setPromptText("Précisions..."); taComment.setPrefHeight(70); taComment.setWrapText(true); taComment.setStyle(Design.INPUT_STYLE);
        Label msgRes = new Label(""); msgRes.setWrapText(true);
        Button btnEnvoyer = Design.btnPrimary("📤  Envoyer la demande", Design.ENS_ACCENT);
        btnEnvoyer.setDisable(true);

        final Salle[] salleChoisie = {null};
        tableSalles.getSelectionModel().selectedItemProperty().addListener((obs,old,sel) -> {
            if (sel == null) return;
            salleChoisie[0] = sel;
            lblSalleChoisie.setText("🏫  Salle : " + sel.getNumero() + " — " + sel.getBatiment() + " (Cap: " + sel.getCapacite() + ")");
            lblSalleChoisie.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.SUCCESS + ";-fx-font-weight:bold;-fx-padding:6 10;-fx-background-color:#e8faf5;-fx-background-radius:6;");
            btnEnvoyer.setDisable(false);
        });

        GridPane gDemande = new GridPane(); gDemande.setHgap(12); gDemande.setVgap(10); gDemande.setPadding(new Insets(10,0,0,0));
        gDemande.add(fl("Motif :"),     0,0); gDemande.add(tfMotif,   1,0);
        gDemande.add(fl("Précisions :"),0,1); gDemande.add(taComment, 1,1);

        btnEnvoyer.setOnAction(e -> {
            if (salleChoisie[0] == null || dp.getValue() == null) { msgRes.setText("⚠️  Sélectionnez une salle et une date."); return; }
            if (tfMotif.getText().isEmpty()) { msgRes.setText("⚠️  Le motif est obligatoire."); return; }
            String sujet = "[Réservation] " + tfMotif.getText().trim() + " — Salle " + salleChoisie[0].getNumero();
            String corps = "Demande de : " + utilisateur.getNomComplet() + " (Enseignant)\n\n"
                + "Salle : " + salleChoisie[0].getNumero() + " — " + salleChoisie[0].getBatiment() + " (Cap: " + salleChoisie[0].getCapacite() + ")\n"
                + "Motif : " + tfMotif.getText().trim() + "\n"
                + "Date : " + dp.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n"
                + "Heure : " + String.format("%02dh%02d", spH.getValue(), spMin.getValue()) + "\n"
                + "Durée : " + spDur.getValue() + " min"
                + (taComment.getText().isEmpty() ? "" : "\nCommentaire : " + taComment.getText());
            Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, corps, "RESERVATION", false, null, "GESTIONNAIRE");
            try {
                msgDAO.envoyer(msg);
                setMsg(msgRes, "✅  Demande envoyée. Le gestionnaire la recevra dans sa boîte de réception.", Design.SUCCESS);
                tfMotif.clear(); taComment.clear(); salleChoisie[0] = null; btnEnvoyer.setDisable(true);
                lblSalleChoisie.setText("Aucune salle sélectionnée."); tableSalles.getSelectionModel().clearSelection();
            } catch (Exception ex) { setMsg(msgRes, "❌  " + ex.getMessage(), Design.DANGER); }
        });

        boxDemande.getChildren().addAll(lblSalleChoisie, gDemande, btnEnvoyer, msgRes);
        panel.getChildren().addAll(boxCherche, boxDemande);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════
    //  SIGNALEMENT
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerSignalement() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");
        Label titre = Design.pageTitle("🔧  Signaler un Problème Technique");
        Label desc  = Design.muted("Votre signalement sera transmis directement au gestionnaire.");
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
        TextArea taDesc = new TextArea(); taDesc.setPromptText("Décrivez le problème..."); taDesc.setPrefHeight(100); taDesc.setWrapText(true); taDesc.setStyle(Design.INPUT_STYLE);

        grid.add(fl("Salle :"),      0,0); grid.add(cbSalle, 1,0);
        grid.add(fl("Problème :"),   0,1); grid.add(cbType,  1,1);
        grid.add(fl("Description :"),0,2); grid.add(taDesc,  1,2);

        Label msgS = new Label(""); msgS.setWrapText(true);
        Button btnEnvoyer = Design.btnDanger("📤  Envoyer au gestionnaire");
        btnEnvoyer.setOnAction(e -> {
            if (cbSalle.getValue()==null||cbType.getValue()==null||taDesc.getText().isEmpty()) { setMsg(msgS,"⚠️  Remplissez tous les champs.",Design.WARNING); return; }
            String sujet = "[Signalement] " + cbType.getValue() + " — Salle " + cbSalle.getValue().getNumero();
            String corps = "De : " + utilisateur.getNomComplet() + "\nSalle : " + cbSalle.getValue().getNumero() + " — " + cbSalle.getValue().getBatiment() + "\nProblème : " + cbType.getValue() + "\n\n" + taDesc.getText().trim();
            Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(), utilisateur.getRole(), sujet, corps, "RECLAMATION", false, null);
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
    //  BOÎTE DE RÉCEPTION
    // ════════════════════════════════════════════════════════════════
    private ScrollPane creerBoiteReception() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");
        Label titre = Design.pageTitle("📬  Mes Messages");

        int nonLus = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
        Label lblStats = new Label(nonLus > 0 ? "🔵  " + nonLus + " message(s) non lu(s)" : "✅  Tous les messages sont lus.");
        lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;-fx-background-radius:8;-fx-text-fill:" +
            (nonLus>0?Design.DANGER:Design.SUCCESS) + ";-fx-background-color:" + (nonLus>0?"#fdecea":"#e8faf5") + ";");

        ObservableList<Message> items = FXCollections.observableArrayList(
            msgDAO.obtenirPourUtilisateur(utilisateur.getId()));
        TableView<Message> table = new TableView<>(items);
        table.setPrefHeight(260);
        table.setStyle("-fx-border-color:#e8ecf5;-fx-border-radius:8;");
        table.setPlaceholder(new Label("Aucun message reçu."));

        DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        TableColumn<Message,String> cLu    = new TableColumn<>("");     cLu.setCellValueFactory(c->new SimpleStringProperty(c.getValue().isLu()?"":"🔵")); cLu.setPrefWidth(30);
        TableColumn<Message,String> cSujet = new TableColumn<>("Sujet");cSujet.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getSujet())); cSujet.setPrefWidth(330);
        TableColumn<Message,String> cDate  = new TableColumn<>("Date"); cDate.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getCreatedAt().format(fmtDate))); cDate.setPrefWidth(90);
        table.getColumns().addAll(cLu, cSujet, cDate);

        VBox zoneDetail = new VBox(10);
        zoneDetail.setPadding(new Insets(14));
        zoneDetail.setStyle(Design.SECTION_STYLE);
        zoneDetail.setVisible(false);
        Label lblTitre = new Label(""); lblTitre.setStyle("-fx-font-size:14;-fx-font-weight:bold;");
        Label lblMeta  = new Label(""); lblMeta.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";");
        TextArea taMsg = new TextArea(); taMsg.setEditable(false); taMsg.setPrefHeight(110); taMsg.setWrapText(true); taMsg.setStyle(Design.INPUT_STYLE);
        zoneDetail.getChildren().addAll(lblTitre, lblMeta, taMsg);

        table.getSelectionModel().selectedItemProperty().addListener((obs,old,sel) -> {
            if (sel==null) { zoneDetail.setVisible(false); return; }
            lblTitre.setText(sel.getSujet());
            lblMeta.setText("De : " + sel.getExpediteurNom() + "   •   " + sel.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
            taMsg.setText(sel.getCorps()); zoneDetail.setVisible(true);
            if (!sel.isLu()) {
                msgDAO.marquerLu(sel.getId()); sel.setLu(true); table.refresh();
                int n = msgDAO.compterNonLusPourUtilisateur(utilisateur.getId());
                lblStats.setText(n>0?"🔵  "+n+" message(s) non lu(s)":"✅  Tous les messages sont lus.");
                lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;-fx-background-radius:8;-fx-text-fill:"+(n>0?Design.DANGER:Design.SUCCESS)+";-fx-background-color:"+(n>0?"#fdecea":"#e8faf5")+";");
            }
        });

        Button btnRefresh = Design.btnSecondary("🔄  Rafraîchir");
        btnRefresh.setOnAction(e -> { items.setAll(msgDAO.obtenirPourUtilisateur(utilisateur.getId())); });
        Button btnTousLus = Design.btnPrimary("✔  Tout marquer lu", Design.SUCCESS);
        btnTousLus.setOnAction(e -> {
            msgDAO.marquerTousLusPourUtilisateur(utilisateur.getId());
            items.setAll(msgDAO.obtenirPourUtilisateur(utilisateur.getId())); table.refresh();
            lblStats.setText("✅  Tous les messages sont lus.");
            lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;-fx-background-radius:8;-fx-text-fill:"+Design.SUCCESS+";-fx-background-color:#e8faf5;");
        });

        panel.getChildren().addAll(titre, lblStats, new HBox(10, btnRefresh, btnTousLus), table, zoneDetail);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    private void setMsg(Label lbl, String text, String color) {
        lbl.setText(text);
        lbl.setStyle("-fx-font-size:12;-fx-text-fill:"+color+";-fx-font-weight:bold;-fx-padding:6 10;-fx-background-color:derive("+color+",85%);-fx-background-radius:6;");
    }
    private TextField sf(String prompt, double w) { TextField tf = new TextField(); tf.setPromptText(prompt); tf.setPrefWidth(w); tf.setStyle(Design.INPUT_STYLE); return tf; }
    private Label fl(String text) { Label lbl = new Label(text); lbl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:"+Design.TEXT_DARK+";-fx-min-width:120;"); return lbl; }
}
