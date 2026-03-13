package ui;

import dao.EmploiDuTempsDAO;
import dao.SalleDAO;
import models.EmploiDuTemps;
import models.Salle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vue GRILLE HEBDOMADAIRE de l'emploi du temps.
 * Utilisable par : Étudiant (sa classe), Enseignant (ses cours), Gestionnaire (toute classe).
 *
 * Usage :
 *   new EmploiDuTempsViewPanel("L2-Informatique", false).createPanel()
 *   new EmploiDuTempsViewPanel(null, false).createPanel()   // toutes classes
 */
public class EmploiDuTempsViewPanel {

    private EmploiDuTempsDAO edtDAO = new EmploiDuTempsDAO();
    private SalleDAO salleDAO = new SalleDAO();

    /** null = afficher le ComboBox de sélection de classe */
    private String classeInitiale;
    /** Si true, la ComboBox de classe est masquée (usage enseignant : filtrer par nom) */
    private boolean modeEnseignant;
    private String nomEnseignant;

    // Couleurs par type de cours
    private static final String CM_BG  = "#d6eaf8"; private static final String CM_BRD = "#2980b9";
    private static final String TD_BG  = "#d5f5e3"; private static final String TD_BRD = "#27ae60";
    private static final String TP_BG  = "#fdf2e9"; private static final String TP_BRD = "#e67e22";
    private static final String DEF_BG = "#f4ecf7"; private static final String DEF_BRD = "#8e44ad";

