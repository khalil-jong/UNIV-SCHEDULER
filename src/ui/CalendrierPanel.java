package ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dao.CoursDAO;
import dao.SalleDAO;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Cours;
import models.Salle;

public class CalendrierPanel {

    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private LocalDate semaineCourante = LocalDate.now();
    private BorderPane zoneCalendrier;
    private DateTimeFormatter heureFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // Vue : SEMAINE ou JOUR
    private String vue = "SEMAINE";

    // Filtre classe : null = toutes
    private String classeFiltre = null;

    // Couleurs par classe (cycle automatique)
    private static final String[] COULEURS = {
        "#d6eaf8", "#d5f5e3", "#fdecea", "#fef9e7",
        "#f4ecf7", "#eafaf1", "#fdf2e9", "#eaf2ff"
    };
    private static final String[] BORDURES = {
        "#2980b9", "#27ae60", "#e74c3c", "#f39c12",
        "#8e44ad", "#1abc9c", "#e67e22", "#2c3e50"
    };

    public VBox createPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Label titre = new Label("📅 Calendrier des Cours");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        HBox navBar = creerNavBar();

        zoneCalendrier = new BorderPane();
        afficherVueSemaine();

        panel.getChildren().addAll(titre, navBar, zoneCalendrier);
        return panel;
    }

    // ── Barre de navigation + filtre classe ──────────────────────────
    private HBox creerNavBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8));
        bar.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5;");

        Button btnPrev      = new Button("◀ Précédent");
        Button btnSuivant   = new Button("Suivant ▶");
        Button btnAujourdhui = new Button("Aujourd'hui");
        btnAujourdhui.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        ToggleGroup tg = new ToggleGroup();
        ToggleButton btnSemaine = new ToggleButton("Semaine");
        ToggleButton btnJour    = new ToggleButton("Jour");
        btnSemaine.setToggleGroup(tg);
        btnJour.setToggleGroup(tg);
        btnSemaine.setSelected(true);

        // ── Filtre par classe ──
        Label lblFiltre = new Label("Classe :");
        lblFiltre.setStyle("-fx-font-size: 12;");

        ComboBox<String> cbClasse = new ComboBox<>();
        cbClasse.getItems().add("Toutes les classes");
        cbClasse.getItems().addAll(coursDAO.obtenirToutesLesClasses());
        cbClasse.setValue("Toutes les classes");
        cbClasse.setPrefWidth(180);
        cbClasse.setOnAction(e -> {
            String val = cbClasse.getValue();
            classeFiltre = (val == null || val.equals("Toutes les classes")) ? null : val;
            rafraichir();
        });

        // Bouton rafraîchir la liste des classes (si de nouvelles ont été ajoutées)
        Button btnRefresh = new Button("🔄");
        btnRefresh.setTooltip(new Tooltip("Rafraîchir la liste des classes"));
        btnRefresh.setOnAction(e -> {
            String current = cbClasse.getValue();
            cbClasse.getItems().clear();
            cbClasse.getItems().add("Toutes les classes");
            cbClasse.getItems().addAll(coursDAO.obtenirToutesLesClasses());
            cbClasse.setValue(current != null && cbClasse.getItems().contains(current) ? current : "Toutes les classes");
        });

        btnPrev.setOnAction(e -> {
            if (vue.equals("SEMAINE")) {
				semaineCourante = semaineCourante.minusWeeks(1);
			} else {
				semaineCourante = semaineCourante.minusDays(1);
			}
            rafraichir();
        });
        btnSuivant.setOnAction(e -> {
            if (vue.equals("SEMAINE")) {
				semaineCourante = semaineCourante.plusWeeks(1);
			} else {
				semaineCourante = semaineCourante.plusDays(1);
			}
            rafraichir();
        });
        btnAujourdhui.setOnAction(e -> {
            semaineCourante = LocalDate.now();
            rafraichir();
        });
        btnSemaine.setOnAction(e -> { vue = "SEMAINE"; rafraichir(); });
        btnJour.setOnAction(e -> { vue = "JOUR"; rafraichir(); });

        bar.getChildren().addAll(
            btnPrev, btnAujourdhui, btnSuivant,
            new Separator(Orientation.VERTICAL),
            new HBox(2, btnSemaine, btnJour),
            new Separator(Orientation.VERTICAL),
            lblFiltre, cbClasse, btnRefresh
        );
        return bar;
    }

    private void rafraichir() {
        if (vue.equals("SEMAINE")) {
			afficherVueSemaine();
		} else {
			afficherVueJour();
		}
    }

    // ── Vue SEMAINE ───────────────────────────────────────────────────
    private void afficherVueSemaine() {
        LocalDate lundi = semaineCourante.with(java.time.DayOfWeek.MONDAY);

        // Récupérer les cours selon le filtre
        List<Cours> coursDeLaSemaine = (classeFiltre == null)
            ? coursDAO.obtenirParSemaine(lundi.atStartOfDay())
            : coursDAO.obtenirParSemaineEtClasse(lundi.atStartOfDay(), classeFiltre);

        // Obtenir les classes présentes pour leur attribuer une couleur
        List<String> classesPresentes = coursDeLaSemaine.stream()
            .map(Cours::getClasse).distinct().sorted()
            .collect(Collectors.toList());

        VBox contenu = new VBox(2);
        contenu.setPadding(new Insets(5));

        // Légende des couleurs par classe
        if (classeFiltre == null && !classesPresentes.isEmpty()) {
            HBox legende = new HBox(12);
            legende.setPadding(new Insets(4, 4, 8, 4));
            legende.setAlignment(Pos.CENTER_LEFT);
            Label lblLeg = new Label("Légende : ");
            lblLeg.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
            legende.getChildren().add(lblLeg);
            for (int i = 0; i < classesPresentes.size(); i++) {
                String couleur = COULEURS[i % COULEURS.length];
                String bordure = BORDURES[i % BORDURES.length];
                Label badge = new Label(classesPresentes.get(i));
                badge.setStyle(
                    "-fx-font-size: 11; -fx-padding: 2 8; " +
                    "-fx-background-color: " + couleur + "; " +
                    "-fx-border-color: " + bordure + "; " +
                    "-fx-border-radius: 10; -fx-background-radius: 10;");
                legende.getChildren().add(badge);
            }
            contenu.getChildren().add(legende);
        }

        // En-têtes des jours
        HBox header = new HBox(2);
        header.getChildren().add(creerCellule("Heure", 70, "#bdc3c7", true));
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
        for (int i = 0; i < 6; i++) {
            LocalDate jour = lundi.plusDays(i);
            String txt = jours[i] + "\n" + jour.getDayOfMonth() + "/" + jour.getMonthValue();
            boolean estAujourdhui = jour.equals(LocalDate.now());
            header.getChildren().add(creerCellule(txt, 160,
                estAujourdhui ? "#3498db" : "#2c3e50", true));
        }
        contenu.getChildren().add(header);

        // Lignes horaires 7h → 20h
        for (int heure = 7; heure <= 20; heure++) {
            HBox ligne = new HBox(2);
            ligne.getChildren().add(creerCellule(heure + ":00", 70, "#ecf0f1", false));

            for (int j = 0; j < 6; j++) {
                final LocalDate jourJ = lundi.plusDays(j);
                final int heureH = heure;

                // ─── CORRECTION CLEF : récupérer TOUS les cours à cette heure ───
                List<Cours> coursIci = coursDeLaSemaine.stream()
                    .filter(c -> c.getDateDebut().toLocalDate().equals(jourJ)
                              && c.getDateDebut().getHour() == heureH)
                    .collect(Collectors.toList());

                if (coursIci.isEmpty()) {
                    ligne.getChildren().add(creerCellule("", 160, "white", false));
                } else {
                    // Cellule multi-cours : empiler verticalement
                    VBox cellule = new VBox(2);
                    cellule.setPrefWidth(160);
                    cellule.setMinHeight(50);
                    cellule.setPadding(new Insets(2));
                    cellule.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 0.5;");

                    for (Cours c : coursIci) {
                        int idx = classesPresentes.indexOf(c.getClasse());
                        String couleur = COULEURS[idx >= 0 ? idx % COULEURS.length : 0];
                        String bordure = BORDURES[idx >= 0 ? idx % BORDURES.length : 0];

                        Salle salle = salleDAO.obtenirParId(c.getSalleId());
                        String nomSalle = salle != null ? salle.getNumero() : "?";

                        Label lbl = new Label(
                            c.getMatiere() + "\n" +
                            c.getClasse() + "\n" +
                            c.getEnseignant() + "\nSalle " + nomSalle);
                        lbl.setWrapText(true);
                        lbl.setMaxWidth(154);
                        lbl.setStyle(
                            "-fx-font-size: 10; -fx-padding: 3; " +
                            "-fx-background-color: " + couleur + "; " +
                            "-fx-border-color: " + bordure + "; " +
                            "-fx-border-radius: 3; -fx-background-radius: 3;");
                        cellule.getChildren().add(lbl);
                    }
                    ligne.getChildren().add(cellule);
                }
            }
            contenu.getChildren().add(ligne);
        }

        ScrollPane scroll = new ScrollPane(contenu);
        scroll.setFitToWidth(true);
        zoneCalendrier.setCenter(scroll);
    }

    // ── Vue JOUR ─────────────────────────────────────────────────────
    private void afficherVueJour() {
        VBox contenu = new VBox(8);
        contenu.setPadding(new Insets(15));

        String nomJour = semaineCourante.getDayOfWeek()
            .getDisplayName(TextStyle.FULL, Locale.FRENCH);
        String filtreLabel = classeFiltre != null ? "  —  Classe : " + classeFiltre : "  —  Toutes les classes";

        Label labelJour = new Label(nomJour + " "
            + semaineCourante.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            + filtreLabel);
        labelJour.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        contenu.getChildren().add(labelJour);

        List<Cours> coursduJour = (classeFiltre == null)
            ? coursDAO.obtenirParJour(semaineCourante.atStartOfDay())
            : coursDAO.obtenirParJourEtClasse(semaineCourante.atStartOfDay(), classeFiltre);

        if (coursduJour.isEmpty()) {
            Label vide = new Label("Aucun cours ce jour" + (classeFiltre != null ? " pour la classe " + classeFiltre : "") + ".");
            vide.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6; -fx-padding: 20;");
            contenu.getChildren().add(vide);
        } else {
            // Grouper par heure pour afficher les cours simultanés côte à côte
            List<Integer> heuresDispo = coursduJour.stream()
                .map(c -> c.getDateDebut().getHour()).distinct().sorted()
                .collect(Collectors.toList());

            for (int heure : heuresDispo) {
                final int h = heure;
                List<Cours> coursCreneau = coursduJour.stream()
                    .filter(c -> c.getDateDebut().getHour() == h)
                    .collect(Collectors.toList());

                // Label heure
                Label lHeure = new Label("🕐 " + String.format("%02d:00", heure));
                lHeure.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
                contenu.getChildren().add(lHeure);

                // Si plusieurs cours au même créneau → afficher côte à côte
                HBox rangee = new HBox(8);
                for (Cours c : coursCreneau) {
                    Salle salle = salleDAO.obtenirParId(c.getSalleId());
                    String nomSalle = salle != null ? salle.getNumero() : "?";

                    VBox carte = new VBox(4);
                    carte.setPadding(new Insets(12));
                    carte.setPrefWidth(280);
                    carte.setStyle(
                        "-fx-background-color: #d6eaf8; -fx-border-color: #3498db; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; " +
                        "-fx-border-width: 0 0 0 4;");

                    Label lMatiere = new Label("📚 " + c.getMatiere());
                    lMatiere.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

                    Label lClasse = new Label("🎓 " + c.getClasse()
                        + (c.getGroupe().isEmpty() ? "" : "  —  " + c.getGroupe()));
                    lClasse.setStyle("-fx-font-size: 12;");

                    Label lEns = new Label("👤 " + c.getEnseignant());
                    lEns.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

                    Label lDuree = new Label("⏱ " + c.getDateDebut().format(heureFormatter)
                        + " → " + c.getDateFin().format(heureFormatter)
                        + "  (" + c.getDuree() + " min)");
                    lDuree.setStyle("-fx-font-size: 12;");

                    Label lSalle = new Label("🏫 Salle " + nomSalle);
                    lSalle.setStyle("-fx-font-size: 12;");

                    carte.getChildren().addAll(lMatiere, lClasse, lEns, lDuree, lSalle);
                    rangee.getChildren().add(carte);
                }
                contenu.getChildren().add(rangee);
            }
        }

        ScrollPane scroll = new ScrollPane(contenu);
        scroll.setFitToWidth(true);
        zoneCalendrier.setCenter(scroll);
    }

    private VBox creerCellule(String texte, double largeur, String couleur, boolean bold) {
        VBox cell = new VBox();
        cell.setPrefWidth(largeur);
        cell.setMinHeight(50);
        cell.setPadding(new Insets(4));
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setStyle("-fx-background-color: " + couleur + "; -fx-border-color: #bdc3c7; -fx-border-width: 0.5;");
        Label lbl = new Label(texte);
        lbl.setWrapText(true);
        lbl.setMaxWidth(largeur - 8);
        lbl.setStyle(bold ? "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 11;"
                          : "-fx-font-size: 10;");
        cell.getChildren().add(lbl);
        return cell;
    }
}
