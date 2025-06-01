package com.garage.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.garage.model.User;
import com.garage.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final UserService userService;

    public CustomSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user.getFirstLogin()) {
            // Rediriger vers page changer mot de passe
            this.setDefaultTargetUrl("/change-password");
        } else {
            // Rediriger selon rôle
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                this.setDefaultTargetUrl("/admin");
            } else {
                this.setDefaultTargetUrl("/factures");
            }
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
