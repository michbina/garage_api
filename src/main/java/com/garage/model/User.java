package com.garage.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Transient;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String username;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Facture> factures;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Devis> devis;

	@Column
	private LocalDate dateInscription = LocalDate.now();

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "first_login", nullable = false)
	private boolean firstLogin = false;

	@ManyToMany
	@JoinTable(name = "user_garages", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "garage_id"))
	private List<Garage> garages;

	// ---------------------------------------
	// Propriété pour le binding du formulaire
	@Transient
	private List<Long> garageIds;

	public void setGarageIds(List<Long> garageIds) {
		this.garageIds = garageIds;
	}

	public List<Long> getGarageIds() {
		if (garageIds != null) {
			return garageIds;
		}
		if (garages == null)
			return new ArrayList<>();

		return garages.stream().map(Garage::getId).toList();
	}

}
