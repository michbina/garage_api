package com.garage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.garage.model.PromotionBanner;

public interface PromotionBannerRepository extends JpaRepository<PromotionBanner, Long> {

	PromotionBanner findFirstByOrderByIdAsc();
    
}