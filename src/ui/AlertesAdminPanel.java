package ui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import dao.MessageDAO;
import dao.UtilisateurDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Message;
import models.Utilisateur;

/**
 * Panel Alertes & Notifications pour l'Administrateur.
 *  - Reçoit les messages envoyés par le GESTIONNAIRE (destinataire_role = 'ADMIN')
 *  - Peut répondre au gestionnaire (destinataire_role = 'GESTIONNAIRE')
 */
public class AlertesAdminPanel {

    private MessageDAO     msgDAO  = new MessageDAO();
    private UtilisateurDAO userDAO = new UtilisateurDAO();
    private Utilisateur    utilisateur;
    private DateTimeFormatter fmt  = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private DateTimeFormatter fmtL = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    public AlertesAdminPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public ScrollPane createPanel() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(20));

        Label titre = new Label("🔔 Alertes & Notifications");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ── Stats non lus ──
        int nonLus = msgDAO.compterNonLusAdmin();
        Label lblStats = new Label(nonLus > 0
            ? nonLus + " message(s) non lu(s) du gestionnaire"
            : "Aucun message non lu.");
        lblStats.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: "
            + (nonLus > 0 ? "#e74c3c" : "#27ae60") + ";");

        // ── Barre de filtres ──
        HBox barFiltres = new HBox(10);
        barFiltres.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> cbFiltre = new ComboBox<>();
        cbFiltre.getItems().addAll("Tous", "Non lus", "Alertes", "Messages");
        cbFiltre.setValue("Tous");
        Button btnTousLus = new Button("✔ Tout marquer lu");
        btnTousLus.setStyle("-fx-background-color:#95a5a6;-fx-text-fill:white;");
        Button btnRefresh = new Button("🔄 Rafraîchir");
        btnRefresh.setStyle("-fx-background-color:#3498db;-fx-text-fill:white;");
        barFiltres.getChildren().addAll(new Label("Afficher :"), cbFiltre, btnTousLus, btnRefresh);

        // ── Tableau des messages ──
        ObservableList<Message> items = FXCollections.observableArrayList(msgDAO.obtenirPourAdmin());
        TableView<Message> table = new TableView<>(items);
        table.setPrefHeight(280);
        table.setPlaceholder(new Label("Aucun message reçu du gestionnaire."));

        TableColumn<Message,String> cLu    = new TableColumn<>("");         cLu.setCellValueFactory(c->new SimpleStringProperty(c.getValue().isLu()?"":"🔵")); cLu.setPrefWidth(28);
        TableColumn<Message,String> cType  = new TableColumn<>("Type");     cType.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getTypeLabel())); cType.setPrefWidth(110);
        TableColumn<Message,String> cExp   = new TableColumn<>("Expéditeur"); cExp.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getExpediteurNom())); cExp.setPrefWidth(150);
        TableColumn<Message,String> cSujet = new TableColumn<>("Sujet");    cSujet.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getSujet())); cSujet.setPrefWidth(250);
        TableColumn<Message,String> cDate  = new TableColumn<>("Date");     cDate.setCellValueFactory(c->new SimpleStringProperty(c.getValue().getCreatedAt().format(fmt))); cDate.setPrefWidth(80);
        TableColumn<Message,Void>   cAct   = new TableColumn<>("Action");   cAct.setPrefWidth(85);
        cAct.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("🗑 Suppr.");
            { btn.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-font-size:11;");
              btn.setOnAction(e -> { msgDAO.supprimer(getTableView().getItems().get(getIndex()).getId()); items.setAll(appliquerFiltre(cbFiltre.getValue())); }); }
            @Override protected void updateItem(Void v,boolean empty){super.updateItem(v,empty);setGraphic(empty?null:btn);}
        });
        table.getColumns().addAll(cLu, cType, cExp, cSujet, cDate, cAct);

        // ── Zone de lecture ──
        VBox zoneDetail = new VBox(8);
        zoneDetail.setPadding(new Insets(14));
        zoneDetail.setStyle("-fx-border-color:#ddd;-fx-border-radius:6;-fx-background-color:white;");
        zoneDetail.setVisible(false);

        Label lblDetailTitre = new Label(""); lblDetailTitre.setStyle("-fx-font-size:14;-fx-font-weight:bold;");
        Label lblDetailMeta  = new Label(""); lblDetailMeta.setStyle("-fx-font-size:12;-fx-text-fill:#7f8c8d;");
        TextArea taDetail    = new TextArea(); taDetail.setEditable(false); taDetail.setPrefHeight(120); taDetail.setWrapText(true);

        zoneDetail.getChildren().addAll(lblDetailTitre, lblDetailMeta, taDetail);

        // ── Zone de réponse ──
        VBox zoneReponse = new VBox(10);
        zoneReponse.setPadding(new Insets(14));
        zoneReponse.setStyle("-fx-border-color:#3498db;-fx-border-width:0 0 0 4;-fx-border-radius:0 6 6 0;-fx-background-color:#eaf4fb;");
        zoneReponse.setVisible(false);

        Label lblRepTitre = new Label("↩️ Répondre au gestionnaire");
        lblRepTitre.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#2980b9;");

        TextField tfSujetRep = new TextField(); tfSujetRep.setPromptText("Sujet de la réponse");
        TextArea  taRep      = new TextArea();  taRep.setPromptText("Votre message..."); taRep.setPrefHeight(90); taRep.setWrapText(true);

        Label msgRep = new Label(""); msgRep.setStyle("-fx-font-size:12;"); msgRep.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer la réponse");
        btnEnvoyer.setStyle("-fx-background-color:#2980b9;-fx-text-fill:white;-fx-padding:8 18;-fx-font-weight:bold;");

        // Référence au message sélectionné (pour contextualiser la réponse)
        final Message[] selRef = {null};

        btnEnvoyer.setOnAction(e -> {
            if (taRep.getText().isEmpty()) { msgRep.setText("⚠️ Le corps du message est vide."); msgRep.setStyle("-fx-text-fill:#e67e22;"); return; }
            String sujet = tfSujetRep.getText().isEmpty()
                ? "Réponse : " + (selRef[0] != null ? selRef[0].getSujet() : "")
                : tfSujetRep.getText().trim();
            Message rep = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, taRep.getText().trim(), "GENERAL", false, null, "GESTIONNAIRE");
            try {
                msgDAO.envoyer(rep);
                msgRep.setText("✅ Réponse envoyée au gestionnaire.");
                msgRep.setStyle("-fx-text-fill:#27ae60;");
                tfSujetRep.clear(); taRep.clear();
            } catch (Exception ex) {
                msgRep.setText("❌ Erreur : " + ex.getMessage()); msgRep.setStyle("-fx-text-fill:#e74c3c;");
            }
        });

        zoneReponse.getChildren().addAll(lblRepTitre, tfSujetRep, taRep, btnEnvoyer, msgRep);

        // ── Clic sur une ligne → lecture + pré-remplir réponse ──
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { zoneDetail.setVisible(false); zoneReponse.setVisible(false); return; }
            selRef[0] = sel;
            lblDetailTitre.setText(sel.getTypeLabel() + "  —  " + sel.getSujet());
            lblDetailMeta.setText("De : " + sel.getExpediteurNom()
                + "   •   " + sel.getCreatedAt().format(fmtL));
            taDetail.setText(sel.getCorps());
            zoneDetail.setVisible(true);
            zoneReponse.setVisible(true);
            tfSujetRep.setText("Réponse : " + sel.getSujet());
            if (!sel.isLu()) { msgDAO.marquerLu(sel.getId()); sel.setLu(true); table.refresh(); }
        });

        // ── Actions barre ──
        cbFiltre.setOnAction(e -> items.setAll(appliquerFiltre(cbFiltre.getValue())));
        btnTousLus.setOnAction(e -> { msgDAO.marquerTousLusAdmin(); items.setAll(appliquerFiltre(cbFiltre.getValue())); table.refresh(); lblStats.setText("Aucun message non lu."); lblStats.setStyle("-fx-text-fill:#27ae60;-fx-font-size:13;-fx-font-weight:bold;"); });
        btnRefresh.setOnAction(e -> {
            items.setAll(appliquerFiltre(cbFiltre.getValue()));
            int n = msgDAO.compterNonLusAdmin();
            lblStats.setText(n>0 ? n+" message(s) non lu(s) du gestionnaire" : "Aucun message non lu.");
            lblStats.setStyle("-fx-text-fill:"+(n>0?"#e74c3c":"#27ae60")+";-fx-font-size:13;-fx-font-weight:bold;");
        });

        // ── Séparateur envoi alerte ──
        Separator sep = new Separator();
        Label titreSend = new Label("📢 Envoyer une alerte au gestionnaire");
        titreSend.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:#2c3e50;");

        // Formulaire envoi alerte vers gestionnaire
        VBox zoneEnvoi = new VBox(10);
        zoneEnvoi.setPadding(new Insets(14));
        zoneEnvoi.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        TextField tfSujetEnvoi = new TextField(); tfSujetEnvoi.setPromptText("Sujet de l'alerte");
        ComboBox<String> cbTypeEnvoi = new ComboBox<>();
        cbTypeEnvoi.getItems().addAll("🔔 Alerte système", "💬 Information générale", "⚠️ Avertissement");
        cbTypeEnvoi.setValue("🔔 Alerte système");
        TextArea taEnvoi = new TextArea(); taEnvoi.setPromptText("Contenu de l'alerte..."); taEnvoi.setPrefHeight(90); taEnvoi.setWrapText(true);
        Label msgEnvoi = new Label(""); msgEnvoi.setStyle("-fx-font-size:12;"); msgEnvoi.setWrapText(true);

        Button btnSendAlerte = new Button("📤 Envoyer l'alerte");
        btnSendAlerte.setStyle("-fx-background-color:#e67e22;-fx-text-fill:white;-fx-padding:8 18;-fx-font-weight:bold;");
        btnSendAlerte.setOnAction(e -> {
            if (tfSujetEnvoi.getText().isEmpty() || taEnvoi.getText().isEmpty()) {
                msgEnvoi.setText("⚠️ Remplissez le sujet et le contenu."); msgEnvoi.setStyle("-fx-text-fill:#e67e22;"); return;
            }
            Message alerte = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), tfSujetEnvoi.getText().trim(), taEnvoi.getText().trim(),
                "ALERTE", false, null, "GESTIONNAIRE");
            try {
                msgDAO.envoyer(alerte);
                msgEnvoi.setText("✅ Alerte envoyée au gestionnaire."); msgEnvoi.setStyle("-fx-text-fill:#27ae60;");
                tfSujetEnvoi.clear(); taEnvoi.clear();
            } catch (Exception ex) { msgEnvoi.setText("❌ "+ex.getMessage()); msgEnvoi.setStyle("-fx-text-fill:#e74c3c;"); }
        });

        zoneEnvoi.getChildren().addAll(
            new HBox(10, new Label("Type :"), cbTypeEnvoi),
            tfSujetEnvoi, taEnvoi, btnSendAlerte, msgEnvoi);

        panel.getChildren().addAll(titre, lblStats, barFiltres, table, zoneDetail, zoneReponse,
            sep, titreSend, zoneEnvoi);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private List<Message> appliquerFiltre(String filtre) {
        List<Message> tous = msgDAO.obtenirPourAdmin();
        if (filtre == null || filtre.equals("Tous")) {
			return tous;
		}
        return tous.stream().filter(m -> {
            switch (filtre) {
                case "Non lus":  return !m.isLu();
                case "Alertes":  return "ALERTE".equals(m.getType()) || "RECLAMATION".equals(m.getType());
                case "Messages": return "GENERAL".equals(m.getType());
                default: return true;
            }
        }).collect(Collectors.toList());
    }
}
