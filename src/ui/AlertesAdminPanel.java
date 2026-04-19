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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Message;
import models.Utilisateur;

/**
 * Panel Alertes & Notifications pour l'Administrateur — redesigné.
 *   📬 Messages reçus  : boîte de réception + réponse rapide
 *   📤 Nouveau message  : formulaire libre vers le gestionnaire
 *
 * Logique métier inchangée.
 */
public class AlertesAdminPanel {

    private MessageDAO  msgDAO = new MessageDAO();
    private Utilisateur utilisateur;
    private static final DateTimeFormatter FMT  = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final DateTimeFormatter FMTL = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    public AlertesAdminPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public ScrollPane createPanel() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-font-size: 13; -fx-tab-min-height: 36;");

        Tab tabRecus = new Tab("📬  Messages reçus",  creerOngletRecus());
        Tab tabEnvoi = new Tab("📤  Nouveau message", creerOngletEnvoi());
        tabs.getTabs().addAll(tabRecus, tabEnvoi);

        VBox wrapper = new VBox(tabs);
        wrapper.setPadding(new Insets(16));
        wrapper.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════
    //  ONGLET 1 — MESSAGES REÇUS
    // ════════════════════════════════════════════════════════════════
    private VBox creerOngletRecus() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("📬  Messages reçus du Gestionnaire");

        // Badge non-lus
        int nonLus = msgDAO.compterNonLusAdmin();
        Label lblStats = new Label(
            nonLus > 0 ? "🔵  " + nonLus + " message(s) non lu(s)" : "✅  Tous les messages sont lus.");
        lblStats.setStyle(
            "-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;-fx-background-radius:8;" +
            "-fx-text-fill:" + (nonLus > 0 ? Design.DANGER : Design.SUCCESS) +
            ";-fx-background-color:" + (nonLus > 0 ? "#fdecea" : "#e8faf5") + ";"
        );
        panel.getChildren().addAll(titre, lblStats);

        // ── Barre actions + filtre ────────────────────────────────────
        HBox barre = new HBox(12);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.setPadding(new Insets(12, 16, 12, 16));
        barre.setStyle(Design.CARD_STYLE);

