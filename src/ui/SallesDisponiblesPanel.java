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
 * Panel unique pour le gestionnaire — 2 onglets :
 *   🟢 Disponibilité temps réel  : grille de toutes les salles, statut vert/rouge/orange, auto-refresh
 *   🔍 Recherche avancée         : filtres date/heure/durée/capacité/type/équipements + résultats
 */
public class SallesDisponiblesPanel {

    private SalleDAO salleDAO = new SalleDAO();
    private static final DateTimeFormatter H_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ScrollPane createPanel() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-font-size: 13;");

        Tab tabTempsReel = new Tab("🟢 Disponibilité en temps réel", creerOngletTempsReel());
        Tab tabRecherche = new Tab("🔍 Recherche avancée",           creerOngletRecherche());
        tabs.getTabs().addAll(tabTempsReel, tabRecherche);

        VBox wrapper = new VBox(tabs);
        wrapper.setPadding(new Insets(14));
        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ════════════════════════════════════════════════════════════
    //  ONGLET 1 — TEMPS RÉEL
    // ════════════════════════════════════════════════════════════
    private VBox creerOngletTempsReel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(18));

        // En-tête + horloge
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("🟢 Disponibilité des Salles — Temps Réel");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label lblHeure = new Label("");
        lblHeure.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        header.getChildren().addAll(titre, sp, lblHeure);

        // Légende
        HBox legende = new HBox(20);
        legende.setAlignment(Pos.CENTER_LEFT);
        legende.setPadding(new Insets(0, 0, 4, 0));
        legende.getChildren().addAll(
            badgeLegende("🟢 Disponible",  "#27ae60"),
            badgeLegende("🔴 Occupée",      "#e74c3c"),
            badgeLegende("🟡 Bientôt libre","#f39c12")
        );

        // Filtres rapides de la vue temps réel
        HBox filtresRapides = new HBox(12);
        filtresRapides.setAlignment(Pos.CENTER_LEFT);
        filtresRapides.setPadding(new Insets(8, 12, 8, 12));
        filtresRapides.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 6; -fx-background-radius: 6;");

        ComboBox<String> cbTypeFiltre = new ComboBox<>();
        cbTypeFiltre.getItems().addAll("Tous les types", "TD", "TP", "Amphi");
        cbTypeFiltre.setValue("Tous les types"); cbTypeFiltre.setPrefWidth(140);

        ToggleGroup tgStatut = new ToggleGroup();
        ToggleButton tbTout  = new ToggleButton("Toutes");   tbTout.setToggleGroup(tgStatut);  tbTout.setSelected(true);
        ToggleButton tbDispo = new ToggleButton("🟢 Libres"); tbDispo.setToggleGroup(tgStatut);
        ToggleButton tbOccup = new ToggleButton("🔴 Occupées"); tbOccup.setToggleGroup(tgStatut);
        String styleTB = "-fx-padding: 5 12; -fx-font-size: 12;";
        tbTout.setStyle(styleTB); tbDispo.setStyle(styleTB); tbOccup.setStyle(styleTB);

        Button btnRefresh = new Button("🔄 Actualiser");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 6 14; -fx-font-weight: bold;");

        filtresRapides.getChildren().addAll(
            new Label("Type :"), cbTypeFiltre,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            new Label("Statut :"), tbTout, tbDispo, tbOccup,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            btnRefresh
        );

        // Grille des salles (FlowPane de cartes)
        FlowPane grille = new FlowPane(12, 12);
        grille.setPadding(new Insets(4));

        // Compteurs
        Label lblCompteurs = new Label("");
        lblCompteurs.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        // Fonction de rafraîchissement
        Runnable refresh = () -> {
            LocalDateTime now = LocalDateTime.now();
            lblHeure.setText("⏱ " + now.format(DateTimeFormatter.ofPattern("HH:mm:ss  —  EEEE dd/MM/yyyy",
                java.util.Locale.FRENCH)));
            List<Salle> salles = salleDAO.obtenirTous();

            // Filtrage type
            String typeChoisi = cbTypeFiltre.getValue();
            if (typeChoisi != null && !typeChoisi.equals("Tous les types")) {
				salles = salles.stream().filter(s -> s.getType().equals(typeChoisi)).collect(Collectors.toList());
			}

            // Filtrage statut
            Toggle sel = tgStatut.getSelectedToggle();
            List<Salle> sallesFiltrees = salles;
            if (sel == tbDispo) {
				sallesFiltrees = salles.stream().filter(s -> !salleDAO.estOccupeeMaintenantET(s.getId(), now)).collect(Collectors.toList());
			} else if (sel == tbOccup) {
				sallesFiltrees = salles.stream().filter(s -> salleDAO.estOccupeeMaintenantET(s.getId(), now)).collect(Collectors.toList());
			}

            long nbLibres = sallesFiltrees.stream().filter(s -> !salleDAO.estOccupeeMaintenantET(s.getId(), now)).count();
            long nbOccup  = sallesFiltrees.size() - nbLibres;
            lblCompteurs.setText("🟢 " + nbLibres + " libre(s)   🔴 " + nbOccup + " occupée(s)   — Total affiché : " + sallesFiltrees.size());

            grille.getChildren().clear();
            for (Salle s : sallesFiltrees) {
                boolean occupee = salleDAO.estOccupeeMaintenantET(s.getId(), now);
                String occupant = occupee ? salleDAO.getOccupantActuel(s.getId(), now) : "";
                // Bientôt libre = occupée mais libération dans < 15 min ? (bonus visuel)
                grille.getChildren().add(carteSalle(s, occupee, occupant, now));
            }
        };

        // Auto-refresh toutes les 60 secondes
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(60), e -> refresh.run()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        // Déclencheurs manuels
        btnRefresh.setOnAction(e -> refresh.run());
        cbTypeFiltre.setOnAction(e -> refresh.run());
        tbTout.setOnAction(e -> refresh.run());
        tbDispo.setOnAction(e -> refresh.run());
        tbOccup.setOnAction(e -> refresh.run());

        // Chargement initial
        refresh.run();

        panel.getChildren().addAll(header, legende, filtresRapides, lblCompteurs, grille);
        return panel;
    }

    /** Carte visuelle d'une salle avec statut coloré */
    private VBox carteSalle(Salle s, boolean occupee, String occupant, LocalDateTime now) {
        VBox carte = new VBox(5);
        carte.setPrefWidth(200);
        carte.setPadding(new Insets(10, 12, 10, 12));

        String bgColor, bordColor, statutText;
        if (occupee) {
            bgColor   = "#fdecea"; bordColor = "#e74c3c"; statutText = "🔴 Occupée";
        } else {
            bgColor   = "#eafaf1"; bordColor = "#27ae60"; statutText = "🟢 Disponible";
        }
        carte.setStyle("-fx-background-color: " + bgColor + "; "
            + "-fx-border-color: " + bordColor + "; "
            + "-fx-border-width: 0 0 0 4; -fx-border-radius: 0 6 6 0; "
            + "-fx-background-radius: 6;");

        Label lNum  = new Label("🏫 " + s.getNumero());
        lNum.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label lBat  = new Label(s.getBatiment() + " · " + s.getEtage());
        lBat.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        Label lInfo = new Label("👥 " + s.getCapacite() + "  ·  " + s.getType());
        lInfo.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");

        Label lEquip = new Label(s.getEquipementsStr().isEmpty() ? "" : "🔧 " + s.getEquipementsStr());
        lEquip.setStyle("-fx-font-size: 10; -fx-text-fill: #888;"); lEquip.setWrapText(true);

        Label lStatut = new Label(statutText);
        lStatut.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + bordColor + ";");

        carte.getChildren().addAll(lNum, lBat, lInfo, lEquip, lStatut);

        if (occupee && !occupant.isEmpty()) {
            Label lOccupant = new Label(occupant);
            lOccupant.setStyle("-fx-font-size: 10; -fx-text-fill: #c0392b; -fx-wrap-text: true;");
            lOccupant.setWrapText(true); lOccupant.setMaxWidth(180);
            carte.getChildren().add(lOccupant);
        }

        // Tooltip avec infos complètes
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

    // ════════════════════════════════════════════════════════════
    //  ONGLET 2 — RECHERCHE AVANCÉE
    // ════════════════════════════════════════════════════════════
    private VBox creerOngletRecherche() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(18));

        Label titre = new Label("🔍 Recherche Avancée de Salles");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ── Formulaire de filtres ──
        VBox boxFiltres = new VBox(12);
        boxFiltres.setPadding(new Insets(16));
        boxFiltres.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 6; -fx-background-color: white;");

        Label lblFiltres = new Label("Critères de recherche");
        lblFiltres.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);

        // Date et heure
        DatePicker datePicker = new DatePicker(LocalDate.now());
        Spinner<Integer> spH   = new Spinner<>(7, 22, LocalTime.now().getHour()); spH.setPrefWidth(80);
        Spinner<Integer> spMin = new Spinner<>(0, 59, 0, 5); spMin.setPrefWidth(80);
        Spinner<Integer> spDur = new Spinner<>(15, 480, 60, 15); spDur.setPrefWidth(90);
        HBox heureBox = new HBox(6, new Label("h"), spH, new Label("min"), spMin);
        heureBox.setAlignment(Pos.CENTER_LEFT);

        // Capacité
        Spinner<Integer> spCap = new Spinner<>(0, 500, 0, 5); spCap.setPrefWidth(90);
        Label lblCapInfo = new Label("(0 = indifférent)");
        lblCapInfo.setStyle("-fx-font-size: 10; -fx-text-fill: #aaa;");

        // Type
        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("Tous", "TD", "TP", "Amphi"); cbType.setValue("Tous"); cbType.setPrefWidth(110);

        // Équipements
        CheckBox chkVideo = new CheckBox("📽 Vidéoprojecteur");
        CheckBox chkTI    = new CheckBox("🖥 Tableau interactif");
        CheckBox chkClim  = new CheckBox("❄ Climatisation");
        HBox equipBox = new HBox(14, chkVideo, chkTI, chkClim);
        equipBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("📅 Date :"),             0, 0); grid.add(datePicker, 1, 0);
        grid.add(new Label("🕐 Heure de début :"),   0, 1); grid.add(heureBox,   1, 1);
        grid.add(new Label("⏱ Durée (min) :"),       0, 2); grid.add(new HBox(6, spDur, new Label("min")), 1, 2);
        grid.add(new Label("👥 Capacité min. :"),    0, 3); grid.add(new HBox(6, spCap, lblCapInfo), 1, 3);
        grid.add(new Label("🏫 Type de salle :"),    0, 4); grid.add(cbType,     1, 4);
        grid.add(new Label("🔧 Équipements :"),      0, 5); grid.add(equipBox,   1, 5);

        // Boutons d'action
        Button btnMaintenant = new Button("⚡ Disponible maintenant");
        btnMaintenant.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white;-fx-padding:9 18;-fx-font-weight:bold;");
        Button btnRechercher = new Button("🔍 Rechercher");
        btnRechercher.setStyle("-fx-background-color:#2980b9;-fx-text-fill:white;-fx-padding:9 18;-fx-font-weight:bold;");
        Button btnReset = new Button("🔄 Réinitialiser");
        btnReset.setStyle("-fx-padding:9 18;");
        HBox boutons = new HBox(10, btnMaintenant, btnRechercher, btnReset);
        boutons.setAlignment(Pos.CENTER_LEFT);

        boxFiltres.getChildren().addAll(lblFiltres, grid, boutons);

        // ── Résultats ──
        Label lblResultats = new Label("Résultats");
        lblResultats.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label lblInfo = new Label("Renseignez les critères et cliquez sur Rechercher.");
        lblInfo.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        // Tableau résultats
        TableView<Salle> table = new TableView<>();
        table.setPrefHeight(300);
        table.setPlaceholder(new Label("Aucune salle ne correspond aux critères."));

        TableColumn<Salle,String>  cNum   = col("Numéro",    90, s -> s.getNumero());
        TableColumn<Salle,String>  cBat   = col("Bâtiment", 120, s -> s.getBatiment());
        TableColumn<Salle,String>  cEtage = col("Étage",     80, s -> s.getEtage());
        TableColumn<Salle,Integer> cCap   = new TableColumn<>("Capacité"); cCap.setPrefWidth(80);
        cCap.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getCapacite()));
        TableColumn<Salle,String>  cType2 = col("Type",      70, s -> s.getType());
        TableColumn<Salle,String>  cEquip = col("Équipements",150, s -> s.getEquipementsStr().isEmpty() ? "—" : s.getEquipementsStr());
        table.getColumns().addAll(cNum, cBat, cEtage, cCap, cType2, cEquip);

        // Panneau de détail au clic (créneaux de la journée)
        VBox zoneCreneaux = new VBox(8);
        zoneCreneaux.setPadding(new Insets(12));
        zoneCreneaux.setStyle("-fx-border-color:#ddd;-fx-background-color:white;-fx-border-radius:6;");
        zoneCreneaux.setVisible(false);
        Label lblCreneauxTitre = new Label("");
        lblCreneauxTitre.setStyle("-fx-font-size:13;-fx-font-weight:bold;");
        FlowPane creneauxFlow = new FlowPane(8, 8);
        zoneCreneaux.getChildren().addAll(lblCreneauxTitre, creneauxFlow);

        // Clic sur une salle → afficher ses créneaux occupés du jour
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null) { zoneCreneaux.setVisible(false); return; }
            LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
            lblCreneauxTitre.setText("📅 Créneaux de " + sel.getNumero() + " le " + date.format(D_FMT) + " :");
            creneauxFlow.getChildren().clear();
            creneauxFlow.getChildren().addAll(creerBadgesCreneaux(sel, date));
            zoneCreneaux.setVisible(true);
        });

        // ── Actions ──
        btnMaintenant.setOnAction(e -> {
            datePicker.setValue(LocalDate.now());
            spH.getValueFactory().setValue(LocalTime.now().getHour());
            spMin.getValueFactory().setValue(0);
            LocalDateTime now = LocalDateTime.now();
            List<Salle> res = filtrer(salleDAO.obtenirSallesDisponibles(now, spDur.getValue()),
                spCap.getValue(), cbType.getValue(), chkVideo.isSelected(), chkTI.isSelected(), chkClim.isSelected());
            table.setItems(FXCollections.observableArrayList(res));
            lblInfo.setText("⚡ " + res.size() + " salle(s) disponible(s) en ce moment pour " + spDur.getValue() + " min.");
            lblInfo.setStyle("-fx-font-size:12;-fx-text-fill:#27ae60;-fx-font-weight:bold;");
            zoneCreneaux.setVisible(false);
        });

        btnRechercher.setOnAction(e -> {
            if (datePicker.getValue() == null) { lblInfo.setText("⚠️ Sélectionnez une date."); lblInfo.setStyle("-fx-text-fill:#e67e22;-fx-font-size:12;"); return; }
            LocalDateTime debut = LocalDateTime.of(datePicker.getValue(), LocalTime.of(spH.getValue(), spMin.getValue()));
            List<Salle> res = filtrer(salleDAO.obtenirSallesDisponibles(debut, spDur.getValue()),
                spCap.getValue(), cbType.getValue(), chkVideo.isSelected(), chkTI.isSelected(), chkClim.isSelected());
            table.setItems(FXCollections.observableArrayList(res));
            String msg = res.size() + " salle(s) disponible(s) le "
                + datePicker.getValue().format(D_FMT) + " à "
                + String.format("%02dh%02d", spH.getValue(), spMin.getValue())
                + " pour " + spDur.getValue() + " min.";
            lblInfo.setText(res.isEmpty() ? "❌ Aucune salle disponible pour ces critères." : "✅ " + msg);
            lblInfo.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + (res.isEmpty() ? "#e74c3c" : "#27ae60") + ";");
            zoneCreneaux.setVisible(false);
        });

        btnReset.setOnAction(e -> {
            datePicker.setValue(LocalDate.now()); spH.getValueFactory().setValue(8);
            spMin.getValueFactory().setValue(0); spDur.getValueFactory().setValue(60);
            spCap.getValueFactory().setValue(0); cbType.setValue("Tous");
            chkVideo.setSelected(false); chkTI.setSelected(false); chkClim.setSelected(false);
            table.setItems(FXCollections.observableArrayList());
            lblInfo.setText("Renseignez les critères et cliquez sur Rechercher.");
            lblInfo.setStyle("-fx-font-size:12;-fx-text-fill:#7f8c8d;");
            zoneCreneaux.setVisible(false);
        });

        panel.getChildren().addAll(titre, boxFiltres, lblResultats, lblInfo, table, zoneCreneaux);
        return panel;
    }

    /** Badges des créneaux horaires d'une salle pour une journée donnée */
    private List<javafx.scene.Node> creerBadgesCreneaux(Salle salle, LocalDate date) {
        List<javafx.scene.Node> badges = new java.util.ArrayList<>();
        int jour = date.getDayOfWeek().getValue(); // 1=Lun..6=Sam
        for (int h = 7; h <= 20; h++) {
            LocalDateTime moment = date.atTime(h, 0);
            boolean occ = salleDAO.estOccupeeMaintenantET(salle.getId(), moment);
            String occupant = occ ? salleDAO.getOccupantActuel(salle.getId(), moment) : "Libre";
            Label badge = new Label(String.format("%02dh", h));
            badge.setPadding(new Insets(4, 8, 4, 8));
            badge.setStyle(occ
                ? "-fx-background-color:#e74c3c;-fx-text-fill:white;-fx-background-radius:4;-fx-font-size:11;-fx-font-weight:bold;"
                : "-fx-background-color:#27ae60;-fx-text-fill:white;-fx-background-radius:4;-fx-font-size:11;");
            Tooltip.install(badge, new Tooltip(String.format("%02dh00 : %s", h, occupant)));
            badges.add(badge);
        }
        return badges;
    }

    // ── Helpers ──
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

    private <T> TableColumn<Salle,String> col(String titre, int width, java.util.function.Function<Salle,String> fn) {
        TableColumn<Salle,String> c = new TableColumn<>(titre); c.setPrefWidth(width);
        c.setCellValueFactory(cell -> new SimpleStringProperty(fn.apply(cell.getValue())));
        return c;
    }

    private Label badgeLegende(String texte, String couleur) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: " + couleur + "; -fx-font-weight: bold;");
        return lbl;
    }
}
