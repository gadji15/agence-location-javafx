package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.Year;
import java.util.Map;

public class StatistiqueView {

    private VBox root;
    private ScrollPane scrollPane;
    private VBox contentContainer;

    // Filtres
    private ComboBox<Integer> anneeComboBox;
    private ComboBox<String> moisComboBox;
    private Button actualiserButton;

    // Composants d'affichage
    private GridPane cardsGrid;
    private BarChart<String, Number> monthlyChart;
    private PieChart statusPieChart;
    private TableView<Map<String, Object>> topVehiculesTable;
    private TableView<Map<String, Object>> topClientsTable;

    private Label caTotalLabel;
    private Label totalClientsLabel;
    private Label totalVehiculesLabel;
    private Label totalReservationsLabel;
    private Label dataStatusLabel;

    public StatistiqueView() {
        initUI();
    }

    private void initUI() {
        root = new VBox();
        root.setSpacing(15);
        root.setPadding(new Insets(10));
        root.setFillWidth(true);

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        contentContainer = new VBox();
        contentContainer.setSpacing(20);
        contentContainer.setPadding(new Insets(10));
        contentContainer.setFillWidth(true);

        // Titre
        Label titleLabel = new Label("Tableau de bord statistique");
        titleLabel.setGraphic(icon(FontAwesomeSolid.CHART_BAR, "#2878a5", 18));
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre de filtres
        HBox filterBar = createFilterBar();
        dataStatusLabel = new Label();
        dataStatusLabel.setWrapText(true);
        dataStatusLabel.setStyle("-fx-text-fill: #b42318; -fx-font-weight: bold;");
        dataStatusLabel.setManaged(false);
        dataStatusLabel.setVisible(false);

        // Cartes statistiques
        cardsGrid = createCardsGrid();

        // Graphique d'évolution mensuelle
        VBox chartSection = createMonthlyChartSection();

        // Graphique circulaire
        VBox pieChartSection = createPieChartSection();

        // Top véhicules
        VBox topVehiculesSection = createTopVehiculesSection();

        // Top clients
        VBox topClientsSection = createTopClientsSection();

        // Assemblage
        contentContainer.getChildren().addAll(
                titleLabel,
                filterBar,
                dataStatusLabel,
                cardsGrid,
                chartSection,
                pieChartSection,
                topVehiculesSection,
                topClientsSection
        );

        scrollPane.setContent(contentContainer);
        root.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox();
        filterBar.setSpacing(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(5, 0, 10, 0));

        Label filterLabel = new Label("Filtres :");
        filterLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        // Année
        Label anneeLabel = new Label("Année :");
        anneeLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        anneeComboBox = new ComboBox<>();
        for (int i = 2020; i <= Year.now().getValue(); i++) {
            anneeComboBox.getItems().add(i);
        }
        anneeComboBox.setValue(Year.now().getValue());
        anneeComboBox.setPrefWidth(80);
        anneeComboBox.getStyleClass().add("input-field");

        // Mois
        Label moisLabel = new Label("Mois :");
        moisLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #34495e;");

        moisComboBox = new ComboBox<>();
        moisComboBox.getItems().addAll("Tous", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre");
        moisComboBox.setValue("Tous");
        moisComboBox.setPrefWidth(120);
        moisComboBox.getStyleClass().add("input-field");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actualiserButton = new Button("Actualiser");
        actualiserButton.setGraphic(icon(FontAwesomeSolid.SYNC_ALT, "#ffffff", 13));
        actualiserButton.getStyleClass().add("action-button");
        actualiserButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-height: 32; -fx-padding: 0 20; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold;");

        filterBar.getChildren().addAll(filterLabel, anneeLabel, anneeComboBox, moisLabel, moisComboBox, spacer, actualiserButton);
        return filterBar;
    }

    private GridPane createCardsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);

        // Création des cartes
        caTotalLabel = createStatCard(grid, "Chiffre d'affaires encaissé", FontAwesomeSolid.MONEY_BILL_ALT, "0 FCFA", 0, 0, 2);
        totalClientsLabel = createStatCard(grid, "Clients", FontAwesomeSolid.USERS, "0", 0, 2, 1);
        totalVehiculesLabel = createStatCard(grid, "Véhicules", FontAwesomeSolid.CAR, "0", 1, 0, 1);
        totalReservationsLabel = createStatCard(grid, "Réservations (période)", FontAwesomeSolid.CALENDAR_ALT, "0", 1, 2, 1);