    private static final String[] JOURS = {"", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    private static final int HEURE_MIN = 7;
    private static final int HEURE_MAX = 21;
    private static final int CELL_H    = 44; // pixels par tranche de 30 min
    private static final int COL_W     = 165;

    /** Constructeur pour étudiant ou gestionnaire (filtre par classe) */
    public EmploiDuTempsViewPanel(String classeInitiale) {
        this.classeInitiale = classeInitiale;
        this.modeEnseignant = false;
    }

    /** Constructeur pour enseignant (filtre par nom) */
    public EmploiDuTempsViewPanel(String nomEnseignant, boolean modeEnseignant) {
        this.nomEnseignant = nomEnseignant;
        this.modeEnseignant = modeEnseignant;
        this.classeInitiale = null;
    }

    public ScrollPane createPanel() {
        VBox container = new VBox(14);
        container.setPadding(new Insets(18));

        Label titre = new Label(modeEnseignant
            ? "📅 Mon Emploi du Temps Hebdomadaire"
            : "📅 Emploi du Temps Hebdomadaire");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ── Sélecteur de classe (masqué en mode enseignant) ──
        ComboBox<String> cbClasse = new ComboBox<>();
        VBox selecteurBox = new VBox(6);

        if (!modeEnseignant) {
            cbClasse.getItems().add("-- Choisir une classe --");
            cbClasse.getItems().addAll(edtDAO.obtenirToutesLesClasses());
            cbClasse.setValue(classeInitiale != null ? classeInitiale : "-- Choisir une classe --");
            cbClasse.setPrefWidth(230);

            HBox selBox = new HBox(10, new Label("Classe :"), cbClasse);
            selBox.setAlignment(Pos.CENTER_LEFT);
            selecteurBox.getChildren().add(selBox);

            // Légende
            HBox legende = creerLegende();
            selecteurBox.getChildren().add(legende);
        } else {
            // Mode enseignant : afficher le nom en info
            Label lInfo = new Label("Enseignant : " + nomEnseignant);
            lInfo.setStyle("-fx-font-size: 13; -fx-text-fill: #8e44ad; -fx-font-weight: bold;");
            selecteurBox.getChildren().addAll(lInfo, creerLegende());
        }

        // Zone de la grille (rechargeable)
        VBox zoneGrille = new VBox();
        zoneGrille.setPadding(new Insets(6, 0, 0, 0));

        // Chargement initial
        rechargerGrille(zoneGrille, modeEnseignant ? null : classeInitiale);

        // Listener classe
        if (!modeEnseignant) {
            cbClasse.setOnAction(e -> {
                String v = cbClasse.getValue();
                String classe = (v == null || v.startsWith("--")) ? null : v;
                rechargerGrille(zoneGrille, classe);
            });
        }

        container.getChildren().addAll(titre, selecteurBox, zoneGrille);
        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ── Grille ────────────────────────────────────────────────────────
    private void rechargerGrille(VBox zone, String classe) {
        zone.getChildren().clear();

        List<EmploiDuTemps> data;
        if (modeEnseignant) {
            data = edtDAO.obtenirParEnseignant(nomEnseignant);
        } else if (classe == null || classe.startsWith("--")) {
            Label hint = new Label("👆 Sélectionnez une classe pour afficher son emploi du temps.");
            hint.setStyle("-fx-font-size: 13; -fx-text-fill: #95a5a6; -fx-padding: 20;");
            zone.getChildren().add(hint);
            return;
        } else {
            data = edtDAO.obtenirParClasse(classe);
        }

        if (data.isEmpty()) {
            Label vide = new Label("Aucun créneau défini" + (classe != null ? " pour la classe " + classe : "") + ".");
            vide.setStyle("-fx-font-size: 13; -fx-text-fill: #95a5a6; -fx-padding: 20;");
            zone.getChildren().add(vide);
            return;
        }

        // ── Construire la grille positionnelle ──
        // En-tête colonnes
        HBox header = new HBox(1);
        header.getChildren().add(creerCellEntete("", 60, "#bdc3c7"));
        for (int j = 1; j <= 6; j++) {
            header.getChildren().add(creerCellEntete(JOURS[j], COL_W, "#2c3e50"));
        }
        zone.getChildren().add(header);

        // Lignes par tranche de 30 min
        int totalTranches = (HEURE_MAX - HEURE_MIN) * 2; // 30 min chacune

        for (int t = 0; t < totalTranches; t++) {
            int minutes = HEURE_MIN * 60 + t * 30;
            int h = minutes / 60;
            int m = minutes % 60;
            LocalTime tranche = LocalTime.of(h, m);

            HBox ligne = new HBox(1);
            // Colonne heure
            Label lHeure = new Label(m == 0 ? String.format("%02d:00", h) : "");
            lHeure.setPrefWidth(60);
            lHeure.setMinHeight(CELL_H);
            lHeure.setStyle("-fx-font-size: 10; -fx-text-fill: #888; -fx-padding: 2 4; -fx-border-color: #ecf0f1; -fx-border-width: 0 1 1 0;");
            lHeure.setAlignment(Pos.TOP_RIGHT);
            ligne.getChildren().add(lHeure);

            // Colonnes jours
            for (int j = 1; j <= 6; j++) {
                final int jourJ = j;
                // Chercher un créneau qui débute à cette tranche
                List<EmploiDuTemps> iciList = data.stream()
                    .filter(e -> e.getJourSemaine() == jourJ && e.getHeureDebut().equals(tranche))
                    .collect(Collectors.toList());

                if (!iciList.isEmpty()) {
                    EmploiDuTemps edt = iciList.get(0); // un seul par créneau normalement
                    Salle salle = salleDAO.obtenirParId(edt.getSalleId());
                    String nomSalle = salle != null ? salle.getNumero() : "?";

                    int nbTranches = Math.max(1, edt.getDuree() / 30);
                    double hauteur = nbTranches * CELL_H;

                    String[] couleurs = couleursPourType(edt.getTypeCours());
                    VBox bloc = new VBox(2);
                    bloc.setPrefWidth(COL_W);
                    bloc.setMinHeight(hauteur);
                    bloc.setPrefHeight(hauteur);
                    bloc.setPadding(new Insets(5, 6, 5, 8));
                    bloc.setStyle(
                        "-fx-background-color: " + couleurs[0] + ";" +
                        "-fx-border-color: " + couleurs[1] + ";" +
                        "-fx-border-width: 0 0 0 4;" +
                        "-fx-border-radius: 2;");

                    Label lMat = new Label(edt.getMatiere());
                    lMat.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #2c3e50;");
                    lMat.setWrapText(true);
                    lMat.setMaxWidth(COL_W - 16);

                    Label lType = new Label("[" + edt.getTypeCours() + "]  "
                        + edt.getHeureDebut() + " – " + edt.getHeureFin());
                    lType.setStyle("-fx-font-size: 10; -fx-text-fill: #555;");

                    Label lEns = new Label("👤 " + edt.getEnseignant());
                    lEns.setStyle("-fx-font-size: 10; -fx-text-fill: #555;");
                    lEns.setWrapText(true);
                    lEns.setMaxWidth(COL_W - 16);

                    Label lSalle = new Label("🏫 " + nomSalle
                        + (edt.getClasse() != null && !modeEnseignant ? "" : "  📚 " + edt.getClasse()));
                    lSalle.setStyle("-fx-font-size: 10; -fx-text-fill: #777;");

                    bloc.getChildren().addAll(lMat, lType, lEns, lSalle);

                    // Tooltip détaillé
                    Tooltip tip = new Tooltip(
                        edt.getMatiere() + " [" + edt.getTypeCours() + "]\n" +
                        "Classe     : " + edt.getClasse() + "\n" +
                        "Enseignant : " + edt.getEnseignant() + "\n" +
                        "Salle      : " + nomSalle + (salle != null ? " — " + salle.getBatiment() : "") + "\n" +
                        "Horaire    : " + edt.getHeureDebut() + " → " + edt.getHeureFin() + "\n" +
                        "Durée      : " + edt.getDuree() + " min");
                    Tooltip.install(bloc, tip);

                    ligne.getChildren().add(bloc);

                } else {
                    // Vérifier si cette tranche est dans un bloc qui a commencé avant
                    boolean couverte = data.stream().anyMatch(e ->
                        e.getJourSemaine() == jourJ
                        && e.getHeureDebut().isBefore(tranche)
                        && e.getHeureFin().isAfter(tranche));

                    if (!couverte) {
                        // Cellule vide
                        Label vide = new Label("");
                        vide.setPrefWidth(COL_W);
                        vide.setMinHeight(CELL_H);
                        vide.setStyle("-fx-border-color: #ecf0f1; -fx-border-width: 0 1 1 0;");
                        ligne.getChildren().add(vide);
                    }
                    // Si couverte → ne rien ajouter (le bloc du créneau précédent s'étend)
                }
            }
            zone.getChildren().add(ligne);
        }
    }

    private Label creerCellEntete(String texte, double largeur, String bg) {
        Label lbl = new Label(texte);
        lbl.setPrefWidth(largeur);
        lbl.setMinHeight(36);
        lbl.setAlignment(Pos.CENTER);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: white; -fx-background-color: " + bg + "; -fx-padding: 5;");
        return lbl;
    }

    private String[] couleursPourType(String type) {
        switch (type.toUpperCase()) {
            case "CM": return new String[]{CM_BG, CM_BRD};
            case "TD": return new String[]{TD_BG, TD_BRD};
            case "TP": return new String[]{TP_BG, TP_BRD};
            default:   return new String[]{DEF_BG, DEF_BRD};
        }
    }

    private HBox creerLegende() {
        HBox leg = new HBox(16);
        leg.setAlignment(Pos.CENTER_LEFT);
        leg.setPadding(new Insets(4, 0, 0, 0));
        leg.getChildren().add(new Label("Légende :"));
        for (String[] tc : new String[][]{{"CM", CM_BRD}, {"TD", TD_BRD}, {"TP", TP_BRD}}) {
            Label badge = new Label(tc[0]);
            badge.setStyle("-fx-padding: 2 10; -fx-background-color: white; -fx-border-color: " + tc[1] +
                "; -fx-border-width: 0 0 0 4; -fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + tc[1] + ";");
            leg.getChildren().add(badge);
        }
        return leg;
    }
}
