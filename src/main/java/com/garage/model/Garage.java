package com.garage.model;

import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "garages")
public class Garage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	private String location; // optionnel, si besoin

	@OneToMany(mappedBy = "garage", cascade = CascadeType.ALL)
	private List<Facture> factures;

	@OneToMany(mappedBy = "garage", cascade = CascadeType.ALL)
	private List<Devis> devis;

	@ManyToMany(mappedBy = "garages")
	private Set<User> users;

}
