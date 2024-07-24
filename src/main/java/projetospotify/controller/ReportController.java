package projetospotify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import projetospotify.config.AppState;
import projetospotify.report.TopTracksReport;
import projetospotify.report.TopArtistsReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private AppState appState;

    @GetMapping("/reportView")
    public String generateReport(@RequestParam("reportType") String reportType, Model model) {
        logger.info("Entering generateReport method");
        logger.info("Report Type: {}", reportType);

        String accessToken = appState.getAccessToken();

        if (accessToken == null) {
            logger.error("Access token is null");
            return "redirect:/login";
        }

        String url = null;
        if ("topTracks".equals(reportType)) {
            url = "https://api.spotify.com/v1/me/top/tracks";
        } else if ("topArtists".equals(reportType)) {
            url = "https://api.spotify.com/v1/me/top/artists";
        }

        if (url == null) {
            logger.error("Invalid report type: {}", reportType);
            return "error";
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            logger.error("HTTP error during report generation: {}", e.getMessage());
            return "error";
        }

        if (response.getStatusCode() == HttpStatus.OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseBody = null;
            try {
                responseBody = objectMapper.readValue(response.getBody(), Map.class);
            } catch (Exception e) {
                logger.error("Error parsing JSON response: {}", e.getMessage());
                return "error";
            }

            model.addAttribute("reportType", reportType);
            model.addAttribute("reportData", responseBody);

            if ("topTracks".equals(reportType)) {
                TopTracksReport topTracksReport = new TopTracksReport(responseBody);
                topTracksReport.generateReport();
            } else if ("topArtists".equals(reportType)) {
                TopArtistsReport topArtistsReport = new TopArtistsReport(responseBody);
                topArtistsReport.generateReport();
            }

            return "reportView";
        } else {
            logger.error("Failed to generate report: HTTP {}", response.getStatusCode());
            return "error";
        }
    }
}
