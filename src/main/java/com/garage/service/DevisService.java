package com.garage.service;

import com.garage.model.Devis;
import com.garage.model.User;
import com.garage.repository.DevisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DevisService {

    @Autowired
    private DevisRepository devisRepository;

    public List<Devis> findByUser(User user) {
        return devisRepository.findByUser(user);
    }
    
    public void validerDevis(Long id, String signature) {
        Devis devis = devisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé"));
        devis.setStatut(Devis.StatutDevis.VALIDE);
        devis.setSignature(signature);
        devisRepository.save(devis);
    }
    
    public Devis save(Devis devis) {
        return devisRepository.save(devis);
    }

	public Optional<Devis> findById(Long id) {
		return devisRepository.findById(id);
	}
}