package com.garage.service;

import com.garage.model.Facture;
import com.garage.model.User;
import com.garage.repository.FactureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
		// TODO Auto-generated method stub
		return factureRepository.findById(id);
	}
}