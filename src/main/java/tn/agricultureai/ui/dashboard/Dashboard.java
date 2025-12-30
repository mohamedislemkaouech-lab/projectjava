package tn.agricultureai.ui.dashboard;

import tn.agricultureai.repository.*;
import tn.agricultureai.ui.observer.DashboardObserver;
import tn.agricultureai.util.DataInitializer;

/**
 * Main dashboard application.
 * Demonstrates: Complete application integration, all patterns working together.
 *
 * @author Your Name
 */
public class Dashboard {

    /**
     * Launch the dashboard
     */
    public static void launch() {
        System.out.println("🚀 Launching Dashboard...\n");

        // Initialize repositories
        ExportDataRepository exportRepo = new ExportDataRepository();
        PredictionRepository predictionRepo = new PredictionRepository();
        ReportRepository reportRepo = new ReportRepository();

        // Load sample data
        System.out.println("📊 Loading sample data...");
        DataInitializer.initializeAllData(exportRepo, predictionRepo, reportRepo);

        // Create MVC components
        DashboardModel model = new DashboardModel(exportRepo, predictionRepo, reportRepo);
        DashboardView view = new DashboardView();
        DashboardController controller = new DashboardController(model, view);

        // Add observer for logging
        controller.addObserver(new LoggingObserver());

        // Start the dashboard
        System.out.println("✅ Dashboard ready!\n");
        controller.start();
    }

    /**
     * Example observer implementation for logging
     */
    private static class LoggingObserver implements DashboardObserver {

        @Override
        public void onDataUpdated(UpdateEvent event) {
            logEvent("DATA UPDATE", event);
        }

        @Override
        public void onPredictionCompleted(UpdateEvent event) {
            logEvent("PREDICTION", event);
        }

        @Override
        public void onReportGenerated(UpdateEvent event) {
            logEvent("REPORT", event);
        }

        private void logEvent(String type, UpdateEvent event) {
            System.out.printf("[LOG] %s: %s (at %d)%n",
                    type, event.message(), event.timestamp());
        }
    }
}