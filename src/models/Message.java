package models;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int expediteurId;
    private String expediteurNom;
    private String expediteurRole;
    private String sujet;
    private String corps;
    private String type; // RESERVATION, RECLAMATION, GENERAL
    private boolean lu;
    private LocalDateTime createdAt;

    public Message() {}

    public Message(int id, int expediteurId, String expediteurNom, String expediteurRole,
                   String sujet, String corps, String type, boolean lu, LocalDateTime createdAt) {
        this.id = id; this.expediteurId = expediteurId; this.expediteurNom = expediteurNom;
        this.expediteurRole = expediteurRole; this.sujet = sujet; this.corps = corps;
        this.type = type; this.lu = lu; this.createdAt = createdAt;
    }

    public int getId()                { return id; }
    public int getExpediteurId()      { return expediteurId; }
    public String getExpediteurNom()  { return expediteurNom; }
    public String getExpediteurRole() { return expediteurRole; }
    public String getSujet()          { return sujet; }
    public String getCorps()          { return corps; }
    public String getType()           { return type != null ? type : "GENERAL"; }
    public boolean isLu()             { return lu; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setLu(boolean lu)     { this.lu = lu; }
    public String getTypeLabel() {
        switch (getType()) {
            case "RESERVATION": return "📅 Réservation";
            case "RECLAMATION": return "⚠️ Réclamation";
            default:            return "💬 Message";
        }
    }
}
