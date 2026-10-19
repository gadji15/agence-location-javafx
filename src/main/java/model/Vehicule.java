package model;


public class Vehicule {
    private int idVehicule;
    private String marque;
    private String modele;
    private int annee;
    private String immatriculation;
    private String carburant;
    private int kilometrage;
    private double prixJournalier;
    private String statut;
    private Integer idParking;
    private boolean actif;

    // Constructeur par défaut
    public Vehicule() {}

    // Constructeur complet
    public Vehicule(int idVehicule, String marque, String modele, int annee, String immatriculation,
                    String carburant, int kilometrage, double prixJournalier, String statut,
                    Integer idParking, boolean actif) {
        this.idVehicule = idVehicule;
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.immatriculation = immatriculation;
        this.carburant = carburant;
        this.kilometrage = kilometrage;
        this.prixJournalier = prixJournalier;
        this.statut = statut;
        this.idParking = idParking;
        this.actif = actif;
    }

    // Constructeur sans ID (pour création)
    public Vehicule(String marque, String modele, int annee, String immatriculation,
                    String carburant, int kilometrage, double prixJournalier, String statut,
                    Integer idParking, boolean actif) {
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.immatriculation = immatriculation;
        this.carburant = carburant;
        this.kilometrage = kilometrage;
        this.prixJournalier = prixJournalier;
        this.statut = statut;
        this.idParking = idParking;
        this.actif = actif;
    }

    // Getters et Setters
    public int getIdVehicule() { return idVehicule; }
    public void setIdVehicule(int idVehicule) { this.idVehicule = idVehicule; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }

    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }

    public String getCarburant() { return carburant; }
    public void setCarburant(String carburant) { this.carburant = carburant; }

    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int kilometrage) { this.kilometrage = kilometrage; }

    public double getPrixJournalier() { return prixJournalier; }
    public void setPrixJournalier(double prixJournalier) { this.prixJournalier = prixJournalier; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getIdParking() { return idParking; }
    public void setIdParking(Integer idParking) { this.idParking = idParking; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    // Méthode pour obtenir le nom complet du véhicule
    public String getFullName() {
        return marque + " " + modele + " (" + immatriculation + ")";
    }

    @Override
    public String toString() {
        return getFullName();
    }
}