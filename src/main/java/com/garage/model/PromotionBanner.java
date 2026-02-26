package com.garage.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "promotion_banners")
public class PromotionBanner {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    private String message;
    
    private String imageUrl;
    
    private boolean enabled;
 
    
}