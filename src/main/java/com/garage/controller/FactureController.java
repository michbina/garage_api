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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.garage.model.Devis;
import com.garage.model.Facture;
import com.garage.model.Garage;
import com.garage.model.Role;
import com.garage.model.User;
import com.garage.repository.GarageRepository;
import com.garage.repository.UserRepository;
import com.garage.service.DevisService;
import com.garage.service.FactureService;
import com.garage.service.GarageService;
import com.garage.service.UserService;
import com.garage.storage.DocumentStorage;

@Controller
public class FactureController {

	private static final Logger logger = LoggerFactory.getLogger(FactureController.class);

	private FactureService factureService;

	private UserService userService;

	private GarageService garageService;

	private UserRepository userRepository;

	private GarageRepository garageRepository;
	
	private DocumentStorage storage;
	
	public FactureController(FactureService factureService,UserService userService,GarageService garageService,UserRepository userRepository,GarageRepository garageRepository,DocumentStorage storage) {
		this.factureService=factureService;
		this.userService=userService;
		this.garageService=garageService;
		this.userRepository=userRepository;
		this.garageRepository=garageRepository;
		this.storage=storage;
	}

	@GetMapping("/factures")
	public String listeFactures(Model model, Authentication authentication) {
		User user = userService.findByUsername(authentication.getName());
		List<Facture> factures = factureService.findByUser(user);
		model.addAttribute("factures", factures);
		return "factures";
	}

	// ======== GESTION DES FACTURES ========

	@GetMapping("/admin/factures/create")
	public ModelAndView showCreateFactureForm(Model model, Authentication auth) {
		logger.info("Affichage du formulaire de création de facture");

		String username = auth.getName();
		User user = userRepository.findByUsername(username).get();

		List<User> clients = userService.findAllClients();
		if (clients == null) {
			clients = new ArrayList<>();
		}

		List<Garage> garages;
		if (user.getRole().contains(Role.ROLE_SUPERADMIN.name())) {
			// Admin global : accès à tous
			garages = new ArrayList<>(garageService.findAllGarages());
		} else if (user.getRole().contains(Role.ROLE_GARAGE_ADMIN.name())) {
			// Garage admin : accès à ses garages
			List<Long> garageIds = user.getGarageIds();
			garages = garageRepository.findAllById(garageIds);
		} else {
			// autres rôles : accès restreint, selon les règles
			garages = new ArrayList<>();
		}

		ModelAndView mav = new ModelAndView("admin/create-facture");
		mav.addObject("clients", clients);
		mav.addObject("facture", new Facture());
		mav.addObject("garages", garages);
		return mav;
	}

	@PostMapping("/admin/factures/create")
	public String createFacture(@ModelAttribute Facture facture, @RequestParam Long clientId,
			@RequestParam Long garageId, @RequestParam(required = false) MultipartFile document,
			RedirectAttributes redirectAttributes) {
		logger.info("Création d'une facture pour le client ID: {}", clientId);

		try {
			User client = userService.findById(clientId);
			facture.setUser(client);
			facture.setDateCreation(LocalDate.now());

			Garage garage = garageService.findById(garageId);

			facture.setGarage(garage);

			// Traitement du document si présent
			if (document != null && !document.isEmpty()) {
				String storageName = storage.save(document, "factures");
				facture.setStorageName(storageName);
				facture.setDocumentNom(document.getOriginalFilename());
				facture.setDocumentType(document.getContentType());
				String fileName = StringUtils.cleanPath(document.getOriginalFilename());
//				String uploadDir = "uploads/factures/";
//				Path uploadPath = Paths.get(uploadDir);
//
//				if (!Files.exists(uploadPath)) {
//					Files.createDirectories(uploadPath);
//				}
//
//				// Générer un nom de fichier unique
//				String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
//				Path filePath = uploadPath.resolve(uniqueFileName);
//
//				// Enregistrer le fichier
//				Files.copy(document.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//				// Enregistrer le chemin dans la facture
//				facture.setDocumentNom(fileName);
//				facture.setDocumentPath(filePath.toString());
//				facture.setDocumentType(document.getContentType());


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
	public ResponseEntity<Resource> downloadFactureDocument(@PathVariable Long id,Authentication authentication) {
		try {
			User currentUser = userService.findByUsername(authentication.getName());

	        Facture facture = factureService.findById(id)
	                .orElseThrow(() -> new RuntimeException("Facture introuvable"));
	        
	        // 🔐 Sécurité
	        boolean isOwner = facture.getUser().getId().equals(currentUser.getId());
	        boolean isAdmin = currentUser.getRole().contains("ADMIN");

	        if (!isOwner && !isAdmin)
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

	        Resource resource = storage.load("factures", facture.getStorageName());;

	        return ResponseEntity.ok()
	                .contentType(MediaType.parseMediaType(facture.getDocumentType()))
	                .header(HttpHeaders.CONTENT_DISPOSITION,
	                        "attachment; filename=\"" + facture.getDocumentNom() + "\"")
	                .body(resource);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}