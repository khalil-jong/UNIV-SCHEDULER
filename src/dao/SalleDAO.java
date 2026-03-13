package dao;

import database.DatabaseConnection;
import models.Salle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalleDAO {

    public void ajouter(Salle salle) {
        String sql = "INSERT INTO salles (numero, capacite, type, batiment, etage, videoprojecteur, tableau_interactif, climatisation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, salle.getNumero());
            pstmt.setInt(2, salle.getCapacite());
            pstmt.setString(3, salle.getType());
            pstmt.setString(4, salle.getBatiment());
            pstmt.setString(5, salle.getEtage());
            pstmt.setBoolean(6, salle.isVideoprojecteur());
            pstmt.setBoolean(7, salle.isTableauInteractif());
            pstmt.setBoolean(8, salle.isClimatisation());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'ajouter la salle : " + e.getMessage(), e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM salles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de supprimer la salle : " + e.getMessage(), e);
        }
    }

    public void modifier(Salle salle) {
        String sql = "UPDATE salles SET numero=?, capacite=?, type=?, batiment=?, etage=?, videoprojecteur=?, tableau_interactif=?, climatisation=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, salle.getNumero());
            pstmt.setInt(2, salle.getCapacite());
            pstmt.setString(3, salle.getType());
            pstmt.setString(4, salle.getBatiment());
            pstmt.setString(5, salle.getEtage());
            pstmt.setBoolean(6, salle.isVideoprojecteur());
            pstmt.setBoolean(7, salle.isTableauInteractif());
            pstmt.setBoolean(8, salle.isClimatisation());
            pstmt.setInt(9, salle.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de modifier la salle : " + e.getMessage(), e);
        }
    }

    public List<Salle> obtenirTous() {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT * FROM salles ORDER BY batiment, numero";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) salles.add(mapper(rs));
        } catch (SQLException e) {
            System.err.println("Erreur lors de la lecture: " + e.getMessage());
        }
        return salles;
    }

    public Salle obtenirParId(int id) {
        String sql = "SELECT * FROM salles WHERE id = ?";
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

    public List<Salle> rechercherParCapacite(int capaciteMin) {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT * FROM salles WHERE capacite >= ? ORDER BY capacite ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, capaciteMin);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) salles.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return salles;
    }

    // Recherche par critères multiples : capacité + équipements + type
    public List<Salle> rechercherParCriteres(int capaciteMin, String type,
                                              boolean videoprojecteur, boolean tableauInteractif, boolean climatisation) {
        List<Salle> salles = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM salles WHERE capacite >= ?");
        if (type != null && !type.equals("Tous")) sql.append(" AND type = ?");
        if (videoprojecteur) sql.append(" AND videoprojecteur = TRUE");
        if (tableauInteractif) sql.append(" AND tableau_interactif = TRUE");
        if (climatisation) sql.append(" AND climatisation = TRUE");
        sql.append(" ORDER BY capacite ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            pstmt.setInt(idx++, capaciteMin);
            if (type != null && !type.equals("Tous")) pstmt.setString(idx++, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) salles.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche: " + e.getMessage());
        }
        return salles;
    }

    // Salles disponibles sur un créneau donné (pas de cours à cette heure-là)
    public List<Salle> obtenirSallesDisponibles(java.time.LocalDateTime debut, int dureeMinutes) {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT * FROM salles WHERE id NOT IN (" +
                     "SELECT salle_id FROM cours WHERE " +
                     "date_debut < ? AND DATE_ADD(date_debut, INTERVAL duree MINUTE) > ?" +
                     ") ORDER BY batiment, numero";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            java.time.LocalDateTime fin = debut.plusMinutes(dureeMinutes);
            pstmt.setTimestamp(1, Timestamp.valueOf(fin));
            pstmt.setTimestamp(2, Timestamp.valueOf(debut));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) salles.add(mapper(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur disponibilité: " + e.getMessage());
        }
        return salles;
    }

    private Salle mapper(ResultSet rs) throws SQLException {
        return new Salle(
            rs.getInt("id"),
            rs.getString("numero"),
            rs.getInt("capacite"),
            rs.getString("type"),
            rs.getString("batiment"),
            rs.getString("etage"),
            rs.getBoolean("videoprojecteur"),
            rs.getBoolean("tableau_interactif"),
            rs.getBoolean("climatisation")
        );
    }
}
