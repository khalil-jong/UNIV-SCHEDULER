package dao;

import database.DatabaseConnection;
import models.EmploiDuTemps;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class EmploiDuTempsDAO {

    public void ajouter(EmploiDuTemps edt) {
        String sql = "INSERT INTO emploi_du_temps (classe, matiere, enseignant, salle_id, jour_semaine, heure_debut, duree, type_cours) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, edt.getClasse());
            ps.setString(2, edt.getMatiere());
            ps.setString(3, edt.getEnseignant());
            ps.setInt(4, edt.getSalleId());
            ps.setInt(5, edt.getJourSemaine());
            ps.setTime(6, Time.valueOf(edt.getHeureDebut()));
            ps.setInt(7, edt.getDuree());
            ps.setString(8, edt.getTypeCours());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout EDT : " + e.getMessage(), e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM emploi_du_temps WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression EDT : " + e.getMessage(), e);
        }
    }

    public List<EmploiDuTemps> obtenirParClasse(String classe) {
        List<EmploiDuTemps> liste = new ArrayList<>();
        String sql = "SELECT * FROM emploi_du_temps WHERE classe = ? AND actif = 1 ORDER BY jour_semaine, heure_debut";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classe);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture EDT : " + e.getMessage());
        }
        return liste;
    }

    public List<EmploiDuTemps> obtenirParEnseignant(String nomComplet) {
        List<EmploiDuTemps> liste = new ArrayList<>();
        // Chercher les deux formats : "Prénom Nom" et "Nom Prénom"
        String inverse = "";
        String[] p = nomComplet.trim().split("\\s+", 2);
        if (p.length == 2) inverse = p[1] + " " + p[0];

        String sql = "SELECT * FROM emploi_du_temps WHERE actif = 1 AND (LOWER(enseignant) = LOWER(?) OR LOWER(enseignant) = LOWER(?)) ORDER BY jour_semaine, heure_debut";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomComplet);
            ps.setString(2, inverse.isEmpty() ? nomComplet : inverse);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture EDT enseignant : " + e.getMessage());
        }
        return liste;
    }

    public List<EmploiDuTemps> obtenirTous() {
        List<EmploiDuTemps> liste = new ArrayList<>();
        String sql = "SELECT * FROM emploi_du_temps WHERE actif = 1 ORDER BY classe, jour_semaine, heure_debut";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) liste.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("Erreur lecture EDT : " + e.getMessage());
        }
        return liste;
    }

    public List<String> obtenirToutesLesClasses() {
        List<String> classes = new ArrayList<>();
        String sql = "SELECT DISTINCT classe FROM emploi_du_temps WHERE actif = 1 ORDER BY classe";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) classes.add(rs.getString("classe"));
        } catch (SQLException e) {
            System.err.println("Erreur classes EDT : " + e.getMessage());
        }
        return classes;
    }

    /** Vérifie si la salle est déjà occupée ce jour/heure dans l'EDT */
    public boolean salleOccupee(int salleId, int jourSemaine, LocalTime heureDebut, int duree, int excluId) {
        LocalTime heureFin = heureDebut.plusMinutes(duree);
        String sql = "SELECT COUNT(*) FROM emploi_du_temps WHERE salle_id = ? AND jour_semaine = ? AND actif = 1 AND id <> ? AND heure_debut < ? AND ADDTIME(heure_debut, SEC_TO_TIME(duree*60)) > ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, salleId);
            ps.setInt(2, jourSemaine);
            ps.setInt(3, excluId);
            ps.setTime(4, Time.valueOf(heureFin));
            ps.setTime(5, Time.valueOf(heureDebut));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérif salle EDT : " + e.getMessage());
        }
        return false;
    }

    /** Vérifie si l'enseignant est déjà occupé ce créneau */
    public boolean enseignantOccupe(String enseignant, int jourSemaine, LocalTime heureDebut, int duree, int excluId) {
        LocalTime heureFin = heureDebut.plusMinutes(duree);
        String sql = "SELECT COUNT(*) FROM emploi_du_temps WHERE LOWER(enseignant) = LOWER(?) AND jour_semaine = ? AND actif = 1 AND id <> ? AND heure_debut < ? AND ADDTIME(heure_debut, SEC_TO_TIME(duree*60)) > ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, enseignant);
            ps.setInt(2, jourSemaine);
            ps.setInt(3, excluId);
            ps.setTime(4, Time.valueOf(heureFin));
            ps.setTime(5, Time.valueOf(heureDebut));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérif enseignant EDT : " + e.getMessage());
        }
        return false;
    }

    private EmploiDuTemps mapper(ResultSet rs) throws SQLException {
        return new EmploiDuTemps(
            rs.getInt("id"),
            rs.getString("classe"),
            rs.getString("matiere"),
            rs.getString("enseignant"),
            rs.getInt("salle_id"),
            rs.getInt("jour_semaine"),
            rs.getTime("heure_debut").toLocalTime(),
            rs.getInt("duree"),
            rs.getString("type_cours")
        );
    }
}
