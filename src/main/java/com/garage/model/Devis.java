package com.garage.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "devis")
public class Devis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String description;
    private BigDecimal montant;
    private LocalDate dateCreation;
    
    @Column(nullable = true, length = 10000)
    private String signature;
    
    @Enumerated(EnumType.STRING)
    private StatutDevis statut;
    
    // Nom visible utilisateur (Facture_Janvier.pdf)
    private String documentNom;
    
    private String storageName;   // Nom réel disque (UUID.pdf)
    
    
    // Type MIME du document
    private String documentType;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // l'utilisateur auquel la facture ou devis est rattaché
    
    @ManyToOne
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;
    
    public enum StatutDevis {
        EN_ATTENTE, VALIDE, ANNULE
    }
    
    @Column(unique = true, nullable = false)
    private String publicId; // UUID public
    
    @PrePersist
    public void generatePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID().toString();
        }
    }
 
    
}