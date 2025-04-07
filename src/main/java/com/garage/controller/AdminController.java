package com.garage.controller;

import com.garage.model.Devis;
import com.garage.model.Facture;
import com.garage.model.User;
import com.garage.service.DevisService;
import com.garage.service.FactureService;
import com.garage.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private FactureService factureService;
    
    @Autowired
    private DevisService devisService;
    
    // ======== DASHBOARD PRINCIPAL ========
    
    @GetMapping("/admin")
    public ModelAndView adminDashboard(Authentication authentication) {
        logger.info("========== ADMIN DASHBOARD ==========");
        logger.info("Utilisateur: {}", authentication.getName());
        logger.info("Autorités: {}", authentication.getAuthorities());
        
        List<User> clients = userService.findAllClients();
        if (clients == null) {
            clients = new ArrayList<>();
        }
        logger.info("Nombre de clients trouvés: {}", clients.size());
        
        ModelAndView mav = new ModelAndView("admin/dashboard");
        mav.addObject("clients", clients);
        mav.addObject("title", "Tableau de bord administrateur");
        
        return mav;
    }
    
    // ======== GESTION DES CLIENTS ========
    
    @GetMapping("/admin/clients")
    public ModelAndView listClients() {
        logger.info("Liste des clients");
        
        List<User> clients = userService.findAllClients();
        if (clients == null) {
            clients = new ArrayList<>();
        }
        
        ModelAndView mav = new ModelAndView("admin/clients");
        mav.addObject("clients", clients);
        return mav;
    }
    
    // ======== GESTION DES FACTURES ========
    
    @GetMapping("/admin/factures/create")
    public ModelAndView showCreateFactureForm() {
        logger.info("Affichage du formulaire de création de facture");
        
        List<User> clients = userService.findAllClients();
        if (clients == null) {
            clients = new ArrayList<>();
        }
        
        ModelAndView mav = new ModelAndView("admin/create-facture");
        mav.addObject("clients", clients);
        mav.addObject("facture", new Facture());
        return mav;
    }
    
    @PostMapping("/admin/factures/create")
    public String createFacture(@ModelAttribute Facture facture, 
                              @RequestParam Long clientId,
                              RedirectAttributes redirectAttributes) {
        logger.info("Création d'une facture pour le client ID: {}", clientId);
        
        try {
            User client = userService.findById(clientId);
            facture.setUser(client);
            facture.setDateCreation(LocalDate.now());
            
            Facture savedFacture = factureService.save(facture);
            logger.info("Facture créée avec succès, ID: {}", savedFacture.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Facture créée avec succès pour " + client.getUsername());
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la facture", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors de la création de la facture: " + e.getMessage());
        }
        
        return "redirect:/admin";
    }
    
    @GetMapping("/admin/client/{id}/factures")
    public ModelAndView viewClientFactures(@PathVariable Long id) {
        logger.info("Affichage des factures du client ID: {}", id);
        
        ModelAndView mav = new ModelAndView("admin/client-factures");
        
        try {
            User client = userService.findById(id);
            List<Facture> factures = factureService.findByUser(client);
            if (factures == null) {
                factures = new ArrayList<>();
            }
            
            mav.addObject("client", client);
            mav.addObject("factures", factures);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des factures du client", e);
            mav.addObject("errorMessage", "Impossible de récupérer les factures: " + e.getMessage());
        }
        
        return mav;
    }
    
    // ======== GESTION DES DEVIS ========
    
    @GetMapping("/admin/devis/create")
    public ModelAndView showCreateDevisForm() {
        logger.info("Affichage du formulaire de création de devis");
        
        List<User> clients = userService.findAllClients();
        if (clients == null) {
            clients = new ArrayList<>();
        }
        
        ModelAndView mav = new ModelAndView("admin/create-devis");
        mav.addObject("clients", clients);
        mav.addObject("devis", new Devis());
        return mav;
    }
    
    @PostMapping("/admin/devis/create")
    public String createDevis(@ModelAttribute Devis devis, 
                            @RequestParam Long clientId,
                            RedirectAttributes redirectAttributes) {
        logger.info("Création d'un devis pour le client ID: {}", clientId);
        
        try {
            User client = userService.findById(clientId);
            devis.setUser(client);
            devis.setDateCreation(LocalDate.now());
            devis.setStatut(Devis.StatutDevis.EN_ATTENTE);
            
            Devis savedDevis = devisService.save(devis);
            logger.info("Devis créé avec succès, ID: {}", savedDevis.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Devis créé avec succès pour " + client.getUsername());
        } catch (Exception e) {
            logger.error("Erreur lors de la création du devis", e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erreur lors de la création du devis: " + e.getMessage());
        }
        
        return "redirect:/admin";
    }
    
    @GetMapping("/admin/client/{id}/devis")
    public ModelAndView viewClientDevis(@PathVariable Long id) {
        logger.info("Affichage des devis du client ID: {}", id);
        
        ModelAndView mav = new ModelAndView("admin/client-devis");
        
        try {
            User client = userService.findById(id);
            List<Devis> devis = devisService.findByUser(client);
            if (devis == null) {
                devis = new ArrayList<>();
            }
            
            mav.addObject("client", client);
            mav.addObject("devis", devis);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des devis du client", e);
            mav.addObject("errorMessage", "Impossible de récupérer les devis: " + e.getMessage());
        }
        
        return mav;
    }
    
    // ======== MÉTHODES UTILITAIRES ========
    
    @GetMapping("/admin/test")
    @ResponseBody
    public String testAdmin() {
        return "Le contrôleur admin fonctionne correctement!";
    }
}