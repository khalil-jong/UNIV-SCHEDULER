package ui;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Message;

/**
 * Boîte de réception du gestionnaire :
 * affiche les messages/demandes de réservation envoyés par enseignants et étudiants.
 */
public class MessageriePanelGestionnaire {

    private MessageDAO msgDAO = new MessageDAO();
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    public ScrollPane createPanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(20));

        Label titre = new Label("📬 Boîte de Réception");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Stats non lus
        int nonLus = msgDAO.compterNonLus();
        Label lblStats = new Label(nonLus > 0
            ? nonLus + " message(s) non lu(s)"
            : "Tous les messages sont lus.");
        lblStats.setStyle("-fx-font-size: 13; -fx-text-fill: " + (nonLus > 0 ? "#e74c3c" : "#27ae60") + "; -fx-font-weight: bold;");

        // Barre de filtres
        HBox barFiltres = new HBox(10);
        barFiltres.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> cbFiltre = new ComboBox<>();
        cbFiltre.getItems().addAll("Tous", "Non lus", "Réservations", "Réclamations", "Alertes admin", "Messages");
        cbFiltre.setValue("Tous");

        Button btnTousLus = new Button("✔ Tout marquer lu");
        btnTousLus.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        Button btnRefresh = new Button("🔄");

        barFiltres.getChildren().addAll(new Label("Afficher :"), cbFiltre, btnTousLus, btnRefresh);

        // Tableau des messages
        ObservableList<Message> items = FXCollections.observableArrayList(msgDAO.obtenirTous());

        TableView<Message> table = new TableView<>(items);
        table.setPrefHeight(320);
        table.setPlaceholder(new Label("Aucun message reçu."));

        TableColumn<Message, String> cLu = new TableColumn<>("");
        cLu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isLu() ? "" : "🔵"));
        cLu.setPrefWidth(30);

        TableColumn<Message, String> cType = new TableColumn<>("Type");
        cType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeLabel()));
        cType.setPrefWidth(120);

        TableColumn<Message, String> cExp = new TableColumn<>("Expéditeur");
        cExp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpediteurNom() + " (" + c.getValue().getExpediteurRole() + ")"));
        cExp.setPrefWidth(170);

        TableColumn<Message, String> cSujet = new TableColumn<>("Sujet");
        cSujet.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSujet()));
        cSujet.setPrefWidth(240);

        TableColumn<Message, String> cDate = new TableColumn<>("Date");
        cDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt().format(fmt)));
        cDate.setPrefWidth(80);

        TableColumn<Message, Void> cAct = new TableColumn<>("Action");
        cAct.setPrefWidth(90);
        cAct.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("🗑 Suppr.");
            { btn.setStyle("-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-font-size:11;");
              btn.setOnAction(e -> {
                Message m = getTableView().getItems().get(getIndex());
                msgDAO.supprimer(m.getId());
                items.setAll(appliquerFiltre(cbFiltre.getValue()));
              });
            }
            @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v,empty); setGraphic(empty?null:btn); }
        });

        table.getColumns().addAll(cLu, cType, cExp, cSujet, cDate, cAct);

        // Zone de lecture du message sélectionné
        VBox zoneDetail = new VBox(8);
        zoneDetail.setPadding(new Insets(14));
        zoneDetail.setStyle("-fx-border-color:#ddd;-fx-border-radius:6;-fx-background-color:white;");
        zoneDetail.setVisible(false);

        Label lblDetailTitre = new Label("");
        lblDetailTitre.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        Label lblDetailMeta  = new Label("");
        lblDetailMeta.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        TextArea taDetail = new TextArea();
        taDetail.setEditable(false); taDetail.setPrefHeight(140); taDetail.setWrapText(true);

        zoneDetail.getChildren().addAll(lblDetailTitre, lblDetailMeta, taDetail);

        // Clic sur une ligne → afficher détail + marquer lu
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { zoneDetail.setVisible(false); return; }
            lblDetailTitre.setText(sel.getTypeLabel() + "  —  " + sel.getSujet());
            lblDetailMeta.setText("De : " + sel.getExpediteurNom() + " (" + sel.getExpediteurRole()
                + ")   •   " + sel.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
            taDetail.setText(sel.getCorps());
            zoneDetail.setVisible(true);
            if (!sel.isLu()) { msgDAO.marquerLu(sel.getId()); sel.setLu(true); table.refresh(); }
        });

        // Filtres
        cbFiltre.setOnAction(e -> items.setAll(appliquerFiltre(cbFiltre.getValue())));
        btnTousLus.setOnAction(e -> { msgDAO.marquerTousLus(); items.setAll(appliquerFiltre(cbFiltre.getValue())); table.refresh(); lblStats.setText("Tous les messages sont lus."); lblStats.setStyle("-fx-text-fill:#27ae60;-fx-font-size:13;-fx-font-weight:bold;"); });
        btnRefresh.setOnAction(e -> { items.setAll(appliquerFiltre(cbFiltre.getValue())); int n = msgDAO.compterNonLus(); lblStats.setText(n>0?n+" message(s) non lu(s)":"Tous les messages sont lus."); lblStats.setStyle("-fx-text-fill:"+(n>0?"#e74c3c":"#27ae60")+";-fx-font-size:13;-fx-font-weight:bold;"); });

        panel.getChildren().addAll(titre, lblStats, barFiltres, table, zoneDetail);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private List<Message> appliquerFiltre(String filtre) {
        if (filtre == null || filtre.equals("Tous")) {
			return msgDAO.obtenirTous();
		}
        List<Message> tous = msgDAO.obtenirTous();
        return tous.stream().filter(m -> {
            switch (filtre) {
                case "Non lus":       return !m.isLu();
                case "Réservations":  return "RESERVATION".equals(m.getType());
                case "Réclamations":  return "RECLAMATION".equals(m.getType());
                case "Alertes admin": return "ALERTE".equals(m.getType());
                case "Messages":      return "GENERAL".equals(m.getType());
                default: return true;
            }
        }).collect(java.util.stream.Collectors.toList());
    }
}
