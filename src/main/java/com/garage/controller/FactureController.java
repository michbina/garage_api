package com.garage.controller;

import com.garage.model.Facture;
import com.garage.model.User;
import com.garage.service.FactureService;
import com.garage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class FactureController {

    @Autowired
    private FactureService factureService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/factures")
    public String listeFactures(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        List<Facture> factures = factureService.findByUser(user);
        model.addAttribute("factures", factures);
        return "factures";
    }
}