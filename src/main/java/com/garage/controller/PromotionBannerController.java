package com.garage.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.garage.dto.banner.BannerDto;
import com.garage.model.PromotionBanner;
import com.garage.service.PromotionBannerService;
import com.garage.storage.DocumentCategory;
import com.garage.storage.LocalDocumentStorage;

import jakarta.validation.Valid;

@Controller
public class PromotionBannerController {

	private static final Logger logger = LoggerFactory.getLogger(PromotionBannerController.class);

	private PromotionBannerService bannerService;

	private final LocalDocumentStorage localDocumentStorage;

	private PromotionBannerController(PromotionBannerService bannerService, LocalDocumentStorage localDocumentStorage) {
		this.localDocumentStorage = localDocumentStorage;
		this.bannerService = bannerService;
	}

	@GetMapping("/admin/banner/create")
	public String showCreateBannerForm(Model model) {
		logger.info("Affichage du formulaire de création de bannière");
		PromotionBanner bannerEntity = bannerService.getBanner();
		BannerDto bannerForm = new BannerDto();

		if (bannerEntity != null) {
			bannerForm.setMessage(bannerEntity.getMessage());
			bannerForm.setEnabled(bannerEntity.isEnabled());
			bannerForm.setExistingImageUrl(bannerEntity.getImageUrl());
		}

		// CHANGEMENT 2: Utiliser un nom cohérent "bannerForm"
		model.addAttribute("bannerForm", bannerForm);

		return "admin/create-banner";
	}

	@PostMapping("/admin/banner/upload")
	@PreAuthorize("hasRole('ADMIN')")
	public String createOrUpdateBanner(@Valid @ModelAttribute("bannerForm") BannerDto bannerForm,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) throws IOException {
		try {
			// Validation de l'image (cas particulier)
			PromotionBanner existingBanner = bannerService.getBanner();
			if (existingBanner == null && (bannerForm.getImageFile() == null || bannerForm.getImageFile().isEmpty())) {
				// Si la bannière est nouvelle, l'image est obligatoire.
				bindingResult.rejectValue("imageFile", "imageFile.notempty",
						"Une image est obligatoire pour une nouvelle bannière.");
			}
			if (bindingResult.hasErrors()) {
				// Si des erreurs de validation sont trouvées, on retourne au formulaire
				logger.warn("Erreurs de validation du formulaire.");
				return "admin/create-banner";
			}
			PromotionBanner bannerToSave = (existingBanner != null) ? existingBanner : new PromotionBanner();

			if (bannerForm.getImageFile() != null && !bannerForm.getImageFile().isEmpty()) {
				logger.info("Nouvelle image reçue, sauvegarde via LocalDocumentStorage...");

				// Utilisation duservice de stockage avec la bonne catégorie
				String storedFilename = localDocumentStorage.uploadFile(bannerForm.getImageFile(),
						DocumentCategory.BANNER);

				// Construction de l'URL web complète
				// Votre méthode retourne "uuid.jpg", on construit "/uploads/banners/uuid.jpg"
				String imageUrl = "/" + "uploads" + "/" + DocumentCategory.BANNER.getFolder() + "/" + storedFilename;

				bannerToSave.setImageUrl(imageUrl);
				logger.info("Fichier sauvegardé en tant que '{}', URL d'accès : '{}'", storedFilename, imageUrl);
			}

			bannerToSave.setMessage(bannerForm.getMessage());
			bannerToSave.setEnabled(bannerForm.isEnabled());

			bannerService.save(bannerToSave);
			redirectAttributes.addFlashAttribute("successMessage", "Bannière ajouté avec succès");
		} catch (Exception e) {
			logger.error("Erreur lors de la sauvegarde de la bannière", e);
			bindingResult.reject("storage.error", "Erreur lors de la sauvegarde de l'image. Veuillez réessayer.");
			return "admin/create-banner";
		}
		return "redirect:/admin/banner/create";
	}

}