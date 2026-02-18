package com.garage.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garage.model.Facture;
import com.garage.model.User;
import com.garage.repository.FactureRepository;

@Service
public class FactureService {

    @Autowired
    private FactureRepository factureRepository;

    public List<Facture> findByUser(User user) {
        return factureRepository.findByUser(user);
    }
    
    
    public Facture save(Facture facture) {
        return factureRepository.save(facture);
    }


	public Optional<Facture> findById(Long id) {
		return factureRepository.findById(id);
	}
	
	 public Facture getFactureById(Long id) {
	        return factureRepository.findById(id)
	                .orElseThrow(() -> new RuntimeException("Facture non trouvé"));
	    }
}