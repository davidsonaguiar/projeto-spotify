package projetospotify.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import projetospotify.config.AppState;
import projetospotify.model.User;
import projetospotify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;

    @Autowired
    private AppState appState;

    @Autowired
    public SearchController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.spotify.redirect-uri}")
    private String redirectUri;

    @GetMapping("/searchResults")
    public String searchResults(@RequestParam("query") String query, Model model) {
        String accessToken = appState.getAccessToken();

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.spotify.com/v1/search?q=" + query + "&type=track,artist,album";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        try {
            Map<String, Object> searchResults = objectMapper.readValue(response.getBody(), Map.class);
            model.addAttribute("searchResults", searchResults);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to process search results.");
        }

        return "searchResults";
    }

    @PostMapping("/createPlaylist")
    public String createPlaylist(@RequestParam String name, @RequestParam String description, @RequestParam(required = false) boolean isPublic, @AuthenticationPrincipal OidcUser principal, Model model) {
        String userId = principal.getAttribute("sub");
        User user = userRepository.findBySpotifyId(userId);
        String accessToken = appState.getAccessToken();

        // Verificação do token de acesso
        if (accessToken == null || accessToken.isEmpty()) {
            model.addAttribute("message", "Access token is missing.");
            return "redirect:/home";
        }

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

        // Log da solicitação para depuração
        logger.info("Creating playlist with request body: " + body);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        // Análise da resposta
        if (response.getStatusCode() == HttpStatus.CREATED) {
            logger.info("Playlist created successfully: " + response.getBody());
            model.addAttribute("message", "Playlist created successfully.");
        } else {
            logger.error("Failed to create playlist: " + response.getBody());
            model.addAttribute("message", "Failed to create playlist.");
            model.addAttribute("errorDetails", response.getBody());

            // Verificação adicional da resposta
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.error("Unauthorized - Invalid token.");
                model.addAttribute("errorDetails", "Unauthorized - Invalid token.");
            } else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.error("Forbidden - Check your permissions.");
                model.addAttribute("errorDetails", "Forbidden - Check your permissions.");
            } else {
                logger.error("Unexpected error: " + response.getStatusCode());
                model.addAttribute("errorDetails", "Unexpected error: " + response.getStatusCode());
            }
        }

        return "redirect:/home";
    }

}
