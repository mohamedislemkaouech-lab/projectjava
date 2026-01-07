package tn.agricultureai.ui.dashboard;

import tn.agricultureai.domain.model.*;
import tn.agricultureai.repository.*;
import tn.agricultureai.service.prediction.PredictionService;
import tn.agricultureai.service.report.ReportGenerator;
import java.util.*;

public class DashboardModel {

    private final ExportDataRepository exportRepository;
    private final PredictionRepository predictionRepository;
    private final ReportRepository reportRepository;

    private PredictionService activePredictionService;
    private ReportGenerator activeReportGenerator;

    private List<PredictionResult> currentPredictions;
    private MarketReport currentReport;

    private ProductType selectedProduct;
    private Country selectedCountry;

    public DashboardModel(
            ExportDataRepository exportRepository,
            PredictionRepository predictionRepository,
            ReportRepository reportRepository
    ) {
        this.exportRepository = exportRepository;
        this.predictionRepository = predictionRepository;
        this.reportRepository = reportRepository;
        this.currentPredictions = new ArrayList<>();
    }

    public ExportDataRepository getExportRepository() {
        return exportRepository;
    }

    public PredictionRepository getPredictionRepository() {
        return predictionRepository;
    }

    public ReportRepository getReportRepository() {
        return reportRepository;
    }

    public PredictionService getActivePredictionService() {
        return activePredictionService;
    }

    public void setActivePredictionService(PredictionService service) {
        this.activePredictionService = service;
    }

    public ReportGenerator getActiveReportGenerator() {
        return activeReportGenerator;
    }

    public void setActiveReportGenerator(ReportGenerator generator) {
        this.activeReportGenerator = generator;
    }

    public List<PredictionResult> getCurrentPredictions() {
        return new ArrayList<>(currentPredictions);
    }

    public void setCurrentPredictions(List<PredictionResult> predictions) {
        this.currentPredictions = new ArrayList<>(predictions);
    }

    public void addPrediction(PredictionResult prediction) {
        this.currentPredictions.add(prediction);
    }

    public MarketReport getCurrentReport() {
        return currentReport;
    }

    public void setCurrentReport(MarketReport report) {
        this.currentReport = report;
    }

    public ProductType getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(ProductType product) {
        this.selectedProduct = product;
    }

    public Country getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(Country country) {
        this.selectedCountry = country;
    }

    public DashboardStatistics getStatistics() {
        long totalExports = exportRepository.count();
        long totalPredictions = predictionRepository.count();
        long totalReports = reportRepository.count();

        // FIXED LINE 116: pricePerUnit to pricePerTon
        double avgExportPrice = exportRepository.findAll().stream()
                .mapToDouble(ExportData::pricePerTon)
                .average()
                .orElse(0.0);

        double avgPredictionConfidence = predictionRepository.findAll().stream()
                .mapToDouble(PredictionResult::confidenceScore)
                .average()
                .orElse(0.0);

        return new DashboardStatistics(
                totalExports,
                totalPredictions,
                totalReports,
                avgExportPrice,
                avgPredictionConfidence
        );
    }

    public record DashboardStatistics(
            long totalExports,
            long totalPredictions,
            long totalReports,
            double averageExportPrice,
            double averagePredictionConfidence
    ) {}
}