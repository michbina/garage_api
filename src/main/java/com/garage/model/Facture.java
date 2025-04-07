package com.garage.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "factures")
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String description;
    private BigDecimal montant;
    private LocalDate dateCreation;
    
    // Nouveau champ pour le nom du document
    private String documentNom;
    
    // Stockage du document (deux options)
    // Stocker le chemin du fichier
    private String documentPath;
    
    // Type MIME du document
    private String documentType;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}