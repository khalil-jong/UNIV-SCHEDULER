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
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, b.getNom());
            pstmt.setString(2, b.getLocalisation());
            pstmt.setInt(3, b.getNombreEtages());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'ajouter le bâtiment : " + e.getMessage(), e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM batiments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de supprimer : " + e.getMessage(), e);
        }
    }

    public List<Batiment> obtenirTous() {
        List<Batiment> liste = new ArrayList<>();
        String sql = "SELECT * FROM batiments ORDER BY nom";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Batiment(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("localisation"),
                    rs.getInt("nombre_etages")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture bâtiments: " + e.getMessage());
        }
        return liste;
    }
}
