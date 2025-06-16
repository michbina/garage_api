package com.garage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.garage.model.Garage;

public interface GarageRepository extends JpaRepository<Garage, Long> {

}