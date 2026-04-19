package ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.CoursDAO;
import dao.SalleDAO;
import dao.UtilisateurDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Cours;
import models.Salle;
import models.Utilisateur;

/**
 * Tableau de bord redesigné — thème cohérent avec Design.java.
 * Structure : En-tête → Cartes statistiques (2 lignes) → Cours du jour → Conflits → État des salles
 */
public class DashboardPanel {

    private CoursDAO        coursDAO        = new CoursDAO();
    private SalleDAO        salleDAO        = new SalleDAO();
    private UtilisateurDAO  utilisateurDAO  = new UtilisateurDAO();

    public ScrollPane createPanel() {
        VBox panel = new VBox(24);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        // ── En-tête ──────────────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titreBox = new VBox(4);
        Label titre = Design.pageTitle("📊  Tableau de Bord");
        Label dateLbl = new Label("📅  " + LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH)));
        dateLbl.setStyle("-fx-font-size: 13; -fx-text-fill: " + Design.TEXT_MUTED + ";");
        titreBox.getChildren().addAll(titre, dateLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnActualiser = Design.btnSecondary("🔄  Actualiser");
        btnActualiser.setOnAction(e -> {
            ScrollPane newPanel = createPanel();
            javafx.scene.Parent parent = btnActualiser.getParent();
            int depth = 0;
            while (parent != null && !(parent instanceof ScrollPane) && depth < 15) {
                parent = parent.getParent();
                depth++;
            }
            if (parent instanceof ScrollPane) {
                ((ScrollPane) parent).setContent(newPanel.getContent());
            }
        });

        header.getChildren().addAll(titreBox, spacer, btnActualiser);
        panel.getChildren().add(header);

        // ── Données ──────────────────────────────────────────────────────────
        List<Salle>       salles   = salleDAO.obtenirTous();
        List<Cours>       cours    = coursDAO.obtenirTousAvecEDT();
        List<Utilisateur> users    = utilisateurDAO.obtenirTous();
        List<String>      conflits = coursDAO.detecterConflits();
        int mois = LocalDate.now().getMonthValue();

        long coursAujourdhui = cours.stream()
            .filter(c -> c.getDateDebut().toLocalDate().equals(LocalDate.now())).count();
        long coursParMois = cours.stream()
            .filter(c -> c.getDateDebut().getMonthValue() == mois).count();
        long sallesLibres = salleDAO
            .obtenirSallesDisponibles(java.time.LocalDateTime.now(), 60).size();
        long nbBatiments  = salles.stream().map(Salle::getBatiment).distinct().count();

        // ── Ligne 1 de cartes ─────────────────────────────────────────────
        HBox row1 = new HBox(14);
        row1.getChildren().addAll(
            Design.statCard("🚪", String.valueOf(salles.size()),   "Salles enregistrées",    Design.ADMIN_ACCENT),
            Design.statCard("📚", String.valueOf(cours.size()),    "Créneaux EDT actifs",    Design.GEST_ACCENT),
            Design.statCard("📅", String.valueOf(coursAujourdhui), "Cours aujourd'hui",       Design.ENS_ACCENT),
            Design.statCard("👥", String.valueOf(users.size()),    "Comptes actifs",          "#e67e22")
        );

        // ── Ligne 2 de cartes ─────────────────────────────────────────────
        HBox row2 = new HBox(14);
        row2.getChildren().addAll(
            Design.statCard("📆", String.valueOf(coursParMois),   "Cours ce mois",           "#1abc9c"),
            Design.statCard("🏗", String.valueOf(nbBatiments),    "Bâtiments actifs",        "#34495e"),
            Design.statCard("✅", String.valueOf(sallesLibres),   "Salles libres (1h)",       Design.SUCCESS)
        );

        panel.getChildren().addAll(row1, row2);

        // ── Cours du jour ─────────────────────────────────────────────────
        VBox sectionCours = Design.section("📋  Créneaux d'aujourd'hui");

        List<Cours> coursJour = cours.stream()
            .filter(c -> c.getDateDebut().toLocalDate().equals(LocalDate.now()))
            .sorted((a, b) -> a.getDateDebut().compareTo(b.getDateDebut()))
            .collect(java.util.stream.Collectors.toList());

        DateTimeFormatter hf = DateTimeFormatter.ofPattern("HH:mm");

        if (coursJour.isEmpty()) {
            Label vide = Design.muted("Aucun créneau EDT planifié aujourd'hui.");
            vide.setPadding(new Insets(10, 0, 4, 0));
            sectionCours.getChildren().add(vide);
        } else {
            VBox liste = new VBox(8);
            for (Cours c : coursJour) {
                Salle s = salleDAO.obtenirParId(c.getSalleId());
                HBox carte = new HBox(18);
                carte.setPadding(new Insets(11, 16, 11, 16));
                carte.setAlignment(Pos.CENTER_LEFT);
                carte.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: " + Design.ADMIN_ACCENT + ";" +
                    "-fx-border-width: 0 0 0 4;" +
                    "-fx-border-radius: 0 8 8 0;" +
                    "-fx-background-radius: 8;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);"
                );

                Label lH = new Label(c.getDateDebut().format(hf));
                lH.setStyle("-fx-font-weight:bold;-fx-font-size:13;-fx-text-fill:" +
                    Design.ADMIN_ACCENT + ";-fx-min-width:52;");
                Label lM = new Label(c.getMatiere());
                lM.setStyle("-fx-font-weight:bold;-fx-font-size:13;-fx-min-width:160;-fx-text-fill:" + Design.TEXT_DARK + ";");
                Label lE = new Label("👤  " + c.getEnseignant());
                lE.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";-fx-min-width:160;");
                Label lS = new Label("🚪  " + (s != null ? s.getNumero() : "?"));
                lS.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";-fx-min-width:80;");
                Label lC = new Label("🎓  " + c.getClasse());
                lC.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.TEXT_MUTED + ";");

                carte.getChildren().addAll(lH, lM, lE, lS, lC);
                liste.getChildren().add(carte);
            }
            sectionCours.getChildren().add(liste);
        }
        panel.getChildren().add(sectionCours);

        // ── Conflits ──────────────────────────────────────────────────────
        VBox sectionConflits = Design.section("⚠️  Conflits Horaires");

        if (!conflits.isEmpty()) {
            VBox listeConflits = new VBox(6);
            for (String conflit : conflits) {
                Label lbl = new Label(conflit);
                lbl.setStyle(
                    "-fx-font-size:12;-fx-text-fill:" + Design.DANGER + ";" +
                    "-fx-padding:8 12;-fx-background-color:#fdecea;" +
                    "-fx-border-radius:6;-fx-background-radius:6;"
                );
                lbl.setWrapText(true);
                listeConflits.getChildren().add(lbl);
            }
            sectionConflits.getChildren().add(listeConflits);
            panel.getChildren().add(sectionConflits);
        }

        // ── État des salles ───────────────────────────────────────────────
        VBox sectionSalles = Design.section("🚪  État des Salles (" + salles.size() + " salles)");

        // Légende
        HBox legende = new HBox(20);
        legende.setAlignment(Pos.CENTER_LEFT);
        legende.setPadding(new Insets(0, 0, 8, 0));
        for (String[] lg : new String[][]{
            {"🟢  Faible (0–40%)", Design.SUCCESS},
            {"🟡  Moyen (40–75%)", Design.WARNING},
            {"🔴  Élevé (>75%)",   Design.DANGER}
        }) {
            Label ll = new Label(lg[0]);
            ll.setStyle("-fx-font-size:11;-fx-text-fill:" + lg[1] + ";-fx-font-weight:bold;");
            legende.getChildren().add(ll);
        }
        sectionSalles.getChildren().add(legende);

        // Liste des salles dans un scroll interne
        VBox listeSalles = new VBox(5);
        listeSalles.setPadding(new Insets(4));
        for (Salle s : salles) {
            long nbCoursSalle = cours.stream().filter(c -> c.getSalleId() == s.getId()).count();
            double taux = Math.min(100, nbCoursSalle == 0 ? 0 : Math.min(100.0, nbCoursSalle * (100.0 / 12.0)));
            String couleur = taux > 75 ? Design.DANGER : taux > 40 ? Design.WARNING : Design.SUCCESS;

            HBox ligne = new HBox(12);
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.setPadding(new Insets(7, 14, 7, 14));
            ligne.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 7;" +
                "-fx-border-color: " + Design.BORDER + ";" +
                "-fx-border-radius: 7;" +
                "-fx-border-width: 1;"
            );

            Label nomSalle = new Label(s.getNumero() + "  –  " + s.getBatiment());
            nomSalle.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.TEXT_DARK + ";-fx-min-width:200;");

            Label typeLbl = new Label(s.getType());
            typeLbl.setStyle(
                "-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:white;" +
                "-fx-padding:2 7;-fx-background-radius:10;" +
                "-fx-background-color:" + Design.NEUTRAL + ";-fx-min-width:42;-fx-alignment:center;"
            );

            Label capLbl = new Label("👥  " + s.getCapacite());
            capLbl.setStyle("-fx-font-size:11;-fx-text-fill:" + Design.TEXT_MUTED + ";-fx-min-width:52;");

            ProgressBar pb = new ProgressBar(taux / 100.0);
            pb.setPrefWidth(160);
            pb.setStyle("-fx-accent: " + couleur + ";");

            Label pct = new Label(String.format("%.0f%%", taux));
            pct.setStyle("-fx-font-size:11;-fx-text-fill:" + couleur + ";-fx-font-weight:bold;-fx-min-width:36;");

            Label equip = new Label(s.getEquipementsStr());
            equip.setStyle("-fx-font-size:11;-fx-text-fill:" + Design.TEXT_MUTED + ";");

            ligne.getChildren().addAll(nomSalle, typeLbl, capLbl, pb, pct, equip);
            listeSalles.getChildren().add(ligne);
        }

        ScrollPane scrollSalles = new ScrollPane(listeSalles);
        scrollSalles.setFitToWidth(true);
        scrollSalles.setPrefHeight(Math.min(300, salles.size() * 44 + 12));
        scrollSalles.setMaxHeight(340);
        scrollSalles.setStyle("-fx-background-color: transparent; -fx-border-color: " + Design.BORDER + "; -fx-border-radius: 6;");
        sectionSalles.getChildren().add(scrollSalles);
        panel.getChildren().add(sectionSalles);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }
}
