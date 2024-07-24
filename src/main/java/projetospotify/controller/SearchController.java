package projetospotify.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import projetospotify.config.AppState;
import projetospotify.model.User;
import projetospotify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private AppState appState;

    @Autowired
    private UserRepository userRepository;

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

    @PostMapping("/createPlaylist")
    @ResponseBody
    public Map<String, Object> createPlaylist(@RequestBody Map<String, Object> payload, @AuthenticationPrincipal OidcUser principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado.");
            return response;
        }

        try {
            String name = (String) payload.get("name");
            String description = (String) payload.get("description");
            boolean isPublic = (Boolean) payload.get("isPublic");

            String userId = principal.getAttribute("sub");
            User user = userRepository.findBySpotifyId(userId);
            String accessToken = appState.getAccessToken();

            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.spotify.com/v1/users/" + userId + "/playlists";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("description", description);
            body.put("public", isPublic);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> spotifyResponse = restTemplate.postForEntity(url, request, String.class);

            if (spotifyResponse.getStatusCode() == HttpStatus.CREATED) {
                response.put("success", true);
                response.put("message", "Playlist criada com sucesso!");
            } else {
                response.put("success", false);
                response.put("message", "Falha ao criar playlist: " + spotifyResponse.getBody());
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao criar playlist: " + e.getMessage());
        }
        return response;
    }
}
