package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import models.Utilisateur;

public class UtilisateurDAO {

    public Utilisateur connecter(String login, String motDePasse) {
        String sql = "SELECT * FROM utilisateurs WHERE login = ? AND mot_de_passe = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            pstmt.setString(2, motDePasse);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("login"),
                        rs.getString("mot_de_passe"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
        return null;
    }

    public List<Utilisateur> obtenirTous() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs ORDER BY role, nom";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Utilisateur(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("login"),
                    rs.getString("mot_de_passe"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture utilisateurs: " + e.getMessage());
        }
        return liste;
    }

    public void ajouter(Utilisateur u) {
        String sql = "INSERT INTO utilisateurs (nom, prenom, login, mot_de_passe, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getNom());
            pstmt.setString(2, u.getPrenom());
            pstmt.setString(3, u.getLogin());
            pstmt.setString(4, u.getMotDePasse());
            pstmt.setString(5, u.getRole());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible d'ajouter l'utilisateur : " + e.getMessage(), e);
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de supprimer l'utilisateur : " + e.getMessage(), e);
        }
    }

    public boolean loginExiste(String login) {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE login = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, login);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
					return rs.getInt(1) > 0;
				}
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification login: " + e.getMessage());
        }
        return false;
    }
}
