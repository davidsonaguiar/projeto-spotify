package projetospotify.controller;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import projetospotify.model.User;
import projetospotify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.spotify.redirect-uri}")
    private String redirectUri;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/login")
    public String login() {
        return "login"; // Retorna a página login.html
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code, @RequestParam("state") String state, Model model) {
        logger.info("Received callback with code: {} and state: {}", code, state);

        // Exchange the authorization code for an access token
        String url = "https://accounts.spotify.com/api/token";

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedCredentials);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            String accessToken = (String) responseBody.get("access_token");
            String refreshToken = (String) responseBody.get("refresh_token");

            // Use the access token to access the Spotify Web API
            String userInfoUrl = "https://api.spotify.com/v1/me";
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoRequest, Map.class);

            if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                model.addAttribute("userInfo", userInfo);

                // Save user info in the database
                User user = new User();
                user.setSpotifyId((String) userInfo.get("id"));
                user.setDisplayName((String) userInfo.get("display_name"));
                user.setEmail((String) userInfo.get("email"));
                user.setCountry((String) userInfo.get("country"));
                user.setFollowers((int) ((Map<String, Object>) userInfo.get("followers")).get("total"));

                // Verifique se a lista de imagens não está vazia antes de tentar acessar a URL da imagem
                List<Map<String, Object>> images = (List<Map<String, Object>>) userInfo.get("images");
                if (images != null && !images.isEmpty()) {
                    user.setProfileImageUrl((String) images.get(0).get("url"));
                }

                userRepository.save(user);
            }
        } else {
            logger.error("Failed to retrieve access token: {}", response.getBody());
        }

        return "redirect:/profile";
    }

    @GetMapping("/profile")
    public String profile(Model model, @AuthenticationPrincipal OidcUser principal) {
        String accessToken = (String) principal.getIdToken().getTokenValue();

        // Fetch user profile details
        String userProfileUrl = "https://api.spotify.com/v1/me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userProfileUrl, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> profile = response.getBody();
            model.addAttribute("profile", profile);
        }

        // Fetch user's top artists
        String topArtistsUrl = "https://api.spotify.com/v1/me/top/artists";
        HttpHeaders topArtistsHeaders = new HttpHeaders();
        topArtistsHeaders.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> topArtistsRequest = new HttpEntity<>(topArtistsHeaders);
        ResponseEntity<Map> topArtistsResponse = restTemplate.exchange(topArtistsUrl, HttpMethod.GET, topArtistsRequest, Map.class);

        if (topArtistsResponse.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> topArtists = topArtistsResponse.getBody();
            model.addAttribute("topArtists", topArtists);
        }

        return "profile";
    }

    @GetMapping("/playlists")
    public String playlists(Model model, @AuthenticationPrincipal OidcUser principal) {
        String accessToken = (String) principal.getIdToken().getTokenValue();

        // Fetch user's playlists
        String playlistsUrl = "https://api.spotify.com/v1/me/playlists";
        HttpHeaders playlistsHeaders = new HttpHeaders();
        playlistsHeaders.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> playlistsRequest = new HttpEntity<>(playlistsHeaders);
        ResponseEntity<Map> playlistsResponse = restTemplate.exchange(playlistsUrl, HttpMethod.GET, playlistsRequest, Map.class);

        if (playlistsResponse.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> playlists = playlistsResponse.getBody();
            model.addAttribute("playlists", playlists);
        }

        return "playlists";
    }

    @GetMapping("/playlists/{playlistId}")
    public String playlistDetail(@PathVariable String playlistId, Model model, @AuthenticationPrincipal OidcUser principal) {
        String accessToken = (String) principal.getIdToken().getTokenValue();

        // Fetch playlist details
        String playlistUrl = "https://api.spotify.com/v1/playlists/" + playlistId;
        HttpHeaders playlistHeaders = new HttpHeaders();
        playlistHeaders.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> playlistRequest = new HttpEntity<>(playlistHeaders);
        ResponseEntity<Map> playlistResponse = restTemplate.exchange(playlistUrl, HttpMethod.GET, playlistRequest, Map.class);

        if (playlistResponse.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> playlist = playlistResponse.getBody();
            model.addAttribute("playlist", playlist);
        }

        return "playlistDetail";
    }

    @GetMapping("/logout")
    public String logout() {
        logger.info("User has been logged out.");
        return "redirect:/login?logout"; // Redireciona para a página de login após o logout
    }
}
