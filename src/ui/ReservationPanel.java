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
import javafx.geometry.Pos;
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
 * Panel de demande de réservation de salle — redesigné.
 * Utilisé par les enseignants ET les étudiants.
 * Logique métier inchangée.
 */
public class ReservationPanel {

    private SalleDAO    salleDAO = new SalleDAO();
    private MessageDAO  msgDAO   = new MessageDAO();
    private Utilisateur utilisateur;

    public ReservationPanel(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public ScrollPane createPanel() {
        VBox panel = new VBox(22);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("📨  Demander une Réservation de Salle");
        Label desc  = Design.muted("Sélectionnez une salle, remplissez le formulaire et envoyez votre demande. Le gestionnaire la recevra dans sa boîte de réception.");
        panel.getChildren().addAll(titre, desc);

        // ── Tableau des salles ────────────────────────────────────────
        VBox sallesSection = Design.section("🚪  Salles disponibles — cliquez pour pré-sélectionner");

        List<Salle> salles = salleDAO.obtenirTous();
        TableView<Salle> tableSalles = new TableView<>(FXCollections.observableArrayList(salles));
        tableSalles.setPrefHeight(200);
        tableSalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableSalles.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + Design.BORDER + ";");
        tableSalles.setPlaceholder(new Label("Aucune salle enregistrée."));

        TableColumn<Salle, String>  cNum  = new TableColumn<>("Numéro");
        cNum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumero()));
        TableColumn<Salle, String>  cBat  = new TableColumn<>("Bâtiment");
        cBat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBatiment()));
        TableColumn<Salle, Integer> cCap  = new TableColumn<>("Capacité");
        cCap.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCapacite())); cCap.setMaxWidth(80);
        TableColumn<Salle, String>  cType = new TableColumn<>("Type");
        cType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType())); cType.setMaxWidth(75);
        TableColumn<Salle, String>  cEq   = new TableColumn<>("Équipements");
        cEq.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEquipementsStr()));
        tableSalles.getColumns().addAll(cNum, cBat, cCap, cType, cEq);

        sallesSection.getChildren().add(tableSalles);
        panel.getChildren().add(sallesSection);

        // ── Formulaire de demande ────────────────────────────────────
        VBox formBox = Design.section("📝  Formulaire de demande");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 4, 0));

        // Indicateur de salle sélectionnée
        Label lblSalleChoisie = new Label("Aucune salle sélectionnée — cliquez sur une ligne ci-dessus.");
        lblSalleChoisie.setStyle(
            "-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";" +
            "-fx-padding:8 12;-fx-background-color:#f8f9fe;-fx-background-radius:6;"
        );

        TextField  tfSalle  = new TextField();
        tfSalle.setPromptText("Ou saisissez directement le numéro de salle…");
        tfSalle.setPrefWidth(340);
        tfSalle.setStyle(Design.INPUT_STYLE);

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Réservation ponctuelle", "Cours supplémentaire", "Soutenance / Examen", "Réunion pédagogique", "Autre");
        cbType.setValue("Réservation ponctuelle"); cbType.setPrefWidth(280);

        TextField  tfMotif  = sf("Ex: Soutenance, Réunion, TD supplémentaire…", 340);
        DatePicker dp       = new DatePicker(LocalDate.now().plusDays(1));

        Spinner<Integer> spH = new Spinner<>(7, 22, 8);  spH.setPrefWidth(82); spH.setEditable(true);
        Spinner<Integer> spM = new Spinner<>(0, 59, 0);  spM.setPrefWidth(82); spM.setEditable(true);
        Spinner<Integer> spD = new Spinner<>(30, 480, 90); spD.setPrefWidth(90); spD.setEditable(true);

        HBox heureBox = new HBox(6, spH, new Label("h"), spM, new Label("min"));
        heureBox.setAlignment(Pos.CENTER_LEFT);

        TextArea taComment = new TextArea();
        taComment.setPromptText("Informations complémentaires, effectif prévu…");
        taComment.setPrefHeight(80); taComment.setPrefWidth(340);
        taComment.setWrapText(true); taComment.setStyle(Design.INPUT_STYLE);

        grid.add(fl("Salle souhaitée :"),  0, 0); grid.add(tfSalle,   1, 0);
        grid.add(fl("Type de demande :"),  0, 1); grid.add(cbType,    1, 1);
        grid.add(fl("Motif précis :"),     0, 2); grid.add(tfMotif,   1, 2);
        grid.add(fl("Date :"),             0, 3); grid.add(dp,        1, 3);
        grid.add(fl("Heure de début :"),   0, 4); grid.add(heureBox,  1, 4);
        grid.add(fl("Durée (min) :"),      0, 5); grid.add(spD,       1, 5);
        grid.add(fl("Commentaire :"),      0, 6); grid.add(taComment, 1, 6);

        // Pré-remplir quand on clique dans le tableau
        tableSalles.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                tfSalle.setText(sel.getNumero() + " — " + sel.getBatiment() + " (Cap: " + sel.getCapacite() + ")");
                lblSalleChoisie.setText("🚪  Salle sélectionnée : " + sel.getNumero() + " — " + sel.getBatiment() + " (Capacité : " + sel.getCapacite() + ")");
                lblSalleChoisie.setStyle(
                    "-fx-font-size:12;-fx-text-fill:" + Design.SUCCESS + ";-fx-font-weight:bold;" +
                    "-fx-padding:8 12;-fx-background-color:#e8faf5;-fx-background-radius:6;"
                );
            }
        });

        Label msgRes = new Label(""); msgRes.setWrapText(true);

        Button btnEnvoyer = Design.btnPrimary("📤  Envoyer la demande au gestionnaire", Design.GEST_ACCENT);
        btnEnvoyer.setOnAction(e -> {
            if (tfSalle.getText().isEmpty() || tfMotif.getText().isEmpty() || dp.getValue() == null) {
                setMsg(msgRes, "⚠️  Remplissez au moins la salle, le motif et la date.", Design.WARNING); return;
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
                setMsg(msgRes, "✅  Demande envoyée ! Le gestionnaire la recevra dans sa boîte de réception.", Design.SUCCESS);
                tfSalle.clear(); tfMotif.clear(); taComment.clear();
                tableSalles.getSelectionModel().clearSelection();
                lblSalleChoisie.setText("Aucune salle sélectionnée.");
                lblSalleChoisie.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";-fx-padding:8 12;-fx-background-color:#f8f9fe;-fx-background-radius:6;");
            } catch (Exception ex) {
                setMsg(msgRes, "❌  Erreur : " + ex.getMessage(), Design.DANGER);
            }
        });

        formBox.getChildren().addAll(lblSalleChoisie, grid, btnEnvoyer, msgRes);
        panel.getChildren().add(formBox);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ── Helpers ──────────────────────────────────────────────────────
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
        lbl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";-fx-min-width:140;");
        return lbl;
    }
}
