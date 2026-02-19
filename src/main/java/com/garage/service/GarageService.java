package com.garage.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.garage.model.Devis;
import com.garage.model.Garage;
import com.garage.repository.GarageRepository;

@Service
public class GarageService {

	@Autowired
	private GarageRepository garageRepository;

	public Garage findById(Long id) {
		return garageRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("Garage non trouvé avec l'ID: " + id));
	}

	public List<Garage> findAllGarages() {
		return garageRepository.findAll();
	}
	
	 public Garage save(Garage garage) {
	        return garageRepository.save(garage);
	    }

}