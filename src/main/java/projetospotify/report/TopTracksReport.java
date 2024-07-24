package projetospotify.report;

import java.util.List;
import java.util.Map;

public class TopTracksReport extends AbstractReport {
    private Map<String, Object> reportData;

    public TopTracksReport(Map<String, Object> reportData) {
        super("Top Tracks Report");
        this.reportData = reportData;
    }

    @Override
    public void generateReport() {
        // Implementação específica para top tracks
        List<Map<String, Object>> tracks = (List<Map<String, Object>>) reportData.get("items");
        System.out.println("Top Tracks:");
        for (Map<String, Object> track : tracks) {
            System.out.println("Track Name: " + track.get("name"));
            System.out.println("Artist: " + ((List<Map<String, Object>>) track.get("artists")).get(0).get("name"));
            System.out.println("Album: " + ((Map<String, Object>) track.get("album")).get("name"));
            System.out.println("Image: " + ((List<Map<String, Object>>) ((Map<String, Object>) track.get("album")).get("images")).get(0).get("url"));
            System.out.println("----");
        }
    }

    public Map<String, Object> getReportData() {
        return reportData;
    }
}
