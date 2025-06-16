package com.garage.model;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

@Embeddable
public class UserGarageId implements Serializable {
	private Long userId;
	private Long garageId;

	// constructeur, equals, hashCode
}
