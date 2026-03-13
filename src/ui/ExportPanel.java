package ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import dao.CoursDAO;
import dao.SalleDAO;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Cours;
import models.Salle;

public class ExportPanel {

    private CoursDAO coursDAO = new CoursDAO();
    private SalleDAO salleDAO = new SalleDAO();
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DateTimeFormatter fmtDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public ScrollPane createPanel() {
        VBox panel = new VBox(25);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: #f5f6fa;");

        Label titre = new Label("📤 Export & Rapports");
        titre.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ─── FILTRE CLASSE ───
        VBox boxFiltre = section("🎓 Filtre de données");
        TextField tfClasse = new TextField();
        tfClasse.setPromptText("Laisser vide = tout exporter  |  ou saisir une classe : ex. L2-Informatique");
        tfClasse.setPrefWidth(420);
        Label infoFiltre = new Label("Ce filtre s'applique à tous les exports ci-dessous.");
        infoFiltre.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");
        boxFiltre.getChildren().addAll(tfClasse, infoFiltre);

        // ─── EXPORT PDF ───
        VBox boxPDF = section("🖨️ Export PDF (imprimable)");
        Label descPDF = new Label("Génère un fichier PDF professionnel de l'emploi du temps, prêt à imprimer.");
        descPDF.setStyle("-fx-font-size: 12; -fx-text-fill: #555;"); descPDF.setWrapText(true);

        Button btnPDF = new Button("📄 Exporter en PDF");
        btnPDF.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");
        Label msgPDF = new Label(""); msgPDF.setStyle("-fx-font-size: 12;"); msgPDF.setWrapText(true);

        btnPDF.setOnAction(e -> exporterPDF(tfClasse.getText().trim(), msgPDF));
        boxPDF.getChildren().addAll(descPDF, btnPDF, msgPDF);

        // ─── EXPORT CSV / HTML ───
        VBox boxExcel = section("📊 Export CSV (Excel) et HTML");
        Label descExcel = new Label("CSV : ouvert directement dans Excel. HTML : visualisable dans un navigateur.");
        descExcel.setStyle("-fx-font-size: 12; -fx-text-fill: #555;"); descExcel.setWrapText(true);

        Button btnCSV = new Button("📊 Exporter en CSV (Excel)");
        btnCSV.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 18; -fx-font-weight: bold;");
        Button btnHTML = new Button("🌐 Exporter en HTML");
        btnHTML.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 18; -fx-font-weight: bold;");
        HBox boutonsExcel = new HBox(12, btnCSV, btnHTML);
        Label msgExcel = new Label(""); msgExcel.setStyle("-fx-font-size: 12;"); msgExcel.setWrapText(true);

        btnCSV.setOnAction(e -> exporterCSV(tfClasse.getText().trim(), msgExcel));
        btnHTML.setOnAction(e -> exporterHTML(tfClasse.getText().trim(), msgExcel));
        boxExcel.getChildren().addAll(descExcel, boutonsExcel, msgExcel);

        // ─── RAPPORT D'UTILISATION ───
        VBox boxRapport = section("📈 Rapport d'Utilisation des Salles");
        Label descRap = new Label("Rapport détaillé sur l'occupation des salles avec statistiques.");
        descRap.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");

        ComboBox<String> cbPeriode = new ComboBox<>();
        cbPeriode.getItems().addAll("Cette semaine", "Ce mois", "Tout"); cbPeriode.setValue("Ce mois");
        HBox periodBox = new HBox(10, new Label("Période :"), cbPeriode);
        periodBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button btnRapport = new Button("📋 Générer le rapport");
        btnRapport.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 10 18; -fx-font-weight: bold;");

        TextArea taRapport = new TextArea(); taRapport.setPrefHeight(260);
        taRapport.setEditable(false); taRapport.setStyle("-fx-font-family: monospace; -fx-font-size: 11;");

        Button btnSauvRapport = new Button("💾 Sauvegarder (.txt)");
        btnSauvRapport.setStyle("-fx-padding: 8 16;");
        Label msgRapport = new Label(""); msgRapport.setStyle("-fx-font-size: 12;");

        btnRapport.setOnAction(e -> taRapport.setText(genererRapport(cbPeriode.getValue())));
        btnSauvRapport.setOnAction(e -> {
            if (taRapport.getText().isEmpty()) { new Alert(Alert.AlertType.WARNING, "Générez d'abord un rapport.", ButtonType.OK).showAndWait(); return; }
            sauvegarderTexte(taRapport.getText(), "rapport_" + LocalDate.now().format(fmtDate) + ".txt", msgRapport);
        });
        boxRapport.getChildren().addAll(descRap, periodBox, btnRapport, taRapport, new HBox(10, btnSauvRapport, msgRapport));

        panel.getChildren().addAll(titre, boxFiltre, boxPDF, boxExcel, new Separator(), boxRapport);
        ScrollPane scroll = new ScrollPane(panel); scroll.setFitToWidth(true);
        return scroll;
    }

