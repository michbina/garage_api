package com.garage.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Transient;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	private String role;
	
	// Méthode pour obtenir le rôle sous forme d'enum
    public Role getRoleEnum() {
        if (role == null || role.isEmpty()) {
            return null; // ou une valeur par défaut
        }
        return Role.valueOf(role);
    }

    // Méthode pour définir le rôle à partir d'un enum
    public void setRoleEnum(Role roleEnum) {
        if (roleEnum != null) {
            this.role = roleEnum.name();
        } else {
            this.role = null;
        }
    }

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Facture> factures;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Devis> devis;

	@Column
	private LocalDate dateInscription = LocalDate.now();

	@Column(nullable = false)
	private Boolean active = true;
	
	@Transient
	private List<Role> roles;
	
	@Column(nullable = false)
	private Boolean firstLogin;

	

}
