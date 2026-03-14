package ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.ClasseDAO;
import dao.EmploiDuTempsDAO;
import dao.SalleDAO;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.EmploiDuTemps;
import models.Salle;

/**
 * Export des emplois du temps (PDF-HTML, CSV, TXT) — une classe à la fois.
 */
public class ExportEdtPanel {

    private EmploiDuTempsDAO edtDAO   = new EmploiDuTempsDAO();
    private ClasseDAO        classeDAO = new ClasseDAO();
    private SalleDAO         salleDAO  = new SalleDAO();
    private static final String[] JOURS = {"","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"};

    public ScrollPane createPanel() {
        VBox panel = new VBox(22);
        panel.setPadding(new Insets(25));

        Label titre = new Label("📤 Export des Emplois du Temps");
        titre.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Sélection de classe
        VBox boxClasse = section("🎓 Classe à exporter");
        ComboBox<String> cbClasse = new ComboBox<>();
        cbClasse.getItems().addAll(classeDAO.obtenirNomsClasses());
        cbClasse.setPromptText("Choisir une classe...");
        cbClasse.setPrefWidth(260);
        Label lblNbCreneaux = new Label("");
        lblNbCreneaux.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");
        cbClasse.setOnAction(e -> {
            if (cbClasse.getValue() != null) {
                int n = edtDAO.obtenirParClasse(cbClasse.getValue()).size();
                lblNbCreneaux.setText(n + " créneau(x) dans l'emploi du temps.");
            }
        });
        boxClasse.getChildren().addAll(cbClasse, lblNbCreneaux);

        // Export HTML/PDF
        VBox boxPDF = section("🖨️ Export PDF (via navigateur)");
        Label descPDF = new Label("Génère un fichier HTML mis en forme (tableau identique à l'affichage). Ouvrez-le dans un navigateur puis faites Ctrl+P → Enregistrer en PDF.");
        descPDF.setWrapText(true); descPDF.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
        Button btnPDF = new Button("📄 Exporter en HTML/PDF");
        btnPDF.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");
        Label msgPDF = new Label(""); msgPDF.setStyle("-fx-font-size: 12;"); msgPDF.setWrapText(true);
        btnPDF.setOnAction(e -> exporterHTML(cbClasse.getValue(), msgPDF));
        boxPDF.getChildren().addAll(descPDF, btnPDF, msgPDF);

        // Export CSV
        VBox boxCSV = section("📊 Export CSV (Excel)");
        Label descCSV = new Label("Fichier .csv compatible Excel. Encodage UTF-8 avec BOM.");
        descCSV.setStyle("-fx-font-size: 12; -fx-text-fill: #555;");
        Button btnCSV = new Button("📊 Exporter en CSV");
        btnCSV.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-weight: bold;");
        Label msgCSV = new Label(""); msgCSV.setStyle("-fx-font-size: 12;"); msgCSV.setWrapText(true);
        btnCSV.setOnAction(e -> exporterCSV(cbClasse.getValue(), msgCSV));
        boxCSV.getChildren().addAll(descCSV, btnCSV, msgCSV);

        panel.getChildren().addAll(titre, boxClasse, boxPDF, boxCSV);
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        return scroll;
    }

