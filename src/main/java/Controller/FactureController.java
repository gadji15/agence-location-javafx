package Controller;

import dao.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.*;
import utils.FormatUtil;
import utils.NotificationManager;
import utils.PDFGenerator;
import view.FactureManagementView;
import view.PaiementDialog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class FactureController {

    private FactureManagementView view;
    private FactureDAO factureDAO;
    private ContratDAO contratDAO;
    private ClientDao clientDAO;
    private VehiculeDAO vehiculeDAO;
    private PaiementDAO paiementDAO;
    private SanctionDAO sanctionDAO;   // ✅ NOUVEAU

    public FactureController(FactureManagementView view) {
        this.view = view;
        this.factureDAO = new FactureDAO();
        this.contratDAO = new ContratDAO();
        this.clientDAO = new ClientDao();
        this.vehiculeDAO = new VehiculeDAO();
        this.paiementDAO = new PaiementDAO();
        this.sanctionDAO = new SanctionDAO();   // ✅ NOUVEAU

        view.setCallback(new FactureManagementView.FactureCallback() {
            @Override
            public void onMarquerPayee(Facture facture) {
                ouvrirDialoguePaiement(facture);
            }

            @Override
            public void onMarquerImpayee(Facture facture) {
                marquerImpayee(facture);
            }

            @Override
            public void onTelechargerPDF(Facture facture) {
                telechargerPDF(facture);
            }

            @Override
            public void onDelete(Facture facture) {
                deleteFacture(facture);
            }
        });

        initEventHandlers();
        loadFactures();
    }

    private void initEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchFactures());
        view.getSearchField().setOnAction(e -> searchFactures());
        view.getRefreshButton().setOnAction(e -> loadFactures());
        view.getStatutFilterComboBox().setOnAction(e -> filterByStatut());
    }

    private void loadFactures() {
        List<Facture> factures = factureDAO.getAll();
        view.refreshTable(factures);
        updateStats(factures);
    }

    private void searchFactures() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadFactures();
        } else {
            List<Facture> factures = factureDAO.search(keyword);
            view.refreshTable(factures);
            updateStats(factures);
        }
    }

    private void filterByStatut() {
        String statut = view.getStatutFilterComboBox().getValue();
        List<Facture> factures;

        if (statut == null || "Tous".equals(statut)) {
            factures = factureDAO.getAll();
        } else {
            factures = factureDAO.getByStatut(statut);
        }

        view.refreshTable(factures);
        updateStats(factures);
    }

    // ===== OUVRIR LE DIALOGUE DE PAIEMENT =====
    private void ouvrirDialoguePaiement(Facture facture) {
        System.out.println(">>> ouvrirDialoguePaiement appelé pour facture #" +
                (facture != null ? facture.getIdFacture() : "null"));

        if (facture == null) return;

        if ("PAYEE".equals(facture.getStatut())) {
            NotificationManager.showInfo("ℹ️ Information", "Cette facture est déjà payée !");
            return;
        }

        try {
            PaiementDialog dialog = new PaiementDialog(facture);
            dialog.showAndWait();

            if (!dialog.isValide()) {
                System.out.println(">>> Dialogue annulé par l'utilisateur");
                return;
            }

            Paiement paiement = dialog.getPaiement();

            // ===== DEBUG : tout afficher AVANT l'appel DAO =====
            System.out.println(">>> === DÉBUT PAIEMENT ===");
            System.out.println("    idFacture        = " + paiement.getIdFacture());
            System.out.println("    mode             = " + paiement.getMode());
            System.out.println("    montantPaye      = " + paiement.getMontantPaye());
            System.out.println("    devise           = " + paiement.getDevise());
            System.out.println("    datePaiement     = " + paiement.getDatePaiement());
            System.out.println("    referenceTrans.  = " + paiement.getReferenceTransaction());
            System.out.println("    notes            = " + paiement.getNotes());

            // ===== ÉTAPE 1 : créer le paiement =====
            boolean paiementCree = paiementDAO.create(paiement);
            System.out.println(">>> paiementDAO.create() → " + paiementCree);

            if (!paiementCree) {
                System.err.println("❌ ÉCHEC : paiementDAO.create() a retourné false");
                NotificationManager.showError("❌ Erreur",
                        "Erreur lors de l'enregistrement du paiement.\n" +
                                "👉 Regarde la console pour la vraie erreur SQL.");
                return;
            }

            System.out.println(">>> Paiement créé, idPaiement = " + paiement.getIdPaiement());

            // ===== ÉTAPE 2 : mettre à jour la facture =====
            boolean factureMaj = factureDAO.ajouterPaiement(
                    facture.getIdFacture(),
                    paiement.getMontantPaye());
            System.out.println(">>> factureDAO.ajouterPaiement() → " + factureMaj);

            if (!factureMaj) {
                System.err.println("❌ ÉCHEC : factureDAO.ajouterPaiement() a retourné false");
                NotificationManager.showError("❌ Erreur",
                        "Erreur lors de la mise à jour de la facture.\n" +
                                "👉 Regarde la console pour la vraie erreur SQL.");
                return;
            }

            // ===== ÉTAPE 3 : recharger la facture =====
            Facture factureUpdated = factureDAO.getById(facture.getIdFacture());
            String statut = factureUpdated.getStatut();
            System.out.println(">>> Nouveau statut facture = " + statut);

            // ===== ÉTAPE 4 : si payée en totalité, lever les sanctions =====
            if ("PAYEE".equals(statut)) {
                boolean ok = sanctionDAO.marquerLeveeByFacture(facture.getIdFacture());
                System.out.println(">>> marquerLeveeByFacture() → " + ok);
            }

            // ===== ÉTAPE 5 : succès =====
            String message = "Paiement enregistré avec succès !\n" +
                    "Montant : " + FormatUtil.formatFCFA(paiement.getMontantPaye()) + "\n" +
                    "Mode : " + paiement.getMode() + "\n" +
                    "Statut facture : " + statut;

            NotificationManager.showSuccess("✅ Paiement enregistré", message);
            loadFactures();

        } catch (Exception ex) {
            System.err.println("❌ EXCEPTION dans ouvrirDialoguePaiement :");
            ex.printStackTrace();
            NotificationManager.showError("❌ Exception",
                    ex.getClass().getSimpleName() + " : " + ex.getMessage());
        }
    }
    private void marquerImpayee(Facture facture) {
        if (facture == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Marquer la facture #" + facture.getIdFacture() + " comme impayée ?");
        alert.setContentText("Cette action réinitialisera les paiements.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (factureDAO.marquerImpayee(facture.getIdFacture())) {
                NotificationManager.showInfo("❌ Impayée",
                        "Facture #" + facture.getIdFacture() + " marquée comme impayée.");
                loadFactures();
            } else {
                NotificationManager.showError("❌ Erreur", "Erreur lors du marquage.");
            }
        }
    }

    private void telechargerPDF(Facture facture) {
        if (facture == null) return;

        Contrat contrat = contratDAO.getById(facture.getIdContrat());
        if (contrat == null) {
            NotificationManager.showError("❌ Erreur", "Contrat non trouvé !");
            return;
        }

        Client client = clientDAO.getById(1);
        Vehicule vehicule = vehiculeDAO.getById(1);

        String pdfPath = PDFGenerator.generateFacturePDF(facture, contrat, client, vehicule);

        if (pdfPath != null && !pdfPath.isEmpty()) {
            NotificationManager.showSuccess("📄 PDF généré",
                    "Facture #" + facture.getIdFacture() + " générée !\n" +
                            "Montant : " + FormatUtil.formatFCFA(facture.getMontantTotal()));
        } else {
            NotificationManager.showError("❌ Erreur",
                    "Erreur lors de la génération du PDF.");
        }
    }

    private void deleteFacture(Facture facture) {
        if (facture == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la facture #" + facture.getIdFacture() + " ?");
        alert.setContentText("Cette action est définitive et irréversible !");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (factureDAO.delete(facture.getIdFacture())) {
                NotificationManager.showSuccess("🗑️ Supprimée",
                        "Facture #" + facture.getIdFacture() + " supprimée !");
                loadFactures();
            } else {
                NotificationManager.showError("❌ Erreur",
                        "Erreur lors de la suppression.");
            }
        }
    }

    private void updateStats(List<Facture> factures) {
        int total = factures.size();
        int payees = 0, partielles = 0, impayees = 0;
        double montantTotal = 0, montantPaye = 0, resteAPayer = 0;

        for (Facture f : factures) {
            montantTotal += f.getMontantTotal();
            montantPaye += f.getMontantPaye();
            resteAPayer += f.getResteAPayer();

            switch (f.getStatut()) {
                case "PAYEE" -> payees++;
                case "PARTIELLE" -> partielles++;
                default -> impayees++;
            }
        }

        view.updateStats(total, payees, partielles, impayees,
                montantTotal, montantPaye, resteAPayer);
    }
}