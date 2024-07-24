package projetospotify.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import projetospotify.model.User;
import projetospotify.repository.UserRepository;


@Controller
public class SearchController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/search")
    public String search(@RequestParam(value = "query", required = false) String query, Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login"; // Redireciona para a página de login se o ID do usuário não estiver presente
        }

        User user = userRepository.findById(Long.parseLong(userId)).orElse(null);

        if (user == null) {
            return "redirect:/login"; // Redireciona para a página de login se o usuário não for encontrado
        }

        String accessToken = user.getAccessToken();

        if (accessToken == null) {
            return "redirect:/login"; // Redireciona para a página de login se o token de acesso não estiver presente
        }

        if (query != null && !query.isEmpty()) {
            RestTemplate restTemplate = new RestTemplate();
            String searchUrl = "https://api.spotify.com/v1/search?q=" + query + "&type=playlist";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("results", response.getBody());
            } else {
                model.addAttribute("error", "Failed to retrieve search results");
            }
        }

        return "searchResults"; // Retorna a página de resultados da pesquisa
    }
}
