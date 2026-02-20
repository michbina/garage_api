package com.garage.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.garage.dto.register.RegisterRequest;
import com.garage.model.Garage;
import com.garage.model.Role;
import com.garage.model.User;
import com.garage.repository.GarageRepository;
import com.garage.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GarageRepository garageRepository;
	
	 public UserService(UserRepository userRepository,
             PasswordEncoder passwordEncoder,GarageRepository garageRepository) {
			 this.userRepository = userRepository;
			 this.passwordEncoder = passwordEncoder;
			 this.garageRepository=garageRepository;
}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));

		// Création d'une autorité à partir du rôle
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(user.getRole().name()));

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				authorities);
	}

	public User findByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + username));
	}

	public boolean isUsernameAlreadyTaken(String username) {
		return userRepository.findByUsername(username).isPresent();
	}

	public User registerNewUser(RegisterRequest registerRequest) {
		// Vérifier si les mots de passe correspondent
		if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
			throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
		}

		// Vérifier si le nom d'utilisateur existe déjà
		if (isUsernameAlreadyTaken(registerRequest.getUsername())) {
			throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé");
		}

		// Créer un nouvel utilisateur
		User newUser = new User();
		newUser.setUsername(registerRequest.getUsername());
		newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
		newUser.setRole(Role.ROLE_CLIENT);
		newUser.setFactures(new ArrayList<>());
		newUser.setDevis(new ArrayList<>());
		newUser.setActive(true);
		newUser.setDateInscription(LocalDate.now());

		User savedUser = userRepository.save(newUser);

		// Ajouter un log pour confirmer l'enregistrement
		logger.info("Nouvel utilisateur créé: ID={}, Username={}", savedUser.getId(), savedUser.getUsername());

		return savedUser;
	}

	// Ajouter ces méthodes à votre UserService existant
	public List<User> findAllClients() {
		return userRepository.findByRole(Role.ROLE_CLIENT);
	}

	public User findById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
	}

	public void updatePassword(Long id, String password) {
		User user = userRepository.findById(id).orElseThrow();
        // chiffrer le mot de passe
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        user.setFirstLogin(false);
        saveUserWithGarages(user);
//        userRepository.save(user);
		
	}

	public List<User> findByGaragesIn(List<Garage> garages) {
		
		return userRepository.findByGaragesIn(garages);
	}
	
	public User saveUserWithGarages(User user) {

	    if (user.getGarageIds() != null) {

//	        List<Garage> garages = garageRepository.findAllById(user.getGarageIds());
	        List<Garage> garages = user.getGarages();
	        if (garages == null) garages = new ArrayList<>();

	        user.setGarages(garages);

	    } else {
	        user.setGarages(new ArrayList<>());
	    }

	    return userRepository.save(user);
	}
	
	public User updateUserGarages(Long userId, List<Long> garageIds) {

	    User user = findById(userId);

//	    List<Garage> garages = garageRepository.findAllById(garageIds);
	    List<Garage> garages = user.getGarages();
	    if (garages == null) garages = new ArrayList<>();
	    user.setGarages(garages);

	    return userRepository.save(user);
	}
	
	public List<User> findClientsByGarageIds(List<Long> garageIds) {
	    return userRepository.findByGarages_IdInAndRole(garageIds, Role.ROLE_CLIENT);
	}
}