package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Système de design centralisé pour UNIV-SCHEDULER.
 * Toutes les couleurs, styles et composants réutilisables sont ici.
 */
public class Design {

    // ─── Palette principale ───────────────────────────────────────────
    public static final String ADMIN_PRIMARY    = "#1a1f3c";   // Navy foncé
    public static final String ADMIN_ACCENT     = "#4f6ef7";   // Bleu indigo
    public static final String ADMIN_MENU_BG    = "#1e2547";   // Navy sidebar
    public static final String ADMIN_HOVER      = "#2d3561";   // Hover sidebar admin

    public static final String GEST_PRIMARY     = "#0f4c3a";   // Vert forêt foncé
    public static final String GEST_ACCENT      = "#00b894";   // Vert émeraude
    public static final String GEST_MENU_BG     = "#13573f";   // Vert sidebar
    public static final String GEST_HOVER       = "#1a6b4e";   // Hover sidebar gest

    public static final String ENS_PRIMARY      = "#3d1a6e";   // Violet foncé
    public static final String ENS_ACCENT       = "#9b59b6";   // Violet
    public static final String ENS_MENU_BG      = "#4a1f82";   // Violet sidebar
    public static final String ENS_HOVER        = "#5d2a9e";   // Hover sidebar ens

    public static final String ETU_PRIMARY      = "#7d3c00";   // Orange foncé
    public static final String ETU_ACCENT       = "#f39c12";   // Orange
    public static final String ETU_MENU_BG      = "#9b4a00";   // Orange sidebar
    public static final String ETU_HOVER        = "#b55a00";   // Hover sidebar etu

    // ─── Couleurs fonctionnelles ──────────────────────────────────────
    public static final String SUCCESS   = "#00b894";
    public static final String DANGER    = "#e74c3c";
    public static final String WARNING   = "#f39c12";
    public static final String INFO      = "#4f6ef7";
    public static final String NEUTRAL   = "#636e72";

    // ─── Background content ────────────────────────────────────────────
    public static final String BG_LIGHT  = "#f8f9fe";
    public static final String BG_WHITE  = "#ffffff";
    public static final String BG_CARD   = "#ffffff";
    public static final String BORDER    = "#e8ecf5";
    public static final String TEXT_DARK = "#1a1f3c";
    public static final String TEXT_MUTED= "#8395a7";

