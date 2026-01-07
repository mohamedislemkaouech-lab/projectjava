package tn.agricultureai.ui.dashboard;

import tn.agricultureai.domain.model.*;
import tn.agricultureai.domain.exception.*;
import tn.agricultureai.ui.observer.DashboardObserver;
import tn.agricultureai.ui.chart.*;
import tn.agricultureai.service.factory.ServiceFactory;
import tn.agricultureai.service.prediction.*;
import java.util.*;
import java.util.stream.Collectors;

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

        model.setActivePredictionService(ServiceFactory.getDefaultPredictionService());
        model.setActiveReportGenerator(ServiceFactory.getReportGenerator());
    }

    public void start() {
        boolean running = true;

        while (running) {
            view.displayMainMenu();

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

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
                scanner.nextLine();
                view.pause();
            } catch (Exception e) {
                view.displayError("An error occurred: " + e.getMessage());
                view.pause();
            }
        }
    }

    private void viewStatistics() {
        DashboardModel.DashboardStatistics stats = model.getStatistics();
        view.displayStatistics(stats);
        notifyObservers(DashboardObserver.UpdateEvent.EventType.DATA_UPDATED,
                "Statistics refreshed", stats);
    }

    private void generatePredictions() {
        try {
            view.displayProductMenu();
            int productChoice = scanner.nextInt();
            scanner.nextLine();

            if (productChoice < 1 || productChoice > ProductType.values().length) {
                view.displayError("Invalid product selection");
                return;
            }

            ProductType product = ProductType.values()[productChoice - 1];

            view.displayCountryMenu();
            int countryChoice = scanner.nextInt();
            scanner.nextLine();

            if (countryChoice < 1 || countryChoice > Country.values().length) {
                view.displayError("Invalid country selection");
                return;
            }

            Country country = Country.values()[countryChoice - 1];

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

    private void viewExportData() {
        List<ExportData> exports = model.getExportRepository().findAll();
        view.displayExports(exports, 20);
    }

    private void generateReport() {
        try {
            if (model.getCurrentPredictions().isEmpty()) {
                view.displayInfo("No predictions available. Generating sample predictions...");

                List<PredictionResult> predictions = new ArrayList<>();
                predictions.add(model.getActivePredictionService()
                        .predict(ProductType.OLIVE_OIL, Country.ITALY));
                predictions.add(model.getActivePredictionService()
                        .predict(ProductType.DATES, Country.FRANCE));

                // FIXED LINE 160: CITRUS_FRUITS not CITRUS
                predictions.add(model.getActivePredictionService()
                        .predict(ProductType.CITRUS_FRUITS, Country.GERMANY));

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

    private void displayCharts() {
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

    public void addObserver(DashboardObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(DashboardObserver observer) {
        observers.remove(observer);
    }

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