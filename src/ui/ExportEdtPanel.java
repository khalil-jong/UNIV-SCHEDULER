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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.EmploiDuTemps;
import models.Salle;

/**
 * Export des emplois du temps (HTML/PDF, CSV) — redesigné.
 * Logique d'export inchangée. Design harmonisé avec Design.java.
 */
public class ExportEdtPanel {

    private EmploiDuTempsDAO edtDAO    = new EmploiDuTempsDAO();
    private ClasseDAO        classeDAO = new ClasseDAO();
    private SalleDAO         salleDAO  = new SalleDAO();
    private static final String[] JOURS = {"", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};

    public ScrollPane createPanel() {
        VBox panel = new VBox(24);
        panel.setPadding(new Insets(28));
        panel.setStyle("-fx-background-color: " + Design.BG_LIGHT + ";");

        Label titre = Design.pageTitle("📤  Export des Emplois du Temps");
        Label desc  = Design.muted("Sélectionnez une classe puis choisissez le format d'export souhaité.");
        panel.getChildren().addAll(titre, desc);

        // ── Sélection de classe ───────────────────────────────────────
        VBox classeSection = Design.section("🎓  Classe à exporter");

        HBox selBox = new HBox(12);
        selBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cbClasse = new ComboBox<>();
        cbClasse.getItems().addAll(classeDAO.obtenirNomsClasses());
        cbClasse.setPromptText("Choisir une classe…");
        cbClasse.setPrefWidth(280);

        Label lblNbCreneaux = new Label("");
        lblNbCreneaux.setStyle(
            "-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + Design.GEST_ACCENT + ";" +
            "-fx-padding: 4 10; -fx-background-color: #e8faf5; -fx-background-radius: 6;"
        );

        cbClasse.setOnAction(e -> {
            if (cbClasse.getValue() != null) {
                int n = edtDAO.obtenirParClasse(cbClasse.getValue()).size();
                lblNbCreneaux.setText("→  " + n + " créneau(x) dans l'emploi du temps.");
            }
        });

        selBox.getChildren().addAll(cbClasse, lblNbCreneaux);
        classeSection.getChildren().add(selBox);
        panel.getChildren().add(classeSection);

        // ── Export HTML / PDF ─────────────────────────────────────────
        VBox pdfSection = Design.section("🖨️  Export PDF via navigateur");

        Label descPDF = Design.muted(
            "Génère un fichier HTML mis en forme (tableau identique à l'affichage). " +
            "Ouvrez-le dans Chrome ou Firefox puis faites Ctrl+P → Enregistrer en PDF.");

        // Info-box visuelle
        HBox infoBox = new HBox(10);
        infoBox.setPadding(new Insets(10, 14, 10, 14));
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setStyle(
            "-fx-background-color: #e8f4fd;" +
            "-fx-border-color: " + Design.ADMIN_ACCENT + ";" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-border-radius: 0 6 6 0; -fx-background-radius: 6;"
        );
        Label infoLbl = new Label("💡  Le fichier HTML contient des styles d'impression — Ctrl+P suffira pour obtenir un PDF propre.");
        infoLbl.setStyle("-fx-font-size:12;-fx-text-fill:" + Design.ADMIN_ACCENT + ";");
        infoLbl.setWrapText(true);
        infoBox.getChildren().add(infoLbl);

        Label msgPDF = new Label(""); msgPDF.setWrapText(true);
        Button btnPDF = Design.btnPrimary("📄  Exporter en HTML / PDF", Design.DANGER);
        btnPDF.setOnAction(e -> exporterHTML(cbClasse.getValue(), msgPDF));

        pdfSection.getChildren().addAll(descPDF, infoBox, btnPDF, msgPDF);
        panel.getChildren().add(pdfSection);

        // ── Export CSV ────────────────────────────────────────────────
        VBox csvSection = Design.section("📊  Export CSV (compatible Excel)");

        Label descCSV = Design.muted(
            "Fichier .csv compatible Excel et LibreOffice Calc. Encodage UTF-8 avec BOM pour un affichage correct des accents.");

        Label msgCSV = new Label(""); msgCSV.setWrapText(true);
        Button btnCSV = Design.btnPrimary("📊  Exporter en CSV", Design.SUCCESS);
        btnCSV.setOnAction(e -> exporterCSV(cbClasse.getValue(), msgCSV));

        csvSection.getChildren().addAll(descCSV, btnCSV, msgCSV);
        panel.getChildren().add(csvSection);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    // ── Export HTML ───────────────────────────────────────────────────
    private void exporterHTML(String classe, Label msg) {
        if (classe == null) { setMsg(msg, "⚠️  Sélectionnez une classe.", Design.WARNING); return; }
        List<EmploiDuTemps> data = edtDAO.obtenirParClasse(classe);
        if (data.isEmpty()) { setMsg(msg, "⚠️  Aucun créneau pour cette classe.", Design.WARNING); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Enregistrer l'emploi du temps");
        fc.setInitialFileName("EDT_" + classe.replace(" ", "_") + ".html");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML", "*.html"));
        File f = fc.showSaveDialog(new Stage());
        if (f == null) {
			return;
		}

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            pw.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            pw.println("<title>EDT " + classe + "</title>");
            pw.println("<style>");
            pw.println("body{font-family:Arial,sans-serif;padding:20px;color:#1a1f3c;}");
            pw.println("h1{color:#1a1f3c;font-size:20px;margin-bottom:4px;}");
            pw.println(".sub{color:#8395a7;font-size:11px;margin-bottom:16px;}");
            pw.println("table{border-collapse:collapse;width:100%;border-radius:8px;overflow:hidden;}");
            pw.println("th{background:#1a1f3c;color:white;padding:9px;font-size:12px;border:1px solid #2d3561;}");
            pw.println("td{border:1px solid #e8ecf5;padding:7px;vertical-align:top;min-width:110px;font-size:11px;}");
            pw.println(".cm{background:#dbeeff;border-left:3px solid #4f6ef7;}");
            pw.println(".td{background:#d5f5e3;border-left:3px solid #00b894;}");
            pw.println(".tp{background:#fdecea;border-left:3px solid #e74c3c;}");
            pw.println(".salle{color:#e74c3c;font-weight:bold;}");
            pw.println(".pause{background:#eaecf0;text-align:center;color:#636e72;font-weight:bold;font-size:12px;}");
            pw.println("@media print{body{padding:0;} .sub{margin-bottom:8px;}}");
            pw.println("</style></head><body>");
            pw.println("<h1>📅  Emploi du Temps — " + classe + "</h1>");
            pw.println("<div class='sub'>Généré le " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) + "</div>");
            pw.println("<table><tr><th>Heures</th>");
            for (String j : JOURS) {
				if (!j.isEmpty()) {
					pw.println("<th>" + j + "</th>");
				}
			}
            pw.println("</tr>");

            boolean[][] drawn = new boolean[6][12];
            for (int h = 8; h <= 19; h++) {
                if (h == 13 || h == 14) {
                    if (h == 13) {
						pw.println("<tr><td><b>" + h + "h–" + (h+1) + "h</b></td><td class='pause' colspan='6'>Pause méridienne (13h–15h)</td></tr>");
					} else {
						pw.println("<tr><td><b>" + h + "h–" + (h+1) + "h</b></td><td colspan='6'></td></tr>");
					}
                    continue;
                }
                pw.print("<tr><td><b>" + String.format("%02dh–%02dh", h, h + 1) + "</b></td>");
                for (int j = 1; j <= 6; j++) {
                    if (drawn[j-1][h-8]) {
						continue;
					}
                    final int jj = j, hh = h;
                    EmploiDuTemps edt = data.stream()
                        .filter(e -> e.getJourSemaine() == jj && e.getHeureDebut().getHour() == hh && e.getHeureDebut().getMinute() == 0)
                        .findFirst().orElse(null);
                    if (edt != null) {
                        int rs = Math.max(1, edt.getDuree() / 60);
                        for (int r = 0; r < rs && (h - 8 + r) < 12; r++) {
							drawn[j-1][h-8+r] = true;
						}
                        Salle salle = salleDAO.obtenirParId(edt.getSalleId());
                        String nomSalle = salle != null ? salle.getNumero() : "?";
                        String cssType  = switch (edt.getTypeCours() != null ? edt.getTypeCours().toUpperCase() : "CM") {
                            case "TD" -> "td";
                            case "TP" -> "tp";
                            default   -> "cm";
                        };
                        pw.print("<td rowspan='" + rs + "' class='" + cssType + "'>");
                        pw.print("<b>" + edt.getMatiere() + "</b><br/>(" + edt.getEnseignant() + ")<br/>");
                        pw.print("<span class='salle'>" + nomSalle + "</span>");
                        pw.print("</td>");
                    } else {
                        pw.print("<td></td>");
                    }
                }
                pw.println("</tr>");
            }
            pw.println("</table></body></html>");
            setMsg(msg,
                "✅  Fichier HTML enregistré : " + f.getName() + "\n→ Ouvrez dans un navigateur puis Ctrl+P pour le PDF.",
                Design.SUCCESS);
        } catch (Exception ex) {
            setMsg(msg, "❌  Erreur : " + ex.getMessage(), Design.DANGER);
        }
    }

    // ── Export CSV ────────────────────────────────────────────────────
    private void exporterCSV(String classe, Label msg) {
        if (classe == null) { setMsg(msg, "⚠️  Sélectionnez une classe.", Design.WARNING); return; }
        List<EmploiDuTemps> data = edtDAO.obtenirParClasse(classe);
        if (data.isEmpty()) { setMsg(msg, "⚠️  Aucun créneau pour cette classe.", Design.WARNING); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Exporter en CSV");
        fc.setInitialFileName("EDT_" + classe.replace(" ", "_") + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
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
                    s != null ? s.getNumero() : "?",
                    s != null ? s.getBatiment() : "?"));
            }
            setMsg(msg, "✅  CSV exporté : " + f.getName(), Design.SUCCESS);
        } catch (Exception ex) {
            setMsg(msg, "❌  Erreur : " + ex.getMessage(), Design.DANGER);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────
    private void setMsg(Label lbl, String text, String color) {
        lbl.setText(text);
        lbl.setStyle("-fx-font-size:12;-fx-text-fill:" + color + ";-fx-font-weight:bold;" +
            "-fx-padding:6 10;-fx-background-color:derive(" + color + ",85%);-fx-background-radius:6;");
    }
}
