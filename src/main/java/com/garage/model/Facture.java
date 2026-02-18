package com.garage.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
    
    private String storageName; // nom réel disque
    
    
    // Type MIME du document
    private String documentType;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // l'utilisateur auquel la facture ou devis est rattaché
    
    
    @ManyToOne
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;
    
    @Column(unique = true, nullable = false)
    private String publicId; // UUID public
    
    @PrePersist
    public void generatePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
    }


    
}