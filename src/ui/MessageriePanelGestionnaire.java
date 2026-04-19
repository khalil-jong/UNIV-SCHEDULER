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
 * Messagerie du gestionnaire — redesignée pour cohérence avec Design.java.
 *   📬 Boîte de réception  : messages des enseignants, étudiants et de l'admin
 *   📤 Contacter l'admin   : formulaire d'envoi vers l'administrateur
 *
 * Logique métier inchangée — seul le design est retravaillé.
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
        tabs.setStyle("-fx-font-size: 13; -fx-tab-min-height: 36;");

        Tab tabRecus = new Tab("📬  Boîte de réception",          creerOngletReception());
        Tab tabEnvoi = new Tab("📤  Contacter l'administrateur",   creerOngletEnvoi());
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
    //  ONGLET 1 — BOÎTE DE RÉCEPTION
    // ════════════════════════════════════════════════════════════════
    private VBox creerOngletReception() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("📬  Boîte de Réception");

        // ── Badge non-lus ─────────────────────────────────────────────
        int nonLus = msgDAO.compterNonLus();
        Label lblStats = new Label(
            nonLus > 0 ? "🔵  " + nonLus + " message(s) non lu(s)" : "✅  Tous les messages sont lus.");
        lblStats.setStyle(
            "-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;" +
            "-fx-background-radius:8;-fx-text-fill:" + (nonLus > 0 ? Design.DANGER : Design.SUCCESS) +
            ";-fx-background-color:" + (nonLus > 0 ? "#fdecea" : "#e8faf5") + ";"
        );

        panel.getChildren().addAll(titre, lblStats);

        // ── Barre de filtres + actions ────────────────────────────────
        HBox barFiltres = new HBox(12);
        barFiltres.setAlignment(Pos.CENTER_LEFT);
        barFiltres.setPadding(new Insets(12, 16, 12, 16));
        barFiltres.setStyle(Design.CARD_STYLE);

        Label lblFil = new Label("Filtrer :");
        lblFil.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";");

        ComboBox<String> cbFiltre = new ComboBox<>();
        cbFiltre.getItems().addAll("Tous", "Non lus", "Réservations", "Réclamations", "Alertes admin", "Messages");
        cbFiltre.setValue("Tous"); cbFiltre.setPrefWidth(190);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnTousLus = Design.btnPrimary("✔  Tout marquer lu", Design.GEST_ACCENT);
        Button btnRefresh = Design.btnSecondary("🔄  Rafraîchir");

        barFiltres.getChildren().addAll(lblFil, cbFiltre, spacer, btnRefresh, btnTousLus);
        panel.getChildren().add(barFiltres);

        // ── Tableau ───────────────────────────────────────────────────
        ObservableList<Message> allItems  = FXCollections.observableArrayList(msgDAO.obtenirTous());
        ObservableList<Message> items     = FXCollections.observableArrayList(allItems);
        TableView<Message> table = new TableView<>(items);
        table.setPrefHeight(280);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + Design.BORDER + ";");
        table.setPlaceholder(new Label("Aucun message reçu."));

        TableColumn<Message, String> cLu     = new TableColumn<>("");
        cLu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isLu() ? "" : "🔵"));
        cLu.setMaxWidth(36);

        TableColumn<Message, String> cType   = new TableColumn<>("Type");
        cType.setCellValueFactory(c -> new SimpleStringProperty(badgeType(c.getValue().getType())));
        cType.setMaxWidth(110);

        TableColumn<Message, String> cExp    = new TableColumn<>("Expéditeur");
        cExp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpediteurNom()));

        TableColumn<Message, String> cSujet  = new TableColumn<>("Sujet");
        cSujet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSujet()));

        TableColumn<Message, String> cDate   = new TableColumn<>("Date");
        cDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt().format(FMT)));
        cDate.setMaxWidth(90);

        table.getColumns().addAll(cLu, cType, cExp, cSujet, cDate);

        // ── Zone de détail ────────────────────────────────────────────
        VBox zoneDetail = new VBox(10);
        zoneDetail.setPadding(new Insets(16));
        zoneDetail.setStyle(Design.SECTION_STYLE);
        zoneDetail.setVisible(false);
        zoneDetail.setManaged(false);

        HBox headerDetail = new HBox(10);
        headerDetail.setAlignment(Pos.CENTER_LEFT);
        Label lblTitreMail  = new Label("");
        lblTitreMail.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";");
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        Label lblMetaMail   = new Label("");
        lblMetaMail.setStyle("-fx-font-size:11;-fx-text-fill:" + Design.TEXT_MUTED + ";");
        headerDetail.getChildren().addAll(lblTitreMail, sp2, lblMetaMail);

        TextArea taMsg = new TextArea();
        taMsg.setEditable(false); taMsg.setPrefHeight(120);
        taMsg.setWrapText(true); taMsg.setStyle(Design.INPUT_STYLE);
        zoneDetail.getChildren().addAll(headerDetail, taMsg);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { zoneDetail.setVisible(false); zoneDetail.setManaged(false); return; }
            lblTitreMail.setText(sel.getSujet());
            lblMetaMail.setText("De : " + sel.getExpediteurNom() + "   •   " + sel.getCreatedAt().format(FMTL));
            taMsg.setText(sel.getCorps());
            zoneDetail.setVisible(true); zoneDetail.setManaged(true);
            if (!sel.isLu()) {
                msgDAO.marquerLu(sel.getId()); sel.setLu(true); table.refresh();
                int n = msgDAO.compterNonLus();
                lblStats.setText(n > 0 ? "🔵  " + n + " message(s) non lu(s)" : "✅  Tous les messages sont lus.");
                lblStats.setStyle(
                    "-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;" +
                    "-fx-background-radius:8;-fx-text-fill:" + (n > 0 ? Design.DANGER : Design.SUCCESS) +
                    ";-fx-background-color:" + (n > 0 ? "#fdecea" : "#e8faf5") + ";"
                );
            }
        });

        // ── Actions filtres ───────────────────────────────────────────
        cbFiltre.setOnAction(e -> {
            String v = cbFiltre.getValue();
            List<Message> source = msgDAO.obtenirTous();
            List<Message> filtered = switch (v) {
                case "Non lus"       -> source.stream().filter(m -> !m.isLu()).collect(Collectors.toList());
                case "Réservations"  -> source.stream().filter(m -> "RESERVATION".equals(m.getType())).collect(Collectors.toList());
                case "Réclamations"  -> source.stream().filter(m -> "RECLAMATION".equals(m.getType())).collect(Collectors.toList());
                case "Alertes admin" -> source.stream().filter(m -> "ALERTE_ADMIN".equals(m.getType())).collect(Collectors.toList());
                case "Messages"      -> source.stream().filter(m -> "MESSAGE".equals(m.getType())).collect(Collectors.toList());
                default              -> source;
            };
            items.setAll(filtered);
        });

        btnRefresh.setOnAction(e -> {
            items.setAll(msgDAO.obtenirTous());
            cbFiltre.setValue("Tous");
            int n = msgDAO.compterNonLus();
            lblStats.setText(n > 0 ? "🔵  " + n + " message(s) non lu(s)" : "✅  Tous les messages sont lus.");
        });

        btnTousLus.setOnAction(e -> {
            msgDAO.marquerTousLus();
            items.setAll(msgDAO.obtenirTous());
            lblStats.setText("✅  Tous les messages sont lus.");
            lblStats.setStyle(
                "-fx-font-size:13;-fx-font-weight:bold;-fx-padding:8 14;" +
                "-fx-background-radius:8;-fx-text-fill:" + Design.SUCCESS + ";-fx-background-color:#e8faf5;"
            );
        });

        panel.getChildren().addAll(table, zoneDetail);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════
    //  ONGLET 2 — ENVOYER UN MESSAGE À L'ADMIN
    // ════════════════════════════════════════════════════════════════
    private VBox creerOngletEnvoi() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("📤  Contacter l'Administrateur");
        Label desc  = Design.muted("Envoyez un message ou une demande directement à l'administrateur système.");
        panel.getChildren().addAll(titre, desc);

        VBox formBox = Design.section("✉️  Nouveau message");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 4, 0));

        TextField tfSujet = sf("Objet de votre message…", 380);
        ComboBox<String> cbCategorie = new ComboBox<>();
        cbCategorie.getItems().addAll("Question générale", "Demande de ressource", "Problème technique", "Autre");
        cbCategorie.setValue("Question générale"); cbCategorie.setPrefWidth(280);

        TextArea taCorps = new TextArea();
        taCorps.setPromptText("Rédigez votre message…");
        taCorps.setPrefHeight(140); taCorps.setWrapText(true);
        taCorps.setStyle(Design.INPUT_STYLE);

        grid.add(fl("Catégorie :"), 0, 0); grid.add(cbCategorie, 1, 0);
        grid.add(fl("Sujet :"),     0, 1); grid.add(tfSujet,     1, 1);
        grid.add(fl("Message :"),   0, 2); grid.add(taCorps,     1, 2);

        Label msgEnvoi = new Label(""); msgEnvoi.setWrapText(true);

        Button btnEnvoyer = Design.btnPrimary("📤  Envoyer le message", Design.GEST_ACCENT);
        btnEnvoyer.setOnAction(e -> {
            if (tfSujet.getText().isEmpty() || taCorps.getText().isEmpty()) {
                setMsg(msgEnvoi, "⚠️  Le sujet et le message sont obligatoires.", Design.WARNING); return;
            }
            if (utilisateur == null) {
                setMsg(msgEnvoi, "❌  Utilisateur non identifié.", Design.DANGER); return;
            }
            String sujet = "[" + cbCategorie.getValue() + "] " + tfSujet.getText().trim();
            Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, taCorps.getText().trim(), "MESSAGE", false, null, "ADMIN");
            try {
                msgDAO.envoyer(msg);
                setMsg(msgEnvoi, "✅  Message envoyé à l'administrateur.", Design.SUCCESS);
                tfSujet.clear(); taCorps.clear(); cbCategorie.setValue("Question générale");
            } catch (Exception ex) {
                setMsg(msgEnvoi, "❌  " + ex.getMessage(), Design.DANGER);
            }
        });

        formBox.getChildren().addAll(grid, btnEnvoyer, msgEnvoi);
        panel.getChildren().add(formBox);
        return panel;
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
