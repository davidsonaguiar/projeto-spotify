package projetospotify.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import projetospotify.config.AppState;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;

@Controller
public class SearchController {

    @Autowired
    private AppState appState;

    @GetMapping("/searchResults")
    public String searchResults(@RequestParam("query") String query, Model model) {
        String accessToken = appState.getAccessToken();

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.spotify.com/v1/search?q=" + query + "&type=track,artist,album";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> searchResults = mapper.readValue(response.getBody(), Map.class);
            model.addAttribute("searchResults", searchResults);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to process search results.");
        }

        return "searchResults";
    }
}
