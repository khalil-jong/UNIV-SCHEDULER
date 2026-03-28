package ui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import dao.MessageDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Message;
import models.Utilisateur;

/**
 * Messagerie du gestionnaire — 2 onglets :
 *   📬 Boîte de réception  : messages des enseignants, étudiants et de l'admin
 *   📤 Contacter l'admin   : formulaire d'envoi vers l'administrateur
 */
public class MessageriePanelGestionnaire {

    private MessageDAO  msgDAO = new MessageDAO();
    private Utilisateur utilisateur;
    private static final DateTimeFormatter FMT  = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final DateTimeFormatter FMTL = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    /** Constructeur sans utilisateur (rétrocompatibilité) */
    public MessageriePanelGestionnaire() {}

    /** Constructeur avec utilisateur (nécessaire pour l'envoi) */
    public MessageriePanelGestionnaire(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public ScrollPane createPanel() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-font-size: 13;");

        Tab tabRecus = new Tab("📬 Boîte de réception",     creerOngletReception());
        Tab tabEnvoi = new Tab("📤 Contacter l'administrateur", creerOngletEnvoi());
        tabs.getTabs().addAll(tabRecus, tabEnvoi);

        VBox wrapper = new VBox(tabs);
        wrapper.setPadding(new Insets(14));
        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  ONGLET 1 — BOÎTE DE RÉCEPTION
    // ════════════════════════════════════════════════════════════
    private VBox creerOngletReception() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(18));

