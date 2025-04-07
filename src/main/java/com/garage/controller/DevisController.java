package com.garage.controller;

import com.garage.model.Devis;
import com.garage.model.User;
import com.garage.service.DevisService;
import com.garage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DevisController {

    @Autowired
    private DevisService devisService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/devis")
    public String listeDevis(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        List<Devis> devis = devisService.findByUser(user);
        model.addAttribute("devis", devis);
        return "devis";
    }
    
    @PostMapping("/devis/{id}/valider")
    public String validerDevis(@PathVariable Long id, @RequestParam String signature) {
        devisService.validerDevis(id, signature);
        return "redirect:/devis";
    }
}