package com.garage.api;

import com.garage.dto.devis.DevisDTO;
import com.garage.model.Devis;
import com.garage.model.Garage;
import com.garage.model.User;
import com.garage.service.DevisService;
import com.garage.service.GarageService;
import com.garage.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devis")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class DevisRestController {
	
	@Autowired
    private DevisService devisService;

    @Autowired
    private UserService userService;

    @Autowired
    private GarageService garageService;

    // ===============================
    // GET - Liste des devis utilisateur connecté
    // ===============================
    @GetMapping
    public List<DevisDTO> getUserDevis(Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        List<Devis> devisList = devisService.findByUser(user);

        return devisList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // ===============================
    // POST - Création devis
    // ===============================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DevisDTO> createDevis(
            @RequestParam Long clientId,
            @RequestParam Long garageId,
            @RequestParam String description,
            @RequestParam BigDecimal montant,
            @RequestParam(required = false) MultipartFile document) {

        try {
            User client = userService.findById(clientId);
            Garage garage = garageService.findById(garageId);

            Devis devis = new Devis();
            devis.setDescription(description);
            devis.setMontant(montant);
            devis.setDateCreation(LocalDate.now());
            devis.setStatut(Devis.StatutDevis.EN_ATTENTE);
            devis.setUser(client);
            devis.setGarage(garage);

            // Upload fichier
            if (document != null && !document.isEmpty()) {

                String uploadDir = "uploads/devis/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = System.currentTimeMillis() + "_" + document.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                Files.copy(document.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                devis.setDocumentNom(document.getOriginalFilename());
                devis.setDocumentPath(filePath.toString());
                devis.setDocumentType(document.getContentType());
            }

            Devis saved = devisService.save(devis);

            return ResponseEntity.ok(convertToDTO(saved));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===============================
    // GET - Télécharger document
    // ===============================
    @GetMapping("/{id}/document")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {

        Optional<Devis> devisOpt = devisService.findById(id);

        if (devisOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Devis devis = devisOpt.get();

        try {
            Path path = Paths.get(devis.getDocumentPath());
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(devis.getDocumentType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + devis.getDocumentNom() + "\"")
                        .body(resource);
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.notFound().build();
    }

    // ===============================
    // Conversion Entity → DTO
    // ===============================
    private DevisDTO convertToDTO(Devis devis) {

        DevisDTO dto = new DevisDTO();

        dto.setId(devis.getId());
        dto.setDescription(devis.getDescription());
        dto.setMontant(devis.getMontant());
        dto.setDateCreation(devis.getDateCreation());
        dto.setStatut(devis.getStatut().name());

        dto.setClientId(devis.getUser().getId());
        dto.setClientUsername(devis.getUser().getUsername());

        dto.setGarageId(devis.getGarage().getId());
        dto.setGarageName(devis.getGarage().getName());

        dto.setDocumentNom(devis.getDocumentNom());

        return dto;
    }

}
