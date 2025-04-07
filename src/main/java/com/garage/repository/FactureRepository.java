package com.garage.repository;

import com.garage.model.Facture;
import com.garage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {
    List<Facture> findByUser(User user);
}