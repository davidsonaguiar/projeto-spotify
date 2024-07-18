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
    @Column(unique = true)
    private String spotifyId;
    private String displayName;
    @Column(unique = true)
    private String email;
    private String country;
    private int followers;
    private String profileImageUrl;

}
