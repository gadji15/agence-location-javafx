package model;

import java.time.LocalDateTime;

public class Reservation {
    private int idReservation;
    private int idClient;
    private int idVehicule;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDateTime dateDemande;
    private String statut;
    private String lieuPriseEnCharge;
    private String lieuRetour;

    // Constructeur par défaut
    public Reservation() {}

    // Constructeur complet
    public Reservation(int idReservation, int idClient, int idVehicule, LocalDateTime dateDebut,
                       LocalDateTime dateFin, LocalDateTime dateDemande, String statut,
                       String lieuPriseEnCharge, String lieuRetour) {
        this.idReservation = idReservation;
        this.idClient = idClient;
        this.idVehicule = idVehicule;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.dateDemande = dateDemande;
        this.statut = statut;
        this.lieuPriseEnCharge = lieuPriseEnCharge;
        this.lieuRetour = lieuRetour;
    }

    // Constructeur sans ID (pour création)
    public Reservation(int idClient, int idVehicule, LocalDateTime dateDebut,
                       LocalDateTime dateFin, String statut,
                       String lieuPriseEnCharge, String lieuRetour) {
        this.idClient = idClient;
        this.idVehicule = idVehicule;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.lieuPriseEnCharge = lieuPriseEnCharge;
        this.lieuRetour = lieuRetour;
        this.dateDemande = LocalDateTime.now();
    }

    // Getters et Setters
    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public int getIdClient() { return idClient; }
    public void setIdClient(int idClient) { this.idClient = idClient; }

    public int getIdVehicule() { return idVehicule; }
    public void setIdVehicule(int idVehicule) { this.idVehicule = idVehicule; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getLieuPriseEnCharge() { return lieuPriseEnCharge; }
    public void setLieuPriseEnCharge(String lieuPriseEnCharge) { this.lieuPriseEnCharge = lieuPriseEnCharge; }

    public String getLieuRetour() { return lieuRetour; }
    public void setLieuRetour(String lieuRetour) { this.lieuRetour = lieuRetour; }

    @Override
    public String toString() {
        return "Réservation #" + idReservation + " - Client: " + idClient + " - Véhicule: " + idVehicule;
    }
}