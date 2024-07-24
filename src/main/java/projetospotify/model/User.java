package projetospotify.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String spotifyId;
    private String displayName;
    private String email;
    private String country;
    private int followers;
    private String profileImageUrl;

public User() {
}

public User(String spotifyId, String displayName, String email, String country, int followers, String profileImageUrl) {
    this.spotifyId = spotifyId;
    this.displayName = displayName;
    this.email = email;
    this.country = country;
    this.followers = followers;
    this.profileImageUrl = profileImageUrl;
    }
}
