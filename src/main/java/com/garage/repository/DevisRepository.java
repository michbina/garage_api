package com.garage.repository;

import com.garage.model.Devis;
import com.garage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DevisRepository extends JpaRepository<Devis, Long> {
    List<Devis> findByUser(User user);
}