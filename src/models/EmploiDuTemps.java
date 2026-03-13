package models;

import java.time.LocalTime;

public class EmploiDuTemps {
    private int id;
    private String classe;
    private String matiere;
    private String enseignant;
    private int salleId;
    private int jourSemaine; // 1=Lundi ... 6=Samedi
    private LocalTime heureDebut;
    private int duree; // minutes
    private String typeCours; // CM, TD, TP

    public EmploiDuTemps() {}

    public EmploiDuTemps(int id, String classe, String matiere, String enseignant,
                          int salleId, int jourSemaine, LocalTime heureDebut,
                          int duree, String typeCours) {
        this.id = id;
        this.classe = classe;
        this.matiere = matiere;
        this.enseignant = enseignant;
        this.salleId = salleId;
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.duree = duree;
        this.typeCours = typeCours;
    }

    public LocalTime getHeureFin() {
        return heureDebut != null ? heureDebut.plusMinutes(duree) : null;
    }

    public String getNomJour() {
        String[] jours = {"", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
        return (jourSemaine >= 1 && jourSemaine <= 6) ? jours[jourSemaine] : "?";
    }

    public int getId()            { return id; }
    public String getClasse()     { return classe; }
    public String getMatiere()    { return matiere; }
    public String getEnseignant() { return enseignant; }
    public int getSalleId()       { return salleId; }
    public int getJourSemaine()   { return jourSemaine; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public int getDuree()         { return duree; }
    public String getTypeCours()  { return typeCours != null ? typeCours : "CM"; }

    public void setId(int id)                   { this.id = id; }
    public void setClasse(String classe)         { this.classe = classe; }
    public void setMatiere(String matiere)       { this.matiere = matiere; }
    public void setEnseignant(String ens)        { this.enseignant = ens; }
    public void setSalleId(int salleId)          { this.salleId = salleId; }
    public void setJourSemaine(int j)            { this.jourSemaine = j; }
    public void setHeureDebut(LocalTime h)       { this.heureDebut = h; }
    public void setDuree(int duree)              { this.duree = duree; }
    public void setTypeCours(String t)           { this.typeCours = t; }
}
