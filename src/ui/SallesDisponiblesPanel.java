package ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import dao.SalleDAO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import models.Salle;

/**
 * Panel Salles Disponibles redesigné — 2 onglets :
 *   🟢 Disponibilité temps réel  : grille de toutes les salles avec statut coloré
 *   🔍 Recherche avancée         : filtres date/heure/durée/capacité/type/équipements
 *
 * Logique métier inchangée. Design harmonisé avec Design.java.
 */
public class SallesDisponiblesPanel {

    private SalleDAO salleDAO = new SalleDAO();
    private static final DateTimeFormatter H_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ScrollPane createPanel() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-font-size: 13; -fx-tab-min-height: 36;");

        Tab tabTempsReel = new Tab("🟢  Disponibilité temps réel", creerOngletTempsReel());
        Tab tabRecherche = new Tab("🔍  Recherche avancée",         creerOngletRecherche());
        tabs.getTabs().addAll(tabTempsReel, tabRecherche);

        VBox wrapper = new VBox(tabs);
        wrapper.setPadding(new Insets(16));
        wrapper.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ════════════════════════════════════════════════════════════════
    //  ONGLET 1 — TEMPS RÉEL
    // ════════════════════════════════════════════════════════════════
    private VBox creerOngletTempsReel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        // ── En-tête avec horloge ──────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titreBox = new VBox(3);
        Label titre = Design.pageTitle("🟢  Disponibilité — Temps Réel");
        Label lblHeure = new Label("");
        lblHeure.setStyle("-fx-font-size: 12; -fx-text-fill: " + Design.TEXT_MUTED + "; -fx-font-weight: bold;");
        titreBox.getChildren().addAll(titre, lblHeure);
        header.getChildren().add(titreBox);
        panel.getChildren().add(header);

        // ── Légende ───────────────────────────────────────────────────
        HBox legende = new HBox(24);
        legende.setAlignment(Pos.CENTER_LEFT);
        legende.setPadding(new Insets(10, 14, 10, 14));
        legende.setStyle(Design.CARD_STYLE);
        legende.getChildren().addAll(
            badgeLegende("🟢  Disponible",   Design.SUCCESS),
            badgeLegende("🔴  Occupée",       Design.DANGER),
            badgeLegende("🟡  Bientôt libre", Design.WARNING)
        );
        panel.getChildren().add(legende);

        // ── Filtres rapides ───────────────────────────────────────────
        HBox filtresBox = new HBox(12);
        filtresBox.setAlignment(Pos.CENTER_LEFT);
        filtresBox.setPadding(new Insets(12, 16, 12, 16));
        filtresBox.setStyle(Design.CARD_STYLE);

