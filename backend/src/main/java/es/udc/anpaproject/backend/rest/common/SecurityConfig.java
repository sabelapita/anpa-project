package es.udc.anpaproject.backend.rest.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) 
public class SecurityConfig {

    @Autowired
    private JwtGenerator jwtGenerator;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtFilter(jwtGenerator), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
				//users
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                .requestMatchers(HttpMethod.POST, "/users/signUp").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/loginFromServiceToken").permitAll()
                .requestMatchers(HttpMethod.PUT, "/users/*").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/users/*/changePassword").hasRole("USER")
               
				// Activities
                .requestMatchers(HttpMethod.GET, "/activities/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/activities/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/activities/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/activities/**").hasRole("ADMIN")
                
                // ENROLLMENTS
                .requestMatchers(HttpMethod.POST, "/enrollments/**").hasRole("USER")
                .requestMatchers(HttpMethod.DELETE, "/enrollments/**").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/enrollments/my").hasRole("USER")
                .requestMatchers(HttpMethod.GET, "/enrollments/**").hasRole("ADMIN")

                // BLOG
                .requestMatchers(HttpMethod.GET, "/posts/admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/posts/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/posts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/posts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/posts/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
                .anyRequest().denyAll()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("http://localhost:3000")); 
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setExposedHeaders(List.of("Authorization")); // por si el front lee el token
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
