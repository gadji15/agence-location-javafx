package Controller;


import dao.SanctionDAO;
import model.Sanction;
import utils.SessionManager;
import view.ClientSanctionView;

import java.util.List;

public class ClientSanctionController {

    private ClientSanctionView view;
    private SanctionDAO sanctionDAO;
    private int clientId;

    public ClientSanctionController(ClientSanctionView view) {
        this.view = view;
        this.sanctionDAO = new SanctionDAO();
        this.clientId = SessionManager.getInstance().getCurrentClientId();

        System.out.println("✅ ClientSanctionController créé pour client ID: " + clientId);

        initEventHandlers();
        loadSanctions();
    }

    private void initEventHandlers() {
        // Recherche
        view.getSearchButton().setOnAction(e -> searchSanctions());
        view.getSearchField().setOnAction(e -> searchSanctions());

        // Rafraîchir
        view.getRefreshButton().setOnAction(e -> loadSanctions());

        // Filtre par statut
        view.getStatutFilterComboBox().setOnAction(e -> filterByStatut());

        // Callback
        view.setCallback(new ClientSanctionView.SanctionCallback() {
            @Override
            public void onSearch(String keyword) {
                searchSanctions();
            }

            @Override
            public void onFilter(String statut) {
                filterByStatut();
            }

            @Override
            public void onRefresh() {
                loadSanctions();
            }
        });
    }

    private void loadSanctions() {
        List<Sanction> sanctions = sanctionDAO.getByClient(clientId);
        view.refreshTable(sanctions);
        updateStats(sanctions);
        System.out.println("⚖️ " + sanctions.size() + " sanctions chargées pour le client " + clientId);
    }

    private void searchSanctions() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadSanctions();
        } else {
            List<Sanction> sanctions = sanctionDAO.search(keyword);
            // Filtrer par client
            sanctions.removeIf(s -> s.getIdClient() != clientId);
            view.refreshTable(sanctions);
            updateStats(sanctions);
        }
    }

    private void filterByStatut() {
        String statut = view.getStatutFilterComboBox().getValue();
        List<Sanction> sanctions;

        if (statut == null || "Tous".equals(statut)) {
            sanctions = sanctionDAO.getByClient(clientId);
        } else {
            sanctions = sanctionDAO.getByStatut(statut);
            // Filtrer par client
            sanctions.removeIf(s -> s.getIdClient() != clientId);
        }

        view.refreshTable(sanctions);
        updateStats(sanctions);
    }

    private void updateStats(List<Sanction> sanctions) {
        int total = sanctions.size();
        int actives = 0, levees = 0, reduites = 0;

        for (Sanction s : sanctions) {
            switch (s.getStatut()) {
                case "ACTIVE" -> actives++;
                case "LEVEE" -> levees++;
                case "REDUITE" -> reduites++;
            }
        }

        view.updateStats(total, actives, levees, reduites);
    }
}
