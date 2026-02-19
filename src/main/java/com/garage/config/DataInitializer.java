package com.garage.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.garage.model.Devis;
import com.garage.model.Facture;
import com.garage.model.Role;
import com.garage.model.User;
import com.garage.repository.DevisRepository;
import com.garage.repository.FactureRepository;
import com.garage.repository.UserRepository;
import com.garage.service.UserService;

import jakarta.annotation.PostConstruct;

@Configuration
public class DataInitializer {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

	private PasswordEncoder passwordEncoder;

	private final DevisRepository devisRepository;
	private final FactureRepository factureRepository;
	private final UserService userService;

	public DataInitializer(PasswordEncoder passwordEncoder, DevisRepository devisRepository,
			FactureRepository factureRepository, UserService userService) {
		this.passwordEncoder = passwordEncoder;
		this.devisRepository = devisRepository;
		this.factureRepository = factureRepository;
		this.userService = userService;
	}

	@Bean
	public CommandLineRunner initData(UserRepository userRepository, FactureRepository factureRepository,
			DevisRepository devisRepository) {
		return args -> {
			// Créer un utilisateur admin si pas déjà présent
			if (userRepository.findByUsername("admin").isEmpty()) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(passwordEncoder.encode("x?Brzwiy7jc?i!Z$"));
				admin.setRole(Role.ROLE_ADMIN);
				admin.setFactures(new ArrayList<>());
				admin.setDevis(new ArrayList<>());
				userRepository.save(admin);
				logger.info("Compte administrateur créé avec succès");
			}
			// Créer un utilisateur si la base de données est vide
			if (userRepository.count() == 0) {
				User user = new User();
				user.setUsername("client");
				user.setPassword(passwordEncoder.encode("password"));
				user.setRole(Role.ROLE_USER);
				user.setFactures(new ArrayList<>());
				user.setDevis(new ArrayList<>());
				userService.saveUserWithGarages(user);
//				userRepository.save(user);

				// Ajouter quelques factures pour l'utilisateur
				Facture facture1 = new Facture();
				facture1.setDescription("Vidange et changement de filtre");
				facture1.setMontant(new BigDecimal("120.50"));
				facture1.setDateCreation(LocalDate.now().minusDays(15));
				factureRepository.save(facture1);

				Facture facture2 = new Facture();
				facture2.setDescription("Remplacement des plaquettes de frein");
				facture2.setMontant(new BigDecimal("230.00"));
				facture2.setDateCreation(LocalDate.now().minusDays(5));
				factureRepository.save(facture2);

				// Ajouter un devis
				Devis devis = new Devis();
				devis.setDescription("Réparation de la boîte de vitesses");
				devis.setMontant(new BigDecimal("850.00"));
				devis.setDateCreation(LocalDate.now());
				devis.setStatut(Devis.StatutDevis.EN_ATTENTE);
				devisRepository.save(devis);
			}
		};
	}

	@PostConstruct
	public void migrate() {

		devisRepository.findAll().forEach(d -> {
			if (d.getPublicId() == null)
				d.setPublicId(UUID.randomUUID().toString());
		});

		factureRepository.findAll().forEach(f -> {
			if (f.getPublicId() == null)
				f.setPublicId(UUID.randomUUID().toString());
		});

		devisRepository.saveAll(devisRepository.findAll());
		factureRepository.saveAll(factureRepository.findAll());
	}
}