        Label lblFil = new Label("Filtrer :");
        lblFil.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";");

        ComboBox<String> cbFiltre = new ComboBox<>();
        cbFiltre.getItems().addAll("Tous", "Non lus", "Réservations", "Réclamations", "Messages");
        cbFiltre.setValue("Tous"); cbFiltre.setPrefWidth(180);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh  = Design.btnSecondary("🔄  Rafraîchir");
        Button btnTousLus  = Design.btnPrimary("✔  Tout marquer lu", Design.ADMIN_ACCENT);

        barre.getChildren().addAll(lblFil, cbFiltre, spacer, btnRefresh, btnTousLus);
        panel.getChildren().add(barre);

        // ── Tableau ───────────────────────────────────────────────────
        ObservableList<Message> items = FXCollections.observableArrayList(msgDAO.obtenirPourAdmin());
        TableView<Message> table = new TableView<>(items);
        table.setPrefHeight(270);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + Design.BORDER + ";");
        table.setPlaceholder(new Label("Aucun message reçu."));

        TableColumn<Message, String> cLu    = new TableColumn<>("");
        cLu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isLu() ? "" : "🔵"));
        cLu.setMaxWidth(36);
        TableColumn<Message, String> cType  = new TableColumn<>("Type");
        cType.setCellValueFactory(c -> new SimpleStringProperty(badgeType(c.getValue().getType())));
        cType.setMaxWidth(120);
        TableColumn<Message, String> cExp   = new TableColumn<>("Expéditeur");
        cExp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpediteurNom()));
        TableColumn<Message, String> cSujet = new TableColumn<>("Sujet");
        cSujet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSujet()));
        TableColumn<Message, String> cDate  = new TableColumn<>("Date");
        cDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt().format(FMT)));
        cDate.setMaxWidth(90);

        TableColumn<Message, Void> cAct = new TableColumn<>("Réponse rapide");
        cAct.setMaxWidth(120);
        cAct.setCellFactory(col -> new TableCell<>() {
            final Button btn = Design.btnPrimary("↩  Répondre", Design.ADMIN_ACCENT);
            {
                btn.setPadding(new Insets(4, 10, 4, 10));
                btn.setStyle(btn.getStyle() + "-fx-font-size: 11;");
                btn.setOnAction(e -> {
                    Message m = getTableView().getItems().get(getIndex());
                    ouvrirReponse(m, panel);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(cLu, cType, cExp, cSujet, cDate, cAct);

        // ── Détail du message ─────────────────────────────────────────
        VBox zoneDetail = new VBox(10);
        zoneDetail.setPadding(new Insets(16));
        zoneDetail.setStyle(Design.SECTION_STYLE);
        zoneDetail.setVisible(false);
        zoneDetail.setManaged(false);

        HBox hdrDetail = new HBox(10);
        hdrDetail.setAlignment(Pos.CENTER_LEFT);
        Label lblTitreMail = new Label("");
        lblTitreMail.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";");
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        Label lblMetaMail = new Label("");
        lblMetaMail.setStyle("-fx-font-size:11;-fx-text-fill:" + Design.TEXT_MUTED + ";");
        hdrDetail.getChildren().addAll(lblTitreMail, sp2, lblMetaMail);

        TextArea taMsg = new TextArea();
        taMsg.setEditable(false); taMsg.setPrefHeight(120);
        taMsg.setWrapText(true); taMsg.setStyle(Design.INPUT_STYLE);
        zoneDetail.getChildren().addAll(hdrDetail, taMsg);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { zoneDetail.setVisible(false); zoneDetail.setManaged(false); return; }
            lblTitreMail.setText(sel.getSujet());
            lblMetaMail.setText("De : " + sel.getExpediteurNom() + "   •   " + sel.getCreatedAt().format(FMTL));
            taMsg.setText(sel.getCorps());
            zoneDetail.setVisible(true); zoneDetail.setManaged(true);
            if (!sel.isLu()) {
                msgDAO.marquerLu(sel.getId()); sel.setLu(true); table.refresh();
                int n = msgDAO.compterNonLusAdmin();
                lblStats.setText(n > 0 ? "🔵  " + n + " message(s) non lu(s)" : "✅  Tous les messages sont lus.");
                lblStats.setStyle(
                    "-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;-fx-background-radius:8;" +
                    "-fx-text-fill:" + (n > 0 ? Design.DANGER : Design.SUCCESS) +
                    ";-fx-background-color:" + (n > 0 ? "#fdecea" : "#e8faf5") + ";"
                );
            }
        });

        // Actions
        cbFiltre.setOnAction(e -> {
            String v = cbFiltre.getValue();
            List<Message> source = msgDAO.obtenirPourAdmin();
            List<Message> filtered = switch (v) {
                case "Non lus"      -> source.stream().filter(m -> !m.isLu()).collect(Collectors.toList());
                case "Réservations" -> source.stream().filter(m -> "RESERVATION".equals(m.getType())).collect(Collectors.toList());
                case "Réclamations" -> source.stream().filter(m -> "RECLAMATION".equals(m.getType())).collect(Collectors.toList());
                case "Messages"     -> source.stream().filter(m -> "MESSAGE".equals(m.getType())).collect(Collectors.toList());
                default             -> source;
            };
            items.setAll(filtered);
        });

        btnRefresh.setOnAction(e -> {
            items.setAll(msgDAO.obtenirPourAdmin()); cbFiltre.setValue("Tous");
            int n = msgDAO.compterNonLusAdmin();
            lblStats.setText(n > 0 ? "🔵  " + n + " message(s) non lu(s)" : "✅  Tous les messages sont lus.");
        });

        btnTousLus.setOnAction(e -> {
            msgDAO.marquerTousLusAdmin();
            items.setAll(msgDAO.obtenirPourAdmin());
            lblStats.setText("✅  Tous les messages sont lus.");
            lblStats.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;-fx-background-radius:8;" +
                "-fx-text-fill:" + Design.SUCCESS + ";-fx-background-color:#e8faf5;");
        });

        panel.getChildren().addAll(table, zoneDetail);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════
    //  ONGLET 2 — NOUVEAU MESSAGE VERS LE GESTIONNAIRE
    // ════════════════════════════════════════════════════════════════
    private VBox creerOngletEnvoi() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("📤  Contacter le Gestionnaire");
        Label desc  = Design.muted("Envoyez un message ou une notification directement au gestionnaire.");
        panel.getChildren().addAll(titre, desc);

        VBox formBox = Design.section("✉️  Nouveau message");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 4, 0));

        ComboBox<String> cbCategorie = new ComboBox<>();
        cbCategorie.getItems().addAll("Notification système", "Instruction", "Alerte", "Question", "Autre");
        cbCategorie.setValue("Notification système"); cbCategorie.setPrefWidth(280);

        TextField tfSujet = sf("Objet de votre message…", 380);
        TextArea  taCorps = new TextArea();
        taCorps.setPromptText("Rédigez votre message…");
        taCorps.setPrefHeight(140); taCorps.setWrapText(true);
        taCorps.setStyle(Design.INPUT_STYLE);

        grid.add(fl("Catégorie :"), 0, 0); grid.add(cbCategorie, 1, 0);
        grid.add(fl("Sujet :"),     0, 1); grid.add(tfSujet,     1, 1);
        grid.add(fl("Message :"),   0, 2); grid.add(taCorps,     1, 2);

        Label msgEnvoi = new Label(""); msgEnvoi.setWrapText(true);

        Button btnEnvoyer = Design.btnPrimary("📤  Envoyer le message", Design.ADMIN_ACCENT);
        btnEnvoyer.setOnAction(e -> {
            if (tfSujet.getText().isEmpty() || taCorps.getText().isEmpty()) {
                setMsg(msgEnvoi, "⚠️  Le sujet et le message sont obligatoires.", Design.WARNING); return;
            }
            String sujet = "[" + cbCategorie.getValue() + "] " + tfSujet.getText().trim();
            Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, taCorps.getText().trim(), "ALERTE_ADMIN", false, null, "GESTIONNAIRE");
            try {
                msgDAO.envoyer(msg);
                setMsg(msgEnvoi, "✅  Message envoyé au gestionnaire.", Design.SUCCESS);
                tfSujet.clear(); taCorps.clear(); cbCategorie.setValue("Notification système");
            } catch (Exception ex) {
                setMsg(msgEnvoi, "❌  " + ex.getMessage(), Design.DANGER);
            }
        });

        formBox.getChildren().addAll(grid, btnEnvoyer, msgEnvoi);
        panel.getChildren().add(formBox);
        return panel;
    }

    // ── Réponse rapide inline ─────────────────────────────────────────
    private void ouvrirReponse(Message original, VBox panel) {
        // Construire une petite zone de réponse en bas du panel si pas déjà présente
        // (approche simple et non-intrusive)
        VBox zoneRep = new VBox(10);
        zoneRep.setPadding(new Insets(16));
        zoneRep.setStyle(
            "-fx-background-color: #f0f4ff;" +
            "-fx-border-color: " + Design.ADMIN_ACCENT + ";" +
            "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;"
        );

        Label lblRep = Design.sectionTitle("↩  Répondre à : " + original.getExpediteurNom());
        Label lblSujet = new Label("Re: " + original.getSujet());
        lblSujet.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";");

        TextArea taRep = new TextArea();
        taRep.setPromptText("Votre réponse…");
        taRep.setPrefHeight(100); taRep.setWrapText(true);
        taRep.setStyle(Design.INPUT_STYLE);

        Label msgR = new Label(""); msgR.setWrapText(true);
        Button btnEnvRep  = Design.btnPrimary("📤  Envoyer la réponse", Design.ADMIN_ACCENT);
        Button btnAnnuler = Design.btnSecondary("✖  Annuler");

        btnAnnuler.setOnAction(ev -> panel.getChildren().remove(zoneRep));

        btnEnvRep.setOnAction(ev -> {
            if (taRep.getText().isEmpty()) {
                setMsg(msgR, "⚠️  Saisissez votre réponse.", Design.WARNING); return;
            }
            String sujet = "Re: " + original.getSujet();
            String corps = taRep.getText().trim();
            Message rep = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, corps, "MESSAGE", false, null, "GESTIONNAIRE");
            try {
                msgDAO.envoyer(rep);
                setMsg(msgR, "✅  Réponse envoyée.", Design.SUCCESS);
                taRep.clear();
            } catch (Exception ex) {
                setMsg(msgR, "❌  " + ex.getMessage(), Design.DANGER);
            }
        });

        HBox btnBar = new HBox(10, btnEnvRep, btnAnnuler);
        zoneRep.getChildren().addAll(lblRep, lblSujet, taRep, btnBar, msgR);

        // Éviter les doublons
        panel.getChildren().removeIf(n -> n instanceof VBox vb &&
            vb.getStyle().contains("#f0f4ff"));
        panel.getChildren().add(zoneRep);
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private String badgeType(String type) {
        if (type == null) {
			return "";
		}
        return switch (type) {
            case "RESERVATION"  -> "📨 Réservation";
            case "RECLAMATION"  -> "🔧 Réclamation";
            case "ALERTE_ADMIN" -> "🔔 Alerte";
            case "MESSAGE"      -> "✉️ Message";
            default             -> type;
        };
    }

    private void setMsg(Label lbl, String text, String color) {
        lbl.setText(text);
        lbl.setStyle("-fx-font-size:12;-fx-text-fill:" + color + ";-fx-font-weight:bold;" +
            "-fx-padding:6 10;-fx-background-color:derive(" + color + ",85%);-fx-background-radius:6;");
    }

    private TextField sf(String prompt, double w) {
        TextField tf = new TextField();
        tf.setPromptText(prompt); tf.setPrefWidth(w); tf.setStyle(Design.INPUT_STYLE);
        return tf;
    }

    private Label fl(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";-fx-min-width:110;");
        return lbl;
    }
}
