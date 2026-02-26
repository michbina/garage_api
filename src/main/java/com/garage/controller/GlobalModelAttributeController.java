package com.garage.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.garage.model.PromotionBanner;
import com.garage.service.PromotionBannerService;

@ControllerAdvice
public class GlobalModelAttributeController {
	
	private PromotionBannerService bannerService;
	
	private GlobalModelAttributeController(PromotionBannerService bannerService) {
		this.bannerService = bannerService;
	}
	
	@ModelAttribute("banner")
    public PromotionBanner addBannerToModel() {
        return bannerService.getBanner();
    }

}
