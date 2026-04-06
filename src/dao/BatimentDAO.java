package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import models.Batiment;

public class BatimentDAO {

    public void ajouter(Batiment b) {
        String sql = "INSERT INTO batiments (nom, localisation, nombre_etages) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getNom());
            ps.setString(2, b.getLocalisation());
            ps.setInt(3, b.getNombreEtages());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'ajouter le bâtiment : " + e.getMessage(), e);
        }
    }

    public void modifier(Batiment b) {
        String sql = "UPDATE batiments SET nom=?, localisation=?, nombre_etages=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getNom());
            ps.setString(2, b.getLocalisation());
            ps.setInt(3, b.getNombreEtages());
            ps.setInt(4, b.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de modifier le bâtiment : " + e.getMessage(), e);
        }
    }

    /**
     * Supprime le bâtiment ET toutes ses salles en cascade.
     */
    public void supprimer(int id) {
        String nomBat = obtenirNomParId(id);
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Supprimer les salles du bâtiment en premier
            if (nomBat != null) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM salles WHERE batiment = ?")) {
                    ps.setString(1, nomBat);
                    ps.executeUpdate();
                }
            }
            // Supprimer le bâtiment
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM batiments WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de supprimer : " + e.getMessage(), e);
        }
    }

    public List<Batiment> obtenirTous() {
        List<Batiment> liste = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM batiments ORDER BY nom")) {
            while (rs.next()) {
                liste.add(new Batiment(rs.getInt("id"), rs.getString("nom"),
                    rs.getString("localisation"), rs.getInt("nombre_etages")));
            }
        } catch (SQLException e) { System.err.println("Bâtiments: " + e.getMessage()); }
        return liste;
    }

    /** Noms des bâtiments uniquement — pour les ComboBox */
    public List<String> obtenirNoms() {
        List<String> noms = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT nom FROM batiments ORDER BY nom")) {
            while (rs.next()) {
				noms.add(rs.getString("nom"));
			}
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return noms;
    }

    public Batiment obtenirParNom(String nom) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM batiments WHERE nom = ?")) {
            ps.setString(1, nom);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
					return new Batiment(rs.getInt("id"), rs.getString("nom"),
					    rs.getString("localisation"), rs.getInt("nombre_etages"));
				}
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return null;
    }

    private String obtenirNomParId(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT nom FROM batiments WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
					return rs.getString("nom");
				}
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return null;
    }
}
