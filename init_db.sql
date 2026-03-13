CREATE DATABASE IF NOT EXISTS univ_scheduler;
USE univ_scheduler;

-- Table des bâtiments
CREATE TABLE IF NOT EXISTS batiments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    localisation VARCHAR(200) NOT NULL,
    nombre_etages INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des salles (avec équipements)
CREATE TABLE IF NOT EXISTS salles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50) UNIQUE NOT NULL,
    capacite INT NOT NULL,
    type VARCHAR(20) NOT NULL,
    batiment VARCHAR(50) NOT NULL,
    etage VARCHAR(20) NOT NULL,
    videoprojecteur BOOLEAN DEFAULT FALSE,
    tableau_interactif BOOLEAN DEFAULT FALSE,
    climatisation BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des cours (avec groupe)
CREATE TABLE IF NOT EXISTS cours (
    id INT AUTO_INCREMENT PRIMARY KEY,
    matiere VARCHAR(100) NOT NULL,
    enseignant VARCHAR(100) NOT NULL,
    classe VARCHAR(50) NOT NULL,
    groupe VARCHAR(50) DEFAULT '',
    date_debut DATETIME NOT NULL,
    duree INT NOT NULL,
    salle_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (salle_id) REFERENCES salles(id) ON DELETE CASCADE,
    INDEX idx_classe (classe),
    INDEX idx_enseignant (enseignant),
    INDEX idx_salle (salle_id),
    INDEX idx_date (date_debut)
);

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    login VARCHAR(50) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(100) NOT NULL,
    role ENUM('ADMIN','GESTIONNAIRE','ENSEIGNANT','ETUDIANT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);






-- DONNÉES DE TEST


-- Bâtiments
INSERT IGNORE INTO batiments (nom, localisation, nombre_etages) VALUES
('Bâtiment A', 'Campus principal - Bloc A', 3),
('Bâtiment B', 'Campus principal - Bloc B', 2),
('Bâtiment C', 'Campus secondaire', 1);

-- Salles avec équipements
INSERT IGNORE INTO salles (numero, capacite, type, batiment, etage, videoprojecteur, tableau_interactif, climatisation) VALUES
('A101', 50, 'TD', 'Bâtiment A', '1er étage', TRUE,  FALSE, TRUE),
('A102', 30, 'TP', 'Bâtiment A', '1er étage', TRUE,  TRUE,  FALSE),
('A201', 100,'Amphi','Bâtiment A','2e étage',  TRUE,  TRUE,  TRUE),
('B101', 45, 'TD', 'Bâtiment B', '1er étage', FALSE, FALSE, FALSE),
('B102', 60, 'TP', 'Bâtiment B', '1er étage', TRUE,  FALSE, TRUE),
('C101', 25, 'TD', 'Bâtiment C', '1er étage', FALSE, FALSE, FALSE);

-- Cours
INSERT IGNORE INTO cours (matiere, enseignant, classe, groupe, date_debut, duree, salle_id) VALUES
('Mathématiques',       'Martin Jean',    'L2-Informatique', 'Groupe A', '2026-03-16 08:00:00', 90,  1),
('Programmation Java',  'Dupont Paul',    'L2-Informatique', 'Groupe A', '2026-03-16 10:00:00', 120, 2),
('Base de Données',     'Bernard Alice',  'L2-Informatique', 'Groupe B', '2026-03-16 14:00:00', 90,  3),
('Algorithmes',         'Leclerc Marc',   'L2-Informatique', 'Groupe A', '2026-03-17 09:00:00', 120, 1),
('Réseaux',             'Thomas Sophie',  'L2-Informatique', 'Groupe B', '2026-03-17 14:00:00', 90,  4),
('Mathématiques',       'Martin Jean',    'L1-Informatique', 'Groupe A', '2026-03-18 08:00:00', 90,  5);

-- Utilisateurs
INSERT IGNORE INTO utilisateurs (nom, prenom, login, mot_de_passe, role) VALUES
('Admin',    'Système',  'admin',        'admin123', 'ADMIN'),
('Diallo',   'Ibrahima', 'gestionnaire', 'gest123',  'GESTIONNAIRE'),
('Martin',   'Jean',     'enseignant',   'ens123',   'ENSEIGNANT'),
('Ndiaye',   'Fatou',    'etudiant',     'etu123',   'ETUDIANT');
