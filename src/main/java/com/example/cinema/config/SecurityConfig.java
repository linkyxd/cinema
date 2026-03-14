package com.example.cinema.config;

import com.example.cinema.model.UserAccount;
import com.example.cinema.repository.UserAccountRepository;
import com.example.cinema.security.JwtAuthFilter;
import com.example.cinema.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserAccountRepository userRepo) {
        return username -> {
            UserAccount user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            UserDetails details = User.withUsername(user.getUsername())
                    .password(user.getPasswordHash())
                    .roles(user.getRole().name().replace("ROLE_", ""))
                    .build();
            return details;
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                            PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DaoAuthenticationProvider authenticationProvider,
                                                   JwtTokenProvider jwtTokenProvider) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(new JwtAuthFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .httpBasic(httpBasic -> { })
                .authorizeHttpRequests(auth -> auth
                        // Публичные
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/movies", "/api/movies/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/screenings", "/api/screenings/**").permitAll()
                        // ADMIN: movies, halls, customers, screenings
                        .requestMatchers(HttpMethod.POST, "/api/movies").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/halls").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/halls/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/halls/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/customers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/screenings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/screenings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/screenings/**").hasRole("ADMIN")
                        // ADMIN: tickets (list, update, delete)
                        .requestMatchers(HttpMethod.GET, "/api/tickets").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tickets/**").hasRole("ADMIN")
                        // USER: buy ticket
                        .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("USER")
                        // ADMIN или USER: view ticket, refund
                        .requestMatchers(HttpMethod.GET, "/api/tickets/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/tickets/refund/**").hasAnyRole("ADMIN", "USER")
                        // Остальное — авторизованным
                        .requestMatchers("/api/halls", "/api/halls/**", "/api/customers", "/api/customers/**").authenticated()
                        .requestMatchers("/api/booking/**").authenticated()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}