    // ─── Styles CSS réutilisables ─────────────────────────────────────
    public static final String CARD_STYLE =
        "-fx-background-color: white;" +
        "-fx-border-color: #e8ecf5;" +
        "-fx-border-width: 1;" +
        "-fx-border-radius: 12;" +
        "-fx-background-radius: 12;" +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 3);";

    public static final String SECTION_STYLE =
        "-fx-background-color: white;" +
        "-fx-border-color: #e8ecf5;" +
        "-fx-border-width: 1;" +
        "-fx-border-radius: 10;" +
        "-fx-background-radius: 10;" +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);";

    public static final String INPUT_STYLE =
        "-fx-background-color: #f8f9fe;" +
        "-fx-border-color: #d8e1f0;" +
        "-fx-border-width: 1;" +
        "-fx-border-radius: 6;" +
        "-fx-background-radius: 6;" +
        "-fx-padding: 6 10;" +
        "-fx-font-size: 13;";

    // ─── Boutons ──────────────────────────────────────────────────────
    public static Button btnPrimary(String label, String couleur) {
        Button btn = new Button(label);
        btn.setStyle(
            "-fx-background-color: " + couleur + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 9 20;" +
            "-fx-background-radius: 7;" +
            "-fx-cursor: hand;"
        );
        String hover = "-fx-background-color: derive(" + couleur + ", -12%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 9 20;" +
            "-fx-background-radius: 7;" +
            "-fx-cursor: hand;";
        String normal = "-fx-background-color: " + couleur + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 9 20;" +
            "-fx-background-radius: 7;" +
            "-fx-cursor: hand;";
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e  -> btn.setStyle(normal));
        return btn;
    }

    public static Button btnSecondary(String label) {
        Button btn = new Button(label);
        String n = "-fx-background-color: #f1f3f9;" +
            "-fx-text-fill: #636e72;" +
            "-fx-font-size: 13;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 9 18;" +
            "-fx-background-radius: 7;" +
            "-fx-border-color: #d8e1f0;" +
            "-fx-border-radius: 7;" +
            "-fx-cursor: hand;";
        String h = "-fx-background-color: #e8ecf5;" +
            "-fx-text-fill: #636e72;" +
            "-fx-font-size: 13;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 9 18;" +
            "-fx-background-radius: 7;" +
            "-fx-border-color: #c0cbdf;" +
            "-fx-border-radius: 7;" +
            "-fx-cursor: hand;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e  -> btn.setStyle(n));
        return btn;
    }

    public static Button btnDanger(String label) {
        return btnPrimary(label, DANGER);
    }

    // ─── Labels ───────────────────────────────────────────────────────
    public static Label pageTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: " + TEXT_DARK + ";");
        return lbl;
    }

    public static Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: " + TEXT_DARK + ";");
        return lbl;
    }

    public static Label muted(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: " + TEXT_MUTED + ";");
        lbl.setWrapText(true);
        return lbl;
    }

    public static Label msgSuccess(String text) {
        Label lbl = new Label("✅ " + text);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: " + SUCCESS + "; -fx-font-weight: bold; " +
            "-fx-padding: 8 12; -fx-background-color: #e8faf5; -fx-background-radius: 6;");
        lbl.setWrapText(true);
        return lbl;
    }

    public static Label msgError(String text) {
        Label lbl = new Label("❌ " + text);
        lbl.setStyle("-fx-font-size: 12; -fx-text-fill: " + DANGER + "; -fx-font-weight: bold; " +
            "-fx-padding: 8 12; -fx-background-color: #fdecea; -fx-background-radius: 6;");
        lbl.setWrapText(true);
        return lbl;
    }

    // ─── Sections avec titre ──────────────────────────────────────────
    public static VBox section(String titre) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(18));
        box.setStyle(SECTION_STYLE);
        Label lbl = sectionTitle(titre);
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e8ecf5;");
        box.getChildren().addAll(lbl, sep);
        return box;
    }

    // ─── Carte de statistique colorée ─────────────────────────────────
    public static VBox statCard(String icon, String value, String label, String color) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(20, 18, 20, 18));
        card.setPrefWidth(175);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 0, 4);"
        );

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 26;");
        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: white;");
        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 12; -fx-text-fill: rgba(255,255,255,0.85);");

        card.getChildren().addAll(iconLbl, valLbl, nameLbl);
        return card;
    }

    // ─── Top bar générique ─────────────────────────────────────────────
    public static HBox topBar(String roleLabel, String nomUtilisateur, String bgColor,
                               Runnable onDeconnexion) {
        HBox bar = new HBox();
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setPrefHeight(56);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: " + bgColor + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 10, 0, 0, 3);");

        // Logo / titre
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 10; -fx-padding: 0 6 0 0;");
        Label titre = new Label("UNIV-SCHEDULER");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-letter-spacing: 1;");
        Label sep = new Label("|");
        sep.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 14; -fx-padding: 0 10;");
        Label role = new Label(roleLabel);
        role.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13;");

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLbl = new Label("👤  " + nomUtilisateur);
        userLbl.setStyle("-fx-text-fill: white; -fx-font-size: 13; -fx-padding: 0 16 0 0;");

        Button btnDeco = new Button("Déconnexion");
        btnDeco.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12;" +
            "-fx-padding: 6 14;" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: rgba(255,255,255,0.3);" +
            "-fx-border-radius: 6;" +
            "-fx-cursor: hand;"
        );
        btnDeco.setOnMouseEntered(e -> btnDeco.setStyle(
            "-fx-background-color: rgba(255,255,255,0.25);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12;" +
            "-fx-padding: 6 14;" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: rgba(255,255,255,0.5);" +
            "-fx-border-radius: 6;" +
            "-fx-cursor: hand;"
        ));
        btnDeco.setOnMouseExited(e -> btnDeco.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12;" +
            "-fx-padding: 6 14;" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: rgba(255,255,255,0.3);" +
            "-fx-border-radius: 6;" +
            "-fx-cursor: hand;"
        ));
        btnDeco.setOnAction(e -> onDeconnexion.run());

        bar.getChildren().addAll(dot, titre, sep, role, spacer, userLbl, btnDeco);
        return bar;
    }

    // ─── Bouton de menu latéral ────────────────────────────────────────
    public static Button menuBtn(String label, String hoverColor) {
        Button btn = new Button(label);
        btn.setPrefWidth(200);
        btn.setPrefHeight(40);
        String sN = "-fx-background-color: transparent;" +
            "-fx-text-fill: rgba(255,255,255,0.88);" +
            "-fx-font-size: 12;" +
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-padding: 0 10;" +
            "-fx-background-radius: 7;" +
            "-fx-cursor: hand;";
        String sH = "-fx-background-color: " + hoverColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12;" +
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-padding: 0 10;" +
            "-fx-background-radius: 7;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;";
        btn.setStyle(sN);
        btn.setOnMouseEntered(e -> btn.setStyle(sH));
        btn.setOnMouseExited(e  -> btn.setStyle(sN));
        return btn;
    }

    public static Label menuTitle(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setStyle("-fx-text-fill: rgba(255,255,255,0.45); -fx-font-size: 9; -fx-font-weight: bold;" +
            "-fx-padding: 14 10 4 10; -fx-letter-spacing: 1;");
        return lbl;
    }
}
