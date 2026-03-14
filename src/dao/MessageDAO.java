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

    public void envoyer(Message msg) {
        String sql = "INSERT INTO messages (expediteur_id, expediteur_nom, expediteur_role, sujet, corps, type) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, msg.getExpediteurId());
            ps.setString(2, msg.getExpediteurNom());
            ps.setString(3, msg.getExpediteurRole());
            ps.setString(4, msg.getSujet());
            ps.setString(5, msg.getCorps());
            ps.setString(6, msg.getType());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e.getMessage(), e); }
    }

    public List<Message> obtenirTous() {
        return requete("SELECT * FROM messages ORDER BY created_at DESC");
    }

    public List<Message> obtenirNonLus() {
        return requete("SELECT * FROM messages WHERE lu = 0 ORDER BY created_at DESC");
    }

    public int compterNonLus() {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM messages WHERE lu = 0")) {
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
        exec("UPDATE messages SET lu = 1");
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
        return new Message(
            rs.getInt("id"), rs.getInt("expediteur_id"),
            rs.getString("expediteur_nom"), rs.getString("expediteur_role"),
            rs.getString("sujet"), rs.getString("corps"), rs.getString("type"),
            rs.getBoolean("lu"), rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
