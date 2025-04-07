package com.garage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TemplateCheckController {
	
	private static final Logger logger = LoggerFactory.getLogger(TemplateCheckController.class);
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    @GetMapping("/check-template")
    @ResponseBody
    public String checkTemplate(@RequestParam String template) {
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/" + template + ".html");
            if (resource.exists()) {
            	 logger.info( "Le template '" + template + ".html' existe!");
                return "Le template '" + template + ".html' existe!";
            } else {
            	logger.info( "Le template '" + template + ".html' existe!");
                return "Le template '" + template + ".html' N'EXISTE PAS!";
            }
        } catch (Exception e) {
            return "Erreur lors de la vérification: " + e.getMessage();
        }
    }
}