CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  date_inscription DATE DEFAULT NULL,
  active TINYINT(1) DEFAULT 1,
  first_login TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE (username)
);



CREATE TABLE garages (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  location VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id)
);


CREATE TABLE devis (
  id BIGINT NOT NULL AUTO_INCREMENT,
  date_creation DATE DEFAULT NULL,
  description VARCHAR(255) DEFAULT NULL,
  montant DECIMAL(38,2) DEFAULT NULL,
  signature VARCHAR(10000) DEFAULT NULL,
  statut VARCHAR(50),
  user_id BIGINT DEFAULT NULL,
  document_nom VARCHAR(255) DEFAULT NULL,
  document_path VARCHAR(255) DEFAULT NULL,
  document_type VARCHAR(255) DEFAULT NULL,
  garage_id BIGINT NOT NULL,
  public_id VARCHAR(255) NOT NULL,
  storage_name VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id)
);


CREATE TABLE factures (
  id BIGINT NOT NULL AUTO_INCREMENT,
  date_creation DATE DEFAULT NULL,
  description VARCHAR(255) DEFAULT NULL,
  montant DECIMAL(38,2) DEFAULT NULL,
  user_id BIGINT DEFAULT NULL,
  document_nom VARCHAR(255) DEFAULT NULL,
  document_path VARCHAR(255) DEFAULT NULL,
  document_type VARCHAR(255) DEFAULT NULL,
  garage_id BIGINT NOT NULL,
  public_id VARCHAR(255) NOT NULL,
  storage_name VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (id)
);


CREATE TABLE user_garages (
  garage_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  PRIMARY KEY (garage_id, user_id)
);


-- Ajout des contraintes sur la table devis
ALTER TABLE devis
  ADD CONSTRAINT FK_devis_user FOREIGN KEY (user_id) REFERENCES users(id),
  ADD CONSTRAINT FK_devis_garage FOREIGN KEY (garage_id) REFERENCES garages(id);

-- Ajout des contraintes sur la table factures
ALTER TABLE factures
  ADD CONSTRAINT FK_facture_user FOREIGN KEY (user_id) REFERENCES users(id),
  ADD CONSTRAINT FK_facture_garage FOREIGN KEY (garage_id) REFERENCES garages(id);

-- Ajout des contraintes sur la table user_garages
ALTER TABLE user_garages
  ADD CONSTRAINT FK_user_garage_g FOREIGN KEY (garage_id) REFERENCES garages(id),
  ADD CONSTRAINT FK_user_garage_u FOREIGN KEY (user_id) REFERENCES users(id);
  
-- Remplacer les NULL existants par false (0)
UPDATE users SET first_login = 0 WHERE first_login IS NULL;

ALTER TABLE users 
MODIFY COLUMN first_login TINYINT(1) NOT NULL DEFAULT 0;


-- Initialisation de l'admin.
INSERT INTO users (id, username, password, role, active, first_login)
VALUES (1,'admin','$2a$10$hashbcrypt','ROLE_ADMIN',1,0);




  