package projetospotify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import projetospotify.model.User;
import projetospotify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.spotify.redirect-uri}")
    private String redirectUri;

    @GetMapping("/login")
    public String login() {
        return "login"; // Retorna a página login.html
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code, @RequestParam("state") String state, Model model) {
        logger.info("Received callback with code: {} and state: {}", code, state);

        // Exchange the authorization code for an access token
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://accounts.spotify.com/api/token";

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedCredentials);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            String accessToken = (String) responseBody.get("access_token");
            String refreshToken = (String) responseBody.get("refresh_token");

            try {
                String responseBodyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseBody);
                logger.info("Response Body JSON: {}", responseBodyJson);
            } catch (Exception e) {
                logger.error("Failed to convert response body to JSON", e);
            }

            // Use the access token to access the Spotify Web API
            String userInfoUrl = "https://api.spotify.com/v1/me";
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoRequest, Map.class);

            if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                model.addAttribute("userInfo", userInfo);

                try {
                    String userInfoJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userInfo);
                    logger.info("User Info JSON: {}", userInfoJson);

                    // Salvar informações do usuário no banco de dados
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

                } catch (Exception e) {
                    logger.error("Failed to convert user info to JSON", e);
                }
            }
        } else {
            logger.error("Failed to retrieve access token: {}", response.getBody());
        }

        return "home";
    }

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal OidcUser principal) {
        if (principal != null) {
            model.addAttribute("name", principal.getAttribute("name"));
        }
        return "home";
    }

    @GetMapping("/logout")
    public String logout() {
        logger.info("User has been logged out.");
        return "redirect:/login?logout"; // Redireciona para a página de login após o logout
    }
}