    // ── Export HTML ───────────────────────────────────────────────
    private void exporterHTML(String classe, Label msg) {
        if (classe == null) { msg.setText("⚠️ Sélectionnez une classe."); return; }
        List<EmploiDuTemps> data = edtDAO.obtenirParClasse(classe);
        if (data.isEmpty()) { msg.setText("⚠️ Aucun créneau pour cette classe."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer l'emploi du temps");
        fc.setInitialFileName("EDT_" + classe.replace(" ","_") + ".html");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML","*.html"));
        File f = fc.showSaveDialog(new Stage());
        if (f == null) {
			return;
		}

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            pw.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            pw.println("<title>EDT " + classe + "</title>");
            pw.println("<style>body{font-family:Arial,sans-serif;padding:20px;}");
            pw.println("h1{color:#2c3e50;font-size:20px;}");
            pw.println("table{border-collapse:collapse;width:100%;}");
            pw.println("th{background:#4D4D4D;color:white;padding:8px;font-size:12px;border:1px solid #999;}");
            pw.println("td{border:1px solid #ccc;padding:6px;vertical-align:top;min-width:100px;font-size:11px;}");
            pw.println(".cours{background:#FFF9E6;padding:5px;}");
            pw.println(".salle{color:#CC2200;font-weight:bold;}");
            pw.println(".pause{background:#E0E0E0;text-align:center;color:#555;font-weight:bold;}");
            pw.println("@media print{body{padding:0;}}</style></head><body>");
            pw.println("<h1>Emploi du Temps — Classe " + classe + "</h1>");
            pw.println("<p style='color:#7f8c8d;font-size:11px;'>Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) + "</p>");
            pw.println("<table><tr><th>Heures</th>");
            for (String j : JOURS) {
				if (!j.isEmpty()) {
					pw.println("<th>" + j + "</th>");
				}
			}
            pw.println("</tr>");

            // Grille 08h → 19h
            boolean[][] drawn = new boolean[6][12];
            for (int h = 8; h <= 19; h++) {
                if (h == 13 || h == 14) {
                    if (h == 13) {
						pw.println("<tr><td>" + h + "h-" + (h+1) + "h</td><td class='pause' colspan='6'>Pause méridienne (13h-15h)</td></tr>");
					} else {
						pw.println("<tr><td>" + h + "h-" + (h+1) + "h</td><td colspan='6'></td></tr>");
					}
                    continue;
                }
                pw.print("<tr><td><b>" + String.format("%02dh-%02dh", h, h+1) + "</b></td>");
                for (int j = 1; j <= 6; j++) {
                    final int jj = j, hh = h;
                    if (drawn[j-1][h-8]) { continue; }
                    EmploiDuTemps edt = data.stream()
                        .filter(e -> e.getJourSemaine()==jj && e.getHeureDebut().getHour()==hh && e.getHeureDebut().getMinute()==0)
                        .findFirst().orElse(null);
                    if (edt != null) {
                        int rs = Math.max(1, edt.getDuree()/60);
                        for (int r=0;r<rs&&(h-8+r)<12;r++) {
							drawn[j-1][h-8+r]=true;
						}
                        Salle salle = salleDAO.obtenirParId(edt.getSalleId());
                        String nomSalle = salle != null ? salle.getNumero() : "?";
                        pw.print("<td rowspan='" + rs + "'><div class='cours'><b>" + edt.getMatiere() + "</b><br/>(" + edt.getEnseignant() + ")<br/><span class='salle'>" + nomSalle + "</span></div></td>");
                    } else {
                        pw.print("<td></td>");
                    }
                }
                pw.println("</tr>");
            }
            pw.println("</table></body></html>");
            msg.setText("✅ Fichier HTML enregistré : " + f.getAbsolutePath() + "\n→ Ouvrez dans un navigateur puis Ctrl+P pour obtenir le PDF.");
            msg.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
        } catch (Exception ex) {
            msg.setText("❌ Erreur : " + ex.getMessage());
            msg.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
        }
    }

    // ── Export CSV ────────────────────────────────────────────────
    private void exporterCSV(String classe, Label msg) {
        if (classe == null) { msg.setText("⚠️ Sélectionnez une classe."); return; }
        List<EmploiDuTemps> data = edtDAO.obtenirParClasse(classe);
        if (data.isEmpty()) { msg.setText("⚠️ Aucun créneau pour cette classe."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en CSV");
        fc.setInitialFileName("EDT_" + classe.replace(" ","_") + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV","*.csv"));
        File f = fc.showSaveDialog(new Stage());
        if (f == null) {
			return;
		}

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            pw.write('\uFEFF'); // BOM UTF-8 pour Excel
            pw.println("Classe;Jour;Heure début;Heure fin;Durée (min);Matière;Type;Enseignant;Salle;Bâtiment");
            for (EmploiDuTemps e : data) {
                Salle s = salleDAO.obtenirParId(e.getSalleId());
                pw.println(String.join(";",
                    e.getClasse(), e.getNomJour(),
                    e.getHeureDebut().toString(), e.getHeureFin().toString(),
                    String.valueOf(e.getDuree()), e.getMatiere(), e.getTypeCours(),
                    e.getEnseignant(),
                    s!=null?s.getNumero():"?",
                    s!=null?s.getBatiment():"?"));
            }
            msg.setText("✅ CSV exporté : " + f.getAbsolutePath());
            msg.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
        } catch (Exception ex) {
            msg.setText("❌ Erreur : " + ex.getMessage());
            msg.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
        }
    }

    private VBox section(String titreSection) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-border-color: #bdc3c7; -fx-border-radius: 6; -fx-background-color: white;");
        Label lbl = new Label(titreSection);
        lbl.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        box.getChildren().add(lbl);
        return box;
    }
}