        Label titre = new Label("📬 Boîte de Réception");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Badge non lus
        int nonLus = msgDAO.compterNonLus();
        Label lblStats = new Label(nonLus > 0
            ? nonLus + " message(s) non lu(s)"
            : "Tous les messages sont lus.");
        lblStats.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: "
            + (nonLus > 0 ? "#e74c3c" : "#27ae60") + ";");

        // Barre de filtres
        HBox barFiltres = new HBox(10);
        barFiltres.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> cbFiltre = new ComboBox<>();
        cbFiltre.getItems().addAll("Tous", "Non lus", "Réservations", "Réclamations", "Alertes admin", "Messages");
        cbFiltre.setValue("Tous");
        Button btnTousLus = new Button("✔ Tout marquer lu");
        btnTousLus.setStyle("-fx-background-color:#95a5a6;-fx-text-fill:white;-fx-padding:6 12;");
        Button btnRefresh = new Button("🔄");
        btnRefresh.setStyle("-fx-background-color:#3498db;-fx-text-fill:white;-fx-padding:6 10;");
        barFiltres.getChildren().addAll(new Label("Afficher :"), cbFiltre, btnTousLus, btnRefresh);

        // Tableau
        ObservableList<Message> items = FXCollections.observableArrayList(msgDAO.obtenirTous());
        TableView<Message> table = new TableView<>(items);
        table.setPrefHeight(290);
        table.setPlaceholder(new Label("Aucun message reçu."));

        TableColumn<Message,String> cLu    = new TableColumn<>("");
        cLu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isLu() ? "" : "🔵")); cLu.setPrefWidth(28);
        TableColumn<Message,String> cType  = new TableColumn<>("Type");
        cType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeLabel())); cType.setPrefWidth(120);
        TableColumn<Message,String> cExp   = new TableColumn<>("Expéditeur");
        cExp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpediteurNom() + " (" + c.getValue().getExpediteurRole() + ")")); cExp.setPrefWidth(170);
        TableColumn<Message,String> cSujet = new TableColumn<>("Sujet");
        cSujet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSujet())); cSujet.setPrefWidth(235);
        TableColumn<Message,String> cDate  = new TableColumn<>("Date");
        cDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt().format(FMT))); cDate.setPrefWidth(78);
        TableColumn<Message,Void>   cAct   = new TableColumn<>("Action"); cAct.setPrefWidth(85);
        cAct.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("🗑 Suppr.");
            { btn.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-font-size:11;");
              btn.setOnAction(e -> { msgDAO.supprimer(getTableView().getItems().get(getIndex()).getId()); items.setAll(appliquerFiltre(cbFiltre.getValue())); }); }
            @Override protected void updateItem(Void v,boolean empty){super.updateItem(v,empty);setGraphic(empty?null:btn);}
        });
        table.getColumns().addAll(cLu, cType, cExp, cSujet, cDate, cAct);

        // Zone lecture
        VBox zoneDetail = new VBox(8);
        zoneDetail.setPadding(new Insets(12));
        zoneDetail.setStyle("-fx-border-color:#ddd;-fx-border-radius:6;-fx-background-color:white;");
        zoneDetail.setVisible(false);
        Label lblDetailTitre = new Label(""); lblDetailTitre.setStyle("-fx-font-size:14;-fx-font-weight:bold;");
        Label lblDetailMeta  = new Label(""); lblDetailMeta.setStyle("-fx-font-size:12;-fx-text-fill:#7f8c8d;");
        TextArea taDetail    = new TextArea(); taDetail.setEditable(false); taDetail.setPrefHeight(110); taDetail.setWrapText(true);
        zoneDetail.getChildren().addAll(lblDetailTitre, lblDetailMeta, taDetail);

        // Zone réponse rapide (uniquement si l'expéditeur est l'ADMIN)
        VBox zoneReponse = new VBox(8);
        zoneReponse.setPadding(new Insets(12));
        zoneReponse.setStyle("-fx-border-color:#8e44ad;-fx-border-width:0 0 0 4;-fx-background-color:#f5eef8;-fx-border-radius:0 6 6 0;");
        zoneReponse.setVisible(false);
        Label lblRepTitre = new Label("↩️ Répondre à l'administrateur");
        lblRepTitre.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#8e44ad;");
        TextField tfSujetRep = new TextField(); tfSujetRep.setPromptText("Sujet de la réponse"); tfSujetRep.setPrefWidth(420);
        TextArea  taRep      = new TextArea();  taRep.setPromptText("Votre réponse..."); taRep.setPrefHeight(80); taRep.setWrapText(true);
        Label     msgRep     = new Label(""); msgRep.setStyle("-fx-font-size:12;"); msgRep.setWrapText(true);
        Button    btnRep     = new Button("📤 Envoyer la réponse");
        btnRep.setStyle("-fx-background-color:#8e44ad;-fx-text-fill:white;-fx-padding:7 16;-fx-font-weight:bold;");
        final Message[] selRef = {null};
        btnRep.setOnAction(e -> {
            if (taRep.getText().isEmpty()) { msgRep.setText("⚠️ Écrivez votre réponse."); msgRep.setStyle("-fx-text-fill:#e67e22;"); return; }
            if (utilisateur == null) { msgRep.setText("❌ Utilisateur non identifié. Relancez l'application."); return; }
            String sujet = tfSujetRep.getText().isEmpty()
                ? "Réponse : " + (selRef[0] != null ? selRef[0].getSujet() : "")
                : tfSujetRep.getText().trim();
            Message rep = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, taRep.getText().trim(), "GENERAL", false, null, "ADMIN");
            try {
                msgDAO.envoyer(rep);
                msgRep.setText("✅ Réponse envoyée à l'administrateur."); msgRep.setStyle("-fx-text-fill:#27ae60;");
                tfSujetRep.clear(); taRep.clear();
            } catch (Exception ex) { msgRep.setText("❌ " + ex.getMessage()); msgRep.setStyle("-fx-text-fill:#e74c3c;"); }
        });
        zoneReponse.getChildren().addAll(lblRepTitre, tfSujetRep, taRep, btnRep, msgRep);

        // Clic → lecture + afficher réponse si expéditeur = ADMIN
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { zoneDetail.setVisible(false); zoneReponse.setVisible(false); return; }
            selRef[0] = sel;
            lblDetailTitre.setText(sel.getTypeLabel() + "  —  " + sel.getSujet());
            lblDetailMeta.setText("De : " + sel.getExpediteurNom() + " (" + sel.getExpediteurRole()
                + ")   •   " + sel.getCreatedAt().format(FMTL));
            taDetail.setText(sel.getCorps());
            zoneDetail.setVisible(true);
            // Proposer la réponse seulement pour les messages venant de l'admin
            boolean vientAdmin = "ADMIN".equalsIgnoreCase(sel.getExpediteurRole());
            zoneReponse.setVisible(vientAdmin);
            if (vientAdmin) {
				tfSujetRep.setText("Réponse : " + sel.getSujet());
			}
            if (!sel.isLu()) { msgDAO.marquerLu(sel.getId()); sel.setLu(true); table.refresh(); }
        });

        // Filtres & boutons
        cbFiltre.setOnAction(e -> items.setAll(appliquerFiltre(cbFiltre.getValue())));
        btnTousLus.setOnAction(e -> {
            msgDAO.marquerTousLus(); items.setAll(appliquerFiltre(cbFiltre.getValue())); table.refresh();
            lblStats.setText("Tous les messages sont lus."); lblStats.setStyle("-fx-text-fill:#27ae60;-fx-font-size:13;-fx-font-weight:bold;");
        });
        btnRefresh.setOnAction(e -> {
            items.setAll(appliquerFiltre(cbFiltre.getValue()));
            int n = msgDAO.compterNonLus();
            lblStats.setText(n > 0 ? n + " message(s) non lu(s)" : "Tous les messages sont lus.");
            lblStats.setStyle("-fx-text-fill:" + (n > 0 ? "#e74c3c" : "#27ae60") + ";-fx-font-size:13;-fx-font-weight:bold;");
        });

        panel.getChildren().addAll(titre, lblStats, barFiltres, table, zoneDetail, zoneReponse);
        return panel;
    }

    // ════════════════════════════════════════════════════════════
    //  ONGLET 2 — CONTACTER L'ADMINISTRATEUR
    // ════════════════════════════════════════════════════════════
    private VBox creerOngletEnvoi() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(18));

        Label titre = new Label("📤 Contacter l'Administrateur");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label desc = new Label("Utilisez ce formulaire pour envoyer un message, une réclamation ou une demande à l'administrateur. Il le recevra dans sa boîte de notifications.");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #555;"); desc.setWrapText(true);

        VBox form = new VBox(12);
        form.setPadding(new Insets(16));
        form.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll(
            "💬 Message général",
            "⚠️ Réclamation",
            "📋 Demande de validation",
            "🔔 Signalement urgent",
            "📊 Rapport d'activité"
        );
        cbType.setValue("💬 Message général");
        cbType.setPrefWidth(280);

        TextField tfSujet = new TextField(); tfSujet.setPromptText("Sujet du message"); tfSujet.setPrefWidth(420);
        TextArea  taCorps = new TextArea();  taCorps.setPromptText("Contenu du message..."); taCorps.setPrefHeight(140); taCorps.setWrapText(true);

        Label msgEnvoi = new Label(""); msgEnvoi.setStyle("-fx-font-size:12;"); msgEnvoi.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer à l'administrateur");
        btnEnvoyer.setStyle("-fx-background-color:#2c3e50;-fx-text-fill:white;-fx-padding:10 22;-fx-font-weight:bold;-fx-font-size:13;");

        btnEnvoyer.setOnAction(e -> {
            if (tfSujet.getText().isEmpty() || taCorps.getText().isEmpty()) {
                msgEnvoi.setText("⚠️ Le sujet et le contenu sont obligatoires."); msgEnvoi.setStyle("-fx-text-fill:#e67e22;"); return;
            }
            if (utilisateur == null) { msgEnvoi.setText("❌ Utilisateur non identifié. Relancez l'application."); return; }
            String typeBase = cbType.getValue().contains("Réclamation") || cbType.getValue().contains("urgent")
                ? "RECLAMATION" : "GENERAL";
            String sujet = "[" + cbType.getValue().replaceAll("[^a-zA-ZÀ-ÿ /]","").trim() + "] " + tfSujet.getText().trim();
            Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, taCorps.getText().trim(), typeBase, false, null, "ADMIN");
            try {
                msgDAO.envoyer(msg);
                msgEnvoi.setText("✅ Message envoyé à l'administrateur."); msgEnvoi.setStyle("-fx-text-fill:#27ae60;");
                tfSujet.clear(); taCorps.clear(); cbType.setValue("💬 Message général");
            } catch (Exception ex) { msgEnvoi.setText("❌ Erreur : " + ex.getMessage()); msgEnvoi.setStyle("-fx-text-fill:#e74c3c;"); }
        });

        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(10);
        grid.add(new Label("Type :"),    0, 0); grid.add(cbType,  1, 0);
        grid.add(new Label("Sujet :"),   0, 1); grid.add(tfSujet, 1, 1);
        grid.add(new Label("Message :"), 0, 2); grid.add(taCorps, 1, 2);

        form.getChildren().addAll(grid, btnEnvoyer, msgEnvoi);
        panel.getChildren().addAll(titre, desc, form);
        return panel;
    }

    // ── Helper filtre ──
    private List<Message> appliquerFiltre(String filtre) {
        List<Message> tous = msgDAO.obtenirTous();
        if (filtre == null || filtre.equals("Tous")) {
			return tous;
		}
        return tous.stream().filter(m -> {
            switch (filtre) {
                case "Non lus":       return !m.isLu();
                case "Réservations":  return "RESERVATION".equals(m.getType());
                case "Réclamations":  return "RECLAMATION".equals(m.getType());
                case "Alertes admin": return "ALERTE".equals(m.getType());
                case "Messages":      return "GENERAL".equals(m.getType());
                default: return true;
            }
        }).collect(Collectors.toList());
    }
}
