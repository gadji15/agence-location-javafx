package Controller;

import dao.ContratDAO;
import dao.FactureDAO;
import dao.ReservationDAO;
import javafx.scene.control.Alert;
import model.Contrat;
import model.Facture;
import model.Reservation;
import utils.SessionManager;
import view.ClientFactureView;

import java.util.ArrayList;
import java.util.List;

public class ClientFactureController {

    private ClientFactureView view;
    private FactureDAO factureDAO;
    private ContratDAO contratDAO;
    private ReservationDAO reservationDAO;
    private int clientId;

    public ClientFactureController(ClientFactureView view) {
        this.view = view;
        this.factureDAO = new FactureDAO();
        this.contratDAO = new ContratDAO();
        this.reservationDAO = new ReservationDAO();
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

        // TODO: Générer et télécharger le PDF
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Téléchargement PDF");
        alert.setHeaderText("Facture #" + facture.getIdFacture());
        alert.setContentText(
                "📄 Génération du PDF en cours...\n\n" +
                        "🔹 Facture: #" + facture.getIdFacture() + "\n" +
                        "🔹 Contrat: #" + facture.getIdContrat() + "\n" +
                        "🔹 Montant: " + String.format("%.2f €", facture.getMontantTotal()) + "\n" +
                        "🔹 Statut: " + facture.getStatut() + "\n\n" +
                        "🔜 Fonctionnalité à implémenter : Génération PDF"
        );
        alert.showAndWait();
    }
}
