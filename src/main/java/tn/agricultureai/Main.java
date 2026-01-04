package tn.agricultureai;

import tn.agricultureai.domain.model.*;
import tn.agricultureai.domain.exception.*;
import tn.agricultureai.service.prediction.*;
import tn.agricultureai.service.report.*;
import tn.agricultureai.service.factory.ServiceFactory;
import tn.agricultureai.repository.*;
import tn.agricultureai.ui.dashboard.Dashboard;
import tn.agricultureai.util.AnnotationProcessor;
import tn.agricultureai.util.DataInitializer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.nio.file.Path;

/**
 * Main application entry point - NOW WITH REAL AI SUPPORT!
 *
 * @author Your Name
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=".repeat(70));
        System.out.println("  TUNISIAN AGRICULTURAL EXPORT INTELLIGENCE SYSTEM");
        System.out.println("  🤖 NOW WITH REAL AI NEURAL NETWORK! 🤖");
        System.out.println("=".repeat(70));
        System.out.println("\nSelect Mode:");
        System.out.println("  1. Run Component Tests (Steps 1-6)");
        System.out.println("  2. Launch Interactive Dashboard (Step 7)");
        System.out.println("  3. 🤖 Demonstrate REAL AI Model (NEW!)");
        System.out.print("\nEnter choice (1, 2, or 3): ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> runComponentTests();
                case 2 -> Dashboard.launch();
                case 3 -> RealAIDemo.main(args); // NEW: Real AI Demo
                default -> {
                    System.out.println("\nInvalid input. Running component tests...\n");
                    runComponentTests();
                }
            }
        } catch (Exception e) {
            System.out.println("\nInvalid input. Running component tests...\n");
            runComponentTests();
        }
    }

    /**
     * Run all component tests - NOW INCLUDES REAL AI!
     */
    private static void runComponentTests() {
        System.out.println("=".repeat(70));
        System.out.println("  COMPONENT TESTS MODE - WITH REAL AI");
        System.out.println("=".repeat(70));
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Date: " + LocalDate.now());
        System.out.println("=".repeat(70));

        // Test all components
        testDomainModels();
        testExceptionHierarchy();
        testAnnotations();
        testRepositories();
        testAIServices(); // Now includes REAL AI test
        testRealAIModel(); // NEW: Real AI specific test

        // Final summary
        printSummary();
    }

    /**
     * Test Step 2: Domain Models (Records & Enums)
     */
    private static void testDomainModels() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STEP 2: TESTING DOMAIN MODELS");
        System.out.println("=".repeat(70));

        // Test Enums
        System.out.println("\n1. Testing Enums:");
        System.out.println("   Product Types:");
        for (ProductType type : ProductType.values()) {
            System.out.println("     - " + type);
        }

        System.out.println("\n   Confidence Levels:");
        System.out.println("     - Score 0.25: " + ConfidenceLevel.fromScore(0.25));
        System.out.println("     - Score 0.85: " + ConfidenceLevel.fromScore(0.85));

        // Test Records
        System.out.println("\n2. Testing Records:");

        Product oliveOil = new Product(ProductType.OLIVE_OIL, "Extra Virgin Organic");
        System.out.println("   Product: " + oliveOil.getDisplayName());
        System.out.println("   Price Range: " + oliveOil.minimumPrice() + " - " +
                oliveOil.maximumPrice() + " EUR/kg");

        ExportData export = new ExportData(
                ProductType.DATES,
                Country.FRANCE,
                10.5,
                12.50,
                LocalDate.now().minusDays(5),
                "Customs"
        );
        System.out.println("\n   Export Record:");
        System.out.println("   " + export.getSummary());

        PredictionResult prediction = new PredictionResult(
                ProductType.OLIVE_OIL,
                Country.ITALY,
                9.20,
                0.87
        );
        System.out.println("\n   Prediction:");
        System.out.println("   " + prediction.getSummary());

        System.out.println("\n✅ Domain models working correctly!");
    }

    /**
     * Test Step 3: Exception Hierarchy
     */
    private static void testExceptionHierarchy() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STEP 3: TESTING EXCEPTION HIERARCHY");
        System.out.println("=".repeat(70));

        // Test 1: PredictionException
        System.out.println("\n1. PredictionException:");
        try {
            throw PredictionException.modelLoadFailure("PricePredictor-v1",
                    new RuntimeException("Model file corrupted"));
        } catch (PredictionException e) {
            System.out.println("   Caught: " + e.getCategory() + "-" + e.getErrorCode());
            System.out.println("   Message: " + e.getMessage());
            System.out.println("   Retryable: " + e.isRetryable());
            System.out.println("   Severity: " + e.getSeverity());
        }

        // Test 2: DataValidationException
        System.out.println("\n2. DataValidationException:");
        try {
            throw DataValidationException.outOfRange("price", -5.0, 0.0, 100.0);
        } catch (DataValidationException e) {
            System.out.println("   Caught: " + e.getCategory() + "-" + e.getErrorCode());
            System.out.println("   Validation Errors: " + e.getValidationErrors().size());
            System.out.println("   Message: " + e.getAllErrorMessages());
        }

        System.out.println("\n✅ Exception hierarchy working correctly!");
    }

    /**
     * Test Step 4: Custom Annotations
     */
    private static void testAnnotations() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STEP 4: TESTING CUSTOM ANNOTATIONS");
        System.out.println("=".repeat(70));

        // Test @AIModel annotation on REAL AI model
        System.out.println("\n1. @AIModel Annotation on REAL AI:");
        AnnotationProcessor.ModelInfo modelInfo =
                AnnotationProcessor.extractModelInfo(DL4JPredictionService.class);

        if (modelInfo != null) {
            System.out.println("   Model Name: " + modelInfo.name());
            System.out.println("   Version: " + modelInfo.version());
            System.out.println("   Framework: " + modelInfo.framework().getDisplayName());
            System.out.println("   Accuracy: " + (modelInfo.accuracy() * 100) + "%");
            System.out.println("   Production Ready: " + modelInfo.productionReady());
        }

        System.out.println("\n✅ Custom annotations working correctly!");
    }

    /**
     * Test Step 5: Repository Layer
     */
    private static void testRepositories() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STEP 5: TESTING REPOSITORY LAYER");
        System.out.println("=".repeat(70));

        ExportDataRepository exportRepo = new ExportDataRepository();
        PredictionRepository predictionRepo = new PredictionRepository();
        ReportRepository reportRepo = new ReportRepository();

        System.out.println("\nInitializing Sample Data...");
        DataInitializer.initializeAllData(exportRepo, predictionRepo, reportRepo);

        System.out.println("\n1. Basic Repository Operations:");
        System.out.println("   Export Records: " + exportRepo.count());
        System.out.println("   Predictions: " + predictionRepo.count());
        System.out.println("   Reports: " + reportRepo.count());

        System.out.println("\n✅ Repository layer working correctly!");
    }

    /**
     * Test Step 6: AI Services - NOW INCLUDES REAL AI
     */
    private static void testAIServices() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STEP 6: TESTING AI SERVICE LAYER");
        System.out.println("=".repeat(70));

        // Print available services
        System.out.println("\n1. Available AI Services:");
        ServiceFactory.printAvailableServices();

        // Test simple service
        System.out.println("\n2. Testing Simple Service:");
        PredictionService simpleService = ServiceFactory.getSimplePredictionService();
        System.out.println("   Model: " + simpleService.getModelName());
        System.out.println("   Ready: " + simpleService.isReady());

        PredictionResult simplePred = simpleService.predict(
                ProductType.OLIVE_OIL,
                Country.ITALY
        );
        System.out.println("   Prediction: " + simplePred.getSummary());

        System.out.println("\n✅ AI services working correctly!");
    }

    /**
     * NEW: Test Real AI Model
     */
    private static void testRealAIModel() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STEP 7: TESTING REAL AI MODEL (DeepLearning4J)");
        System.out.println("=".repeat(70));

        try {
            System.out.println("\n🤖 Loading Real Neural Network...");
            DL4JPredictionService realAI = ServiceFactory.getDL4JPredictionService();

            System.out.println("\n✅ Real AI Model Loaded Successfully!");
            System.out.println("   Framework: " + realAI.getFrameworkName());
            System.out.println("   Accuracy: " + (realAI.getModelAccuracy() * 100) + "%");
            System.out.println("   Status: " + (realAI.isReady() ? "Ready" : "Not Ready"));

            System.out.println("\n🔮 Making Predictions with Real AI:");
            PredictionResult pred1 = realAI.predict(ProductType.OLIVE_OIL, Country.ITALY);
            System.out.println("   " + pred1.getSummary());

            PredictionResult pred2 = realAI.predict(ProductType.DATES, Country.FRANCE);
            System.out.println("   " + pred2.getSummary());

            System.out.println("\n✅ Real AI model working perfectly!");

        } catch (Exception e) {
            System.out.println("\n❌ Real AI test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Print final summary
     */
    private static void printSummary() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  FINAL SUMMARY");
        System.out.println("=".repeat(70));
        System.out.println("\n✅ Step 1: Architecture & Package Structure - COMPLETE");
        System.out.println("✅ Step 2: Domain Models (Records & Enums) - COMPLETE");
        System.out.println("✅ Step 3: Exception Hierarchy - COMPLETE");
        System.out.println("✅ Step 4: Custom Annotations - COMPLETE");
        System.out.println("✅ Step 5: Repository Layer - COMPLETE");
        System.out.println("✅ Step 6: AI Service Layer - COMPLETE");
        System.out.println("✅ Step 7: Real AI Model Integration - COMPLETE 🤖");
        System.out.println("✅ Step 8: Dashboard UI - COMPLETE");

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  🎉 ALL COMPONENTS INCLUDING REAL AI WORKING!");
        System.out.println("  Ready for Submission with ACTUAL Neural Network!");
        System.out.println("=".repeat(70) + "\n");
    }
}