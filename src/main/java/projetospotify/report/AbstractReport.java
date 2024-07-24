// src/main/java/projetospotify/report/AbstractReport.java
package projetospotify.report;

public abstract class AbstractReport {
    private String reportName;

    public AbstractReport(String reportName) {
        this.reportName = reportName;
    }

    public String getReportName() {
        return reportName;
    }

    public abstract void generateReport();
}
