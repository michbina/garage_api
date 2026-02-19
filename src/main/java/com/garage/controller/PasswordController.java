package com.garage.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.garage.dto.register.PasswordDto;
import com.garage.model.User;
import com.garage.service.UserService;

import jakarta.validation.Valid;

@Controller
public class PasswordController {

	private UserService userService;

	public PasswordController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/change-password")
	public String showChangePasswordForm(Model model, Authentication authentication) {
		model.addAttribute("passwordDto", new PasswordDto());

		boolean firstLogin = false;
		if (authentication != null) {
			User user = userService.findByUsername(authentication.getName());
			firstLogin = user.getFirstLogin(); // true si première connexion
		}

		model.addAttribute("firstLogin", firstLogin);
		return "change-password";
	}

	@PostMapping("/save-new-password")
	public String saveNewPassword(@Valid @ModelAttribute("passwordDto") PasswordDto passwordDto,
			BindingResult bindingResult, Authentication authentication, Model model, // <--- ajouter
			RedirectAttributes redirectAttributes) {

		User user = userService.findByUsername(authentication.getName());
		boolean firstLogin = user.getFirstLogin(); // passer au template
		model.addAttribute("firstLogin", firstLogin);

		// Validation
		if (bindingResult.hasErrors()) {
			return "change-password"; // Thymeleaf a now firstLogin
		}

		if (!passwordDto.getPassword().equals(passwordDto.getConfirmPassword())) {
			bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
					"Les mots de passe ne correspondent pas");
			return "change-password"; // firstLogin est passé
		}

		// Mise à jour du mot de passe
		userService.updatePassword(user.getId(), passwordDto.getPassword());
		redirectAttributes.addFlashAttribute("passwordSuccessMessage", "Mot de passe modifié avec succès");

		return "redirect:/login";
	}

}