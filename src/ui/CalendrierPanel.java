package ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import dao.CoursDAO;
import dao.EmploiDuTempsDAO;
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

/**
 * Calendrier des cours — affiche :
 *   • Les cours ponctuels (table `cours`)
 *   • Les créneaux d'emploi du temps hebdomadaire (table `emploi_du_temps`)
 * Filtre optionnel par classe.
 */
public class CalendrierPanel {

    private CoursDAO          coursDAO = new CoursDAO();
    private SalleDAO          salleDAO = new SalleDAO();
    private EmploiDuTempsDAO  edtDAO   = new EmploiDuTempsDAO();

    private LocalDate  semaineCourante = LocalDate.now();
    private BorderPane zoneCalendrier;
    private DateTimeFormatter heureFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private String vue         = "SEMAINE";
    private String classeFiltre = null;

    // Couleurs cours ponctuels
    private static final String[] COULEURS = {
        "#d6eaf8","#d5f5e3","#fdecea","#fef9e7",
        "#f4ecf7","#eafaf1","#fdf2e9","#eaf2ff"
    };
    private static final String[] BORDURES = {
        "#2980b9","#27ae60","#e74c3c","#f39c12",
        "#8e44ad","#1abc9c","#e67e22","#2c3e50"
    };
    // Couleur EDT (crème comme l'EDT)

    public VBox createPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        Label titre = new Label("📅 Calendrier des Cours");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        zoneCalendrier = new BorderPane();
        afficherVueSemaine();

