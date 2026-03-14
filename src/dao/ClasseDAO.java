package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import models.Classe;

public class ClasseDAO {

    public void ajouter(Classe cl) {
        String sql = "INSERT INTO classes (nom, filiere, niveau, effectif) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cl.getNom()); ps.setString(2, cl.getFiliere());
            ps.setString(3, cl.getNiveau()); ps.setInt(4, cl.getEffectif());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage(), e); }
    }

    public void modifier(Classe cl) {
        String sql = "UPDATE classes SET nom=?, filiere=?, niveau=?, effectif=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cl.getNom()); ps.setString(2, cl.getFiliere());
            ps.setString(3, cl.getNiveau()); ps.setInt(4, cl.getEffectif());
            ps.setInt(5, cl.getId()); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage(), e); }
    }

    public void supprimer(int id) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM classes WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage(), e); }
    }

    public List<Classe> obtenirTous() {
        List<Classe> liste = new ArrayList<>();
        String sql = "SELECT * FROM classes ORDER BY nom";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
				liste.add(mapper(rs));
			}
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return liste;
    }

    public List<String> obtenirNomsClasses() {
        List<String> noms = new ArrayList<>();
        String sql = "SELECT nom FROM classes ORDER BY nom";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
				noms.add(rs.getString("nom"));
			}
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        // Fallback: récupérer aussi depuis cours et EDT si la table classes est vide
        if (noms.isEmpty()) {
            try (Connection c = DatabaseConnection.getConnection(); Statement st = c.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT DISTINCT classe FROM cours UNION SELECT DISTINCT classe FROM emploi_du_temps ORDER BY classe");
                while (rs.next()) {
					noms.add(rs.getString(1));
				}
            } catch (SQLException e) { System.err.println(e.getMessage()); }
        }
        return noms;
    }

    private Classe mapper(ResultSet rs) throws SQLException {
        return new Classe(rs.getInt("id"), rs.getString("nom"),
            rs.getString("filiere"), rs.getString("niveau"), rs.getInt("effectif"));
    }
}
