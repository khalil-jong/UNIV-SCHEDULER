package ui;

import java.time.LocalTime;
import java.util.List;

import dao.ClasseDAO;
import dao.EmploiDuTempsDAO;
import dao.SalleDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.EmploiDuTemps;
import models.Salle;

/**
 * Emploi du temps hebdomadaire au format tableau (image de référence) :
 *  - Lignes = tranches horaires (08h-09h, 09h-10h ...)
 *  - Colonnes = Lundi → Samedi
 *  - Cellules fusionnées (rowspan) si le cours dure plusieurs heures
 *  - Couleur de fond crème (#FFF9E6) pour les cours, en-têtes gris foncé
 *  - Nom de salle en rouge
 *
 * Constructeur (classe)   → étudiant / gestionnaire
 * Constructeur (nom,true) → enseignant
 */
public class EmploiDuTempsViewPanel {

    private ClasseDAO         classeDAO = new ClasseDAO();
    private EmploiDuTempsDAO edtDAO  = new EmploiDuTempsDAO();
    private SalleDAO          salleDAO = new SalleDAO();

    private String  classeInitiale;
    private boolean modeEnseignant;
    private String  nomEnseignant;

    // Grille horaire : 08h → 19h par tranches d'1h
    private static final int H_DEBUT = 8;
    private static final int H_FIN   = 19;
    private static final String[] JOURS = {"Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"};

    // Couleurs (fidèles à l'image)
    private static final String ENTETE_BG   = "#4D4D4D";
    private static final String ENTETE_FG   = "white";
    private static final String COURS_BG    = "#FFF9E6"; // fallback
    private static final String COURS_BORD  = "#CCBBAA"; // fallback
    private static final String SALLE_COLOR = "#CC2200";
    private static final String VIDE_BG     = "white";
    private static final String PAUSE_BG    = "#E0E0E0";

    // Couleurs par type de cours — cohérentes avec la légende
    private static final String CM_BG   = "#dbeeff"; // bleu clair
    private static final String CM_BRD  = "#2980b9"; // bleu
    private static final String TD_BG   = "#d5f5e3"; // vert clair
    private static final String TD_BRD  = "#27ae60"; // vert
    private static final String TP_BG   = "#fdecea"; // orange clair
    private static final String TP_BRD  = "#e67e22"; // orange

    /** Pour étudiant ou gestionnaire */
    public EmploiDuTempsViewPanel(String classeInitiale) {
        this.classeInitiale = classeInitiale;
        this.modeEnseignant = false;
    }

    /** Pour enseignant */
    public EmploiDuTempsViewPanel(String nomEnseignant, boolean modeEnseignant) {
        this.nomEnseignant  = nomEnseignant;
        this.modeEnseignant = modeEnseignant;
    }

