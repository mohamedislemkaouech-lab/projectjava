package tn.agricultureai.domain.model;

import java.util.Objects;

/**
 * Represents metadata about a machine learning model used for predictions.
 */
public class ModelInfo {
    private String modelName;
    private String version;
    private String framework;
    private double accuracy;
    private boolean isReady;

    public ModelInfo() {
    }

    public ModelInfo(String modelName, String version, String framework, double accuracy, boolean isReady) {
        this.modelName = modelName;
        this.version = version;
        this.framework = framework;
        this.accuracy = accuracy;
        this.isReady = isReady;
    }

    // Getters
    public String getModelName() {
        return modelName;
    }

    public String getVersion() {
        return version;
    }

    public String getFramework() {
        return framework;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public boolean isReady() {
        return isReady;
    }

    // Setters
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelInfo modelInfo = (ModelInfo) o;
        return Double.compare(accuracy, modelInfo.accuracy) == 0 &&
                isReady == modelInfo.isReady &&
                Objects.equals(modelName, modelInfo.modelName) &&
                Objects.equals(version, modelInfo.version) &&
                Objects.equals(framework, modelInfo.framework);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelName, version, framework, accuracy, isReady);
    }

    @Override
    public String toString() {
        return "ModelInfo{" +
                "modelName='" + modelName + '\'' +
                ", version='" + version + '\'' +
                ", framework='" + framework + '\'' +
                ", accuracy=" + accuracy +
                ", isReady=" + isReady +
                '}';
    }
}