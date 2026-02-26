package com.garage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.garage.storage.LocalDocumentStorage;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	private final LocalDocumentStorage storageService;

	public MvcConfig(LocalDocumentStorage storageService) {
		this.storageService = storageService;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// La variable 'rootPathUri' contiendra "file:/C:/garage/storage/"
		String rootPathUri = storageService.getRootLocation().toUri().toString();

		// 3. On configure le mapping dynamiquement avec le chemin fourni.
		registry.addResourceHandler("/uploads/**").addResourceLocations(rootPathUri);
	}

}
