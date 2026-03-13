package dao;

import database.DatabaseConnection;
import models.Cours;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CoursDAO {

    public void ajouter(Cours cours) {
        String sql = "INSERT INTO cours (matiere, enseignant, classe, groupe, date_debut, duree, salle_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cours.getMatiere());
            pstmt.setString(2, cours.getEnseignant());
            pstmt.setString(3, cours.getClasse());
            pstmt.setString(4, cours.getGroupe());
            pstmt.setTimestamp(5, Timestamp.valueOf(cours.getDateDebut()));
            pstmt.setInt(6, cours.getDuree());
            pstmt.setInt(7, cours.getSalleId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'ajouter le cours : " + e.getMessage(), e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM cours WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de supprimer : " + e.getMessage(), e);
        }
    }

    public void modifier(Cours cours) {
        String sql = "UPDATE cours SET matiere=?, enseignant=?, classe=?, groupe=?, date_debut=?, duree=?, salle_id=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cours.getMatiere());
            pstmt.setString(2, cours.getEnseignant());
            pstmt.setString(3, cours.getClasse());
            pstmt.setString(4, cours.getGroupe());
            pstmt.setTimestamp(5, Timestamp.valueOf(cours.getDateDebut()));
            pstmt.setInt(6, cours.getDuree());
            pstmt.setInt(7, cours.getSalleId());
            pstmt.setInt(8, cours.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de modifier : " + e.getMessage(), e);
        }
    }

    public List<Cours> obtenirTous() {
        List<Cours> cours = new ArrayList<>();
        String sql = "SELECT * FROM cours ORDER BY date_debut ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) cours.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("Erreur lecture: " + e.getMessage());
        }
        return cours;
    }

    public Cours obtenirParId(int id) {
        String sql = "SELECT * FROM cours WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapper(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return null;
    }

    public List<Cours> obtenirParClasse(String classe) {
        List<Cours> cours = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE classe = ? ORDER BY date_debut ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, classe);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) cours.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return cours;
    }

    /**
     * Recherche par nom d'enseignant en testant les deux formats :
     *   - "Prénom Nom"  (format getNomComplet() de Utilisateur)
     *   - "Nom Prénom"  (format souvent saisi manuellement dans la base)
     * Ainsi un enseignant connecté voit toujours ses cours, quel que soit
     * le format utilisé lors de la saisie.
     */
    public List<Cours> obtenirParEnseignant(String nomComplet) {
        List<Cours> cours = new ArrayList<>();

        // Format inversé : "Prénom Nom" → "Nom Prénom"
        String nomInverse = "";
        String[] parties = nomComplet.trim().split("\\s+", 2);
        if (parties.length == 2) {
            nomInverse = parties[1] + " " + parties[0];
        }

        String sql = "SELECT * FROM cours WHERE LOWER(enseignant) = LOWER(?) OR LOWER(enseignant) = LOWER(?) ORDER BY date_debut ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomComplet);
            pstmt.setString(2, nomInverse.isEmpty() ? nomComplet : nomInverse);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) cours.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return cours;
    }

    public List<Cours> obtenirParSalle(int salleId) {
        List<Cours> cours = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE salle_id = ? ORDER BY date_debut ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, salleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) cours.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return cours;
    }

    public List<Cours> obtenirParSemaine(LocalDateTime debutSemaine) {
        List<Cours> cours = new ArrayList<>();
        LocalDateTime finSemaine = debutSemaine.plusDays(7);
        String sql = "SELECT * FROM cours WHERE date_debut >= ? AND date_debut < ? ORDER BY date_debut ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(debutSemaine));
            pstmt.setTimestamp(2, Timestamp.valueOf(finSemaine));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) cours.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return cours;
    }

    // Cours d'une semaine filtrés par classe
    public List<Cours> obtenirParSemaineEtClasse(LocalDateTime debutSemaine, String classe) {
        List<Cours> cours = new ArrayList<>();
        LocalDateTime finSemaine = debutSemaine.plusDays(7);
        String sql = "SELECT * FROM cours WHERE date_debut >= ? AND date_debut < ? AND classe = ? ORDER BY date_debut ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(debutSemaine));
            pstmt.setTimestamp(2, Timestamp.valueOf(finSemaine));
            pstmt.setString(3, classe);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) cours.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return cours;
    }

    public List<Cours> obtenirParJour(LocalDateTime jour) {
        List<Cours> cours = new ArrayList<>();
        LocalDateTime finJour = jour.plusDays(1);
        String sql = "SELECT * FROM cours WHERE date_debut >= ? AND date_debut < ? ORDER BY date_debut ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(jour));
            pstmt.setTimestamp(2, Timestamp.valueOf(finJour));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) cours.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return cours;
    }

    // Cours d'un jour filtré par classe
    public List<Cours> obtenirParJourEtClasse(LocalDateTime jour, String classe) {
        List<Cours> cours = new ArrayList<>();
        LocalDateTime finJour = jour.plusDays(1);
        String sql = "SELECT * FROM cours WHERE date_debut >= ? AND date_debut < ? AND classe = ? ORDER BY date_debut ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(jour));
            pstmt.setTimestamp(2, Timestamp.valueOf(finJour));
            pstmt.setString(3, classe);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) cours.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return cours;
    }

    // Toutes les classes distinctes (pour le filtre du calendrier)
    public List<String> obtenirToutesLesClasses() {
        List<String> classes = new ArrayList<>();
        String sql = "SELECT DISTINCT classe FROM cours ORDER BY classe ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) classes.add(rs.getString("classe"));
        } catch (SQLException e) {
            System.err.println("Erreur classes: " + e.getMessage());
        }
        return classes;
    }

    public List<String> detecterConflits() {
        List<Cours> tousLesCours = obtenirTous();
        List<String> conflits = new ArrayList<>();
        for (int i = 0; i < tousLesCours.size(); i++) {
            for (int j = i + 1; j < tousLesCours.size(); j++) {
                Cours c1 = tousLesCours.get(i);
                Cours c2 = tousLesCours.get(j);
                boolean seSuperposent = c1.getDateDebut().isBefore(c2.getDateFin())
                        && c2.getDateDebut().isBefore(c1.getDateFin());
                if (!seSuperposent) continue;
                if (c1.getSalleId() == c2.getSalleId()) {
                    conflits.add("🏫 CONFLIT SALLE : \"" + c1.getMatiere() + "\" et \"" + c2.getMatiere()
                            + "\" utilisent la même salle au même moment.");
                }
                if (c1.getEnseignant().equalsIgnoreCase(c2.getEnseignant())) {
                    conflits.add("👤 CONFLIT ENSEIGNANT : " + c1.getEnseignant()
                            + " a deux cours en même temps (\"" + c1.getMatiere() + "\" et \"" + c2.getMatiere() + "\").");
                }
            }
        }
        return conflits;
    }

    private Cours mapper(ResultSet rs) throws SQLException {
        String groupe = "";
        try { groupe = rs.getString("groupe"); } catch (SQLException ignored) {}
        return new Cours(
            rs.getInt("id"),
            rs.getString("matiere"),
            rs.getString("enseignant"),
            rs.getString("classe"),
            groupe,
            rs.getTimestamp("date_debut").toLocalDateTime(),
            rs.getInt("duree"),
            rs.getInt("salle_id")
        );
    }
}