        Label lblType = new Label("Type :");
        lblType.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";");
        ComboBox<String> cbTypeFiltre = new ComboBox<>();
        cbTypeFiltre.getItems().addAll("Tous les types", "TD", "TP", "Amphi");
        cbTypeFiltre.setValue("Tous les types"); cbTypeFiltre.setPrefWidth(150);

        // Séparateur vertical
        Separator sepV = new Separator(javafx.geometry.Orientation.VERTICAL);

        Label lblStatut = new Label("Statut :");
        lblStatut.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";");

        ToggleGroup tgStatut = new ToggleGroup();
        ToggleButton tbTout  = styledToggle("Toutes",       tgStatut, true);
        ToggleButton tbDispo = styledToggle("🟢  Libres",    tgStatut, false);
        ToggleButton tbOccup = styledToggle("🔴  Occupées",  tgStatut, false);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnRefresh = Design.btnSecondary("🔄  Actualiser");

        filtresBox.getChildren().addAll(lblType, cbTypeFiltre, sepV, lblStatut, tbTout, tbDispo, tbOccup, spacer, btnRefresh);
        panel.getChildren().add(filtresBox);

        // ── Compteurs + grille ────────────────────────────────────────
        Label lblCompteurs = new Label("");
        lblCompteurs.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";");

        FlowPane grille = new FlowPane(14, 14);
        grille.setPadding(new Insets(4));

        // Fonction de rafraîchissement
        Runnable refresh = () -> {
            LocalDateTime now = LocalDateTime.now();
            lblHeure.setText("⏱  " + now.format(DateTimeFormatter.ofPattern(
                "HH:mm:ss  —  EEEE dd/MM/yyyy", java.util.Locale.FRENCH)));
            List<Salle> salles = salleDAO.obtenirTous();

            String typeChoisi = cbTypeFiltre.getValue();
            if (typeChoisi != null && !typeChoisi.equals("Tous les types")) {
				salles = salles.stream().filter(s -> s.getType().equals(typeChoisi)).collect(Collectors.toList());
			}

            Toggle sel = tgStatut.getSelectedToggle();
            List<Salle> sallesFiltrees = salles;
            if (sel == tbDispo) {
				sallesFiltrees = salles.stream().filter(s -> !salleDAO.estOccupeeMaintenantET(s.getId(), now)).collect(Collectors.toList());
			} else if (sel == tbOccup) {
				sallesFiltrees = salles.stream().filter(s -> salleDAO.estOccupeeMaintenantET(s.getId(), now)).collect(Collectors.toList());
			}

            long nbLibres = sallesFiltrees.stream().filter(s -> !salleDAO.estOccupeeMaintenantET(s.getId(), now)).count();
            long nbOccup  = sallesFiltrees.size() - nbLibres;
            lblCompteurs.setText("🟢  " + nbLibres + " libre(s)   🔴  " + nbOccup + " occupée(s)   —  " + sallesFiltrees.size() + " affichée(s)");

            grille.getChildren().clear();
            for (Salle s : sallesFiltrees) {
                boolean occupee  = salleDAO.estOccupeeMaintenantET(s.getId(), now);
                String  occupant = occupee ? salleDAO.getOccupantActuel(s.getId(), now) : "";
                grille.getChildren().add(carteSalle(s, occupee, occupant, now));
            }
        };

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(60), e -> refresh.run()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        btnRefresh.setOnAction(e -> refresh.run());
        cbTypeFiltre.setOnAction(e -> refresh.run());
        tbTout.setOnAction(e -> refresh.run());
        tbDispo.setOnAction(e -> refresh.run());
        tbOccup.setOnAction(e -> refresh.run());

        refresh.run();

        panel.getChildren().addAll(lblCompteurs, grille);
        return panel;
    }

    /** Carte visuelle d'une salle avec statut coloré — design modernisé */
    private VBox carteSalle(Salle s, boolean occupee, String occupant, LocalDateTime now) {
        VBox carte = new VBox(6);
        carte.setPrefWidth(210);
        carte.setPadding(new Insets(12, 14, 12, 14));

        String bgColor   = occupee ? "#fff5f5" : "#f0faf5";
        String bordColor = occupee ? Design.DANGER : Design.SUCCESS;
        String statutTxt = occupee ? "🔴  Occupée" : "🟢  Disponible";

        carte.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-border-color: " + bordColor + ";" +
            "-fx-border-width: 0 0 0 4;" +
            "-fx-border-radius: 0 10 10 0;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        Label lNum = new Label("🚪  " + s.getNumero());
        lNum.setStyle("-fx-font-size:14;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";");

        Label lBat = new Label(s.getBatiment() + "  ·  " + s.getEtage());
        lBat.setStyle("-fx-font-size:11;-fx-text-fill:" + Design.TEXT_MUTED + ";");

        Label lInfo = new Label("👥  " + s.getCapacite() + "  ·  " + s.getType());
        lInfo.setStyle("-fx-font-size:11;-fx-text-fill:" + Design.TEXT_MUTED + ";");

        if (!s.getEquipementsStr().isEmpty()) {
            Label lEquip = new Label("🔧  " + s.getEquipementsStr());
            lEquip.setStyle("-fx-font-size:10;-fx-text-fill:" + Design.TEXT_MUTED + ";");
            lEquip.setWrapText(true);
            carte.getChildren().add(lEquip);
        }

        Label lStatut = new Label(statutTxt);
        lStatut.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + bordColor + ";");

        carte.getChildren().addAll(lNum, lBat, lInfo, lStatut);

        if (occupee && !occupant.isEmpty()) {
            Label lOcc = new Label(occupant);
            lOcc.setStyle("-fx-font-size:10;-fx-text-fill:#c0392b;");
            lOcc.setWrapText(true); lOcc.setMaxWidth(182);
            carte.getChildren().add(lOcc);
        }

        Tooltip tip = new Tooltip(
            "Salle : " + s.getNumero() + " — " + s.getBatiment() + "\n" +
            "Capacité : " + s.getCapacite() + "  |  Type : " + s.getType() + "\n" +
            "Équipements : " + (s.getEquipementsStr().isEmpty() ? "—" : s.getEquipementsStr()) + "\n" +
            (occupee ? "▶ En cours : " + occupant : "✔ Disponible maintenant") + "\n" +
            "Mis à jour : " + now.format(H_FMT));
        tip.setStyle("-fx-font-size: 12;");
        Tooltip.install(carte, tip);

        return carte;
    }

    // ════════════════════════════════════════════════════════════════
    //  ONGLET 2 — RECHERCHE AVANCÉE
    // ════════════════════════════════════════════════════════════════
    private VBox creerOngletRecherche() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("🔍  Recherche Avancée de Salles");
        Label desc  = Design.muted("Sélectionnez la date, l'heure, la durée et vos critères puis lancez la recherche.");
        panel.getChildren().addAll(titre, desc);

        // ── Formulaire de filtres ─────────────────────────────────────
        VBox formBox = Design.section("⚙️  Critères de recherche");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 4, 0));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        Spinner<Integer> spH   = new Spinner<>(7, 22, LocalTime.now().getHour()); spH.setPrefWidth(80); spH.setEditable(true);
        Spinner<Integer> spMin = new Spinner<>(0, 59, 0, 5); spMin.setPrefWidth(80); spMin.setEditable(true);
        Spinner<Integer> spDur = new Spinner<>(15, 480, 60, 15); spDur.setPrefWidth(90); spDur.setEditable(true);
        Spinner<Integer> spCap = new Spinner<>(0, 500, 0, 5); spCap.setPrefWidth(90); spCap.setEditable(true);

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Tous", "TD", "TP", "Amphi");
        cbType.setValue("Tous"); cbType.setPrefWidth(120);

        CheckBox chkVideo = new CheckBox("📽  Vidéoprojecteur");
        CheckBox chkTI    = new CheckBox("🖥  Tableau interactif");
        CheckBox chkClim  = new CheckBox("❄  Climatisation");
        HBox equipBox = new HBox(16, chkVideo, chkTI, chkClim);
        equipBox.setAlignment(Pos.CENTER_LEFT);

        HBox heureBox = new HBox(6, spH, new Label("h"), spMin, new Label("min"));
        heureBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(fl("📅  Date :"),             0, 0); grid.add(datePicker, 1, 0);
        grid.add(fl("🕐  Heure début :"),      0, 1); grid.add(heureBox,   1, 1);
        grid.add(fl("⏱  Durée (min) :"),      0, 2); grid.add(spDur,      1, 2);
        grid.add(fl("👥  Capacité min. :"),    0, 3); grid.add(new HBox(8, spCap, Design.muted("(0 = indifférent)")), 1, 3);
        grid.add(fl("🚪  Type de salle :"),    0, 4); grid.add(cbType,     1, 4);
        grid.add(fl("🔧  Équipements :"),      0, 5); grid.add(equipBox,   1, 5);

        Button btnMaintenant = Design.btnPrimary("⚡  Disponible maintenant", Design.SUCCESS);
        Button btnRechercher = Design.btnPrimary("🔍  Rechercher", Design.GEST_ACCENT);
        Button btnReset      = Design.btnSecondary("🔄  Réinitialiser");

        HBox boutons = new HBox(10, btnMaintenant, btnRechercher, btnReset);
        boutons.setAlignment(Pos.CENTER_LEFT);
        formBox.getChildren().addAll(grid, boutons);
        panel.getChildren().add(formBox);

        // ── Résultats ─────────────────────────────────────────────────
        Label lblInfo = Design.muted("Renseignez les critères et cliquez sur Rechercher.");

        TableView<Salle> table = new TableView<>();
        table.setPrefHeight(280);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: " + Design.BORDER + ";");
        table.setPlaceholder(new Label("Aucune salle ne correspond aux critères."));

        TableColumn<Salle, String>  cNum   = col("Numéro",       s -> s.getNumero());
        TableColumn<Salle, String>  cBat   = col("Bâtiment",     s -> s.getBatiment());
        TableColumn<Salle, String>  cEtage = col("Étage",        s -> s.getEtage());
        TableColumn<Salle, Integer> cCap   = new TableColumn<>("Capacité");
        cCap.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCapacite()));
        cCap.setMaxWidth(80);
        TableColumn<Salle, String>  cType2 = col("Type",         s -> s.getType());
        cType2.setMaxWidth(70);
        TableColumn<Salle, String>  cEquip = col("Équipements",  s -> s.getEquipementsStr().isEmpty() ? "—" : s.getEquipementsStr());
        table.getColumns().addAll(cNum, cBat, cEtage, cCap, cType2, cEquip);

        // ── Détail créneaux au clic ───────────────────────────────────
        VBox zoneCreneaux = new VBox(10);
        zoneCreneaux.setPadding(new Insets(14));
        zoneCreneaux.setStyle(Design.SECTION_STYLE);
        zoneCreneaux.setVisible(false);
        zoneCreneaux.setManaged(false);

        Label lblCreneauxTitre = Design.sectionTitle("");
        FlowPane creneauxFlow  = new FlowPane(8, 8);
        zoneCreneaux.getChildren().addAll(lblCreneauxTitre, creneauxFlow);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { zoneCreneaux.setVisible(false); zoneCreneaux.setManaged(false); return; }
            LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
            lblCreneauxTitre.setText("📅  Créneaux de " + sel.getNumero() + " le " + date.format(D_FMT) + " :");
            creneauxFlow.getChildren().clear();
            creneauxFlow.getChildren().addAll(creerBadgesCreneaux(sel, date));
            zoneCreneaux.setVisible(true); zoneCreneaux.setManaged(true);
        });

        // ── Actions ───────────────────────────────────────────────────
        btnMaintenant.setOnAction(e -> {
            datePicker.setValue(LocalDate.now());
            spH.getValueFactory().setValue(LocalTime.now().getHour());
            spMin.getValueFactory().setValue(0);
            LocalDateTime now = LocalDateTime.now();
            List<Salle> res = filtrer(salleDAO.obtenirSallesDisponibles(now, spDur.getValue()),
                spCap.getValue(), cbType.getValue(), chkVideo.isSelected(), chkTI.isSelected(), chkClim.isSelected());
            table.setItems(FXCollections.observableArrayList(res));
            setInfo(lblInfo, res.isEmpty()
                ? "❌  Aucune salle disponible maintenant pour " + spDur.getValue() + " min."
                : "⚡  " + res.size() + " salle(s) disponible(s) en ce moment pour " + spDur.getValue() + " min.",
                res.isEmpty() ? Design.DANGER : Design.SUCCESS);
            zoneCreneaux.setVisible(false); zoneCreneaux.setManaged(false);
        });

        btnRechercher.setOnAction(e -> {
            if (datePicker.getValue() == null) {
                setInfo(lblInfo, "⚠️  Sélectionnez une date.", Design.WARNING); return;
            }
            LocalDateTime debut = LocalDateTime.of(datePicker.getValue(),
                LocalTime.of(spH.getValue(), spMin.getValue()));
            List<Salle> res = filtrer(salleDAO.obtenirSallesDisponibles(debut, spDur.getValue()),
                spCap.getValue(), cbType.getValue(), chkVideo.isSelected(), chkTI.isSelected(), chkClim.isSelected());
            table.setItems(FXCollections.observableArrayList(res));
            String msg = res.size() + " salle(s) disponible(s) le "
                + datePicker.getValue().format(D_FMT) + " à "
                + String.format("%02dh%02d", spH.getValue(), spMin.getValue())
                + " pour " + spDur.getValue() + " min.";
            setInfo(lblInfo,
                res.isEmpty() ? "❌  Aucune salle disponible pour ces critères." : "✅  " + msg,
                res.isEmpty() ? Design.DANGER : Design.SUCCESS);
            zoneCreneaux.setVisible(false); zoneCreneaux.setManaged(false);
        });

        btnReset.setOnAction(e -> {
            datePicker.setValue(LocalDate.now());
            spH.getValueFactory().setValue(8); spMin.getValueFactory().setValue(0);
            spDur.getValueFactory().setValue(60); spCap.getValueFactory().setValue(0);
            cbType.setValue("Tous");
            chkVideo.setSelected(false); chkTI.setSelected(false); chkClim.setSelected(false);
            table.setItems(FXCollections.observableArrayList());
            lblInfo.setText("Renseignez les critères et cliquez sur Rechercher.");
            lblInfo.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";");
            zoneCreneaux.setVisible(false); zoneCreneaux.setManaged(false);
        });

        panel.getChildren().addAll(lblInfo, table, zoneCreneaux);
        return panel;
    }

    /** Badges créneaux horaires d'une salle pour un jour donné */
    private List<javafx.scene.Node> creerBadgesCreneaux(Salle salle, LocalDate date) {
        List<javafx.scene.Node> badges = new java.util.ArrayList<>();
        for (int h = 7; h <= 20; h++) {
            LocalDateTime moment = date.atTime(h, 0);
            boolean occ      = salleDAO.estOccupeeMaintenantET(salle.getId(), moment);
            String  occupant = occ ? salleDAO.getOccupantActuel(salle.getId(), moment) : "Libre";
            Label badge = new Label(String.format("%02dh", h));
            badge.setPadding(new Insets(5, 10, 5, 10));
            badge.setStyle(
                "-fx-background-color:" + (occ ? Design.DANGER : Design.SUCCESS) + ";" +
                "-fx-text-fill:white;-fx-background-radius:6;" +
                "-fx-font-size:11;-fx-font-weight:bold;"
            );
            Tooltip.install(badge, new Tooltip(String.format("%02dh00 : %s", h, occupant)));
            badges.add(badge);
        }
        return badges;
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private List<Salle> filtrer(List<Salle> salles, int capMin, String type,
                                 boolean video, boolean ti, boolean clim) {
        return salles.stream()
            .filter(s -> s.getCapacite() >= capMin)
            .filter(s -> type == null || type.equals("Tous") || s.getType().equals(type))
            .filter(s -> !video || s.isVideoprojecteur())
            .filter(s -> !ti   || s.isTableauInteractif())
            .filter(s -> !clim || s.isClimatisation())
            .collect(Collectors.toList());
    }

    private TableColumn<Salle, String> col(String titre, java.util.function.Function<Salle, String> fn) {
        TableColumn<Salle, String> c = new TableColumn<>(titre);
        c.setCellValueFactory(cell -> new SimpleStringProperty(fn.apply(cell.getValue())));
        return c;
    }

    private ToggleButton styledToggle(String label, ToggleGroup group, boolean selected) {
        ToggleButton tb = new ToggleButton(label);
        tb.setToggleGroup(group);
        tb.setSelected(selected);
        tb.setStyle(
            "-fx-padding: 6 14; -fx-font-size: 12; -fx-background-radius: 6;" +
            "-fx-cursor: hand; -fx-border-radius: 6;"
        );
        return tb;
    }

    private Label badgeLegende(String texte, String couleur) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: " + couleur + "; -fx-font-weight: bold;");
        return lbl;
    }

    private void setInfo(Label lbl, String text, String color) {
        lbl.setText(text);
        lbl.setStyle("-fx-font-size:12;-fx-text-fill:" + color + ";-fx-font-weight:bold;");
    }

    private Label fl(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";-fx-min-width:140;");
        return lbl;
    }
}
