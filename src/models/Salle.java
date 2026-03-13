package models;

public class Salle {
    private int id;
    private String numero;
    private int capacite;
    private String type; // TD, TP, Amphi
    private String batiment;
    private String etage;
    // Équipements
    private boolean videoprojecteur;
    private boolean tableauInteractif;
    private boolean climatisation;

    public Salle() {}

    public Salle(int id, String numero, int capacite, String type, String batiment, String etage,
                 boolean videoprojecteur, boolean tableauInteractif, boolean climatisation) {
        this.id = id;
        this.numero = numero;
        this.capacite = capacite;
        this.type = type;
        this.batiment = batiment;
        this.etage = etage;
        this.videoprojecteur = videoprojecteur;
        this.tableauInteractif = tableauInteractif;
        this.climatisation = climatisation;
    }

    // Ancien constructeur compatible avec le code existant
    public Salle(int id, String numero, int capacite, String type, String batiment, String etage) {
        this(id, numero, capacite, type, batiment, etage, false, false, false);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBatiment() { return batiment; }
    public void setBatiment(String batiment) { this.batiment = batiment; }

    public String getEtage() { return etage; }
    public void setEtage(String etage) { this.etage = etage; }

    public boolean isVideoprojecteur() { return videoprojecteur; }
    public void setVideoprojecteur(boolean videoprojecteur) { this.videoprojecteur = videoprojecteur; }

    public boolean isTableauInteractif() { return tableauInteractif; }
    public void setTableauInteractif(boolean tableauInteractif) { this.tableauInteractif = tableauInteractif; }

    public boolean isClimatisation() { return climatisation; }
    public void setClimatisation(boolean climatisation) { this.climatisation = climatisation; }

    public String getEquipementsStr() {
        StringBuilder sb = new StringBuilder();
        if (videoprojecteur) {
			sb.append("📽 ");
		}
        if (tableauInteractif) {
			sb.append("🖥 ");
		}
        if (climatisation) {
			sb.append("❄ ");
		}
        return sb.length() == 0 ? "Aucun" : sb.toString().trim();
    }

    @Override
    public String toString() {
        return numero + " (" + batiment + " - Capacité: " + capacite + ")";
    }
}
