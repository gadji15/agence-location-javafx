package model;


import java.time.LocalDateTime;

public class Contrat {
    private int idContrat;
    private int idReservation;
    private LocalDateTime dateSignature;
    private LocalDateTime dateDebutEffective;
    private LocalDateTime dateFinEffective;
    private String etatRetour;
    private int kilometrageFinal;
    private double montantTotal;
    private double montantProvisoire;   // ← NOUVEAU
    private boolean actif;

    // ===== CHAMPS POUR LE CALCUL =====
    private int nombreJours;
    private double prixJournalier;
    private double sousTotal;
    private double fraisRetourDiffere;
    private double fraisCarburant;
    private double fraisRetard;
    private double fraisDegradation;
    private double remise;

    // Constructeur par défaut
    public Contrat() {}

    // Constructeur complet
    public Contrat(int idContrat, int idReservation, LocalDateTime dateSignature,
                   LocalDateTime dateDebutEffective, LocalDateTime dateFinEffective,
                   String etatRetour, int kilometrageFinal, double montantTotal, boolean actif) {
        this.idContrat = idContrat;
        this.idReservation = idReservation;
        this.dateSignature = dateSignature;
        this.dateDebutEffective = dateDebutEffective;
        this.dateFinEffective = dateFinEffective;
        this.etatRetour = etatRetour;
        this.kilometrageFinal = kilometrageFinal;
        this.montantTotal = montantTotal;
        this.actif = actif;
    }

    // Constructeur sans ID
    public Contrat(int idReservation, LocalDateTime dateSignature,
                   LocalDateTime dateDebutEffective, LocalDateTime dateFinEffective,
                   String etatRetour, int kilometrageFinal, double montantTotal, boolean actif) {
        this.idReservation = idReservation;
        this.dateSignature = dateSignature;
        this.dateDebutEffective = dateDebutEffective;
        this.dateFinEffective = dateFinEffective;
        this.etatRetour = etatRetour;
        this.kilometrageFinal = kilometrageFinal;
        this.montantTotal = montantTotal;
        this.actif = actif;
    }

    // Getters et Setters
    public int getIdContrat() { return idContrat; }
    public void setIdContrat(int idContrat) { this.idContrat = idContrat; }

    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public LocalDateTime getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDateTime dateSignature) { this.dateSignature = dateSignature; }

    public LocalDateTime getDateDebutEffective() { return dateDebutEffective; }
    public void setDateDebutEffective(LocalDateTime dateDebutEffective) { this.dateDebutEffective = dateDebutEffective; }

    public LocalDateTime getDateFinEffective() { return dateFinEffective; }
    public void setDateFinEffective(LocalDateTime dateFinEffective) { this.dateFinEffective = dateFinEffective; }

    public String getEtatRetour() { return etatRetour; }
    public void setEtatRetour(String etatRetour) { this.etatRetour = etatRetour; }

    public int getKilometrageFinal() { return kilometrageFinal; }
    public void setKilometrageFinal(int kilometrageFinal) { this.kilometrageFinal = kilometrageFinal; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }

    // ===== NOUVEAU CHAMP =====
    public double getMontantProvisoire() { return montantProvisoire; }
    public void setMontantProvisoire(double montantProvisoire) { this.montantProvisoire = montantProvisoire; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    // ===== GETTERS ET SETTERS POUR LES CHAMPS DE CALCUL =====
    public int getNombreJours() { return nombreJours; }
    public void setNombreJours(int nombreJours) { this.nombreJours = nombreJours; }

    public double getPrixJournalier() { return prixJournalier; }
    public void setPrixJournalier(double prixJournalier) { this.prixJournalier = prixJournalier; }

    public double getSousTotal() { return sousTotal; }
    public void setSousTotal(double sousTotal) { this.sousTotal = sousTotal; }

    public double getFraisRetourDiffere() { return fraisRetourDiffere; }
    public void setFraisRetourDiffere(double fraisRetourDiffere) { this.fraisRetourDiffere = fraisRetourDiffere; }

    public double getFraisCarburant() { return fraisCarburant; }
    public void setFraisCarburant(double fraisCarburant) { this.fraisCarburant = fraisCarburant; }

    public double getFraisRetard() { return fraisRetard; }
    public void setFraisRetard(double fraisRetard) { this.fraisRetard = fraisRetard; }

    public double getFraisDegradation() { return fraisDegradation; }
    public void setFraisDegradation(double fraisDegradation) { this.fraisDegradation = fraisDegradation; }

    public double getRemise() { return remise; }
    public void setRemise(double remise) { this.remise = remise; }

    @Override
    public String toString() {
        return "Contrat #" + idContrat + " - Réservation #" + idReservation;
    }
}