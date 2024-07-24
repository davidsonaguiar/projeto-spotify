package projetospotify.config;

import org.springframework.stereotype.Component;

@Component
public class AppState {
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
