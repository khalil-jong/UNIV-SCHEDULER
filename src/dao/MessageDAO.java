package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import models.Message;

public class MessageDAO {

    /** Envoie un message (vers un rôle ou un utilisateur spécifique via destinataireId) */
    public void envoyer(Message msg) {
        String dest = msg.getDestinataireRole() != null ? msg.getDestinataireRole() : "GESTIONNAIRE";
        String sql = "INSERT INTO messages (expediteur_id, expediteur_nom, expediteur_role, sujet, corps, type, destinataire_role, destinataire_id) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, msg.getExpediteurId());
            ps.setString(2, msg.getExpediteurNom());
            ps.setString(3, msg.getExpediteurRole());
            ps.setString(4, msg.getSujet());
            ps.setString(5, msg.getCorps());
            ps.setString(6, msg.getType());
            ps.setString(7, dest);
            if (msg.getDestinataireId() > 0) {
				ps.setInt(8, msg.getDestinataireId());
			} else {
				ps.setNull(8, java.sql.Types.INTEGER);
			}
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage(), e); }
    }

    /** Tous les messages destinés au gestionnaire */
    public List<Message> obtenirTous() {
        return requete("SELECT * FROM messages WHERE destinataire_role = 'GESTIONNAIRE' ORDER BY created_at DESC");
    }

    /** Tous les messages destinés à l'administrateur */
    public List<Message> obtenirPourAdmin() {
        return requete("SELECT * FROM messages WHERE destinataire_role = 'ADMIN' ORDER BY created_at DESC");
    }

    /** Messages reçus par un utilisateur spécifique (envoyés par le gestionnaire) */
    public List<Message> obtenirPourUtilisateur(int userId) {
        String sql = "SELECT * FROM messages WHERE destinataire_id = ? ORDER BY created_at DESC";
        List<Message> liste = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
					liste.add(mapper(rs));
				}
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return liste;
    }

    /** Compter les non lus pour un utilisateur spécifique */
    public int compterNonLusPourUtilisateur(int userId) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM messages WHERE destinataire_id = ? AND lu = 0")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
					return rs.getInt(1);
				}
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    /** Marquer tous les messages d'un utilisateur comme lus */
    public void marquerTousLusPourUtilisateur(int userId) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE messages SET lu = 1 WHERE destinataire_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    public List<Message> obtenirNonLus() {
        return requete("SELECT * FROM messages WHERE lu = 0 AND destinataire_role = 'GESTIONNAIRE' ORDER BY created_at DESC");
    }

    public int compterNonLus() {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT COUNT(*) FROM messages WHERE lu = 0 AND destinataire_role = 'GESTIONNAIRE'")) {
            if (rs.next()) {
				return rs.getInt(1);
			}
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    public int compterNonLusAdmin() {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT COUNT(*) FROM messages WHERE lu = 0 AND destinataire_role = 'ADMIN'")) {
            if (rs.next()) {
				return rs.getInt(1);
			}
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    public void marquerLu(int id) {
        exec("UPDATE messages SET lu = 1 WHERE id = " + id);
    }

    public void marquerTousLus() {
        exec("UPDATE messages SET lu = 1 WHERE destinataire_role = 'GESTIONNAIRE'");
    }

    public void marquerTousLusAdmin() {
        exec("UPDATE messages SET lu = 1 WHERE destinataire_role = 'ADMIN'");
    }

    public void supprimer(int id) {
        exec("DELETE FROM messages WHERE id = " + id);
    }

    private List<Message> requete(String sql) {
        List<Message> liste = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
				liste.add(mapper(rs));
			}
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return liste;
    }

    private void exec(String sql) {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    private Message mapper(ResultSet rs) throws SQLException {
        String destRole = "GESTIONNAIRE";
        try { destRole = rs.getString("destinataire_role"); } catch (SQLException ignored) {}
        Message m = new Message(
            rs.getInt("id"), rs.getInt("expediteur_id"),
            rs.getString("expediteur_nom"), rs.getString("expediteur_role"),
            rs.getString("sujet"), rs.getString("corps"), rs.getString("type"),
            rs.getBoolean("lu"), rs.getTimestamp("created_at").toLocalDateTime(),
            destRole
        );
        try { m.setDestinataireId(rs.getInt("destinataire_id")); } catch (Exception ignored) {}
        return m;
    }
}
