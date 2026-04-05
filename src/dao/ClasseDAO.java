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
        // Récupérer le nom de la classe avant suppression (pour nettoyer les données liées)
        String nomClasse = null;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT nom FROM classes WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) {
				nomClasse = rs.getString("nom");
			} }
        } catch (SQLException e) { System.err.println("Erreur lecture nom classe: " + e.getMessage()); }

        if (nomClasse == null) {
			return; // classe introuvable, rien à faire
		}

        // Supprimer en cascade : cours, emploi_du_temps, puis la classe elle-même
        try (Connection c = DatabaseConnection.getConnection()) {
            // 1. Cours ponctuels de cette classe
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM cours WHERE classe = ?")) {
                ps.setString(1, nomClasse); ps.executeUpdate();
            }
            // 2. Créneaux EDT de cette classe
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM emploi_du_temps WHERE classe = ?")) {
                ps.setString(1, nomClasse); ps.executeUpdate();
            }
            // 3. La classe elle-même
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM classes WHERE id = ?")) {
                ps.setInt(1, id); ps.executeUpdate();
            }
        } catch (SQLException e) { throw new RuntimeException("Erreur suppression cascade : " + e.getMessage(), e); }
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
        // Source unique : la table classes — sans fallback vers cours/EDT
        // pour éviter de remonter des classes supprimées
        List<String> noms = new ArrayList<>();
        String sql = "SELECT nom FROM classes ORDER BY nom";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
				noms.add(rs.getString("nom"));
			}
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return noms;
    }
    private Classe mapper(ResultSet rs) throws SQLException {
        return new Classe(rs.getInt("id"), rs.getString("nom"),
            rs.getString("filiere"), rs.getString("niveau"), rs.getInt("effectif"));
    }
}
