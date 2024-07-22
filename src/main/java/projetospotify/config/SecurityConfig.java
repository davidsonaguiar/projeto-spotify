package projetospotify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/login", "/callback", "/css/**", "/images/**").permitAll() // Permite acesso sem autenticação
                                .anyRequest().authenticated() // Outras requisições exigem autenticação
                )
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .loginPage("/login")  // Página de login personalizada
                                .defaultSuccessUrl("/home", true)
                )
                .logout(logout ->
                        logout
                                .logoutSuccessUrl("/login")
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies("JSESSIONID")
                )
                .csrf(AbstractHttpConfigurer::disable); // Desabilitar CSRF para simplificação, em ambiente de produção deve ser configurado corretamente

        return http.build();
    }
}
