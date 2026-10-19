package Controller;


import dao.ContratDAO;
import dao.ReservationDAO;
import model.Contrat;
import model.Reservation;
import utils.SessionManager;
import view.ClientContratView;

import java.util.ArrayList;
import java.util.List;

public class ClientContratController {

    private ClientContratView view;
    private ContratDAO contratDAO;
    private ReservationDAO reservationDAO;
    private int clientId;

    public ClientContratController(ClientContratView view) {
        this.view = view;
        this.contratDAO = new ContratDAO();
        this.reservationDAO = new ReservationDAO();
        this.clientId = SessionManager.getInstance().getCurrentClientId();

        System.out.println("✅ ClientContratController créé pour client ID: " + clientId);

        initEventHandlers();
        loadContrats();
    }

    private void initEventHandlers() {
        // Recherche
        view.getSearchButton().setOnAction(e -> searchContrats());
        view.getSearchField().setOnAction(e -> searchContrats());

        // Rafraîchir
        view.getRefreshButton().setOnAction(e -> loadContrats());

        // Filtre par statut
        view.getStatutFilterComboBox().setOnAction(e -> filterByStatut());

        // Callback
        view.setCallback(new ClientContratView.ContratCallback() {
            @Override
            public void onSearch(String keyword) {
                searchContrats();
            }

            @Override
            public void onFilter(String statut) {
                filterByStatut();
            }

            @Override
            public void onRefresh() {
                loadContrats();
            }
        });
    }

    private void loadContrats() {
        // Récupérer toutes les réservations du client
        List<Reservation> reservations = reservationDAO.getByClient(clientId);
        List<Contrat> contrats = new ArrayList<>();

        // Pour chaque réservation, récupérer le contrat associé
        for (Reservation r : reservations) {
            Contrat contrat = contratDAO.getByReservation(r.getIdReservation());
            if (contrat != null) {
                contrats.add(contrat);
            }
        }

        view.refreshTable(contrats);
        updateStats(contrats);
        System.out.println("📄 " + contrats.size() + " contrats chargés pour le client " + clientId);
    }

    private void searchContrats() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadContrats();
        } else {
            // Rechercher dans les contrats du client
            List<Reservation> reservations = reservationDAO.search(keyword);
            List<Contrat> contrats = new ArrayList<>();

            for (Reservation r : reservations) {
                if (r.getIdClient() == clientId) {
                    Contrat contrat = contratDAO.getByReservation(r.getIdReservation());
                    if (contrat != null) {
                        contrats.add(contrat);
                    }
                }
            }

            view.refreshTable(contrats);
            updateStats(contrats);
        }
    }

    private void filterByStatut() {
        String statut = view.getStatutFilterComboBox().getValue();
        List<Contrat> contrats = new ArrayList<>();

        if (statut == null || "Tous".equals(statut)) {
            loadContrats();
            return;
        }

        // Récupérer toutes les réservations du client
        List<Reservation> reservations = reservationDAO.getByClient(clientId);

        for (Reservation r : reservations) {
            Contrat contrat = contratDAO.getByReservation(r.getIdReservation());
            if (contrat != null) {
                if ("Actifs".equals(statut) && contrat.isActif()) {
                    contrats.add(contrat);
                } else if ("Clôturés".equals(statut) && !contrat.isActif()) {
                    contrats.add(contrat);
                }
            }
        }

        view.refreshTable(contrats);
        updateStats(contrats);
    }

    private void updateStats(List<Contrat> contrats) {
        int total = contrats.size();
        int actifs = 0, clotures = 0;

        for (Contrat c : contrats) {
            if (c.isActif()) {
                actifs++;
            } else {
                clotures++;
            }
        }

        view.updateStats(total, actifs, clotures);
    }
}
