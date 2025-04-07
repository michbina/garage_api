package com.garage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// Configuration des règles d'autorisation
				.authorizeHttpRequests(authorize -> authorize
						// Ressources statiques et pages d'inscription accessibles sans authentification
						.requestMatchers("/css/**", "/js/**", "/register", "/api/register").permitAll()
						// Page de connexion accessible sans authentification
						.requestMatchers("/login").permitAll()
						// page admin restreint
						.requestMatchers("/admin/**").authenticated()
						//.requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN") // Restreint l'accès à /admin/
						// Page d'informations utilisateur nécessite une authentification
						.requestMatchers("/api/user/info").authenticated()
						// Toutes les autres pages nécessitent une authentification
						.anyRequest().authenticated())
				// Configuration du formulaire de connexion
//            .formLogin(formLogin -> formLogin
//                .loginPage("/login")
//                .defaultSuccessUrl("/factures", true)
//                .permitAll()
//            )

				.formLogin(formLogin -> formLogin.loginPage("/login")
						.successHandler((request, response, authentication) -> {
							// Redirection différente selon le rôle
							for (GrantedAuthority auth : authentication.getAuthorities()) {
								if (auth.getAuthority().equals("ROLE_ADMIN")) {
									response.sendRedirect("/admin");
									return;
								}
							}
							response.sendRedirect("/factures");
						}).permitAll())
				// Configuration de la déconnexion
				.logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
						.logoutSuccessUrl("/login?logout").invalidateHttpSession(true).clearAuthentication(true)
						.deleteCookies("JSESSIONID").permitAll())
				// Gestion des sessions
				.sessionManagement(session -> session.maximumSessions(1).expiredUrl("/login?expired"))
				// Protection CSRF (utilisez l'une des options ci-dessous)
				// Option 1: Configuration par défaut (recommandée)
				.csrf(Customizer.withDefaults());

		// Option 2: Si vous avez besoin de personnaliser
		// .csrf(csrf -> {
		// // Votre configuration CSRF personnalisée ici
		// });

		// Option 3: Pour désactiver CSRF (déconseillé en production)
		// .csrf(csrf -> csrf.disable());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}