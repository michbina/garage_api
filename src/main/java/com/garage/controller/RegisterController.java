package com.garage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.garage.dto.register.RegisterRequest;
import com.garage.model.User;
import com.garage.service.UserService;

import jakarta.validation.Valid;

@Controller
public class RegisterController {

	private UserService userService;

	public RegisterController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		if (!model.containsAttribute("registerRequest")) {
			model.addAttribute("registerRequest", new RegisterRequest());
		}
		return "register";
	}

	@PostMapping("/api/register")
	public String registerUser(@ModelAttribute @Valid RegisterRequest registerRequest, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		// Vérifier les erreurs de validation
		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registerRequest",
					bindingResult);
			redirectAttributes.addFlashAttribute("registerRequest", registerRequest);
			return "redirect:/register";
		}

		// Vérifier si les mots de passe correspondent
		if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Les mots de passe ne correspondent pas");
			redirectAttributes.addFlashAttribute("registerRequest", registerRequest);
			return "redirect:/register";
		}

		try {
			// Tenter d'enregistrer l'utilisateur
			User user = userService.registerNewUser(registerRequest);
			redirectAttributes.addFlashAttribute("successMessage",
					"Compte créé avec succès! Vous pouvez maintenant vous connecter.");
			return "redirect:/login";
		} catch (IllegalArgumentException e) {
			// Gérer les erreurs d'enregistrement
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("registerRequest", registerRequest);
			return "redirect:/register";
		}
	}
}