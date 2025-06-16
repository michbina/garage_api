package com.garage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.garage.model.Garage;
import com.garage.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
 // Ajouter cette méthode à votre UserRepository
    List<User> findByRole(String role);
    
    List<User> findByGaragesIn(List<Garage> garages);
}