    // ══════════════════════════════════════════════════════════
    //  EXPORT PDF  (HTML → fichier .html nommé .pdf pour demo)
    //  Pour un VRAI PDF en Java, ajouter iText7 dans lib/
    //  Ici on génère un HTML stylé prêt pour impression (Ctrl+P → Enregistrer en PDF)
    // ══════════════════════════════════════════════════════════
    private void exporterPDF(String classe, Label msg) {
        List<Cours> liste = classe.isEmpty() ? coursDAO.obtenirTous() : coursDAO.obtenirParClasse(classe);

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer le PDF");
        fc.setInitialFileName("emploi_du_temps_" + LocalDate.now().format(fmtDate) + ".html");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Page HTML pour impression (*.html)", "*.html"));
        File file = fc.showSaveDialog(new Stage());
        if (file == null) {
			return;
		}

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            pw.println("<title>Emploi du Temps</title>");
            pw.println("<style>");
            pw.println("@media print { @page { size: A4 landscape; margin: 15mm; } .no-print{display:none;} }");
            pw.println("body{font-family:'Segoe UI',Arial,sans-serif;margin:0;padding:20px;background:white;}");
            pw.println(".header{background:#2c3e50;color:white;padding:20px;border-radius:8px;margin-bottom:20px;display:flex;justify-content:space-between;align-items:center;}");
            pw.println(".header h1{margin:0;font-size:22px;} .header p{margin:0;font-size:13px;opacity:0.8;}");
            pw.println("table{width:100%;border-collapse:collapse;font-size:12px;}");
            pw.println("th{background:#3498db;color:white;padding:10px 8px;text-align:left;}");
            pw.println("td{padding:8px;border-bottom:1px solid #ecf0f1;}");
            pw.println("tr:nth-child(even){background:#f8f9fa;}");
            pw.println(".badge{display:inline-block;padding:2px 8px;border-radius:10px;font-size:10px;font-weight:bold;}");
            pw.println(".td-badge{background:#d6eaf8;color:#1a5276;} .tp-badge{background:#d5f5e3;color:#1a5c35;} .amphi-badge{background:#fdecea;color:#922b21;}");
            pw.println(".footer{margin-top:20px;font-size:10px;color:#95a5a6;text-align:center;}");
            pw.println(".print-btn{background:#e74c3c;color:white;border:none;padding:10px 20px;border-radius:5px;cursor:pointer;font-size:13px;margin-bottom:15px;}");
            pw.println("</style></head><body>");

            // Header
            String titreDoc = classe.isEmpty() ? "Emploi du Temps Complet" : "Emploi du Temps — " + classe;
            pw.println("<div class='header'>");
            pw.println("<div><h1>📅 " + titreDoc + "</h1><p>UNIV-SCHEDULER — Gestion des Salles et Emplois du Temps</p></div>");
            pw.println("<div><p>Généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</p><p>" + liste.size() + " cours</p></div>");
            pw.println("</div>");

            // Bouton impression
            pw.println("<button class='print-btn no-print' onclick='window.print()'>🖨️ Imprimer / Enregistrer en PDF</button>");

            // Table
            pw.println("<table><tr><th>Matière</th><th>Enseignant</th><th>Classe</th><th>Groupe</th><th>Date & Heure</th><th>Durée</th><th>Fin</th><th>Salle</th><th>Type</th></tr>");
            for (Cours c : liste) {
                Salle s = salleDAO.obtenirParId(c.getSalleId());
                String typeBadge = s != null ? (s.getType().equals("TD") ? "td-badge" : s.getType().equals("TP") ? "tp-badge" : "amphi-badge") : "";
                String nomSalle = s != null ? s.getNumero() : "?";
                String typeStr = s != null ? s.getType() : "?";
                pw.printf("<tr><td><strong>%s</strong></td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%d min</td><td>%s</td><td>%s</td><td><span class='badge %s'>%s</span></td></tr>%n",
                    c.getMatiere(), c.getEnseignant(), c.getClasse(), c.getGroupe(),
                    c.getDateDebut().format(fmt), c.getDuree(),
                    c.getDateFin().format(DateTimeFormatter.ofPattern("HH:mm")),
                    nomSalle, typeBadge, typeStr);
            }
            pw.println("</table>");
            pw.println("<div class='footer'>UNIV-SCHEDULER — Document généré automatiquement — " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</div>");
            pw.println("</body></html>");

            msg.setText("✅ Fichier généré : " + file.getName() + "\n💡 Ouvrez-le dans votre navigateur, puis faites Ctrl+P → 'Enregistrer en PDF' pour obtenir un vrai PDF.");
            msg.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
        } catch (IOException ex) {
            msg.setText("❌ Erreur : " + ex.getMessage());
            msg.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private void exporterCSV(String classe, Label msg) {
        List<Cours> liste = classe.isEmpty() ? coursDAO.obtenirTous() : coursDAO.obtenirParClasse(classe);
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer en CSV");
        fc.setInitialFileName("emploi_du_temps_" + LocalDate.now().format(fmtDate) + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier CSV", "*.csv"));
        File file = fc.showSaveDialog(new Stage());
        if (file == null) {
			return;
		}
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            pw.println("\uFEFF" + "ID,Matière,Enseignant,Classe,Groupe,Date/Heure,Durée(min),Heure fin,Salle,Type salle,Bâtiment");
            for (Cours c : liste) {
                Salle s = salleDAO.obtenirParId(c.getSalleId());
                pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    c.getId(), c.getMatiere(), c.getEnseignant(), c.getClasse(), c.getGroupe(),
                    c.getDateDebut().format(fmt), c.getDuree(),
                    c.getDateFin().format(DateTimeFormatter.ofPattern("HH:mm")),
                    s != null ? s.getNumero() : "?", s != null ? s.getType() : "?", s != null ? s.getBatiment() : "?");
            }
            msg.setText("✅ CSV exporté : " + file.getName() + " (" + liste.size() + " cours) — Ouvert directement dans Excel.");
            msg.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
        } catch (IOException ex) {
            msg.setText("❌ Erreur : " + ex.getMessage()); msg.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private void exporterHTML(String classe, Label msg) {
        List<Cours> liste = classe.isEmpty() ? coursDAO.obtenirTous() : coursDAO.obtenirParClasse(classe);
        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer en HTML");
        fc.setInitialFileName("emploi_du_temps_" + LocalDate.now().format(fmtDate) + ".html");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier HTML", "*.html"));
        File file = fc.showSaveDialog(new Stage());
        if (file == null) {
			return;
		}
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Emploi du Temps</title>");
            pw.println("<style>body{font-family:Arial;margin:20px;} h1{color:#2c3e50;} table{border-collapse:collapse;width:100%;} th{background:#3498db;color:white;padding:10px;} td{padding:8px;border:1px solid #ddd;} tr:nth-child(even){background:#f5f6fa;}</style>");
            pw.println("</head><body><h1>Emploi du Temps</h1><p>Généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</p>");
            pw.println("<table><tr><th>Matière</th><th>Enseignant</th><th>Classe</th><th>Date/Heure</th><th>Durée</th><th>Salle</th></tr>");
            for (Cours c : liste) {
                Salle s = salleDAO.obtenirParId(c.getSalleId());
                pw.printf("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%d min</td><td>%s</td></tr>%n",
                    c.getMatiere(), c.getEnseignant(), c.getClasse(), c.getDateDebut().format(fmt), c.getDuree(), s != null ? s.getNumero() : "?");
            }
            pw.println("</table></body></html>");
            msg.setText("✅ HTML exporté : " + file.getName());
            msg.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
        } catch (IOException ex) {
            msg.setText("❌ Erreur : " + ex.getMessage()); msg.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private String genererRapport(String periode) {
        List<Cours> tousLesCours = coursDAO.obtenirTous();
        List<Salle> salles = salleDAO.obtenirTous();
        LocalDate now = LocalDate.now();
        List<Cours> filtered; String labelPeriode;

        if (periode.equals("Cette semaine")) {
            LocalDate lundi = now.with(java.time.DayOfWeek.MONDAY);
            LocalDate dimanche = lundi.plusDays(6);
            filtered = tousLesCours.stream()
                .filter(c -> !c.getDateDebut().toLocalDate().isBefore(lundi) && !c.getDateDebut().toLocalDate().isAfter(dimanche))
                .collect(Collectors.toList());
            labelPeriode = "Semaine du " + lundi.format(DateTimeFormatter.ofPattern("dd/MM")) + " au " + dimanche.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else if (periode.equals("Ce mois")) {
            filtered = tousLesCours.stream()
                .filter(c -> c.getDateDebut().getMonthValue() == now.getMonthValue() && c.getDateDebut().getYear() == now.getYear())
                .collect(Collectors.toList());
            labelPeriode = "Mois de " + now.format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.FRENCH));
        } else { filtered = tousLesCours; labelPeriode = "Toute la période"; }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("         RAPPORT D'UTILISATION — UNIV-SCHEDULER\n");
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append(" Période   : ").append(labelPeriode).append("\n");
        sb.append(" Généré le : ").append(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        sb.append("───────────────────────────────────────────────────────────\n\n");
        sb.append("RÉSUMÉ\n");
        sb.append("  Cours planifiés   : ").append(filtered.size()).append("\n");
        sb.append("  Salles actives    : ").append(salles.size()).append("\n");
        sb.append("  Conflits détectés : ").append(coursDAO.detecterConflits().size()).append("\n\n");
        sb.append("UTILISATION PAR SALLE\n");
        sb.append(String.format("  %-12s %-20s %8s %10s %12s\n", "Numéro", "Bâtiment", "Cours", "Heures", "Équipements"));
        sb.append("  " + "─".repeat(66) + "\n");
        for (Salle s : salles) {
            List<Cours> cs = filtered.stream().filter(c -> c.getSalleId() == s.getId()).collect(Collectors.toList());
            int totalMin = cs.stream().mapToInt(Cours::getDuree).sum();
            sb.append(String.format("  %-12s %-20s %8d %9.1fh  %s\n", s.getNumero(), s.getBatiment(), cs.size(), totalMin / 60.0, s.getEquipementsStr()));
        }
        sb.append("\nDÉTAIL DES COURS (").append(filtered.size()).append(")\n");
        sb.append(String.format("  %-20s %-18s %-15s %-10s %s\n", "Matière", "Enseignant", "Classe", "Salle", "Date/Heure"));
        sb.append("  " + "─".repeat(80) + "\n");
        for (Cours c : filtered) {
            Salle s = salleDAO.obtenirParId(c.getSalleId());
            sb.append(String.format("  %-20s %-18s %-15s %-10s %s\n", c.getMatiere(), c.getEnseignant(), c.getClasse(), s != null ? s.getNumero() : "?", c.getDateDebut().format(fmt)));
        }
        sb.append("\n═══════════════════════════════════════════════════════════\n");
        return sb.toString();
    }

    private void sauvegarderTexte(String contenu, String nom, Label msg) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sauvegarder"); fc.setInitialFileName(nom);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier texte", "*.txt"));
        File file = fc.showSaveDialog(new Stage());
        if (file == null) {
			return;
		}
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) { pw.print(contenu); msg.setText("✅ Sauvegardé : " + file.getName()); msg.setStyle("-fx-text-fill: #27ae60;");
        } catch (IOException ex) { msg.setText("❌ " + ex.getMessage()); msg.setStyle("-fx-text-fill: #e74c3c;"); }
    }

    private VBox section(String titre) {
        VBox box = new VBox(10); box.setPadding(new Insets(15));
        box.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 6; -fx-background-color: white; -fx-background-radius: 6;");
        Label lbl = new Label(titre); lbl.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        box.getChildren().add(lbl); return box;
    }
}