    public ScrollPane createPanel() {
        VBox container = new VBox(14);
        container.setPadding(new Insets(18));

        Label titre = new Label(modeEnseignant
            ? "📅 Mon Emploi du Temps — " + nomEnseignant
            : "📅 Emploi du Temps Hebdomadaire");
        titre.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox zoneGrille = new VBox();

        if (!modeEnseignant) {
            // ComboBox de sélection de classe
            ComboBox<String> cbClasse = new ComboBox<>();
            // Source unique : table `classes` (sans orphelins EDT)
            List<String> classes = classeDAO.obtenirNomsClasses();
            cbClasse.getItems().addAll(classes);
            if (classeInitiale != null && classes.contains(classeInitiale)) {
                cbClasse.setValue(classeInitiale);
            } else {
                cbClasse.setPromptText("-- Choisir une classe --");
            }
            cbClasse.setPrefWidth(240);
            cbClasse.setOnAction(e -> {
                zoneGrille.getChildren().clear();
                String v = cbClasse.getValue();
                if (v != null) {
					zoneGrille.getChildren().add(construireGrille(edtDAO.obtenirParClasse(v), v));
				}
            });

            HBox selBox = new HBox(10, new Label("Classe :"), cbClasse);
            selBox.setAlignment(Pos.CENTER_LEFT);

            // Légende
            HBox legende = creerLegende();
            container.getChildren().addAll(titre, selBox, legende);

            // Afficher immédiatement si classe connue
            if (cbClasse.getValue() != null) {
                zoneGrille.getChildren().add(construireGrille(
                    edtDAO.obtenirParClasse(cbClasse.getValue()), cbClasse.getValue()));
            }
        } else {
            // Mode enseignant : grille directe
            HBox legende = creerLegende();
            container.getChildren().addAll(titre, legende);
            List<EmploiDuTemps> data = edtDAO.obtenirParEnseignant(nomEnseignant);
            zoneGrille.getChildren().add(construireGrille(data, null));
        }

        container.getChildren().add(zoneGrille);
        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ── Construction du tableau style "image" ─────────────────────────
    private GridPane construireGrille(List<EmploiDuTemps> data, String classe) {
        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setStyle("-fx-background-color: #999; -fx-padding: 1;");

        int nbHeures = H_FIN - H_DEBUT; // nombre de lignes horaires

        // ── Ligne 0 : en-têtes ──
        grid.add(cellEntete("Heures", 80, 36), 0, 0);
        for (int j = 0; j < JOURS.length; j++) {
            grid.add(cellEntete(JOURS[j], 145, 36), j + 1, 0);
        }

        // ── Matrice : occupied[jour][heure] = true si déjà dessinée ──
        boolean[][] occupied = new boolean[6][nbHeures];

        // ── Lignes horaires ──
        for (int h = 0; h < nbHeures; h++) {
            int heure = H_DEBUT + h;
            String label = String.format("%02dh-%02dh", heure, heure + 1);

            // Cellule heure
            grid.add(cellHeure(label, 80, 38), 0, h + 1);

            // Pour chaque jour
            for (int j = 0; j < 6; j++) {
                if (occupied[j][h]) {
					continue; // déjà couverte par un rowspan
				}

                final int jourIdx = j + 1; // 1=Lundi
                final LocalTime tranche = LocalTime.of(heure, 0);

                // Chercher un cours qui commence à cette heure ce jour
                EmploiDuTemps cours = trouverCours(data, jourIdx, tranche);

                if (cours != null) {
                    // Calculer le nombre de lignes (rowspan)
                    int rowspan = Math.max(1, (int) Math.ceil(cours.getDuree() / 60.0));
                    // Marquer les lignes couvertes
                    for (int r = 0; r < rowspan && (h + r) < nbHeures; r++) {
                        occupied[j][h + r] = true;
                    }

                    Salle salle = salleDAO.obtenirParId(cours.getSalleId());
                    String nomSalle = salle != null ? salle.getNumero() : "?";

                    javafx.scene.Node cellule = cellCours(cours, nomSalle, 145, 38 * rowspan + (rowspan - 1));
                    GridPane.setRowSpan(cellule, rowspan);
                    grid.add(cellule, j + 1, h + 1);
                } else {
                    // Vérifier si c'est la pause méridienne (13h-15h)
                    if (heure == 13 || heure == 14) {
                        // Pause (sera gérée en bloc sur toute la ligne)
                        grid.add(cellVide(145, 38, true), j + 1, h + 1);
                    } else {
                        grid.add(cellVide(145, 38, false), j + 1, h + 1);
                    }
                }
            }

            // Ligne de pause : remplacer toute la ligne par "Pause"
            if (heure == 13) {
                // Recréer la ligne pause sur 6 colonnes fusionnées
                // (déjà fait cellule par cellule avec fond gris)
                // Ajouter le label "Pause" au centre
                Label lPause = new Label("Pause");
                lPause.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #555;");
                lPause.setAlignment(Pos.CENTER);
                lPause.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(lPause, Priority.ALWAYS);
                // Remplacer les 6 cellules de la ligne 13h par une cellule fusionnée
                // On utilise GridPane.setColumnSpan
                grid.getChildren().removeIf(n -> {
                    Integer row = GridPane.getRowIndex(n);
                    Integer col = GridPane.getColumnIndex(n);
                    return row != null && row == (13 - H_DEBUT + 1) && col != null && col >= 1;
                });
                StackPane pauseCell = new StackPane(lPause);
                pauseCell.setStyle("-fx-background-color: " + PAUSE_BG + "; -fx-border-color: #bbb;");
                pauseCell.setPrefHeight(38);
                GridPane.setColumnSpan(pauseCell, 6);
                grid.add(pauseCell, 1, 13 - H_DEBUT + 1);
            }
        }

        return grid;
    }

    // ── Recherche d'un créneau pour un jour/heure donnés ─────────────
    private EmploiDuTemps trouverCours(List<EmploiDuTemps> data, int jourIdx, LocalTime tranche) {
        for (EmploiDuTemps e : data) {
            if (e.getJourSemaine() == jourIdx && e.getHeureDebut().equals(tranche)) {
                return e;
            }
        }
        return null;
    }

    // ── Cellules ─────────────────────────────────────────────────────

    private StackPane cellEntete(String texte, double largeur, double hauteur) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + ENTETE_FG + ";");
        lbl.setAlignment(Pos.CENTER);
        StackPane cell = new StackPane(lbl);
        cell.setPrefWidth(largeur);
        cell.setPrefHeight(hauteur);
        cell.setStyle("-fx-background-color: " + ENTETE_BG + ";");
        return cell;
    }

    private StackPane cellHeure(String texte, double largeur, double hauteur) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size: 11; -fx-text-fill: #333; -fx-font-weight: bold;");
        lbl.setAlignment(Pos.CENTER);
        StackPane cell = new StackPane(lbl);
        cell.setPrefWidth(largeur);
        cell.setPrefHeight(hauteur);
        cell.setStyle("-fx-background-color: #F0F0F0; -fx-border-color: #ccc; -fx-border-width: 0.5;");
        return cell;
    }

    private VBox cellCours(EmploiDuTemps cours, String nomSalle, double largeur, double hauteur) {
        // Couleur selon le type (CM/TD/TP) — cohérent avec la légende
        String type = cours.getTypeCours() != null ? cours.getTypeCours().toUpperCase() : "CM";
        String bg, brd;
        switch (type) {
            case "TD": bg = TD_BG; brd = TD_BRD; break;
            case "TP": bg = TP_BG; brd = TP_BRD; break;
            default:   bg = CM_BG; brd = CM_BRD; break; // CM ou inconnu
        }

        // Matière en gras
        Label lMat = new Label(cours.getMatiere());
        lMat.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        lMat.setWrapText(true);
        lMat.setMaxWidth(largeur - 8);
        lMat.setAlignment(Pos.CENTER);

        // Enseignant + type
        String typeLabel = type.isEmpty() ? "" : " (" + type + ")";
        Label lEns = new Label("(" + cours.getEnseignant() + typeLabel + ")");
        lEns.setStyle("-fx-font-size: 10; -fx-text-fill: #333;");
        lEns.setWrapText(true);
        lEns.setMaxWidth(largeur - 8);
        lEns.setAlignment(Pos.CENTER);

        // Salle en rouge comme dans l'image
        Label lSalle = new Label(nomSalle);
        lSalle.setStyle("-fx-font-size: 11; -fx-text-fill: " + SALLE_COLOR + "; -fx-font-weight: bold;");
        lSalle.setAlignment(Pos.CENTER);

        VBox cell = new VBox(2, lMat, lEns, lSalle);
        cell.setPrefWidth(largeur);
        cell.setPrefHeight(hauteur);
        cell.setPadding(new Insets(4, 4, 4, 4));
        cell.setAlignment(Pos.CENTER);
        // Barre colorée à gauche selon le type (comme la légende)
        cell.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + brd + "; -fx-border-width: 0 0 0 4;");

        // Tooltip horaire
        Tooltip tip = new Tooltip(
            cours.getMatiere() + "  [" + cours.getTypeCours() + "]\n" +
            cours.getHeureDebut() + " → " + cours.getHeureFin() +
            "  (" + cours.getDuree() + " min)\n" +
            "Salle : " + nomSalle + "\n" +
            "Classe : " + cours.getClasse());
        Tooltip.install(cell, tip);

        return cell;
    }

    private StackPane cellVide(double largeur, double hauteur, boolean estPause) {
        StackPane cell = new StackPane();
        cell.setPrefWidth(largeur);
        cell.setPrefHeight(hauteur);
        cell.setStyle("-fx-background-color: " + (estPause ? PAUSE_BG : VIDE_BG) + "; -fx-border-color: #ddd; -fx-border-width: 0.3;");
        return cell;
    }

    private HBox creerLegende() {
        HBox leg = new HBox(20);
        leg.setAlignment(Pos.CENTER_LEFT);
        leg.setPadding(new Insets(2, 0, 4, 0));
        String[][] types = {{"CM", CM_BG, CM_BRD}, {"TD", TD_BG, TD_BRD}, {"TP", TP_BG, TP_BRD}};
        for (String[] tc : types) {
            Label badge = new Label(tc[0]);
            badge.setStyle("-fx-padding: 2 10; -fx-background-color: " + tc[1] +
                "; -fx-border-color: " + tc[2] + "; -fx-border-width: 0 0 0 4; -fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + tc[2] + ";");
            leg.getChildren().add(badge);
        }
        Label lSalle = new Label("Salle en rouge");
        lSalle.setStyle("-fx-font-size: 11; -fx-text-fill: " + SALLE_COLOR + "; -fx-font-weight: bold;");
        leg.getChildren().add(lSalle);
        return leg;
    }
}