        panel.getChildren().addAll(titre, creerNavBar(), creerLegende(), zoneCalendrier);
        return panel;
    }

    // ── Barre de navigation ──────────────────────────────────────────
    private HBox creerNavBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8));
        bar.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5;");

        Button btnPrev       = new Button("◀ Précédent");
        Button btnSuivant    = new Button("Suivant ▶");
        Button btnAujourdhui = new Button("Aujourd'hui");
        btnAujourdhui.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        ToggleGroup tg = new ToggleGroup();
        ToggleButton btnSemaine = new ToggleButton("Semaine");
        ToggleButton btnJour    = new ToggleButton("Jour");
        btnSemaine.setToggleGroup(tg); btnJour.setToggleGroup(tg);
        btnSemaine.setSelected(true);

        // Filtre classe
        ComboBox<String> cbClasse = new ComboBox<>();
        cbClasse.getItems().add("Toutes les classes");
        // Fusionner classes des cours ponctuels + EDT
        List<String> classesEDT   = edtDAO.obtenirToutesLesClasses();
        List<String> classesCours = coursDAO.obtenirToutesLesClasses();
        classesEDT.stream().filter(c -> !cbClasse.getItems().contains(c)).forEach(cbClasse.getItems()::add);
        classesCours.stream().filter(c -> !cbClasse.getItems().contains(c)).forEach(cbClasse.getItems()::add);
        cbClasse.setValue("Toutes les classes");
        cbClasse.setPrefWidth(185);
        cbClasse.setOnAction(e -> {
            String v = cbClasse.getValue();
            classeFiltre = (v == null || v.equals("Toutes les classes")) ? null : v;
            rafraichir();
        });

        Button btnRefresh = new Button("🔄");
        btnRefresh.setTooltip(new Tooltip("Rafraîchir les classes"));
        btnRefresh.setOnAction(e -> {
            String cur = cbClasse.getValue();
            cbClasse.getItems().clear();
            cbClasse.getItems().add("Toutes les classes");
            edtDAO.obtenirToutesLesClasses().forEach(c -> { if (!cbClasse.getItems().contains(c)) {
				cbClasse.getItems().add(c);
			} });
            coursDAO.obtenirToutesLesClasses().forEach(c -> { if (!cbClasse.getItems().contains(c)) {
				cbClasse.getItems().add(c);
			} });
            cbClasse.setValue(cbClasse.getItems().contains(cur) ? cur : "Toutes les classes");
            rafraichir();
        });

        btnPrev.setOnAction(e -> {
            semaineCourante = vue.equals("SEMAINE") ? semaineCourante.minusWeeks(1) : semaineCourante.minusDays(1);
            rafraichir();
        });
        btnSuivant.setOnAction(e -> {
            semaineCourante = vue.equals("SEMAINE") ? semaineCourante.plusWeeks(1) : semaineCourante.plusDays(1);
            rafraichir();
        });
        btnAujourdhui.setOnAction(e -> { semaineCourante = LocalDate.now(); rafraichir(); });
        btnSemaine.setOnAction(e -> { vue = "SEMAINE"; rafraichir(); });
        btnJour.setOnAction(e    -> { vue = "JOUR";    rafraichir(); });

        bar.getChildren().addAll(
            btnPrev, btnAujourdhui, btnSuivant,
            new Separator(Orientation.VERTICAL),
            new HBox(2, btnSemaine, btnJour),
            new Separator(Orientation.VERTICAL),
            new Label("Classe :"), cbClasse, btnRefresh
        );
        return bar;
    }

    private HBox creerLegende() {
        HBox leg = new HBox(14);
        leg.setAlignment(Pos.CENTER_LEFT);
        leg.setPadding(new Insets(2, 0, 4, 0));

        Label lPonct = new Label("📚 Cours sous demande (ponctuel)");
        lPonct.setStyle("-fx-padding: 2 8; -fx-background-color: #d6eaf8; -fx-border-color: #2980b9; -fx-border-width: 0 0 0 4; -fx-font-size: 11;");

        Label note = new Label("ℹ️ Les emplois du temps hebdomadaires sont consultables dans « Emploi du temps »");
        note.setStyle("-fx-font-size: 10; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        leg.getChildren().addAll(new Label("Légende : "), lPonct, note);
        return leg;
    }

    private void rafraichir() {
        if (vue.equals("SEMAINE")) {
			afficherVueSemaine();
		} else {
			afficherVueJour();
		}
    }

    // ── Vue SEMAINE ──────────────────────────────────────────────────
    private void afficherVueSemaine() {
        LocalDate lundi = semaineCourante.with(java.time.DayOfWeek.MONDAY);

        // Cours ponctuels (sous demande uniquement — l'EDT est dans les emplois du temps)
        List<Cours> coursPonctuels = (classeFiltre == null)
            ? coursDAO.obtenirParSemaine(lundi.atStartOfDay())
            : coursDAO.obtenirParSemaineEtClasse(lundi.atStartOfDay(), classeFiltre);

        // Classes présentes (pour couleurs)
        List<String> classesPresentes = new ArrayList<>();
        coursPonctuels.stream().map(Cours::getClasse).filter(c -> !classesPresentes.contains(c)).forEach(classesPresentes::add);

        VBox contenu = new VBox(2);
        contenu.setPadding(new Insets(5));

        // En-têtes
        HBox header = new HBox(2);
        header.getChildren().add(creerCellule("Heure", 70, "#2c3e50", true, false));
        String[] jours = {"Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"};
        for (int i = 0; i < 6; i++) {
            LocalDate jour = lundi.plusDays(i);
            String txt = jours[i] + "\n" + jour.getDayOfMonth() + "/" + jour.getMonthValue();
            header.getChildren().add(creerCellule(txt, 155,
                jour.equals(LocalDate.now()) ? "#3498db" : "#2c3e50", true, false));
        }
        contenu.getChildren().add(header);

        // Lignes 7h → 20h
        for (int heure = 7; heure <= 20; heure++) {
            HBox ligne = new HBox(2);
            ligne.getChildren().add(creerCellule(heure + ":00", 70, "#ecf0f1", false, false));

            for (int j = 0; j < 6; j++) {
                final LocalDate jourJ = lundi.plusDays(j);
                final int heureH = heure;

                List<Cours> ponctuels = coursPonctuels.stream()
                    .filter(c -> c.getDateDebut().toLocalDate().equals(jourJ)
                              && c.getDateDebut().getHour() == heureH)
                    .collect(Collectors.toList());

                if (ponctuels.isEmpty()) {
                    ligne.getChildren().add(creerCellule("", 155, "white", false, false));
                } else {
                    VBox cellule = new VBox(2);
                    cellule.setPrefWidth(155); cellule.setMinHeight(50);
                    cellule.setPadding(new Insets(2));
                    cellule.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 0.5;");

                    // Cours ponctuels (sous demande)
                    for (Cours c : ponctuels) {
                        int idx = classesPresentes.indexOf(c.getClasse());
                        String bg  = COULEURS[idx >= 0 ? idx % COULEURS.length : 0];
                        String brd = BORDURES[idx >= 0 ? idx % BORDURES.length : 0];
                        Salle salle = salleDAO.obtenirParId(c.getSalleId());
                        String nomSalle = salle != null ? salle.getNumero() : "?";
                        Label lbl = new Label(
                            c.getMatiere() + "\n" + c.getClasse() + "\n" +
                            c.getEnseignant() + "\nSalle " + nomSalle);
                        lbl.setWrapText(true); lbl.setMaxWidth(149);
                        lbl.setStyle("-fx-font-size: 10; -fx-padding: 3; " +
                            "-fx-background-color: " + bg + "; " +
                            "-fx-border-color: " + brd + "; " +
                            "-fx-border-width: 0 0 0 3; -fx-border-radius: 2;");
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

    // ── Vue JOUR ────────────────────────────────────────────────────
    private void afficherVueJour() {
        VBox contenu = new VBox(8);
        contenu.setPadding(new Insets(15));

        String nomJour = semaineCourante.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        String filtreLabel = classeFiltre != null ? "  —  Classe : " + classeFiltre : "  —  Toutes les classes";
        Label labelJour = new Label(nomJour + " "
            + semaineCourante.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + filtreLabel);
        labelJour.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        contenu.getChildren().add(labelJour);

        // Cours ponctuels sous demande uniquement
        List<Cours> coursPonctuels = (classeFiltre == null)
            ? coursDAO.obtenirParJour(semaineCourante.atStartOfDay())
            : coursDAO.obtenirParJourEtClasse(semaineCourante.atStartOfDay(), classeFiltre);

        List<Cours> tous = new ArrayList<>(coursPonctuels);
        tous.sort((a, b) -> a.getDateDebut().compareTo(b.getDateDebut()));

        if (tous.isEmpty()) {
            Label vide = new Label("Aucun cours ce jour" + (classeFiltre != null ? " pour la classe " + classeFiltre : "") + ".");
            vide.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6; -fx-padding: 20;");
            contenu.getChildren().add(vide);
        } else {
            List<Integer> heuresDispo = tous.stream()
                .map(c -> c.getDateDebut().getHour()).distinct().sorted()
                .collect(Collectors.toList());

            for (int heure : heuresDispo) {
                final int h = heure;
                List<Cours> rangee = tous.stream()
                    .filter(c -> c.getDateDebut().getHour() == h).collect(Collectors.toList());

                Label lHeure = new Label("🕐 " + String.format("%02d:00", heure));
                lHeure.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
                contenu.getChildren().add(lHeure);

                HBox row = new HBox(8);
                for (Cours c : rangee) {
                    Salle salle = salleDAO.obtenirParId(c.getSalleId());
                    String nomSalle = salle != null ? salle.getNumero() : "?";

                    VBox carte = new VBox(4);
                    carte.setPadding(new Insets(12)); carte.setPrefWidth(270);
                    carte.setStyle("-fx-background-color: #d6eaf8; -fx-border-color: #3498db; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 0 0 0 4;");

                    Label lMat = new Label("📚 " + c.getMatiere());
                    lMat.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
                    Label lClasse = new Label("🎓 " + c.getClasse());
                    lClasse.setStyle("-fx-font-size: 12;");
                    Label lEns = new Label("👤 " + c.getEnseignant());
                    lEns.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
                    Label lDuree = new Label("⏱ " + c.getDateDebut().format(heureFormatter)
                        + " → " + c.getDateFin().format(heureFormatter) + "  (" + c.getDuree() + " min)");
                    lDuree.setStyle("-fx-font-size: 12;");
                    Label lSalle = new Label("🚪 Salle " + nomSalle);
                    lSalle.setStyle("-fx-font-size: 12;");

                    carte.getChildren().addAll(lMat, lClasse, lEns, lDuree, lSalle);
                    row.getChildren().add(carte);
                }
                contenu.getChildren().add(row);
            }
        }

        ScrollPane scroll = new ScrollPane(contenu);
        scroll.setFitToWidth(true);
        zoneCalendrier.setCenter(scroll);
    }



    private VBox creerCellule(String texte, double largeur, String couleur, boolean bold, boolean unused) {
        VBox cell = new VBox();
        cell.setPrefWidth(largeur); cell.setMinHeight(50);
        cell.setPadding(new Insets(4)); cell.setAlignment(Pos.TOP_CENTER);
        cell.setStyle("-fx-background-color: " + couleur + "; -fx-border-color: #bdc3c7; -fx-border-width: 0.5;");
        Label lbl = new Label(texte);
        lbl.setWrapText(true); lbl.setMaxWidth(largeur - 8);
        lbl.setStyle(bold ? "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 11;" : "-fx-font-size: 10;");
        cell.getChildren().add(lbl);
        return cell;
    }
}
