package model;

import java.time.LocalDateTime;

public class Client {
    private int idClient;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private LocalDateTime dateInscription;
    private boolean actif;

    // Constructeur par défaut
    public Client() {}

    // Constructeur complet
    public Client(int idClient, String nom, String prenom, String email, String telephone,
                  String adresse, LocalDateTime dateInscription, boolean actif) {
        this.idClient = idClient;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.dateInscription = dateInscription;
        this.actif = actif;
    }

    // Constructeur sans ID (pour création)
    public Client(String nom, String prenom, String email, String telephone,
                  String adresse, boolean actif) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.actif = actif;
        this.dateInscription = LocalDateTime.now();
    }

    // Getters et Setters
    public int getIdClient() { return idClient; }
    public void setIdClient(int idClient) { this.idClient = idClient; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    // Méthode pour obtenir le nom complet
    public String getFullName() {
        return prenom + " " + nom;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
