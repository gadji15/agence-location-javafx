package model;


public class Parking {
    private int idParking;
    private String nom;
    private String adresse;
    private int capacite;
    private boolean actif;

    // Constructeur par défaut
    public Parking() {}

    // Constructeur complet
    public Parking(int idParking, String nom, String adresse, int capacite, boolean actif) {
        this.idParking = idParking;
        this.nom = nom;
        this.adresse = adresse;
        this.capacite = capacite;
        this.actif = actif;
    }

    // Constructeur sans ID (pour création)
    public Parking(String nom, String adresse, int capacite, boolean actif) {
        this.nom = nom;
        this.adresse = adresse;
        this.capacite = capacite;
        this.actif = actif;
    }

    // Getters et Setters
    public int getIdParking() { return idParking; }
    public void setIdParking(int idParking) { this.idParking = idParking; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    // Méthode pour obtenir le nom complet
    public String getFullName() {
        return nom + " (" + capacite + " places)";
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
