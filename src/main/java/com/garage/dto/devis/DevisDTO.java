package com.garage.dto.devis;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class DevisDTO {
	
	private Long id;
    private String description;
    private BigDecimal montant;
    private LocalDate dateCreation;
    private String statut;

    private String clientUsername;
    private Long clientId;

    private String garageName;
    private Long garageId;

    private String documentNom;	 	

}
