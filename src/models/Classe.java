package models;

public class Classe {
    private int id;
    private String nom;
    private String filiere;
    private String niveau;
    private int effectif;

    public Classe() {}
    public Classe(int id, String nom, String filiere, String niveau, int effectif) {
        this.id = id; this.nom = nom; this.filiere = filiere;
        this.niveau = niveau; this.effectif = effectif;
    }

    public int    getId()       { return id; }
    public String getNom()      { return nom; }
    public String getFiliere()  { return filiere != null ? filiere : ""; }
    public String getNiveau()   { return niveau != null ? niveau : ""; }
    public int    getEffectif() { return effectif; }
    public void setId(int id)           { this.id = id; }
    public void setNom(String nom)      { this.nom = nom; }
    public void setFiliere(String f)    { this.filiere = f; }
    public void setNiveau(String n)     { this.niveau = n; }
    public void setEffectif(int e)      { this.effectif = e; }

    @Override public String toString() { return nom + " (" + niveau + ")"; }
}
