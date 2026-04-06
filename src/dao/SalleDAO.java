package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;
import models.Salle;

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
            while (rs.next()) {
				salles.add(mapper(rs));
			}
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
                if (rs.next()) {
					return mapper(rs);
				}
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
        return null;
    }


    /** Salles d'un bâtiment donné, triées par numéro */
    public List<Salle> obtenirParBatiment(String nomBatiment) {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT * FROM salles WHERE batiment = ? ORDER BY numero";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomBatiment);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
					salles.add(mapper(rs));
				}
            }
        } catch (SQLException e) { System.err.println("Salles/bâtiment: " + e.getMessage()); }
        return salles;
    }

    /** Numéros de salles existants pour un bâtiment (pour vérification doublon) */
    public List<String> obtenirNumerosPourBatiment(String nomBatiment) {
        List<String> nums = new ArrayList<>();
        String sql = "SELECT numero FROM salles WHERE batiment = ? ORDER BY numero";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomBatiment);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
					nums.add(rs.getString("numero"));
				}
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return nums;
    }

    public List<Salle> rechercherParCapacite(int capaciteMin) {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT * FROM salles WHERE capacite >= ? ORDER BY capacite ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, capaciteMin);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
					salles.add(mapper(rs));
				}
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
        if (type != null && !type.equals("Tous")) {
			sql.append(" AND type = ?");
		}
        if (videoprojecteur) {
			sql.append(" AND videoprojecteur = TRUE");
		}
        if (tableauInteractif) {
			sql.append(" AND tableau_interactif = TRUE");
		}
        if (climatisation) {
			sql.append(" AND climatisation = TRUE");
		}
        sql.append(" ORDER BY capacite ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            pstmt.setInt(idx++, capaciteMin);
            if (type != null && !type.equals("Tous")) {
				pstmt.setString(idx++, type);
			}
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
					salles.add(mapper(rs));
				}
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche: " + e.getMessage());
        }
        return salles;
    }

    // Salles disponibles sur un créneau donné :
    // vérifie AUSSI les créneaux de l'emploi_du_temps (jour de semaine + heure)
    public List<Salle> obtenirSallesDisponibles(java.time.LocalDateTime debut, int dureeMinutes) {
        List<Salle> salles = new ArrayList<>();
        // Exclure les salles occupées par un cours ponctuel
        // ET par un créneau EDT qui chevauche le créneau demandé
        int jourSemaine = debut.getDayOfWeek().getValue(); // 1=Lun..6=Sam
        java.time.LocalTime hDebut = debut.toLocalTime();
        java.time.LocalTime hFin   = hDebut.plusMinutes(dureeMinutes);
        String sql =
            "SELECT * FROM salles WHERE id NOT IN (" +
            "  SELECT salle_id FROM cours " +
            "  WHERE date_debut < ? AND DATE_ADD(date_debut, INTERVAL duree MINUTE) > ?" +
            "  UNION " +
            "  SELECT salle_id FROM emploi_du_temps " +
            "  WHERE actif = 1 AND jour_semaine = ? " +
            "  AND heure_debut < ? AND ADDTIME(heure_debut, SEC_TO_TIME(duree*60)) > ?" +
            ") ORDER BY batiment, numero";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            java.time.LocalDateTime fin = debut.plusMinutes(dureeMinutes);
            ps.setTimestamp(1, Timestamp.valueOf(fin));
            ps.setTimestamp(2, Timestamp.valueOf(debut));
            ps.setInt(3, jourSemaine);
            ps.setTime(4, java.sql.Time.valueOf(hFin));
            ps.setTime(5, java.sql.Time.valueOf(hDebut));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
					salles.add(mapper(rs));
				}
            }
        } catch (SQLException e) {
            System.err.println("Erreur disponibilité: " + e.getMessage());
        }
        return salles;
    }

    // Vérifier si une salle est occupée à un instant donné (temps réel)
    public boolean estOccupeeMaintenantET(int salleId, java.time.LocalDateTime maintenant) {
        int jour = maintenant.getDayOfWeek().getValue();
        java.time.LocalTime heure = maintenant.toLocalTime();
        // Cours ponctuel en cours
        String sql1 = "SELECT COUNT(*) FROM cours WHERE salle_id = ? " +
            "AND date_debut <= ? AND DATE_ADD(date_debut, INTERVAL duree MINUTE) > ?";
        // Créneau EDT en cours
        String sql2 = "SELECT COUNT(*) FROM emploi_du_temps WHERE salle_id = ? AND actif = 1 " +
            "AND jour_semaine = ? AND heure_debut <= ? AND ADDTIME(heure_debut, SEC_TO_TIME(duree*60)) > ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setInt(1, salleId);
                ps.setTimestamp(2, Timestamp.valueOf(maintenant));
                ps.setTimestamp(3, Timestamp.valueOf(maintenant));
                try (ResultSet rs = ps.executeQuery()) { if (rs.next() && rs.getInt(1) > 0) {
					return true;
				} }
            }
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setInt(1, salleId);
                ps.setInt(2, jour);
                ps.setTime(3, java.sql.Time.valueOf(heure));
                ps.setTime(4, java.sql.Time.valueOf(heure));
                try (ResultSet rs = ps.executeQuery()) { if (rs.next() && rs.getInt(1) > 0) {
					return true;
				} }
            }
        } catch (SQLException e) { System.err.println("Erreur statut: " + e.getMessage()); }
        return false;
    }

    // Récupérer le cours/créneau en cours dans une salle
    public String getOccupantActuel(int salleId, java.time.LocalDateTime maintenant) {
        int jour = maintenant.getDayOfWeek().getValue();
        java.time.LocalTime heure = maintenant.toLocalTime();
        String sql1 = "SELECT matiere, enseignant, classe, " +
            "DATE_FORMAT(date_debut,'%H:%i') as h_debut, " +
            "DATE_FORMAT(DATE_ADD(date_debut, INTERVAL duree MINUTE),'%H:%i') as h_fin " +
            "FROM cours WHERE salle_id = ? " +
            "AND date_debut <= ? AND DATE_ADD(date_debut, INTERVAL duree MINUTE) > ? LIMIT 1";
        String sql2 = "SELECT matiere, enseignant, classe, " +
            "TIME_FORMAT(heure_debut,'%H:%i') as h_debut, " +
            "TIME_FORMAT(ADDTIME(heure_debut,SEC_TO_TIME(duree*60)),'%H:%i') as h_fin " +
            "FROM emploi_du_temps WHERE salle_id = ? AND actif = 1 AND jour_semaine = ? " +
            "AND heure_debut <= ? AND ADDTIME(heure_debut,SEC_TO_TIME(duree*60)) > ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setInt(1, salleId);
                ps.setTimestamp(2, Timestamp.valueOf(maintenant));
                ps.setTimestamp(3, Timestamp.valueOf(maintenant));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
						return rs.getString("matiere") + " – " + rs.getString("enseignant")
						    + " / " + rs.getString("classe")
						    + "  (" + rs.getString("h_debut") + "→" + rs.getString("h_fin") + ")";
					}
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setInt(1, salleId);
                ps.setInt(2, jour);
                ps.setTime(3, java.sql.Time.valueOf(heure));
                ps.setTime(4, java.sql.Time.valueOf(heure));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
						return rs.getString("matiere") + " – " + rs.getString("enseignant")
						    + " / " + rs.getString("classe")
						    + "  (" + rs.getString("h_debut") + "→" + rs.getString("h_fin") + ") [EDT]";
					}
                }
            }
        } catch (SQLException e) { System.err.println("Erreur occupant: " + e.getMessage()); }
        return "";
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
