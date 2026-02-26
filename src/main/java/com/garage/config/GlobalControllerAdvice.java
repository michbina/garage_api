package com.garage.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.garage.model.PromotionBanner;
import com.garage.service.PromotionBannerService;

@ControllerAdvice
public class GlobalControllerAdvice {
	
	private final PromotionBannerService bannerService;

    public GlobalControllerAdvice(PromotionBannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**
     * Cette méthode injecte automatiquement la bannière dans le modèle de chaque requête.
     * Le résultat sera disponible dans toutes les vues Thymeleaf sous le nom "banner".
     */
    @ModelAttribute("banner")
    public PromotionBanner addBannerToModel() {
        // Cette ligne sera exécutée pour chaque page
        return bannerService.getBanner(); 
    }

}
