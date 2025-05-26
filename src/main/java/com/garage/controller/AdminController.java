package com.garage.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.garage.dto.CreateUserRequest;
import com.garage.model.Devis;
import com.garage.model.Facture;
import com.garage.model.User;
import com.garage.repository.UserRepository;
import com.garage.service.DevisService;
import com.garage.service.FactureService;
import com.garage.service.UserService;

@Controller
public class AdminController {

	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private FactureService factureService;

	@Autowired
	private DevisService devisService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	// ======== DASHBOARD PRINCIPAL ========

	@GetMapping("/admin")
	public ModelAndView adminDashboard(Authentication authentication) {
		logger.info("========== ADMIN DASHBOARD ==========");
		logger.info("Utilisateur: {}", authentication.getName());
		logger.info("Autorités: {}", authentication.getAuthorities());

		List<User> clients = userService.findAllClients();
		if (clients == null) {
			clients = new ArrayList<>();
		}
		logger.info("Nombre de clients trouvés: {}", clients.size());

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

	// ======== GESTION DES FACTURES ========

	@GetMapping("/admin/factures/create")
	public ModelAndView showCreateFactureForm() {
		logger.info("Affichage du formulaire de création de facture");

		List<User> clients = userService.findAllClients();
		if (clients == null) {
			clients = new ArrayList<>();
		}

		ModelAndView mav = new ModelAndView("admin/create-facture");
		mav.addObject("clients", clients);
		mav.addObject("facture", new Facture());
		return mav;
	}

	@PostMapping("/admin/factures/create")
	public String createFacture(@ModelAttribute Facture facture, @RequestParam Long clientId,
			@RequestParam(required = false) MultipartFile document, RedirectAttributes redirectAttributes) {
		logger.info("Création d'une facture pour le client ID: {}", clientId);

		try {
			User client = userService.findById(clientId);
			facture.setUser(client);
			facture.setDateCreation(LocalDate.now());

			// Traitement du document si présent
			if (document != null && !document.isEmpty()) {
				// Option 1: Enregistrer le fichier sur le serveur
				String fileName = StringUtils.cleanPath(document.getOriginalFilename());
				String uploadDir = "uploads/factures/";
				Path uploadPath = Paths.get(uploadDir);

				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				// Générer un nom de fichier unique
				String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
				Path filePath = uploadPath.resolve(uniqueFileName);

				// Enregistrer le fichier
				Files.copy(document.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

				// Enregistrer le chemin dans la facture
				facture.setDocumentNom(fileName);
				facture.setDocumentPath(filePath.toString());
				facture.setDocumentType(document.getContentType());

				// Option 2: Enregistrer le fichier directement en base de données
				// facture.setDocumentData(document.getBytes());

				logger.info("Document ajouté à la facture: {}", fileName);
			}

			Facture savedFacture = factureService.save(facture);
			logger.info("Facture créée avec succès, ID: {}", savedFacture.getId());

			redirectAttributes.addFlashAttribute("successMessage",
					"Facture créée avec succès pour " + client.getUsername());
		} catch (Exception e) {
			logger.error("Erreur lors de la création de la facture", e);
			redirectAttributes.addFlashAttribute("errorMessage",
					"Erreur lors de la création de la facture: " + e.getMessage());
		}

		return "redirect:/admin";
	}

	@GetMapping("/admin/client/{id}/factures")
	public ModelAndView viewClientFactures(@PathVariable Long id) {
		logger.info("Affichage des factures du client ID: {}", id);

		ModelAndView mav = new ModelAndView("admin/client-factures");

		try {
			User client = userService.findById(id);
			List<Facture> factures = factureService.findByUser(client);
			if (factures == null) {
				factures = new ArrayList<>();
			}

			mav.addObject("client", client);
			mav.addObject("factures", factures);
		} catch (Exception e) {
			logger.error("Erreur lors de la récupération des factures du client", e);
			mav.addObject("errorMessage", "Impossible de récupérer les factures: " + e.getMessage());
		}

		return mav;
	}

	@GetMapping("/factures/{id}/document")
	public ResponseEntity<Resource> downloadFactureDocument(@PathVariable Long id) {
		try {
			Optional<Facture> factureO = factureService.findById(id);

			if (factureO.get() != null) {

				Facture facture = factureO.get();

				if (facture.getDocumentPath() != null) {
					// Option 1: Récupérer depuis le système de fichiers
					Path path = Paths.get(facture.getDocumentPath());
					Resource resource = new UrlResource(path.toUri());

					if (resource.exists() || resource.isReadable()) {
						return ResponseEntity.ok().contentType(MediaType.parseMediaType(facture.getDocumentType()))
								.header(HttpHeaders.CONTENT_DISPOSITION,
										"attachment; filename=\"" + facture.getDocumentNom() + "\"")
								.body(resource);
					}
				}

			}

			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}

	// ======== GESTION DES DEVIS ========

	@GetMapping("/admin/devis/create")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ModelAndView showCreateDevisForm() {
		logger.info("Affichage du formulaire de création de devis");

		List<User> clients = userService.findAllClients();
		if (clients == null) {
			clients = new ArrayList<>();
		}

		ModelAndView mav = new ModelAndView("admin/create-devis");
		mav.addObject("clients", clients);
		mav.addObject("devis", new Devis());
		return mav;
	}

	@PostMapping("/admin/devis/create")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String createDevis(@ModelAttribute Devis devis, @RequestParam Long clientId,
			@RequestParam(required = false) MultipartFile document, RedirectAttributes redirectAttributes) {
		logger.info("Création d'un devis pour le client ID: {}", clientId);

		try {
			User client = userService.findById(clientId);
			devis.setUser(client);
			devis.setDateCreation(LocalDate.now());
			devis.setStatut(Devis.StatutDevis.EN_ATTENTE);

			// Traitement du document si présent
			if (document != null && !document.isEmpty()) {
				// Option 1: Enregistrer le fichier sur le serveur
				String fileName = StringUtils.cleanPath(document.getOriginalFilename());
				String uploadDir = "uploads/factures/";
				Path uploadPath = Paths.get(uploadDir);

				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				// Générer un nom de fichier unique
				String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
				Path filePath = uploadPath.resolve(uniqueFileName);

				// Enregistrer le fichier
				Files.copy(document.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

				// Enregistrer le chemin dans la facture
				devis.setDocumentNom(fileName);
				devis.setDocumentPath(filePath.toString());
				devis.setDocumentType(document.getContentType());

				logger.info("Document ajouté à la facture: {}", fileName);
			}

			Devis savedDevis = devisService.save(devis);
			logger.info("Devis créé avec succès, ID: {}", savedDevis.getId());

			redirectAttributes.addFlashAttribute("successMessage",
					"Devis créé avec succès pour " + client.getUsername());
		} catch (Exception e) {
			logger.error("Erreur lors de la création du devis", e);
			redirectAttributes.addFlashAttribute("errorMessage",
					"Erreur lors de la création du devis: " + e.getMessage());
		}

		return "redirect:/admin";
	}

	@GetMapping("/admin/client/{id}/devis")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ModelAndView viewClientDevis(@PathVariable Long id) {
		logger.info("Affichage des devis du client ID: {}", id);

		ModelAndView mav = new ModelAndView("admin/client-devis");

		try {
			User client = userService.findById(id);
			List<Devis> devis = devisService.findByUser(client);
			if (devis == null) {
				devis = new ArrayList<>();
			}

			mav.addObject("client", client);
			mav.addObject("devis", devis);
		} catch (Exception e) {
			logger.error("Erreur lors de la récupération des devis du client", e);
			mav.addObject("errorMessage", "Impossible de récupérer les devis: " + e.getMessage());
		}

		return mav;
	}

	@GetMapping("/devis/{id}/document")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<Resource> downloadDevisDocument(@PathVariable Long id) {
		try {
			Optional<Devis> devisO = devisService.findById(id);

			if (devisO.get() != null) {

				Devis devis = devisO.get();

				if (devis.getDocumentPath() != null) {
					// Option 1: Récupérer depuis le système de fichiers
					Path path = Paths.get(devis.getDocumentPath());
					Resource resource = new UrlResource(path.toUri());

					if (resource.exists() || resource.isReadable()) {
						return ResponseEntity.ok().contentType(MediaType.parseMediaType(devis.getDocumentType()))
								.header(HttpHeaders.CONTENT_DISPOSITION,
										"attachment; filename=\"" + devis.getDocumentNom() + "\"")
								.body(resource);
					}
				}

			}

			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	// seulement accessible par ROLE_ADMIN
		@GetMapping("/admin/user/create")
		@PreAuthorize("hasRole('ROLE_ADMIN')")
		public ModelAndView showCreateUser() {
			logger.info("Affichage du formulaire de création d'utilisateur");
			
			ModelAndView mav = new ModelAndView("admin/create-user");
			mav.addObject("user", new User());

			return mav;

		}
	
	
	// seulement accessible par ROLE_ADMIN
		@PostMapping("/admin/user/create")
		@PreAuthorize("hasRole('ROLE_ADMIN')")
		public ResponseEntity<String> createUser(@ModelAttribute User user) {
			//TODO: mettre le role sur la page html
			//créer en fonction du role.
			if (userRepository.findByUsername(user.getUsername()).isPresent()) {
				return ResponseEntity.badRequest().body("Utilisateur déjà existant");
			}

			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setRole("ROLE_ADMIN");
			user.setFactures(new ArrayList<>());
			user.setDevis(new ArrayList<>());

			userRepository.save(user);
			logger.info("Utilisateur créé avec succès");

			return ResponseEntity.ok("Utilisateur créé avec succès");

		}

	

	// ======== MÉTHODES UTILITAIRES ========

	@GetMapping("/admin/test")
	@ResponseBody
	public String testAdmin() {
		return "Le contrôleur admin fonctionne correctement!";
	}
}