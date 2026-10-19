package Controller;

import dao.StatistiqueDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import view.StatistiqueView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatistiqueController {

    private StatistiqueView view;
    private StatistiqueDAO statistiqueDAO;
    private NumberFormat currencyFormat;
    private NumberFormat numberFormat;

    public StatistiqueController(StatistiqueView view) {
        this.view = view;
        this.statistiqueDAO = new StatistiqueDAO();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("fr", "SN"));
        this.numberFormat = NumberFormat.getNumberInstance(new Locale("fr", "SN"));

        // Ajouter un message de débogage
        System.out.println("📊 StatistiqueController initialisé");

        initEventHandlers();
        loadStats();
    }

    private void initEventHandlers() {
        view.getActualiserButton().setOnAction(e -> {
            System.out.println("🔄 Actualisation demandée");
            loadStats();
        });
        view.getAnneeComboBox().setOnAction(e -> {
            System.out.println("📅 Changement d'année: " + view.getAnneeComboBox().getValue());
            loadStats();
        });
        view.getMoisComboBox().setOnAction(e -> {
            System.out.println("📅 Changement de mois: " + view.getMoisComboBox().getValue());
            loadStats();
        });
    }

    private void loadStats() {
        System.out.println("📊 Chargement des statistiques...");
        loadGeneralStats();
        loadMonthlyChart();
        loadPieChart();
        loadTopVehicules();
        loadTopClients();
        System.out.println("✅ Statistiques chargées");
    }

    private void loadGeneralStats() {
        System.out.println("   Chargement des statistiques générales...");
        Map<String, Object> stats = statistiqueDAO.getGeneralStats();

        System.out.println("   Stats reçues: " + stats);

        // Mettre à jour les cartes
        double ca = stats.containsKey("caTotal") && stats.get("caTotal") != null ? (double) stats.get("caTotal") : 0;
        view.getCaTotalLabel().setText(currencyFormat.format(ca));

        int clients = stats.containsKey("totalClients") && stats.get("totalClients") != null ? (int) stats.get("totalClients") : 0;
        view.getTotalClientsLabel().setText(numberFormat.format(clients));

        int vehicules = stats.containsKey("totalVehicules") && stats.get("totalVehicules") != null ? (int) stats.get("totalVehicules") : 0;
        view.getTotalVehiculesLabel().setText(numberFormat.format(vehicules));

        int reservations = stats.containsKey("totalReservations") && stats.get("totalReservations") != null ? (int) stats.get("totalReservations") : 0;
        view.getTotalReservationsLabel().setText(numberFormat.format(reservations));

        System.out.println("   CA: " + currencyFormat.format(ca));
        System.out.println("   Clients: " + clients);
        System.out.println("   Véhicules: " + vehicules);
        System.out.println("   Réservations: " + reservations);
    }

    private void loadMonthlyChart() {
        System.out.println("   Chargement du graphique mensuel...");
        int annee = view.getAnneeComboBox().getValue() != null ? view.getAnneeComboBox().getValue() : 2026;

        BarChart<String, Number> chart = view.getMonthlyChart();
        chart.getData().clear();

        List<Map<String, Object>> evolution = statistiqueDAO.getMonthlyEvolution(annee);
        System.out.println("   Évolution reçue: " + evolution.size() + " mois");

        XYChart.Series<String, Number> totalSeries = new XYChart.Series<>();
        totalSeries.setName("Total");

        String[] moisLabels = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"};

        if (evolution != null && !evolution.isEmpty()) {
            for (Map<String, Object> month : evolution) {
                int mois = (int) month.get("mois");
                String label = moisLabels[mois - 1];
                int total = (int) month.get("total");
                totalSeries.getData().add(new XYChart.Data<>(label, total));
                System.out.println("      " + label + ": " + total + " réservations");
            }
        } else {
            System.out.println("   ⚠️ Aucune donnée mensuelle trouvée");
            // Données de démonstration
            for (int i = 0; i < 12; i++) {
                totalSeries.getData().add(new XYChart.Data<>(moisLabels[i], (int) (Math.random() * 20 + 5)));
            }
        }

        chart.getData().add(totalSeries);
        chart.setTitle("Évolution des réservations " + annee);
    }

    private void loadPieChart() {
        System.out.println("   Chargement du graphique circulaire...");
        PieChart pieChart = view.getStatusPieChart();
        pieChart.getData().clear();

        Map<String, Integer> distribution = statistiqueDAO.getReservationStatusDistribution();
        System.out.println("   Distribution reçue: " + distribution);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        if (distribution != null && !distribution.isEmpty()) {
            for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
                String statut = entry.getKey();
                int count = entry.getValue();
                if (count > 0) {
                    String emoji = switch (statut) {
                        case "EN_ATTENTE" -> "⏳";
                        case "ACCEPTEE" -> "✅";
                        case "REFUSEE" -> "❌";
                        case "ANNULEE" -> "🚫";
                        default -> "📌";
                    };
                    pieData.add(new PieChart.Data(emoji + " " + statut + " (" + count + ")", count));
                }
            }
        } else {
            System.out.println("   ⚠️ Aucune donnée de réservation trouvée");
            // Données de démonstration
            pieData.add(new PieChart.Data("⏳ EN_ATTENTE (15)", 15));
            pieData.add(new PieChart.Data("✅ ACCEPTEE (25)", 25));
            pieData.add(new PieChart.Data("❌ REFUSEE (5)", 5));
            pieData.add(new PieChart.Data("🚫 ANNULEE (8)", 8));
        }

        pieChart.setData(pieData);
        pieChart.setTitle("Répartition des réservations");
    }

    @SuppressWarnings("unchecked")
    private void loadTopVehicules() {
        System.out.println("   Chargement du top véhicules...");
        TableView<Map<String, Object>> table = view.getTopVehiculesTable();
        table.getItems().clear();
        table.getColumns().clear();

        // Créer les colonnes
        TableColumn<Map<String, Object>, Object> colMarque = new TableColumn<>("Marque");
        colMarque.setCellValueFactory(param -> {
            Map<String, Object> row = param.getValue();
            return new SimpleObjectProperty<>(row != null ? row.get("marque") : "");
        });
        colMarque.setPrefWidth(120);

        TableColumn<Map<String, Object>, Object> colModele = new TableColumn<>("Modèle");
        colModele.setCellValueFactory(param -> {
            Map<String, Object> row = param.getValue();
            return new SimpleObjectProperty<>(row != null ? row.get("modele") : "");
        });
        colModele.setPrefWidth(120);

        TableColumn<Map<String, Object>, Object> colImmatriculation = new TableColumn<>("Immatriculation");
        colImmatriculation.setCellValueFactory(param -> {
            Map<String, Object> row = param.getValue();
            return new SimpleObjectProperty<>(row != null ? row.get("immatriculation") : "");
        });
        colImmatriculation.setPrefWidth(130);

        TableColumn<Map<String, Object>, Object> colNbLocations = new TableColumn<>("Locations");
        colNbLocations.setCellValueFactory(param -> {
            Map<String, Object> row = param.getValue();
            return new SimpleObjectProperty<>(row != null ? row.get("nb_locations") : 0);
        });
        colNbLocations.setPrefWidth(100);

        table.getColumns().addAll(colMarque, colModele, colImmatriculation, colNbLocations);

        List<Map<String, Object>> topVehicules = statistiqueDAO.getTopVehicules();
        System.out.println("   Top véhicules reçus: " + topVehicules.size());

        if (topVehicules != null && !topVehicules.isEmpty()) {
            table.getItems().addAll(topVehicules);
        } else {
            System.out.println("   ⚠️ Aucune donnée de véhicule trouvée");
            // Données de démonstration
            Map<String, Object> demo1 = Map.of("marque", "Peugeot", "modele", "208", "immatriculation", "AB-123-CD", "nb_locations", 12);
            Map<String, Object> demo2 = Map.of("marque", "Renault", "modele", "Clio", "immatriculation", "EF-456-GH", "nb_locations", 10);
            Map<String, Object> demo3 = Map.of("marque", "Citroën", "modele", "C3", "immatriculation", "IJ-789-KL", "nb_locations", 8);
            Map<String, Object> demo4 = Map.of("marque", "Toyota", "modele", "Yaris", "immatriculation", "MN-012-OP", "nb_locations", 7);
            Map<String, Object> demo5 = Map.of("marque", "Ford", "modele", "Fiesta", "immatriculation", "QR-345-ST", "nb_locations", 5);
            table.getItems().addAll(demo1, demo2, demo3, demo4, demo5);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTopClients() {
        System.out.println("   Chargement du top clients...");
        TableView<Map<String, Object>> table = view.getTopClientsTable();
        table.getItems().clear();
        table.getColumns().clear();

        // Créer les colonnes
        TableColumn<Map<String, Object>, Object> colNom = new TableColumn<>("Nom");
        colNom.setCellValueFactory(param -> {
            Map<String, Object> row = param.getValue();
            return new SimpleObjectProperty<>(row != null ? row.get("nom") : "");
        });
        colNom.setPrefWidth(120);

        TableColumn<Map<String, Object>, Object> colPrenom = new TableColumn<>("Prénom");
        colPrenom.setCellValueFactory(param -> {
            Map<String, Object> row = param.getValue();
            return new SimpleObjectProperty<>(row != null ? row.get("prenom") : "");
        });
        colPrenom.setPrefWidth(120);

        TableColumn<Map<String, Object>, Object> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(param -> {
            Map<String, Object> row = param.getValue();
            return new SimpleObjectProperty<>(row != null ? row.get("email") : "");
        });
        colEmail.setPrefWidth(180);

        TableColumn<Map<String, Object>, Object> colNbReservations = new TableColumn<>("Réservations");
        colNbReservations.setCellValueFactory(param -> {
            Map<String, Object> row = param.getValue();
            return new SimpleObjectProperty<>(row != null ? row.get("nb_reservations") : 0);
        });
        colNbReservations.setPrefWidth(100);

        table.getColumns().addAll(colNom, colPrenom, colEmail, colNbReservations);

        List<Map<String, Object>> topClients = statistiqueDAO.getTopClients();
        System.out.println("   Top clients reçus: " + topClients.size());

        if (topClients != null && !topClients.isEmpty()) {
            table.getItems().addAll(topClients);
        } else {
            System.out.println("   ⚠️ Aucune donnée de client trouvée");
            // Données de démonstration
            Map<String, Object> demo1 = Map.of("nom", "Dupont", "prenom", "Jean", "email", "jean.dupont@email.com", "nb_reservations", 8);
            Map<String, Object> demo2 = Map.of("nom", "Martin", "prenom", "Marie", "email", "marie.martin@email.com", "nb_reservations", 6);
            Map<String, Object> demo3 = Map.of("nom", "Durand", "prenom", "Pierre", "email", "pierre.durand@email.com", "nb_reservations", 5);
            Map<String, Object> demo4 = Map.of("nom", "Bernard", "prenom", "Sophie", "email", "sophie.bernard@email.com", "nb_reservations", 4);
            Map<String, Object> demo5 = Map.of("nom", "Petit", "prenom", "Lucas", "email", "lucas.petit@email.com", "nb_reservations", 3);
            table.getItems().addAll(demo1, demo2, demo3, demo4, demo5);
        }
    }
}