package Controller;

import dao.StatistiqueDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import utils.FormatUtil;
import view.StatistiqueView;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatistiqueController {

    private static final String[] MONTH_LABELS = {
            "Jan", "Fév", "Mar", "Avr", "Mai", "Juin",
            "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"
    };

    private final StatistiqueView view;
    private final StatistiqueDAO statistiqueDAO;
    private final NumberFormat numberFormat;

    public StatistiqueController(StatistiqueView view) {
        this.view = view;
        this.statistiqueDAO = new StatistiqueDAO();
        this.numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE);
        initEventHandlers();
        loadStats();
    }

    private void initEventHandlers() {
        view.getActualiserButton().setOnAction(e -> loadStats());
        view.getAnneeComboBox().setOnAction(e -> loadStats());
        view.getMoisComboBox().setOnAction(e -> loadStats());
    }

    private void loadStats() {
        Integer yearSelection = view.getAnneeComboBox().getValue();
        int year = yearSelection == null ? java.time.Year.now().getValue() : yearSelection;
        Integer month = selectedMonth();

        try {
            Map<String, Object> generalStats = statistiqueDAO.getGeneralStats(year, month);
            List<Map<String, Object>> monthlyEvolution = statistiqueDAO.getMonthlyEvolution(year, month);
            Map<String, Integer> statusDistribution =
                    statistiqueDAO.getReservationStatusDistribution(year, month);
            List<Map<String, Object>> topVehicles = statistiqueDAO.getTopVehicules(year, month);
            List<Map<String, Object>> topClients = statistiqueDAO.getTopClients(year, month);

            loadGeneralStats(generalStats);
            loadMonthlyChart(monthlyEvolution, year, month);
            loadPieChart(statusDistribution);
            loadTopVehicules(topVehicles);
            loadTopClients(topClients);
            view.setDataStatus("", false);
        } catch (SQLException | RuntimeException e) {
            clearDashboard();
            view.setDataStatus("Impossible de charger les statistiques : " + e.getMessage(), true);
        }
    }

    private Integer selectedMonth() {
        int selectionIndex = view.getMoisComboBox().getSelectionModel().getSelectedIndex();
        return selectionIndex <= 0 ? null : selectionIndex;
    }

    private void loadGeneralStats(Map<String, Object> stats) {
        double revenue = ((Number) stats.get("caTotal")).doubleValue();
        int clients = ((Number) stats.get("totalClients")).intValue();
        int vehicles = ((Number) stats.get("totalVehicules")).intValue();
        int reservations = ((Number) stats.get("totalReservations")).intValue();

        view.getCaTotalLabel().setText(FormatUtil.formatFCFA(revenue));
        view.getTotalClientsLabel().setText(numberFormat.format(clients));
        view.getTotalVehiculesLabel().setText(numberFormat.format(vehicles));
        view.getTotalReservationsLabel().setText(numberFormat.format(reservations));
    }

    private void loadMonthlyChart(List<Map<String, Object>> evolution, int year, Integer month) {
        BarChart<String, Number> chart = view.getMonthlyChart();
        chart.getData().clear();
        XYChart.Series<String, Number> reservations = new XYChart.Series<>();
        reservations.setName("Réservations");

        for (Map<String, Object> row : evolution) {
            int monthNumber = ((Number) row.get("mois")).intValue();
            int total = ((Number) row.get("total")).intValue();
            reservations.getData().add(new XYChart.Data<>(MONTH_LABELS[monthNumber - 1], total));
        }

        if (!reservations.getData().isEmpty()) {
            chart.getData().add(reservations);
            chart.setTitle("Réservations — " + year + (month == null ? "" : " / " + MONTH_LABELS[month - 1]));
        } else {
            chart.setTitle("Aucune réservation pour la période sélectionnée");
        }
    }

    private void loadPieChart(Map<String, Integer> distribution) {
        PieChart pieChart = view.getStatusPieChart();
        pieChart.setData(FXCollections.observableArrayList());

        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            if (entry.getValue() > 0) {
                pieChart.getData().add(new PieChart.Data(
                        statusLabel(entry.getKey()) + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }
        pieChart.setTitle(pieChart.getData().isEmpty()
                ? "Aucune réservation pour la période sélectionnée"
                : "Réservations par statut");
    }

    private String statusLabel(String status) {
        return switch (status) {
            case "EN_ATTENTE" -> "En attente";
            case "ACCEPTEE" -> "Acceptée";
            case "REFUSEE" -> "Refusée";
            case "ANNULEE" -> "Annulée";
            default -> status.replace('_', ' ');
        };
    }

    private void loadTopVehicules(List<Map<String, Object>> topVehicules) {
        TableView<Map<String, Object>> table = view.getTopVehiculesTable();
        table.getItems().setAll(topVehicules);
        table.getColumns().clear();

        TableColumn<Map<String, Object>, Object> makeColumn = column("Marque", "marque", 120);
        TableColumn<Map<String, Object>, Object> modelColumn = column("Modèle", "modele", 120);
        TableColumn<Map<String, Object>, Object> plateColumn =
                column("Immatriculation", "immatriculation", 130);
        TableColumn<Map<String, Object>, Object> rentalsColumn =
                column("Locations", "nb_locations", 100);
        table.getColumns().addAll(makeColumn, modelColumn, plateColumn, rentalsColumn);
    }

    private void loadTopClients(List<Map<String, Object>> topClients) {
        TableView<Map<String, Object>> table = view.getTopClientsTable();
        table.getItems().setAll(topClients);
        table.getColumns().clear();

        TableColumn<Map<String, Object>, Object> nameColumn = column("Nom", "nom", 120);
        TableColumn<Map<String, Object>, Object> firstNameColumn = column("Prénom", "prenom", 120);
        TableColumn<Map<String, Object>, Object> emailColumn = column("Email", "email", 180);
        TableColumn<Map<String, Object>, Object> reservationsColumn =
                column("Réservations", "nb_reservations", 100);
        table.getColumns().addAll(nameColumn, firstNameColumn, emailColumn, reservationsColumn);
    }

    private TableColumn<Map<String, Object>, Object> column(String title, String key, double width) {
        TableColumn<Map<String, Object>, Object> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleObjectProperty<>(
                cell.getValue().getOrDefault(key, "")));
        column.setPrefWidth(width);
        return column;
    }

    private void clearDashboard() {
        Label[] labels = {
                view.getCaTotalLabel(), view.getTotalClientsLabel(),
                view.getTotalVehiculesLabel(), view.getTotalReservationsLabel()
        };
        for (Label label : labels) {
            label.setText("—");
        }
        view.getMonthlyChart().getData().clear();
        view.getMonthlyChart().setTitle("Données indisponibles");
        view.getStatusPieChart().getData().clear();
        view.getStatusPieChart().setTitle("Données indisponibles");
        view.getTopVehiculesTable().getItems().clear();
        view.getTopClientsTable().getItems().clear();
    }
}
