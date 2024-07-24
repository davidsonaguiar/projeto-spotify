package projetospotify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import projetospotify.model.User;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTests {

    private User user;
    private Validator validator;

    @BeforeEach
    public void setUp() {
        user = new User();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void gettersAndSetters() {
        user.setId(1L);
        user.setSpotifyId("spotify123");
        user.setDisplayName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setCountry("US");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        assertEquals(1L, user.getId());
        assertEquals("spotify123", user.getSpotifyId());
        assertEquals("John Doe", user.getDisplayName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("US", user.getCountry());
        assertEquals(100, user.getFollowers());
        assertEquals("http://example.com/image.jpg", user.getProfileImageUrl());
    }

    @Test
    public void defaultValues() {
        assertNull(user.getId());
        assertNull(user.getSpotifyId());
        assertNull(user.getDisplayName());
        assertNull(user.getEmail());
        assertNull(user.getCountry());
        assertEquals(null, user.getFollowers());
        assertNull(user.getProfileImageUrl());
    }

    @Test
    public void equality() {
        User user1 = new User();
        user1.setId(1L);
        user1.setSpotifyId("spotify123");
        user1.setDisplayName("John Doe");
        user1.setEmail("john.doe@example.com");
        user1.setCountry("US");
        user1.setFollowers(100);
        user1.setProfileImageUrl("http://example.com/image.jpg");

        User user2 = new User();
        user2.setId(1L);
        user2.setSpotifyId("spotify123");
        user2.setDisplayName("John Doe");
        user2.setEmail("john.doe@example.com");
        user2.setCountry("US");
        user2.setFollowers(100);
        user2.setProfileImageUrl("http://example.com/image.jpg");

        assertEquals(user1, user2);
    }

    @Test
    public void differentIdsNotEqual() {
        User user1 = new User();
        user1.setId(1L);
        user1.setSpotifyId("spotify123");
        user1.setDisplayName("John Doe");
        user1.setEmail("john.doe@example.com");
        user1.setCountry("US");
        user1.setFollowers(100);
        user1.setProfileImageUrl("http://example.com/image.jpg");

        User user2 = new User();
        user2.setId(2L);
        user2.setSpotifyId("spotify123");
        user2.setDisplayName("John Doe");
        user2.setEmail("john.doe@example.com");
        user2.setCountry("US");
        user2.setFollowers(100);
        user2.setProfileImageUrl("http://example.com/image.jpg");

        assertNotEquals(user1, user2);
    }


    @Test
    public void nullEmailInvalid() {
        user.setSpotifyId("spotify123");
        user.setDisplayName("John Doe");
        user.setCountry("US");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void invalidEmailFormat() {
        user.setSpotifyId("spotify123");
        user.setDisplayName("John Doe");
        user.setEmail("invalid-email");
        user.setCountry("US");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void validUser() {
        user.setSpotifyId("spotify123");
        user.setDisplayName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setCountry("US");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void nullSpotifyIdInvalid() {
        user.setDisplayName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setCountry("US");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void blankSpotifyIdInvalid() {
        user.setSpotifyId("");
        user.setDisplayName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setCountry("US");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void nullDisplayNameInvalid() {
        user.setSpotifyId("spotify123");
        user.setEmail("john.doe@example.com");
        user.setCountry("US");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void blankDisplayNameInvalid() {
        user.setSpotifyId("spotify123");
        user.setDisplayName("");
        user.setEmail("john.doe@example.com");
        user.setCountry("US");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void nullCountryInvalid() {
        user.setSpotifyId("spotify123");
        user.setDisplayName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void blankCountryInvalid() {
        user.setSpotifyId("spotify123");
        user.setDisplayName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setCountry("");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void nullFollowersInvalid() {
        user.setSpotifyId("spotify123");
        user.setDisplayName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setCountry("US");
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void profileImageUrlCanBeNull() {
        user.setSpotifyId("spotify123");
        user.setDisplayName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setCountry("US");
        user.setFollowers(100);
        user.setProfileImageUrl(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void profileImageUrlCanBeNonNull() {
        user.setSpotifyId("spotify123");
        user.setDisplayName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setCountry("US");
        user.setFollowers(100);
        user.setProfileImageUrl("http://example.com/image.jpg");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }
}
