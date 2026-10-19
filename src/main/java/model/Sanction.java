package model;

import java.time.LocalDateTime;

public class Sanction {
    private int idSanction;
    private int idClient;
    private Integer idContrat;
    private Integer idFacture;     // ⬅️ NOUVEAU
    private String type;
    private double montant;
    private String description;
    private LocalDateTime dateEnregistrement;
    private String statut;

    public Sanction() {}

    public Sanction(int idSanction, int idClient, Integer idContrat, Integer idFacture,
                    String type, double montant, String description,
                    LocalDateTime dateEnregistrement, String statut) {
        this.idSanction = idSanction;
        this.idClient = idClient;
        this.idContrat = idContrat;
        this.idFacture = idFacture;
        this.type = type;
        this.montant = montant;
        this.description = description;
        this.dateEnregistrement = dateEnregistrement;
        this.statut = statut;
    }

    public Sanction(int idClient, Integer idContrat, String type,
                    double montant, String description, String statut) {
        this.idClient = idClient;
        this.idContrat = idContrat;
        this.type = type;
        this.montant = montant;
        this.description = description;
        this.statut = statut;
        this.dateEnregistrement = LocalDateTime.now();
    }

    // Getters / Setters
    public int getIdSanction() { return idSanction; }
    public void setIdSanction(int idSanction) { this.idSanction = idSanction; }

    public int getIdClient() { return idClient; }
    public void setIdClient(int idClient) { this.idClient = idClient; }

    public Integer getIdContrat() { return idContrat; }
    public void setIdContrat(Integer idContrat) { this.idContrat = idContrat; }

    public Integer getIdFacture() { return idFacture; }          // ⬅️ NOUVEAU
    public void setIdFacture(Integer idFacture) { this.idFacture = idFacture; }  // ⬅️ NOUVEAU

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateEnregistrement() { return dateEnregistrement; }
    public void setDateEnregistrement(LocalDateTime dateEnregistrement) { this.dateEnregistrement = dateEnregistrement; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Sanction #" + idSanction + " - " + type + " - " + montant + " FCFA";
    }
}