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
import com.garage.model.Garage;
import com.garage.model.Role;
import com.garage.model.User;
import com.garage.repository.GarageRepository;
import com.garage.repository.UserRepository;
import com.garage.service.DevisService;
import com.garage.service.GarageService;
import com.garage.service.UserService;
import com.garage.storage.DocumentStorage;

@Controller
public class DevisController {

	private static final Logger logger = LoggerFactory.getLogger(DevisController.class);

	private DevisService devisService;

	private UserService userService;

	private GarageService garageService;

	private UserRepository userRepository;

	private GarageRepository garageRepository;
	
	private DocumentStorage storage;
	
	public DevisController(DevisService devisService,UserService userService,GarageService garageService,UserRepository userRepository,GarageRepository garageRepository,DocumentStorage storage) {
		this.devisService=devisService;
		this.userService=userService;
		this.garageService=garageService;
		this.userRepository=userRepository;
		this.garageRepository=garageRepository;
		this.storage=storage;
	}

	@GetMapping("/devis")
	public String listeDevis(Model model, Authentication authentication) {
		User user = userService.findByUsername(authentication.getName());
		List<Devis> devis = devisService.findByUser(user);
		model.addAttribute("devis", devis);
		return "devis";
	}

	// ======== GESTION DES DEVIS ========

	@GetMapping("/admin/devis/create")
	public ModelAndView showCreateDevisForm(Authentication auth) {
		logger.info("Affichage du formulaire de création de devis");

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

		ModelAndView mav = new ModelAndView("admin/create-devis");
		mav.addObject("clients", clients);
		mav.addObject("devis", new Devis());
		mav.addObject("garages", garages);
		return mav;
	}

	@PostMapping("/admin/devis/create")
	public String createDevis(@ModelAttribute Devis devis, @RequestParam Long clientId, @RequestParam Long garageId,
			@RequestParam(required = false) MultipartFile document, RedirectAttributes redirectAttributes) {
		logger.info("Création d'un devis pour le client ID: {}", clientId);

		try {
			User client = userService.findById(clientId);
			devis.setDateCreation(LocalDate.now());
			devis.setStatut(Devis.StatutDevis.EN_ATTENTE);

			Garage garage = garageService.findById(garageId);
			devis.setGarage(garage);
			
			devis.setUser(client);

			// Traitement du document si présent
			if (document != null && !document.isEmpty()) {
				
				 String storageName = storage.save(document, "devis");
				 devis.setStorageName(storageName);
				 devis.setDocumentNom(document.getOriginalFilename());
				 devis.setDocumentType(document.getContentType());
				
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
//				devis.setDocumentNom(fileName);
//				devis.setDocumentPath(filePath.toString());
//				devis.setDocumentType(document.getContentType());

				logger.info("Document ajouté au devis: {}", fileName);
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
	public ResponseEntity<Resource> downloadDevisDocument(
	        @PathVariable Long id,
	        Authentication authentication) {

	    try {

	        User currentUser = userService.findByUsername(authentication.getName());

	        Devis devis = devisService.findById(id)
	                .orElseThrow(() -> new RuntimeException("Devis introuvable"));

	        // 🔐 Sécurité
	        boolean isOwner = devis.getUser().getId().equals(currentUser.getId());
	        boolean isAdmin = currentUser.getRole().contains("ADMIN");

	        if (!isOwner && !isAdmin)
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

	        Resource resource = storage.load("devis", devis.getStorageName());;

	        return ResponseEntity.ok()
	                .contentType(MediaType.parseMediaType(devis.getDocumentType()))
	                .header(HttpHeaders.CONTENT_DISPOSITION,
	                        "attachment; filename=\"" + devis.getDocumentNom() + "\"")
	                .body(resource);

	    } catch (Exception e) {
	        return ResponseEntity.internalServerError().build();
	    }
	}

	
	@PostMapping("/devis/{id}/valider")
    public String validerDevis(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            devisService.validerDevis(id);
            redirectAttributes.addFlashAttribute("success", "Le devis a été validé !");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/devis";
    }

}