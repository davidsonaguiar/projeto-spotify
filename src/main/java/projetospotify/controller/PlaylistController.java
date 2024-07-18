package projetospotify.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
public class PlaylistController {

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    @GetMapping("/playlist")
    public String getPlaylist(@RequestParam("id") String playlistId,
                              @RegisteredOAuth2AuthorizedClient("spotify") OAuth2AuthorizedClient authorizedClient,
                              Model model) {
        if (authorizedClient != null) {
            String accessToken = authorizedClient.getAccessToken().getTokenValue();

            RestTemplate restTemplate = new RestTemplate();
            String playlistUrl = "https://api.spotify.com/v1/playlists/" + playlistId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> playlistResponse = restTemplate.exchange(playlistUrl, HttpMethod.GET, entity, Map.class);

            if (playlistResponse.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("playlist", playlistResponse.getBody());
            } else {
                model.addAttribute("error", "Unable to fetch playlist details");
            }
        }

        return "playlist";
    }
}
