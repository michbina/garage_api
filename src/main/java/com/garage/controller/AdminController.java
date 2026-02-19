package com.garage.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.garage.model.Garage;
import com.garage.model.Role;
import com.garage.model.User;
import com.garage.repository.GarageRepository;
import com.garage.repository.UserRepository;
import com.garage.service.GarageService;
import com.garage.service.UserService;

@Controller
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

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
    @GetMapping("/admin")
    public ModelAndView adminDashboard(Authentication authentication) {
        logger.info("========== ADMIN DASHBOARD ==========");
        logger.info("Utilisateur: {}", authentication.getName());
        logger.info("Autorités: {}", authentication.getAuthorities());

        Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            logger.error("Utilisateur introuvable !");
            return new ModelAndView("error/404"); // page d'erreur si besoin
        }
        User user = userOpt.get();

        List<User> clients = getClientsForUser(user);

        ModelAndView mav = new ModelAndView("admin/dashboard");
        mav.addObject("clients", clients);
        mav.addObject("title", "Tableau de bord administrateur");

        return mav;
    }

    // ======== LISTE DES CLIENTS ========
    @GetMapping("/admin/clients")
    public ModelAndView listClients() {
        logger.info("Liste des clients");
        List<User> clients = userService.findAllClients();
        if (clients == null) clients = new ArrayList<>();

        ModelAndView mav = new ModelAndView("admin/clients");
        mav.addObject("clients", clients);

        return mav;
    }

    // ======== FORMULAIRE DE CRÉATION UTILISATEUR ========
    @GetMapping("/admin/user/create")
    @PreAuthorize("hasAnyRole('ADMIN','GARAGE_ADMIN')")
    public ModelAndView showCreateUser(Authentication authentication) {
        logger.info("Affichage du formulaire de création d'utilisateur");
        Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return new ModelAndView("error/404");
        }
        User currentUser = userOpt.get();

        List<Garage> garages = new ArrayList<>();
        Role[] roles = new Role[] {};
        
        if (currentUser.getRole().contains(Role.ROLE_ADMIN.name())) {
            garages = new ArrayList<>(garageService.findAllGarages());
            roles = Role.values();
        } else if (currentUser.getRole().contains(Role.ROLE_GARAGE_ADMIN.name())) {
            garages = currentUser.getGarages() != null ? currentUser.getGarages() : new ArrayList<>();
            roles = new Role[] { Role.ROLE_USER };
        }
        
//        List<Garage> garages = new ArrayList<>(garageService.findAllGarages());

        ModelAndView mav = new ModelAndView("admin/create-user");
        mav.addObject("user", new User());
        mav.addObject("roles", roles);
//        mav.addObject("roles", Role.values());
        mav.addObject("garages", garages);

        return mav;
    }

    // ======== CRÉATION UTILISATEUR ========
    @PostMapping("/admin/user/create")
    @PreAuthorize("hasAnyRole('ADMIN','GARAGE_ADMIN')")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes,Authentication authentication) {
    	 Optional<User> currentUserOpt = userRepository.findByUsername(authentication.getName());
    	    if (currentUserOpt.isEmpty()) {
    	        redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur courant introuvable.");
    	        return "redirect:/admin/user/create";
    	    }
    	    User currentUser = currentUserOpt.get();
    	    
    	 // Restriction pour GARAGE_ADMIN
    	    if (currentUser.getRole().contains(Role.ROLE_GARAGE_ADMIN.name())) {
    	        // Forcer le rôle à ROLE_USER
    	        user.setRole(Role.ROLE_USER.name());
    	        // Restreindre les garages à ceux du garage_admin
    	        List<Long> allowedGarageIds = currentUser.getGarages().stream().map(Garage::getId).toList();
    	        List<Long> requestedGarageIds = user.getGarageIds();
    	        if (requestedGarageIds != null) {
    	            requestedGarageIds.retainAll(allowedGarageIds);
    	            user.setGarageIds(requestedGarageIds);
    	        } else {
    	            user.setGarageIds(new ArrayList<>());
    	        }
    	    }

    	    // Vérification de l'existence de l'utilisateur
    	    Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
    	    if (existingUser.isPresent()) {
    	        redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur " + user.getUsername() + " existe déjà !");
    	        return "redirect:/admin/user/create";
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

    	    userService.saveUserWithGarages(user);
    	    redirectAttributes.addFlashAttribute("successMessage",
    	            "Utilisateur " + user.getUsername() + " créé avec succès avec le rôle: " + user.getRole());

    	    return "redirect:/admin";
    	    
    	    // Vérification de l'existence de l'utilisateur
    	
//        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
//        if (existingUser.isPresent()) {
//            logger.error("Utilisateur {} déjà existant", user.getUsername());
//            redirectAttributes.addFlashAttribute("errorMessage",
//                    "Utilisateur " + user.getUsername() + " existe déjà !");
//            return "redirect:/admin/user/create";
//        }
//
//        // Encodage du mot de passe
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setFactures(new ArrayList<>());
//        user.setDevis(new ArrayList<>());
//        user.setFirstLogin(true);
//
//        // Affectation des garages si fournis
//        List<Long> garageIds = user.getGarageIds();
//        if (garageIds != null && !garageIds.isEmpty()) {
//            List<Garage> garages = garageRepository.findAllById(garageIds);
//            user.setGarages(garages);
//        }
//
//        userService.saveUserWithGarages(user);
////        userRepository.save(user);
//        logger.info("Utilisateur {} créé avec succès", user.getUsername());
//
//        redirectAttributes.addFlashAttribute("successMessage",
//                "Utilisateur " + user.getUsername() + " créé avec succès avec le rôle: " + user.getRole());
//
//        return "redirect:/admin";
    }

    // ======== MÉTHODE DE TEST ========
    @GetMapping("/admin/test")
    @ResponseBody
    public String testAdmin() {
        return "Le contrôleur admin fonctionne correctement!";
    }

    // ======== MÉTHODE UTILITAIRE PRIVÉE ========
    private List<User> getClientsForUser(User user) {
        if (user.getRole().contains(Role.ROLE_ADMIN.name())) {
            logger.info("Accès Admin : récupération de tous les clients");
            return userService.findAllClients();
        } else if (user.getRole().contains(Role.ROLE_GARAGE_ADMIN.name())) {
            logger.info("Accès GarageAdmin : récupération des clients de ses garages");
            List<Garage> garages = user.getGarages();
            if (garages == null) garages = new ArrayList<>();
            // Filtre les utilisateurs pour ne garder que ceux avec le rôle ROLE_USER
            return userService.findByGaragesIn(garages).stream()
                .filter(u -> u.getRole() != null && u.getRole().contains(Role.ROLE_USER.name()))
                .toList();
        } else {
            logger.info("Autres rôles : aucun client accessible");
            return new ArrayList<>();
        }
    }
}
