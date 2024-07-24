package projetospotify.report;

import java.util.List;
import java.util.Map;

public class TopArtistsReport extends AbstractReport {
    private Map<String, Object> reportData;

    public TopArtistsReport(Map<String, Object> reportData) {
        super("Top Artists Report");
        this.reportData = reportData;
    }

    @Override
    public void generateReport() {
        // Implementação específica para top artists
        List<Map<String, Object>> artists = (List<Map<String, Object>>) reportData.get("items");
        System.out.println("Top Artists:");
        for (Map<String, Object> artist : artists) {
            System.out.println("Artist Name: " + artist.get("name"));
            System.out.println("Genres: " + artist.get("genres"));
            System.out.println("Followers: " + ((Map<String, Object>) artist.get("followers")).get("total"));
            System.out.println("Image: " + ((List<Map<String, Object>>) artist.get("images")).get(0).get("url"));
            System.out.println("----");
        }
    }

    public Map<String, Object> getReportData() {
        return reportData;
    }
}
