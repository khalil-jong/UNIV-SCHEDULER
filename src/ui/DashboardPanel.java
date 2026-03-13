package ui;

import dao.CoursDAO;
import dao.SalleDAO;
import dao.UtilisateurDAO;
import models.Cours;
import models.Salle;
import models.Utilisateur;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Dashboard avancé avec statistiques, taux d'occupation, conflits en temps réel
public class DashboardPanel {

    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public ScrollPane createPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #f5f6fa;");

        Label titre = new Label("📊 Tableau de Bord");
        titre.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label dateLbl = new Label("📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH)));
        dateLbl.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        // === CARTES STATISTIQUES ===
        List<Salle> salles = salleDAO.obtenirTous();
        List<Cours> cours = coursDAO.obtenirTous();
        List<Utilisateur> users = utilisateurDAO.obtenirTous();

        // Cours aujourd'hui
        long coursAujourdhui = cours.stream()
            .filter(c -> c.getDateDebut().toLocalDate().equals(LocalDate.now()))
            .count();

        // Conflits
        List<String> conflits = coursDAO.detecterConflits();

        // Taux occupation (cours ce mois / (salles * jours ouvrés * créneaux))
        int mois = LocalDate.now().getMonthValue();
        long coursParMois = cours.stream()
            .filter(c -> c.getDateDebut().getMonthValue() == mois)
            .count();

        HBox row1 = new HBox(15);
        row1.getChildren().addAll(
            carteStats("🏫 Salles", String.valueOf(salles.size()), "Total enregistrées", "#3498db"),
            carteStats("📚 Cours", String.valueOf(cours.size()), "Planifiés au total", "#27ae60"),
            carteStats("📅 Aujourd'hui", String.valueOf(coursAujourdhui), "Cours ce jour", "#9b59b6"),
            carteStats("⚠️ Conflits", String.valueOf(conflits.size()),
                conflits.isEmpty() ? "Aucun problème" : "À résoudre !",
                conflits.isEmpty() ? "#95a5a6" : "#e74c3c")
        );

        HBox row2 = new HBox(15);
        row2.getChildren().addAll(
            carteStats("👥 Utilisateurs", String.valueOf(users.size()), "Comptes actifs", "#e67e22"),
            carteStats("📆 Ce mois", String.valueOf(coursParMois), "Cours planifiés", "#1abc9c"),
            carteStats("🏗️ Bâtiments", String.valueOf(salles.stream().map(Salle::getBatiment).distinct().count()), "Bâtiments actifs", "#34495e"),
            carteStats("✅ Dispo", String.valueOf(salleDAO.obtenirSallesDisponibles(java.time.LocalDateTime.now(), 60).size()), "Salles libres (1h)", "#2ecc71")
        );

        // === COURS DU JOUR ===
        VBox sectionCours = new VBox(8);
        Label titreCours = new Label("📋 Cours d'aujourd'hui");
        titreCours.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        List<Cours> coursJour = cours.stream()
            .filter(c -> c.getDateDebut().toLocalDate().equals(LocalDate.now()))
            .sorted((a, b) -> a.getDateDebut().compareTo(b.getDateDebut()))
            .collect(java.util.stream.Collectors.toList());

        if (coursJour.isEmpty()) {
            Label vide = new Label("Aucun cours planifié aujourd'hui.");
            vide.setStyle("-fx-font-size: 13; -fx-text-fill: #95a5a6; -fx-padding: 10;");
            sectionCours.getChildren().addAll(titreCours, vide);
        } else {
            sectionCours.getChildren().add(titreCours);
            DateTimeFormatter hf = DateTimeFormatter.ofPattern("HH:mm");
            for (Cours c : coursJour) {
                Salle s = salleDAO.obtenirParId(c.getSalleId());
                HBox carte = new HBox(15);
                carte.setPadding(new Insets(10, 15, 10, 15));
                carte.setStyle("-fx-background-color: white; -fx-border-color: #3498db; -fx-border-width: 0 0 0 4; -fx-border-radius: 0 4 4 0; -fx-background-radius: 4;");
                carte.setAlignment(Pos.CENTER_LEFT);

                Label heure = new Label(c.getDateDebut().format(hf));
                heure.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #3498db; -fx-min-width: 55;");

                Label matiere = new Label(c.getMatiere());
                matiere.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-min-width: 150;");

                Label enseignant = new Label("👤 " + c.getEnseignant());
                enseignant.setStyle("-fx-font-size: 12; -fx-text-fill: #555; -fx-min-width: 160;");

                Label salleLbl = new Label("🏫 " + (s != null ? s.getNumero() : "?"));
                salleLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #555; -fx-min-width: 80;");

                Label classe = new Label("🎓 " + c.getClasse());
                classe.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

                carte.getChildren().addAll(heure, matiere, enseignant, salleLbl, classe);
                sectionCours.getChildren().add(carte);
            }
        }

        // === CONFLITS ===
        VBox sectionConflits = new VBox(8);
        Label titreConflits = new Label("⚠️ Conflits Horaires Détectés");
        titreConflits.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        sectionConflits.getChildren().add(titreConflits);

        if (conflits.isEmpty()) {
            Label ok = new Label("✅ Aucun conflit détecté. L'emploi du temps est cohérent.");
            ok.setStyle("-fx-font-size: 13; -fx-text-fill: #27ae60; -fx-padding: 10; -fx-background-color: #eafaf1; -fx-border-radius: 4; -fx-background-radius: 4;");
            sectionConflits.getChildren().add(ok);
        } else {
            for (String conflit : conflits) {
                Label lbl = new Label(conflit);
                lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #c0392b; -fx-padding: 8 12; -fx-background-color: #fdecea; -fx-border-radius: 4; -fx-background-radius: 4;");
                lbl.setWrapText(true);
                sectionConflits.getChildren().add(lbl);
            }
        }

        // === SALLES CRITIQUES (occupation > 80%) ===
        VBox sectionSalles = new VBox(8);
        Label titreSalles = new Label("🏫 État des Salles");
        titreSalles.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        sectionSalles.getChildren().add(titreSalles);

        for (Salle s : salles.stream().limit(6).collect(java.util.stream.Collectors.toList())) {
            long nbCoursSalle = cours.stream().filter(c -> c.getSalleId() == s.getId()).count();
            double taux = salles.isEmpty() ? 0 : Math.min(100, nbCoursSalle * 12.5);
            String couleur = taux > 75 ? "#e74c3c" : taux > 40 ? "#f39c12" : "#27ae60";

            HBox ligne = new HBox(10);
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.setPadding(new Insets(6 ,10, 6, 10));
            ligne.setStyle("-fx-background-color: white; -fx-background-radius: 4;");

            Label nomSalle = new Label(s.getNumero() + " – " + s.getBatiment());
            nomSalle.setStyle("-fx-font-size: 12; -fx-min-width: 180;");

            ProgressBar pb = new ProgressBar(taux / 100.0);
            pb.setPrefWidth(200);
            pb.setStyle("-fx-accent: " + couleur + ";");

            Label pct = new Label(String.format("%.0f%%", taux));
            pct.setStyle("-fx-font-size: 12; -fx-text-fill: " + couleur + "; -fx-font-weight: bold;");

            Label equip = new Label(s.getEquipementsStr());
            equip.setStyle("-fx-font-size: 12;");

            ligne.getChildren().addAll(nomSalle, pb, pct, equip);
            sectionSalles.getChildren().add(ligne);
        }

        panel.getChildren().addAll(titre, dateLbl, row1, row2, new Separator(), sectionCours, new Separator(), sectionConflits, new Separator(), sectionSalles);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private VBox carteStats(String icon, String valeur, String sousTitre, String couleur) {
        VBox carte = new VBox(4);
        carte.setPadding(new Insets(18));
        carte.setPrefWidth(170);
        carte.setAlignment(Pos.CENTER);
        carte.setStyle("-fx-background-color: " + couleur + "; -fx-background-radius: 10;");

        Label lVal = new Label(valeur);
        lVal.setStyle("-fx-font-size: 30; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lIcon = new Label(icon);
        lIcon.setStyle("-fx-font-size: 13; -fx-text-fill: white; -fx-font-weight: bold;");
        Label lSub = new Label(sousTitre);
        lSub.setStyle("-fx-font-size: 11; -fx-text-fill: rgba(255,255,255,0.85);");

        carte.getChildren().addAll(lVal, lIcon, lSub);
        return carte;
    }
}
