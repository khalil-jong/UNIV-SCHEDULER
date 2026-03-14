package ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.MessageDAO;
import dao.SalleDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Message;
import models.Salle;
import models.Utilisateur;

/**
 * Panel de demande de réservation de salle (enseignant ET étudiant).
 * Le message est enregistré en base et apparaît dans la boîte de réception du gestionnaire.
 */
public class ReservationPanel {

    private SalleDAO   salleDAO  = new SalleDAO();
    private MessageDAO msgDAO    = new MessageDAO();
    private Utilisateur utilisateur;

    public ReservationPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public ScrollPane createPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(25));

        Label titre = new Label("📨 Demander une Réservation de Salle");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Label desc = new Label("Sélectionnez une salle et remplissez le formulaire. Le gestionnaire recevra votre demande dans sa boîte de réception.");
        desc.setStyle("-fx-font-size: 12; -fx-text-fill: #555;"); desc.setWrapText(true);

        // Tableau des salles
        Label lblSalles = new Label("🏫 Salles disponibles (cliquez pour pré-remplir) :");
        lblSalles.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        List<Salle> salles = salleDAO.obtenirTous();
        TableView<Salle> tableSalles = new TableView<>(FXCollections.observableArrayList(salles));
        tableSalles.setPrefHeight(185);

        TableColumn<Salle,String>  cNum  = new TableColumn<>("Numéro");  cNum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumero())); cNum.setPrefWidth(80);
        TableColumn<Salle,String>  cBat  = new TableColumn<>("Bâtiment"); cBat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBatiment())); cBat.setPrefWidth(120);
        TableColumn<Salle,Integer> cCap  = new TableColumn<>("Capacité"); cCap.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCapacite())); cCap.setPrefWidth(70);
        TableColumn<Salle,String>  cType = new TableColumn<>("Type");     cType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType())); cType.setPrefWidth(70);
        TableColumn<Salle,String>  cEq   = new TableColumn<>("Équipements"); cEq.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEquipementsStr())); cEq.setPrefWidth(150);
        tableSalles.getColumns().addAll(cNum, cBat, cCap, cType, cEq);

        // Formulaire
        Label lblForm = new Label("📝 Formulaire de demande :");
        lblForm.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(12);
        grid.setPadding(new Insets(14)); grid.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");

        TextField  tfSalle  = new TextField(); tfSalle.setPromptText("Cliquez sur une salle ci-dessus ou saisissez"); tfSalle.setPrefWidth(300);
        TextField  tfMotif  = new TextField(); tfMotif.setPromptText("Ex: Soutenance, Réunion, TD supplémentaire..."); tfMotif.setPrefWidth(300);
        DatePicker dp       = new DatePicker(LocalDate.now().plusDays(1));
        Spinner<Integer> spH = new Spinner<>(7,22,8);  spH.setPrefWidth(80);
        Spinner<Integer> spM = new Spinner<>(0,59,0);  spM.setPrefWidth(80);
        Spinner<Integer> spD = new Spinner<>(30,480,90); spD.setPrefWidth(90);
        TextArea taComment  = new TextArea(); taComment.setPromptText("Informations complémentaires, effectif..."); taComment.setPrefHeight(70); taComment.setPrefWidth(300);

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Réservation ponctuelle", "Cours supplémentaire", "Soutenance / Examen", "Réunion pédagogique", "Autre");
        cbType.setValue("Réservation ponctuelle");

        grid.add(new Label("Salle souhaitée :"),  0,0); grid.add(tfSalle,  1,0);
        grid.add(new Label("Type de demande :"),  0,1); grid.add(cbType,   1,1);
        grid.add(new Label("Motif précis :"),      0,2); grid.add(tfMotif,  1,2);
        grid.add(new Label("Date :"),              0,3); grid.add(dp,       1,3);
        grid.add(new Label("Heure de début :"),    0,4); grid.add(new HBox(6,new Label("h"),spH,new Label("min"),spM),1,4);
        grid.add(new Label("Durée (min) :"),       0,5); grid.add(spD,      1,5);
        grid.add(new Label("Commentaire :"),       0,6); grid.add(taComment,1,6);

        // Pré-remplir quand on clique dans le tableau
        tableSalles.getSelectionModel().selectedItemProperty().addListener((obs,old,sel) -> {
            if (sel != null) {
				tfSalle.setText(sel.getNumero() + " — " + sel.getBatiment() + " (Cap: " + sel.getCapacite() + ")");
			}
        });

        Label msgRes = new Label(""); msgRes.setStyle("-fx-font-size: 12;"); msgRes.setWrapText(true);

        Button btnEnvoyer = new Button("📤 Envoyer la demande au gestionnaire");
        btnEnvoyer.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");

        btnEnvoyer.setOnAction(e -> {
            if (tfSalle.getText().isEmpty() || tfMotif.getText().isEmpty() || dp.getValue() == null) {
                msgRes.setText("⚠️ Remplissez au moins la salle, le motif et la date.");
                msgRes.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12;"); return;
            }
            String sujet = "[" + cbType.getValue() + "] " + tfMotif.getText().trim()
                + " — Salle " + tfSalle.getText().split("—")[0].trim();
            String corps = "Demande de : " + utilisateur.getNomComplet() + " (" + utilisateur.getRole() + ")\n\n"
                + "Type         : " + cbType.getValue() + "\n"
                + "Salle        : " + tfSalle.getText() + "\n"
                + "Motif        : " + tfMotif.getText().trim() + "\n"
                + "Date         : " + dp.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n"
                + "Heure        : " + String.format("%02dh%02d", spH.getValue(), spM.getValue()) + "\n"
                + "Durée        : " + spD.getValue() + " min\n"
                + "Commentaire  : " + (taComment.getText().isEmpty() ? "—" : taComment.getText());

            Message msg = new Message(0, utilisateur.getId(), utilisateur.getNomComplet(),
                utilisateur.getRole(), sujet, corps, "RESERVATION", false, null);
            try {
                msgDAO.envoyer(msg);
                msgRes.setText("✅ Demande envoyée ! Le gestionnaire la recevra dans sa boîte de réception.");
                msgRes.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
                tfSalle.clear(); tfMotif.clear(); taComment.clear();
                tableSalles.getSelectionModel().clearSelection();
            } catch (Exception ex) {
                msgRes.setText("❌ Erreur : " + ex.getMessage());
                msgRes.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
            }
        });

        panel.getChildren().addAll(titre, desc, lblSalles, tableSalles, lblForm, grid, btnEnvoyer, msgRes);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }
}
