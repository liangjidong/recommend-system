package pojo;

/**
 * Created by author on 17-12-6.
 */
public class ExperimentResult {
    private double mae;
    private double precision;
    private double recall;
    private double coverage;

    public double getMae() {
        return mae;
    }

    public void setMae(double mae) {
        this.mae = mae;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getCoverage() {
        return coverage;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }
}
