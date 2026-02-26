package com.garage.service;

import org.springframework.stereotype.Service;

import com.garage.model.PromotionBanner;
import com.garage.repository.PromotionBannerRepository;

@Service
public class PromotionBannerService {
	
	private PromotionBannerRepository promotionBannerRepository;
	
	
	public PromotionBannerService(PromotionBannerRepository promotionBannerRepository) {
		this.promotionBannerRepository = promotionBannerRepository;
	}
	
	
	public PromotionBanner save(PromotionBanner promotionBanner) {
        return promotionBannerRepository.save(promotionBanner);
    }
	
	public PromotionBanner getBanner() {
	    // Récupère le bandeau promotionnel (voir si on en a besoin de plusieurs à l’avenir)
	    return promotionBannerRepository.findFirstByOrderByIdAsc();
	}

}