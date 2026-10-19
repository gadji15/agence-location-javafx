package Controller;


import dao.PaiementDAO;
import model.Paiement;
import view.PaiementManagementView;

import java.util.List;

public class PaiementController {

    private PaiementManagementView view;
    private PaiementDAO paiementDAO;

    public PaiementController(PaiementManagementView view) {
        this.view = view;
        this.paiementDAO = new PaiementDAO();

        initEventHandlers();
        loadPaiements();
    }

    private void initEventHandlers() {
        view.getSearchButton().setOnAction(e -> searchPaiements());
        view.getSearchField().setOnAction(e -> searchPaiements());
        view.getRefreshButton().setOnAction(e -> loadPaiements());
        view.getModeFilterComboBox().setOnAction(e -> filterByMode());
    }

    private void loadPaiements() {
        List<Paiement> paiements = paiementDAO.getAll();
        view.refreshTable(paiements);
        updateStats(paiements);
    }

    private void searchPaiements() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadPaiements();
        } else {
            // Recherche par référence (à adapter)
            List<Paiement> paiements = paiementDAO.getAll();
            paiements.removeIf(p ->
                    (p.getReferenceTransaction() == null || !p.getReferenceTransaction().toLowerCase().contains(keyword.toLowerCase())) &&
                            (p.getNotes() == null || !p.getNotes().toLowerCase().contains(keyword.toLowerCase()))
            );
            view.refreshTable(paiements);
            updateStats(paiements);
        }
    }

    private void filterByMode() {
        String mode = view.getModeFilterComboBox().getValue();
        List<Paiement> paiements = paiementDAO.getAll();

        if (mode != null && !"Tous".equals(mode)) {
            paiements.removeIf(p -> !mode.equals(p.getMode()));
        }

        view.refreshTable(paiements);
        updateStats(paiements);
    }

    private void updateStats(List<Paiement> paiements) {
        int total = paiements.size();
        double montantTotal = 0;
        int especes = 0, mobiles = 0;

        for (Paiement p : paiements) {
            montantTotal += p.getMontantPaye();

            if ("ESPECE".equals(p.getMode())) {
                especes++;
            } else if ("WAVE".equals(p.getMode()) || "ORANGE_MONEY".equals(p.getMode()) ||
                    "FREE_MONEY".equals(p.getMode()) || "MOBILE_MONEY".equals(p.getMode())) {
                mobiles++;
            }
        }

        view.updateStats(total, montantTotal, especes, mobiles);
    }
}
