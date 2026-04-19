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
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Cours;
import models.Salle;
import models.Utilisateur;

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

        // Bouton d'actualisation manuelle — recharge tout le panel
        Button btnActualiser = new Button("🔄 Actualiser les statistiques");
        btnActualiser.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 7 16; -fx-font-weight: bold;");
        Label lblMaj = new Label("Les données sont recalculées à chaque ouverture du tableau de bord.");
        lblMaj.setStyle("-fx-font-size: 11; -fx-text-fill: #95a5a6;");
        HBox barActu = new HBox(12, btnActualiser, lblMaj);
        barActu.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        // Le clic recrée le panel entier (pattern identique au menu)
        btnActualiser.setOnAction(e -> {
            ScrollPane newPanel = createPanel();
            // Remplacer le contenu dans le ScrollPane parent
            javafx.scene.Parent parent = btnActualiser.getParent();
            int depth = 0;
            while (parent != null && !(parent instanceof ScrollPane) && depth < 10) {
                parent = parent.getParent(); depth++;
            }
            if (parent instanceof ScrollPane) {
                ((ScrollPane) parent).setContent(newPanel.getContent());
            }
        });

        // === CARTES STATISTIQUES ===
        List<Salle> salles = salleDAO.obtenirTous();
        // Fusion cours ponctuels + créneaux EDT (4 semaines) → compteurs exacts
        List<Cours> cours = coursDAO.obtenirTousAvecEDT();
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
            carteStats("📚 Cours", String.valueOf(cours.size()), "Créneaux EDT actifs", "#27ae60"),
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
        Label titreCours = new Label("📋 Créneaux EDT d'aujourd'hui");
        titreCours.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        List<Cours> coursJour = cours.stream()
            .filter(c -> c.getDateDebut().toLocalDate().equals(LocalDate.now()))
            .sorted((a, b) -> a.getDateDebut().compareTo(b.getDateDebut()))
            .collect(java.util.stream.Collectors.toList());

        if (coursJour.isEmpty()) {
            Label vide = new Label("Aucun créneau EDT planifié aujourd'hui.");
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

        // === ÉTAT DE TOUTES LES SALLES (avec scroll) ===
        VBox sectionSalles = new VBox(8);
        Label titreSalles = new Label("🏫 État des Salles (" + salles.size() + " salles)");
        titreSalles.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        sectionSalles.getChildren().add(titreSalles);

        // Légende couleurs
        HBox legendeSalles = new HBox(16);
        legendeSalles.setAlignment(Pos.CENTER_LEFT);
        for (String[] lg : new String[][]{{"🟢 Faible (0-40%)", "#27ae60"}, {"🟡 Moyen (40-75%)", "#f39c12"}, {"🔴 Élevé (>75%)", "#e74c3c"}}) {
            Label ll = new Label(lg[0]);
            ll.setStyle("-fx-font-size: 11; -fx-text-fill: " + lg[1] + "; -fx-font-weight: bold;");
            legendeSalles.getChildren().add(ll);
        }
        sectionSalles.getChildren().add(legendeSalles);

        // Conteneur scrollable pour toutes les salles
        VBox listeSalles = new VBox(4);
        listeSalles.setPadding(new Insets(4));
        for (Salle s : salles) {
            long nbCoursSalle = cours.stream().filter(c -> c.getSalleId() == s.getId()).count();
            // Calcul du taux : chaque créneau EDT représente ~8% de la capacité hebdomadaire (12 créneaux max par semaine)
            double taux = Math.min(100, nbCoursSalle == 0 ? 0 : Math.min(100.0, nbCoursSalle * (100.0 / 12.0)));
            String couleur = taux > 75 ? "#e74c3c" : taux > 40 ? "#f39c12" : "#27ae60";

            HBox ligne = new HBox(10);
            ligne.setAlignment(Pos.CENTER_LEFT);
            ligne.setPadding(new Insets(5, 10, 5, 10));
            ligne.setStyle("-fx-background-color: white; -fx-background-radius: 4; -fx-border-color: #ecf0f1; -fx-border-width: 1;");

            Label nomSalle = new Label(s.getNumero() + " – " + s.getBatiment());
            nomSalle.setStyle("-fx-font-size: 12; -fx-min-width: 190;");

            Label typeLbl = new Label("[" + s.getType() + "]");
            typeLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d; -fx-min-width: 50;");

            Label capLbl = new Label("👥 " + s.getCapacite());
            capLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #555; -fx-min-width: 55;");

            ProgressBar pb = new ProgressBar(taux / 100.0);
            pb.setPrefWidth(180);
            pb.setStyle("-fx-accent: " + couleur + ";");

            Label pct = new Label(String.format("%.0f%%", taux));
            pct.setStyle("-fx-font-size: 12; -fx-text-fill: " + couleur + "; -fx-font-weight: bold; -fx-min-width: 42;");

            Label equip = new Label(s.getEquipementsStr());
            equip.setStyle("-fx-font-size: 12;");

            ligne.getChildren().addAll(nomSalle, typeLbl, capLbl, pb, pct, equip);
            listeSalles.getChildren().add(ligne);
        }

        // ScrollPane dédié à la liste des salles (max 280px de haut)
        ScrollPane scrollSalles = new ScrollPane(listeSalles);
        scrollSalles.setFitToWidth(true);
        scrollSalles.setPrefHeight(Math.min(280, salles.size() * 42 + 10));
        scrollSalles.setMaxHeight(320);
        scrollSalles.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4;");
        sectionSalles.getChildren().add(scrollSalles);

        panel.getChildren().addAll(titre, dateLbl, barActu, row1, row2, new Separator(), sectionCours, new Separator(), sectionConflits, new Separator(), sectionSalles);

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
