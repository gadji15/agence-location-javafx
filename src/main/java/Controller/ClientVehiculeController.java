package Controller;

import dao.VehiculeDAO;
import javafx.scene.control.Alert;
import model.Vehicule;
import view.ClientVehiculeView;

import java.util.List;

public class ClientVehiculeController {

    private ClientVehiculeView view;
    private VehiculeDAO vehiculeDAO;

    public ClientVehiculeController(ClientVehiculeView view) {
        this.view = view;
        this.vehiculeDAO = new VehiculeDAO();

        System.out.println("✅ ClientVehiculeController créé");

        initEventHandlers();
        loadVehicules();
    }

    private void initEventHandlers() {
        // Recherche
        view.getSearchButton().setOnAction(e -> searchVehicules());
        view.getSearchField().setOnAction(e -> searchVehicules());

        // Rafraîchir
        view.getRefreshButton().setOnAction(e -> loadVehicules());

        // Filtre par statut
        view.getStatutFilterComboBox().setOnAction(e -> filterByStatut());

        // Callback pour réserver
        view.setCallback(new ClientVehiculeView.VehiculeCallback() {
            @Override
            public void onSearch(String keyword) {
                searchVehicules();
            }

            @Override
            public void onFilter(String statut) {
                filterByStatut();
            }

            @Override
            public void onRefresh() {
                loadVehicules();
            }

            @Override
            public void onReserver(Vehicule vehicule) {
                handleReserver(vehicule);
            }
        });
    }

    private void loadVehicules() {
        List<Vehicule> vehicules = vehiculeDAO.getAll();
        view.refreshTable(vehicules);
        updateStats(vehicules);
        System.out.println("🚗 " + vehicules.size() + " véhicules chargés");
    }

    private void searchVehicules() {
        String keyword = view.getSearchField().getText().trim();
        if (keyword.isEmpty()) {
            loadVehicules();
        } else {
            List<Vehicule> vehicules = vehiculeDAO.search(keyword);
            view.refreshTable(vehicules);
            updateStats(vehicules);
        }
    }

    private void filterByStatut() {
        String statut = view.getStatutFilterComboBox().getValue();
        List<Vehicule> vehicules;

        if (statut == null || "Tous".equals(statut)) {
            vehicules = vehiculeDAO.getAll();
        } else {
            // Filtrer manuellement par statut
            vehicules = vehiculeDAO.getAll();
            vehicules.removeIf(v -> !v.getStatut().equals(statut));
        }

        view.refreshTable(vehicules);
        updateStats(vehicules);
    }

    private void updateStats(List<Vehicule> vehicules) {
        int total = vehicules.size();
        int disponibles = 0, loues = 0, maintenance = 0;

        for (Vehicule v : vehicules) {
            switch (v.getStatut()) {
                case "DISPONIBLE" -> disponibles++;
                case "LOUE" -> loues++;
                case "MAINTENANCE" -> maintenance++;
                default -> {}
            }
        }

        view.updateStats(total, disponibles, loues, maintenance);
    }

    private void handleReserver(Vehicule vehicule) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Réservation");
        alert.setHeaderText("Véhicule: " + vehicule.getMarque() + " " + vehicule.getModele());
        alert.setContentText(
                "📌 Pour réserver ce véhicule, rendez-vous dans la section 'Mes Réservations'.\n\n" +
                        "🔹 Véhicule: " + vehicule.getMarque() + " " + vehicule.getModele() + "\n" +
                        "🔹 Immatriculation: " + vehicule.getImmatriculation() + "\n" +
                        "🔹 Prix journalier: " + String.format("%.2f €", vehicule.getPrixJournalier()) + "\n\n" +
                        "Cliquez sur 'Nouvelle réservation' et sélectionnez ce véhicule."
        );
        alert.showAndWait();
    }
}