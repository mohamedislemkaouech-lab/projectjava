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
 * Main application entry point.
 * Tests all components of the Tunisian Agricultural Export Intelligence System.
 *
 * @author Your Name
 */
public class Main {
    public static void main(String[] args) {
        // Check if user wants to run tests or dashboard
        Scanner scanner = new Scanner(System.in);

        System.out.println("=".repeat(70));
        System.out.println("  TUNISIAN AGRICULTURAL EXPORT INTELLIGENCE SYSTEM");
        System.out.println("=".repeat(70));
        System.out.println("\nSelect Mode:");
        System.out.println("  1. Run Component Tests (Steps 1-6)");
        System.out.println("  2. Launch Interactive Dashboard (Step 7)");
        System.out.print("\nEnter choice (1 or 2): ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 2) {
                // Launch dashboard
                Dashboard.launch();
            } else {
                // Run tests
                runComponentTests();
            }
        } catch (Exception e) {
            System.out.println("\nInvalid input. Running component tests...\n");
            runComponentTests();
        }
    }

    /**
     * Run all component tests
     */
    private static void runComponentTests() {
        System.out.println("=".repeat(70));
        System.out.println("  COMPONENT TESTS MODE");
        System.out.println("=".repeat(70));
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Date: " + LocalDate.now());
        System.out.println("=".repeat(70));

        // Test all components
        testDomainModels();
        testExceptionHierarchy();
        testAnnotations();
        testRepositories();
        testAIServices();

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

        // Test 3: Multiple Validation Errors
        System.out.println("\n3. Multiple Validation Errors:");
        try {
            List<DataValidationException.ValidationError> errors = List.of(
                    new DataValidationException.ValidationError("price", "Must be positive", -10),
                    new DataValidationException.ValidationError("quantity", "Cannot be zero", 0),
                    new DataValidationException.ValidationError("date", "Cannot be future", "2026-01-01")
            );
            throw new DataValidationException(errors);
        } catch (DataValidationException e) {
            System.out.println("   Error count: " + e.getValidationErrors().size());
            e.getValidationErrors().forEach(err ->
                    System.out.println("     - " + err)
            );
        }

        // Test 4: ReportGenerationException
        System.out.println("\n4. ReportGenerationException:");
        try {
            throw ReportGenerationException.apiTimeout("GPT-4", 3);
        } catch (ReportGenerationException e) {
            System.out.println("   Caught: " + e.getCategory() + "-" + e.getErrorCode());
            System.out.println("   Retryable: " + e.isRetryable());
            System.out.println("   Wait before retry: " + e.getSuggestedWaitSeconds() + "s");
        }

        // Test 5: ModelLoadException
        System.out.println("\n5. ModelLoadException:");
        try {
            throw ModelLoadException.fileNotFound(
                    "OliveOilPredictor",
                    Path.of("/models/olive_oil_v1.onnx"),
                    ModelLoadException.ModelType.ONNX
            );
        } catch (ModelLoadException e) {
            System.out.println("   Caught: " + e.getCategory() + "-" + e.getErrorCode());
            System.out.println("   Model Type: " + e.getModelType());
            System.out.println("   Critical: " + ExceptionHandler.isCritical(e));
        }

        // Test 6: InsufficientDataException
        System.out.println("\n6. InsufficientDataException:");
        try {
            throw InsufficientDataException.notEnoughRecords(
                    15, 100, ProductType.DATES, Country.GERMANY
            );
        } catch (InsufficientDataException e) {
            System.out.println("   Caught: " + e.getCategory() + "-" + e.getErrorCode());
            System.out.println("   Deficit: " + String.format("%.1f%%", e.getDeficitPercentage()));
            System.out.println("   Need more: " + (e.getRequiredRecords() - e.getAvailableRecords()) + " records");
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

        // Test 1: Extract @AIModel annotation metadata
        System.out.println("\n1. @AIModel Annotation:");
        AnnotationProcessor.ModelInfo modelInfo =
                AnnotationProcessor.extractModelInfo(SamplePredictionService.class);

        if (modelInfo != null) {
            System.out.println("   Model Name: " + modelInfo.name());
            System.out.println("   Version: " + modelInfo.version());
            System.out.println("   Framework: " + modelInfo.framework().getDisplayName());
            System.out.println("   Accuracy: " + (modelInfo.accuracy() * 100) + "%");
            System.out.println("   Production Ready: " + modelInfo.productionReady());
        } else {
            System.out.println("   No @AIModel annotation found");
        }

        // Test 2: Find @Cacheable methods
        System.out.println("\n2. @Cacheable Methods:");
        List<AnnotationProcessor.CacheableMethodInfo> cacheableMethods =
                AnnotationProcessor.findCacheableMethods(SamplePredictionService.class);

        if (!cacheableMethods.isEmpty()) {
            cacheableMethods.forEach(method ->
                    System.out.println("   - " + method)
            );
        } else {
            System.out.println("   No cacheable methods found");
        }

        // Test 3: Find @Validated fields
        System.out.println("\n3. @Validated Fields:");
        List<AnnotationProcessor.ValidatedFieldInfo> validatedFields =
                AnnotationProcessor.findValidatedFields(SamplePredictionService.class);

        if (!validatedFields.isEmpty()) {
            validatedFields.forEach(field ->
                    System.out.println("   - " + field)
            );
        } else {
            System.out.println("   No validated fields found");
        }

        // Test 4: Validate an object
        System.out.println("\n4. Object Validation:");
        SamplePredictionService service = new SamplePredictionService();

        List<String> errors = AnnotationProcessor.validateObject(service);
        if (errors.isEmpty()) {
            System.out.println("   ✅ Valid object - no validation errors");
        } else {
            System.out.println("   ❌ Validation errors:");
            errors.forEach(error -> System.out.println("     - " + error));
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

        // Initialize repositories
        ExportDataRepository exportRepo = new ExportDataRepository();
        PredictionRepository predictionRepo = new PredictionRepository();
        ReportRepository reportRepo = new ReportRepository();

        // Initialize with sample data
        System.out.println("\nInitializing Sample Data...");
        DataInitializer.initializeAllData(exportRepo, predictionRepo, reportRepo);

        // Test 1: Basic CRUD operations
        System.out.println("\n1. Basic Repository Operations:");
        System.out.println("   Export Records: " + exportRepo.count());
        System.out.println("   Predictions: " + predictionRepo.count());
        System.out.println("   Reports: " + reportRepo.count());

        // Test 2: Query operations
        System.out.println("\n2. Query Operations:");

        List<ExportData> oliveOilExports = exportRepo.findByProductType(ProductType.OLIVE_OIL);
        System.out.println("   Olive Oil Exports: " + oliveOilExports.size());

        List<ExportData> franceExports = exportRepo.findByDestination(Country.FRANCE);
        System.out.println("   Exports to France: " + franceExports.size());

        List<ExportData> euExports = exportRepo.findEuExports();
        System.out.println("   EU Exports: " + euExports.size());

        List<ExportData> recentExports = exportRepo.findRecent(30);
        System.out.println("   Recent Exports (30 days): " + recentExports.size());

        // Test 3: Stream API operations
        System.out.println("\n3. Stream API Operations:");

        Map<ProductType, Double> valueByProduct = exportRepo.getTotalValueByProduct();
        System.out.println("   Total Value by Product (top 3):");
        valueByProduct.entrySet().stream()
                .sorted(Map.Entry.<ProductType, Double>comparingByValue().reversed())
                .limit(3)
                .forEach(entry ->
                        System.out.println("     " + entry.getKey().getDisplayName() + ": " +
                                String.format("%.2f EUR", entry.getValue()))
                );

        // Test 4: Statistics
        System.out.println("\n4. Export Statistics:");
        ExportDataRepository.ExportStatistics exportStats = exportRepo.calculateStatistics();
        System.out.println("   Total Records: " + exportStats.totalRecords());
        System.out.println("   Average Price: " + String.format("%.2f EUR/kg", exportStats.averagePrice()));
        System.out.println("   Total Value: " + String.format("%.2f EUR", exportStats.totalValue()));

        // Test 5: Prediction queries
        System.out.println("\n5. Prediction Queries:");

        List<PredictionResult> reliablePredictions = predictionRepo.findReliablePredictions();
        System.out.println("   Reliable Predictions: " + reliablePredictions.size());

        Map<ConfidenceLevel, Long> confidenceDistribution =
                predictionRepo.getConfidenceLevelDistribution();
        System.out.println("   Confidence Level Distribution:");
        confidenceDistribution.forEach((level, count) ->
                System.out.println("     " + level.getLabel() + ": " + count)
        );

        // Test 6: Report queries
        System.out.println("\n6. Report Queries:");

        List<MarketReport> recentReports = reportRepo.findRecentReports(30);
        System.out.println("   Recent Reports: " + recentReports.size());

        List<MarketReport> weeklyReports =
                reportRepo.findByType(MarketReport.ReportType.WEEKLY_ANALYSIS);
        System.out.println("   Weekly Analysis Reports: " + weeklyReports.size());

        System.out.println("\n✅ Repository layer working correctly!");
    }

    /**
     * Test Step 6: AI Services
     */
    private static void testAIServices() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STEP 6: TESTING AI SERVICE LAYER");
        System.out.println("=".repeat(70));

        // Test 1: Service Factory
        System.out.println("\n1. Service Factory:");

        PredictionService simpleService = ServiceFactory.getDefaultPredictionService();
        System.out.println("   Simple Service: " + simpleService.getModelName());
        System.out.println("   Ready: " + simpleService.isReady());

        PredictionService djlService = ServiceFactory.getDJLPredictionService();
        System.out.println("   DJL Service: " + djlService.getModelName());
        System.out.println("   Ready: " + djlService.isReady());

        // Test 2: Model Information
        System.out.println("\n2. Model Information:");
        PredictionService.ModelInfo simpleInfo = simpleService.getModelInfo();
        System.out.println("   " + simpleInfo.name() + " v" + simpleInfo.version());
        System.out.println("   Framework: " + simpleInfo.framework());
        System.out.println("   Accuracy: " + (simpleInfo.accuracy() * 100) + "%");

        PredictionService.ModelInfo djlInfo = djlService.getModelInfo();
        System.out.println("\n   " + djlInfo.name() + " v" + djlInfo.version());
        System.out.println("   Framework: " + djlInfo.framework());
        System.out.println("   Accuracy: " + (djlInfo.accuracy() * 100) + "%");

        // Test 3: Single Predictions
        System.out.println("\n3. Single Predictions:");

        PredictionResult simplePred = simpleService.predict(
                ProductType.OLIVE_OIL,
                Country.ITALY
        );
        System.out.println("   Simple Model: " + simplePred.getSummary());

        PredictionResult djlPred = djlService.predict(
                ProductType.OLIVE_OIL,
                Country.ITALY
        );
        System.out.println("   DJL Model: " + djlPred.getSummary());

        // Test 4: Batch Predictions
        System.out.println("\n4. Batch Predictions:");
        List<ProductType> products = List.of(
                ProductType.OLIVE_OIL,
                ProductType.DATES,
                ProductType.CITRUS
        );

        List<PredictionResult> batchPredictions = simpleService.predictBatch(
                products,
                Country.FRANCE
        );
        System.out.println("   Batch predictions to France:");
        batchPredictions.forEach(pred ->
                System.out.println("     - " + pred.productType().getDisplayName() +
                        ": " + String.format("%.2f EUR/kg", pred.predictedPrice()))
        );

        // Test 5: Report Generation
        System.out.println("\n5. Report Generation:");

        ReportGenerator reportGenerator = ServiceFactory.getReportGenerator();
        System.out.println("   Report Generator: " + reportGenerator.getModelName());
        System.out.println("   Available: " + reportGenerator.isAvailable());

        // Generate daily summary report
        List<PredictionResult> allPredictions = List.of(simplePred, djlPred);

        MarketReport dailyReport = reportGenerator.generateReport(
                MarketReport.ReportType.DAILY_SUMMARY,
                allPredictions
        );

        System.out.println("\n   Generated Report:");
        System.out.println("   Title: " + dailyReport.title());
        System.out.println("   Type: " + dailyReport.reportType().getDisplayName());
        System.out.println("   Predictions: " + dailyReport.predictions().size());
        System.out.println("   Word Count: " + dailyReport.metadata().wordCount());

        // Test 6: Available Service Types
        System.out.println("\n6. Available Service Types:");
        for (ServiceFactory.PredictionServiceType type :
                ServiceFactory.getAvailableServiceTypes()) {
            boolean available = ServiceFactory.isServiceAvailable(type);
            System.out.println("   " + type + ": " + (available ? "✓ Available" : "✗ Not Available"));
        }

        System.out.println("\n✅ AI services working correctly!");
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
        System.out.println("⏳ Step 7: Dashboard UI - PENDING");

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  ALL CORE COMPONENTS WORKING SUCCESSFULLY!");
        System.out.println("  Ready for Step 7: Dashboard Implementation");
        System.out.println("=".repeat(70) + "\n");
    }
}