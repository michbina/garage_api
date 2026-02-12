package com.garage.api;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.garage.model.Garage;
import com.garage.model.Role;
import com.garage.model.User;
import com.garage.repository.GarageRepository;
import com.garage.repository.UserRepository;
import com.garage.service.GarageService;
import com.garage.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private static final Logger logger = LoggerFactory.getLogger(AdminRestController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private GarageService garageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GarageRepository garageRepository;

    // ======== DASHBOARD PRINCIPAL ========
    @GetMapping
    public List<User> adminDashboard(Authentication authentication) {
        logger.info("========== ADMIN DASHBOARD ==========");
        logger.info("Utilisateur: {}", authentication.getName());
        logger.info("Autorités: {}", authentication.getAuthorities());
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return new ArrayList<>();
        }

        List<User> clients;

        if (user.getRole().contains(Role.ROLE_SUPERADMIN.name())) {
            clients = userService.findAllClients();
        } else if (user.getRole().contains(Role.ROLE_GARAGE_ADMIN.name())) {
            List<Long> garageIds = user.getGarageIds();
            List<Garage> garages = garageRepository.findAllById(garageIds);
            clients = userService.findByGaragesIn(garages);
        } else {
            clients = new ArrayList<>();
        }

        return clients;
    }

    // ======== LISTE DES CLIENTS ========
    @GetMapping("/clients")
    public List<User> listClients() {
        logger.info("Liste des clients");
        List<User> clients = userService.findAllClients();
        return clients != null ? clients : new ArrayList<>();
    }

    // ======== DONNÉES POUR CRÉER UN UTILISATEUR ========
    @PreAuthorize("hasAnyRole('ROLE_SUPERADMIN','ROLE_GARAGE_ADMIN')")
    @GetMapping("/user/create")
    public CreateUserData showCreateUser() {
        logger.info("Préparation des données pour la création d'utilisateur");

        List<Garage> garages = new ArrayList<>(garageService.findAllGarages());

        return new CreateUserData(Role.values(), garages);
    }

    public static class CreateUserData {
        private Role[] roles;
        private List<Garage> garages;

        public CreateUserData(Role[] roles, List<Garage> garages) {
            this.roles = roles;
            this.garages = garages;
        }

        public Role[] getRoles() { return roles; }
        public List<Garage> getGarages() { return garages; }
    }

    // ======== CRÉATION D’UTILISATEUR ========
    @PreAuthorize("hasAnyRole('ROLE_SUPERADMIN','ROLE_GARAGE_ADMIN')")
    @PostMapping("/user/create")
    public ApiResponse createUser(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            logger.error("Utilisateur " + user.getUsername() + " déjà existant");
            return new ApiResponse(false, "Utilisateur déjà existant", null);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setFactures(new ArrayList<>());
        user.setDevis(new ArrayList<>());
        user.setFirstLogin(true);
        
        List<Long> garageIds = user.getGarageIds();
        if (garageIds != null && !garageIds.isEmpty()) {
            List<Garage> garages = garageRepository.findAllById(garageIds);
            user.setGarages(garages);
        }

        userRepository.save(user);
        logger.info("Utilisateur créé avec succès");

        return new ApiResponse(true, "Utilisateur créé avec succès", user);
    }

    public static class ApiResponse {
        private boolean success;
        private String message;
        private User data;

        public ApiResponse(boolean success, String message, User data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getData() { return data; }
    }

    // ======== MÉTHODE DE TEST ========
    @GetMapping("/test")
    public String testAdmin() {
        return "Le contrôleur admin fonctionne correctement!";
    }
}
