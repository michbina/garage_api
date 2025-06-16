package com.garage.model;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

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
    
    // Nouveau champ pour le nom du document
    private String documentNom;
    
    // Stockage du document (deux options)
    // Stocker le chemin du fichier
    private String documentPath;
    
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
}