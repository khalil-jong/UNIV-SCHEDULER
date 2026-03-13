package models;

import java.time.LocalDateTime;

public class Cours {
    private int id;
    private String matiere;
    private String enseignant;
    private String classe;
    private String groupe;          // Groupe A / Groupe B
    private LocalDateTime dateDebut;
    private int duree;              // en minutes
    private int salleId;

    public Cours() {}

    // Constructeur complet (avec groupe)
    public Cours(int id, String matiere, String enseignant, String classe, String groupe,
                 LocalDateTime dateDebut, int duree, int salleId) {
        this.id = id;
        this.matiere = matiere;
        this.enseignant = enseignant;
        this.classe = classe;
        this.groupe = groupe;
        this.dateDebut = dateDebut;
        this.duree = duree;
        this.salleId = salleId;
    }

    // Constructeur rétrocompatible (sans groupe)
    public Cours(int id, String matiere, String enseignant, String classe,
                 LocalDateTime dateDebut, int duree, int salleId) {
        this(id, matiere, enseignant, classe, "", dateDebut, duree, salleId);
    }

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getMatiere()            { return matiere; }
    public void setMatiere(String m)      { this.matiere = m; }

    public String getEnseignant()         { return enseignant; }
    public void setEnseignant(String e)   { this.enseignant = e; }

    public String getClasse()             { return classe; }
    public void setClasse(String c)       { this.classe = c; }

    public String getGroupe()             { return groupe != null ? groupe : ""; }
    public void setGroupe(String g)       { this.groupe = g; }

    public LocalDateTime getDateDebut()   { return dateDebut; }
    public void setDateDebut(LocalDateTime d) { this.dateDebut = d; }

    public int getDuree()                 { return duree; }
    public void setDuree(int d)           { this.duree = d; }

    public int getSalleId()               { return salleId; }
    public void setSalleId(int s)         { this.salleId = s; }

    public LocalDateTime getDateFin() {
        return dateDebut != null ? dateDebut.plusMinutes(duree) : null;
    }

    @Override
    public String toString() {
        return matiere + " - " + enseignant + " (" + classe + ")";
    }
}
