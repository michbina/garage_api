package com.garage.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_garages")
public class UserGarage {

	@EmbeddedId
	private UserGarageId id;

	@ManyToOne
	@MapsId("userId")
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@MapsId("garageId")
	@JoinColumn(name = "garage_id")
	private Garage garage;

	// constructeurs, getters, setters
}
