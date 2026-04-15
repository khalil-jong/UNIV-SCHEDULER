package models;

public class Utilisateur {
    private int id;
    private String nom;
    private String prenom;
    private String login;
    private String motDePasse;
    private String role;   // ADMIN, GESTIONNAIRE, ENSEIGNANT, ETUDIANT
    private String classe; // Classe de l'étudiant (null pour les autres rôles)

    public Utilisateur() {}

    /** Constructeur rétrocompatible sans classe */
    public Utilisateur(int id, String nom, String prenom, String login, String motDePasse, String role) {
        this(id, nom, prenom, login, motDePasse, role, null);
    }

    /** Constructeur complet avec classe */
    public Utilisateur(int id, String nom, String prenom, String login, String motDePasse, String role, String classe) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.login = login;
        this.motDePasse = motDePasse;
        this.role = role;
        this.classe = classe;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    /** Classe de l'étudiant — null pour Admin, Gestionnaire, Enseignant */
    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    /** Indique si cet utilisateur est un étudiant affecté à une classe */
    public boolean hasClasse() { return classe != null && !classe.isEmpty(); }

    public String getNomComplet() { return prenom + " " + nom; }

    @Override
    public String toString() { return getNomComplet() + " (" + role + ")"; }
}
