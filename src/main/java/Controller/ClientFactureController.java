package Controller;

import dao.ContratDAO;
import dao.ClientDao;
import dao.FactureDAO;
import dao.ReservationDAO;
import dao.VehiculeDAO;
import model.Contrat;
import model.Facture;
import model.Reservation;
import model.Client;
import model.Vehicule;
import utils.NotificationManager;
import utils.PDFGenerator;
import utils.SessionManager;
import view.ClientFactureView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientFactureController {

    private ClientFactureView view;
    private FactureDAO factureDAO;
    private ContratDAO contratDAO;
    private ReservationDAO reservationDAO;
    private ClientDao clientDAO;
    private VehiculeDAO vehiculeDAO;
    private int clientId;

    public ClientFactureController(ClientFactureView view) {
        this.view = view;
        this.factureDAO = new FactureDAO();
        this.contratDAO = new ContratDAO();
        this.reservationDAO = new ReservationDAO();
        this.clientDAO = new ClientDao();
        this.vehiculeDAO = new VehiculeDAO();
        this.clientId = SessionManager.getInstance().getCurrentClientId();

        System.out.println("✅ ClientFactureController créé pour client ID: " + clientId);

        initEventHandlers();
        loadFactures();
    }

    private void initEventHandlers() {
        // Recherche
        view.getSearchButton().setOnAction(e -> searchFactures());
        view.getSearchField().setOnAction(e -> searchFactures());

        // Rafraîchir
        view.getRefreshButton().setOnAction(e -> loadFactures());

        // Filtre par statut
        view.getStatutFilterComboBox().setOnAction(e -> filterByStatut());

        // Callback
        view.setCallback(new ClientFactureView.FactureCallback() {
            @Override
            public void onSearch(String keyword) {
                searchFactures();
            }

            @Override
            public void onFilter(String statut) {
                filterByStatut();
            }

            @Override
            public void onRefresh() {
                loadFactures();
            }

            @Override
            public void onTelechargerPDF(Facture facture) {
                telechargerPDF(facture);
            }
        });
    }

    private void loadFactures() {
        // Récupérer toutes les réservations du client
        List<Reservation> reservations = reservationDAO.getByClient(clientId);
        List<Facture> factures = new ArrayList<>();

        // Pour chaque réservation, récupérer le contrat puis la facture associée
        for (Reservation r : reservations) {
            Contrat contrat = contratDAO.getByReservation(r.getIdReservation());
            if (contrat != null) {
                Facture facture = factureDAO.getByContrat(contrat.getIdContrat());
                if (facture != null) {
                    factures.add(facture);
                }
            }
        }

        view.refreshTable(factures);
        updateStats(factures);
        System.out.println("💰 " + factures.size() + " factures chargées pour le client " + clientId);
    }

    private void searchFactures() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadFactures();
        } else {
            // Rechercher dans les factures du client
            List<Reservation> reservations = reservationDAO.search(keyword);
            List<Facture> factures = new ArrayList<>();

            for (Reservation r : reservations) {
                if (r.getIdClient() == clientId) {
                    Contrat contrat = contratDAO.getByReservation(r.getIdReservation());
                    if (contrat != null) {
                        Facture facture = factureDAO.getByContrat(contrat.getIdContrat());
                        if (facture != null) {
                            factures.add(facture);
                        }
                    }
                }
            }

            view.refreshTable(factures);
            updateStats(factures);
        }
    }

    private void filterByStatut() {
        String statut = view.getStatutFilterComboBox().getValue();
        List<Facture> factures = new ArrayList<>();

        if (statut == null || "Tous".equals(statut)) {
            loadFactures();
            return;
        }

        // Récupérer toutes les réservations du client
        List<Reservation> reservations = reservationDAO.getByClient(clientId);

        for (Reservation r : reservations) {
            Contrat contrat = contratDAO.getByReservation(r.getIdReservation());
            if (contrat != null) {
                Facture facture = factureDAO.getByContrat(contrat.getIdContrat());
                if (facture != null && facture.getStatut().equals(statut)) {
                    factures.add(facture);
                }
            }
        }

        view.refreshTable(factures);
        updateStats(factures);
    }

    private void updateStats(List<Facture> factures) {
        int total = factures.size();
        int payees = 0, impayees = 0;

        for (Facture f : factures) {
            if ("PAYEE".equals(f.getStatut())) {
                payees++;
            } else {
                impayees++;
            }
        }

        view.updateStats(total, payees, impayees);
    }

    private void telechargerPDF(Facture facture) {
        if (facture == null) return;

        Contrat contrat = contratDAO.getById(facture.getIdContrat());
        if (contrat == null) {
            NotificationManager.showError("Erreur", "Contrat associé à la facture non trouvé.");
            return;
        }

        Reservation reservation = reservationDAO.getById(contrat.getIdReservation());
        if (reservation == null || reservation.getIdClient() != clientId) {
            NotificationManager.showError("Erreur", "Réservation associée à la facture non trouvée.");
            return;
        }

        Client client = clientDAO.getById(reservation.getIdClient());
        Vehicule vehicule = vehiculeDAO.getById(reservation.getIdVehicule());
        if (client == null || vehicule == null) {
            NotificationManager.showError("Erreur", "Client ou véhicule associé à la facture non trouvé.");
            return;
        }

        try {
            String pdfPath = PDFGenerator.generateFacturePDF(facture, contrat, client, vehicule);
            NotificationManager.showSuccess("PDF généré",
                    "Facture #" + facture.getIdFacture() + " générée.\nFichier : " + pdfPath);
        } catch (IOException | RuntimeException e) {
            NotificationManager.showError("Erreur", "Impossible de générer le PDF : " + e.getMessage());
        }
    }
}