        return grid;
    }

    private Label createStatCard(GridPane grid, String title, FontAwesomeSolid icon,
                                 String value, int row, int col, int colspan) {
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(5);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setMinWidth(180);
        card.setMinHeight(80);
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.setGraphic(icon(icon, "#64748b", 14));
        titleLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #7f8c8d;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        valueLabel.setId("value_" + title.replaceAll(" ", "_"));

        card.getChildren().addAll(titleLabel, valueLabel);
        grid.add(card, col, row, colspan, 1);
        GridPane.setHgrow(card, Priority.ALWAYS);

        return valueLabel;
    }

    private VBox createMonthlyChartSection() {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label title = new Label("Évolution mensuelle des réservations");
        title.setGraphic(icon(FontAwesomeSolid.CHART_LINE, "#2878a5", 15));
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Création du graphique
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        monthlyChart = new BarChart<>(xAxis, yAxis);
        monthlyChart.setTitle("Réservations par mois");
        monthlyChart.setPrefHeight(300);
        monthlyChart.setPrefWidth(800);
        monthlyChart.setLegendVisible(true);
        monthlyChart.setAnimated(false);

        section.getChildren().addAll(title, monthlyChart);
        VBox.setVgrow(monthlyChart, Priority.ALWAYS);
        return section;
    }

    private VBox createPieChartSection() {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label title = new Label("Répartition des réservations par statut");
        title.setGraphic(icon(FontAwesomeSolid.CHART_PIE, "#2878a5", 15));
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        statusPieChart = new PieChart();
        statusPieChart.setTitle("Statut des réservations");
        statusPieChart.setPrefHeight(300);
        statusPieChart.setPrefWidth(400);
        statusPieChart.setLabelsVisible(true);
        statusPieChart.setClockwise(true);
        statusPieChart.setAnimated(false);

        HBox pieContainer = new HBox();
        pieContainer.setAlignment(Pos.CENTER);
        pieContainer.getChildren().add(statusPieChart);

        section.getChildren().addAll(title, pieContainer);
        return section;
    }

    private VBox createTopVehiculesSection() {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label title = new Label("Top 5 des véhicules loués");
        title.setGraphic(icon(FontAwesomeSolid.CAR, "#2878a5", 15));
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        topVehiculesTable = new TableView<>();
        topVehiculesTable.setPrefHeight(200);
        topVehiculesTable.getStyleClass().add("table-view");
        topVehiculesTable.setPlaceholder(new Label("Aucune location contractualisée pour cette période."));

        section.getChildren().addAll(title, topVehiculesTable);
        return section;
    }

    private VBox createTopClientsSection() {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        Label title = new Label("Top 5 des clients les plus actifs");
        title.setGraphic(icon(FontAwesomeSolid.USERS, "#2878a5", 15));
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        topClientsTable = new TableView<>();
        topClientsTable.setPrefHeight(200);
        topClientsTable.getStyleClass().add("table-view");
        topClientsTable.setPlaceholder(new Label("Aucune réservation pour cette période."));

        section.getChildren().addAll(title, topClientsTable);
        return section;
    }

    // ===== GETTERS =====

    public VBox getRoot() {
        return root;
    }

    public ComboBox<Integer> getAnneeComboBox() {
        return anneeComboBox;
    }

    public ComboBox<String> getMoisComboBox() {
        return moisComboBox;
    }

    public Button getActualiserButton() {
        return actualiserButton;
    }

    public BarChart<String, Number> getMonthlyChart() {
        return monthlyChart;
    }

    public PieChart getStatusPieChart() {
        return statusPieChart;
    }

    public TableView<Map<String, Object>> getTopVehiculesTable() {
        return topVehiculesTable;
    }

    public TableView<Map<String, Object>> getTopClientsTable() {
        return topClientsTable;
    }

    public Label getCaTotalLabel() {
        return caTotalLabel;
    }

    public Label getTotalClientsLabel() {
        return totalClientsLabel;
    }

    public Label getTotalVehiculesLabel() {
        return totalVehiculesLabel;
    }

    public Label getTotalReservationsLabel() {
        return totalReservationsLabel;
    }

    public void setDataStatus(String message, boolean error) {
        dataStatusLabel.setText(message);
        dataStatusLabel.setVisible(error && message != null && !message.isBlank());
        dataStatusLabel.setManaged(dataStatusLabel.isVisible());
    }

    private FontIcon icon(FontAwesomeSolid code, String color, int size) {
        FontIcon icon = new FontIcon(code);
        icon.setIconColor(javafx.scene.paint.Color.web(color));
        icon.setIconSize(size);
        return icon;
    }
}