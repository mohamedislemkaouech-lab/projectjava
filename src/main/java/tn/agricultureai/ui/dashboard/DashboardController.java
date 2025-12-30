package tn.agricultureai.ui.dashboard;

import tn.agricultureai.domain.model.*;
import tn.agricultureai.domain.exception.*;
import tn.agricultureai.ui.observer.DashboardObserver;
import tn.agricultureai.ui.chart.*;
import tn.agricultureai.service.factory.ServiceFactory;
import tn.agricultureai.service.prediction.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard controller - handles user interactions and business logic.
 * Demonstrates: MVC Controller, Observer pattern integration.
 *
 * @author Your Name
 */
public class DashboardController {

    private final DashboardModel model;
    private final DashboardView view;
    private final Scanner scanner;
    private final List<DashboardObserver> observers;

    public DashboardController(DashboardModel model, DashboardView view) {
        this.model = model;
        this.view = view;
        this.scanner = new Scanner(System.in);
        this.observers = new ArrayList<>();

        // Initialize services
        model.setActivePredictionService(ServiceFactory.getDefaultPredictionService());
        model.setActiveReportGenerator(ServiceFactory.getReportGenerator());
    }

    /**
     * Start the dashboard application
     */
    public void start() {
        boolean running = true;

        while (running) {
            view.displayMainMenu();

            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> viewStatistics();
                    case 2 -> generatePredictions();
                    case 3 -> viewExportData();
                    case 4 -> generateReport();
                    case 5 -> displayCharts();
                    case 6 -> searchAndFilter();
                    case 0 -> {
                        view.displayInfo("Thank you for using the system. Goodbye!");
                        running = false;
                    }
                    default -> view.displayError("Invalid choice. Please try again.");
                }

                if (running && choice != 0) {
                    view.pause();
                }

            } catch (InputMismatchException e) {
                view.displayError("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
                view.pause();
            } catch (Exception e) {
                view.displayError("An error occurred: " + e.getMessage());
                view.pause();
            }
        }
    }

    /**
     * View statistics
     */
    private void viewStatistics() {
        DashboardModel.DashboardStatistics stats = model.getStatistics();
        view.displayStatistics(stats);
        notifyObservers(DashboardObserver.UpdateEvent.EventType.DATA_UPDATED,
                "Statistics refreshed", stats);
    }

    /**
     * Generate predictions
     */
    private void generatePredictions() {
        try {
            // Select product
            view.displayProductMenu();
            int productChoice = scanner.nextInt();
            scanner.nextLine();

            if (productChoice < 1 || productChoice > ProductType.values().length) {
                view.displayError("Invalid product selection");
                return;
            }

            ProductType product = ProductType.values()[productChoice - 1];

            // Select country
            view.displayCountryMenu();
            int countryChoice = scanner.nextInt();
            scanner.nextLine();

            if (countryChoice < 1 || countryChoice > Country.values().length) {
                view.displayError("Invalid country selection");
                return;
            }

            Country country = Country.values()[countryChoice - 1];

            // Generate prediction
            view.displayInfo("Generating prediction...");
            PredictionResult prediction = model.getActivePredictionService()
                    .predict(product, country);

            model.addPrediction(prediction);
            model.getPredictionRepository().save(prediction);

            view.displaySuccess("Prediction generated successfully!");
            view.displayPredictions(List.of(prediction));

            notifyObservers(DashboardObserver.UpdateEvent.EventType.PREDICTION_COMPLETED,
                    "New prediction created", prediction);

        } catch (PredictionException e) {
            view.displayError("Prediction failed: " + e.getUserMessage());
            ExceptionHandler.handle(e);
        }
    }

    /**
     * View export data
     */
    private void viewExportData() {
        List<ExportData> exports = model.getExportRepository().findAll();
        view.displayExports(exports, 20);
    }

    /**
     * Generate market report
     */
    private void generateReport() {
        try {
            if (model.getCurrentPredictions().isEmpty()) {
                view.displayInfo("No predictions available. Generating sample predictions...");

                // Generate some predictions
                List<PredictionResult> predictions = new ArrayList<>();
                predictions.add(model.getActivePredictionService()
                        .predict(ProductType.OLIVE_OIL, Country.ITALY));
                predictions.add(model.getActivePredictionService()
                        .predict(ProductType.DATES, Country.FRANCE));
                predictions.add(model.getActivePredictionService()
                        .predict(ProductType.CITRUS, Country.GERMANY));

                model.setCurrentPredictions(predictions);
            }

            view.displayInfo("Generating market report...");

            MarketReport report = model.getActiveReportGenerator()
                    .generateReport(
                            MarketReport.ReportType.DAILY_SUMMARY,
                            model.getCurrentPredictions()
                    );

            model.setCurrentReport(report);
            model.getReportRepository().save(report);

            view.displaySuccess("Report generated successfully!");
            view.displayReport(report);

            notifyObservers(DashboardObserver.UpdateEvent.EventType.REPORT_GENERATED,
                    "New report created", report);

        } catch (ReportGenerationException e) {
            view.displayError("Report generation failed: " + e.getUserMessage());
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Display charts
     */
    private void displayCharts() {
        // Select chart type
        view.displayChartTypeMenu();
        int chartChoice = scanner.nextInt();
        scanner.nextLine();

        ChartStrategy strategy = switch (chartChoice) {
            case 1 -> new BarChartStrategy();
            case 2 -> new LineChartStrategy();
            case 3 -> new PieChartStrategy();
            default -> {
                view.displayError("Invalid chart type");
                yield null;
            }
        };

        if (strategy == null) {
            return;
        }

        view.setChartStrategy(strategy);

        // Get data for chart
        Map<String, Double> data = model.getExportRepository()
                .getTotalValueByProduct()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getDisplayName(),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        view.displayChart("Total Export Value by Product", data);
    }

    /**
     * Search and filter
     */
    private void searchAndFilter() {
        System.out.println("\n🔍 SEARCH & FILTER");
        System.out.println("  1. Filter by Product");
        System.out.println("  2. Filter by Country");
        System.out.println("  3. Recent Exports (30 days)");
        System.out.print("\nEnter choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> {
                view.displayProductMenu();
                int productChoice = scanner.nextInt();
                scanner.nextLine();

                if (productChoice >= 1 && productChoice <= ProductType.values().length) {
                    ProductType product = ProductType.values()[productChoice - 1];
                    List<ExportData> filtered = model.getExportRepository()
                            .findByProductType(product);
                    view.displayExports(filtered, 20);
                }
            }
            case 2 -> {
                view.displayCountryMenu();
                int countryChoice = scanner.nextInt();
                scanner.nextLine();

                if (countryChoice >= 1 && countryChoice <= Country.values().length) {
                    Country country = Country.values()[countryChoice - 1];
                    List<ExportData> filtered = model.getExportRepository()
                            .findByDestination(country);
                    view.displayExports(filtered, 20);
                }
            }
            case 3 -> {
                List<ExportData> recent = model.getExportRepository().findRecent(30);
                view.displayExports(recent, 20);
            }
            default -> view.displayError("Invalid choice");
        }
    }

    /**
     * Add observer
     */
    public void addObserver(DashboardObserver observer) {
        observers.add(observer);
    }

    /**
     * Remove observer
     */
    public void removeObserver(DashboardObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notify all observers
     */
    private void notifyObservers(
            DashboardObserver.UpdateEvent.EventType type,
            String message,
            Object data
    ) {
        DashboardObserver.UpdateEvent event =
                new DashboardObserver.UpdateEvent(type, message, data);

        for (DashboardObserver observer : observers) {
            switch (type) {
                case DATA_UPDATED -> observer.onDataUpdated(event);
                case PREDICTION_COMPLETED -> observer.onPredictionCompleted(event);
                case REPORT_GENERATED -> observer.onReportGenerated(event);
            }
        }
    }
}