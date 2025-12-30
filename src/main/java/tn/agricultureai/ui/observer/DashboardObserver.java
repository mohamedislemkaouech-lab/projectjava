package tn.agricultureai.ui.observer;

/**
 * Observer interface for dashboard updates.
 * Demonstrates: Observer pattern, event-driven architecture.
 *
 * @author Your Name
 */
public interface DashboardObserver {

    /**
     * Called when dashboard data is updated
     *
     * @param event Update event with details
     */
    void onDataUpdated(UpdateEvent event);

    /**
     * Called when prediction is completed
     *
     * @param event Prediction event
     */
    void onPredictionCompleted(UpdateEvent event);

    /**
     * Called when report is generated
     *
     * @param event Report event
     */
    void onReportGenerated(UpdateEvent event);

    /**
     * Record representing an update event
     */
    record UpdateEvent(
            EventType type,
            String message,
            Object data,
            long timestamp
    ) {
        public UpdateEvent(EventType type, String message, Object data) {
            this(type, message, data, System.currentTimeMillis());
        }

        public enum EventType {
            DATA_UPDATED,
            PREDICTION_COMPLETED,
            REPORT_GENERATED,
            ERROR_OCCURRED
        }
    }
}