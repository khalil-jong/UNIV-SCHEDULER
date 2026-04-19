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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.EmploiDuTemps;
import models.Salle;

/**
 * Emploi du temps hebdomadaire au format grille visuelle — redesigné.
 *  - Lignes = tranches horaires (08h–19h)
 *  - Colonnes = Lundi → Samedi
 *  - Cellules colorées par type de cours (CM/TD/TP)
 *  - En-têtes épurés, légende claire
 *
 * Logique de construction inchangée. Design harmonisé avec Design.java.
 */
public class EmploiDuTempsViewPanel {

    private ClasseDAO        classeDAO = new ClasseDAO();
    private EmploiDuTempsDAO edtDAO    = new EmploiDuTempsDAO();
    private SalleDAO         salleDAO  = new SalleDAO();

    private String  classeInitiale;
    private boolean modeEnseignant;
    private String  nomEnseignant;

    private static final int H_DEBUT = 8;
    private static final int H_FIN   = 19;
    private static final String[] JOURS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};

    // Palette des cellules — alignée sur Design.java
    private static final String ENTETE_BG   = Design.TEXT_DARK;
    private static final String ENTETE_FG   = "white";
    private static final String VIDE_BG     = Design.BG_WHITE;
    private static final String PAUSE_BG    = "#eaecf0";
    private static final String SALLE_COLOR = Design.DANGER;

    private static final String CM_BG  = "#dbeeff"; private static final String CM_BRD  = Design.ADMIN_ACCENT;
    private static final String TD_BG  = "#d5f5e3"; private static final String TD_BRD  = Design.SUCCESS;
    private static final String TP_BG  = "#fdecea"; private static final String TP_BRD  = Design.DANGER;

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
        VBox container = new VBox(16);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        // ── En-tête ──────────────────────────────────────────────────
        Label titre = Design.pageTitle(modeEnseignant
            ? "📅  Mon Emploi du Temps — " + nomEnseignant
            : "📅  Emploi du Temps Hebdomadaire");

        VBox zoneGrille = new VBox(12);

        if (!modeEnseignant) {
            // ── Sélecteur de classe ───────────────────────────────────
            HBox selCard = new HBox(12);
            selCard.setAlignment(Pos.CENTER_LEFT);
            selCard.setPadding(new Insets(12, 16, 12, 16));
            selCard.setStyle(Design.CARD_STYLE);

            Label lblCl = new Label("Classe :");
            lblCl.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";");

            ComboBox<String> cbClasse = new ComboBox<>();
            List<String> classes = classeDAO.obtenirNomsClasses();
            cbClasse.getItems().addAll(classes);
            if (classeInitiale != null && classes.contains(classeInitiale)) {
				cbClasse.setValue(classeInitiale);
			} else {
				cbClasse.setPromptText("Sélectionner une classe…");
			}
            cbClasse.setPrefWidth(260);

            Label lblNb = new Label("");
            lblNb.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";");

            cbClasse.setOnAction(e -> {
                zoneGrille.getChildren().clear();
                String v = cbClasse.getValue();
                if (v != null) {
                    List<EmploiDuTemps> data = edtDAO.obtenirParClasse(v);
                    zoneGrille.getChildren().addAll(creerLegende(), construireGrille(data, v));
                    lblNb.setText("→  " + data.size() + " créneau(x)");
                }
            });

            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            selCard.getChildren().addAll(lblCl, cbClasse, lblNb);

            container.getChildren().addAll(titre, selCard);

            if (cbClasse.getValue() != null) {
                List<EmploiDuTemps> data = edtDAO.obtenirParClasse(cbClasse.getValue());
                zoneGrille.getChildren().addAll(creerLegende(), construireGrille(data, cbClasse.getValue()));
                lblNb.setText("→  " + data.size() + " créneau(x)");
            }
        } else {
            container.getChildren().addAll(titre, creerLegende());
            List<EmploiDuTemps> data = edtDAO.obtenirParEnseignant(nomEnseignant);
            zoneGrille.getChildren().add(construireGrille(data, null));
        }

        container.getChildren().add(zoneGrille);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ── Grille ───────────────────────────────────────────────────────
    private GridPane construireGrille(List<EmploiDuTemps> data, String classe) {
        GridPane grid = new GridPane();
        grid.setHgap(1); grid.setVgap(1);
        grid.setStyle("-fx-background-color: #c8cdd8; -fx-padding: 1; -fx-background-radius: 8;");

        int nbHeures = H_FIN - H_DEBUT;

        // Ligne 0 : en-têtes
        grid.add(cellEntete("Heures", 85, 38), 0, 0);
        for (int j = 0; j < JOURS.length; j++) {
			grid.add(cellEntete(JOURS[j], 148, 38), j + 1, 0);
		}

        boolean[][] occupied = new boolean[6][nbHeures];

        for (int h = 0; h < nbHeures; h++) {
            int heure = H_DEBUT + h;
            grid.add(cellHeure(String.format("%02dh–%02dh", heure, heure + 1), 85, 40), 0, h + 1);

            for (int j = 0; j < 6; j++) {
                if (occupied[j][h]) {
					continue;
				}

                EmploiDuTemps cours = trouverCours(data, j + 1, LocalTime.of(heure, 0));

                if (cours != null) {
                    int rowspan = Math.max(1, (int) Math.ceil(cours.getDuree() / 60.0));
                    for (int r = 0; r < rowspan && (h + r) < nbHeures; r++) {
						occupied[j][h + r] = true;
					}
                    Salle salle = salleDAO.obtenirParId(cours.getSalleId());
                    javafx.scene.Node cellule = cellCours(cours, salle != null ? salle.getNumero() : "?", 148, 40 * rowspan + (rowspan - 1));
                    GridPane.setRowSpan(cellule, rowspan);
                    grid.add(cellule, j + 1, h + 1);
                } else if (heure == 13 || heure == 14) {
                    grid.add(cellVide(148, 40, true), j + 1, h + 1);
                } else {
                    grid.add(cellVide(148, 40, false), j + 1, h + 1);
                }
            }

            // Pause méridienne fusionnée
            if (heure == 13) {
                grid.getChildren().removeIf(n -> {
                    Integer row = GridPane.getRowIndex(n);
                    Integer col = GridPane.getColumnIndex(n);
                    return row != null && row == (13 - H_DEBUT + 1) && col != null && col >= 1;
                });
                Label lPause = new Label("Pause méridienne");
                lPause.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_MUTED + ";");
                StackPane pauseCell = new StackPane(lPause);
                pauseCell.setStyle("-fx-background-color: " + PAUSE_BG + "; -fx-border-color: #ccc;");
                pauseCell.setPrefHeight(40);
                GridPane.setColumnSpan(pauseCell, 6);
                grid.add(pauseCell, 1, 13 - H_DEBUT + 1);
            }
        }

        return grid;
    }

    private EmploiDuTemps trouverCours(List<EmploiDuTemps> data, int jourIdx, LocalTime tranche) {
        for (EmploiDuTemps e : data) {
			if (e.getJourSemaine() == jourIdx && e.getHeureDebut().equals(tranche)) {
				return e;
			}
		}
        return null;
    }

    // ── Cellules ─────────────────────────────────────────────────────
    private StackPane cellEntete(String texte, double w, double h) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + ENTETE_FG + ";");
        lbl.setAlignment(Pos.CENTER);
        StackPane cell = new StackPane(lbl);
        cell.setPrefWidth(w); cell.setPrefHeight(h);
        cell.setStyle("-fx-background-color: " + ENTETE_BG + ";");
        return cell;
    }

    private StackPane cellHeure(String texte, double w, double h) {
        Label lbl = new Label(texte);
        lbl.setStyle("-fx-font-size: 10; -fx-text-fill: #444; -fx-font-weight: bold;");
        lbl.setAlignment(Pos.CENTER);
        StackPane cell = new StackPane(lbl);
        cell.setPrefWidth(w); cell.setPrefHeight(h);
        cell.setStyle("-fx-background-color: #f0f1f5; -fx-border-color: #d0d5e0; -fx-border-width: 0.5;");
        return cell;
    }

    private VBox cellCours(EmploiDuTemps cours, String nomSalle, double w, double h) {
        String type = cours.getTypeCours() != null ? cours.getTypeCours().toUpperCase() : "CM";
        String bg, brd;
        switch (type) {
            case "TD": bg = TD_BG; brd = TD_BRD; break;
            case "TP": bg = TP_BG; brd = TP_BRD; break;
            default:   bg = CM_BG; brd = CM_BRD; break;
        }

        Label lMat = new Label(cours.getMatiere());
        lMat.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + Design.TEXT_DARK + ";");
        lMat.setWrapText(true); lMat.setMaxWidth(w - 10); lMat.setAlignment(Pos.CENTER);

        Label lEns = new Label(cours.getEnseignant() + "  (" + type + ")");
        lEns.setStyle("-fx-font-size: 9; -fx-text-fill: " + Design.TEXT_MUTED + ";");
        lEns.setWrapText(true); lEns.setMaxWidth(w - 10); lEns.setAlignment(Pos.CENTER);

        Label lSalle = new Label(nomSalle);
        lSalle.setStyle("-fx-font-size: 10; -fx-text-fill: " + SALLE_COLOR + "; -fx-font-weight: bold;");
        lSalle.setAlignment(Pos.CENTER);

        VBox cell = new VBox(2, lMat, lEns, lSalle);
        cell.setPrefWidth(w); cell.setPrefHeight(h);
        cell.setPadding(new Insets(4));
        cell.setAlignment(Pos.CENTER);
        cell.setStyle("-fx-background-color: " + bg + "; -fx-border-color: " + brd + "; -fx-border-width: 0 0 0 4;");

        Tooltip.install(cell, new Tooltip(
            cours.getMatiere() + "  [" + type + "]\n" +
            cours.getHeureDebut() + " → " + cours.getHeureFin() +
            "  (" + cours.getDuree() + " min)\n" +
            "Salle : " + nomSalle + "\n" +
            "Classe : " + cours.getClasse()));
        return cell;
    }

    private StackPane cellVide(double w, double h, boolean estPause) {
        StackPane cell = new StackPane();
        cell.setPrefWidth(w); cell.setPrefHeight(h);
        cell.setStyle("-fx-background-color: " + (estPause ? PAUSE_BG : VIDE_BG) + "; -fx-border-color: #dde1ea; -fx-border-width: 0.3;");
        return cell;
    }

    private HBox creerLegende() {
        HBox leg = new HBox(16);
        leg.setAlignment(Pos.CENTER_LEFT);
        leg.setPadding(new Insets(8, 14, 8, 14));
        leg.setStyle(Design.CARD_STYLE);

        String[][] types = {{"CM", CM_BG, CM_BRD}, {"TD", TD_BG, TD_BRD}, {"TP", TP_BG, TP_BRD}};
        for (String[] tc : types) {
            Label badge = new Label("  " + tc[0] + "  ");
            badge.setStyle(
                "-fx-padding: 3 10; -fx-background-color: " + tc[1] + ";" +
                "-fx-border-color: " + tc[2] + "; -fx-border-width: 0 0 0 4;" +
                "-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + tc[2] + ";" +
                "-fx-background-radius: 4;"
            );
            leg.getChildren().add(badge);
        }
        Label lSalle = new Label("🔴  Salle en rouge");
        lSalle.setStyle("-fx-font-size: 11; -fx-text-fill: " + SALLE_COLOR + "; -fx-font-weight: bold;");
        leg.getChildren().add(lSalle);
        return leg;
    }
}
