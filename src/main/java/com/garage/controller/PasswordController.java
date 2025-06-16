package com.garage.controller;

import com.garage.model.User;
import com.garage.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordController {

    @Autowired
    private UserService userService;

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "change-password"; // vue thymeleaf
    }

    @PostMapping("/save-new-password")
    public String saveNewPassword(@RequestParam String password, Authentication authentication,RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        userService.updatePassword(user.getId(), password);
     // Ajouter un message flash
	    redirectAttributes.addFlashAttribute("passwordSuccessMessage", "Mot de passe modifié pour le user: "+user.getUsername());
        return "redirect:/login"; // ou autre page après changement
    }
}