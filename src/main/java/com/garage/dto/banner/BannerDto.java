package com.garage.dto.banner;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class BannerDto {

	@NotEmpty(message = "Le message ne peut pas être vide.")
	private String message;

	private boolean enabled;

	// Pour le téléchargement d'une nouvelle image.
	// Ce champ est optionnel si une image existe déjà.
	private MultipartFile imageFile;

	// Pour afficher l'URL de l'image qui existe déjà dans le formulaire.
	private String existingImageUrl;

}
