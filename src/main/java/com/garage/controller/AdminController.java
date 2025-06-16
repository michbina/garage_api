package com.garage.controller;

import java.util.ArrayList;
import java.util.List;

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
		
		String username = authentication.getName();
		User user = userRepository.findByUsername(username).get();

		List<User> clients;
		
		
		if (user.getRole().contains(Role.ROLE_SUPERADMIN.name())) {
			// Admin global : accès à tous
			clients = userService.findAllClients();
			logger.info("Nombre de clients trouvés: {}", clients.size());
		} else if (user.getRole().contains(Role.ROLE_GARAGE_ADMIN.name())) {
			// Garage admin : accès à ses garages
			List<Long> garageIds = user.getGarageIds();
			List<Garage> garages = garageRepository.findAllById(garageIds);
	        clients = userService.findByGaragesIn(garages);
	        logger.info("Nombre de clients trouvés: {}", clients.size());
		} else {
			// autres rôles : accès restreint, selon les règles
			clients = new ArrayList<>();
		}

		ModelAndView mav = new ModelAndView("admin/dashboard");
		mav.addObject("clients", clients);
		mav.addObject("title", "Tableau de bord administrateur");

		return mav;
	}

	// ======== GESTION DES CLIENTS ========

	@GetMapping("/admin/clients")
	public ModelAndView listClients() {
		logger.info("Liste des clients");

		List<User> clients = userService.findAllClients();
		if (clients == null) {
			clients = new ArrayList<>();
		}

		ModelAndView mav = new ModelAndView("admin/clients");
		mav.addObject("clients", clients);

		return mav;
	}

	// seulement accessible par ROLE_ADMIN
	@GetMapping("/admin/user/create")
	@PreAuthorize("hasRole('ROLE_SUPERADMIN','ROLE_GARAGE_ADMIN')")
	public ModelAndView showCreateUser() {
		logger.info("Affichage du formulaire de création d'utilisateur");

		List<Garage> garages = new ArrayList<>(garageService.findAllGarages());

		ModelAndView mav = new ModelAndView("admin/create-user");
		mav.addObject("user", new User());
		mav.addObject("roles", Role.values());
		mav.addObject("garages", garages);

		return mav;

	}

	// seulement accessible par ROLE_ADMIN
	@PostMapping("/admin/user/create")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN','ROLE_GARAGE_ADMIN')")
	public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
		// créer en fonction du role.
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			logger.error("Utilisateur " + user.getUsername() + " déjà existant");
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

		// return ResponseEntity.ok("Utilisateur créé avec succès");

		// Ajouter un message flash
		redirectAttributes.addFlashAttribute("successMessage",
				"Utilisateur: " + user.getUsername() + " créé avec succès avec le rôle: " + user.getRole());

		// Redirection vers le dashboard
		return "redirect:/admin";

	}

	// ======== MÉTHODES UTILITAIRES ========

	@GetMapping("/admin/test")
	@ResponseBody
	public String testAdmin() {
		return "Le contrôleur admin fonctionne correctement!";
	}
}